GW.Modules = GW.Modules || {};
GW.Modules.WelcomeMessage = function() {
   "use strict";

   var displayAboutMessage = function() {
      if (_.isUndefined(Cookies.get('welcome_message'))) {
         Events.publish("gw/message", {
            messages: "Grasswire is powered by the world’s largest open newsroom. <a href='//grasswire.com/story/15/FAQ' class='message-bar__links'>Learn more</a>.",
            position: "center"
         });
         Cookies.set('welcome_message', true, { expires: 365 });
      }

   };

   return {
      init: function() { return this; },
      events: function() {
         //Events.bind("load").where("body[data-is-logged-in]", "false").where("body[class]", "reader").to(displayAboutMessage, this);

         return this;
      }
   };
}();
