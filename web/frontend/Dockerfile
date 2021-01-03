#FROM node:alpine
#
#
#WORKDIR /usr/src/app
#
#COPY package.json .
#
##RUN apt-get update
##RUN apt-get -y install g++ build-essential
#
#RUN apk add --no-cache --virtual .gyp \
#      python \
#      make \
#      g++ \
#      && npm install
#
#EXPOSE 8080
#
#COPY . .
#
#CMD [ "npm", "run","prod" ]

FROM tiangolo/node-frontend:10 as build-stage

WORKDIR /app
COPY package*.json /app/

RUN npm install


COPY ./ /app/

ARG configuration=prod

RUN npm run build

FROM nginx:1.15

COPY ./dist/ /usr/share/nginx/html


COPY --from=build-stage /nginx.conf /etc/nginx/conf.d/default.conf

#TODO nginx-config
COPY ./nginx-nedrex.conf /etc/nginx/extra-conf.d/

EXPOSE 8080
