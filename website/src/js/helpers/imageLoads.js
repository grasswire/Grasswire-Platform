GW.Helpers = GW.Helpers || {};

GW.Helpers.imageLoads = function(imageUrl) {
   var image = new Image();
   image.src = imageUrl;
   var jImage =  $(image);


   if (jImage.context.naturalWidth == 0 || jImage.readyState == 'uninitialized') {
      return false;
   }
   return true;
}
