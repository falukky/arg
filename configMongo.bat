docker volume create --name=mydb
timeout 3
docker run -d -p 27017:27017 -v mydb:/data/db mongo
timeout 5