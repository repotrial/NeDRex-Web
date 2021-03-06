FROM node:latest as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY ./ .

ARG configuration=prod

RUN npm run build

FROM nginx as production-stage
RUN mkdir /app
COPY --from=build-stage /app/dist /app
COPY nginx-nedrex.conf /etc/nginx/nginx.conf

#TODO need to fix websocket connection (just use env var for ws in connection script?)

