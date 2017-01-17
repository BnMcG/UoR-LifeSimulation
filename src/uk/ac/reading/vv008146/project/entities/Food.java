package uk.ac.reading.vv008146.project.entities;

import javafx.scene.image.Image;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

/**
 * The food entity represents plants/vegetation that living beings can eat to sustain
 * themselves.
 *
 */
public class Food extends Entity {

    // Poisonous food will reduce an entity's energy, rather than increasing it.
    // Trophic levels don't apply to poisonous foods.
    protected boolean poisonous;

    /**
     * Food constructor that will create a food entity at the given position, in the given world,
     * with the given amount of energy.
     *
     * @param position Vector2 position in world to spawn food
     * @param energy int energy to give to food
     * @param world World world to spawn food in
     */

    public Food(Vector2 position, int energy, World world) {

        this.setUuid();

        this.setSpriteName("food/candy");

        this.setPosition(position);
        this.energy = energy;
        this.world = world;

        this.setVelocity(new Vector2(0,0));
    }

    /**
     * Spawn food in the given world, at the given position, with the given entity,
     * with the given sprite.
     *
     * @param spriteName String Name of sprite to use (without .png)
     * @param position Vector2 position in world to spawn sprite
     * @param energy int Amount of energy to give food
     * @param world World world to spawn food into
     */

    public Food(String spriteName, Vector2 position, int energy, World world) {

        this.setUuid();

        this.setSpriteName(spriteName);
        this.setPosition(position);
        this.energy = energy;
        this.world = world;
    }

    /**
     * Returns whether or not this food is poisonous. Poisonous food will reduce an entity's energy
     * instead of increasing it. Trophic levels don't apply to poisonous food.
     *
     * @return Boolean poisonous
     */

    public boolean isPoisonous() {
        return poisonous;
    }

    /**
     * Sets whether or not this food is poisonous. Poisonous food will reduce an entity's energy
     * instead of increasing it. Trophic levels don't apply to poisonous food.
     *
     * @param poisonous Boolean, posionous or not
     */

    public void setPoisonous(boolean poisonous) {
        this.poisonous = poisonous;
    }

    /**
     * Load an item of food from the drive and then set its world to the given world.
     * Requires that the food has been saved first.
     *
     * @param path String path where food has been saved
     * @param world World world which will contain food
     * @return Food loaded food item.
     */

    public static Food load(String path, World world) {

        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object loadedObject = ois.readObject();

            if(loadedObject instanceof Food) {
                Food food = (Food) loadedObject;
                food.setUuid();
                food.world = world;
                return food;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // something went wrong
        return null;
    }
}
