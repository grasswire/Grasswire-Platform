# Front End Coding Standards

## Introduction
Not close to done, just starting to get thoughts onto "paper".

## Browser and Device Support
+ IE10+
+ Firefox
+ Safari
+ Chrome
+ iOS 7+
+ Android...god I have no idea...just pick something that and we'll go from there

## IDE Settings
The choice of IDE is personal, but we need to make sure that we maintain a coherent looking codebase.  In the root of this project we have an [EditorConfig](http://editorconfig.org/) file that defines the settings we need your IDE of choice to adhere to.  

If you IDE does not have an editorconfig plugin please set the following settings globally:
+ **SPACES NOT TABS** - no one wants to start a holy war, but something needs to be picked. Use spaces, our codebase is spaces, if you'd like the change that...no we're set--don't 
+ **Indents** - .scala files have **2 spaces per tab**, HTML, SASS, JavaScript **3 spaces per tab**

## Git and Git Flow
We use Git to version control our code.  Git is great, we love it, but we also need to have a bit of a strategy around it.  Generally we follow the [GitFlow model of branching]().  I highly recommend that you install that [gitflow homebrew plugin](http://danielkummer.github.io/git-flow-cheatsheet/) it'll make your life a lot easier.  

### The `master` Branch
Anything you push to `master` can and will be deployed.  The master branch always reflects what is in production.  Don't put half-finished or untested stuff into master.  Also, don't branch off master.  Since it's whatever is in production it's most likely not the latest-and-greatest.  

### The `develop`  branch
This is our long-running branch that has all our latest code.  This stuff gets deployed to our beta environment where it gets looked at by other team members.  If you code is not yet ready for testing, it shouldn't go in here. 

### Prune Your Branches
Two important things to keep in mind about all the branches you're going to create:
1. Delete all branches you're not using
2. Do not push up any branch that doesn't need to be used by someone else or isn't going to be around that long

Regarding point two: if you create a branch today and it's going to be merged today--there's no reason to push that branch up to GitHub.  Just keep it running locally then merge and delete the branch.  

Now, if you know it's going to take you a couple of days to finish the features out--push the branch up.  This does two important things: it allows other people some visibility into the fact that you are indeed working (we're remote, we trust you, but long stretches with no commits look weird) and if something happened to your computer, your work won't all be down-the-drain.  

## Pull Requests 
When you're ready to merge a feature into `develop` the first thing you need to do is push the branch up and issue a pull request.  You need to have another set of eyes take a look at your code before it's allowed to go into the wild. 

**You do not merge code into develop until you've gotten a thumbs up from the person who is looking at your code.**

## HTML
We write semantic HTML. 

### Fragments
How do we write partials

### Modified BEM
Discuss: 
`.foo //block`

`.foo__bar //block__element`

`.foo__bar--baz //block__element--modifier`

## CSS/Sass
We don't write pure CSS, we use Sass.  

+ words are separated with dashes do not camelCase anything
+ Use `mixins()`
+ Don't use `extend`
+ 

### Class Rules
Discuss the concept of `.js-*` classes

### Susy Grids
What is Susy

### Media Queries
Talk about our breakpoints

### Normalize
Normalize.css

## JavaScript
Modules -- discuss module setup and Heisenberg

### Events
Explain 

```JavaScript
   Events.bind(.foo)
         .as(gw/bar)
         .where('[class=foo]')
         .to(func, this);
```

### Ajax Calls
Explain       

   ```JavaScript
      AjaxRoute.as("put")
               .to('path/to/foo', data)
               .on({
                  success: foo,
                  error: bar,
                  complete: callFunc
               }, this);
   ```

### PubSub

### Validation

### Handlebars

## Compiling Assets

### Bower
Front end stuff is brought in through Bower.

### Gulp
Gulp compiles our stuff

#### Gulp Tasks

### Node Modules (NPM)
You might be wondering why we use both NPM and Bower, because they can both be used for pulling in dependencies.  NPM is great, but there is better flexibilty with Bower in certain respects (ie directory structure).  There is also a chance that we may move away from Gulp and NPM (the Play! framework has things that can do a lot of what we need).  So if we do move away from Gulp, we don't want to have front end dependencies hanging around.  

TL;DR: NPM should only be used to pull in dependencies for *gulp* only.  

### Images
Images are reserved for very custom things. We try to use [FontAwesome](http://fortawesome.github.io/Font-Awesome/) icons before we use an image. If it can be avoided don't use an image. 

If you do need to use an image for something we do have a couple of general rules you need to follow:

1. **Make use of SVG**.  It's not always possible to use SVG but really, try.  SVG images scale nicely so we don't have to worry about creating retina images or mobile images.  Making SVGs isn't something you need to worry about either, if you have the skill to create one, great, go for it, but if you don't please feel free to ask someone on the design team to make it for you.  
2. **Use PNG24 and PNG8**. If you can't use an SVG for some reason go with a PNG and try to compress it as much as possible without degrading quality.  Try to save as a PNG8 whenever possible.
3. **Images should have a transparent background**.  Transparent backgrounds add to an images portability.  
4. **Save @2x**.  If you created a PNG, make sure you also save an image at double size for retina displays too.
5. **Images go into the `website/src/images` folder**.  We have a gulp task that will compress images and will put them in the correct place in the public directory.  

