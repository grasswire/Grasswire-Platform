GW.Modules = GW.Modules || {};
GW.Modules.Reorder = function () {
   "use strict";

   var options = {
   };

   var buildSortList = function(data) {
      var stories = $(".js-story"),
          storyList = [];

      storyList = _.map(stories, function(item, key, index){
         var el = $(item);
         return {
            id: el.data('story-id'),
            title: el.data('story-name'),
            currentPosition: key
         };
      });

     Events.publish("gw/reorder/list-built", {storyList: storyList});
   };

   var renderList = function(data) {
      $(".js-order-list").html(Handlebars.templates.storyOrder({"stories" : data.storyList}));
      $('.js-sort').sortable({
         onUpdate: updateSort,
         handle: ".js-dragbar"
      });
   };

   var saveOrder = function(data) {
      var stories = $(".js-sort-item"),
          storyList = [];

      storyList = _.map(stories, function(item){
         var el = $(item);
         return {
            storyId: parseInt(el.data('story-id')),
            position: parseInt(el.data('current-position'))
         };
      });

      AjaxRoute.as("put")
         .to(jsRoutes.controllers.AjaxController.editStoryOrdering().url, storyList)
         .on({
            complete: orderUpdated
         }, this);
   };

   var orderUpdated = function(response) {
      location.href = "/";
   };

   var updateSort = function() {
      var storyList = $(".js-sort-item");
      storyList.each(function(index, item) {
         $(item).attr('data-current-position', index);
      });
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(buildSortList, this);
         Events.bind("click", ".js-reorder").to(saveOrder, this);
         Events.subscribe("gw/reorder/list-built", renderList);
      }
   };

}();
