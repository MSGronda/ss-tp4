package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

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

        systemEnergyVsDeltaT(2 * 365 * SECONDS_IN_DAY, 1000000);
        testStartingDays(deltaT, spaceshipOrbitSpeed, cutoffDistance, cutoffTime, 173 * SECONDS_IN_DAY + 5 * SECONDS_IN_HOUR, 173 * SECONDS_IN_DAY + 7 * SECONDS_IN_HOUR, deltaT);
        testStartingSpeed(173 * SECONDS_IN_DAY, deltaT, cutoffTime);
        simulateAndSave(deltaT, startTime, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
    }

    private static void systemEnergyVsDeltaT(double cutoffTime, int dumpToFileLimit) {

        List<Double> deltaTs = List.of(1.0, 10.0, 100.0, 500.0, 1000.0, 10000.0);

        for (Double deltaT : deltaTs) {
            try (FileWriter writer = new FileWriter("./python/ej2/output-files/energy-deltat-" + deltaT + ".csv")) {
                Simulation simulation = new Simulation(Util.generateSun(), getBodies(), deltaT);

                double initialEnergy = simulation.calculateTotalEnergy();

                double accumulatedTime = 0;
                int timesDumped = 1;
                while (accumulatedTime < cutoffTime) {
                    simulation.simulate();

                    accumulatedTime += deltaT;

                    if (accumulatedTime >= dumpToFileLimit * timesDumped) {
                        writer.write(accumulatedTime + ", " + Math.abs((simulation.calculateTotalEnergy() - initialEnergy) / initialEnergy) * 100 + "\n");
                        timesDumped++;
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }

    }

    private static void testStartingDays(double deltaT, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime, double rangeStart, double rangeEnd, double step) {

        List<Double> startingTimes = new ArrayList<>();
        for (double i = rangeStart; i < rangeEnd; i += step) {
            startingTimes.add(i);
        }
        ConcurrentHashMap<Double, Double> map = new ConcurrentHashMap<>();

        startingTimes.parallelStream().forEach(startingTime -> {
            double currentMinDistance = simulateMinDistance(startingTime, cutoffDistance, cutoffTime, deltaT, spaceshipOrbitSpeed)[0];
            map.put((startingTime - rangeStart) / step, currentMinDistance);
        });

        Util.dumpMap(map, "./python/ej2/output-files/starting-day-comparison.csv");
    }

    private static void testStartingSpeed(double startTime, double deltaT, double cutoffTime) {
        double speedIncrement = 0.1;

        List<Double> speeds = new ArrayList<>();
        for (double speed = 7; speed < 9; speed += speedIncrement) {
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

        Util.dumpMap(distanceMap, "./python/ej2/output-files/speed-comparison-distance.csv");
        Util.dumpMap(timeMap, "./python/ej2/output-files/speed-comparison-time.csv");
    }

    private static void simulateAndSave(double deltaT, double startTime, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime) {

        int dumpAfterSteps = 80;

        long timestamp = System.currentTimeMillis();

        Simulation simulation = new Simulation(Util.generateSun(), getBodies(), deltaT);

        // Avanzamos hasta el comienzo de la mision
        simulation.simulateUntil(startTime);

        // Agregamos la nave
        simulation.addBody(Util.generateSpaceship(simulation.getBody(Body.BodyType.SUN).get(), simulation.getBody(Body.BodyType.EARTH).get(), SPACE_STATION_ORBIT_ELEVATION, spaceshipOrbitSpeed));

        // Hago una simulacion normal y guardo los resultados en un archivo
        try (FileWriter writer = new FileWriter("./python/ej2/output-files/bodies-" + timestamp + ".csv")) {
            double cumulativeTime = 0;
            int cumulativeSteps = 0;

            // Posiciones iniciales
            Util.dumpPositions(cumulativeTime, simulation.getBodies(), writer);

            while (!simulation.spaceShipCloseToObjective(cutoffDistance, objective) && cumulativeTime < cutoffTime) {
                simulation.simulate();

                cumulativeTime += deltaT;

                cumulativeSteps++;

                if (cumulativeSteps > dumpAfterSteps) {
                    Util.dumpPositions(cumulativeTime, simulation.getBodies(), writer);
                    cumulativeSteps = 0;
                }
            }

            // Se llego a marte, hay que imprimir la posicion actual
            Util.dumpPositions(cumulativeTime, simulation.getBodies(), writer);

        } catch (IOException e) {
            System.out.println(e);
            ;
        }

        Util.writeStaticData(simulation.getBodies(), deltaT, spaceshipOrbitSpeed, SPACE_STATION_ORBIT_ELEVATION, timestamp);
    }

    // = = = = = = = = = = = Helpers = = = = = = = = = = = = = = = = = =

    private static double[] simulateMinDistance(double startingTime, double cutoffDistance, double cutoffTime, double deltaT, double spaceshipOrbitSpeed) {
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
        simulation.addBody(Util.generateSpaceship(sun, earth, SPACE_STATION_ORBIT_ELEVATION, spaceshipOrbitSpeed));

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

    private static Body[] getBodies() {
        if (objective == Body.BodyType.MARS) {
            return Util.generateCelestialBodies();
        } else if (objective == Body.BodyType.JUPITER) {
            return Util.generateCelestialBodiesWithJupiter();
        }
        return Util.generateCelestialBodies();
    }
}
