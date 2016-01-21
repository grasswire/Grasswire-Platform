var Binder = (function() {

   return {
      asLoad: function(eo) {
         if (eo.bindEvent === "unload") {
            $(document).unload(function() {
               PubSub.publish(eo.asEventName, eo.userData);
            });

            return false;
         }

         if (eo.context == window) {
            $(window).load(function() {
               PubSub.publish(eo.asEventName, eo.userData);
            });

            return false;
         }

         $(document).ready(function() {
            PubSub.publish(eo.asEventName, eo.userData);
         });

         return false;
      },

      asKeyboard: function(eo) {
         var keyType = (eo.bindEvent === "key" || eo.bindEvent === "keydown") ? "keydown" : eo.bindEvent;
         $(document).on(keyType, eo.selector, function(e) {
            var charCode = (e.which ? e.which : e.keyCode);
            _.each(eo.keyPress, function(element, index, list){
               if (element === charCode) {
                  PubSub.publish(eo.asEventName, _.extend({
                     eventElement: $(eo.selector +":focus") || null
                  }, eo.userData));
               }
            });
         });
      },

      asTyping: function(eo) {
         $(document).on("keyup", eo.selector, function(e) {
            PubSub.publish(eo.asEventName, _.extend({
               eventElement: $(eo.selector +":focus") || null
            }, eo.userData));
         });
      },

      asDocument: function(eo) {
         $(document).on(eo.bindEvent, eo.selector, function(e) {

            PubSub.publish(eo.asEventName, _.extend({
               eventElement: $(this)
            }, eo.userData));

            e.preventDefault();
         });

      },

      asWindow: function(eo) {
         $(window).on(eo.bindEvent, eo.selector, function(e) {
            PubSub.publish(eo.asEventName, _.extend({
               eventElement: $(this)
            }, eo.userData));
            e.preventDefault();
         });

         return false;
      },

      asMouse: function(eo) {
         return this.asDocument(eo);
      },

      asForm: function(eo) {
         return this.asDocument(eo);
      },

      generic: function(eo) {
         $(document).on(eo.bindEvent, eo.selector, function(e) {
            PubSub.publish(eo.asEventName, _.extend({
               eventElement: $(this)
            }, eo.userData));
         });
      }

    };
}());
