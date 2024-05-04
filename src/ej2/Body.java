package ej2;

public class Body {
    public static final double UGC = 6.693E-20;  // -11 + -9 para conversion a km (en vez de metros)
    private double x;
    private double y;
    private double vx;
    private double vy;
    private final double r;
    private final double m;
    private final BodyType type;

    public Body(double x, double y, double vx, double vy, double r, double m, BodyType type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.r = r;
        this.m = m;
        this.type = type;
    }

    public void nextPosition(double deltaT, double prevAx, double prevAy, double ax, double ay) {
        double t2 = deltaT * deltaT;
        double newX = x + vx * deltaT + (2 * ax * t2) / 3 - (prevAx * t2) / 6;
        double newY = y + vy * deltaT + (2 * ay * t2) / 3 - (prevAy * t2) / 6;

        // Podemos hacer esto, el nextVelocity no necesariamente requiere
        x = newX;
        y = newY;
    }


    public void nextVelocity(double deltaT, double prevAx, double prevAy, double ax, double ay, double futureAx, double futureAy) {
        double newVx = vx + (futureAx * deltaT) / 3 + (5 * ax * deltaT) / 6 - (prevAx * deltaT) / 6;
        double newVy = vy + (futureAy * deltaT) / 3 + (5 * ay * deltaT) / 6 - (prevAy * deltaT) / 6;

        vx = newVx;
        vy = newVy;
    }

    public double calcForce(Body b2) {
        return (UGC * m * b2.getM()) / ((x - b2.x) * (x - b2.x) + (y - b2.y) * (y - b2.y));
    }

    public double distanceFrom(Body b2) {
        return Math.sqrt((x - b2.x) * (x - b2.x) + (y - b2.y) * (y - b2.y));
    }

    public double normalX(Body b2) {
        return (b2.x - x) / distanceFrom(b2);
    }

    public double normalY(Body b2) {
        return (b2.y - y) / distanceFrom(b2);
    }

    public double tangX(Body b2) {
        return -normalY(b2);
    }

    public double tangY(Body b2) {
        return normalX(b2);
    }


    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getR() {
        return r;
    }

    public double getM() {
        return m;
    }

    public BodyType getType() {
        return type;
    }

    // Setters
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + vx + "," + vy;
    }

    public enum BodyType {
        EARTH,
        MARS,
        SPACESHIP,
        SUN
    }
}
