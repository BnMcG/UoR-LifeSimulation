package uk.ac.reading.vv008146.project;

import java.util.Random;

/**
 * Simple compass enumeration
 */

public enum Direction {
    NORTH,EAST,SOUTH,WEST;

    /**
     * Return a random direction
     * @return Random Direction
     */

    public static Direction random() {
        Random r = new Random();
        return Direction.values()[r.nextInt(Direction.values().length)];
    }
}
