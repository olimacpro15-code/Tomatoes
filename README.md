# Tomatoes

Plataforma de microservicios para la gestión de **insumos, cosechas y empleados** de una empresa agroindustrial.
Proyecto integrador de aula, Desarrollo de Aplicaciones Empresariales.

## Arquitectura

| Carpeta | Responsabilidad | Base de datos (MongoDB Atlas) | Puerto local |
|---|---|---|---|
| `web-agrogestion` | Interfaz Thymeleaf + Bootstrap, seguridad por roles | no tiene | 8080 |
| `ms-empleados` | Login (emite JWT), empleados, roles, jornadas | `agro_empleados` | 8081 |
| `ms-insumos` | Insumos, proveedores, entradas y salidas | `agro_insumos` | 8082 |
| `ms-cosechas` | Lotes, cosechas, producción, reportes | `agro_cosechas` | 8083 |

Cada microservicio tiene la estructura `Controlador / Entidades / Repositorio / Servicio / Dto / Seguridad / Config / Excepciones`.

## Ejecutar en local (Spring Tools for Eclipse)

1. Requisitos: JDK 17, Maven (viene con Spring Tools) y acceso a Internet para MongoDB Atlas.
2. El archivo `.env` de la raíz ya contiene `MONGODB_URI` y `JWT_SECRET`. **No lo subas a GitHub** (está en `.gitignore`).
3. En Atlas: *Network Access* → agregar `0.0.0.0/0` (necesario también para Render).
4. `File → Import → Maven → Existing Maven Projects` y seleccione la carpeta `Tomatoes` (importa los cuatro proyectos).
5. Ejecute cada proyecto con *Run As → Spring Boot App*, en este orden: `ms-empleados`, `ms-insumos`, `ms-cosechas`, `web-agrogestion`.
6. Abra <http://localhost:8080> e ingrese con una cuenta de demostración (ver abajo).

Con Maven por consola: `mvn spring-boot:run` dentro de cada carpeta.

### Cuentas de demostración (se crean solas la primera vez)

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | admin@tomatoes.com | Admin123* |
| Gerente | gerente@tomatoes.com | Gerente123* |
| Supervisor de cosechas | supervisor@tomatoes.com | Supervisor123* |
| Encargado de bodega | bodega@tomatoes.com | Bodega123* |
| Operario | operario@tomatoes.com / operario2@tomatoes.com | Operario123* |

Para desactivarlas: variable `SEED_DEMO=false` en `ms-empleados`. Cambie las contraseñas antes de usar datos reales.

## Documentación de la API (OpenAPI / Swagger)

Cada microservicio publica su documentación interactiva:

- <http://localhost:8081/swagger-ui.html> (empleados)
- <http://localhost:8082/swagger-ui.html> (insumos)
- <http://localhost:8083/swagger-ui.html> (cosechas)

Pasos: `POST /api/auth/login` en el Swagger de empleados → copiar el `token` → botón **Authorize** → pegar el token (en cada Swagger).
El JSON crudo de la especificación está en `/v3/api-docs`. Estado de cada servicio: `/actuator/health`.

## Docker

Cada carpeta tiene su `Dockerfile` (compilación con Maven y ejecución en un JRE liviano; no necesita subir `target/`).

```bash
docker compose up --build        # levanta los cuatro servicios en local
```

## Subir a GitHub y desplegar en Render

```bash
git init
git add .
git status                        # verifique que .env NO aparece en la lista
git commit -m "Tomatoes: version inicial"
git branch -M main
git remote add origin https://github.com/SU_USUARIO/tomatoes.git
git push -u origin main
```

En Render: **New → Blueprint** → elija el repositorio (usa `render.yaml`) y complete las variables:

1. `MONGODB_URI` y `JWT_SECRET` (el **mismo** `JWT_SECRET` en los tres microservicios).
2. Después de crear los servicios, copie la URL pública de cada uno y configure:
   - en `tomatoes-ms-cosechas`: `MS_EMPLEADOS_URL`, `MS_INSUMOS_URL`
   - en `tomatoes-web`: `MS_EMPLEADOS_URL`, `MS_INSUMOS_URL`, `MS_COSECHAS_URL`
3. En el plan gratuito los servicios se duermen por inactividad: abra los tres Swagger y la web unos minutos antes de cada sustentación.
4. Cuando termine las pruebas ponga `MOSTRAR_DEMO=false` en la web.

## Variables de entorno

| Variable | Servicios | Descripción |
|---|---|---|
| `MONGODB_URI` | ms-* | Cadena de conexión de Atlas |
| `MONGODB_DATABASE` | ms-* | Opcional: cambia el nombre de la base (por defecto `agro_empleados`, `agro_insumos`, `agro_cosechas`) |
| `JWT_SECRET` | ms-* | Clave de firma del token (mínimo 32 caracteres, igual en todos) |
| `MS_EMPLEADOS_URL`, `MS_INSUMOS_URL`, `MS_COSECHAS_URL` | ms-cosechas, web | URLs de los otros servicios |
| `PORT` | todos | Lo define Render automáticamente |
| `SEED_DEMO` | ms-empleados | `false` para no crear usuarios de demostración |
| `MOSTRAR_DEMO` | web | `true` muestra las cuentas de demostración en el login |

## Pruebas

`mvn test` dentro de cada microservicio ejecuta las pruebas unitarias (JUnit 5 + Mockito) de la lógica de negocio.
