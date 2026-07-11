# Sistema de Gestión - Policía Federal

Proyecto desarrollado para la materia Programación II de la Tecnicatura Superior en Informática Aplicada del INSPT, a cargo del Profesor Miguel Silva. 
Este sistema es una aplicación de consola (CLI) orientada a la gestión operativa de seguridad, estructurada bajo un patrón **MVC (Modelo-Vista-Controlador)** y con persistencia de datos en archivos de texto plano.

## 🏗️ Arquitectura del Sistema
El proyecto sigue una separación clara de responsabilidades:

* **`views/`**: Capa de presentación. Contiene el punto de entrada (`Main`) y las clases encargadas de la interacción con el usuario a través de la terminal.
* **`controllers/`**: Contiene la lógica de negocio y la orquestación de servicios.
* **`models/`**: Define las entidades del dominio (Usuarios, Vigilantes, Sucursales, etc.).
* **`daos/`**: Capa de acceso a datos que gestiona la lectura y escritura en archivos `.txt`.
* **`dtos/`**: Objetos para la transferencia de información durante el proceso de autenticación.

## 🔐 Módulos de Acceso
El sistema implementa un control de acceso basado en roles. Al iniciar, el usuario debe autenticarse para acceder a su menú específico:

1. **Administrador**: Gestión total del sistema.
2. **Investigador**: Funcionalidades de consulta sobre todo el sistema.
3. **Vigilante**: Registro y control operativo de sus actividades.

## 💾 Persistencia
La información es persistida en archivos `.txt` ubicados en la raíz del proyecto:
* `usuarios.txt`: Credenciales y roles.
* `vigilantes.txt`: Registro de personal operativo.
* `sucursales.txt`: Datos de locaciones bancarias.
* `contratos_vigilancia.txt`: Información de servicios activos.

## 🚀 Guía de Instalación

### Requisitos
* **JDK**: Compatible con el entorno de desarrollo utilizado.
* **IDE**: NetBeans (configuración original del proyecto).

### Compilación y Ejecución
1. **Clonar el repositorio:**
```bash
git clone https://github.com/mwojtasikINSPT/grupoG.git
```

## 👥 Integrantes del Grupo
* **Rivarola, Guadalupe** - maria.rivarola@alu.inspt.utn.edu.ar
* **Santos, Mayra** - mayra.santos@alu.inspt.utn.edu.ar
* **Wojtasik, Marcela** - marcela.wojtasik@alu.inspt.utn.edu.ar

## ⚖️ Licencia
Este proyecto se encuentra bajo la **Licencia MIT**. Esto significa que es de código abierto y puedes utilizarlo, modificarlo y distribuirlo con fines académicos, siempre manteniendo el reconocimiento de la autoría original.   

## 🛠️ Notas sobre el desarrollo
Este proyecto fue desarrollado por los integrantes del grupo, con asistencia de herramientas de IA para la optimización de la estructura y la documentación del código.