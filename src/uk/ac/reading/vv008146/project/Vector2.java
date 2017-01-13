package uk.ac.reading.vv008146.project;

import java.io.Serializable;

/**
 * Created by Ben Magee on 26/11/2016.
 * Contact me: ben@bmagee.com
 */
public class Vector2 implements Serializable {

    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2() {
        this.x = 0;
        this.y = 0;
    }

    /**
     * Subtract a vector FROM this vector
     *
     * @param vec Vector2 vector to subtract
     * @return Vector with the resultant components
     */

    public Vector2 subtract(Vector2 vec) {
        Vector2 result = new Vector2();

        result.setX(this.getX() - vec.getX());
        result.setY(this.getY() - vec.getY());

        return result;
    }

    /**
     * Add a vector to this vector
     *
     * @param vec Vector2 vector to add
     * @return Vector with the sum of the components
     */

    public Vector2 add(Vector2 vec) {
        Vector2 result = new Vector2();

        result.setX(this.getX() + vec.getX());
        result.setY(this.getY() + vec.getY());

        return result;
    }

    /**
     * Multiply this vector by a scalar value
     *
     * @param value Value to multiply by
     * @return New vector with the components multiplied by the scalar
     */

    public Vector2 scalarMultiply(double value) {
        Vector2 result = new Vector2();
        result.setX(this.getX() * value);
        result.setY(this.getY() * value);

        return result;
    }

    /**
     * Divide this vector by a scalar value
     *
     * @param value Value to divide by
     * @return New vector with components divided by the scalar
     */

    public Vector2 scalarDivide(double value) {
        return new Vector2(this.getX() / value, this.getY() / value);
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(this.getX(), 2) + Math.pow(this.getY(), 2));
    }

    @Override
    public String toString() {
        return "Vector2{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
