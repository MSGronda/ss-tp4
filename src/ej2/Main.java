package ej2;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        long timestamp = System.currentTimeMillis();

        double deltaT = 100;                    // En segundos
        double spaceshipOrbitDistance = 1500;
        double spaceshipOrbitSpeed = 7.12;

        double cutoffTime = 100000;

        Body[] bodies = Util.generateBodies(spaceshipOrbitDistance, spaceshipOrbitSpeed);
        Simulation simulation = new Simulation(
                bodies[Body.BodyType.SUN.ordinal()],
                bodies[Body.BodyType.MARS.ordinal()],
                bodies[Body.BodyType.EARTH.ordinal()],
                bodies[Body.BodyType.SPACESHIP.ordinal()],
                deltaT
        );

        try(FileWriter writer = new FileWriter("./python/ej2/output-files/particle-movement-" + timestamp + ".csv")){
            double cumulativeTime = 0;

            // Posiciones iniciales
            dumpPositions(cumulativeTime, simulation.getBodies(), writer);

            while(cumulativeTime < cutoffTime) {

                simulation.simulate();

                dumpPositions(cumulativeTime, simulation.getBodies(), writer);

                cumulativeTime += deltaT;
            }
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
            fileWriter.write(body.getX() + "," + body.getY() + "," + body.getType().name() + "\n");
        }
    }
}
