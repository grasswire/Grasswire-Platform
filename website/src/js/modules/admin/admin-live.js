GW.Modules = GW.Modules || {};
GW.Modules.AdminLive = function () {
   "use strict";

   var options = { };

   var saveNewLive = function(data) {
      var data = {
         title: $(".js-live-title").val(),
         videoEmbedUrl: $(".js-live-embed").val()
      }

      AjaxRoute.as("post")
         .to(jsRoutes.controllers.AdminController.createLivePage().url, data)
         .on({
            success: success,
            fail: fail
         }, this);
   };

   var success = function(response) {
      Events.publish("gw/message", {
         messages: "Success! Your event is now on the air!",
         position: "center"
      });
   };

   var fail = function(response) {
      Events.publish("gw/message", {
         messages: "Oops! There has been an error trying to get your event online, please check the console",
         position: "center"
      });
      GW.Helpers.logger.log("ERROR: ", "Error with the save  in GW.Modules.AdminLive.js", JSON.stringify(response));
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-live-button-start").to(saveNewLive, this);

         return this;
      }
   };

}();
