# ARGO Mon Status Page API

The **Status Page API** is part of the **ARGO Monitoring Service**, developed and maintained by **GRNET**.  
It provides REST endpoints for creating, updating, and displaying **service status pages**, integrating with ARGO’s monitoring reports and data sources.

The API allows authenticated users to manage **status pages** and **groups**, while relying on **Keycloak** for authentication and access control.

---

## Overview

The Status Page API enables:

- Management of **Status Pages** (create, list, update, delete)
- Retrieval of **Status Groups** and **Reports** from ARGO Monitoring
- Configuration of **Public Status Pages** for external visibility
- Management of **User Profiles** linked with Keycloak identities

---

## Project Structure

This project is a **REST API** built with **Quarkus** and **Maven**, following a modular architecture and the **service–repository pattern**.

### Modules

1. **api**  
   Exposes REST endpoints for client interaction.

2. **dto**  
   Defines Data Transfer Objects used between API and service layers.

3. **entity**  
   Contains ORM entities representing database tables.

4. **enum**  
   Contains enumeration definitions used across the API.

5. **exception**  
   Defines custom exceptions used throughout the application.

6. **handler**  
   Centralizes error handling and exception mapping.

7. **mapper**  
   Uses **MapStruct** for entity-to-DTO conversions.

8. **repository**  
   Provides data access logic and database queries.

9. **service**  
   Contains the business logic of the API.

10. **util**  
    Contains utility classes and helpers such as configuration utilities.

---

## Authentication

All protected resources require authentication through **Keycloak**.  
Clients must include a **Bearer token** in each request to access restricted endpoints.

### Access the Protected Resources

Since the API’s endpoints must only be accessible to verified clients, every client who wants to access the API must be authenticated.  
Communication with protected endpoints is performed using **Bearer Authentication** — a token-based authentication method commonly used in HTTP APIs.

Include the access token in your HTTP requests using the **Authorization** header:

```
Authorization: Bearer {{token}}
```

---

## Access Token Retrieval

The [ARGO Mon Status Pages API](https://api-status-next.devel.mon.argo.grnet.gr/) allows users to obtain an access token for authentication purposes.  
By following these steps, users can retrieve an access token to authenticate API requests.

### Instructions

1. **Open the Web Page**  
   Navigate to the [ARGO Mon Status Pages API](https://api-status-next.devel.mon.argo.grnet.gr/) page.

2. **Locate the Access Token Button**  
   Once the page loads, locate the button that retrieves the access token.

3. **Click the “Obtain an Access Token” Button**  
   Clicking this button initiates the retrieval process from the authentication server.

4. **Provide Required Information**  
   Choose your preferred identity provider and log in with your credentials.

5. **Retrieve the Access Token**  
   After successful authentication, the token will appear on the page.

6. **Use the Access Token**  
   Include it in your API requests as described above.

---

# Instructions for Developers

## Prerequisites

Ensure the following software is installed on your development environment:

- **Java Development Kit (JDK)** 17
- **Apache Maven** 3.9
- **Docker**

---

## Install Dependencies

After cloning the repository, navigate to the root directory and execute:

```bash
mvn clean install -DskipTests=true -U
```

This command installs all required dependencies in your local Maven repository.

---
## Start Argo Web API in Development Profile

In development mode, Argo Mon Status Page Api uses a locally hosted Argo Web API instead of the production service. To start the local Argo Web API instance, follow these instructions:

1. Clone the Argo Web API repository
Clone the repository and checkout the devel branch:

git clone -b devel https://github.com/argoeu/argo-web-api


2. Navigate to the Docker folder
Change directory to the Docker setup folder:

cd argo-web-api/docker

3. Start the local Argo Web API using Docker Compose
Run the following command to start the services:

docker-compose up

The local instance of the Argo Web API will then be accessible at:
http://localhost:8843

## Start the Application with Quarkus Dev Services

To launch the application in development mode:

```bash
mvn clean quarkus:dev
```

This will start the API along with supporting services (PostgreSQL, Keycloak, etc.) using **Quarkus Dev Services**.

---

## Access the Dev Service Database

Connect to the automatically started PostgreSQL instance using:

```bash
psql -h localhost -U status -d status
```

> Default development password: `status`

Or connect directly inside the running container:
```bash
docker exec -it <container_id> psql -U status -d status
```

---

## Obtain an Access Token from Dev Service Keycloak

To get a development token, follow the [Quarkus guide](https://quarkus.io/guides/security-openid-connect-dev-services#dev-services-for-keycloak)  
or access the Keycloak instance automatically launched by Dev Services.

Default credentials:

- **Username:** `alice`
- **Password:** `alice`

Once logged in, copy the **Access Token** and include it in API requests.

---

## License & Credits

ARGO Mon Status Pages is a service developed and maintained by **GRNET**.  
Distributed under the **Apache 2.0 License**.
