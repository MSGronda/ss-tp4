package ej2;

import java.io.FileWriter;
import java.io.IOException;

public class Simulation {

    // Constantes
    private static final int X = 0;
    private static final int Y = 1;
    private static final int BODY_AMOUNT = 4;

    // Variables
    private Body[] bodies;
    private final double deltaT;

    private double[][] forces;

    public Simulation(Body sun, Body mars, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double deltaT, double startingFrom) {
        if(sun.getType() != Body.BodyType.SUN || mars.getType() != Body.BodyType.MARS || earth.getType() != Body.BodyType.EARTH){
            throw new RuntimeException("Mandaste algo mal!");
        }
        this.deltaT = deltaT;

        // Primero vamos a inicializar el sistema sin la nave y simular hasta cierto dia especifico
        bodies = new Body[BODY_AMOUNT-1];
        forces = new double[BODY_AMOUNT-2][];
        bodies[0] = mars;
        bodies[1] = earth;
        bodies[2] = sun;

        // Si quiero empezar la mision en un dia posterior al cargado inicialmente
        advanceSystemUntil(startingFrom);

        // Resetemos el posicionamiento en los arreglos
        bodies = new Body[BODY_AMOUNT];
        forces = new double[BODY_AMOUNT-1][];

        bodies[sun.getType().ordinal()] = sun;
        bodies[mars.getType().ordinal()] = mars;
        bodies[earth.getType().ordinal()] = earth;

        // Ahora si generamos la nave
        bodies[Body.BodyType.SPACESHIP.ordinal()] = Util.generateSpaceship(sun, earth, spaceshipOrbitDistance, spaceshipOrbitSpeed);

        // Calculo inicial de fuerzas actuales
        for(int i=0; i<BODY_AMOUNT - 1; i++) {            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol
            forces[i] = calcTotalForce(i);
        }
    }


    private void advanceSystemUntil(double startingFrom){
        double cumulativeTime = 0;

        for(int i=0; i<bodies.length - 1; i++) {
            forces[i] = calcTotalForce(i);
        }

        while(cumulativeTime < startingFrom) {
            simulate();
            cumulativeTime += deltaT;
        }
    }

    public void simulate() {
        // Paso 0: las fuerzas las tenemos del futureForces del paso anterior

        // Paso 1: avanzamos la simulacion 1 paso
        for(int i=0; i< bodies.length - 1; i++) {            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol
            double m = bodies[i].getM();

            // Actualizamos la posicion
            bodies[i].nextPosition(deltaT, forces[i][X] / m, forces[i][Y] / m);
        }
        // Paso 2: calculamos las nuevas velocidades
        for(int i=0; i<bodies.length - 1; i++) {            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol
            double m = bodies[i].getM();

            // Con la posicion actualizada, calculamos la fuerzas que va a recibir en el futuro.
            double[] futureForces = calcTotalForce(i);

            // Actualizamos la velocidad
            bodies[i].nextVelocity(deltaT, forces[i][X] / m, forces[i][Y] / m, futureForces[X] / m, futureForces[Y] / m);

            // Cacheamos las fuerzas futuras para usar en el paso uno
            forces[i] = futureForces;
        }
    }

    public double[] calcTotalForce(int position){
        double forceX = 0;
        double forceY = 0;

        Body body = bodies[position];
        for(int i=0; i<bodies.length; i++){
            if(i == position){
                continue;
            }
            double force = body.calcForce(bodies[i]);
            forceX += force * body.normalX(bodies[i]);
            forceY += force * body.normalY(bodies[i]);
        }
        return new double[]{forceX, forceY};
    }

    public Body[] getBodies() {
        return bodies;
    }

    public boolean cutoffCondition(double distanceFromMars){
        Body mars = bodies[Body.BodyType.MARS.ordinal()];
        Body spaceship = bodies[Body.BodyType.SPACESHIP.ordinal()];
        return mars.distanceFrom(spaceship) <= mars.getR() + distanceFromMars;
    }
}
