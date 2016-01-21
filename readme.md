# Setting up your local development machine

**These instructions are particular to mac.** If you aren't using a Mac, follow the instructions [here](http://docs.docker.com/linux/started/) to install Docker and then skip to **step 3**

1. [Install boot2docker](http://docs.docker.com/mac/step_one/).
2. once installed, run `$(boot2docker shellinit 2> /dev/null)`
3. Follow the instructions [here](https://docs.docker.com/compose/install/) to install Docker Compose

## Running the web app.

To run the web app against our stage API (https://api-stage.grasswire.com), then run:

`docker-compose -f docker.compose.web.stage.yml up`

You can open the website in your browser using your docker ip. To get the ip, run `boot2docker ip` and then use ${MY_DOCKER_UP}:9000 in your browser.

--

If you're working on the API project or otherwise want to run the web app against an API service running in a docker container:

First build the API docker image with `docker build -t api -f Dockerfile.api .` and bring up the service with `docker-compose up api`.

Then, run the web service against this API, with `docker-compose -f docker.compose.web.local.yml up`

## Compiling Sass and JavaScript

*TL;DR you know you have all the prerequisites to run the site, awesome, just run the following command: `./watcher` and everything should work*

*If you've run this project before, in any other environment, you're going to need to make sure you remove the node_modules folder before you're able to run watcher*

From the project root:

`$> rm -Rf website/node_modules`

### Setup

We're going to make sure you have the all the stuff you need to run the code.  You really only need 3 things: npm, bower and gulp.

First let's check if you have NPM: `npm -v` if you get an error, you don't have NPM.  Head over to [the NodeJS website](https://nodejs.org/) download and install Node.  Once node is installed you may need to reload your terminal tab in order to have the changes, so run this: `source ~/.bash_profile`.  Now check that everything is working `node -v && npm -v`.  If you didn't get any errors you're good.

Once NPM is working, it's time to get bower and gulp.  These are two simple commands:

`npm install -g bower gulp`

This might take a bit, depending on your Internet connection.

Once that's done reload your terminal again: `source ~/.bash_profile`.

*Now you're ready to run the watcher.  The first time through the watcher is going to have to download all the NPM and Bower packages so this could take a bit of time.*

### Running the watcher

The watcher is just a bash script that will first check for any new NPM or Bower packages then run gulp.  You run this right from the root of your project and you must leave the terminal window open.

`./watcher`

That's it.  Now you're ready to make changes to the JS and Sass.
