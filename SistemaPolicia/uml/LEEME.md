# UML mejorado del Sistema Policial

`PoliciaFederal-mejorado.uxf` fue reconstruido a partir del código Java ubicado en `src` y mantiene el estilo de UMLetino del diagrama original.

Organización visual:

- `VIEWS` → `CONTROLLERS` → `DAOS` → `MODELS`.
- `DTOS` debajo de las vistas.
- `EXCEPTIONS` debajo de los DAO.
- Las relaciones del dominio se concentran dentro de `MODELS`.

Notación:

- Triángulo con línea continua: herencia.
- Triángulo con línea discontinua: implementación de interfaz.
- Línea continua: asociación.
- Línea discontinua con flecha: dependencia.
- Rombo vacío: agregación.
- `1`, `0..1` y `0..*`: multiplicidades.

Para regenerar el archivo después de modificar el código, ejecutar `generar-uml-mejorado.ps1` indicando `src` mediante `-SourceRoot` si fuera necesario.
