package uk.ac.reading.vv008146.project.entities;

import javafx.scene.image.Image;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

/**
 * Obstacle class represents an obstacle that cannot be passed in the world
 */
public class Obstacle extends Entity {

    /**
     * Create an obstacle that living beings can't pass through
     *
     * @param position Vector2 Position in world to spawn
     * @param world World Which world to spawn in
     */

    public Obstacle(Vector2 position, World world) {

        this.setUuid();

        this.setSpriteName("objects/tree-christmas");
        this.setPosition(position);
        this.energy = 5;
        this.world = world;

        this.setVelocity(new Vector2(0,0));
    }
}
