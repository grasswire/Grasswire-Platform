GW.Modules = GW.Modules || {};
GW.Modules.HideStoryItem = function () {
   "use strict"

   var options = {
      hideButton: ".js-hide-story-item"
   };

   var hideStoryItem = function(data) {
      var hideMe = {
         storyId: parseInt(data.eventElement.data("story-id")),
         linkId: parseInt(data.eventElement.data("element-id")),
         thumbnail: undefined,
         title: undefined,
         description: undefined,
         hidden: true
      };

      AjaxRoute.as("put", true)
         .to(jsRoutes.controllers.AjaxController.editLink(hideMe.storyId, hideMe.linkId, hideMe.thumbnail, hideMe.title, hideMe.description, hideMe.hidden).url)
         .on({
            complete: itemIsHidden
         }, this, {element: data.eventElement});
   };

   var itemIsHidden = function(response) {
      response.element.parent().fadeOut(150);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", options.hideButton).to(hideStoryItem, this);

         return this;
      }
   };

}();
