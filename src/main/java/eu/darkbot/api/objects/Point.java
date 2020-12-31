package eu.darkbot.api.objects;

/**
 * Represents point on-screen.
 */
public interface Point {
    /**
     * Creates new instance of {@link Point} with given parameters.
     * @param x coordinate
     * @param y coordinate
     * @return {@link Point} with given coordinates
     */
    static Point of(double x, double y) {
        return new Point() {
            public double getX() { return x; }
            public double getY() { return y; }
        };
    }

    /**
     * @return x coordinate of the {@link Point}
     */
    double getX();

    /**
     * @return y coordinate of the {@link Point}
     */
    double getY();
}
