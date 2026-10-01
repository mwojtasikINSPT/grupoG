# 🚔 Policía Backend

Backend desarrollado con **Spring Boot, Spring Data JPA, Hibernate y MySQL**.

## 📌 Incluye actualmente

* 🏦 Entidades bancarias y sucursales
* 👮 Vigilantes, investigadores y administradores
* 🔍 Asaltantes, bandas y asaltos
* ⚖️ Casos judiciales y jueces
* 🗃️ DTOs, Repositories y Services
* 🌐 API REST con operaciones CRUD
* ♻️ Baja lógica mediante `activo`
* 🔑 Generación automática de códigos
* ⚠️ Manejo global de excepciones

## 🛠️ Tecnologías

* ☕ Java 21
* 🌱 Spring Boot
* 🗂️ Spring Data JPA
* 🗄️ Hibernate
* 🐬 MySQL
* 📦 Maven


🗄️ Base de datos

1. Crear la base de datos `policia_db`.
```sql
CREATE DATABASE policia_db;
```
2. Configurar las credenciales en `application-local.properties` (Modificar nombre de EXAMPLE).
3. Ejecutar el Script `database/data.sql` para cargar los datos de prueba.