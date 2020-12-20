FROM node:alpine


WORKDIR /usr/src/app

COPY package.json .

#RUN apt-get update
#RUN apt-get -y install g++ build-essential

RUN apk add --no-cache --virtual .gyp \
      python \
      make \
      g++ \
      && npm install

EXPOSE 8080

COPY . .

CMD [ "npm", "run","prod" ]
