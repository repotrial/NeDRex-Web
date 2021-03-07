# NeDRex-Web / RepoScape-Web
Is an interactive and responsive web interface for the heterogenous, molecularbiological network database RepoTrialDB.

## Material
The folder for data of example cases and according figures can be found [here](/material) 


## Deployement with Docker
Use the [docker-compose](docker-compose.yml) file to deploy NeDRex-Web on your machine:
Adjust image names based on the purpose:
- Local execution:
  `andimajore/nedrex_repo:server` and `andimajore/nedrex_repo:web`
- Execution on exbio servers:
   `andimajore/nedrex_repo:server_exbio` and `andimajore/nedrex_repo:web_exbio`
   
```bash
wget https://raw.githubusercontent.com/AndiMajore/RepoScapeWeb/master/docker-compose.yml -O docker-compose.yml
docker-compose pull
docker-compose up
```
NeDRex-Web interface is running on [localhost:8080/nedrex](http://localhost:8080/nedrex/)

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
npm run serve
```
### Production
#### Tomcat settings
If the services are planned to be deployed using a tomcat server, the configuration files used for the dockerized tomcat versions can be found in `frontend/docker/` and `backend/docker/`.

#### Backend
Use maven to compile project and start application, make sure you use >=java14.
```
cd $gitroot/web/backend
mvn package
java -Xmx4g -jar target/nerdrexweb-backend.war
```
or deploy with a tomcat server.

#### Frontend
Generate webcontent to `dist/` directory:
```
cd $gitroot/web/frontend
npm run build
```
Host paths can be set by editing the `src/Config.js` file before building.
The content of `dist/` can be served by nginx or tomcat server.
