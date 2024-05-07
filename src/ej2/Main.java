package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static final int SECONDS_IN_DAY = 60 * 60 * 24;
    private static final int SECONDS_IN_HOUR = 60 * 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final double SPACE_STATION_SPEED = 7.12;
    private static final double SPACE_STATION_ORBIT_ELEVATION = 1500;
    private static final Body.BodyType objective = Body.BodyType.MARS;


    public static void main(String[] args) {

        double deltaT = 500.0;                    // En segundos
        double spaceshipOrbitSpeed = SPACE_STATION_SPEED + 8;

        double cutoffDistance = 1500;
        double cutoffTime = 2 * 365 * SECONDS_IN_DAY;

        double startTime = 173 * SECONDS_IN_DAY + 339 * SECONDS_IN_MINUTE;

//        systemEnergyVsDeltaT( 2 * 365 * SECONDS_IN_DAY, 1000000);

//        double bestDay = testStartingDays(deltaT, spaceshipOrbitSpeed, cutoffDistance, cutoffTime, 0, 365 * SECONDS_IN_DAY, SECONDS_IN_DAY);
//        System.out.println(bestDay);
//
//        double[] optimalStartTime = findOptimalMissionStartTime(173 * SECONDS_IN_DAY, deltaT, spaceshipOrbitSpeed, 0, cutoffTime);
//        System.out.println("Best starting time: " + optimalStartTime[0] + " with a min distance of: " + optimalStartTime[1]);

//        simulateAndSave(deltaT, startTime, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
        testStartingSpeed(173 * SECONDS_IN_DAY, deltaT, cutoffTime);

    }


    private static void testStartingSpeed(double startTime, double deltaT, double cutoffTime){
        double speedIncrement = 0.1;

        List<Double> speeds = new ArrayList<>();
        for(double speed = 7; speed < 9 ; speed += speedIncrement){
            speeds.add(SPACE_STATION_SPEED + speed);
        }

        ConcurrentHashMap<Double, Double> distanceMap = new ConcurrentHashMap<>();

        speeds.parallelStream().forEach(speed -> {
            double[] res = simulateMinDistance(startTime, 0, cutoffTime, deltaT, speed);
            distanceMap.put(speed - SPACE_STATION_SPEED, res[0]);
        });


        ConcurrentHashMap<Double, Double> timeMap = new ConcurrentHashMap<>();
        distanceMap.entrySet().parallelStream().forEach(e -> {
            double[] res = simulateMinDistance(startTime, e.getValue() + e.getValue() * 0.1, cutoffTime, deltaT, e.getKey() + SPACE_STATION_SPEED);
            timeMap.put(e.getKey(), res[1]);
        });

        dumpMap(distanceMap, "./python/ej2/output-files/speed-comparison-distance.csv");
        dumpMap(timeMap, "./python/ej2/output-files/speed-comparison-time.csv");
    }

    private static void systemEnergyVsDeltaT(double cutoffTime, int dumpToFileLimit){

        List<Double> deltaTs = List.of(1.0, 10.0, 100.0, 500.0, 1000.0, 10000.0);

        for(Double deltaT : deltaTs){
            try(FileWriter writer = new FileWriter("./python/ej2/output-files/energy-deltat-" + deltaT + ".csv")) {
                Simulation simulation = new Simulation(Util.generateSun(), getBodies(), deltaT);

                double initialEnergy = simulation.calculateTotalEnergy();

                double accumulatedTime = 0;
                int timesDumped = 1;
                while (accumulatedTime < cutoffTime) {
                    simulation.simulate();

                    accumulatedTime += deltaT;

                    if(accumulatedTime >= dumpToFileLimit * timesDumped) {
                        writer.write(accumulatedTime + ", " + Math.abs((simulation.calculateTotalEnergy() - initialEnergy) / initialEnergy) * 100 + "\n");
                        timesDumped++;
                    }
                }
            } catch (IOException e){
                System.out.println(e);
            }
        }

    }

    private static double testStartingDays(double deltaT, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime, double rangeStart, double rangeEnd, double step) {

        List<Double> startingTimes = new ArrayList<>();
        for (double i = rangeStart; i < rangeEnd; i += step) {
            startingTimes.add(i);
        }
        ConcurrentHashMap<Double, Double> map = new ConcurrentHashMap<>();

        startingTimes.parallelStream().forEach(startingTime -> {
            double currentMinDistance = simulateMinDistance(startingTime, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed)[0];
            map.put((startingTime - rangeStart) / step, currentMinDistance);
        });

        dumpMap(map, "./python/ej2/output-files/starting-day-comparison.csv");

        return getBestFromMap(map);
    }
    private static void dumpMap(ConcurrentHashMap<Double, Double> map, String filename){
        try(FileWriter writer = new FileWriter(filename)){
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                    {
                        try {
                            writer.write(e.getKey() + "," + e.getValue() + "\n" );
                        } catch (IOException ex) {
                            System.out.println(ex);;
                        }
                    }
            );
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    private static double getBestFromMap(ConcurrentHashMap<Double, Double> map){
        double min = Double.POSITIVE_INFINITY;
        double minKey = -1;
        for(Map.Entry<Double, Double> entry : map.entrySet()){
            if(min > entry.getValue()){
                min = entry.getValue();
                minKey = entry.getKey();
            }
        }
        return minKey;
    }

    private static double[] simulateMinDistance(double startingTime, double cutoffDistance, double cutoffTime, double deltaT, double spaceshipOrbitSpeed){
        double currentMinDistance = Double.POSITIVE_INFINITY;

        Simulation simulation = new Simulation(
                Util.generateSun(),
                getBodies(),
                deltaT
        );

        // Avanzamos hasta el comienzo de la mision
        simulation.simulateUntil(startingTime);

        // Generamos el spaceship
        Body sun = simulation.getBody(Body.BodyType.SUN).get();
        Body earth = simulation.getBody(Body.BodyType.EARTH).get();
        simulation.addBody(Util.generateSpaceship(sun,earth, SPACE_STATION_ORBIT_ELEVATION, spaceshipOrbitSpeed));

        // Iteramos por el resto de la mision y vamos guardando la distancia minima
        double timeTake = 0;
        while (!simulation.spaceShipCloseToObjective(cutoffDistance, objective) && timeTake < cutoffTime) {
            simulation.simulate();
            timeTake += deltaT;
            double distance = simulation.getBody(objective).get().distanceFrom(simulation.getBody(Body.BodyType.SPACESHIP).get());

            if (distance < currentMinDistance) {
                currentMinDistance = distance;
            }
        }
        return new double[]{currentMinDistance, timeTake};
    }


    private static double[] findOptimalMissionStartTime(double candidateStartTime, double deltaT,  double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){
        double bestStartTime = candidateStartTime;
        double minDistance = Double.POSITIVE_INFINITY;

        // Primero probamos +- 1 dia, en incrementos de una hora
        double currentCandidate = candidateStartTime - SECONDS_IN_DAY;
        while(currentCandidate < candidateStartTime + SECONDS_IN_DAY){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed)[0];

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += SECONDS_IN_HOUR;
        }

        // Para el mejor candidate que tenemos probamos +- 1 hora, en incrementos de 1 minuto
        currentCandidate = bestStartTime - SECONDS_IN_HOUR;
        while(currentCandidate < candidateStartTime + SECONDS_IN_HOUR){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed)[0];

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += SECONDS_IN_MINUTE;
        }

        // Hacemos lo mismo pero +- 1 minuto, en incrementos de 1 segundo
//        currentCandidate = bestStartTime - SECONDS_IN_MINUTE;
//        while(currentCandidate < candidateStartTime + SECONDS_IN_MINUTE){
//
//            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed);
//
//            if(currentMinDistance < minDistance) {
//                minDistance = currentMinDistance;
//                bestStartTime = currentCandidate;
//            }
//
//            currentCandidate += 1;
//        }

        return new double[]{bestStartTime, minDistance};
    }


    private static void simulateAndSave(double deltaT, double startTime, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){

        int dumpAfterSteps = 80;

        long timestamp = System.currentTimeMillis();

        Simulation simulation = new Simulation(Util.generateSun(), getBodies(), deltaT);

        // Avanzamos hasta el comienzo de la mision
        simulation.simulateUntil(startTime);

        // Agregamos la nave
        simulation.addBody(Util.generateSpaceship(simulation.getBody(Body.BodyType.SUN).get(), simulation.getBody(Body.BodyType.EARTH).get(), SPACE_STATION_ORBIT_ELEVATION, spaceshipOrbitSpeed));

        // Hago una simulacion normal y guardo los resultados en un archivo
        try(FileWriter writer = new FileWriter("./python/ej2/output-files/bodies-" + timestamp + ".csv")){
            double cumulativeTime = 0;
            int cumulativeSteps = 0;

            // Posiciones iniciales
            dumpPositions(cumulativeTime, simulation.getBodies(), writer);

            while(!simulation.spaceShipCloseToObjective(cutoffDistance, objective) && cumulativeTime < cutoffTime) {
                simulation.simulate();

                cumulativeTime += deltaT;

                cumulativeSteps++;

                if(cumulativeSteps > dumpAfterSteps) {
                    dumpPositions(cumulativeTime, simulation.getBodies(), writer);
                    cumulativeSteps = 0;
                }
            }

            // Se llego a marte, hay que imprimir la posicion actual
            dumpPositions(cumulativeTime, simulation.getBodies(), writer);

        } catch (IOException e) {
            System.out.println(e);;
        }

        writeStaticData(simulation.getBodies(), deltaT, spaceshipOrbitSpeed, timestamp);
    }

    public static void dumpPositions(double time, Body[] bodies, FileWriter fileWriter) throws IOException {
        fileWriter.write(time + "\n");
        for(int i=0; i<bodies.length; i++){
            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol

            Body body = bodies[i];

            if(body.getType() == Body.BodyType.SUN){ continue; }

            fileWriter.write(body.toString() + "\n");
        }
    }

    public static void writeStaticData(Body[] bodies, double deltaT, double spaceshipOrbitSpeed, long timestamp) {
        try(FileWriter writer = new FileWriter("./python/ej2/output-files/properties-" + timestamp + ".csv")){
            writer.write("deltaT," + deltaT + "\n");
            writer.write("spaceshipOrbitDistance," + SPACE_STATION_ORBIT_ELEVATION + "\n");
            writer.write("spaceshipOrbitSpeed," + spaceshipOrbitSpeed + "\n");
            for(int i=0; i<bodies.length; i++){
                Body body = bodies[i];
                writer.write(body.getType().name() + "," + i + "," + body.getM() + "," + body.getR() + "\n");
            }
        } catch (IOException e) {
            System.out.println(e);;
        }
    }

    private static Body[] getBodies() {
        if (objective  == Body.BodyType.MARS){
            return Util.generateCelestialBodies();
        } else if (objective == Body.BodyType.JUPITER) {
            return Util.generateCelestialBodies();
        }
        return Util.generateCelestialBodies();
    }
}
