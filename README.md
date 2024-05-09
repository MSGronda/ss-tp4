# Trabajo Práctico 4 - SS

## Requisitos:
1) Java 17 o mayor
2) Python 3
2) Pip 3

## Preparacion del entorno:
Ejecutar en una linea de comnados (con pip instalado):
```
pip install -r requirements.txt
```
## Sistema 1 - Ejecucion de simulación:
1. Editar configuracion de funcion main en el archivo Main.
```agsl
double mass = 70;
double springConstant = 10000;
double gamma = 100;
double totalTime = 5;
SimulationType type = SimulationType.GEAR_PREDICTOR_CORRECTOR;

double[] deltaTs = {0.000001, 0.00001, 0.0001, 0.001, 0.01};
```
2. Ejecutar funcion main en archivo Main.
3. La salida la simulacion se encuentra en ```python/ej1/output-files```.

## Graficos
Las funciones para generar los graficos se encuentran en ```python/ej1/src/main.py```.
Debe elegir la correspondiente y ejecutar el archivo:
```
python3 python/ej1/src/main.py
```


## Sistema 2 - Ejecucion de simulación:
1. Editar configuracion de funcion main en el archivo Main.
```agsl
double deltaT = 500.0;
double spaceshipOrbitSpeed = SPACE_STATION_SPEED + 8;
double cutoffDistance = 1500;
double cutoffTime = 2 * 365 * SECONDS_IN_DAY;
double startTime = 173 * SECONDS_IN_DAY + 339 * SECONDS_IN_MINUTE;
```
2. Ejecutar funcion main en archivo Main.
3. La salida la simulacion se encuentra en ```python/ej2/output-files```.


## Graficos y Animaciones
Las funciones para generar los graficos y animaciones se encuentran en ```python/ej2/src/main.py```.
Debe elegir la correspondiente y ejecutar el archivo:
```
python3 python/ej2/src/main.py
```
La salida las animaciones se encuentran en  ```python/ej2/animations```.
