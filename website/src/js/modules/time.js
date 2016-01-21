GW.Modules = GW.Modules || {};
GW.Modules.Time = function() {
   "use strict";

   var options = {};

   var configTimeSettings = function() {
      moment.locale('en', {
          relativeTime : {
              future: "in %s",
              past:   "%s ago",
              s:  "seconds",
              m:  "a minute",
              mm: "%dmin",
              h:  "an hour",
              hh: "%dh",
              d:  "a day",
              dd: "%dd",
              M:  "a month",
              MM: "%dm",
              y:  "a year",
              yy: "%dyr"
          }
      });
      $('time').hide().delay(1000).fadeIn(250);
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("load").to(configTimeSettings, this);

         return this;
      }
   };
}();
