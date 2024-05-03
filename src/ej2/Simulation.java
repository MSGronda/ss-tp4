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

    private double[][] prevForces;
    private double[][] forces;

    public Simulation(Body sun, Body mars, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitSpeed, double deltaT, double startingFrom) {
        if(sun.getType() != Body.BodyType.SUN || mars.getType() != Body.BodyType.MARS || earth.getType() != Body.BodyType.EARTH){
            throw new RuntimeException("Mandaste algo mal!");
        }
        this.deltaT = deltaT;

        // Primero vamos a inicializar el sistema sin la nave y simular hasta cierto dia especifico
        bodies = new Body[BODY_AMOUNT-1];
        forces = new double[BODY_AMOUNT-2][];
        prevForces = new double[BODY_AMOUNT-2][];
        bodies[0] = mars;
        bodies[1] = earth;
        bodies[2] = sun;

        // Si quiero empezar la mision en un dia posterior al cargado inicialmente

        advanceSystemUntil(startingFrom);

        // Resetemos el posicionamiento en los arreglos
        bodies = new Body[BODY_AMOUNT];
        forces = new double[BODY_AMOUNT-1][];
        prevForces = new double[BODY_AMOUNT-1][];

        bodies[sun.getType().ordinal()] = sun;
        bodies[mars.getType().ordinal()] = mars;
        bodies[earth.getType().ordinal()] = earth;

        // Ahora si generamos la nave
        bodies[Body.BodyType.SPACESHIP.ordinal()] = Util.generateSpaceship(sun, earth, spaceshipOrbitDistance, spaceshipOrbitSpeed);

        // Calculo inicial de fuerzas actuales
        setupForces();
    }


    private void advanceSystemUntil(double startingFrom){
        double cumulativeTime = 0;

        setupForces();

        while(cumulativeTime < startingFrom) {
            simulate();
            cumulativeTime += deltaT;
        }
    }
    private void setupForces(){
        setupCurrentForces();
        setupPrevForces();
    }

    private void setupCurrentForces(){
        for(int i=0; i<bodies.length - 1; i++) {
            forces[i] = calcTotalForce(i);
        }
    }
    private void setupPrevForces(){
        double[][] positions = new double[bodies.length-1][];
        for(int i=0; i<bodies.length -1 ; i++){
            positions[i] = new double[]{bodies[i].getX(), bodies[i].getY()};
        }

        for(int i=0; i<bodies.length -1 ; i++){
            bodies[i].setX(bodies[i].getX() - deltaT * bodies[i].getVx() + (deltaT * deltaT * forces[i][X]) / (2 * bodies[i].getM()));
            bodies[i].setY(bodies[i].getY() - deltaT * bodies[i].getVy() + (deltaT * deltaT * forces[i][Y]) / (2 * bodies[i].getM()));

        }
        for(int i=0; i<bodies.length - 1; i++) {
            prevForces[i] = calcTotalForce(i);
        }
        for(int i=0; i<bodies.length -1; i++){
            bodies[i].setX(positions[i][X]);
            bodies[i].setY(positions[i][Y]);
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

            // Con la posicion actualizada, calculamos la fuerzas que va a recibir en el futuro.
            double[] futureForces = calcTotalForce(i);

            // Actualizamos la velocidad
            double m = bodies[i].getM();
            bodies[i].nextVelocity(
                    deltaT,
                    prevForces[i][X] / m ,
                    prevForces[i][Y] / m ,
                    forces[i][X] / m,
                    forces[i][Y] / m,
                    futureForces[X] / m,
                    futureForces[Y] / m
            );

            // Cacheamos las fuerzas futuras para usar en el paso uno
            prevForces[i] = forces[i];
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
