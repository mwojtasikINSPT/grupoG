# 🚔 Sistema de Gestión Policial

Backend de un sistema de gestión policial desarrollado con **Java 21, Spring Boot, Spring Data JPA, Hibernate y MySQL**. Expone una API REST para administrar entidades bancarias, personal, asaltos, casos judiciales y contratos de vigilancia.

## 📌 Incluye

- 🏦 Gestión de entidades bancarias y sucursales.
- 👮 Gestión de usuarios, vigilantes y jueces.
- 🔍 Asaltantes, bandas y asaltos.
- ⚖️ Casos judiciales y sentencias de asaltantes.
- 🛡️ Contratos de vigilancia.
- 🌐 API REST con operaciones CRUD, DTOs, Services y Repositories.
- ♻️ Baja lógica y conservación del historial.
- 🔑 Generación automática de códigos.
- 🔎 Consultas de entidades relacionadas.
- ⚠️ Validaciones y manejo global de excepciones.
- 📝 Auditoría de creación y modificación.
- 🔐 Autenticación y autorización mediante Spring Security.
- 🧪 Pruebas automatizadas de servicios, repositorios, controladores e integración.

## 🛠️ Tecnologías

- ☕ Java 21
- 🌱 Spring Boot 4.1.1
- 🗂️ Spring Data JPA
- 💤 Hibernate
- 🔐 Spring Security
- 🐬 MySQL
- 📦 Maven
- 🧰 Lombok
- 🧪 JUnit y Mockito
- 🗄️ H2 para pruebas automatizadas

## 📁 Estructura

El backend se encuentra en la carpeta `policia-backend/` y está organizado por capas: `controllers`, `DTOs`, `services`, `repositories`, `models`, `security`, `configs`, `validations`, `exceptions` y `utils`.

## 🚀 Instalación y ejecución

**Requisitos:** JDK 21, Maven y un servidor MySQL en funcionamiento.

1. Clonar el repositorio y abrir la carpeta `policia-backend/` como proyecto Maven.
2. En `policia-backend/src/main/resources/`, copiar `application-local-EXAMPLE.properties` y renombrar la copia a `application-local.properties`.
3. Editar el archivo local con los datos de conexión a MySQL:

   ```properties
   DB_URL=jdbc:mysql://localhost:3306/policia_db?createDatabaseIfNotExist=true
   DB_USERNAME=root
   DB_PASSWORD=TU_PASSWORD
   ```

4. Iniciar la aplicación desde el IDE o, dentro de `policia-backend/`, ejecutar:

   ```bash
   mvn spring-boot:run
   ```

La API queda disponible en `http://localhost:8080`.

## 🗄️ Base de datos

El proyecto utiliza **MySQL** y una base de datos llamada `policia_db`.

### ⚙️ Configuración

1. Copiar `application-local-EXAMPLE.properties` y renombrarlo como `application-local.properties`.
2. Configurar `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con las credenciales locales.
3. Ejecutar la aplicación desde Spring Boot.

### 🚀 Inicialización automática

- La base de datos `policia_db` se crea automáticamente si no existe.
- Hibernate genera o actualiza las tablas necesarias.
- `InicializadorDatos` carga los datos iniciales automáticamente cuando corresponde.
- **No es necesario ejecutar manualmente `data.sql`.**

El archivo `application-local.properties` está excluido de Git para proteger las credenciales.


## 🔐 Seguridad

La API utiliza **HTTP Basic** y permisos de acceso:

- Usuarios con permiso `LECTURA` pueden realizar las consultas generales habilitadas.
- Usuarios con `ESCRITURA` pueden acceder a las operaciones de modificación autorizadas.
- Los vigilantes con `LECTURA_DATOS_PROPIOS` pueden consultar sus propios datos y contratos mediante los endpoints específicos, sin acceder a los de otros vigilantes.

Ejemplo de consulta de contratos propios:

```http
GET /api/vigilantes/VIG00001/contratos
```

Se requiere autenticación válida. Los permisos se comprueban en la configuración de seguridad y, donde corresponde, mediante autorización a nivel de método.

## 🔎 Consultas relacionadas

La API permite consultar, entre otras relaciones:

- Contratos de vigilancia por vigilante o sucursal.
- Asaltos por sucursal o asaltante.
- Sucursales por entidad bancaria.
- Casos judiciales por asaltante o juez.

## 🧪 Pruebas

Desde la carpeta `policia-backend/`:

```bash
mvn test
```

Las pruebas utilizan **H2**, de forma independiente de la base MySQL local. La carga de datos iniciales está deshabilitada en la configuración de pruebas.

## ♻️ Estados, códigos y auditoría

Las entidades aplicables permiten baja lógica mediante el campo `activo`. Se registran fechas y usuarios de creación y modificación cuando corresponde.

Los códigos públicos se generan automáticamente con un prefijo, por ejemplo `SUC00004` o `VIG00001`.

Algunas actualizaciones permiten enviar solo los campos que se desean modificar, conservando los restantes.
