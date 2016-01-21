GW.Modules = GW.Modules || {};
GW.Modules.EditPhoto = function () {
   "use strict";

   var options = {};

   var showNewPhoto = function(data) {
      var parent = data.eventElement.closest(".js-edit-feature"),
          newImageText = $(".js-edit-image", parent).val();

      if (!s.isBlank(newImageText) && GW.Helpers.isImage(newImageText)) {
         if (newImageText != $(".js-edit-current-image", parent).attr("src")) {
            var oldImage = $(".js-edit-current-image", parent).attr("src");
            $(".js-edit-current-image", parent).attr("src", newImageText).attr("data-old-image", oldImage).attr("data-invalid-image", "false");

            $(".js-edit-current-image", parent).error(function() {
               $(this).attr("data-invalid-image", "true").attr("src", oldImage);
               return false;
            });

            Events.publish("triggerDirty", {
               eventElement:  parent,
               type: "feature"
            });
         }

      }
   };

   var revertPhotoChange = function(data) {
      var parent = data.eventElement.closest(".js-image-container"),
          revertTo = parent.find("input").data("current");

      $(".js-edit-image", parent).val(revertTo);

      Events.publish("revertPhoto", {
         eventElement: $(".js-edit-image", parent)
      });
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-edit-cancel-image-change").to(revertPhotoChange, this);

         Events.bind("typing", ".js-edit-image").to(showNewPhoto, this);
         Events.subscribe('revertPhoto', showNewPhoto, this);

         return this;
      }
   };

}();
