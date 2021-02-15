# NeDRex-Web / RepoScape-Web
Is an interactive and responsive web interface for the heterogenous, molecularbiological network database RepoTrialDB.

## Exmaple Data
The folder for example cases and according figures can be found [here](/material) 


## Deployement with Docker
Use the [docker-compose](docker-compose.yml) file to deploy NeDRex-Web on your machine:
```bash
wget https://raw.githubusercontent.com/AndiMajore/RepoScapeWeb/master/docker-compose.yml -O docker-compose.yml
docker-compose pull
docker-compose up
```
NeDRex-Web interface is running on [localhost:8080](http://localhost:8080)

## Manual Deployement
First clone the project:
```bash
git clone https://github.com/AndiMajore/RepoScapeWeb.git
```
or
```bash
git clone https://github.com/AndiMajore/RepoScapeWeb.git
git@github.com:AndiMajore/RepoScapeWeb.git
```
#### Database
Use the docker container [andimajore/nedrex_repo:db](https://hub.docker.com/repository/docker/andimajore/nedrex_repo) execute it with the parameters from the db-service in the [docker-compose](docker-compose.yml) file and map the exposed port to default mysql port (33060:3306):
or
Set up a MySQL database and use the database setup batch file:
```
mysql < (curl https://raw.githubusercontent.com/AndiMajore/RepoScapeWeb/master/web/resources/scripts/db_setup.sh)
```
### For Development
#### Backend
Use IDE to run [Main-Class/Spring-Boot-Application](web/backend/src/main/java/de/exbio/reposcapeweb/ReposcapewebApplication.java).
#### Frontend
```
cd $gitroot/web/frontend
npm run dev
```
### Deployement
#### Backend
Use maven to compile project and start application, make sure you use >=java14.
```
cd $gitroot/web/backend
mvn package
java -Xmx4g -jar target/reposcapeweb-backend-0.0.1-SNAPSHOT-spring-boot.jar
```
#### Frontend
```
cd $gitroot/web/frontend
npm run prod
```
or
```
cd $gitroot/web/frontend
npm run dev
```
