package ej1;

public abstract class Simulation {
    public final double mass;
    public final double springConstant;
    public final double gamma;
    public final double deltaT;
    public Simulation(double mass, double springConstant, double gamma, double deltaT) {
        this.mass = mass;
        this.springConstant = springConstant;
        this.gamma = gamma;
        this.deltaT = deltaT;
    }

    public double calcForce(double position, double velocity){
        // Notese que usamos la velocidad disponible (la actual)
        return - springConstant * position - gamma * velocity;
    }

    public double calculateInitialPrevPosition(double position, double velocity){
        // Usamos euler normal
        // El deltaT tiene que ser con menos
        return position - deltaT * velocity + (deltaT * deltaT * calcForce(position, velocity)) / (2 * mass);
    }

    public double calculateInitialPrevVelocity(double position, double velocity) {
        return velocity - (deltaT * calcForce(position, velocity)) / mass;
    }

    public double simulate(){
        throw new RuntimeException("Implement this method!");
    }
}
