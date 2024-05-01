package ej2;

import javax.security.auth.Subject;

public class Util {

    public static Body[] generateBodies(double spaceshipOrbitDistance, double spaceshipOrbitalSpeed) {
        Body[] resp = new Body[4];

        // TODO: no tener todo hardcodeado.

        Body sun = new Body(
                0,
                0,
                0,
                0,
                695700,
                1988500 * Math.pow(10, 24),
                Body.BodyType.SUN
        );
        Body earth = new Body(
                -1.230026285921387E+08,
                -8.885940118038680E+07,
                1.699147638530967E+01,
                -2.424060139046478E+01,
                6371.01,
                5.97219 * Math.pow(10, 24),
                Body.BodyType.EARTH
        );
        Body mars = new Body(
                1.747499342937683E+08,
                -1.092462413483886E+08,
                1.366936519025863E+01,
                2.266986633954994E+01,
                3389.92,
                6.4171 * Math.pow(10, 23),
                Body.BodyType.MARS
        );
        Body spaceship = generateSpaceship(sun, earth, spaceshipOrbitDistance, spaceshipOrbitalSpeed);

        resp[Body.BodyType.SUN.ordinal()] = sun;
        resp[Body.BodyType.EARTH.ordinal()] = earth;
        resp[Body.BodyType.MARS.ordinal()] = mars;
        resp[Body.BodyType.SPACESHIP.ordinal()] = spaceship;

        return resp;
    }

    public static Body generateSpaceship(Body sun, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitalSpeed){
        // TODO: check todo

        double xNormalVersor = sun.normalX(earth);
        double yNormalVersor = sun.normalY(earth);
        double xTangVersor = sun.tangX(earth);
        double yTangVersor = sun.tangY(earth);

        double x = earth.getX() + xNormalVersor * spaceshipOrbitDistance;
        double y = earth.getY() + yNormalVersor * spaceshipOrbitDistance;

        double cvx = xTangVersor * spaceshipOrbitalSpeed;
        double vx = earth.getVx() + cvx * earth.getVx() > 0 ? cvx  : - cvx;
        double cvy = yTangVersor * spaceshipOrbitalSpeed;
        double vy = earth.getVy() + cvy * earth.getVy() > 0 ? cvy  : - cvy;

        return new Body(
                x,
                y,
                vx,
                vy,
                0,
                2 * Math.pow(10, 5),
                Body.BodyType.SPACESHIP
        );
    }
}
