GW.Modules = GW.Modules || {};
GW.Modules.SaveNewDigest = function() {
   "use strict";

   var op = {
      button: ".js-save-new",
      text: ".js-digest-text"
   };

   var saveNew = function() {

      AjaxRoute.as("post")
         .to(jsRoutes.controllers.AdminController.createDigest().url, JSON.parse($(op.text).val()))
         .on({
            success: messageDigestSaved,
            fail: messageDigestFailed
         }, this);
   };

   var messageDigestSaved = function(response) {
      Events.publish("gw/message", {
         messages: "Digest has been saved",
         position: "center"
      });
   };

   var messageDigestFailed = function(response) {
      Events.publish("gw/message", {
         messages: "Oops, we were unable to save the digest at this time.",
         position: "center"
      });

      GW.Helpers.logger.log("ERROR: ", "Error with the digest", response);
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("click", ".js-save-new").to(saveNew, this);

         return this;
      }
   };
}();
