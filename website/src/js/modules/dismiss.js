GW.Modules = GW.Modules || {};
GW.Modules.Dismiss = function() {
   "use strict";

   var dismissError = function(data) {
      data.eventElement.closest(".js-error").fadeOut(150);
   };

   var dismissNewsletter = function(data) {
      data.eventElement.closest(".js-digest").fadeOut(150);
   };
   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-dismiss-error").to(dismissError, this);
         Events.bind("click", ".js-dismiss-newsletter").to(dismissNewsletter, this);

         return this;
      }
   };
}();
