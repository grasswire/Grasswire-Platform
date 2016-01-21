GW.Modules = GW.Modules || {};
GW.Modules.HideStory = function () {
   "use strict";

   var options = {};

   var setHeight = function(data) {
      var textboxes = $(".js-autofit");
      $.each(textboxes, function(i, textbox) {
         $(textbox).css("height", textbox.scrollHeight);
      })
   };

   var currentBoxAdjustHeight = function(data) {
      var rawElement = data.eventElement.get(0);

      data.eventElement.css("height", 0);
      data.eventElement.css("height", rawElement.scrollHeight);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(setHeight, this);
         Events.bind("typing", ".js-autofit").to(currentBoxAdjustHeight, this);
         Events.bind("resize").to(setHeight);

         return this;
      }
   };

}();
