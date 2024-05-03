package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final int SECONDS_IN_DAY = 60 * 60 * 24;

    public static void main(String[] args) {

        double deltaT = 745000.0;                    // En segundos
        double spaceshipOrbitDistance = 1500;
        double spaceshipOrbitSpeed = 7.12 + 8;  // TODO: check si es sumar

        double cutoffDistance = 6000;
        double cutoffTime = 20000 * SECONDS_IN_DAY;

        testStartingDays(deltaT, spaceshipOrbitDistance, spaceshipOrbitSpeed, cutoffDistance, cutoffTime);
    }
    private static void testDelta(){
        double cutoffTime = 31579200;

        List<Double> deltaTs = List.of(7.40e5, 7.425e5, 7.45e5, 7.47e5, 7.5e5);

        for(Double deltaT : deltaTs){
            Body[] bodies = Util.generateCelestialBodies();
            Body[] onlyCelestiaBodies = new Body[]{bodies[Body.BodyType.SUN.ordinal()],bodies[Body.BodyType.MARS.ordinal()], bodies[Body.BodyType.EARTH.ordinal()]};

            double energyBefore = calculateTotalEnergy(onlyCelestiaBodies);

            Simulation simulation = new Simulation(
                    bodies[Body.BodyType.SUN.ordinal()],
                    bodies[Body.BodyType.MARS.ordinal()],
                    bodies[Body.BodyType.EARTH.ordinal()],
                    0,
                    0,
                    deltaT,
                    cutoffTime
            );
            double energyAfter = calculateTotalEnergy(onlyCelestiaBodies);

//            Body earth = simulation.getBodies()[Body.BodyType.EARTH.ordinal()];
//            Body realEarth = new Body(-1.222909445704976E+08, -8.776988375652307E+07, 1.688906686002370E+01, -2.432486393096968E+01, 0, 0, Body.BodyType.EARTH);
//
//            Body mars = simulation.getBodies()[Body.BodyType.MARS.ordinal()];
//            Body realMars = new Body(-2.342323002211556E+08, 8.466649149852000E+07, -7.329716775397554E+00, -2.071609768210218E+01, 0, 0, Body.BodyType.MARS);

            System.out.println("For deltaT: " + deltaT + " error was: " + (Math.abs(energyAfter - energyBefore)));
        }

    }
    private static double calculateTotalEnergy(Body[] bodies){
        double UGC = 6.693 * Math.pow(10, -20);

        double totalEnergy = 0;
        // Energia cinetica
        for(Body body : bodies){
            totalEnergy += 0.5 * body.getM() * (body.getVx()* body.getVx() + body.getVx() + body.getVy());
        }
        // Energia potencial gravitatoria
        for(int i=0; i<bodies.length; i++) {
            for(int j=0; j< bodies.length; j++){
                if(i == j){
                    continue;
                }
                // TODO: check el -
                totalEnergy += - (UGC * bodies[i].getM() + bodies[j].getM()) / bodies[i].distanceFrom(bodies[j]);
            }
        }
        return totalEnergy;
    }

    private static void testStartingDays(double deltaT, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance, double cutoffTime){
        int totalStartingDays = 5000;

        List<Integer> days = new ArrayList<>();
        for(int i=0; i<totalStartingDays; i++){
            days.add(i);
        }

        days.parallelStream().forEach(day  -> {
            Body[] bodies = Util.generateCelestialBodies();
            Simulation simulation = new Simulation(
                    bodies[Body.BodyType.SUN.ordinal()],
                    bodies[Body.BodyType.MARS.ordinal()],
                    bodies[Body.BodyType.EARTH.ordinal()],
                    spaceshipOrbitDistance,
                    spaceshipOrbitSpeed,
                    deltaT,
                    day * SECONDS_IN_DAY
            );
            double timeTake = 0;
            while(!simulation.cutoffCondition(cutoffDistance) && timeTake < cutoffTime){
                simulation.simulate();
                timeTake += deltaT;
            }
            if(simulation.cutoffCondition(cutoffDistance)){
                System.out.println("(!!!) Starting day: " + day + ", it took : " + timeTake / SECONDS_IN_DAY + " days to reach the objective. (!!!)");
            }
//            else {
//                System.out.println("Starting day: " + day + " failed to reach objective in an acceptable time (" + cutoffTime / SECONDS_IN_DAY +" days).");
//            }
        });
    }


    private static void simulateAndSave(double deltaT, double startingFrom, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double cutoffDistance){
        // Hago una simulacion normal y guardo los resultados en un archivo

        int dumpAfterSteps = 800;
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

            while(!simulation.cutoffCondition(cutoffDistance)) {
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
