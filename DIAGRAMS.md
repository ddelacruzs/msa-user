# Diagramas de la Solución - MSA User

## 1. Diagrama de Arquitectura General

```mermaid
graph TB
    subgraph "Cliente"
        A[REST Client<br/>cURL/Postman/Browser]
    end
    
    subgraph "API Layer - Spring WebFlux"
        B[UserController<br/>REST Endpoint]
        C[GlobalExceptionHandler<br/>Manejo de Errores]
    end
    
    subgraph "Service Layer"
        D[UserServiceImpl<br/>Lógica de Negocio]
        E[ValidationService<br/>Validación de Datos]
        F[JwtUtil<br/>Generación JWT]
    end
    
    subgraph "Mapping Layer"
        G[UserMapper<br/>MapStruct]
    end
    
    subgraph "Repository Layer"
        H[ReactiveUserRepository<br/>Wrapper Reactivo]
        I[UserRepository<br/>JPA Repository]
    end
    
    subgraph "Persistence Layer"
        J[(H2 Database<br/>In-Memory)]
    end
    
    subgraph "Configuration"
        K[ValidationProperties<br/>application.yaml]
        L[JwtProperties<br/>application.yaml]
        M[SecurityConfig<br/>BCrypt]
    end
    
    A -->|HTTP POST /api/v1/users| B
    B -->|Validar Request| D
    B -.->|En caso de error| C
    D -->|Validar Email/Password| E
    D -->|Mapear DTO → Entity| G
    D -->|Verificar Email Existe| H
    D -->|Encriptar Password| M
    D -->|Generar Token| F
    D -->|Guardar Usuario| H
    D -->|Mapear Entity → DTO| G
    E -->|Usar Regex| K
    F -->|Usar Secret| L
    H -->|Delegar a JPA| I
    I -->|SQL| J
    C -->|JSON Error| A
    B -->|JSON Response| A
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#ffe1e1
    style D fill:#e1ffe1
    style E fill:#e1ffe1
    style F fill:#e1ffe1
    style G fill:#f0e1ff
    style H fill:#ffe1f0
    style I fill:#ffe1f0
    style J fill:#ffd700
    style K fill:#e1e1e1
    style L fill:#e1e1e1
    style M fill:#e1e1e1
```

## 2. Diagrama de Flujo - Registro de Usuario

```mermaid
sequenceDiagram
    participant C as Cliente
    participant UC as UserController
    participant US as UserService
    participant VS as ValidationService
    participant UR as UserRepository
    participant DB as H2 Database
    participant JU as JwtUtil
    participant PE as PasswordEncoder

    C->>UC: POST /api/v1/users<br/>{name, email, password, phones}
    
    UC->>US: createUser(request)
    
    US->>VS: validateEmailAndPassword(email, password)
    alt Email inválido
        VS-->>US: InvalidEmailFormatException
        US-->>UC: Error
        UC-->>C: 400 Bad Request<br/>{"mensaje": "Email inválido"}
    else Password inválido
        VS-->>US: InvalidPasswordFormatException
        US-->>UC: Error
        UC-->>C: 400 Bad Request<br/>{"mensaje": "Password inválido"}
    end
    
    US->>UR: existsByEmail(email)
    UR->>DB: SELECT COUNT(*) WHERE email = ?
    DB-->>UR: count
    
    alt Email ya existe
        UR-->>US: true
        US-->>UC: EmailAlreadyExistsException
        UC-->>C: 409 Conflict<br/>{"mensaje": "El correo ya está registrado"}
    end
    
    US->>US: buildUserEntity(request)
    US->>US: Generar UUID
    US->>PE: encode(password)
    PE-->>US: encryptedPassword
    US->>US: setPassword(encryptedPassword)
    
    US->>UR: save(user)
    UR->>DB: INSERT INTO users...
    DB-->>UR: usuario guardado
    UR-->>US: savedUser
    
    US->>JU: generateToken(userId, email)
    JU-->>US: jwtToken
    US->>US: setToken(jwtToken)
    
    US->>UC: PostUserResponse
    UC->>C: 201 Created<br/>{id, created, modified, lastLogin, token, isActive}
```

## 3. Diagrama de Componentes

```mermaid
graph LR
    subgraph "External"
        EXT[Cliente HTTP]
    end
    
    subgraph "Presentation Layer"
        CTR[UserController]
        EXH[GlobalExceptionHandler]
    end
    
    subgraph "Business Layer"
        USI[UserServiceImpl]
        VAL[ValidationService]
    end
    
    subgraph "Infrastructure Layer"
        subgraph "Utilities"
            JWT[JwtUtil]
            ENC[PasswordEncoder]
        end
        
        subgraph "Data Access"
            RUR[ReactiveUserRepository]
            UR[UserRepository<br/>JPA]
        end
        
        subgraph "Mapping"
            MAP[UserMapper<br/>MapStruct]
        end
    end
    
    subgraph "Data Layer"
        H2[(H2 Database)]
    end
    
    subgraph "Configuration"
        CFG[application.yaml]
    end
    
    EXT --> CTR
    CTR --> USI
    CTR -.-> EXH
    USI --> VAL
    USI --> JWT
    USI --> ENC
    USI --> MAP
    USI --> RUR
    RUR --> UR
    UR --> H2
    VAL --> CFG
    JWT --> CFG
    
    style EXT fill:#e1f5ff
    style CTR fill:#fff4e1
    style EXH fill:#ffe1e1
    style USI fill:#e1ffe1
    style VAL fill:#e1ffe1
    style JWT fill:#f0e1ff
    style ENC fill:#f0e1ff
    style MAP fill:#f0e1ff
    style RUR fill:#ffe1f0
    style UR fill:#ffe1f0
    style H2 fill:#ffd700
    style CFG fill:#e1e1e1
```

## 4. Modelo de Datos (Entidad-Relación)

```mermaid
erDiagram
    USER ||--o{ PHONE : has
    
    USER {
        UUID id PK
        VARCHAR name
        VARCHAR email UK
        VARCHAR password
        VARCHAR token
        BOOLEAN is_active
        TIMESTAMP created
        TIMESTAMP modified
        TIMESTAMP last_login
    }
    
    PHONE {
        BIGINT id PK
        VARCHAR number
        VARCHAR city_code
        VARCHAR country_code
        UUID user_id FK
    }
```

## 5. Diagrama de Clases Principales

```mermaid
classDiagram
    class UserController {
        -UserService userService
        +postUser(PostUserRequest) Mono~PostUserResponse~
    }
    
    class UserServiceImpl {
        -ValidationService validationService
        -ReactiveUserRepository repository
        -PasswordEncoder passwordEncoder
        -JwtUtil jwtUtil
        -UserMapper mapper
        +createUser(PostUserRequest) Mono~PostUserResponse~
        -validateEmailNotExists(String) Mono~Void~
        -buildUserEntity(PostUserRequest) UserEntity
        -generateAndAssignToken(UserEntity) Mono~UserEntity~
    }
    
    class ValidationService {
        -ValidationProperties properties
        +validateEmail(String) Mono~Void~
        +validatePassword(String) Mono~Void~
        +validateEmailAndPassword(String, String) Mono~Void~
    }
    
    class JwtUtil {
        -JwtProperties properties
        +generateToken(UUID, String) String
        +extractUserId(String) UUID
        +extractEmail(String) String
        +validateToken(String, String) boolean
    }
    
    class UserEntity {
        -UUID id
        -String name
        -String email
        -String password
        -String token
        -Boolean isActive
        -LocalDateTime created
        -LocalDateTime modified
        -LocalDateTime lastLogin
        -List~PhoneEntity~ phones
    }
    
    class PhoneEntity {
        -Long id
        -String number
        -String cityCode
        -String countryCode
        -UserEntity user
    }
    
    class PostUserRequest {
        -String name
        -String email
        -String password
        -List~Phone~ phones
    }
    
    class PostUserResponse {
        -UUID id
        -LocalDateTime created
        -LocalDateTime modified
        -LocalDateTime lastLogin
        -String token
        -Boolean isActive
    }
    
    UserController --> UserServiceImpl
    UserServiceImpl --> ValidationService
    UserServiceImpl --> JwtUtil
    UserServiceImpl --> UserEntity
    UserEntity "1" --> "*" PhoneEntity
    UserController ..> PostUserRequest
    UserController ..> PostUserResponse
```

## 6. Diagrama de Estados del Usuario

```mermaid
stateDiagram-v2
    [*] --> Validando: POST /api/v1/users
    
    Validando --> Error_Email: Email inválido
    Validando --> Error_Password: Password inválido
    Validando --> Verificando: Validación OK
    
    Verificando --> Error_Duplicado: Email ya existe
    Verificando --> Creando: Email disponible
    
    Creando --> Encriptando: Usuario creado
    Encriptando --> Generando_Token: Password encriptado
    Generando_Token --> Persistiendo: Token generado
    
    Persistiendo --> Activo: Usuario guardado
    Activo --> [*]: Response 201
    
    Error_Email --> [*]: Response 400
    Error_Password --> [*]: Response 400
    Error_Duplicado --> [*]: Response 409
```

## 7. Diagrama de Despliegue

```mermaid
graph TB
    subgraph "Client Tier"
        CLI[Cliente<br/>Web/Mobile/API]
    end
    
    subgraph "Application Server - Puerto 8080"
        subgraph "Spring Boot Application"
            WF[WebFlux<br/>Netty Server]
            APP[MSA User<br/>Application]
            H2DB[(H2 Database<br/>File-based)]
        end
    end
    
    CLI -->|HTTP/JSON| WF
    WF -->|Request| APP
    APP -->|JDBC| H2DB
    
    style CLI fill:#e1f5ff
    style WF fill:#fff4e1
    style APP fill:#e1ffe1
    style H2DB fill:#ffd700
```

## 8. Flujo de Validación Configurable

```mermaid
flowchart TD
    A[Recibir Request] --> B{Validar Email}
    B -->|Regex desde<br/>application.yaml| C[Pattern.matches]
    C -->|No cumple| D[InvalidEmailFormatException]
    C -->|Cumple| E{Validar Password}
    
    E -->|Regex desde<br/>application.yaml| F[Pattern.matches]
    F -->|No cumple| G[InvalidPasswordFormatException]
    F -->|Cumple| H{Email existe en BD?}
    
    H -->|Sí| I[EmailAlreadyExistsException]
    H -->|No| J[Continuar con registro]
    
    D --> K[Return 400]
    G --> K
    I --> L[Return 409]
    J --> M[Return 201]
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style E fill:#fff4e1
    style H fill:#ffe1f0
    style D fill:#ffe1e1
    style G fill:#ffe1e1
    style I fill:#ffe1e1
    style J fill:#e1ffe1
    style M fill:#e1ffe1
```

---

## Leyenda de Colores

- 🔵 **Azul claro**: Cliente/Entrada
- 🟡 **Amarillo**: Controllers/API
- 🟢 **Verde**: Servicios/Lógica
- 🟣 **Morado**: Utilidades/Mappers
- 🔴 **Rojo**: Errores/Excepciones
- 🟠 **Naranja**: Base de Datos
- ⚪ **Gris**: Configuración

## Herramientas Utilizadas

- **Mermaid**: Para todos los diagramas
- Renderizar en: GitHub, GitLab, VS Code (con extensión), o [Mermaid Live Editor](https://mermaid.live/)
