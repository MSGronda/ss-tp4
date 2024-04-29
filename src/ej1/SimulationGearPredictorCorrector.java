package ej1;

public class SimulationGearPredictorCorrector extends Simulation {

    // Particle state
    private double p;
    private double p1;  // Velocidad
    private double p2;  // Aceleracion
    private double p3;
    private double p4;
    private double p5;

    private final double[] alpha = {(double) 3 / 16, (double) 251 / 360, 1, (double) 11 / 18, (double) 1 / 6,  (double) 1 / 60};

    public SimulationGearPredictorCorrector(double mass, double springConstant, double gamma, double deltaT) {
        super(mass, springConstant, gamma, deltaT);

        double m2 = mass * mass;
        double g2 = gamma * gamma;
        double m3 = m2 * mass;
        double g3 = g2 * gamma;
        double m4 = m3 * mass;
        double g4 = g3 * gamma;

        double k2 = springConstant * springConstant;

        p = 1;
        p1 = (- p * gamma) / (2 * mass);
        p2 = calcForce(p, p1) / mass;
        p3 = (springConstant * gamma * p) / (m2) + p1 * (- springConstant / mass + (g2) / (m2));
        p4 = ((k2) / (m2) - (springConstant * g2) / (m3)) * p
                + ((2 * springConstant * gamma) / (m2) - (g3) / (m2)) * p1;
        p5 = ((-2 * k2 * gamma) / (m3) + (springConstant * g3) / (m4)) * p
                + ((k2) / (m2) - (3 * springConstant * g2) / (m3) + (g4) / (m4)) * p1;
    }

    @Override
    public double simulate(){
        double t2 = time(deltaT, 2);
        double t3 = time(deltaT, 3);
        double t4 = time(deltaT, 4);
        double t5 = time(deltaT, 5);

        double predP =  p  + p1 * deltaT + p2 * t2 + p3 * t3 + p4 * t4 + p5 * t5;
        double predP1 = p1 + p2 * deltaT + p3 * t2 + p4 * t3 + p5 * t4;
        double predP2 = p2 + p3 * deltaT + p4 * t2 + p5 * t3;
        double predP3 = p3 + p4 * deltaT + p5 * t2;
        double predP4 = p4 + p5 * deltaT;
        double predP5 = p5;

        double futureA = calcForce(predP, predP1) / mass;
        double deltaR2 = (futureA - predP2) *  t2;

        double newP = predP + alpha[0] * deltaR2;
        double newP1 = predP1 + alpha[1] * deltaR2 / deltaT;
        double newP2 = predP2 + alpha[2] * deltaR2 / t2;
        double newP3 = predP3 + alpha[3] * deltaR2 / t3;
        double newP4 = predP4 + alpha[4] * deltaR2 / t4;
        double newP5 = predP5 + alpha[5] * deltaR2 / t5;

        p = newP;
        p1 = newP1;
        p2 = newP2;
        p3 = newP3;
        p4 = newP4;
        p5 = newP5;

        return p;
    }

    private static double time(double deltaT, int n) {
        double resp = deltaT;
        for(int i=1; i<n; i++){
            resp = (resp * deltaT) / (i+1);
        }
        return resp;
    }

}
