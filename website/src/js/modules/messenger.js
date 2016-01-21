GW.Modules = GW.Modules || {};
/**
 *  All those messages that show up at the top of
 *  the website? This is what does all that
 */
GW.Modules.Messenger = function () {
   "use strict";

   var options = {
      codes: {
         '426': "Due to heavy volume, Grasswire has been put into Read-Only mode for the time being"
      }
   };

   var setMessage = function(data) {
      if (! _.isUndefined(data.status)) {
         data.messages = getMessageForStatus(data.status);
      }
      if (_.isObject(data.messages) || _.isArray(data.messages) ) {
         $(".js-message-bar .js-messages").html(Handlebars.templates.multiMessage(data));
      } else {
         $(".js-message-bar .js-messages").html(Handlebars.templates.message(data));
      }

      $(".js-message-bar").slideDown(150);
   };

   var flashSetMessage = function(data) {
      Cookies.set("flash_message", data);
   };

   var checkFlashMessages = function() {
      if (! _.isUndefined(Cookies.get("flash_message"))) {
         var data = {
            position: "center",
            messages: Cookies.get("flash_message")
         };
         Cookies.remove("flash_message");
         setMessage(data);
      }
   };

   var getMessageForStatus = function (statusCode) {
      return options.codes[statusCode];
   };

   var hideMessages = function(data) {
      $(".js-message-bar").slideUp(150);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(checkFlashMessages, this);
         Events.subscribe("gw/message", setMessage);
         Events.subscribe("gw/message/flash", flashSetMessage);
         Events.bind("click", ".js-dismiss").to(hideMessages, this);
         return this;
      }
   };

}();
