package ej1;

public class SimulationBeeman extends Simulation{

    // Particle state
    private double position;
    private double prevPosition;
    private double velocity;
    private double prevVelocity;

    public SimulationBeeman(double mass, double springConstant, double gamma, double deltaT) {
        super(mass, springConstant, gamma, deltaT);

        position = 1;
        velocity = (- position * gamma) / (2 * mass);
        prevPosition = calculateInitialPrevPosition(position, velocity);
        prevVelocity = calculateInitialPrevVelocity(position, velocity);
    }

    @Override
    public double simulate(){
        double newPosition = calcPosition(position, prevPosition, velocity, prevVelocity);

        double predictedVelocity = calcVelocityPrediction(position, prevPosition, velocity, prevVelocity);

        double correctedVelocity = calcVelocityCorrected(position, prevPosition, velocity, prevVelocity, newPosition, predictedVelocity);

        prevPosition = position;
        position = newPosition;

        prevVelocity = velocity;
        velocity = correctedVelocity;

        return position;
    }
    private double calcPosition(double position, double prevPosition, double velocity, double prevVelocity){
        double acceleration = calcForce(position, velocity) / mass;
        double prevAcceleration = calcForce(prevPosition, prevVelocity) / mass;

        return position + velocity * deltaT + (2 * acceleration * deltaT * deltaT) / 3 - (prevAcceleration * deltaT * deltaT) / 6;
    }
    private double calcVelocityPrediction(double position, double prevPosition, double velocity, double prevVelocity){
        double acceleration = calcForce(position, velocity) / mass;
        double prevAcceleration = calcForce(prevPosition, prevVelocity) / mass;

        return velocity + (3 * acceleration * deltaT) / 2 - (prevAcceleration * deltaT) / 2;
    }

    private double calcVelocityCorrected(double position, double prevPosition, double velocity, double prevVelocity, double newPosition, double predictedVelocity){
        double futureAcceleration = calcForce(newPosition, predictedVelocity) / mass;
        double acceleration = calcForce(position, velocity) / mass;
        double prevAcceleration = calcForce(prevPosition, predictedVelocity) / mass;

        return velocity + (futureAcceleration * deltaT) / 3 + (5 * acceleration * deltaT) / 6 - (prevAcceleration * deltaT) / 6;
    }
}
