var Events = (function() {
   var eventObject = {
      context:          undefined,
      keyPress:         undefined,
      selector:         undefined,
      whereKey:         undefined,
      userData:         undefined,
      bindEvent:        undefined,
      whereType:        undefined,
      whereValue:       undefined,
      asEventName:      undefined,
      bindEventContext: undefined
   };

   var triggers = {
      pub: function(eo) {
         if (_.isUndefined(eo.whereKey) || eo.whereKey.length <= 0) {
            bindEventAs(eo);
         } else {
            var whereKey   = eo.whereKey.shift();
            var whereValue = eo.whereValue.shift();
            var whereType  = eo.whereType.shift();
            var attribute  = whereKey.match(/\[(.*?)\]/);

            if (whereType == "equal" && $(whereKey).attr(attribute[1]) == whereValue) {
                  this.pub(eo);
            }

            if (whereType == "not-equal" && $(whereKey).attr(attribute[1]) != whereValue) {
               this.pub(eo);
            }

            return false;
         }
      },
      sub: function(eo, funcName) {
         PubSub.subscribe(eo.asEventName, function(data) {
            if (_.isArray(funcName)) {
               _.each(funcName, function(userFunc) {
                  userFunc.call(eo.context, data);
               });
               return false;
            }
            return funcName.call(eo.context, data);
         });
      }
   };

   var bindEventAs = function(eo) {
      if (eo.bindEvent === "ready" || eo.bindEvent === "load" || eo.bindEvent === "unload") {
         return Binder.asLoad(eo);
      }

      if (eo.bindEvent.indexOf("key") === 0) {
         return Binder.asKeyboard(eo);
      }

      if (eo.bindEvent === "resize" || eo.bindEvent === "scroll") {
         return Binder.asWindow(eo);
      }

      if (eo.bindEvent.indexOf("mouse") === 0 || eo.bindEvent === "hover" || eo.bindEvent === "click" || eo.bindEvent === "dblclick") {
         return Binder.asMouse(eo);
      }

      if (eo.bindEvent.indexOf("focus") === 0 || eo.bindEvent === "blur" || eo.bindEvent === "change" || eo.bindEvent === "select" || eo.bindEvent === "submit") {
         return Binder.asForm(eo);
      }

      if (eo.bindEvent === "typing") {
         return Binder.asTyping(eo);
      }

      // we don't kow wtf you're trying to bind so
      // lets do something and see what happens
      return Binder.generic(eo);
   };

   return {

      bind: function(bindEvent, selector, key) {
         eventObject.bindEvent = bindEvent;
         eventObject.selector  = _.isUndefined(selector) ? null : selector;
         eventObject.keyPress  = _.isUndefined(key) ? null : key;

         if (_.isArray(eventObject.selector)) {
            eventObject.selector = eventObject.selector.join(', ');
         }

         if (eventObject.bindEvent.indexOf("key") === 0 && _.isNull(eventObject.keyPress)) {
            eventObject.keyPress = eventObject.selector;
            eventObject.selector = null;
         }

         return this;
      },

      where: function(key, value) {
         this.whereType("equal", key, _.isUndefined(value) ? key : value);

         return this;
      },

      whereNot: function(key, value) {
         this.whereType("not-equal", key, _.isUndefined(value) ? key : value);

         return this;
      },

      whereType: function(type, key, value) {
         if (_.isUndefined(eventObject.whereType)) {
            eventObject.whereType = [];
            eventObject.whereKey = [];
            eventObject.whereValue = [];
         }

         eventObject.whereType.push(type);
         eventObject.whereKey.push(key);
         eventObject.whereValue.push(value);
      },

      as: function(eventName) {
         eventObject.asEventName = eventName;

         return this;
      },

      to: function(funcName, context, userData) {
         eventObject.asEventName = Utilities.generateEventName(eventObject.asEventName);
         eventObject.userData    = _.isUndefined(userData) ? {} : userData;
         eventObject.context     = _.isUndefined(context) ? window : context;
         triggers.pub(eventObject);

         if (_.isFunction(funcName) ||  _.isArray(funcName)) {
            triggers.sub(eventObject, funcName);
         }

         eventObject = {};
         return false;
      },

      publish: function(eventName, userData) {
         PubSub.publish(eventName, userData);
      },

      subscribe: function(eventName, funcName, context) {
         PubSub.subscribe(eventName, function(data) {
            return funcName.call(context, data);
         });
      }
   };
}());

