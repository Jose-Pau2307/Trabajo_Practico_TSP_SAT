Práctica: Reducción del problema TSP a SAT

1. Descripción

Este proyecto implementa una reducción del problema del Viajante de Comercio (TSP) al problema de satisfacibilidad booleana (SAT), utilizando Java y la biblioteca Sat4j.

El programa determina si existe un recorrido que visite todas las ciudades exactamente una vez, regrese a la ciudad inicial y tenga un costo total menor o igual que el límite \(K\).

2. Requisitos

- Java JDK 11 o superior.
- Apache Maven.
- NetBeans u otro entorno compatible con proyectos Maven.
- Conexión a Internet durante la primera descarga de las dependencias.

3. Estructura del proyecto

Practica_TSPSAT/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            ├── Practica_TSPSAT.java
	    └── instancia.txt


La ubicación de `Practica_TSPSAT.java` debe coincidir con la estructura real del proyecto. Si el archivo utiliza una declaración `package`, también debe respetarse la carpeta correspondiente a ese paquete.

4. Dependencias

El proyecto utiliza Sat4j Core, versión 2.3.6, para resolver las cláusulas SAT.

Maven descarga automáticamente esta dependencia al compilar el proyecto. No es necesario agregar manualmente el archivo JAR si el proyecto está configurado correctamente.

5. Formato de entrada

El archivo de entrada contiene:

1. El número de ciudades (N).
2. El límite máximo de costo (K).
3. Una matriz de distancias de (N times N).

Ejemplo de `instancia.txt`:

3 10
0 3 4
3 0 5
4 5 0

El valor `-1` indica que no existe una conexión entre dos ciudades. Las distancias deben ser números enteros no negativos cuando existe una conexión.

6. Compilación

Desde una terminal, ingrese a la carpeta principal del proyecto, donde se encuentra `pom.xml`, y ejecute:

bash
mvn clean compile

Si la compilación termina correctamente, Maven mostrará `BUILD SUCCESS`.

7. Ejecución desde NetBeans

1. Abra el proyecto Maven en NetBeans.
2. Espere a que Maven descargue las dependencias.
3. Compruebe que `Practica_TSPSAT.java` se encuentre en el directorio de código fuente.
4. Ejecute el proyecto configurando la clase principal `Practica_TSPSAT`.
5. Proporcione la ruta de la instancia como argumento del programa.

El programa necesita al menos un argumento: la ruta del archivo de entrada. El segundo argumento es opcional y permite indicar dónde guardar la fórmula CNF.

8. Ejecución desde la terminal

Después de compilar, puede ejecutar la clase con las dependencias de Maven disponibles en el classpath.

La forma general es:
java -cp "<classpath>" Practica_TSPSAT instancia_ejemplo.txt salida.cnf

Reemplace `<classpath>` por la ruta a las clases compiladas y a las bibliotecas de Sat4j. En Windows, los elementos del classpath se separan con punto y coma (`;`); en Linux y macOS se separan con dos puntos (`:`).

El segundo argumento, `salida.cnf`, es opcional. Si se omite, el programa genera el archivo `tsp_reduccion.cnf` en el directorio de ejecución.

9. Funcionamiento

El programa:

1. Lee y valida la instancia.
2. Crea variables booleanas para representar la posición de cada ciudad.
3. Formula las restricciones para que cada ciudad se visite una sola vez.
4. Representa las transiciones entre ciudades consecutivas y el regreso al origen.
5. Codifica la restricción del costo máximo mediante operaciones booleanas.
6. Genera las cláusulas CNF y las guarda en formato DIMACS.
7. Utiliza Sat4j para comprobar la satisfacibilidad.
8. Si existe una solución, reconstruye el recorrido y calcula su costo.

10. Interpretación de resultados

- SAT: se encontró una asignación que satisface las restricciones; el programa presenta la ruta y su costo.
- UNSAT: no existe una ruta que cumpla todas las restricciones para la instancia y el límite \(K\) proporcionados.
- Timeout: el solucionador agotó el tiempo permitido y no pudo determinar el resultado.

11. Pruebas recomendadas

Ejecute el programa con instancias que permitan comprobar:

- Una instancia con una ruta válida cuyo costo no supere (K).
- Una instancia cuyo límite (K) sea demasiado bajo para permitir una ruta.
- Una instancia sin conexiones suficientes para formar un ciclo.
- Una instancia con un archivo incompleto o con datos inválidos.

Registre los resultados reales obtenidos durante la ejecución.

12. Limitaciones

El rendimiento depende del número de ciudades, la cantidad de conexiones y el tamaño de la fórmula CNF generada.

El programa utiliza distancias enteras y representa las conexiones ausentes mediante `-1`. Las instancias deben respetar las validaciones implementadas en el código.

13. Autoría

Asignatura: ESTRUCTURA DE DATOS Y ALGORITMOS II  
Estudiante: Joselyn Pauta 
Institución: Escuela Politécnica Nacional  
Práctica: Reducción de TSP a SAT
