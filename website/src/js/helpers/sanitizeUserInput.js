GW.Helpers = GW.Helpers || {};

GW.Helpers.sanitizeUserInput = function(userInputObject) {
   var sanitized = {};
   _.map(userInputObject, function(item, key) {
      return sanitized[key] = s(item).clean().escapeHTML().value();
   });

   return sanitized;
}


