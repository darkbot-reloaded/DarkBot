package com.github.manolo8.darkbot.core.utils;

import com.github.manolo8.darkbot.core.entities.Entity;
import com.github.manolo8.darkbot.core.objects.LocationInfo;

import java.util.Objects;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.atan2;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

public class Location implements eu.darkbot.api.objects.Location {

    public double x;
    public double y;

    public Location() {
    }

    public Location(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Location of(Location loc, double angle, double distance) {
        return new Location(loc.x - cos(angle) * distance, loc.y - sin(angle) * distance);
    }

    public double distance(double ox, double oy) {
        return sqrt(pow(x - ox, 2) + pow(y - oy, 2));
    }

    public double distance(Location o) {
        return distance(o.x, o.y);
    }

    public double distance(LocationInfo o) {
        return distance(o.now);
    }

    public double distance(Entity e) {
        return distance(e.locationInfo);
    }

    public double angle(Location o) {
        return atan2(y - o.y, x - o.x);
    }

    public Location toAngle(Location center, double angle, double distance) {
        this.x = center.x - cos(angle) * distance;
        this.y = center.y - sin(angle) * distance;
        return this;
    }

    public Location set(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Location set(Location o) {
        return set(o.x, o.y);
    }

    public Location copy() {
        return new Location(x, y);
    }

    @Override
    public eu.darkbot.api.objects.Location setTo(double x, double y) {
        return set(x, y);
    }

    @Override
    public String toString() {
        return (int) x + "," + (int) y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(location.x, x) == 0 &&
                Double.compare(location.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }
}
