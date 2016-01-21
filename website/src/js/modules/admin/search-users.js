GW.Modules = GW.Modules || {};
GW.Modules.SearchUsers = function() {
   "use strict";

   var search = function() {
      var xhr;
      $(".js-user-search").autoComplete({
         source: function(term, response) {
            try { xhr.abort(); } catch(e) {}
            xhr = $.ajax({
               url: jsRoutes.controllers.AjaxController.searchUsers(term).url
            })
            .success(function(data) {
               response(_.map(JSON.parse(data), function(value, key) {
                  return value.twitterScreenName;
               }));
            });
         },
         onSelect: function() {
            getUserInfo();
         }
      });
   };

   var getUserInfo = function() {
      var value = $(".js-user-search").val();

      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.searchUsers(value).url)
         .on({
            success: displayUserInfo
         }, this);
   };

   var displayUserInfo = function(response) {
         var user = response.shift(); // there can be only one...

         $(".js-quick-profile").html(Handlebars.templates.quickProfile(user));
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("load").to(search, this);
         Events.bind("key", ".js-user-search", [13]).to(getUserInfo, this);

         return this;
      }
   };
}();
