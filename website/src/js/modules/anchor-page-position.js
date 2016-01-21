GW.Modules = GW.Modules || {};
/**
 *  Adds anchor tags to links in the masthead so that way we
 *  can keep the users position synced between various
 *  pages.
 */
GW.Modules.AnchorPagePosition = function () {
   "use strict";

   var options = { };

   var adjustPosition = function(data) {

      if (window.location.hash != "" && window.location.hash != " ") {
         var hash = window.location.hash,
             toHref   = stripOldHref($(".js-edit-mode").attr("href"), hash),
             fromHref = stripOldHref($(".js-back-to-reader").attr("href"), hash),
             updatePosition = $(hash + " .js-image-container");

         if (updatePosition.offset().top > 0) {
            $(window).scrollTop(updatePosition.offset().top - 60);
            $(".js-back-to-reader").attr("href", fromHref.replace("/edit", "/"));
            $(".js-edit-mode").attr("href", toHref);

            Events.publish("gw/sidebar-highlight", {storyId: hash.replace("#feature-", "")});
         }
      } else {
         Events.publish("gw/sidebar-highlight", {storyId: null});
      }

   };

   var stripOldHref = function(currentHref, id) {
      if (! _.isUndefined(currentHref)) {
         var index = currentHref.indexOf("#");

         if (index > 0) {
            currentHref = currentHref.substring(0, index);
         }

         return currentHref +"#"+id.replace("#", "");
      }

      return "";
   };

   var getClosestId = function(data) {
      var storyList = $(".js-story-list, .js-story");

      $.each(storyList, function(index, value){
            var topStory = $(value),
                position = topStory.position().top - ($(window).scrollTop() - 60);

            if (position <= 120 && position >= 0) {
               var toHref   = stripOldHref($(".js-edit-mode").attr("href"), topStory.attr("id")),
                   fromHref = stripOldHref($(".js-back-to-reader").attr("href"), topStory.attr("id"));

               $(".js-back-to-reader").attr("href", fromHref.replace("/edit", "/"));
               $(".js-edit-mode").attr("href", toHref);
               Events.publish("gw/sidebar-highlight", {storyId: topStory.attr("id").replace("feature-", "")});
            }
      });

   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(adjustPosition, window);
         Events.bind("scroll").to(getClosestId, window);
         return this;
      }
   };

}();
