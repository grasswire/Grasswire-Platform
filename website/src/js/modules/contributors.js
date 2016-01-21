GW.Modules = GW.Modules || {};
/**
 *  Gets a JSON list of the most recent (24 hour period)
 *  contributors and we display them in the footer
 */
GW.Modules.Contributors = function() {
   "use strict";

   var options = {
      contrib: ".js-contributors",
      gotContributors: false
   };

   var getContributors = function() {
      if (! options.gotContributors) {
         options.gotContributors = true;

         AjaxRoute.as("get")
            .to(jsRoutes.controllers.AjaxController.todaysContributors().url, {})
            .on({
              success: appendUsers
            }, this);
      }
   };

   var appendUsers = function(response) {
      if (response.length > 0) {
         if (! _.isUndefined(response.responseArray)) {
            $(options.contrib).html(Handlebars.templates.contributors({"people" : response.responseArray}));
         }
         $(options.contrib).html(Handlebars.templates.contributors({"people" : response}));
      }
   };

   var getScrollPosition = function(data) {
      var almostThere = $(document).innerHeight() * 0.50;

      if ($(window).scrollTop() >= almostThere || $(window).height() >= $(document).innerHeight()) {
         getContributors();
      }
   };

   return {
      init: function() { return this; },

      events: function() {
         Events.bind("load").whereNot("body[class]", "admin").whereNot("body[class]", "profile").to(getScrollPosition, this);
         Events.bind("scroll").to(getScrollPosition, this);
         return this;
      }
   };
}();
