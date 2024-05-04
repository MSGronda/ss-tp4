package ej2;

import static ej2.Body.UGC;

public class Simulation {

    // Constantes
    private static final int X = 0;
    private static final int Y = 1;

    // Variables
    private Body[] bodies;
    private final double deltaT;
    private double[][] prevForces;
    private double[][] forces;


    public Simulation(Body sun, Body[] bodies, double deltaT) {
        this.deltaT = deltaT;

        // Incializamos los cuerpos
        setupBodies(sun, bodies);

        // Calculo inicial de fuerzas actuales
        setupForces();
    }

    // = = = = = = =  Public methods  = = = = = = =

    public void simulate() {
        // Paso 0: las fuerzas las tenemos del futureForces del paso anterior

        // Paso 1: avanzamos la simulacion 1 paso
        for (int i = 0; i < bodies.length - 1; i++) {            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol
            double m = bodies[i].getM();

            // Actualizamos la posicion
            bodies[i].nextPosition(deltaT, prevForces[i][X] / m, prevForces[i][Y] / m, forces[i][X] / m, forces[i][Y] / m);
        }

        // Paso 2: calculamos las nuevas velocidades
        for (int i = 0; i < bodies.length - 1; i++) {            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol

            // Con la posicion actualizada, calculamos la fuerzas que va a recibir en el futuro.
            double[] futureForces = calcTotalForce(i);

            // Actualizamos la velocidad
            double m = bodies[i].getM();
            bodies[i].nextVelocity(
                    deltaT,
                    prevForces[i][X] / m,
                    prevForces[i][Y] / m,
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

    public void simulateUntil(double totalTime) {
        double accumulatedTime = 0;
        while (accumulatedTime < totalTime) {
            simulate();
            accumulatedTime += deltaT;
        }
    }

    public void addBody(Body body) {
        Body[] newBodies = new Body[bodies.length + 1];
        System.arraycopy(bodies, 0, newBodies, 0, bodies.length - 1);
        newBodies[newBodies.length - 2] = body;

        // El sol lo ponemos siempre ultimo (asi lo podemos ignorar en ciertas operaciones)
        newBodies[newBodies.length - 1] = this.bodies[bodies.length - 1];

        this.bodies = newBodies;

        // Resetamos las fuerzas dado el nuevo cuerpo
        setupForces();
    }

    public double calculateTotalEnergy() {
        double totalEnergy = 0;

        // Energia cinetica
        for (Body body : bodies) {
            totalEnergy += 0.5 * body.getM() * (body.getVx() * body.getVx() + body.getVy() * body.getVy());
        }

        // Energia potencial gravitatoria
        for (int i = 0; i < bodies.length; i++) {
            Body body1 = bodies[i];
            for (int j = 0; j < i; j++) {
                Body body2 = bodies[j];
                totalEnergy += -(UGC * body1.getM() * body2.getM()) / body1.distanceFrom(body2);
            }
        }

        return totalEnergy;
    }

    public boolean spaceShipCloseToMars(double distanceFromMars) {
        Body mars = getBody(Body.BodyType.MARS);
        Body spaceship = getBody(Body.BodyType.SPACESHIP);
        return mars.distanceFrom(spaceship) <= mars.getR() + distanceFromMars;
    }


    // = = = = = = =  Private methods  = = = = = = =

    private void setupBodies(Body sun, Body[] bodies) {
        // Copiamos los cuerpos que nos pasaron
        this.bodies = new Body[bodies.length + 1];
        System.arraycopy(bodies, 0, this.bodies, 0, bodies.length);

        // El sol lo ponemos siempre ultimo (asi lo podemos ignorar en ciertas operaciones)
        this.bodies[this.bodies.length - 1] = sun;
    }

    private void setupForces() {

        // Inicializamos los vectores de fuerzas
        this.forces = new double[bodies.length][];
        this.prevForces = new double[bodies.length][];

        // Calculamos fuerzas actuales
        for (int i = 0; i < bodies.length - 1; i++) {
            forces[i] = calcTotalForce(i);
        }

        // Calculamos fuerzas anteriores

        // Guardamos las posciones actuales (para despues resetearlas)
        double[][] positions = new double[bodies.length - 1][];
        for (int i = 0; i < bodies.length - 1; i++) {
            positions[i] = new double[]{bodies[i].getX(), bodies[i].getY()};
        }

        // Ponemos la posicion anterior (momentaneamente asi podemos usar el calcTotalForce)
        for (int i = 0; i < bodies.length - 1; i++) {
            bodies[i].setX(bodies[i].getX() - deltaT * bodies[i].getVx() + (deltaT * deltaT * forces[i][X]) / (2 * bodies[i].getM()));
            bodies[i].setY(bodies[i].getY() - deltaT * bodies[i].getVy() + (deltaT * deltaT * forces[i][Y]) / (2 * bodies[i].getM()));
        }
        for (int i = 0; i < bodies.length - 1; i++) {
            prevForces[i] = calcTotalForce(i);
        }

        // Reseteamos las posiciones a como estaban antes
        for (int i = 0; i < bodies.length - 1; i++) {
            bodies[i].setX(positions[i][X]);
            bodies[i].setY(positions[i][Y]);
        }
    }

    private double[] calcTotalForce(int position) {
        double forceX = 0;
        double forceY = 0;

        Body body = bodies[position];
        for (int i = 0; i < bodies.length; i++) {
            if (i == position) {
                continue;
            }
            double force = body.calcForce(bodies[i]);
            forceX += force * body.normalX(bodies[i]);
            forceY += force * body.normalY(bodies[i]);
        }
        return new double[]{forceX, forceY};
    }


    // = = = = = = =  Getters  = = = = = = =

    public Body[] getBodies() {
        return bodies.clone();
    }

    public Body getBody(Body.BodyType type) {
        for (Body body : bodies) {
            if (body.getType() == type) {
                return body;
            }
        }
        throw new RuntimeException("No lo encontramos");
    }

}
