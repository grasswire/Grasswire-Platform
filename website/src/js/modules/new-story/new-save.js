GW.Modules = GW.Modules || {};
/**
 *  Save functions for a new story.
 */
GW.Modules.NewSave = function () {
   "use strict";

   var options = {
      showNewPhoto: ".js-show-new-image",
      saveButton: ".js-new-save",
      addNewForm: ".js-validate-new-story"
   };

   var saveNewStory = function (data) {

      if ($(options.addNewForm).valid()) {
         var story = {
            name:       GW.Helpers.titleize($(".js-new-name").val()),
            summary:    $(".js-new-summary").val() || null,
            coverPhoto: $(".js-show-new-image").val() || null,
            headline:   GW.Helpers.titleize($(".js-new-headline").val()) || null
         };

         AjaxRoute.as("post")
            .to(jsRoutes.controllers.AjaxController.createStory().url, story)
            .on({
               success: newStorySaved,
               fail: failedSave
            });
      }
   };

   var newStorySaved = function(response) {
      Events.publish("gw/message/flash", "Your new story is now live");
      window.location.href = "/edit#feature-"+ response.id;
   };

   var failedSave = function(response) {
      GW.Helpers.logger.message(response);
   };

   var showNewPhoto = function(data) {
      var newImageText = $(".js-show-new-image").val();

      if (!s.isBlank(newImageText) && GW.Helpers.isImage(newImageText)) {
         var oldImage = $(".js-show-new-current-image").attr("src");
         $(".js-show-new-current-image").attr("src", newImageText).attr("data-old-image", oldImage).attr("data-invalid-image", "false");

         $(".js-show-new-current-image").error(function() {
            $(this).attr("data-invalid-image", "true").attr("src", oldImage);
         });
      }
   };

   var focusHighlight = function(data) {
      data.eventElement.select();
   };

   return {
      init: function() {
         jQuery.validator.setDefaults({
            debug: false
         });

         jQuery.validator.addMethod("notEqual", function(value, element, param) {
            return this.optional(element) || value.toLowerCase() != param.toLowerCase();
         }, "Please specify a different (non-default) value");

         $(options.addNewForm).validate({
            onkeyup: false,
            onfocusout: false,
            showErrors: function(errorMap, errorList) {
               if (errorList.length > 0) {
                  Events.publish("gw/message", {
                     messages: errorList,
                     position: "left"
                  });
               }
            },
            rules: {
               url:      { required: true, url: true },
               title:    { required: true, maxlength: 30, notEqual: "Add a title" },
               summary:  { required: true, maxlength: 2500, notEqual: "Add some facts" },
               headline: { required: true, maxlength: 76, notEqual: "Add a new headline" }
            },
            messages: {
               title:    {
                  required: "Please specify a title",
                  maxLength: "You've gone over the character limit of 30 characters please edit your title",
                  notEqual: "Please specify a title"
               },
               summary:  {
                  required: "Please specify a summary",
                  maxLength: "You've gone over the character limit of 2500 characters please edit your summary",
                  notEqual: "Please specify a summary",
               },
               headline: {
                  required: "Please specify a headline",
                  maxlength: "You've gone over the character limit of 76 characters please edit your headline",
                  notEqual: "Please specify a headline"
               },
               url: {
                  required: "Please specify an image--images can be .gif, .jpg, .jpeg or .png",
                  notEqual: "Please specify an image--images can be .gif, .jpg, .jpeg or .png"
               }
            }

         });

         return this;
      },
      events: function() {
         Events.bind("click", options.saveButton).to(saveNewStory, this);
         Events.bind("typing", options.showNewPhoto).to(showNewPhoto, this);

         Events.bind("focusin", ".js-new-summary").to(focusHighlight, this);
         Events.bind("focusin", ".js-show-new-image").to(focusHighlight, this);
         Events.bind("focusin", ".js-new-name").to(focusHighlight, this);
         Events.bind("focusin", ".js-new-headline").to(focusHighlight, this);

         return this;
      }
   };

}();
