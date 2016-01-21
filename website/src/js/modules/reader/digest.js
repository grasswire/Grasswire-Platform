GW.Modules = GW.Modules || {};
/**
 *  Ajax calls to save a users email address to
 *  our mailchimp mailing list.
 */
GW.Modules.Digest = function() {
   "use strict";

   var sendContentToMC = function() {
      $('#mc-form').ajaxChimp({
         url: 'https://graswire.us2.list-manage.com/subscribe/post?u=3ad2895e9613627e104c66f13&id=2d4482fd83',
         callback: function (resp) {
            $(".js-digest-text").html(resp.msg);
            $(".js-digest-input").hide();
            $(".js-digest-button").hide();
            $(".js-digest-label").hide();
         }
      });
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("load").where("body[data-is-logged-in]", "false").where("body[class]", "reader").to(sendContentToMC, this);

         return this;
      }
   };
}();
