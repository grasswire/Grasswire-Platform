var Utilities = (function(){
   "use strict";

   var generateUUID = function() {
      var d = Date.now();
      var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
         var r = (d + Math.random()*16)%16 | 0;
         d = Math.floor(d/16);
         return (c=='x' ? r : (r&0x3|0x8)).toString(16);
      });

      return uuid;
   };

   return {

      generateEventName: function(asEventName) {
         if (_.isUndefined(asEventName)) {
            return "app/event/" + generateUUID();
         }

         return asEventName;
      },

      isUrl: function(str) {
         var urlRegex = '^(?!(mailto|ftp):)(?:(?:http|https)://)(?:\\S+(?::\\S*)?@)?(?:(?:(?:[1-9]\\d?|1\\d\\d|2[01]\\d|22[0-3])(?:\\.(?:1?\\d{1,2}|2[0-4]\\d|25[0-5])){2}(?:\\.(?:[0-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-4]))|(?:(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)(?:\\.(?:[a-z\\u00a1-\\uffff0-9]+-?)*[a-z\\u00a1-\\uffff0-9]+)*(?:\\.(?:[a-z\\u00a1-\\uffff]{2,})))|localhost)(?::\\d{2,5})?(?:(/|\\?|#)[^\\s]*)?$';
         var url = new RegExp(urlRegex, 'i');
         return str.length < 2083 && url.test(str);
      },

      prependProtocol: function(str) {
         if (! s.startsWith(str, "http://") && ! s.startsWith(str, "https://")) {
            if (s.startsWith(str, "//")) {
               str = "http:" + str;
            } else {
               str = "http://" + str;
            }
         }

         return str;
      }
   };
}());
