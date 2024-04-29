package ej2;

public class Simulation {

    // Constantes
    private static final int X = 0;
    private static final int Y = 1;
    private static final int BODY_AMOUNT = 4;

    // Variables
    private final Body[] bodies = new Body[BODY_AMOUNT];
    private final double deltaT;

    public Simulation(Body sun, Body mars, Body earth, Body ship, double deltaT) {
        if(sun.getType() != Body.BodyType.SUN || mars.getType() != Body.BodyType.MARS || earth.getType() != Body.BodyType.EARTH || ship.getType() != Body.BodyType.SPACESHIP){
            throw new RuntimeException("Mandaste algo mal!");
        }
        this.deltaT = deltaT;

        bodies[sun.getType().ordinal()] = sun;
        bodies[mars.getType().ordinal()] = mars;
        bodies[earth.getType().ordinal()] = earth;
        bodies[ship.getType().ordinal()] = ship;
    }

    public void simulate() {
        // No se modifica a la posicion y velocidad del sol
        double[][] forces = new double[2][BODY_AMOUNT];

        for(int i=0; i<BODY_AMOUNT; i++) {
            if(i == Body.BodyType.SUN.ordinal()){
                continue;
            }
            forces[X][i] = calcTotalForce(i)[X];
            forces[Y][i] = calcTotalForce(i)[Y];
        }



    }

    public double[] calcTotalForce(int position){
        double forceX = 0;
        double forceY = 0;

        Body body = bodies[position];
        for(int i=0; i<BODY_AMOUNT; i++){
            if(i == position){
                continue;
            }
            double force = body.calcForce(bodies[i]);
            forceX += force * body.normalX(bodies[i]);
            forceY += force * body.normalY(bodies[i]);
        }
        return new double[]{forceX, forceY};
    }

}
