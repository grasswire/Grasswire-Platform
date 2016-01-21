GW.Modules = GW.Modules || {};
GW.Modules.Revert = function() {
   "use strict";

   var op = {
      headline: ".js-revert-headline",
      summary:  ".js-revert-summary",
      name:     ".js-revert-name",
      hidden:   ".js-revert-hidden",
      image:    ".js-revert-image"
   }

   var revert = function(data) {
      var revisedList = $('.js-revision-row');

      if (revisedList.length > 0) {
         var revision = revisedList.first(),
         revertGroup = {
            revertHeadline: $(op.headline, revision).hasClass("js-revision-item"),
            revertSummary: $(op.summary, revision).hasClass("js-revision-item"),
            revertName: $(op.name, revision).hasClass("js-revision-item"),
            revertHidden: $(op.hidden, revision).hasClass("js-revision-item"),
            revertCoverPhoto: $(op.image, revision).hasClass("js-revision-item")
         };

         $.when(
            $.ajax({
               type: "POST",
               url: jsRoutes.controllers.AdminController.revertStory(revision.data("story-id"), revision.data("id")).url,
               dataType: "json",
               contentType: "application/json",
               processData: false,
               data: JSON.stringify(revertGroup)
            })
         )
         .done(function(response) {
            revision.removeClass("js-revision-row");
            GW.Helpers.logger.message(response);
            revert(data);

            return false;
         });
      } else {
         window.location.replace("/admin/change_logs");
      }
   };

   var selectRevision = function(data) {
      var row = data.eventElement.closest(".js-revision");

      if (data.eventElement.hasClass("js-revision-item")) {
         data.eventElement.removeClass("js-revision-item").css("background", "inherit");

         if ($(".js-revision-item", row).length <= 0) {
            row.removeClass("js-revision-row");
         }
      } else {
         row.addClass("js-revision-row");
         data.eventElement.addClass("js-revision-item").css("background", "#ccc");
      }
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("click", ".js-revertable").to(selectRevision, this);
         Events.bind("click", ".js-revert-to").to(revert, this);

         return this;
      }
   };
}();
