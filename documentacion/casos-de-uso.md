# 5. Casos de uso y diagramas

## Actores involucrados

- **Administrador:** posee permisos de lectura y escritura. Puede consultar y administrar la información completa del sistema.
- **Investigador:** posee permiso de lectura general. Puede consultar información policial, judicial, bancaria, usuarios y roles.
- **Vigilante:** posee permiso de lectura de datos propios. Puede consultar su perfil y sus contratos.
- El sistema permite crear nuevos roles combinando los permisos existentes, sin crear nuevas clases de usuario.

## Breve descripción de cada caso de uso

| Caso de uso | Actores | Descripción breve |
|---|---|---|
| Iniciar sesión | Todos | Valida el código y la contraseña y habilita las opciones correspondientes a los permisos del usuario. |
| Cerrar sesión | Todos | Finaliza la sesión activa y regresa al acceso, evitando reutilizar los datos anteriores. |
| Consultar mi perfil | Todos | Muestra los datos propios del usuario autenticado sin exponer la contraseña almacenada. |
| Cambiar mi contraseña | Todos | Permite reemplazar la contraseña propia aplicando las validaciones definidas por el backend. |
| Consultar usuarios y roles | Administrador e Investigador | Lista usuarios y roles según los permisos de lectura del actor. |
| Consultar información policial, judicial y bancaria | Administrador e Investigador | Permite buscar y consultar entidades bancarias, sucursales, contratos, bandas, asaltantes, asaltos, jueces y casos judiciales. |
| Consultar mis contratos | Vigilante | Muestra únicamente los contratos asociados al vigilante autenticado. |
| Administrar usuarios y roles | Administrador | Permite crear, modificar, dar de baja o reactivar usuarios y roles, y asignar permisos existentes a cada rol. |
| Administrar vigilantes | Administrador | Permite registrar vigilantes, modificar sus datos y gestionar su estado. |
| Administrar entidades bancarias y sucursales | Administrador | Permite registrar y modificar bancos y sucursales, además de aplicar bajas y reactivaciones respetando sus relaciones. |
| Administrar contratos de vigilancia | Administrador | Permite crear, modificar y cancelar contratos vinculados con un vigilante y una sucursal. |
| Administrar bandas y asaltantes | Administrador | Permite registrar bandas y asaltantes, asociarlos, consultar integrantes y gestionar sus estados. |
| Registrar y administrar asaltos | Administrador | Permite registrar asaltos indicando fecha, sucursal y participantes, y actualizar sus relaciones permitidas. |
| Administrar jueces | Administrador | Permite registrar y modificar jueces, consultar sus casos y gestionar bajas o reactivaciones. |
| Administrar casos judiciales | Administrador | Permite crear casos a partir de un asalto, asignar juez, consultar su estado y gestionar reaperturas por apelación. |
| Registrar y modificar sentencias | Administrador | Registra el resultado individual de cada asaltante involucrado y actualiza automáticamente el estado del caso cuando corresponde. |

