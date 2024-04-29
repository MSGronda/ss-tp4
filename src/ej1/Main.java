package ej1;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        double mass = 70;
        double springConstant = 10000;
        double gamma = 100;
        double deltaT = 0.000001;
        double totalTime = 5;
        SimulationType type = SimulationType.BEEMAN;

        double[] deltaTs = {0.000001, 0.00001, 0.0001, 0.001, 0.01};


//        for (double i = 0.000001 ; i <= 0.00101 ; i += 0.0001665 ) {
        for (double i : deltaTs ) {
            deltaT = i;
            long timestamp = System.currentTimeMillis();

            Simulation simulation = instantiateClass(mass, springConstant, gamma, deltaT, type);
            writeStaticFile(mass, springConstant, gamma, deltaT, type, timestamp);

            double cumulativeTime = 0;
            try (FileWriter writer = new FileWriter("./python/ej1/output-files/particle-movement-" + timestamp + ".csv")) {
                writer.write(cumulativeTime + "," + 1 + "\n");
                while (cumulativeTime < totalTime) {

                    cumulativeTime += deltaT;
                    double pos = simulation.simulate();
                    writer.write(cumulativeTime + "," + pos + "\n");


                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    private static void writeStaticFile(double mass, double springConstant, double gamma, double deltaT, SimulationType type, long timestamp){
        try(FileWriter writer = new FileWriter("./python/ej1/output-files/static-data-" + timestamp + ".csv")) {
            writer.write("mass," + mass + "\n");
            writer.write("springConstant," + springConstant + "\n");
            writer.write("gamma," + gamma + "\n");
            writer.write("deltaT," + deltaT + "\n");
            writer.write("type," + type.name() + "\n");
        }
        catch (IOException e){
            System.out.println(e);
        }
    }

    private static Simulation instantiateClass(double mass, double springConstant, double gamma, double deltaT, SimulationType type) {
        return switch (type) {
            case VERLET -> new SimulationVerlet(mass, springConstant, gamma, deltaT);
            case BEEMAN -> new SimulationBeeman(mass, springConstant, gamma, deltaT);
            case GEAR_PREDICTOR_CORRECTOR -> new SimulationGearPredictorCorrector(mass, springConstant, gamma, deltaT);
        };
    }

    enum SimulationType {
        VERLET,
        BEEMAN,
        GEAR_PREDICTOR_CORRECTOR
    }
}
