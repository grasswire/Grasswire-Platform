GW.Modules = GW.Modules || {};
/**
 *  This checks if any of the feature images are broken
 *  and if they are, it will drop in our dummy image
 *  so the site doesn't look all borked.
 */
GW.Modules.ImageChecker = function () {
   "use strict"

   var options = {
      delay: 250,
      featureImages: ".js-image-checker",
   };

   var imageChecker = function(data) {
      window.setTimeout(function() {

         $(".js-image-checker").each(function() {
            var image = $(this);
            if (image.context.naturalWidth == 0 || image.readyState == 'uninitialized') {
               /**
                * Why am I setting an attribute when I could just set the src
                * right here you say? Good point and the answer lies in the
                * fact that JavaScript is annoying and sometimes things
                * load in too fast and we don't get the correct data
                * so, I need to slow things down with a delay in
                * the start of this function and then by just
                * marking the items as potentially errored
                * images. Then we can check the images
                * one more time to be sure that
                * they're indeed dead.
                */
               $(image).attr("data-bad-image", true);
            }
         }).promise().done(function() {
            reviewBadImage();
         });
      }, options.delay);
   };

   var reviewBadImage = function() {
      var path = $("body").data("default-image");
      $("[data-bad-image=true]").each(function(){
            var image = $(this);
            if (image.context.naturalWidth == 0 || image.readyState == 'uninitialized') {
               $(image).unbind("error").attr("src", path);
            }
      });
   }

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(imageChecker, window);

         return this;
      }
   };

}();
