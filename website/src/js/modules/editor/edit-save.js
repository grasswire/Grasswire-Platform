GW.Modules = GW.Modules || {};
GW.Modules.EditSave = function () {
   "use strict";

   var options = {
      saveButton: ".js-edit-save",
      editPhoto: ".js-edit-image",
      editHeadline: ".js-edit-headline",
      editSummary: ".js-edit-summary",
      editTag: ".js-edit-tag",
      editDirty: ".js-dirty",
      editClean: ".js-clean"
   };

   var cleanFeaturedData = function(changed) {
      var updates = {
         id:       parseInt(changed.data("story-id")),
         name:     getValue(changed, options.editTag, false),
         summary:  getValue(changed, options.editSummary, false),
         coverPhoto:    getValue(changed, options.editPhoto, true),
         headline: getValue(changed, options.editHeadline, false),
         hidden:   undefined
      };

      if (! _.isUndefined(updates.headline)) {
         updates.headline = GW.Helpers.titleize(updates.headline);
      }

      if (! _.isUndefined(updates.name)) {
         updates.name = GW.Helpers.titleize(updates.name);
      }

      return updates;
   };

   var cleanItemData = function(changed) {

      var item      = changed.hasClass("is-dirty-new-item-error") ? changed : changed.next(),
          url       = item.find(".js-new-item-url").val(),
          canonical = item.find(".js-new-item-canonical-url").val(),
          newItem = {
             storyId:      parseInt(changed.data('story-id')),
             url:          s.startsWith(url, "https://") ? url : s.startsWith(url, "http://") ? url : "http://"+url,
             canonicalUrl: s.startsWith(canonical, "https://") ? canonical.replace("https://", "") : s.startsWith(canonical, "http://") ? canonical.replace("http://", "") : canonical,
             thumbnail:    getValue(item, '.js-new-item-image', true),
             title:        getValue(item, '.js-new-item-title'),
             description:  getValue(item, '.js-new-item-description')
          };

      return newItem;
   };

   var persistFeatured = function(data) {
      var allChanged = $(".is-dirty");
      if (allChanged.length > 0) {
         var updates = cleanFeaturedData(allChanged.first());
         allChanged.first().removeClass("is-dirty");

         AjaxRoute.as("PUT")
                  .to(jsRoutes.controllers.AjaxController.editStory(updates.id).url, _.omit(updates, 'id'))
                  .when({
                     success: function() {
                        persistFeatured(data);
                     }
                  });
      } else {
         persistItems(data);
      }

   };

   var persistItems = function(data) {
      var allChanged = $(".is-dirty-new-item, .is-dirty-new-item-error");

      if (allChanged.length > 0) {
         var newItem = cleanItemData(allChanged.first());
         allChanged.first().removeClass("is-dirty-new-item").removeClass("is-dirty-new-item-error");

         AjaxRoute.as("POST", true)
                  .to(jsRoutes.controllers.AjaxController.createLink(newItem.url, newItem.canonicalUrl, newItem.thumbnail, newItem.title, newItem.description, newItem.storyId).url)
                  .when({
                     success: function() {
                        persistItems(data);
                     }
                  });

      } else {
         Events.publish("gw/message/flash", "Your edits are now live");

         window.location.href = $(".js-edit-save").first().attr("href");
      }

   };

   var getValue = function(el, tag, checkImage) {
      var item = el.find(tag);

      if (checkImage && item.data("invalid-image") ) {
         return undefined;
      }

      if (item.val() == item.data("current") || s.isBlank(item.val())) {
         return undefined;
      }

      return s.trim(item.val());
   };


   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-edit-save").to(persistFeatured, this);

         return this;
      }
   };

}();
