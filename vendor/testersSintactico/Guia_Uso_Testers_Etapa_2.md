# Guía de uso de los testers — Etapa 2

Esta guía explica cómo incorporar, ejecutar y ampliar los testers de JUnit entregados para la Etapa 2 (Análisis Sintáctico).

Los testers invocan el método `main` del compilador con un archivo fuente como único argumento y verifican el resultado informado por `System.out`.

## 1. Instalar los testers

Copiar los dos archivos de tester en el directorio de fuentes de prueba del proyecto:

```text
src/test/java/
├── TesterDeCasosSinErrores.java
└── TesterDeCasosConErrores.java
```

También se deben crear, en el directorio raíz del proyecto, las carpetas que contendrán los casos de prueba:

```text
resources/
├── sinErrores/
└── conErrores/
```

La estructura completa queda, por ejemplo:

```text
mi-proyecto/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── ... código del compilador ...
│   └── test/
│       └── java/
│           ├── TesterDeCasosSinErrores.java
│           └── TesterDeCasosConErrores.java
└── resources/
    ├── sinErrores/
    └── conErrores/
```

### Paquete de los testers

La declaración `package` situada al comienzo de cada tester debe ser válida para la organización del proyecto. Si se mantiene una declaración de paquete, el archivo debe ubicarse en la carpeta correspondiente a ese paquete dentro de `src/test/java`.

Por ejemplo, si el tester comienza con:

```java
package minijavaCompiler;
```

su ubicación debe ser:

```text
src/test/java/minijavaCompiler/TesterDeCasosSinErrores.java
```

Si se prefiere ubicar ambos testers directamente en `src/test/java`, se debe quitar la línea `package ...;` de ambos archivos. También puede adaptarse el paquete para que coincida con el que se use en el proyecto.

## 2. Vincular el `main`

Los dos testers contienen una línea similar a esta:

```java
// TODO: el tipo de esta variable init tiene que ser la clase que tiene el main
private static final Main init = null;
```

Reemplazar `Main` por el nombre de la clase que declara el método de entrada del compilador:

```java
public static void main(String[] args)
```

Por ejemplo, si el método está en una clase llamada `ModuloPrincipal`, la línea queda:

```java
private static final ModuloPrincipal init = null;
```

No se debe instanciar la clase: `init` se usa solamente para invocar el método estático mediante:

```java
init.main(args);
```

La clase indicada debe ser accesible desde el paquete del tester. Si pertenece a otro paquete, debe importarse o utilizarse su nombre completamente calificado.

## 3. Ejecutar los testers

Los testers usan JUnit 4 y se ejecutan como pruebas JUnit desde el IDE.

En IntelliJ IDEA:

1. Verificar que `src/test/java` esté marcado como **Test Sources Root**.
2. Abrir uno de los archivos `TesterDeCasos...java`.
3. Ejecutar la clase completa con la opción **Run** de JUnit.

Cada archivo existente dentro de `resources/sinErrores/` o `resources/conErrores/` se ejecuta como un caso parametrizado independiente. Los nombres se ordenan alfabéticamente antes de correrlos.

Los testers capturan lo que el compilador escribe en `System.out`. La variable:

```java
private boolean fullCompilerOuputPrintingInEachTest = true;
```

controla si esa salida también se muestra en la consola de la corrida de JUnit. Puede dejarse en `true` para observar los reportes del compilador mientras se desarrollan los casos.

## 4. Casos sin errores

Los archivos correctos se colocan en:

```text
resources/sinErrores/
```

El `TesterDeCasosSinErrores` ejecuta el compilador sobre cada archivo de esa carpeta y verifica que la salida contenga:

```text
[SinErrores]
```

En esta etapa, el tester de casos correctos **no verifica ni la lista de tokens ni sus lexemas o líneas**. A diferencia del tester usado en la etapa léxica, se limita a comprobar que el programa completo sea sintácticamente válido y que el módulo principal informe éxito.

Ejemplo de archivo `resources/sinErrores/claseMinima.java`:

```java
class A {
}
```

La salida puede incluir un mensaje descriptivo, pero debe incluir el código final de éxito:

```text
[SinErrores]
```

No es necesario agregar anotaciones `//#...` ni un oráculo de EOF a los casos correctos de esta etapa: esas anotaciones pertenecían al tester léxico anterior y no son interpretadas por estos testers.

## 5. Casos con errores

Los archivos inválidos se colocan en:

```text
resources/conErrores/
```

La **primera línea** de cada caso debe contener el código de error esperado como comentario. El formato es:

```java
// [Error:<lexema>|<numeroDeLinea>]
```

El tester lee esa primera línea y, mediante `substring(3)`, obtiene el texto que debe encontrar en la salida. Por ello, deben respetarse exactamente las dos barras y el espacio iniciales.

Ejemplo de archivo `resources/conErrores/faltaIdDeClase.java`:

```java
// [Error:{|1]
class {
}
```

El compilador debe detectar el error sintáctico y escribir un reporte que incluya:

```text
[Error:{|1]
```

El texto descriptivo anterior al código queda a criterio de la implementación, pero debe indicar adecuadamente el problema. El código debe tener exactamente esta estructura:

```text
[Error:Lexema|NroLinea]
```

El lexema y el número de línea deben corresponder al token con el que se detectó el error.

### Error al llegar a EOF

Cuando el error se detecta al llegar al fin del archivo, el lexema del token EOF es vacío. Por lo tanto, el código esperado conserva el separador `:` seguido inmediatamente de `|`:

```java
// [Error:|3]
class A {
```

En este ejemplo, el valor `3` debe reemplazarse por el número de línea que corresponda al EOF según el archivo concreto.

## 6. Agregar nuevos casos

No es necesario modificar el código de los testers al agregar pruebas.

1. Crear un archivo fuente MiniJava.
2. Guardarlo en `resources/sinErrores/` si debe ser aceptado, o en `resources/conErrores/` si debe producir un error.
3. Para un caso con error, escribir en la primera línea el comentario con el código esperado.
4. Ejecutar nuevamente el tester correspondiente.

Los archivos se incorporan automáticamente porque los testers recorren el contenido completo de sus respectivas carpetas.

Ejemplos de nombres útiles:

```text
resources/sinErrores/01_clase_minima.java
resources/sinErrores/02_metodo_con_return.java
resources/conErrores/01_falta_id_clase.java
resources/conErrores/02_llave_faltante.java
```

Usar nombres ordenables ayuda a identificar rápidamente el caso que falla durante la ejecución parametrizada de JUnit.
