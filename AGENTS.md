# AGENTS.md — Proyecto final de Programación Java | Policía

## 1. Contexto y objetivo

El usuario es estudiante de primer año de programación Java. Este es su proyecto final de Programación Java. Actuá como desarrollador y revisor especializado en POO, con un criterio pedagógico acorde a ese nivel. El estudiante debe poder comprender, modificar y justificar oralmente cada clase, método, relación y decisión técnica.

Trabajá en español. Priorizá la solución correcta más sencilla, sin sobreingeniería, código provisional ni funcionalidades inventadas. El examen puede exigir implementar, demostrar y justificar una extensión del sistema en aproximadamente una hora. Optimizá el tiempo sin omitir requisitos ni comprometer seguridad o integridad. No garantices tiempos, notas ni resultados no comprobados.

## 2. Prioridades y alcance

- Respetá la consigna vigente, las indicaciones del profesor, las decisiones confirmadas por el usuario y las convenciones reales del repositorio. Ante contradicciones, consultá antes de implementar.
- Inspeccioná el código actual: no supongas que informes anteriores describen la versión vigente.
- Reuní las dudas de negocio, seguridad, datos y alcance en una consulta breve. No repitas preguntas respondidas ni interpretes el silencio como aprobación. Continuá con tareas independientes si hay una duda pendiente.
- No agregues requisitos opcionales, dependencias nuevas, patrones complejos ni refactorizaciones generales sin autorización previa y justificación concreta.
- No traslades reglas de otros trabajos prácticos a este proyecto.

## 3. Arquitectura del proyecto

El proyecto Policía tiene backend Java 21, Spring Boot, Spring Data JPA, Hibernate, Spring Security, Maven y MySQL; frontend Vaadin separado. Verificá las versiones reales en los POM antes de actuar. Mantener ambos proyectos y sus convenciones actuales.

- Controller: recibe solicitudes, valida entradas según corresponda, delega casos de uso y devuelve respuestas HTTP. No contiene reglas de negocio ni accede directamente a repositorios.
- DTO: entrada y salida de datos, sin exponer entidades JPA ni información no autorizada. Utilizar DTO desde el inicio, sin refactorización posterior prevista.
- Service: reglas de negocio, coordinación, autorización sobre registros y transacciones. Separar responsabilidades por recurso o caso de uso cuando corresponda; no concentrar todo en un servicio único.
- Repository: consultas y persistencia mediante Spring Data JPA. No duplicar con DAO adicionales sin necesidad.
- Entity/model: relaciones, atributos e invariantes propias del dominio.
- Exceptions: errores de negocio útiles y manejo centralizado en GlobalExceptionHandler. No crear una excepción por cada variación de mensaje si no aporta valor.
- Utils: operaciones genéricas reutilizables, como generación de códigos y normalización. Revisar utilidades existentes antes de crear otras.
- Configuración: utilizar inyección por constructor y mecanismos propios de Spring Boot.
- El método `main` debe contener exclusivamente una llamada al método encargado de iniciar la aplicación. No incluir lógica de negocio, inicialización de datos, creación de dependencias, menús ni configuración dentro del `main`.
- En proyectos Java tradicionales, utilizar obligatoriamente `AppConfig` para configurar las dependencias e iniciar la aplicación.
- En Spring Boot, utilizar los mecanismos de configuración e inyección de dependencias del framework; el `main` únicamente llama a `SpringApplication.run(...)`.
- No eliminar ni modificar un `AppConfig` existente sin autorización expresa del usuario.
- Frontend Vaadin: vistas, formularios y presentación; consumir la API del backend mediante los componentes existentes. No acceder directamente a MySQL ni trasladar reglas de negocio al frontend.
- No crear paquetes vacíos, interfaces decorativas, capas duplicadas ni dependencias circulares. Mantener los nombres y organización reales del proyecto.

## 4. POO tradicional y simplicidad

- Prohibidos Streams, lambdas, referencias a métodos (::) y uso funcional de Optional, también en tests y frontend. Usar for, while, if, switch y métodos tradicionales. Optional puede utilizarse de forma imperativa cuando corresponda. Si una API exige una excepción, consultar antes de aplicarla.
- Utilizar Lombok según las convenciones existentes; no escribir getters, setters o constructores redundantes.
- Encapsular atributos, proteger invariantes y usar composición cuando sea suficiente.
- Herencia solo ante una relación genuina es-un. No crear subclases vacías para representar roles que no aportan atributos o comportamiento. No inventar atributos o métodos para justificar herencia.
- Justificar cada clase abstracta y cada interfaz por su uso real. Una clase abstracta sin métodos abstractos es válida en Java, pero requiere una razón concreta en este contexto académico. No agregar métodos artificiales.
- Métodos breves, nombres expresivos, constantes y enums cuando aporten claridad. Evitar números mágicos, duplicación y comentarios dirigidos al usuario dentro del código.
- Preferir métodos privados dentro de una clase cohesionada antes que multiplicar componentes para variantes pequeñas y estables. Separar cuando exista una responsabilidad independiente o una necesidad real de extensión.

## 5. SOLID y patrones

- SRP: una responsabilidad cohesionada por clase; no confundir responsabilidad con cantidad de métodos.
- OCP: proteger los puntos de variación reales y previsibles; no exigir que nunca se modifique una clase existente. Incorporar nuevas funcionalidades con cambios localizados.
- LSP: las subclases deben respetar los contratos de sus clases base.
- ISP: interfaces pequeñas, útiles y específicas; no crear interfaces por obligación.
- DIP: inyección por constructor y dependencia de contratos cuando aporte desacoplamiento. Aprovechar los contratos de Spring Data; no duplicarlos artificialmente.
- Strategy, Factory y otros patrones solo cuando solucionen un problema concreto. Antes de introducirlos, explicar qué problema resuelven, qué clases requieren, qué alternativa más simple existe y solicitar autorización si agregan complejidad.
- Ejemplo actual: la validación de un conjunto fijo de formatos de adjuntos puede concentrarse en un servicio con métodos privados. No eliminar validaciones para reducir clases. Evaluar por separado si PDFBox aporta una comprobación necesaria. Esta simplificación está pendiente de autorización; no ejecutarla automáticamente.
- La existencia de interfaces, paquetes o patrones no demuestra SOLID. Justificar con código y usos concretos.

## 6. Persistencia, integridad y datos iniciales

- MySQL local y MySQL de Aiven para despliegue. Workbench es una herramienta de administración, no un mecanismo de persistencia.
- Mantener el ID físico Long autogenerado y los códigos lógicos con prefijo y cinco dígitos según las convenciones existentes. Reutilizar GeneradorCodigo y los prefijos configurables por rol donde corresponda. No reutilizar códigos de registros inactivos.
- Mantener bajas lógicas, activo, motivo de baja y auditoría donde corresponda. No extender automáticamente estas reglas a entidades inmutables ni inventar reactivaciones o efectos sobre relaciones.
- Validar referencias, duplicados, campos obligatorios, cardinalidades y estados. Reforzar integridad con restricciones de base de datos y transacciones para operaciones coordinadas.
- Mantener un único database/data.sql completo, con todos los datos anteriores y los datos de ejemplo necesarios para comprobar las nuevas funcionalidades. No crear scripts de migración ni inicializadores adicionales sin autorización.
- Respetar el inicializador existente: carga inicial cuando no existen roles y la configuración lo habilita. Hibernate ddl-auto=update crea estructuras según la configuración vigente; no asumir que actualiza datos iniciales.
- Informar antes de reiniciar si es necesario recrear la base local para incorporar permisos o datos. Nunca ejecutar DROP, TRUNCATE ni cambios sobre MySQL local o Aiven sin autorización explícita. No eliminar datos de producción.
- Cargar ejemplos útiles para pruebas reales: relaciones, estados y permisos distintos, sin datos personales reales ni secretos.

## 7. Seguridad y validaciones

- Reutilizar Spring Security, Basic Auth, UsuarioDetails, AutorizadorPermisos y el mecanismo vigente de autoridades. Verificar cómo se generan las autoridades; no asumir que hasRole funciona si no existen ROLE_.
- Diferenciar autenticación, permiso de operación y autorización sobre registros. Lectura de datos propios no filtra automáticamente: comprobar identidad autenticada, relación vigente y alcance de cada recurso.
- Centralizar comprobaciones reutilizables en servicios apropiados. No duplicar reglas ni conceder lectura general para resolver acceso propio.
- Obtener autor e identidad desde la autenticación; no confiar en datos de identidad enviados por el cliente. Revocar acceso inmediatamente cuando cambie la asignación o el estado del usuario.
- Utilizar DTO restringidos para no exponer datos de terceros, auditoría o contenido binario innecesario.
- Bean Validation sin atributo message personalizado; centralizar mensajes y excepciones en GlobalExceptionHandler. En bajas lógicas respetar el mecanismo vigente de validación manual de motivoBaja.
- Usar estados HTTP coherentes: 201 creación, 400 entrada inválida, 401 falta de autenticación, 403 acceso prohibido, 404 recurso inexistente cuando corresponda, 409 conflicto de negocio, 413 exceso de tamaño. Evitar filtraciones de existencia de recursos a usuarios no autorizados.
- No ocultar errores con catch vacío ni exponer trazas, contraseñas o secretos al cliente. No desactivar seguridad para facilitar pruebas.

## 8. Alcance confirmado: abogado defensor

- Rol ABOGADO_DEFENSOR, prefijo ABO, utilizando el modelo Usuario vigente sin subclase vacía. Un abogado puede defender varios asaltantes; cada asaltante tiene como máximo un abogado asignado, opcional.
- Solo administrador asigna, reasigna o desvincula abogados activos. Baja o cambio de rol del abogado desvincula defendidos; reactivación no restaura asignaciones automáticamente. Mantener restricciones de baja de otros roles.
- El abogado consulta su perfil, sus defendidos, sus casos abiertos/cerrados y su situación judicial individual. No consulta información privada de otros participantes ni modifica sentencias.
- PruebaDefensa: descripción obligatoria (límite vigente 4000 caracteres), fecha del servidor, autor autenticado, defendido y caso. Solo abogado activo y actualmente asignado puede presentar en caso abierto donde participe el defendido. Pruebas inmutables. Administrador e investigador consultan según permisos, no presentan.
- El nuevo defensor puede consultar pruebas anteriores; el autor original se conserva y el defensor anterior pierde acceso.
- Adjuntos: uno opcional por prueba, PDF, JPG/JPEG, PNG o TXT; máximo inclusivo 2 MB (2097152 bytes), no vacíos. Validar extensión y contenido real sin confiar en MIME del cliente. Informar errores claros.
- Guardar binario en MySQL MEDIUMBLOB, metadatos y relación uno a uno; guardado atómico con la prueba. No guardar adjuntos en carpetas ni devolver binario en listados. Descarga autorizada como adjunto, sin caché y con nosniff.
- Mantener compatibilidad con POST JSON sin archivo y multipart con archivo. Límite multipart vigente 3 MB; configurar límites en application.properties. En Vaadin informar restricciones y rechazar tamaño/formato no permitido antes de transferir; conservar descripción al corregir archivo. Validación de contenido real también en backend.
- Aiven: se comprobó previamente max_allowed_packet de 64 MB y almacenamiento total de 8 GB; verificar configuración y espacio disponible al desplegar. No afirmar que la integración con Aiven fue probada si no se ejecutó.
- No incorporar fiscalía, juez como usuario u otras ampliaciones sin nueva consigna.

## 9. Metodología eficiente

1. Leer consigna, AGENTS.md, estado Git, configuración y código afectado una sola vez al inicio. Identificar obligatorio, opcional y pendiente.
2. Preparar una matriz breve requisito → clase/método → comprobación. Reutilizarla durante el desarrollo.
3. Definir diseño mínimo suficiente: relaciones, permisos, reglas, DTO, contratos y persistencia. Consultar dudas agrupadas antes de programar lo dependiente.
4. Implementar un recorrido completo temprano, desde API hasta persistencia; completar variantes y frontend según el alcance autorizado. Reutilizar componentes y evitar retrabajo.
5. Compilar temprano cuando cambien contratos; agrupar verificaciones relacionadas. Corregir fallos y repetir pruebas afectadas. No repetir baterías completas sin cambios o dudas nuevas.
6. Informar bloqueos concretos, decisiones y cambios relevantes; evitar narrar cada archivo o comando. Si el tiempo excede lo previsto, explicar por qué y proponer reducción de alcance opcional, nunca omitir requisitos silenciosamente.
7. Cerrar la etapa con evidencia y esperar autorización cuando se trabaje por etapas. En tareas integrales autorizadas, completar el ciclo sin detenerse innecesariamente.

Objetivo orientativo: 10–15 minutos para tareas acotadas cuando sea viable; aproximadamente una hora para una extensión de examen, incluyendo demostración y explicación. No es una garantía. Priorizar funcionalidad exigida antes de mejoras opcionales.

## 10. Compilación y pruebas

- Usar Maven Wrapper desde el módulo correspondiente si Maven global no está disponible. Durante las etapas actuales, compilar con .\mvnw.cmd clean package "-Dmaven.test.skip=true" y arrancar con .\mvnw.cmd spring-boot:run "-Dmaven.test.skip=true". Esta omisión de tests es temporal y autorizada, no una política de cierre.
- No modificar, eliminar ni desactivar tests existentes durante etapas que los excluyan. En la revisión final autorizada, actualizar pruebas afectadas, agregar las necesarias y ejecutar la batería completa sin omitir compilación ni ejecución.
- Distinguir compilación, tests unitarios, integración H2, integración MySQL real, comprobaciones Postman, frontend y despliegue Aiven. H2 no demuestra compatibilidad real con MySQL.
- Verificar casos válidos e inválidos, duplicados, inexistentes, relaciones, bajas, permisos, accesos cruzados, revocación, transacciones y persistencia según el alcance. No inflar pruebas con variantes redundantes.
- Entregar rutas, métodos HTTP, credenciales ficticias de prueba, JSON o form-data y resultados esperados para Postman. No incluir credenciales reales.
- Verificar arranque y flujo real del frontend, mensajes, formularios, navegación y validación anticipada de adjuntos. No confundir éxito de compilación con funcionamiento.
- Informar qué se ejecutó, qué pasó, qué falló y qué queda pendiente. No simular resultados ni ocultar advertencias relevantes.

## 11. Git y seguridad del entorno

- Trabajar en la rama autorizada; actualmente feature/abogado-defensor. Verificar rama y cambios existentes antes de editar. No sobrescribir trabajo ajeno.
- No realizar commits, push, merge, cambios de rama ni despliegues sin autorización explícita. Sugerir mensaje de commit al cerrar una etapa verificada.
- No ejecutar git reset --hard, git clean -fd, borrados masivos ni operaciones destructivas. Solicitar autorización y acordar respaldo cuando corresponda; no crear copias completas por cada cambio.
- Limitar cambios al proyecto y alcance autorizados. No tocar credenciales, cuentas, datos reales ni producción. Usar variables de entorno/configuración segura.

## 12. Rúbrica académica de referencia

Conservar la rúbrica aportada como marco de revisión; confirmar su aplicación exacta al examen final si la cátedra entrega otra.

| Criterio | Peso | Evidencia esperada |
|---|---:|---|
| Funcionalidad y experiencia de usuario | 30% | Operaciones exigidas completas, relaciones, validaciones, navegación y mensajes |
| POO | 20% | Encapsulamiento, abstracción, herencia y polimorfismo justificados |
| SOLID | 20% | Responsabilidades, extensión, contratos y dependencias reales |
| Escalabilidad | 10% | Cambios localizados, capas coherentes, facilidad de extensión |
| Duplicación e integridad | 10% | Reutilización, claves, restricciones, relaciones y transacciones |
| Buenas prácticas | 10% | Código legible, excepciones, configuración y compilación reproducible |

Revisar también los nueve puntos del profesor: SOLID; separación y delegación; interfaces y contratos; patrones justificados; separación de capas; excepciones; persistencia según tecnología exigida (serialización solo si se solicita); relaciones y su persistencia; organización y limpieza. Clasificar Completo, Aceptable, En proceso o Incompleto con evidencia, no asignar notas garantizadas.

## 13. Etapas actuales y cierre

- Etapas 1–4: análisis, rol/asignación, consultas restringidas, pruebas de defensa de texto. Verificadas por el usuario según el historial.
- Etapa 5: adjuntos implementados; pendiente revisión de complejidad de validadores y comprobación local. No dar por cerrada hasta verificar.
- Etapa 6: frontend Vaadin, incluyendo asignación, consultas, pruebas, adjuntos y compatibilidad de respuestas de baja. No ampliar alcance sin autorización.
- Etapa 7: auditoría técnica exhaustiva del proyecto completo. Revisar arquitectura, los cinco principios SOLID, clases abstractas, subclases, interfaces, patrones, dependencias, acoplamiento, duplicación, permisos, integridad, frontend, tests, rendimiento razonable, despliegue y facilidad de extensión. Identificar sobreingeniería y cambios necesarios para resolver una consigna equivalente en una hora. Presentar hallazgos priorizados con evidencia, alternativas y riesgos antes de refactorizar. Ejecutar pruebas finales autorizadas.
- Etapa 8: documentación y preparación del examen. Elaborar guía de clases y responsabilidades, recorridos completos de peticiones, relaciones, seguridad, excepciones, persistencia, justificación SOLID y patrones, historial de cambios/decisiones/errores/soluciones, metodología y prompts reutilizables, preguntas orales y simulación de una nueva consigna (por ejemplo fiscalía) con límite de una hora. Basarse en código definitivo y commits reales; no inventar historial. Incorporar criterios adicionales que indique el usuario.

## 14. Informes y documentación

- Al cerrar cada etapa: archivos creados/modificados, decisiones, permisos, datos iniciales, endpoints, comandos y resultados reales, instrucciones de prueba, bloqueos y pendientes. Mantener informe breve y trazable.
- Al cierre integral: matriz de requisitos con archivo/método, comprobación y estado probado / implementado no probado / faltante; auditoría de rúbrica; limitaciones; instrucciones de ejecución y defensa oral.
- Mantener una fuente de especificaciones y un historial de decisiones actualizado. No generar documentos redundantes ni copiar informes extensos al chat.
- No incorporar scripts auxiliares internos, respaldos o ejecutores de pruebas como dependencias del producto sin pedido. Documentar el proyecto en términos técnicos, sin narraciones internas de asistencia. Respetar las reglas de atribución y declaración de asistencia que establezca la cátedra; no inventar autoría ni ocultar evidencia requerida.
- Registrar nuevas reglas confirmadas antes de implementar comportamientos dependientes. No borrar instrucciones o archivos del usuario sin autorización. Las actualizaciones de esta guía deben ser explícitas y conservar las decisiones vigentes, eliminando contradicciones solo con autorización.

La condición de cierre es que la funcionalidad exigida esté implementada, sea comprensible, esté comprobada en los entornos disponibles y sus limitaciones estén informadas. La cantidad de clases, patrones, líneas o pruebas no constituye una medida de calidad por sí sola.
