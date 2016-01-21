GW.Helpers = GW.Helpers || {};
GW.Helpers.getQueryParams = function(queryString) {
   var params = queryString.split("&"),
       paramObject = {};

   _.each(params, function(item){
      var pair = item.split("=");
      if (!_.isUndefined(pair[1])) {
         paramObject[pair[0]] = pair[1];
      } else {
         paramObject[pair[0]] = pair[0];
      }
   });

   return paramObject;
}
