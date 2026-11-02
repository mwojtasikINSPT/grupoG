# 6. Base de datos

La aplicación utiliza una base de datos relacional MySQL administrada mediante Spring Data JPA e Hibernate. Las claves primarias son numéricas y autogeneradas; los códigos de negocio se mantienen como valores únicos para identificar los registros en la interfaz y en la API.

## Descripción de las tablas

| Tabla | Descripción |
|---|---|
| **rol** | Define cada rol disponible, su código, nombre, prefijo, descripción y estado. |
| **rol_permiso** | Tabla asociativa que guarda los permisos asignados a cada rol. |
| **usuario** | Contiene los datos comunes de autenticación y administración de usuarios: código, nombre, contraseña cifrada, rol, estado y motivo de baja. |
| **usuario_generico** | Especialización de usuario utilizada para usuarios que no requieren atributos adicionales. |
| **vigilante** | Especialización de usuario; agrega la edad del vigilante. |
| **entidad_bancaria** | Almacena bancos, domicilio central, estado y motivo de baja. |
| **sucursal** | Almacena las sucursales y las relaciona con su entidad bancaria. Incluye domicilio, cantidad de empleados y motivo de cierre. |
| **contrato_vigilancia** | Representa un servicio de vigilancia para una fecha, vinculado con un vigilante y una sucursal. |
| **banda** | Registra agrupaciones delictivas y su estado. La cantidad de miembros se mantiene de acuerdo con sus integrantes. |
| **asaltante** | Registra asaltantes y permite vincular cada uno con una banda de manera opcional. |
| **asalto** | Registra un hecho delictivo ocurrido en una sucursal y en una fecha determinada. |
| **asalto_asaltante** | Tabla intermedia que resuelve la relación muchos a muchos entre asaltos y asaltantes. |
| **juez** | Registra jueces, la fecha desde la que ejercen y su estado. |
| **caso_judicial** | Representa el caso originado por un asalto. Se vincula con un único asalto y un juez, y almacena su estado judicial. |
| **sentencia_asaltante** | Guarda el resultado individual de un asaltante dentro de un caso: estado, condena y tiempo de cárcel. |

## Descripción de las relaciones

- Un **rol** puede asignarse a muchos usuarios y contiene uno o más permisos.
- Cada **usuario** posee un único rol. usuario_generico y vigilante comparten la misma clave primaria que su registro de usuario debido a la herencia JOINED.
- Una **entidad bancaria** posee muchas sucursales; cada sucursal pertenece a una sola entidad bancaria.
- Una **sucursal** puede tener muchos contratos y registrar muchos asaltos.
- Un **vigilante** puede cumplir muchos contratos; cada contrato corresponde a un vigilante y una sucursal.
- Una **banda** puede agrupar muchos asaltantes; un asaltante puede no pertenecer a ninguna banda o pertenecer a una sola.
- Un **asalto** puede incluir varios asaltantes y un asaltante puede participar en varios asaltos. La relación se almacena en asalto_asaltante.
- Un **asalto** puede originar como máximo un caso judicial, y cada caso pertenece obligatoriamente a un único asalto.
- Un **juez** puede intervenir en muchos casos judiciales; cada caso tiene un solo juez responsable.
- Un **caso judicial** contiene resultados individuales para sus asaltantes. La combinación caso–asaltante no puede repetirse en sentencia_asaltante.
- Las entidades principales incluyen campos de auditoría para registrar fechas y usuarios responsables de creación y modificación.

