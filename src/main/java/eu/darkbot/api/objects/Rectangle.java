package eu.darkbot.api.objects;

public interface Rectangle extends Locatable {

    double getWidth();
    double getHeight();

    default double getX2() {
        return getX() + getWidth();
    }

    default double getY2() {
        return getY() + getHeight();
    }
}
