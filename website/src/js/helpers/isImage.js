GW.Helpers = GW.Helpers || {};

GW.Helpers.isImage = function(imgName) {
   var imageTypes = [
         ".png", ".jpg", ".jpeg", ".gif", ".svg",
         ".PNG", ".JPG", ".JPEG", ".GIF", ".SVG"
      ];

   var matched = _.find(imageTypes, function(type) {
      return (imgName.slice(-type.length) == type);
   });

   return ! _.isUndefined(matched) ? true : false;
}
