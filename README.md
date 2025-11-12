# MSA User - API RESTful de Registro de Usuarios

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/WebFlux-Reactive-blue.svg)](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

API RESTful reactiva para registro y gestión de usuarios con autenticación JWT, validaciones configurables y persistencia en base de datos H2.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Tecnologías](#-tecnologías)
- [Arquitectura](#-arquitectura)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#-configuración)
- [Ejecución](#-ejecución)
- [Uso de la API](#-uso-de-la-api)
- [Testing](#-testing)
- [Documentación API](#-documentación-api)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Diagramas](#-diagramas)

## ✨ Características

- ✅ **Programación Reactiva** con Spring WebFlux
- ✅ **JWT**
- ✅ **Validaciones Configurables** via properties (email y password con regex)
- ✅ **Encriptación BCrypt** para contraseñas
- ✅ **Base de Datos H2** en memoria con persistencia JPA
- ✅ **Contract-First** con OpenAPI Generator
- ✅ **MapStruct** para mapeo de entidades
- ✅ **Manejo Global de Excepciones**
- ✅ **Test** con JUnit 5 y Reactor Test
- ✅ **UUID** como identificador de usuarios

## 🛠 Tecnologías

| Tecnología | Versión | Descripción |
|------------|---------|-------------|
| Java | 21 | Lenguaje de programación |
| Spring Boot | 3.5.7 | Framework principal |
| Spring WebFlux | 3.5.7 | Programación reactiva |
| Spring Data JPA | 3.5.7 | Persistencia de datos |
| H2 Database | 2.3.232 | Base de datos en memoria |
| JWT (jjwt) | 0.12.3 | Autenticación con tokens |
| MapStruct | 1.5.5.Final | Mapeo de objetos |
| Lombok | 1.18.34 | Reducción de código boilerplate |
| OpenAPI Generator | 7.5.0 | Generación de código desde contrato |
| Gradle | 8.x | Herramienta de build |

## 🏗 Arquitectura

El proyecto sigue una arquitectura en capas con los siguientes componentes:

```
┌─────────────────────────────────────────┐
│         REST Controller Layer           │
│    (UserController - WebFlux)           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│  (UserServiceImpl, ValidationService)   │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│      Repository Layer (Reactive)        │
│    (ReactiveUserRepository - JPA)       │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         H2 Database (In-Memory)         │
└─────────────────────────────────────────┘
```

## 📦 Requisitos Previos

- **Java 21** o superior
- **Gradle 8.x** (o usar el wrapper incluido)
- **Puerto 8080** disponible (configurable)

## 🚀 Instalación

1. **Clonar el repositorio:**
```bash
git clone <url-repositorio>
cd msa-user
```

2. **Dar permisos al Gradle Wrapper (Linux/Mac):**
```bash
chmod +x gradlew
```

3. **Compilar el proyecto:**
```bash
./gradlew clean build
```

## ⚙️ Configuración

### application.yaml

El archivo `src/main/resources/application.yaml` contiene toda la configuración:

```yaml
server:
  port: 8080  # Puerto de la aplicación

spring:
  application:
    name: msa-user
  datasource:
    url: jdbc:h2:file:./data/testdb  # Base de datos persistente en disco
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  jpa:
    hibernate:
      ddl-auto: update  # Crea/actualiza tablas automáticamente
    show-sql: true

# Configuración de validaciones (MODIFICABLE)
validation:
  email:
    pattern: ^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$
    message: "El formato del email no es válido"
  password:
    pattern: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$
    message: "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"

# Configuración JWT
jwt:
  secret: mySecretKeyForJwtTokenGenerationMustBeAtLeast256BitsLong
  expiration: 86400000  # 24 horas en milisegundos
```

### Personalización de Validaciones

Puedes modificar los patrones de validación editando los valores en `validation.email.pattern` y `validation.password.pattern`.

**Ejemplos de patrones de password:**

```yaml
# Contraseña fuerte (8+ caracteres, mayúscula, minúscula, número, especial)
pattern: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$

# Contraseña simple (solo 6+ caracteres)
pattern: ^.{6,}$

# Contraseña muy fuerte (12+ caracteres con todos los tipos)
pattern: ^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{12,}$
```

## 🎯 Ejecución

### Opción 1: Usando Gradle

```bash
./gradlew bootRun
```

### Opción 2: Usando JAR

```bash
./gradlew bootJar
java -jar build/libs/msa-user-1.0.0.jar
```

### Opción 3: Desde IDE

Ejecutar la clase principal: `com.smartjob.user.UserApplication`

La aplicación estará disponible en: **http://localhost:8080**

## 📡 Uso de la API

### Endpoint: Registro de Usuario

**POST** `/api/v1/users`

#### Request

```json
{
  "name": "Juan Rodriguez",
  "email": "juan@rodriguez.org",
  "password": "Hunter2!",
  "phones": [
    {
      "number": "1234567",
      "cityCode": "1",
      "countryCode": "57"
    }
  ]
}
```

#### Response Exitoso (201 Created)

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "created": "2025-11-11T10:30:00",
  "modified": "2025-11-11T10:30:00",
  "lastLogin": "2025-11-11T10:30:00",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJlbWFpbCI6Imp1YW5Acm9kcmlndWV6Lm9yZyIsInN1YiI6Imp1YW5Acm9kcmlndWV6Lm9yZyIsImlhdCI6MTYzMjIzNDU2NywiZXhwIjoxNjMyMzIwOTY3fQ.xyz",
  "isActive": true
}
```

#### Response Error - Email Duplicado (409 Conflict)

```json
{
  "mensaje": "El correo ya está registrado"
}
```

#### Response Error - Email Inválido (400 Bad Request)

```json
{
  "mensaje": "El formato del email no es válido"
}
```

#### Response Error - Password Inválido (400 Bad Request)

```json
{
  "mensaje": "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial"
}
```

### Ejemplos con cURL

#### Registro exitoso:

```bash
curl -X POST http://localhost/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Juan Rodriguez",
    "email": "juan@rodriguez.org",
    "password": "Hunter2!",
    "phones": [
      {
        "number": "1234567",
        "cityCode": "1",
        "countryCode": "57"
      }
    ]
  }'
```

#### Registro con email duplicado:

```bash
curl -X POST http://localhost/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Pedro Sanchez",
    "email": "juan@rodriguez.org",
    "password": "Password123!",
    "phones": []
  }'
```

## 🧪 Testing

El proyecto incluye tests para cobertura de:

- ✅ Tests Unitarios de Validación
- ✅ Tests de Integración de Servicios
- ✅ Tests de Utilidades JWT
- ✅ Tests Reactivos con StepVerifier

### Ejecutar todos los tests:

```bash
./gradlew test
```

### Ejecutar tests específicos:

```bash
# Tests de ValidationService
./gradlew test --tests ValidationServiceTest

# Tests de JwtUtil
./gradlew test --tests JwtUtilTest

# Tests de UserService
./gradlew test --tests UserServiceImplTest
```

### Ver reporte de tests:

Después de ejecutar los tests, abre: `build/reports/tests/test/index.html`

### Cobertura de Código

```bash
./gradlew jacocoTestReport
```

Reporte disponible en: `build/reports/jacoco/test/html/index.html`

## 📚 Documentación API

Se encuentra en el archivo: `src/main/resources/openapi/openapi.yaml`

## 📂 Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/smartjob/user/
│   │   ├── config/              # Configuraciones (JWT, Validation, Security)
│   │   ├── controller/          # REST Controllers
│   │   ├── dto/                 # DTOs generados por OpenAPI
│   │   ├── entity/              # Entidades JPA
│   │   ├── exception/           # Excepciones personalizadas
│   │   │   └── handler/         # GlobalExceptionHandler
│   │   ├── mapper/              # Mappers MapStruct
│   │   ├── repository/          # Repositorios JPA y Wrappers Reactivos
│   │   ├── service/             # Servicios de negocio
│   │   │   ├── domain/          # Interfaces de servicio
│   │   │   ├── impl/            # Implementaciones de servicio
│   │   │   └── util/            # Servicios utilitarios
│   │   ├── util/                # Utilidades (JWT)
│   │   └── UserApplication.java # Clase principal
│   └── resources/
│       ├── openapi/
│       │   └── openapi.yaml     # Contrato OpenAPI
│       └── application.yaml     # Configuración de la aplicación
└── test/
    └── java/com/smartjob/user/
        ├── service/
        │   ├── impl/            # Tests de UserServiceImpl
        │   └── util/            # Tests de ValidationService
        └── util/                # Tests de JwtUtil
```

## 🗄️ Base de Datos

### Esquema

La aplicación crea automáticamente las siguientes tablas:

#### Tabla: `users`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | UUID | Primary Key |
| name | VARCHAR | Nombre del usuario |
| email | VARCHAR | Email único |
| password | VARCHAR | Contraseña encriptada (BCrypt) |
| token | VARCHAR | JWT token |
| is_active | BOOLEAN | Estado del usuario |
| created | TIMESTAMP | Fecha de creación |
| modified | TIMESTAMP | Fecha de modificación |
| last_login | TIMESTAMP | Último inicio de sesión |

#### Tabla: `phones`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGINT | Primary Key |
| number | VARCHAR | Número de teléfono |
| city_code | VARCHAR | Código de ciudad |
| country_code | VARCHAR | Código de país |
| user_id | UUID | Foreign Key → users(id) |

## 🔐 Seguridad

### Encriptación de Contraseñas

Las contraseñas se encriptan usando **BCrypt** con un factor de costo de 10.

### JWT Tokens

- Algoritmo: **HS256**
- Expiración: Configurable (por defecto 24 horas)
- Claims incluidos: `userId`, `email`, `iat`, `exp`

### Ejemplo de validación de token:

```java
String token = "eyJhbGciOiJIUzI1NiJ9...";
String email = "juan@rodriguez.org";

boolean isValid = jwtUtil.validateToken(token, email);
UUID userId = jwtUtil.extractUserId(token);
```

## 🐛 Troubleshooting

### Error: Puerto 8080 en uso

Cambiar el puerto en `application.yaml`:

```yaml
server:
  port: 8090
```

### Error: Base de datos bloqueada

Eliminar el archivo de base de datos:

```bash
rm -rf data/
```

### Error: Validación de password muy estricta

Modificar el patrón en `application.yaml` por uno más simple:

```yaml
validation:
  password:
    pattern: ^.{6,}$
    message: "La contraseña debe tener al menos 6 caracteres"
```

## 🔄 Actualizaciones del Contrato OpenAPI

Si modificas el archivo `openapi.yaml`, regenera los DTOs:

```bash
./gradlew clean generateApi build
```

## 📝 Licencia

Este proyecto es de uso educativo y está disponible bajo la licencia MIT.

## 👥 Autor

**David De La Cruz S.**
- Email: david.delacruzs@outlook.com
- LinkedIn: [linkedin.com/in/daviddelacruzs](https://linkedin.com/in/daviddelacruzs)

## 📞 Soporte

Para reportar problemas o sugerencias, crear un issue en el repositorio.

---

**Desarrollado con ❤️ usando Spring Boot + WebFlux**
