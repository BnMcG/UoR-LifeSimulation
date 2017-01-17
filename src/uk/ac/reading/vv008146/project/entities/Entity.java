package uk.ac.reading.vv008146.project.entities;

import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

import java.io.*;
import java.util.UUID;

/*
 * Created by Ben Magee on 11/10/2016.
 * Contact me: ben@bmagee.com
 */

/**
 * Entity class is abstract and must be inherited before it can be instantiated. This class holds common
 * properties and methods between all entities in the game, and is serializable so that entities can be
 * saved to a file.
 */

public abstract class Entity implements Serializable {

    protected World world;

    protected Vector2 position;

    protected double energy;
    protected int id;

    protected double energyDepletionValue;
    protected double consumptionEfficiencyPercentage; // How much energy is lost when eating food

    protected String spriteName;

    private Vector2 velocity;
    protected double maxSpeed;
    private double boundingConstant; // How quickly entities will return back inside the bounds if they travel outside

    private UUID uuid;

    /**
     * Set the name of the sprite that will be loaded to represent this entity in the world.
     * @param name Sprite file name, without .png extension
     */

    public void setSpriteName(String name) {
        this.spriteName = name;
    }

    /**
     * Get the current sprite name
     * @return String Current sprite name
     */

    public String getSpriteName() {
        return spriteName;
    }

    /**
     * Set a random unique ID to identify this entity. Used to lookup all of the entities in the world
     * using a HashMap
     */

    public void setUuid() {
        this.uuid = UUID.randomUUID();
    }

    /**
     * Default constructor for the Entity class. Sets a UUID, a default sprite, a default position in the
     * world (0, 0), a default energy value (100), adds the entity to a new world, sets a max speed of 1.5,
     * a consumption energy percentage of 0.1, an energy depletion value of 0.01 and a bounding constant
     * of 0.5
     */

    public Entity() {

        this.setUuid();

        this.setSpriteName("objects/pin.png");
        this.setPosition(new Vector2(0, 0));
        this.energy = 100;
        this.id = 1;
        this.world = new World();

        this.energyDepletionValue = 0.01d;
        this.boundingConstant = 0.5;

        this.maxSpeed = 1.5;

        // https://en.wikipedia.org/wiki/Ten_percent_law
        this.consumptionEfficiencyPercentage = 0.10;
    }

    /**
     * Get the entity's current velocity as a 2D vector.
     * @return Vector2
     */

    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * Set the entity's velocity. This method will check to make sure that the entity is
     * not about to go out of bounds. If it is, it will offset the current velocity with the
     * value of the bounding constant to "encourage" the entity to remain inside the bounds
     * of the world.
     *
     * The method also checks that the entity's velocity is not greater than its max speed
     * attribute. If this is the case, it caps the entity's velocity at the max speed
     * attribute.
     *
     * @param velocity Vector2 representation of velocity
     */

    public void setVelocity(Vector2 velocity) {

        // Bounding
        Vector2 offset = new Vector2(0,0);

        if(this.getPosition().getX() < world.getMinimumPosition().getX()) {
            offset.setX(this.boundingConstant);
        } else if(this.getPosition().getX() > world.getMaximumPosition().getX()) {
            offset.setX(this.boundingConstant*-1);
        }

        if(this.getPosition().getY() < world.getMinimumPosition().getY()) {
            offset.setY(this.boundingConstant);
        } else if(this.getPosition().getY() > world.getMaximumPosition().getY()) {
            offset.setY(this.boundingConstant*-1);
        }

        velocity = velocity.add(offset);

        // Obstacle avoidance
        double closestDistance = Double.POSITIVE_INFINITY;
        Obstacle closestObstacle = null;

        for(Entity e : world.getEntities().values()) {
            if(e instanceof Obstacle) {
                if(e.getPosition().subtract(this.getPosition()).getMagnitude() < 50) {
                    if(e.getPosition().subtract(this.getPosition()).getMagnitude() < closestDistance) {
                        closestDistance = e.getPosition().subtract(this.getPosition()).getMagnitude();
                        closestObstacle = (Obstacle) e;
                    }
                }
            }
        }

        if(closestObstacle != null) {
            velocity = closestObstacle.getPosition().subtract(this.getPosition()).scalarMultiply(-1);
        }

        // Speed limiting
        if(velocity.getMagnitude() > this.maxSpeed) {
            Vector2 velocityUnit = velocity.scalarDivide(velocity.getMagnitude());
            velocity = velocityUnit.scalarMultiply(this.maxSpeed);
        }

        this.velocity = velocity;

    }

    /**
     * Returns the entity's energy depletion value (how much energy is used each step)
     * @return Double
     */

    public double getEnergyDepletionValue() {
        return energyDepletionValue;
    }

    /**
     * Sets how much energy is used each step
     * @param energyDepletionValue
     */

    public void setEnergyDepletionValue(double energyDepletionValue) {
        this.energyDepletionValue = energyDepletionValue;
    }

    /**
     * Returns the entity's energy consumption percentage (how much energy is absorbed from food eaten)
     * @return double Percentage of energy absorbed from eaten food
     */

    public double getConsumptionEfficiencyPercentage() {
        return consumptionEfficiencyPercentage;
    }

    public void setConsumptionEfficiencyPercentage(double consumptionEfficiencyPercentage) {
        this.consumptionEfficiencyPercentage = consumptionEfficiencyPercentage;
    }

    /**
     * Entity constructor which sets the position, energy, ID and world of the entity
     *
     * @param species Entity's species (not currently used)
     * @param position Entity's position in the world
     * @param energy Entity's energy
     * @param id Entity's ID (deprecated in favour of the UUID)
     * @param world The world the entity is contained within
     */

    public Entity(String species, Vector2 position, int energy, int id, World world) {

        this.setUuid();

        this.setPosition(position);
        this.energy = energy;
        this.id = id;
        this.world = world;
    }

    /**
     * Return the entity's UUID
     * @return UUID
     */

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the entity's current position in the world as a 2D vector
     * @return Vector2 current position
     */

    public Vector2 getPosition() {
        return position;
    }

    /**
     * Set the entity's current position on the world. This method does NOT perform any bounds checking,
     * and you should probably adjust the entity's velocity in such a way that it will reach this position,
     * instead.
     *
     * @param position Vector2 of position
     */

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    /**
     * Get how much energy the entity currently has
     * @return double Amount of energy
     */

    public double getEnergy() {
        return energy;
    }

    /**
     * Set the amount of energy that the entity has
     * @param energy Amount of energy the entity has
     */

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    /**
     * Get the entity's ID
     * @return int Entity ID
     * @deprecated Use the entity's UUID, instead
     */

    public int getId() {
        return id;
    }

    /**
     * Set the entity's ID
     * @param id Unique ID as integer
     * @deprecated Use the entity's UUID, instead
     */

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns a string representation of the entity in human-readable format
     * @return String string representation of entity
     */

    @Override
    public String toString() {
        return "Entity{" +
                ", x=" + getPosition().getX() +
                ", y=" + getPosition().getY() +
                '}';
    }

    /**
     * Like the toString() method, but with some more information
     *
     * @return String entity information
     * @see Entity#toString()
     */

    public String toText() {
        return "Entity{" +
                ", x=" + getPosition().getX() +
                ", y=" + getPosition().getY() +
                ", energy=" + energy +
                ", id=" + id +
                '}';
    }

    /**
     * Save an entity instance to the disk so that it can be loaded again later
     *
     * @param path Path to save the entity at
     */

    public void save(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load an entity from the disk
     *
     * @param path Path from which to load the entity
     * @return Entity Loaded entity
     */

    public static Object load(String path) {

        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object loadedObject = ois.readObject();

            if(loadedObject instanceof Entity) {
                return (Entity) loadedObject;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // something went wrong
        return null;
    }

    /**
     * Returns the world the entity is contained within
     * @return World
     */

    public World getWorld() {
        return world;
    }

    /**
     * Set the world that the entity is contained within
     * @param world Containing world
     */

    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Get the entity's max speed
     * @return Double maximum speed
     */

    public double getMaxSpeed() {
        return maxSpeed;
    }

    /**
     * Set the entity's maximum speed
     * @param maxSpeed Double
     */

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }
}
