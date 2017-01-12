package uk.ac.reading.vv008146.project.entities;

import javafx.scene.image.Image;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

/**
 * Created by Ben Magee on 08/11/2016.
 * Contact me: ben@bmagee.com
 */
public class Obstacle extends Entity {

    public Obstacle(Vector2 position, World world) {

        this.setUuid();

        this.setSpriteName("objects/tree-christmas");
        this.setPosition(position);
        this.energy = 5;
        this.world = world;

        this.setVelocity(new Vector2(0,0));
    }
}
