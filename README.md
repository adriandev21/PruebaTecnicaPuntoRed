# API Pagos Referenciados (Spring Boot)

Backend RESTful que implementa la API de Pagos Referenciados V1.3.

## Características
- Endpoints: autenticación, creación/consulta/listado/cancelación de pagos
- Seguridad: Spring Security + JWT (24h)
- Persistencia: MySQL + Spring Data JPA
- Callbacks con reintentos automáticos (1m, 2m, 3m, luego cada 10m, máx 10)
- Sandbox: cada 30m marca pagos `01 Created` como `02 Paid`
- Restricción de IPs editable por admin y dinámica (sin reinicio)
- Validaciones con @Valid y manejo global de errores
- Tests con JUnit/Mockito y Testcontainers

## Requisitos
- Java 21
- Maven 3.9+
- MySQL 8 (o Docker)

## Configuración de properties y perfiles
El proyecto usa dos archivos de configuración:

- `src/main/resources/application.properties`: NO tiene valores fijos; solo lee variables de entorno. Úsalo para ambientes (dev/qa/prod) inyectando env vars.
- `src/main/resources/application-local.properties`: Valores por defecto para desarrollo local (no subir credenciales reales a repositorios).
- Para usos practicos de la prueba les dejo mi properties.local

Variables de entorno leídas por `application.properties`:
- SPRING_APPLICATION_NAME
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SPRING_JPA_HIBERNATE_DDL_AUTO (ej: validate|update)
- SPRING_JPA_SHOW_SQL (true|false)
- SPRING_JPA_FORMAT_SQL (true|false)
- SECURITY_JWT_SECRET (clave larga, 32+ bytes)
- SECURITY_JWT_EXPIRATION_MS (ej: 86400000)
- SANDBOX_ENABLED (true|false)
- SPRING_CACHE_TYPE (ej: caffeine|none)
- LOGGING_LEVEL_ROOT (ej: INFO|DEBUG)

Notas:
- Para desactivar el modo sandbox, usa `SANDBOX_ENABLED=false`.

## Ejecutar local
Opción A (perfil local, recomendado):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Opción B (variables de entorno explícitas):

```bash
export SPRING_PROFILES_ACTIVE=default
export SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/prueba?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="<tu-pass>"
export SECURITY_JWT_SECRET="<clave-larga-32+bytes>"
export SECURITY_JWT_EXPIRATION_MS=86400000
export SPRING_JPA_HIBERNATE_DDL_AUTO=update
export SPRING_JPA_SHOW_SQL=false
export SPRING_JPA_FORMAT_SQL=true
export SANDBOX_ENABLED=true
export SPRING_CACHE_TYPE=caffeine
export LOGGING_LEVEL_ROOT=INFO
./mvnw spring-boot:run
```

## Autenticación
- Usuario por defecto: `admin/admin123` (rol ADMIN) y `client/client123` (rol USER)

```bash
curl -s -X POST http://localhost:8080/v1/authenticate \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123"}'
```

## Crear pago
```bash
TOKEN=<jwt>
curl -s -X POST http://localhost:8080/v1/payment \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{
    "externalId":"ext-123",
    "amount": 100.50,
    "description":"Pago de prueba",
    "dueDate":"2030-01-01T12:00:00",
    "callbackURL":"http://localhost:9000/callback"
  }'
```

## Consultar pago
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/v1/payment/<reference>/<paymentId>
```

## Buscar pagos
```bash
curl -s -H "Authorization: Bearer $TOKEN" \
  'http://localhost:8080/v1/payments/search?startDate=2025-01-01&endDate=2025-01-15&status=01&page=0&size=10'
```

## Cancelar pago
```bash
curl -s -X PUT http://localhost:8080/v1/payment/cancel \
  -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"reference":"<ref>","status":"03","updateDescription":"cancelación por cliente"}'
```

## Admin IPs
```bash
# agregar
curl -s -X POST 'http://localhost:8080/v1/admin/ip?ip=127.0.0.1' -H "Authorization: Bearer $TOKEN"
# habilitar
curl -s -X PUT 'http://localhost:8080/v1/admin/ip-restriction/enabled/true' -H "Authorization: Bearer $TOKEN"
```

## Respuestas JSON
```
{
  "status": 200,
  "message": "OK",
  "data": { }
}
```

## Pruebas y cobertura
- Unit tests: JUnit + Mockito (sin levantar el contexto cuando no es necesario).
- Si usas Jacoco, genera coverage con:

```bash
./mvnw -q clean verify
open target/site/jacoco/index.html
```

## Despliegue en AWS
A continuación dos caminos comunes. Ambos requieren exponer variables de entorno listadas en la sección de configuración.

### Opción 1: Elastic Beanstalk (sin contenedor)
1) Base de datos (RDS MySQL)
- Crea un RDS MySQL 8.x (Multi-AZ opcional).
- Security Group: permite entrada desde el SG del ambiente de la app (no 0.0.0.0/0 en prod).
- Obtén el endpoint para construir `SPRING_DATASOURCE_URL` (ej: `jdbc:mysql://<endpoint>:3306/prueba?useSSL=false&serverTimezone=UTC`).

2) Empaquetado y carga
- Empaqueta el jar: `./mvnw -q -DskipTests=false clean package`.
- Crea una app EB con plataforma Java 21 (Corretto).
- Sube el jar generado en `target/*.jar`.

3) Variables de entorno en EB
- Define: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`.
- Define: `SECURITY_JWT_SECRET`, `SECURITY_JWT_EXPIRATION_MS=86400000`.
- Define: `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` (prod) y `SPRING_JPA_SHOW_SQL=false`, `SPRING_JPA_FORMAT_SQL=true`.
- Define: `SANDBOX_ENABLED=false`, `SPRING_CACHE_TYPE=caffeine`, `LOGGING_LEVEL_ROOT=INFO`.
- Opcional: `SPRING_APPLICATION_NAME=prueba`.

4) Balanceo, escalado y logs
- Habilita Application Load Balancer.
- Auto Scaling por CPU/requests (p. ej. 2–6 instancias).
- Logs a CloudWatch; configura rotación/retención.

5) Salud
- Health check en `/` (o endpoint propio si agregas Actuator).

6) Secretos
- Guarda secretos en AWS Secrets Manager/SSM. Inyecta en EB como env vars (manual) o agrega código para leerlos en runtime.

### Opción 2: ECS Fargate (contenedores)
1) Imagen
- Construye imagen Docker y publícala en Amazon ECR.
- Define variables de entorno del contenedor (mismas que arriba) y mapea secretos desde Secrets Manager.

2) Servicio y red
- Crea un Service con Fargate detrás de un ALB.
- Target group HTTP 80 → contenedor 8080.
- Auto Scaling por CPU/requests. Logs a CloudWatch.

3) Base de datos
- RDS MySQL en subred privada. Permite acceso desde el SG del servicio ECS.

4) Salud
- Health check del target group en `/` (o el que definas).



## Postman
Se incluye la colección `postman_collection.json` en la raíz del proyecto.
