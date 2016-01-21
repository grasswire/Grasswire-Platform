GW.Modules = GW.Modules || {};
GW.Modules.CreateDigestEmail = function() {
   "use strict";

   var readyImages = function() {
      var limit = $('.js-digest-limit').val();

      $('.js-digest-next').html(Handlebars.templates.insertFeatureImageUrls({limit: limit})).show();
   };

   var generateEmail = function(data) {
      var limit = $('.js-digest-limit').val();
      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AdminController.generateEmailDigest(limit).url)
         .on({
           success: displayEmail,
           fail: displayError
         }, this, {limit: limit});
   };

   var displayEmail = function(response) {
      for (var i = 0; i < response.limit; i++) {
         response.email = s.replaceAll(response.email, "{{IMAGE_PATH}}-"+i, $(".js-image-url-"+i).val());
      };

      response.email = s.replaceAll(response.email, "{{MESSAGE}}", $(".js-email-message").val());

      $(".js-email-content").val(response.email);
      $(".js-success-email").show();
   };

   var displayError = function(response) {
      Events.publish("gw/message", {
         messages: "Oops! There has been an error trying to generate your email. Chances are your JSON is invalid.",
         position: "center"
      });
      GW.Helpers.logger.log("ERROR: ", "Error with the save  in GW.Modules.CreateDigestEmail.js", JSON.stringify(response));
   };

   return {
      init: function() { return this;},

      events: function() {
         Events.bind("click", ".js-generate-digest-next").to(readyImages, this);
         Events.bind("click", ".js-generate-digest").to(generateEmail, this);

         return this;
      }
   };
}();
