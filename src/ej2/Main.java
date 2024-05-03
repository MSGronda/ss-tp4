package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static final int SECONDS_IN_DAY = 60 * 60 * 24;

    public static void main(String[] args) {

        double deltaT = 500.0;                    // En segundos
        double spaceshipOrbitDistance = 1500;
        double spaceshipOrbitSpeed = 7.12 + 8;

        double cutoffDistance = 14000;
        double cutoffTime = 60 * SECONDS_IN_DAY;

//        testDelta();
        simulateAndSave(deltaT,68304001, spaceshipOrbitDistance, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
    }
    private static void testDelta(){
        double cutoffTime = 31579200;

        List<Double> deltaTs = List.of(1.0, 10.0, 100.0, 1000.0, 10000.0, 100000.0);

        for(Double deltaT : deltaTs){
            Body[] bodies = Util.generateCelestialBodies();
            Simulation simulation = new Simulation(
                    bodies[Body.BodyType.SUN.ordinal()],
                    bodies[Body.BodyType.MARS.ordinal()],
                    bodies[Body.BodyType.EARTH.ordinal()],
                    0,
                    0,
                    deltaT,
                    cutoffTime
            );
        }

    }

    private static void testStartingDays(double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime) {
        int totalStartingMoments = 60 * 24 * 60;

        List<Integer> startingTimes = new ArrayList<>();
        for (int i = 0; i < totalStartingMoments; i++) {
            startingTimes.add(i + 790  * SECONDS_IN_DAY);
        }

        Object minDistanceLock  = new Object();

        AtomicReference<Double> minDistance = new AtomicReference<>(Double.POSITIVE_INFINITY);
        AtomicInteger minDistanceDay = new AtomicInteger(-1);


        startingTimes.parallelStream().forEach(startingTime -> {
            double currentMinDistance = Double.POSITIVE_INFINITY;

            Body[] bodies = Util.generateCelestialBodies();
            Simulation simulation = new Simulation(
                    bodies[Body.BodyType.SUN.ordinal()],
                    bodies[Body.BodyType.MARS.ordinal()],
                    bodies[Body.BodyType.EARTH.ordinal()],
                    spaceshipOrbitDistance,
                    spaceshipOrbitSpeed,
                    deltaT,
                    startingTime
            );
            double timeTake = 0;
            while (!simulation.cutoffCondition(cutoffDistance) && timeTake < cutoffTime) {
                simulation.simulate();
                timeTake += deltaT;
                Body[] simBodies = simulation.getBodies();

                double distanceFromMars = simBodies[Body.BodyType.MARS.ordinal()].distanceFrom(simBodies[Body.BodyType.SPACESHIP.ordinal()]);
                if (distanceFromMars < currentMinDistance) {
                    currentMinDistance = distanceFromMars;
                }
            }
            synchronized (minDistanceLock){
                if(minDistance.get() > currentMinDistance){
                    minDistance.set(currentMinDistance);
                    minDistanceDay.set(startingTime);
                }
            }
        });

        System.out.println("Best moment: " + minDistanceDay.get() + " with a min distance of : " + minDistance.get() + "\n");

    }


    private static void simulateAndSave(double deltaT, double startingFrom, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){
        // Hago una simulacion normal y guardo los resultados en un archivo

        int dumpAfterSteps = 40;
        long timestamp = System.currentTimeMillis();

        Body[] bodies = Util.generateCelestialBodies();
        Simulation simulation = new Simulation(
                bodies[Body.BodyType.SUN.ordinal()],
                bodies[Body.BodyType.MARS.ordinal()],
                bodies[Body.BodyType.EARTH.ordinal()],
                spaceshipOrbitDistance,
                spaceshipOrbitSpeed,
                deltaT,
                startingFrom
        );

        writeStaticData(simulation.getBodies(), deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed, timestamp);

        try(FileWriter writer = new FileWriter("./python/ej2/output-files/bodies-" + timestamp + ".csv")){
            double cumulativeTime = 0;
            int cumulativeSteps = 0;

            // Posiciones iniciales
            dumpPositions(cumulativeTime, simulation.getBodies(), writer);

            while(!simulation.cutoffCondition(cutoffDistance) && cumulativeTime < cutoffTime) {
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
