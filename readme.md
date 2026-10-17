# 🚔 Sistema de Gestión Policial

Backend desarrollado con **Spring Boot, Spring Data JPA, Hibernate y MySQL** para la gestión de una base de datos policial.

## 📌 Incluye

* 🏦 Entidades bancarias y sucursales
* 👮 Vigilantes, investigadores, administradores y jueces
* 🔍 Asaltantes, bandas y asaltos
* ⚖️ Casos judiciales y relaciones con jueces y asaltantes
* 🛡️ Contratos de vigilancia
* 🗃️ DTOs, Repositories y Services
* 🌐 API REST con operaciones CRUD
* ♻️ Baja lógica mediante `activo`
* 🔑 Generación automática de códigos
* 🔎 Consultas de entidades relacionadas
* ⚠️ Validaciones mediante Bean Validation
* 🚨 Manejo global de excepciones
* 📝 Auditoría de creación y modificación
* 🔐 Autenticación y autorización mediante Spring Security y permisos

## 🛠️ Tecnologías

* ☕ Java 21
* 🌱 Spring Boot 4.1.1
* 🗂️ Spring Data JPA
* 💤 Hibernate
* 🐬 MySQL
* 📦 Maven
* 🔐 Spring Security
* 🧰 Lombok

## 🏗️ Arquitectura

El proyecto sigue una arquitectura por capas:

```text
Controller → DTO → Service → Repository → Entity → MySQL
```

Los **DTOs se utilizan desde la capa Service** para separar los datos expuestos por la API de las entidades persistidas.

## 🗄️ Base de datos

1. Crear la base de datos `policia_db`:

```sql
CREATE DATABASE policia_db;
```

2. Configurar las credenciales en `application-local.properties` a partir del archivo de ejemplo.

3. Ejecutar el script `database/data.sql` para cargar los datos de prueba.

## 🔐 Seguridad

La API utiliza **HTTP Basic** y maneja los siguientes roles:

* `ADMINISTRADOR`: acceso completo.
* `INVESTIGADOR`: acceso de lectura general.
* `VIGILANTE`: acceso limitado a sus propios datos y consultas permitidas.

## 🔎 Consultas relacionadas

La API incluye consultas específicas para obtener información relacionada, por ejemplo:

* Contratos de vigilancia por sucursal.
* Asaltos por sucursal.
* Sucursales por entidad bancaria.
* Contratos por vigilante.
* Casos judiciales por asaltante o juez.
* Asaltos por asaltante.

## ♻️ Estados y auditoría

Las entidades que corresponden utilizan **baja lógica** mediante `activo`, conservando los registros históricos.

Además, se registra automáticamente:

* Fecha de creación.
* Fecha de modificación.
* Usuario que creó el registro.
* Usuario que realizó la última modificación.

Los códigos públicos de las entidades se generan automáticamente mediante un **prefijo y un identificador numérico**, por ejemplo:

```text
SUC00004
```

Las modificaciones permiten actualizar únicamente los campos enviados cuando corresponde, manteniendo los valores existentes para los campos no informados.
