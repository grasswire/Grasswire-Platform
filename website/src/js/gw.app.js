(function() {
   "use strict";

   var GW = {
      Config: {
         mastheadOffset: 60,
         triggerMobileSize: 767
      },
      init: function () {
         _.each(GW.Modules, function(module) {
            module.init().events();
         });
      }
   };

   window.GW = GW;
}());
