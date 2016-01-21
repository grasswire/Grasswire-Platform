GW.Modules = GW.Modules || {};
GW.Modules.MakeAdmin = function () {
    "use strict";

    var options = { };

    var createAdmin = function(data) {
      AjaxRoute.as("get")
         .to(jsRoutes.controllers.AjaxController.makeAdmin(data.eventElement.attr("data-username")).url)
         .on({
            success: userIsAdmin
         }, this);
    };

    var userIsAdmin = function(response) {
       location.reload(true);
    };

    return {
        init: function() { return this; },
        events: function() {
            Events.bind("click", ".js-admin").to(createAdmin, this);
            return this;
        }
    };

}();
