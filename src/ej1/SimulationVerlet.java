package ej1;

public class SimulationVerlet extends Simulation{

    // Particle state
    private double position;
    private double prevPosition;
    private double velocity;

    public SimulationVerlet(double mass, double springConstant, double gamma, double deltaT) {
        super(mass, springConstant, gamma, deltaT);

        position = 1;
        velocity = (- position * gamma) / (2 * mass);
        prevPosition = calculateInitialPrevPosition(position, velocity);
    }

    @Override
    public double simulate(){
        double newPosition = calcPosition(position, prevPosition, velocity);

        velocity = calcVelocity(newPosition, prevPosition);

        prevPosition = position;
        position = newPosition;

        return position;
    }

    private double calcPosition(double position, double prevPosition, double velocity){
        return 2 * position - prevPosition + (deltaT * deltaT * calcForce(position, velocity)) / mass;
    }

    private double calcVelocity(double newPosition, double prevPosition){
        return (newPosition - prevPosition) / (2 * deltaT);
    }
}
