# How to run application
## Using Docker Compose:
1. Build the project:
```
docker compose build
```

2. Run the project:
```
docker compose up
```

## Using Docker and Maven directly:
1. Run postgres with docker:
```
docker run --name local-postgres \ -e POSTGRES_USER=testuser \ -e POSTGRES_PASSWORD=testpass \ -e POSTGRES_DB=testdb \ -p 5432:5432 \ -d postgres:15 """
```

2. Build the project:
```
    mvn clean package
```

3. Run the Spring Boot application in the *local* profile (alternately, use your preffered IDE):
```
   mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Accessing the service:
1. The service runs on port 8080 by default: `http://localhost:8080`.
2. Use prepared Postman collection to query the endpoints: [Cards Service.postman_collection.json](Cards%20Service.postman_collection.json)
