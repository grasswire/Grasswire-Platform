GW.Modules = GW.Modules || {};
/**
 *  Code to display a Twitter widget on the /live page
 */
GW.Modules.LiveTweets = function() {
   "use strict";
   var op = {};

   var getContainerHeight = function() {
      if ($(window).outerWidth(true) > 640) {
         $('.js-tweet-wrapper').outerHeight($(".js-livestream-block").outerHeight(true));
      } else {
         $('.js-tweet-wrapper').height(480).css("overflow", "scroll");
      }

   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("load").where("body[class]", "live").to(getContainerHeight, this);
         Events.bind("resize").where("body[class]", "live").to(getContainerHeight, this);
         return this;
      }
   };
}();
