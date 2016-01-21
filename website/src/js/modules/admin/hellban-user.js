GW.Modules = GW.Modules || {};
GW.Modules.HellbanUser = function () {
    "use strict";

    var options = { };

    var toggleHellban = function(data) {
      var username = data.eventElement.attr("data-username"),
          isHellbanned = data.eventElement.attr("data-current-hellban-status"),
          status = (isHellbanned == "true") ? false : true;

      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AdminController.hellban(username, status).url)
         .on({
            success: success
         }, this);
    };

    var success = function(response) {
      location.reload(true);
    };

    return {
        init: function() { return this; },
        events: function() {
            Events.bind("click", ".js-hellban").to(toggleHellban, this);

            return this;
        }
    };

}();
