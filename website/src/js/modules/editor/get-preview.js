GW.Modules = GW.Modules || {};
GW.Modules.GetPreview = function () {
   "use strict";

   var options = {
      contentInput: ".js-preview-item",
      previewArea: ".js-preview-box",
      contentArea: ".js-scrape-preview",
      delayTimer: "",
      delay: 1500
   };

   var responseCleanup = function(response) {
      response.image = response.canonicalUrl == "twitter.com" ? undefined : response.image;
      response.description = s.trim(response.description);
      response.title = s.trim(response.title);

      return response;
   };

   var renderPreview = function(content, parent) {
      content = Utilities.prependProtocol(content);
      clearInterval(options.delayTimer);

      if (Utilities.isUrl(content)) {
         options.delayTimer = setTimeout(function() {
            AjaxRoute.as("get")
               .to(jsRoutes.controllers.AjaxController.linkPreview(content).url)
               .on({
                  success: displayPreview,
                  fail: displayErrorForm
               }, this, {parentEl: parent, content: content});
         }, options.delay);
      }
   };

   var displayPreview = function(response) {
      $("input", response.parentEl).val(response.content);
      response.parentEl.find(".js-add-new-success").show();
      response.parentEl.find(".js-add-new-error").hide();
      var sibling = response.parentEl.next(),
           sanitizedResponse = responseCleanup(response);

      sibling.find('.js-scrape-preview').html(Handlebars.templates.previewPost(sanitizedResponse));
      sibling.show();
      Events.publish("previewLoaded", {
         parent: response.parentEl,
         type: 'item'
      });
   };

   var displayErrorForm = function(response) {
      var url = response.parentEl.find(".js-preview-item").val();
      $(".js-new-item-url", response.parentEl).val(url);

      response.parentEl.find(".js-add-new-success").hide();
      response.parentEl.find(".js-add-new-error").show();
      response.parentEl.find(".js-error").show();
      Events.publish("unableToLoadPreview", {
         parent: response.parentEl,
         type: 'item-error'
      });
   };

   var getPreviewByCP = function(data) {
      var parent = data.eventElement.closest(".js-add-to-story-block"),
          content = $("input", parent).val();

      renderPreview(content, parent);
   };

   var getPreviewByKey = function(data) {
      var parent = data.eventElement.closest(".js-add-to-story-block"),
          content = $("input", parent).val();

      renderPreview(content, parent);
   };

   var getPreviewByCPFail = function(data) {
      var parent = data.eventElement.closest(".js-add-to-story-block"),
          content = $("input.js-new-item-url", parent).val();

      renderPreview(content, parent);
   };

   var getPreviewByKeyFail = function(data) {
      var parent = data.eventElement.closest(".js-add-to-story-block"),
          content = $("input.js-new-item-url", parent).val();

      renderPreview(content, parent);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("paste", options.contentInput).to(getPreviewByCP, this);
         Events.bind("typing", options.contentInput).to(getPreviewByKey, this);
         Events.bind("paste", ".js-new-item-url").to(getPreviewByCPFail, this);
         Events.bind("typing", ".js-new-item-url").to(getPreviewByKeyFail, this);

         return this;
      }
   };

}();
