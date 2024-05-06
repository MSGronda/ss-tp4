package ej2;

import java.util.Arrays;

public class Util {
    public static Body generateSun(){
        return new Body(
                0,
                0,
                0,
                0,
                695700,
                1988500E24,
                Body.BodyType.SUN
        );
    }

    public static Body[] generateCelestialBodies() {
        Body[] resp = new Body[2];

        resp[0] = new Body(
                -1.219024854566760E+08,
                -8.830999621339682E+07,
                1.698154915953803E+01,
                -2.422995800936565E+01,
                6371.01,
                5.97219E24,
                Body.BodyType.EARTH
        );
        resp[1]  = new Body(
                1.758500774292310E+08,
                -1.086968363813986E+08,
                1.365943796448699E+01,
                2.268050972064907E+01,
                3389.92,
                6.4171E23,
                Body.BodyType.MARS
        );

        return resp;
    }

    public static Body generateSpaceship(Body sun, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitalSpeed){

        double xNormalVersor = sun.normalX(earth);
        double yNormalVersor = sun.normalY(earth);
        double xTangVersor = sun.tangX(earth);
        double yTangVersor = sun.tangY(earth);

        double x = earth.getX() + xNormalVersor * (spaceshipOrbitDistance + earth.getR());
        double y = earth.getY() + yNormalVersor * (spaceshipOrbitDistance + earth.getR());

        double cvx = xTangVersor * spaceshipOrbitalSpeed;
        double vx = earth.getVx() + (cvx * earth.getVx() > 0 ? cvx  : - cvx);

        double cvy = yTangVersor * spaceshipOrbitalSpeed;
        double vy = earth.getVy() + (cvy * earth.getVy() > 0 ? cvy  : - cvy);

        return new Body(
                x,
                y,
                vx,
                vy,
                0,
                2.0E5,
                Body.BodyType.SPACESHIP
        );
    }

    public static Body[] generateAllBodies(double spaceshipOrbitDistance, double spaceshipOrbitalSpeed){
        Body[] bodies = Util.generateCelestialBodies();

        Body[] resp = new Body[bodies.length + 1];
        System.arraycopy(bodies, 0, resp, 0, bodies.length);

        Body sun = generateSun();
        Body earth = Arrays.stream(bodies).filter(b -> b.getType() == Body.BodyType.EARTH).toList().stream().findFirst().get(); // Medio un crimen esto

        resp[resp.length - 1] = generateSpaceship(sun, earth, spaceshipOrbitDistance, spaceshipOrbitalSpeed);

        return resp;
    }
}
