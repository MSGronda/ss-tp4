package ej2;

import javax.security.auth.Subject;

public class Util {

    public static Body[] generateCelestialBodies() {
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
                -1.219024854566760E+08,
                -8.830999621339682E+07,
                1.698154915953803E+01,
                -2.422995800936565E+01,
                6371.01,
                5.97219 * Math.pow(10, 24),
                Body.BodyType.EARTH
        );
        Body mars = new Body(
                1.758500774292310E+08,
                -1.086968363813986E+08,
                1.365943796448699E+01,
                2.268050972064907E+01,
                3389.92,
                6.4171 * Math.pow(10, 23),
                Body.BodyType.MARS
        );

        resp[Body.BodyType.SUN.ordinal()] = sun;
        resp[Body.BodyType.EARTH.ordinal()] = earth;
        resp[Body.BodyType.MARS.ordinal()] = mars;

        return resp;
    }

    public static Body generateSpaceship(Body sun, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitalSpeed){

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
