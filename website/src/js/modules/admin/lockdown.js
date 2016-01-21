GW.Modules = GW.Modules || {};
GW.Modules.Lockdown = function() {
   "use strict";

   var op = {
      lockButton: ".js-toggle-lockdown"
   }

   var checkLockdownStatus = function() {
      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.lockdownStatus().url)
         .on({
           complete: updateLockdownButton
         }, this);
   };

   var updateLockdownButton = function(response) {
      if (response.locked) {
         $(op.lockButton).text("Unlock Grasswire").attr("data-current-locked-status", "true");
      } else {
         $(op.lockButton).text("Lock Grasswire").attr("data-current-locked-status", "false");
      }
   };

   var toggleLockdown = function(data) {
      var locked = data.eventElement.data("current-locked-status") ? false : true;

      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.lockdown(locked).url)
         .on({
            complete: lockdownComplete
         }, this);
   };

   var lockdownComplete = function(response) {
      location.reload(true);
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("load").where("body[class]", "admin").to(checkLockdownStatus, this);
         Events.bind("click", op.lockButton).to(toggleLockdown, this);

         return this;
      }
   };
}();
