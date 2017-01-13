package uk.ac.reading.vv008146.project;

import javafx.scene.paint.Color;
import uk.ac.reading.vv008146.project.behaviour.BoidFlock;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.entities.Food;
import uk.ac.reading.vv008146.project.entities.LivingBeing;
import uk.ac.reading.vv008146.project.entities.Obstacle;
import uk.ac.reading.vv008146.project.generation.SimplexNoise;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.prefs.Preferences;

/**
 * Created by Ben Magee on 11/10/2016.
 * Contact me: ben@bmagee.com
 */
public class World implements Serializable {

    private int width;
    private int height;
    private Map<UUID, Entity> entities;

    private Map<String, BoidFlock> flockMap;

    private Vector2 minimumPosition;
    private Vector2 maximumPosition;

    private Color backgroundColor;

    private int foodDetectionDistance;

    private int populationLimit;

    private double[][] noise;

    Preferences preferences;

    /**
     * Default constructor
     */

    public World() {

        this.preferences = Preferences.userRoot().node("life-simulation");

        this.width = 5;
        this.height = 5;
        this.entities = new HashMap<>();
        this.populationLimit = this.width * this.height;
        this.foodDetectionDistance = 4;
        this.setupMinMaxPositions();

        //this.boidTestFlock = new BoidFlock(new ArrayList<LivingBeing>(), new Vector2(0, 0), new Vector2(this.width, this.height), this);
    }

    private void setupMinMaxPositions() {
        this.minimumPosition = new Vector2(0,0);
        this.maximumPosition = new Vector2(this.width, this.height);
    }

    public int getFoodDetectionDistance() {
        return foodDetectionDistance;
    }

    public void setFoodDetectionDistance(int foodDetectionDistance) {
        this.foodDetectionDistance = foodDetectionDistance;
    }

    /**
     * Custom constructor
     *
     * @param width Width of world
     * @param height Height of world
     * @param numEntities Number of entities in world (can't be larger than width*height)
     */

    public World(int width, int height, int numEntities) {
        this.width = width;
        this.height = height;
        this.entities = new HashMap<>();
        this.populationLimit = numEntities;
        this.setupMinMaxPositions();
    }

    /**
     * Get width of world
     * @return Width of world
     */

    public int getWidth() {
        return width;
    }

    /**
     * Get height of world
     * @return height
     */

    public int getHeight() {
        return height;
    }

    /**
     * Get entities in world
     * @return Get array of entities
     */

    public Map<UUID, Entity> getEntities() {
        return entities;
    }

    /**
     * Add entity to the world
     * @param e Entity object
     */

    public void addEntity(Entity e) {

        if(this.entities.size() < this.populationLimit) {
            this.entities.put(e.getUuid(), e);
        } else {
            System.out.println("World is full!");
        }
    }

    /**
     * @return String representation of world
     */

    @Override
    public String toString() {
        return "World{" +
                "width=" + width +
                ", height=" + height +
                ", entities=" + entities.toString() +
                '}';
    }

    /**
     * Returns whether or not the world is full
     * @return
     */

    public boolean isFull() {
        return !(this.entities.size() < this.populationLimit);
    }

    /**
     * Seed format WORLD_WIDTH WORLD_HEIGHT PERCENT_FOOD PERCENT_OBSTACLES [ANIMAL QUANTITY]*
     * @param seed
     */
    public static World fromText(String seed, int maxEntities, int foodDetectionDistance) {

        Preferences preferences = Preferences.userRoot().node("life-simulation");

        String[] splitString = seed.split(" ");

        int width = Integer.parseInt(splitString[0]);
        int height = Integer.parseInt(splitString[1]);
        int area = maxEntities;

        int foodPercent = Integer.parseInt(splitString[2]);
        int obstaclesPercent = Integer.parseInt(splitString[3]);

        World world = new World(width, height, maxEntities);
        world.generateNoise();
        world.setupMinMaxPositions();

        world.flockMap = new HashMap<>();

        // Add food to the world
        float foodPercentageMultiplier = (float) foodPercent / 100;
        int foodQuantity = Math.round((area * foodPercentageMultiplier));
        double foodThreshold = 0.6;

        for(int j = 0; j < foodQuantity; j++) {
            boolean positionFound = false;

            int[] randomPos;

            do {
                randomPos = world.findRandomEmptyPosition();

                if(world.getNoise()[randomPos[0]][randomPos[1]] >= foodThreshold) {
                    positionFound = true;
                }

            } while(!positionFound);

            Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);

            Food newFood = new Food(randomPosAsVec, 5, world);

            world.addEntity(newFood);
        }

        // Add obstacles to the world
        float obstaclesPercentageModifier = (float) obstaclesPercent / 100;
        int obstaclesQuantity = Math.round(area * obstaclesPercentageModifier);
        for(int j = 0; j < obstaclesQuantity; j++) {
            int[] randomPos = world.findRandomEmptyPosition();
            Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);

            Obstacle newObstacle = new Obstacle(randomPosAsVec, world);

            world.addEntity(newObstacle);
        }

        for(int i = 4; i < splitString.length - 1; i+= 2) {
            // Convert quantity to int
            int quantity = Integer.parseInt(splitString[i+1]);
            String entity = splitString[i];

            for(int j = 0; j < quantity; j++) {
                LivingBeing being = LivingBeing.load(preferences.get("settings-directory", ".") + "/" + entity + ".entity", world);
                being.setUuid();

                int[] randomPos = world.findRandomEmptyPosition();
                Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);

                being.setPosition(randomPosAsVec);
                being.setWorld(world);

                if(being.isFlock()) {
                    if(world.flockMap.get(being.getSpecies()) != null) {
                        BoidFlock flock = world.flockMap.get(being.getSpecies());
                        flock.add(being);
                    } else {
                        List<LivingBeing> flockContents = new ArrayList<>();
                        flockContents.add(being);
                        BoidFlock flock = new BoidFlock(flockContents, world.getMinimumPosition(), world.getMaximumPosition(), world);
                        world.flockMap.put(being.getSpecies(), flock);
                    }
                }

                world.addEntity(being);
            }
        }

        world.foodDetectionDistance = foodDetectionDistance;

        return world;
    }

    public void generateNoise() {
        // Noise generation
        this.noise = new double[width][height];
        double scaling = 0.007;
        Random rng = new Random();
        double seed = rng.nextDouble();

        System.out.println("Noise seed: " + seed);

        for(int x = 0; x < this.getWidth(); x++) {
            for(int y = 0; y < this.getHeight(); y++) {

                this.noise[x][y] = SimplexNoise.noise( scaling * x, scaling * y, seed);
            }
        }
    }

    /**
     * Seed format WORLD_WIDTH WORLD_HEIGHT PERCENT_FOOD PERCENT_OBSTACLES [ANIMAL QUANTITY]*
     * @param seed
     */

    public static World fromText(String seed) {
        String[] splitString = seed.split(" ");

        int width = Integer.parseInt(splitString[0]);
        int height = Integer.parseInt(splitString[1]);
        int area = width * height;

        int foodPercent = Integer.parseInt(splitString[2]);
        int obstaclesPercent = Integer.parseInt(splitString[3]);

        World world = new World(width, height, width*height);
        world.generateNoise();
        world.setupMinMaxPositions();

        // Add food to the world
        float foodPercentageMultiplier = (float) foodPercent / 100;
        int foodQuantity = Math.round((area * foodPercentageMultiplier));

        for(int j = 0; j < foodQuantity; j++) {
            int[] randomPos = world.findRandomEmptyPosition();
            Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);
            world.addEntity(new Food(randomPosAsVec, 5, world));
        }

        // Add obstacles to the world
        float obstaclesPercentageModifier = (float) obstaclesPercent / 100;
        int obstaclesQuantity = Math.round(area * obstaclesPercentageModifier);
        for(int j = 0; j < obstaclesQuantity; j++) {
            int[] randomPos = world.findRandomEmptyPosition();
            Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);
            world.addEntity(new Obstacle(randomPosAsVec, world));
        }

        world.foodDetectionDistance = 15;

        for(int i = 4; i < splitString.length - 1; i+= 2) {
            // Convert quantity to int
            int quantity = Integer.parseInt(splitString[i+1]);

            for(int j = 0; j < quantity; j++) {
                int[] randomPos = world.findRandomEmptyPosition();
                Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);
                world.addEntity(new LivingBeing(splitString[i].substring(0,1), randomPosAsVec, world));
            }
        }

        return world;
    }

    public void toFile(String path) {
        // Export the world to a file
        FileOutputStream fileOS = null;
        try {
            fileOS = new FileOutputStream(path);
            ObjectOutputStream objectOS = new ObjectOutputStream(fileOS);

            objectOS.writeObject(this);
            objectOS.close();
            fileOS.close();

        } catch(IOException io) {
            io.printStackTrace();
        }
    }

    public static World fromFile(String path) {
        World world = null;

        try {
            FileInputStream fileIS = new FileInputStream(path);
            ObjectInputStream objectIS = new ObjectInputStream(fileIS);
            world = (World) objectIS.readObject();
            return world;

        } catch(IOException | ClassNotFoundException io) {
            io.printStackTrace();
        }

        return world;
    }

    /**
     * Check whether or not food is at a given position
     *
     * @param x X coordinate to check
     * @param y Y coordinate to check
     * @return
     *

    public boolean foodAt(int x, int y) {
        boolean food = false;

        for(Entity e : this.entities) {
            if(e != null) {
                if(e instanceof Food && e.gethPos() == x && e.getvPos() == y) {
                    food = true;
                    break;
                }
            }
        }

        return food;
    } */

    /**
     * Check whether this tile is blocked by a
     * @param x X coordinate
     * @param y Y coordinate
     * @return
     */

    public boolean blocked(double x, double y) {
        boolean blocked = false;

        for(Entity e : this.entities.values()) {
            if(e != null) {
                if(e.getPosition().getX() == x && e.getPosition().getY() == y) {
                    if(!(e instanceof Food)) {
                        blocked = true;
                    }
                }
            }
        }

        return blocked;
    }

    /**
     * Find an empty position in the world
     * @return horizontal position at return[0] and vertical position at return[1]
     */

    public int[] findRandomEmptyPosition() {

        Random rng = new Random();
        int[] toReturn = new int[2];

        toReturn[0] = rng.nextInt(this.getWidth() - 1);
        toReturn[1] = rng.nextInt(this.getHeight() - 1);

        return toReturn;
    }

    /**
     * Find the total amount of energy held by the entities
     *
     * @return Integer energy count
     */

    public int totalEntityEnergy() {
        int sum = 0;

        for(Entity e : this.entities.values()) {

            if(e == null) {
                continue;
            }

            if(!(e instanceof Food)) {
                sum+= e.getEnergy();
            }
        }

        return sum;
    }

    /**
     * Find the number of items of food left in the world
     * @return Number of food items left
     */

    public int foodCount() {
        int sum = 0;

        for(Entity e : this.entities.values()) {

            if(e == null) {
                continue;
            }

            if(e instanceof Food) {
                sum++;
            }
        }

        return sum;
    }

    /**
     * Move the entity in a random direction
     * @param e
     */

    private void moveEntityInRandomDirection(LivingBeing e) {

        // Set a new random goal
        if((System.currentTimeMillis() - e.getLastRandomMovement()) > 5000) {
            Random rng = new Random();
            e.setGoal(new Vector2(rng.nextInt((int) this.getMaximumPosition().getX()), rng.nextInt((int) this.getMaximumPosition().getY())));
            e.setLastRandomMovement(System.currentTimeMillis());
        }

        e.setVelocity(e.getGoal().subtract(e.getPosition()));
        e.setEnergy(e.getEnergy() - e.getEnergyDepletionValue());

    }

    /**
     * Simulate a tick in the world


    public void simulate() {

        for(int i = 0; i < this.entities.size() - 1; i++) {

            Entity e = entities.get(i);

            // Can't simulate food or obstacles
            if(e instanceof Food || e instanceof Obstacle) {
                // Abort mission, cap'n!
                continue;
            }

            // Scan for food
            if(e instanceof LivingBeing) {

                // Cast the variable so we can smell for food
                LivingBeing being = (LivingBeing) e;

                if(being.smellFood(Direction.NORTH, foodDetectionDistance)) {
                    // Move in this direction
                    if(!(this.blocked(e.getPosition().getX(), e.getPosition().getY() - 1))) {
                        if(e.getPosition().getY() - 1 > 0) {
                            e.setPosition(new Vector2(e.getPosition().getX(), e.getPosition().getY() - 1));
                        } else {
                            moveEntityInRandomDirection((LivingBeing) e);
                        }
                    }
                }
                else if(being.smellFood(Direction.EAST, foodDetectionDistance)) {
                    if(!(this.blocked(e.gethPos() + 1, e.getvPos()))) {
                        if(e.gethPos() + 1 <= this.width) {
                            e.sethPos(e.gethPos() + 1);
                        } else {
                            moveEntityInRandomDirection((LivingBeing) e);
                        }
                    }
                }
                else if(being.smellFood(Direction.SOUTH, foodDetectionDistance)) {
                    if(!(this.blocked(e.gethPos(), e.getvPos() + 1))) {
                        if(e.getvPos() + 1 <= this.height) {
                            e.setvPos(e.getvPos() + 1);
                        } else {
                            moveEntityInRandomDirection((LivingBeing) e);
                        }
                    }
                }
                else if(being.smellFood(Direction.WEST, foodDetectionDistance)) {
                    if(!(this.blocked(e.gethPos() -1, e.getvPos()))) {
                        if(e.gethPos() - 1 > 0) {
                            e.sethPos(e.gethPos() - 1);
                        } else {
                            moveEntityInRandomDirection((LivingBeing) e);
                        }
                    }
                } else {
                    moveEntityInRandomDirection((LivingBeing) e);
                }

                removeEatenFood(e);
            }
        }
    } */

    public void removeEatenFood(Entity e) {

        // How far away an entity can actually be from food before eating it
        double variance = 10;

        for(Entity worldEntity : entities.values()) {
            if(worldEntity.getPosition().getX() > e.getPosition().getX() - variance && worldEntity.getPosition().getX() < e.getPosition().getX() + variance) {
                if(worldEntity.getPosition().getY() > e.getPosition().getY() - variance && worldEntity.getPosition().getY() < e.getPosition().getY() + variance) {
                    if (worldEntity instanceof Food) {
                        // Dinner time for our entity
                        // Eating isn't a 100% efficient process though, so the entity won't gain all of the energy in the
                        // food, some will be expended... :(
                        e.setEnergy(e.getEnergy() + (worldEntity).getEnergy() * e.getConsumptionEfficiencyPercentage());

                        // Gobble gobble
                        // We don't remove food any more, since it can grow back. There was also a pesky ConcurrentModificationException that wouldn't
                        // disappear, even when using an iterator.
                        worldEntity.setEnergy(0);
                    }
                }
            }
        }
    }

    /**
     * Simulate without the limitations of a grid structure. For use in interfaces that aren't constricted
     * to grid-like behaviour
     */

    public void simulateOutsideOfGrid() {

        for(Entity e : entities.values()) {

            // Scan for food
            if(e instanceof LivingBeing) {

                // Cast the variable so we can smell for food
                LivingBeing being = (LivingBeing) e;

                if (being.getEnergy() <= 0) {
                    being.setDead(true);
                }

                // Only process living beings that aren't part of a flock, this should prevent stuttering

                if (!being.isFlock()) {

                    if (being.smellFood(foodDetectionDistance) != null) {

                        Food food = being.smellFood(foodDetectionDistance);
                        being.setVelocity(being.getVelocity().add(being.calculateVectorToFood(food)));


                    } else {
                        // No food detected, go for a wander...
                        moveEntityInRandomDirection((LivingBeing) e);
                    }

                    e.setPosition(e.getPosition().add(e.getVelocity()));
                }

                removeEatenFood(e);

            }
        }

        /*
         * Iterate over each flock in the world and simulate them. Herds/flocks are simulated separately to
         * other entities in the world so we can properly apply Boids algorithm rules to make them move as
         * a flock.
         */

        // More Lambda goodness, it seems
        this.flockMap.values().forEach(BoidFlock::simulateFlock);

    }

    public double[][] getNoise() {
        return noise;
    }

    public Vector2 getMinimumPosition() {
        return minimumPosition;
    }

    public void setMinimumPosition(Vector2 minimumPosition) {
        this.minimumPosition = minimumPosition;
    }

    public Vector2 getMaximumPosition() {
        return maximumPosition;
    }

    public void setMaximumPosition(Vector2 maximumPosition) {
        this.maximumPosition = maximumPosition;
    }

    public void save(String path) {
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static World load(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object loadedObject = ois.readObject();

            if(loadedObject instanceof World) {
                return (World) loadedObject;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // something went wrong
        return null;
    }

}
