GW.Modules = GW.Modules || {};
/**
 *  Adjusts the character count text above each textbox
 *
 *  NOTE: this just does the adjustment of the text, the
 *  thing that *really* checks the character counter
 *  is done during validation via maxlength. Check
 *  out new-save.js to adjust the validation
 */
GW.Modules.CharacterCounter = function() {
   "use strict";

   var op = {};

   var adjustCount = function(data) {
      var maxCount = parseInt(data.eventElement.attr('data-maxcount')),
          currentCount = data.eventElement.val().length,
          currentCountBox = data.eventElement.prev();

      currentCountBox.text(maxCount - currentCount);

      if(maxCount < currentCount) {
         currentCountBox.css('color', '#f00');
      } else {
         currentCountBox.css('color', "");
      }
   };

   return {
      init: function() { return this; },

      events: function() {

         Events.bind("typing", ".js-char-counter").to(adjustCount, this);
         return this;
      }
   };

}();
