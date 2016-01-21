GW.Modules = GW.Modules || {};
GW.Modules.ShareLink = function() {
   "use strict";


   var generateTwitterPopup = function(data) {
      generatePopup(data.eventElement, "https://twitter.com/intent/tweet?text={{text}}&via=grasswire&url={{url}}");
   };

   var generateFacebookPopup = function(data) {
      generatePopup(data.eventElement, "https://www.facebook.com/sharer/sharer.php?u={{url}}");
   };

   var generateRedditPopup = function(data) {
      generatePopup(data.eventElement, "https://www.reddit.com/submit?url={{url}}&amp;title={{text}};");
   };

   var generatePopup = function(element, link) {
      var url    = element.closest(".js-share-container").data("share-url"),
          text   = element.closest(".js-share-container").data("share-headline"),
          width  = 640,
          height = 480,
          left   = Math.floor((screen.width/2)-(width/2)),
          top    = Math.floor((screen.height/2)-(height/2));

      link = s.replaceAll(link, "{{url}}", url);
      link = s.replaceAll(link, "{{text}}", text);

      window.open(encodeURI(link), 'Share', 'toolbar=no,location=no,directories=no,status=no,menubar=no,scrollbars=no,resizable=no,copyhistory=no,width='+width+',height='+height+',top='+top+',left='+left);
   };

   var generateLink = function(data) {
      var url = data.eventElement.closest(".js-share-container").data("share-url");

      $(".js-copy-link-content").attr("value", url).select();
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("click", ".js-share-popup-twitter").to(generateTwitterPopup, this);
         Events.bind("click", ".js-share-popup-facebook").to(generateFacebookPopup, this);
         Events.bind("click", ".js-share-popup-reddit").to(generateRedditPopup, this);
         Events.bind("click", ".js-share-popup-link").to(generateLink, this);

         return this;
      }
   };
}();
