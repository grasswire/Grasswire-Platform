GW.Modules = GW.Modules || {};
/**
 *  Code that handles the opening/closing of various
 *  modals around the site.
 */
GW.Modules.Modal = function () {
   "use strict";

   var options = {
      el: ".js-modal",
      fadeTime: 250,
      currentModal: ".js-open-modal-window",
      offsetClass: ".js-offset",
      bg: ".js-modal-background"
   };

   var autoOpenModal = function(data) {
      var urlParams = GW.Helpers.getQueryParams(window.location.search.substring(1));
      if (_.isUndefined(urlParams.modal)) return false;

      open({modalContentId: urlParams.modal});
   };

   var appendMessage = function(message) {
      if (! _.isUndefined(message)) {
         $(".js-modal-message").html(message);
      }
   };

   var open = function(data) {
      var modalContentId = data.modalContentId || data.eventElement.data('modal-id') || data.eventElement.closest(".js-mediabox-tile").data("content-id"),
          modal = $('.js-modal[data-modal-content-id="'+modalContentId+'"]'),
          triggerOnMobile = modal.data('trigger-on-mobile');

      data.eventElement.addClass("js-triggered-modal");
      $("body").css("overflow-y", "hidden");

      if (($(window).outerWidth(true) > GW.Config.triggerMobileSize || triggerOnMobile) && modal.length > 0) {
         appendMessage(data.message);
         options.modalBackground.fadeIn(options.fadeTime);
         calculatePosition(modal, true);

         modal.addClass(GW.Helpers.toClassString(options.currentModal)).addClass("flexme");

      }
   };

   var close = function(data) {
      var openModal = $(options.currentModal);

      if (openModal.length > 0) {
         $(".js-triggered-modal").removeClass("js-triggered-modal");
         $("body").css("overflow-y", "scroll");

         openModal.removeClass(GW.Helpers.toClassString(options.currentModal)).removeClass("flexme");
         options.modalBackground.fadeOut(options.fadeTime);

         closeVideoPlayer(openModal);
      }
   };

   var closeVideoPlayer = function(modal) {
      var iframe = modal.find('iframe');
      if (iframe.length > 0) {
         var tempSrc = iframe.attr('src');

         iframe.attr('src', '');
         iframe.attr('src', tempSrc);
      }
   };

   var recalculatePosition = function(data) {
      calculatePosition($(options.currentModal), true);
   };

   var calculatePosition = function(modal, useOffset) {
      var currentModal = $(options.el);

      if (! _.isUndefined(modal)) {
         currentModal = modal;
      }

      var modalWidth = currentModal.outerWidth(true),
          modalHeight = currentModal.outerHeight(true),
          screenWidth = $(window).outerWidth(true),
          screenHeight = $(window).outerHeight(true);

      currentModal.css({
         top: 0,
         left: 0
      });

      if (screenWidth > GW.Config.triggerMobileSize) {
         currentModal.css({
            top: (screenHeight / 2) - (modalHeight / 2),
            left: (screenWidth / 2) - (modalWidth / 2)
         });
      }
   };

   return {
      init: function() {
         options.modalBackground = $(options.bg);
         return this;
      },
      events: function() {
         Events.bind("key", "body", [27]).to(close, this);
         Events.bind("click", '.js-modal-close, .js-modal-background').to(close, this);
         Events.subscribe("GW/dismiss-modal", close);
         Events.bind("resize").to(recalculatePosition, this);
         Events.bind("click", ".js-modal-open").to(open, this);
         Events.bind("load").to(autoOpenModal, this);
      }
   };
}();

