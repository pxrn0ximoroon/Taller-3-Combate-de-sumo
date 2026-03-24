# Control de Versiones con Git

## 1. Estrategia de Ramas
- Se creó un fork del repositorio original en GitHub para trabajar sobre una copia propia.
- **main**: Rama principal donde se integran las funcionalidades ya probadas y decididas.
- **master**: Rama de desarrollo donde se realizan pequeños cambios, pruebas y ajustes antes de pasar a `main`.

## 2. Flujo de Trabajo
1. Se crean nuevas funcionalidades en la rama `master`.
2. Una vez probadas y validadas, se realiza un **merge** hacia la rama `main`.
3. La rama `main` contiene siempre la versión estable del proyecto.

## 3. Historial de Cambios

### Commit inicial
- **Autor**: Ziheaus
- **Rama**: main
- **Acción**: Push
- **Descripción**: Se definieron los estándares de paquetes   
  Se agregó el archivo `.properties` con las **82 técnicas oficiales de kimarite** para ser cargadas desde la carpeta `data`.

- **Commit **: Definicion de estandares de paquetes.
- **Commit **: Se reescribe el archivo propierties.

### Commit 2
- **Autor**: Ziheaus
- **Rama**: master
- **Acción**: Push
- Descripción: 
  - Se elimina el paquete `modelo.servidor` .  
  - Se actualiza la clase `VentanaCliente` . 
  -Se quitan joption.pane y jfilechooser de controlservidor.
  - Los datos de las técnicas pasan de objetos `Kimarite` a representarse como `String`, en vista del cambio del objetio kimarites .

### Commit 3
- **Autor**: Ziheaus
- **Rama**: master
- **Acción**: push
- **Descripción**: 
-  Se cambia kimarite y raikishi en modeloSErvidor.

### Commit 4
- **Autor**: Ziheaus
- **Rama**: master
- **Acción**: Actualización
- **Descripción**:  
  - Se actualiza la clase `KimaroteRender`.  
  - Se realizan ajustes en `VentanaCliente` .  
  - Se añade el controlador de sockets (`ControladorSocket`) para gestionar la comunicación cliente-servidor.  
  - Se incorpora `LauncherCliente` como punto de entrada para la ejecución del cliente.  
  - Se agrega `ControladorCliente` para manejar la lógica de interacción entre la interfaz y el servidor.


### Commit 5
- **Autor**: Ziheaus
- **Rama**: master
- **Acción**: Implementación
- **Descripción**:  
  - Se añade la clase `HiloLuchador` para gestionar la ejecución concurrente de combates entre luchadores.  
  - Se incorpora `ControladorServidor` para manejar la lógica de comunicación y coordinación desde el lado del servidor.  
  - Se crea `VentanaServidor` como interfaz gráfica para visualizar y administrar el estado del servidor.  
  - Se agrega `LauncherServidor` como punto de entrada para la ejecución del servidor.


### Commit 6
- **Autor**: JUl?
- **Rama**: fork
- **Acción**: pull
- **Descripción**: 
  -Se crea script de la base de la base de datos haci como archivos de configuracion


### Commit 7
- **Autor**: Ziheaus
- **Rama**: master
- **Acción**: Implementación
- **Descripción**:  
  - Se añade la clase `Dohyo` para representar el área de combate y gestionar la lógica de los enfrentamientos.  
  - Se incorpora la lógica de persistencia mediante `RandomAccessFile` para almacenar y recuperar resultados de combates.  
  - Se implementa la clase `ConexionDB` para manejar la conexión con la base de datos.  
  - Se crean `RikishiDAO` y `RikishiDAOImpl` siguiendo el patrón DAO, permitiendo operaciones CRUD sobre los luchadores en la base de datos.


## 4. Fusiones (Merge)
- Fecha: [dd/mm/aaaa]
- Rama origen: `master`
- Rama destino: `main`
- Descripción: [Ejemplo: "Se integró la lógica de combate con selección aleatoria de kimarites"].

## 5. Versiones del Proyecto
- **Versión 0.1**: Proyecto inicial con estructura básica.
- **Versión 0.2**: Se implementa lógica de combates concurrentes.
- **Versión 0.3**: Se añade conexión a BD y DAO.
- **Versión final (1.0)**: Proyecto completo con interfaz gráfica y persistencia en archivos.

---
