package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;

public class Main {
    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();

        double deltaT = 100;                    // En segundos
        double spaceshipOrbitDistance = 1500;
        double spaceshipOrbitSpeed = 7.12;

        double cutoffDistance = 4000;

        int dumpAfterSteps = 800;

        Body[] bodies = Util.generateBodies(spaceshipOrbitDistance, spaceshipOrbitSpeed);
        Simulation simulation = new Simulation(
                bodies[Body.BodyType.SUN.ordinal()],
                bodies[Body.BodyType.MARS.ordinal()],
                bodies[Body.BodyType.EARTH.ordinal()],
                bodies[Body.BodyType.SPACESHIP.ordinal()],
                deltaT
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
