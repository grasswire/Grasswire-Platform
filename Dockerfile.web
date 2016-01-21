FROM java:latest
RUN curl --silent --location https://deb.nodesource.com/setup_0.12 | bash -
RUN apt-get install -y nodejs
RUN apt-get install -y build-essential
RUN npm install -g bower
RUN npm install -g gulp
ADD . /code
WORKDIR /code
CMD ./entrypoint-web
