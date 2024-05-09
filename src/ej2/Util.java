package ej2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Util {
    public static Body generateSun() {
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
        resp[1] = new Body(
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

    public static Body[] generateCelestialBodiesWithJupiter() {
        Body[] resp = new Body[3];

        resp[0] = new Body(
                -1.219024854566760E+08,
                -8.830999621339682E+07,
                1.698154915953803E+01,
                -2.422995800936565E+01,
                6371.01,
                5.97219E24,
                Body.BodyType.EARTH
        );
        resp[1] = new Body(
                1.758500774292310E+08,
                -1.086968363813986E+08,
                1.365943796448699E+01,
                2.268050972064907E+01,
                3389.92,
                6.4171E23,
                Body.BodyType.MARS
        );
        resp[2] = new Body(
                4.197239507543979E+08,
                6.208501496231644E+08,
                -1.098467423816427E+01,
                7.940485717710249E+00,
                69911,
                189818722,
                Body.BodyType.JUPITER
        );

        return resp;
    }

    public static Body generateSpaceship(Body sun, Body earth, double spaceshipOrbitDistance, double spaceshipOrbitalSpeed) {

        double xNormalVersor = sun.normalX(earth);
        double yNormalVersor = sun.normalY(earth);
        double xTangVersor = sun.tangX(earth);
        double yTangVersor = sun.tangY(earth);

        double x = earth.getX() + xNormalVersor * (spaceshipOrbitDistance + earth.getR());
        double y = earth.getY() + yNormalVersor * (spaceshipOrbitDistance + earth.getR());

        double cvx = xTangVersor * spaceshipOrbitalSpeed;
        double vx = earth.getVx() + (cvx * earth.getVx() > 0 ? cvx : -cvx);

        double cvy = yTangVersor * spaceshipOrbitalSpeed;
        double vy = earth.getVy() + (cvy * earth.getVy() > 0 ? cvy : -cvy);

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

    public static Body[] generateAllBodies(double spaceshipOrbitDistance, double spaceshipOrbitalSpeed) {
        Body[] bodies = Util.generateCelestialBodies();

        Body[] resp = new Body[bodies.length + 1];
        System.arraycopy(bodies, 0, resp, 0, bodies.length);

        Body sun = generateSun();
        Body earth = Arrays.stream(bodies).filter(b -> b.getType() == Body.BodyType.EARTH).toList().stream().findFirst().get(); // Medio un crimen esto

        resp[resp.length - 1] = generateSpaceship(sun, earth, spaceshipOrbitDistance, spaceshipOrbitalSpeed);

        return resp;
    }

    public static void dumpMap(ConcurrentHashMap<Double, Double> map, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e ->
                    {
                        try {
                            writer.write(e.getKey() + "," + e.getValue() + "\n");
                        } catch (IOException ex) {
                            System.out.println(ex);
                            ;
                        }
                    }
            );
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void dumpPositions(double time, Body[] bodies, FileWriter fileWriter) throws IOException {
        fileWriter.write(time + "\n");
        for (int i = 0; i < bodies.length; i++) {
            // No se modifica a la posicion y velocidad del sol => no nos importa la fuerza que se aplica sobre el sol

            Body body = bodies[i];

            if (body.getType() == Body.BodyType.SUN) {
                continue;
            }

            fileWriter.write(body.toString() + "\n");
        }
    }

    public static void writeStaticData(Body[] bodies, double deltaT, double spaceshipOrbitSpeed, double spaceshipOrbitElevation, long timestamp) {
        try (FileWriter writer = new FileWriter("./python/ej2/output-files/properties-" + timestamp + ".csv")) {
            writer.write("deltaT," + deltaT + "\n");
            writer.write("spaceshipOrbitDistance," + spaceshipOrbitElevation + "\n");
            writer.write("spaceshipOrbitSpeed," + spaceshipOrbitSpeed + "\n");
            for (int i = 0; i < bodies.length; i++) {
                Body body = bodies[i];
                writer.write(body.getType().name() + "," + i + "," + body.getM() + "," + body.getR() + "\n");
            }
        } catch (IOException e) {
            System.out.println(e);
            ;
        }
    }
}
