var AjaxRoute = (function() {

   var ro = {
      url: "",
      name: "",
      type: "",
      data: {},
      callType: "",
      userData: {},
      useQueryParams: false
   };

   var mergeParams = function(response, userData) {
      response = _.isString(response) ? JSON.parse(response) : _.isArray(response) ? {responseArray: response} : response;

      return _.extend(response, userData);
   };

   var sendAjaxRequest = function() {
      $.ajax(paramSetup())
       .done(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/success", params);
       })
       .fail(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/fail", params);
       })
       .always(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/complete", params);
       });
   };

   var sendAjaxPromiseRequest = function() {
      $.when($.ajax(paramSetup()))

       .done(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/success", params);
       })
       .fail(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/fail", params);
       })
       .always(function(response) {
          var params = mergeParams(response, ro.userData);

          Events.publish(ro.name+"/complete", params);
       });
   };

   var paramSetup = function() {
      if (ro.type == "get") {
         return {
            type: ro.type,
            url: ro.url
         };
      }

      if (ro.useQueryParams) {
         return {
            type: ro.type,
            url: ro.url
         };
      }

      return {
         type: ro.type,
         url: ro.url,
         dataType: "json",
         contentType: "application/json",
         processData: false,
         data: ro.data
      };
   };

   return {
      as: function(type, useQueryParams) {
         ro.type = type;
         ro.useQueryParams = _.isUndefined(useQueryParams) || ! _.isBoolean(useQueryParams) ? false : useQueryParams;

         return this;
      },

      to: function(url, data) {
         ro.name = Utilities.generateEventName();
         ro.url = url;
         ro.data = JSON.stringify(data);

         return this;
      },

      on: function(responseTypes, context, userData) {
         ro.userData = userData || {};

         _.each(responseTypes, function(value, key) {
            Events.subscribe(ro.name+"/"+key, value, context);
         });

         sendAjaxRequest();

         return false;
      },

      when: function(responseTypes, context, userData) {
         ro.userData = userData || {};

         _.each(responseTypes, function(value, key) {
            Events.subscribe(ro.name+"/"+key, value, context);
         });

         sendAjaxPromiseRequest();

      }
   };
}());
