package ej2;

public class Main {
    public static void main(String[] args) {
        Body[] bodies = Util.generateBodies(1500, 7.12);

        double deltaT = 1;

        Simulation simulation = new Simulation(
                bodies[Body.BodyType.SUN.ordinal()],
                bodies[Body.BodyType.MARS.ordinal()],
                bodies[Body.BodyType.EARTH.ordinal()],
                bodies[Body.BodyType.SPACESHIP.ordinal()],
                deltaT
        );
    }
}
