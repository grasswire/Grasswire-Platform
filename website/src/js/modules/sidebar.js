GW.Modules = GW.Modules || {};
/**
 * Sidebar navigation on GW
 */
GW.Modules.Sidebar = function () {
   "use strict";

   var op = {};

   var toggleSidebar = function(data) {
      var sidebar = $(".js-sidebar"),
          width = sidebar.css('width'),
          marginLeft = parseInt($(".js-sidebar").css('margin-left'));

      if (marginLeft >= 0 && ! sidebar.is(":animated")) {
         sidebar.animate({"margin-left": "-="+width}, 150, function() {
            $('.js-sidebar-background').hide();
            $('html body').css("overflow-y", "scroll");
            $('html body').removeClass('js-sidebar-open');
         });
      } else {
         if (! sidebar.is(":animated")) {
            sidebar.animate({"margin-left": "+="+width}, 150, function() {
               $('.js-sidebar-background').show();
               $('html body').css("overflow-y", "hidden");
               $('html body').addClass('js-sidebar-open');
            });
         }
      }
   };

   var scrollToItem = function(data) {
      var storyId = data.eventElement.data('sidebar-story');
      $("html, body").animate({ scrollTop: $('#feature-'+storyId).offset().top - 60 }, 1000, function() {
         // this needs to be trigered after scrolling stops because
         // as the page scrolls the highlighter is going to be
         // getting lots of events pushed to it, it'll need
         // this last one to make sure the page correctly
         // shows the highlight
         Events.publish("gw/sidebar-highlight", {storyId: storyId});
      });
   };

   var highlight = function(data) {
      if (! _.isNull(data.storyId)) {
         $(".currently-active").removeClass("currently-active");
         $(".js-sidebar-item[data-sidebar-story="+data.storyId+"]").addClass("currently-active");
      } else {
         $(".js-sidebar-item").first().addClass("currently-active");
      }
   };

   return {
      init: function() { return this; },
      events: function() {
         Events.bind("click", ".js-sidebar-button").to(toggleSidebar, this);
         Events.bind("mouseenter", ".js-sidebar-button").to(toggleSidebar, this);
         Events.bind("mouseleave", ".js-sidebar").to(toggleSidebar, this);

         Events.bind("click touchstart", ".js-sidebar-background").to(toggleSidebar, this);
         Events.bind("click", '.js-sidebar-item').to(scrollToItem, this);
         Events.subscribe("gw/close-sidebar", toggleSidebar);
         Events.subscribe("gw/sidebar-highlight", highlight);
         return this;
      }
   };

}();
