GW.Modules = GW.Modules || {};
GW.Modules.ProfileHover = function() {
   "use strict";

   var displayMiniProfile = function(data) {
      data.eventElement.next().fadeIn(100);
   };

   var hideMiniProfile = function(data) {
      data.eventElement.find('.js-profile-hover').next().fadeOut(100);
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("mouseenter", ".js-profile-hover").to(displayMiniProfile, this);
         Events.bind("mouseleave", ".js-profile-hover-parent").to(hideMiniProfile, this);

         return this;
      }
   };
}();
