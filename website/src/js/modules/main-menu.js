GW.Modules = GW.Modules || {};
/**
*  Toggles the menu in the masthead
*/
GW.Modules.MainMenu = function () {
   "use strict";

   var ops = { };

   var toggleDropdown = function(data) {
      var clicked = data.eventElement.attr('href');

      $(".js-dropdown-content[data-dropdown="+clicked+"]").toggle();
   };

   var showHoverTitle = function(data) {
      var title = data.eventElement.attr("data-hover-title");
      data.eventElement.append(Handlebars.templates.mastheadHover({title: title}));
   };

   var hideHoverTitle = function(data) {
      data.eventElement.find(".js-hover").remove();
   };

   return {
      init: function() { return this; },
      events: function() {
         if (!Modernizr.touch) {
            Events.bind("mouseenter", "[data-hover-title]").to(showHoverTitle, this);
            Events.bind("mouseleave", "[data-hover-title]").to(hideHoverTitle, this);
         }

         Events.bind("click", ".js-dropdown").to(toggleDropdown, this);

         return this;
      }
   };

}();
