package uk.ac.reading.vv008146.project.entities;

import javafx.scene.image.Image;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

import java.util.Random;

/**
 * Created by Ben Magee on 08/11/2016.
 * Contact me: ben@bmagee.com
 */
public class Food extends Entity {

    public Food(Vector2 position, int energy, World world) {

        this.setUuid();

        this.setSpriteName("food/candy");

        this.setPosition(position);
        this.energy = energy;
        this.world = world;

        this.setVelocity(new Vector2(0,0));
    }

    public Food(String spriteName, Vector2 position, int energy, World world) {

        this.setUuid();

        this.setSpriteName(spriteName);
        this.setPosition(position);
        this.energy = energy;
        this.world = world;
    }
}
