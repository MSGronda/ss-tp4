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

    public static void main(String[] args) {

        double deltaT = 100.0;                    // En segundos
        double spaceshipOrbitDistance = 1500;
        double spaceshipOrbitSpeed = 7.12 + 8;

        double cutoffDistance = 14000;
        double cutoffTime = 2 * 365 * SECONDS_IN_DAY;

//        systemEnergyVsDeltaT( 2 * 365 * SECONDS_IN_DAY, 1000000);
//        simulateAndSave(deltaT, 0, spaceshipOrbitDistance, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
//        testStartingDays(deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);

        double[] optimalStartTime = findOptimalMissionStartTime(68342400, deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
        System.out.println("Best starting time: " + optimalStartTime[0] + " with a min distance of: " + optimalStartTime[1]);
    }
    private static void systemEnergyVsDeltaT(double cutoffTime, int dumpToFileLimit){

        List<Double> deltaTs = List.of(1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0);

        for(Double deltaT : deltaTs){
            try(FileWriter writer = new FileWriter("./python/ej2/output-files/energy-deltat-" + deltaT + ".csv")) {
                Simulation simulation = new Simulation(Util.generateSun(), Util.generateCelestialBodies(), deltaT);

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

    private static void testStartingDays(double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime) {
        int totalStartingMoments = 3 * 365 * 2;

        List<Integer> startingTimes = new ArrayList<>();
        for (int i = 0; i < totalStartingMoments; i++) {
            startingTimes.add(i * SECONDS_IN_DAY / 2);
        }
        ConcurrentHashMap<Integer, Double> map = new ConcurrentHashMap<>();

        startingTimes.parallelStream().forEach(startingTime -> {
            double currentMinDistance = simulateMinDistance(startingTime, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed);
            map.put(startingTime/(SECONDS_IN_DAY / 2), currentMinDistance);
        });

        try(FileWriter writer = new FileWriter("./python/ej2/output-files/min-distances.csv")){
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

    private static double simulateMinDistance(double startingTime, double cutoffDistance, double cutoffTime, double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed){
        double currentMinDistance = Double.POSITIVE_INFINITY;

        Simulation simulation = new Simulation(
                Util.generateSun(),
                Util.generateCelestialBodies(),
                deltaT
        );

        // Avanzamos hasta el comienzo de la mision
        simulation.simulateUntil(startingTime);

        // Generamos el spaceship
        Body sun = simulation.getBody(Body.BodyType.SUN);
        Body earth = simulation.getBody(Body.BodyType.EARTH);
        simulation.addBody(Util.generateSpaceship(sun,earth, spaceshipOrbitDistance, spaceshipOrbitSpeed));

        // Iteramos por el resto de la mision y vamos guardando la distancia minima
        double timeTake = 0;
        while (!simulation.spaceShipCloseToMars(cutoffDistance) && timeTake < cutoffTime) {
            simulation.simulate();
            timeTake += deltaT;

            double distanceFromMars = simulation.getBody(Body.BodyType.MARS).distanceFrom(simulation.getBody(Body.BodyType.SPACESHIP));
            if (distanceFromMars < currentMinDistance) {
                currentMinDistance = distanceFromMars;
            }
        }
        return currentMinDistance;
    }

    private static double[] findOptimalMissionStartTime(double candidateStartTime, double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){
        double bestStartTime = candidateStartTime;
        double minDistance = Double.POSITIVE_INFINITY;

        // Primero probamos +- 1 dia, en incrementos de una hora
        double currentCandidate = candidateStartTime - SECONDS_IN_DAY;
        while(currentCandidate < candidateStartTime + SECONDS_IN_DAY){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed);

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += SECONDS_IN_HOUR;
        }

        // Para el mejor candidate que tenemos probamos +- 1 hora, en incrementos de 1 minuto
        currentCandidate = bestStartTime - SECONDS_IN_HOUR;
        while(currentCandidate < candidateStartTime + SECONDS_IN_HOUR){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed);

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += SECONDS_IN_MINUTE;
        }

        // Hacemos lo mismo pero +- 1 minuto, en incrementos de 1 segundo
        currentCandidate = bestStartTime - SECONDS_IN_MINUTE;
        while(currentCandidate < candidateStartTime + SECONDS_IN_MINUTE){

            double currentMinDistance = simulateMinDistance(currentCandidate, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed);

            if(currentMinDistance < minDistance) {
                minDistance = currentMinDistance;
                bestStartTime = currentCandidate;
            }

            currentCandidate += 1;
        }

        return new double[]{bestStartTime, minDistance};
    }


    private static void simulateAndSave(double deltaT, double startTime, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){

        int dumpAfterSteps = 80;

        long timestamp = System.currentTimeMillis();

        Simulation simulation = new Simulation(Util.generateSun(), Util.generateAllBodies(spaceshipOrbitDistance, spaceshipOrbitSpeed), deltaT);

        // Avanzamos hasta el comienzo de la simulacion
        simulation.simulateUntil(startTime);

        // Hago una simulacion normal y guardo los resultados en un archivo
        try(FileWriter writer = new FileWriter("./python/ej2/output-files/bodies-" + timestamp + ".csv")){
            double cumulativeTime = 0;
            int cumulativeSteps = 0;

            // Posiciones iniciales
            dumpPositions(cumulativeTime, simulation.getBodies(), writer);

            while(!simulation.spaceShipCloseToMars(cutoffDistance) && cumulativeTime < cutoffTime) {
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

        writeStaticData(simulation.getBodies(), deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed, timestamp);
    }

    public static void dumpPositions(double time, Body[] bodies, FileWriter fileWriter) throws IOException {
        fileWriter.write(time + "\n");
        for(int i=0; i<bodies.length; i++){
            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol
            if(i == Body.BodyType.SUN.ordinal()){ continue; }

            Body body = bodies[i];
            fileWriter.write(body.getX() + "," + body.getY() + "\n");
        }
    }

    public static void writeStaticData(Body[] bodies, double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, long timestamp) {
        try(FileWriter writer = new FileWriter("./python/ej2/output-files/properties-" + timestamp + ".csv")){
            writer.write("deltaT," + deltaT + "\n");
            writer.write("spaceshipOrbitDistance," + spaceshipOrbitDistance + "\n");
            writer.write("spaceshipOrbitSpeed," + spaceshipOrbitSpeed + "\n");
            for(Body body : bodies) {
                writer.write(body.getType().name() + "," + body.getType().ordinal() + "," + body.getM() + "," + body.getR() + "\n");
            }
        } catch (IOException e) {
            System.out.println(e);;
        }
    }
}
