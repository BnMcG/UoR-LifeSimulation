package uk.ac.reading.vv008146.project.entities;

import javafx.scene.image.Image;
import uk.ac.reading.vv008146.project.Direction;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Random;

/**
 * LivingBeing class represents an entity that moves about and eats within a world
 */

public class LivingBeing extends Entity {

    private long lastRandomMovement; // When was the last random movement made?

    protected String species; // What species is this being?

    private boolean carnivore; // Does this being eat other beings?
    private boolean dead; // Has this being died?
    private boolean flock; // Will be used to determine if we should apply Boids algorithm

    private Vector2 goal; // Where is this being moving towards?

    private double attackValue; // How strongly can this being attack?

    /**
     * Get the position that the being is aiming to reach
     * @return Vector2 goal position
     */

    public Vector2 getGoal() {
        return goal;
    }

    /**
     * Set the position that the being is aiming to reach
     * @param goal Vector2 position
     */

    public void setGoal(Vector2 goal) {
        this.goal = goal;
    }


    /**
     * Create a living being that is represented in the world using the given sprite.
     * It will be spawned at the given position, in the given world.
     *
     * @param spriteName Sprite's name
     * @param position Position of being in the world
     * @param world Which world to spawn being into
     */

    public LivingBeing(String spriteName, Vector2 position, World world) {

        this.setUuid();

        this.spriteName = spriteName;
        this.setPosition(position);
        this.energy = 10;
        this.world = world;
        this.dead = false;

        this.lastRandomMovement = System.currentTimeMillis();
    }

    /**
     * Get the species of this being
     * @return String species name
     */

    public String getSpecies() {
        return species;
    }

    /**
     * Set the species of this being
     * @param species String species name
     */

    public void setSpecies(String species) {
        this.species = species;
    }

    /**
     * Get how powerful this being's attacks are
     * @return double AP
     */

    public double getAttackValue() {
        return attackValue;
    }

    /**
     * Set how powerful this being's attacks are
     * @param attackValue double AP
     */

    public void setAttackValue(double attackValue) {
        this.attackValue = attackValue;
    }

    /**
     * Returns whether or not this being should be in a Boids flock or not
     * @return Boolean whether the being belongs in a flock
     */

    public boolean isFlock() {
        return flock;
    }

    /**
     * Set whether or not this being should be part of a flock.
     * @param flock Boolean whether or not being belongs in a flock
     */

    public void setFlock(boolean flock) {
        this.flock = flock;
    }

    /**
     * Returns whether or not the living being is dead or not. s
     * @return Boolean dead?
     */

    public boolean isDead() {
        return dead;
    }

    /**
     * Set whether or not this living being has died
     * @param dead Boolean dead?
     */

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    /**
     * Get when the last random movement was made
     * @return long Last random movenemt time in ms
     */

    public long getLastRandomMovement() {
        return lastRandomMovement;
    }

    /**
     * set when the last random movement was made
     * @param lastRandomMovement long Time in ms
     */

    public void setLastRandomMovement(long lastRandomMovement) {
        this.lastRandomMovement = lastRandomMovement;
    }

    /**
     * Check for food in any direction within the given range
     * Returns the closest item of food, if one exists within the given range. If the being is a carnivore,
     * returns the closest entity that it can eat.
     *
     * @param range Distance to search
     * @return Food or null if nothing in range
     */

    public Entity smellFood(int range) {

        Entity closestFood = null;
        double shortestDistance = Double.POSITIVE_INFINITY;

        for(Entity e : world.getEntities().values()) {

            if(!this.isCarnivore()) {
                if(e instanceof Food) {

                    Food food = (Food) e;

                    if(food.getEnergy() <= 0) {
                        continue;
                    }

                    double distance = this.calculateDistance(this, food);

                    if (distance < shortestDistance) {
                        closestFood = food;
                        shortestDistance = distance;
                    }
                }
            } else {
                if(e instanceof LivingBeing && !(e.equals(this)) ) {

                    LivingBeing meal = (LivingBeing) e;

                    if(meal.getEnergy() <= 0) {
                        continue;
                    }

                    double distance = this.calculateDistance(this, meal);

                    if (distance < shortestDistance) {
                        closestFood = meal;
                        shortestDistance = distance;
                    }
                }
            }
        }


        if(shortestDistance <= range) {
            return closestFood;
        } else {
            return null;
        }
    }

    /**
     * Calculate the distance between two entities, as the crow flies. This method is used to determine whether
     * food is close enough to aim towards it.
     *
     * @param e1 Entity first entity
     * @param e2 Entity second entity
     * @return Distance between e1 and e2, as the crow flies
     */

    private double calculateDistance(Entity e1, Entity e2) {
        // Get being's x/y coordinates:
        double beingX = e1.getPosition().getX();
        double beingY = e1.getPosition().getY();

        // Get food's x/y coordinates
        double foodX = e2.getPosition().getX();
        double foodY = e2.getPosition().getY();

        // a^2 + b^2 = c^2

        // Get length of horizontal side
        double horizLength = Math.abs(beingX - foodX);
        // Get length of vertical side
        double vertLength = Math.abs(beingY - foodY);

        // Work out the length of the hypotenuse, this is also the
        // distance between the being and the food, as the crow flies

        // sqrt(a^2 + b^2) = c
        return Math.sqrt(Math.pow(horizLength, 2) + Math.pow(vertLength, 2));

    }

    /**
     * Calculates a vector from this entity towards the given entity. This is used to aim the entity
     * towards food it wants to eat.
     *
     * @param food Entity which will be eaten
     * @return Vector2 offset of current vector to navigate towards food
     */

    public Vector2 calculateVectorToFood(Entity food) {
        Vector2 velocityVector = new Vector2(0,0);
        // Total length
        // a^2 + b^2 = c^2
        double a = Math.abs(food.getPosition().getX() - this.getPosition().getX());
        double b = Math.abs(food.getPosition().getY() - this.getPosition().getY());

        // Magnitude of the line between the food and the entity
        // magnitude = c (in the above equation)
        double magnitude = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));

        Vector2 directionVector = food.getPosition().subtract(this.getPosition());
        Vector2 directionUnitVector = directionVector.scalarDivide(magnitude);

        double proposedX = (this.getPosition().getX() + directionUnitVector.getX());
        double proposedY = (this.getPosition().getY() + directionUnitVector.getY());

        if (!(world.blocked(proposedX, proposedY))) {
            velocityVector = this.getVelocity().add(directionUnitVector);
            this.setEnergy(this.getEnergy() - this.getEnergyDepletionValue());
        } else {
            Random rng = new Random();
            velocityVector = this.getVelocity().add(new Vector2(rng.nextInt(5), rng.nextInt(5)));
        }

        return velocityVector;
    }

    /**
     * Load an entity from the disk
     *
     * @param path Path to load from
     * @return LivingBeing
     */

    public static LivingBeing load(String path, World world) {

        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object loadedObject = ois.readObject();

            if(loadedObject instanceof LivingBeing) {
                LivingBeing being = (LivingBeing) loadedObject;
                being.world = world;
                return being;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // something went wrong
        return null;
    }

    /**
     * Return whether or not this being is a carnivore
     * @return Boolean carnivore?
     */

    public boolean isCarnivore() {
        return carnivore;
    }

    /**
     * Set whether or not this being eats other living beings.
     * @param carnivore Boolean carnivore?
     */

    public void setCarnivore(boolean carnivore) {
        this.carnivore = carnivore;
    }
}
