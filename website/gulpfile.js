/**
 * Heisenberg Toolkit Gulpfile
 *
 * USAGE local:
 * gulp
 *
 * USAGE production:
 * gulp --production
 *
 * In production heisenberg will uglify your JS and minify your SASS and
 * obviously not turn on live reload
 *
 * Live Reload is turned on by default, if you DO NOT want to use it:
 * gulp --noreload
 * - or -
 * gulp --production
 *
 * Chrome Plugin:
 * https://chrome.google.com/webstore/detail/livereload/jnihajbhpnppcggbcgedagnkighmdlei?hl=en
 *
 * Firefox Plugin:
 * https://addons.mozilla.org/en-us/firefox/addon/livereload/
 */

/**
 * Why don't I use gulp-load-plugins here?  Wouldn't that make
 * this easier? Sure, except for the fact that the plugin list
 * then becomes a black box.  Personally, I don't like that
 * plugin because it forces me to have to look in a different file
 * to find out if you have a specific plugin and I'm lazy.
 *
 */
var gulp       = require('gulp'),
    del        = require('del'),
    gulpif     = require('gulp-if'),
    wrap       = require('gulp-wrap'),
    sass       = require('gulp-sass'),
    yargs      = require('yargs').argv
    bower      = require('gulp-bower'),
    concat     = require('gulp-concat'),
    notify     = require('gulp-notify'),
    uglify     = require('gulp-uglify'),
    plumber    = require('gulp-plumber'),
    declare    = require('gulp-declare'),
    imagemin   = require('gulp-imagemin'),
    sourcemaps = require('gulp-sourcemaps'),
    handlebars = require('gulp-handlebars'),
    livereload = require('gulp-livereload'),
    prefixer   = require('gulp-autoprefixer'),
    pngquant   = require('imagemin-pngquant');

/**
 * Configuration object
 * Various folders that Gulp is going to need to know about.
 * Feel free to move all this stuff around, just make sure you
 * keep this file up-to-date
 */
var config = {
   dest: {
      js:     "public/js/",
      css:    "public/css/",
      imgs:   "public/images/",
      fonts:  "public/fonts/",
   },
   src: {
      js:        "src/js/",
      hbs:       "src/js/templates/",
      // If you change where bower installs files, make sure you also
      // update the .bowerrc file too.
      bower:     "src/bower/",
      sass:      "src/scss/",
      // When you crate a new image you should put them in the SRC directory
      // from there imagemin will see it and compress the image and copy the
      // image into the /assets/images folder where you can call it.
      imgs:    "src/images/",
      // when handlebars compiles all your scripts together, it needs a place to put them.
      // it goes into this .tpl file before getting compiled into the main JS file.
      // Why .tpl? If you name it .js then the gulp watcher goes crazy because it sees
      // you writing a new JS file.
      templates: "templates.tpl"
   }
};

/**
 * Scripts object-array
 * These are the various JS scripts that are being used in the site.
 * There are a couple of things going on here so let's take a look
 */
var scripts = {
   // jQuery and Modernizr should not be concatenated with everything else
   // Why? Modernizer needs to be in the <head> and jQuery only needs to be
   // loaded IF the google CDN version fails to load
   jquery:     [config.src.bower +"jquery/dist/jquery.js"],
   modernizr:  [config.src.bower +"modernizr/modernizr.js"],

   main: [
      config.src.bower + "jquery-validation/dist/jquery.validate.js",
      config.src.bower + "jquery.easing/js/jquery.easing.js",
      config.src.bower + "jquery-auto-complete/jquery.auto-complete.js",
      config.src.bower + "underscore/underscore.js",
      config.src.bower + "underscore.string/dist/underscore.string.js",
      config.src.bower + "html5-polyfills/EventSource.js",
      config.src.bower + "sortable.js/Sortable.js",
      config.src.bower + "sortable.js/jquery.binding.js",
      config.src.bower + "momentjs/moment.js",
      config.src.bower + "livestampjs/livestamp.js",
      config.src.bower + "handlebars/handlebars.runtime.js",
      config.src.bower + "amplify/lib/amplify.js",
      config.src.bower + "loglevel/dist/loglevel.js",
      config.src.bower + "js-cookie/src/js.cookie.js",
      config.src.bower + "ajaxchimp/jquery.ajaxchimp.js",
      config.src.js    + config.src.templates,
      config.src.js    + "gw.app.js",
      config.src.js    + "resources/**/*.js",
      config.src.js    + "helpers/**/*.js",
      config.src.js    + "modules/**/*.js",
      config.src.js    + "main.js"
   ],
};

// Grab latest from Bower .....................................................
gulp.task('bower', function() {
    return bower()
        .pipe(gulp.dest(config.src.bower));
});

// Blow out the destination files on fresh compile ............................
gulp.task('cleaner', function () {
   del([
      config.dest.css   + "**/*.*",
      config.dest.js    + "**/*.*",
      config.dest.imgs  + "**/*.*",
      config.dest.fonts + "**/*.*"
   ]);
});

// Copy assets to public folder ...............................................
gulp.task('copy', ['bower'], function () {
   gulp.src([config.src.bower +"fontawesome/fonts/fontawesome-webfont.*"])
      .pipe(gulp.dest(config.dest.fonts));
});

// Minify images ..............................................................
gulp.task('imagemin', function () {
    return gulp.src(config.src.imgs + '**/*.*')
        .pipe(plumber({errorHandler: notify.onError("Imagemin Error:\n<%= error.message %>")}))
        .pipe(imagemin({
            progressive: true,
            svgoPlugins: [{removeViewBox: false}],
            use: [pngquant()]
        }))
        .pipe(gulp.dest(config.dest.imgs))
        .pipe(livereload());
});

// Compile handlebars templates ...............................................
gulp.task('handlebars', function () {
    gulp.src(config.src.hbs+'*.hbs')
      .pipe(plumber({errorHandler: notify.onError("Handlebars Error:\n<%= error.message %>")}))
      .pipe(handlebars())
      .pipe(wrap('Handlebars.template(<%= contents %>)'))
      .pipe(declare({
          namespace: 'Handlebars.templates',
          noRedeclare: true,
      }))
      .pipe(concat(config.src.templates))
      .pipe(gulp.dest(config.src.js))
      .pipe(livereload());
});

// Do everything to JavaScript ................................................
gulp.task('js', ['handlebars'], function() {
   gulp.src(scripts.modernizr)
       .pipe(plumber({errorHandler: notify.onError("JS Error:\n<%= error.message %>")}))
       .pipe(concat("modernizr.min.js"))
       .pipe(gulpif(yargs.production, uglify()))
       .pipe(gulp.dest(config.dest.js))
       .pipe(livereload());

   gulp.src(scripts.jquery)
       .pipe(plumber({errorHandler: notify.onError("JS Error:\n<%= error.message %>")}))
       .pipe(concat("jquery.min.js"))
       .pipe(gulpif(yargs.production, uglify()))
       .pipe(gulp.dest(config.dest.js))
       .pipe(livereload());

   gulp.src(scripts.main)
       .pipe(plumber({errorHandler: notify.onError("JS Error:\n<%= error.message %>")}))
       .pipe(sourcemaps.init())
          .pipe(concat("app.min.js"))
          .pipe(gulpif(yargs.production, uglify()))
       .pipe(sourcemaps.write("maps"))
       .pipe(gulp.dest(config.dest.js))
       .pipe(livereload());
});

// Compile the Sass ...........................................................
gulp.task('sass', function () {
   gulp.src(config.src.sass + '*.scss')
       .pipe(plumber({errorHandler: notify.onError("Sass Error:\n<%= error.message %>")}))
       .pipe(sourcemaps.init())
          .pipe(sass({
             outputStyle: yargs.production ? "compressed" : "nested"
          }))
          .pipe(prefixer({
             browsers: ['last 2 versions'],
             cascade: false,
             remove: true
           }))
       .pipe(sourcemaps.write("maps"))
       .pipe(gulp.dest(config.dest.css))
       .pipe(livereload());
});

// Watch for changes ..........................................................
gulp.task('watch', function () {
   if (!yargs.noreload && !yargs.production) {
      livereload.listen();
   }
   gulp.watch(config.src.js   + '**/*.js',   ['js']);
   gulp.watch(config.src.hbs  + '**/*.hbs',  ['handlebars', 'js']);
   gulp.watch(config.src.sass + '**/*.scss', ['sass']);
   gulp.watch(config.src.imgs + '**/*.*',    ['imagemin']);
});

// just say $> gulp
gulp.task('default', ['js', 'sass', 'imagemin', 'watch']);

// Does a little spring cleaning if you ever need it...
// just say $> gulp boot
gulp.task('boot', ['cleaner', 'bower', 'copy']);

// just say $> gulp compile
gulp.task('compile', ['boot', 'js', 'sass', 'imagemin']);


