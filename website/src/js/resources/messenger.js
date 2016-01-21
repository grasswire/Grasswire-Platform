var Messenger = (function(){
   "use strict";

   var commentErrorMessage = function(id, text) {
      message('.js-mediabox-tile[data-content-id='+id+'] .js-messagebag', "commentErrorMessage", text);
   };

   var submissionMessage = function(text) {
      message('.js-messagebag-submission', "commentErrorMessage", text);
   };


   var message = function(position, template, text) {
      $(position).html(Handlebars.templates[template]({
         errorMessage: text
      }));
   };

   return {
      commentErrorMessage: commentErrorMessage,
      submissionMessage: submissionMessage
   };
}());
