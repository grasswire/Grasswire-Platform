GW.Modules = GW.Modules || {};
GW.Modules.HideFullStory = function () {
   "use strict";

   var options = {
      fullStory: ".js-story",
      hideButton: ".js-hide-story"
   };

   var hideStory = function() {

      var story = $(".js-triggered-modal").closest(".js-story");

      var hideMe = {
         storyId: parseInt(story.data("story-id"))
      };

      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.hideStory(hideMe.storyId).url)
         .on({
            complete: storyHidden
         }, this, {story: story});
   };

   var storyHidden = function(response) {
      Events.publish("GW/dismiss-modal");
      response.story.fadeOut(150);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-hide-story").to(hideStory, this);

         return this;
      }
   };

}();
