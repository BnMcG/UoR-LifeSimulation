package uk.ac.reading.vv008146.project;

import java.util.Random;

/**
 * Created by Ben Magee on 22/10/2016.
 * Contact me: ben@bmagee.com
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
