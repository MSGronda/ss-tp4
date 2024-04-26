package ej1;

import java.io.FileWriter;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        double mass = 70;
        double springConstant = 10000;
        double gamma = 100;
        double deltaT = 0.01;
        double totalTime = 5;
        SimulationType type = SimulationType.GEAR_PREDICTOR_CORRECTOR;

        Simulation simulation = instantiateClass(mass, springConstant, gamma, deltaT, type);
        double cumulativeTime = 0;
        try (FileWriter writer = new FileWriter("./python/ej1/output-files/particle-movement-" + type.name() + ".csv")) {
            while (cumulativeTime < totalTime) {

                double pos = simulation.simulate();
                writer.write(cumulativeTime + "," + pos + "\n");

                cumulativeTime += deltaT;
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private static void writeStaticFile(double mass, double springConstant, double gamma, double deltaT, SimulationType type){
        try(FileWriter writer = new FileWriter("./python/ej1/output-files/static-data-" + type.name() + ".csv")) {
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
