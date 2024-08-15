# Conference booking application

## Description
This application consists of a conference room booking REST API intended for a company's internal use. Four conference rooms exists with the following traits:

| Conference Name | Capacity |
|-----------------|----------|
| Amaze           | 3        |
| Beauty          | 7        |
| Inspire         | 12       |
| Strive          | 20       |

These are configured through [script](./src/main/resources/data.sql) which injects the conference rooms into the in memory database on start-up.

During the maintenance windows the rooms are unavailable, which is conduced of the following:

- 09:00 - 09:15
- 13:00 - 13:15
- 17:00 - 17:15

> ***NOTE:*** Conference room bookings can only be done on the current date in the future.

## How to run the application

### Requirements

- Java 21
- Spring boot 3.2.5
- Maven
- In memory database H2 driver

### Run the application

The application can be run by building the project and thereafter starting the spring boot application, e.g. through: `mvn clean intall` followed by `mvn spring-boot:run`, or through you preferred IDE.

### Example requests

**Get available rooms (change to correct timestamps)**

```
curl "http://localhost:8080/api/conference-room/availability?from=2024-08-15T02:00:00&to=2024-08-15T04:00:00"
```

**Book conference room (change to correct timestamps)**

```
curl -X POST \
  http://localhost:8080/api/conference-room/book \
  -H 'Content-Type: application/json' \
  -d '{
    "from": "2024-08-15T10:00:00",
    "to": "2024-08-15T12:00:00",
    "numberOfParticipants": 10
}'

```

**Get conference room bookings (change to correct timestamps)**

```
curl "http://localhost:8080/api/conference-room/bookings?from=2024-08-15T09:45:00&to=2024-08-15T13:00:00"
```

## Potential Future improvements

- Delete existing future conference booking, requires post endpoint to expose an ID of the entity
- Get existing conference booking by ID
- CRUD operations on the conference rooms
- CURD operations on the maintenance windows which is currently static
- Replace in memory database with persistant storage
- Dockerize the application and the potential storage with docker compose for local setup
