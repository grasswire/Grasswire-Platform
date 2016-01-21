GW.Modules = GW.Modules || {};

/**
 *   EmailCapture is called when a new user signs up for GW.
 *   Once they've been returned from Twitter we pop up a
 *   modal and have them give us their email address.
 */
GW.Modules.CaptureEmail = function () {
   "use strict";

   var options = {
      form: ".js-capture-email-form",
      emailField: ".js-signup-email",
      submitButton: ".js-submit-email",
      modalCloseButton: ".js-modal-close",
      captureContent:  "js-email-capture-content",
      captureSuccess: ".js-email-capture-success",
      fadeTime: 250,
      modalCloseDelay: 2500
   };

   var emailCaptureComplete = function(redirectURL) {
      window.location = redirectURL;
      setTimeout(function() {
         $(options.modalCloseButton).trigger("click");
      }, options.modalCloseDelay);
   };

   var validateEmailAddress = function(data) {
      $(options.form).validate({
         onsubmit: false,
         rules: {
            signupEmail: {
               email: true
            }
         },
         messages: {
            signupEmail: {
               email: "The email address you entered is invalid"
            }
         }
      });

      if ($(options.form).valid()) {
         updateEmailField({
            email: $.trim($(options.emailField).val())
         });
      }

      return false;
   };

   var updateEmailField = function(data) {
      var noModalUrl = GW.Helpers.updateQueryParams("modal");
      if (s.isBlank(data.email)) {
         emailCaptureComplete(noModalUrl);
         return false;
      }

      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.updateEmail(data.email).url)
         .on({
           complete: emailCaptured
         }, this);

      var emailCaptured = function(response) {
         $(".js-email-capture-content").fadeOut(options.fadeTime, function() {
            $(".js-email-capture-success").fadeIn(options.fadeTime, function() {
               emailCaptureComplete(noModalUrl);
            });
         });
      };
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", options.submitButton).to(validateEmailAddress, this);

         return this;
      }
   };

}();
