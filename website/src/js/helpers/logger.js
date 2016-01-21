GW.Helpers = GW.Helpers || {};
GW.Helpers.logger = {
   message: function(message) {
      log.debug(message);
   },
   log: function(type, message, response, captureEvent) {
      log.debug("--------------------");
      log.debug(type+": "+message);
      log.debug("URL: "+window.location.href);
      log.debug("Response: "+JSON.stringify(response));
      log.debug("--------------------");

      if (! _.isUndefined(captureEvent)) {
         _gs('event', captureEvent);
      }
   }
};


