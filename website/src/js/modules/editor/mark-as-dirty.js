GW.Modules = GW.Modules || {};
GW.Modules.MarkAsDirty = function () {
   "use strict";

   var markAsDirty = function(data) {
      if (data.type == "feature") {
         var current = data.eventElement.data("current"),
             parent = data.eventElement.closest('.js-edit-feature');
         if (current != data.eventElement.val()) {
            if (! parent.hasClass("is-dirty")) {
               parent.addClass("is-dirty");
            }
         } else {
            if (parent.hasClass("is-dirty")) {
               parent.removeClass("is-dirty");
            }
         }
      }

      if (data.type == "item-error") {
         if (! data.parent.hasClass("is-dirty-new-item-error")) {
            data.parent.removeClass("is-dirty-new-item");
            data.parent.addClass("is-dirty-new-item-error");
         }
      }

      if (_.isUndefined(data.type) || data.type == "item") {
         if (! data.parent.hasClass("is-dirty-new-item")) {
            data.parent.removeClass("is-dirty-new-item-error");
            data.parent.addClass("is-dirty-new-item");
         }
      }

      Events.publish('pingPublish', {});
   };

   var notifyNeedToPublish = function() {
      var isDirty = $(".is-dirty, .is-dirty-new-item, .is-dirty-new-item-error");
      if (isDirty.length > 0) {
         $(".js-dirty").show();
         $(".js-clean").hide();

         return false;
      }
      $(".js-dirty").hide();
      $(".js-clean").show();
   };


   return {
      init: function() { return this; },
      events: function() {
         Events.subscribe('markAsDirty', markAsDirty, this);
         Events.subscribe('pingPublish', notifyNeedToPublish, this);
         Events.subscribe('previewLoaded', markAsDirty, this);
         Events.subscribe('unableToLoadPreview', markAsDirty, this);
         Events.subscribe('triggerDirty', markAsDirty, this);

         Events.bind("typing", ".js-edit-headline").to(markAsDirty, this, {type: "feature"});
         Events.bind("typing", ".js-edit-tag").to(markAsDirty, this, {type: "feature"});
         Events.bind("typing", ".js-edit-summary").to(markAsDirty, this, {type: "feature"});

         return this;
      }
   };

}();
