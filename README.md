# Sabores del Mundo — Backend

API REST desarrollada para el proyecto **Sabores del Mundo** en el contexto del curso **Seguridad y Calidad en el Desarrollo de Software** — DUOC UC, Bimestre 4.

---

## Descripción General

El backend expone una API REST stateless que gestiona recetas gastronómicas de diversas culturas del mundo y la autenticación de usuarios. Toda la comunicación con el frontend se protege mediante tokens **JWT (JSON Web Tokens)**.

---

## Stack Tecnológico

| Tecnología      | Versión    | Rol                             |
| --------------- | ---------- | ------------------------------- |
| Java            | 21         | Lenguaje principal              |
| Spring Boot     | 3.4.3      | Framework base                  |
| Spring Security | (incluido) | Seguridad y autenticación       |
| Spring Data JPA | (incluido) | Persistencia ORM                |
| MySQL           | 8.x        | Base de datos relacional        |
| JJWT            | 0.12.3     | Generación y validación de JWT  |
| Lombok          | (incluido) | Reducción de boilerplate        |
| Maven           | -          | Gestión de dependencias y build |

---

## Estructura del Proyecto

```
src/main/java/com/duoc/backend/
├── BackendApplication.java          # Punto de entrada
├── config/
│   └── SecurityConfig.java          # Configuración de Spring Security y CORS
├── controller/
│   ├── AuthController.java          # Endpoint de login (público)
│   └── RecetaController.java        # Endpoints de recetas (protegidos)
├── dto/
│   ├── LoginRequest.java            # Cuerpo de la petición de login
│   └── LoginResponse.java           # Respuesta con token JWT, rol y usuario
├── entity/
│   ├── Recipe.java                  # Entidad JPA: tabla `recetas`
│   └── User.java                    # Entidad JPA: tabla `usuarios`
├── exception/
│   └── GlobalExceptionHandler.java  # Manejo centralizado de errores
├── repository/
│   ├── RecipeRepository.java        # Repositorio JPA para recetas
│   └── UserRepository.java          # Repositorio JPA para usuarios
├── security/
│   ├── JwtAuthFilter.java           # Filtro que valida el JWT en cada request
│   └── UserDetailsServiceImpl.java  # Implementación de UserDetailsService
└── service/
    ├── DataInitializer.java         # Carga datos iniciales al arrancar
    ├── JwtService.java              # Generación y validación de tokens JWT
    └── RecetaService.java           # Lógica de negocio para recetas
```

---

## Configuración

El archivo de configuración principal es `src/main/resources/application.properties`:

```properties
spring.application.name=sabores-del-mundo-backend
server.port=8081

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/sabores_del_mundo?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# JWT
app.jwt.secret=<secreto-de-al-menos-32-caracteres>
app.jwt.expiration=86400000   # 24 horas en ms
```

> **Nota:** La BD `sabores_del_mundo` se crea automáticamente si no existe gracias al parámetro `createDatabaseIfNotExist=true`.

---

## Modelos de Datos

### `Recipe` (tabla `recetas`)

| Campo              | Tipo           | Descripción                                         |
| ------------------ | -------------- | --------------------------------------------------- |
| `id`               | Long           | PK autoincremental                                  |
| `nombre`           | String         | Nombre de la receta                                 |
| `tipoCocina`       | String         | Tipo de cocina (Italiana, Mexicana…)                |
| `paisOrigen`       | String         | País de origen                                      |
| `dificultad`       | String         | Nivel de dificultad                                 |
| `tiempoCoccion`    | int            | Tiempo en minutos                                   |
| `descripcionCorta` | String         | Resumen breve                                       |
| `descripcion`      | TEXT           | Descripción completa                                |
| `ingredientes`     | List\<String\> | Lista de ingredientes (tabla `receta_ingredientes`) |
| `instrucciones`    | List\<String\> | Pasos de preparación (tabla `receta_instrucciones`) |
| `imagenUrl`        | String         | URL de la imagen                                    |
| `popularidad`      | int            | Puntaje de popularidad                              |
| `reciente`         | boolean        | Marcado como receta reciente                        |

### `User` (tabla `usuarios`)

| Campo      | Tipo   | Descripción                     |
| ---------- | ------ | ------------------------------- |
| `id`       | Long   | PK autoincremental              |
| `username` | String | Nombre de usuario (único)       |
| `password` | String | Contraseña hasheada con BCrypt  |
| `role`     | String | Rol (`ROLE_ADMIN`, `ROLE_USER`) |

---

## API Endpoints

### Autenticación (pública)

| Método | Ruta              | Descripción                            |
| ------ | ----------------- | -------------------------------------- |
| POST   | `/api/auth/login` | Autentica usuario y devuelve token JWT |

**Request body:**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response:**

```json
{
  "token": "<jwt>",
  "type": "Bearer",
  "username": "admin",
  "role": "ROLE_ADMIN"
}
```

---

### Recetas (requieren JWT)

Todos los endpoints de recetas requieren el header:

```
Authorization: Bearer <token>
```

| Método | Ruta                     | Descripción                                       |
| ------ | ------------------------ | ------------------------------------------------- |
| GET    | `/api/recetas`           | Devuelve todas las recetas                        |
| GET    | `/api/recetas/{id}`      | Devuelve una receta por ID                        |
| GET    | `/api/recetas/recientes` | Devuelve recetas marcadas como recientes          |
| GET    | `/api/recetas/populares` | Devuelve recetas ordenadas por popularidad (desc) |
| GET    | `/api/recetas/buscar`    | Búsqueda con filtros opcionales                   |

**Parámetros de búsqueda (`/buscar`):**

| Parámetro     | Requerido | Descripción                       |
| ------------- | --------- | --------------------------------- |
| `nombre`      | No        | Filtro por nombre (contiene)      |
| `tipoCocina`  | No        | Filtro por tipo de cocina         |
| `ingrediente` | No        | Filtro por ingrediente (contiene) |
| `pais`        | No        | Filtro por país de origen         |
| `dificultad`  | No        | Filtro por nivel de dificultad    |

Ejemplo: `GET /api/recetas/buscar?pais=Italia&dificultad=Media`

---

## Seguridad

- **Autenticación:** JWT firmado con HS256. Expiración configurada a 24 horas.
- **Contraseñas:** Hasheadas con `BCryptPasswordEncoder`.
- **Sesiones:** Completamente stateless (`SessionCreationPolicy.STATELESS`). Spring Security no crea ni utiliza `HttpSession`.
- **CSRF:** Deshabilitado (no aplica a APIs REST stateless).
- **CORS:** Configurado para aceptar peticiones desde cualquier puerto en `localhost`. En producción se debe restringir al dominio del frontend.
- **Filtro JWT (`JwtAuthFilter`):** Intercepta cada request, extrae el token del header `Authorization: Bearer <token>`, lo valida y establece la autenticación en el `SecurityContext`.
- **Ruta pública:** Solo `/api/auth/**`. Todas las demás rutas requieren autenticación.
- **Errores 401:** Se retorna directamente un estado HTTP 401 en lugar de redirigir al formulario de login.

---

## Datos Iniciales (Seed)

Al arrancar la aplicación, el componente `DataInitializer` verifica si las tablas están vacías y, de ser así, carga datos de prueba de forma idempotente:

**Usuarios creados:**

| Username  | Password     | Rol          |
| --------- | ------------ | ------------ |
| `admin`   | `admin123`   | `ROLE_ADMIN` |
| `chef`    | `chef123`    | `ROLE_USER`  |
| `usuario` | `usuario123` | `ROLE_USER`  |

**Recetas cargadas:** Un conjunto de recetas de distintas cocinas del mundo (Italiana, Mexicana, etc.) con ingredientes, instrucciones, imágenes e índices de popularidad.

---

## Cómo Ejecutar

### Prerrequisitos

- Java 21+
- Maven 3.8+
- MySQL 8.x en ejecución

### Pasos

1. Crear la base de datos (opcional, se autocrea):

   ```sql
   CREATE DATABASE IF NOT EXISTS sabores_del_mundo;
   ```

2. Ajustar credenciales en `application.properties` si es necesario.

3. Compilar y ejecutar:

   ```bash
   mvn spring-boot:run
   ```

4. El servidor queda disponible en `http://localhost:8081`.

---

## Ejemplo de Flujo de Uso

```bash
# 1. Login — obtener token
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# 2. Usar el token para consultar recetas
curl http://localhost:8081/api/recetas \
  -H "Authorization: Bearer <token>"

# 3. Buscar recetas por país y dificultad
curl "http://localhost:8081/api/recetas/buscar?pais=Italia&dificultad=Media" \
  -H "Authorization: Bearer <token>"
```

---

## Autor

Proyecto académico — DUOC UC  
Curso: Seguridad y Calidad en el Desarrollo de Software  
Bimestre 4
