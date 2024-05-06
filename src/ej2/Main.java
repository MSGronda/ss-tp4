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

    private static final Body[] bodies = Util.generateCelestialBodies();


    public static void main(String[] args) {

        double deltaT = 500.0;                    // En segundos
        double spaceshipOrbitSpeed = SPACE_STATION_SPEED + 8;

        double cutoffDistance = 1500;
        double cutoffTime = 2 * 365 * SECONDS_IN_DAY;

        double startTime = 8.068104E7;

//        systemEnergyVsDeltaT( 2 * 365 * SECONDS_IN_DAY, 1000000);

//        testStartingDays(deltaT, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
        double[] optimalStartTime = findOptimalMissionStartTime(173.0, deltaT, spaceshipOrbitSpeed, 0, cutoffTime);
        System.out.println("Best starting time: " + optimalStartTime[0] + " with a min distance of: " + optimalStartTime[1]);
//
//        simulateAndSave(deltaT, startTime, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
        testStartingSpeed(startTime, deltaT, cutoffDistance, cutoffTime);

    }


    private static void testStartingSpeed(double startTime, double deltaT, double cutoffDistance, double cutoffTime){
        double speedRangeLength = 20;
        double speedIncrement = 0.1;

        List<Double> speeds = new ArrayList<>();
        for(double speed = 0; speed < speedRangeLength ; speed += speedIncrement){
            speeds.add(SPACE_STATION_SPEED + speed);
        }

        ConcurrentHashMap<Double, Double> minDistances = new ConcurrentHashMap<>();

        speeds.parallelStream().forEach(speed -> {
            double minDistance = simulateMinDistance(startTime, cutoffDistance, cutoffTime, deltaT, speed);
            minDistances.put(speed, minDistance);
        });

        try(FileWriter writer = new FileWriter("./python/ej2/output-files/speed-comparison.csv")){
            minDistances.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                    {
                        try {
                            writer.write((e.getKey() - SPACE_STATION_SPEED)+ "," + e.getValue() + "\n" );
                        } catch (IOException ex) {
                            System.out.println(ex);;
                        }
                    }
            );
        } catch (IOException e){
            System.out.println(e);
        }

    }

    private static void systemEnergyVsDeltaT(double cutoffTime, int dumpToFileLimit){

        List<Double> deltaTs = List.of(1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0);

        for(Double deltaT : deltaTs){
            try(FileWriter writer = new FileWriter("./python/ej2/output-files/energy-deltat-" + deltaT + ".csv")) {
                Simulation simulation = new Simulation(Util.generateSun(), bodies.clone(), deltaT);

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

    private static void testStartingDays(double deltaT, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime) {
        int totalStartingMoments = 3 * 365 * 2;

        List<Integer> startingTimes = new ArrayList<>();
        for (int i = 0; i < totalStartingMoments; i++) {
            startingTimes.add(i * SECONDS_IN_DAY / 2);
        }
        ConcurrentHashMap<Integer, Double> map = new ConcurrentHashMap<>();

        startingTimes.parallelStream().forEach(startingTime -> {
            double currentMinDistance = simulateMinDistance(startingTime, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed);
            map.put(startingTime/(SECONDS_IN_DAY / 2), currentMinDistance);
        });

        try(FileWriter writer = new FileWriter("./python/ej2/output-files/starting-day-comparison.csv")){
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                    {
                        try {
                            double day = Math.floor((double) e.getKey() / 2) + (e.getKey() % 2) * 0.5;
                            writer.write(day + "," + e.getValue() + "\n" );
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

    private static double simulateMinDistance(double startingTime, double cutoffDistance, double cutoffTime, double deltaT, double spaceshipOrbitSpeed){
        double currentMinDistance = Double.POSITIVE_INFINITY;

        Simulation simulation = new Simulation(
                Util.generateSun(),
                bodies.clone(),
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
        while (!simulation.spaceShipCloseToObjective(cutoffDistance) && timeTake < cutoffTime) {
            simulation.simulate();
            timeTake += deltaT;

            double distanceFromMars = simulation.getBody(Body.BodyType.MARS).get().distanceFrom(simulation.getBody(Body.BodyType.SPACESHIP).get());
            if (distanceFromMars < currentMinDistance) {
                currentMinDistance = distanceFromMars;
            }
        }
        return currentMinDistance;
    }

    private static double[] findOptimalMissionStartTime(double candidateStartTime, double deltaT,  double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){
        double bestStartTime = candidateStartTime;
        double minDistance = Double.POSITIVE_INFINITY;

        // Primero probamos +- 1 dia, en incrementos de una hora
        double currentCandidate = candidateStartTime - SECONDS_IN_DAY;
        while(currentCandidate < candidateStartTime + SECONDS_IN_DAY){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed);

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += SECONDS_IN_HOUR;
        }

        // Para el mejor candidate que tenemos probamos +- 1 hora, en incrementos de 1 minuto
        currentCandidate = bestStartTime - SECONDS_IN_HOUR;
        while(currentCandidate < candidateStartTime + SECONDS_IN_HOUR){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed);

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

        Simulation simulation = new Simulation(Util.generateSun(), bodies.clone(), deltaT);

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

            while(!simulation.spaceShipCloseToObjective(cutoffDistance) && cumulativeTime < cutoffTime) {
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
            if(i == Body.BodyType.SUN.ordinal()){ continue; }

            Body body = bodies[i];
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
}
