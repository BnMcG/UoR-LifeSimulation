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
 * Created by Ben Magee on 08/11/2016.
 * Contact me: ben@bmagee.com
 */
public class LivingBeing extends Entity {

    private long lastRandomMovement;
    private Direction lastRandomDirection;

    protected String species;
    private boolean dead;
    private boolean flock; // Will be used to determine if we should apply Boids algorithm

    private Vector2 goal;
    private long goalLastSetMillis;

    private double attackValue;

    public Vector2 getGoal() {
        return goal;
    }

    public void setGoal(Vector2 goal) {
        this.goal = goal;
    }

    public long getGoalLastSetMillis() {
        return goalLastSetMillis;
    }

    public void setGoalLastSetMillis(long goalLastSetMillis) {
        this.goalLastSetMillis = goalLastSetMillis;
    }

    public LivingBeing(String spriteName, Vector2 position, World world) {

        this.setUuid();

        this.spriteName = spriteName;
        this.setPosition(position);
        this.energy = 10;
        this.world = world;
        this.dead = false;

        this.lastRandomMovement = System.currentTimeMillis();
    }

    public LivingBeing(Vector2 position, World world) {

        this.setUuid();

        this.setSpriteName("animals/tiger.png");
        this.setPosition(position);
        this.energy = 10;
        this.world = world;
        this.dead = false;
        this.flock = true;

        this.setVelocity(new Vector2(0,0));
        this.maxSpeed = 5;
        this.attackValue = 1;

        this.lastRandomMovement = System.currentTimeMillis();
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public double getAttackValue() {
        return attackValue;
    }

    public void setAttackValue(double attackValue) {
        this.attackValue = attackValue;
    }

    public boolean isFlock() {
        return flock;
    }

    public void setFlock(boolean flock) {
        this.flock = flock;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public long getLastRandomMovement() {
        return lastRandomMovement;
    }

    public void setLastRandomMovement(long lastRandomMovement) {
        this.lastRandomMovement = lastRandomMovement;
    }

    /**
     * Check for food in any direction within the given range
     * Returns the closest item of food, if one exists within the given range
     *
     * @param range Distance to search
     * @return Food or null if nothing in range
     */

    public Food smellFood(int range) {

        Food closestFood = null;
        double shortestDistance = Double.POSITIVE_INFINITY;

        for(Entity e : world.getEntities().values()) {
            if(e instanceof Food) {

                Food food = (Food) e;

                if(food.getEnergy() <= 0) {
                    continue;
                }

                // Get being's x/y coordinates:
                double beingX = this.getPosition().getX();
                double beingY = this.getPosition().getY();

                // Get food's x/y coordinates
                double foodX = food.getPosition().getX();
                double foodY = food.getPosition().getY();

                // a^2 + b^2 = c^2

                // Get length of horizontal side
                double horizLength = Math.abs(beingX - foodX);
                // Get length of vertical side
                double vertLength = Math.abs(beingY - foodY);

                // Work out the length of the hypotenuse, this is also the
                // distance between the being and the food, as the crow flies

                // sqrt(a^2 + b^2) = c
                double distance = Math.sqrt(Math.pow(horizLength, 2) + Math.pow(vertLength, 2));

                if (distance < shortestDistance) {
                    closestFood = food;
                    shortestDistance = distance;
                }
            }
        }


        if(shortestDistance <= range) {
            return closestFood;
        } else {
            return null;
        }
    }

    public Vector2 calculateVectorToFood(Food food) {
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
     * @param path
     * @return
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
}
