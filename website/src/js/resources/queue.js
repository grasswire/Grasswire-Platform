var Queue = (function(){
   "use strict";

   var q = [];

   var push = function(which, element) {
      if (isEmpty(which)) {
         q[which] = [];
      }

      q[which].push(element);
   };

   var pop = function(which) {
      if (isEmpty(which)) {
         return [];
      }

      return q[which].pop();
   };

   var isEmpty = function(which) {
      return _.isUndefined(q[which]);
   };

   var truncate = function(which) {
      if (! isEmpty(which)) {
         q[which] = [];
      }
   };

   var dump = function(which) {
      var t = [];
      if (! isEmpty(which)) {
          t = q[which];
          truncate(which);
      }

      return t;
   };

   var debug = function() {
      if (! isEmpty(which)) {
          console.log(q[which]);
      }
   };

   var count = function(which) {
      return isEmpty(which) ? 0 : q[which].length;
   };

   return {
      push: push,
      pop: pop,
      isEmpty: isEmpty,
      count: count,
      truncate: truncate
   };
}());
