package uk.ac.reading.vv008146.project;

import javafx.scene.paint.Color;
import uk.ac.reading.vv008146.project.behaviour.BoidFlock;
import uk.ac.reading.vv008146.project.entities.*;
import uk.ac.reading.vv008146.project.generation.SimplexNoise;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.prefs.Preferences;

/**
 * World class represents a simulated world
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

    /**
     * Setup the minimum positions that entities in the world can travel to
     */

    private void setupMinMaxPositions() {
        this.minimumPosition = new Vector2(0,0);
        this.maximumPosition = new Vector2(this.width, this.height);
    }

    /**
     * Return the food detection distance (px) for entities in the world
     * @return int
     */

    public int getFoodDetectionDistance() {
        return foodDetectionDistance;
    }


    /**
     * Set how far away entities can see food
     * @param foodDetectionDistance int how far away food can be detected
     */

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
    public static World fromText(String seed, int maxEntities, int foodDetectionDistance, List<String> food) {

        Preferences preferences = Preferences.userRoot().node("life-simulation");
        Random rng = new Random();

        // Split the seed to get individual properties from it
        String[] splitString = seed.split(" ");

        int width = Integer.parseInt(splitString[0]);
        int height = Integer.parseInt(splitString[1]);
        int area = maxEntities;

        int foodPercent = Integer.parseInt(splitString[2]);
        int obstaclesPercent = Integer.parseInt(splitString[3]);

        // Create a new world
        World world = new World(width, height, maxEntities);

        // Generate a noise map used to distribute food
        world.generateNoise();

        // Setup the min/max positions that an entity can travel to
        world.setupMinMaxPositions();

        // Declare an empty hashmap for flocks, in case there are any
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
            String foodName = food.get(rng.nextInt(food.size()));

            Food newFood = Food.load(preferences.get("settings-directory", ".") + "/" + foodName + ".food", world);
            newFood.setPosition(randomPosAsVec);

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

        // Add living beings to the world
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

    /**
     * Generates a noise map for the world. This map is used to determine where food can spawn.
     * Uses code in SimplexNoise.
     *
     * @see SimplexNoise#noise(double, double, double)
     */

    private void generateNoise() {
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
     * @param seed Seed to generate world from
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

    /**
     * Save a world to a file which can be loaded again later. Uses Java's Serializable capabilities.
     * @param path Path to save world to
     */

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

    /**
     * Load a saved world from a file. The path must be exist and be a world file saved by the current
     * version of the application.
     *
     * @param path Path to world
     * @return World
     */

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
     * Check whether this tile is blocked by an obstacle
     * @param x X coordinate
     * @param y Y coordinate
     * @return Boolean
     *
     * @deprecated Use vector positioning with the avoidObstacles rule instead
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
     *
     * @deprecated Will be removed in a future release. All positions are now empty since transitioning
     * to the GUI
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

     int totalEntityEnergy() {
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

     int foodCount() {
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
     * Move the entity in a random direction. tihs is no longer restricted to compass directions, as a
     * vector is set.
     *
     * @param e LivingBeing to move
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
     * Removes food eaten by entities. If the eaten food is actually another being, will set its energy to 0
     * @param e LivingBeing doing the eating
     */

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
                        Food asFood = (Food) worldEntity;

                        if (asFood.isPoisonous()) {
                            e.setEnergy(e.getEnergy() - (worldEntity).getEnergy());
                        } else {
                            e.setEnergy(e.getEnergy() + (worldEntity).getEnergy() * e.getConsumptionEfficiencyPercentage());
                        }

                        // Gobble gobble
                        // We don't remove food any more, since it can grow back. There was also a pesky ConcurrentModificationException that wouldn't
                        // disappear, even when using an iterator.
                        worldEntity.setEnergy(0);
                    }

                    if(worldEntity instanceof LivingBeing && e instanceof LivingBeing) {

                        if(((LivingBeing) e).isCarnivore()) {
                            if(!worldEntity.equals(e)) {
                                LivingBeing lb = (LivingBeing) e;

                                if (lb.isCarnivore()) {
                                    e.setEnergy(e.getEnergy() + (worldEntity).getEnergy() * e.getConsumptionEfficiencyPercentage());
                                }

                                ((LivingBeing) worldEntity).setEnergy(0);
                            }
                        }
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

                if (!being.isFlock() && !(being.isDead())) {

                    if (being.smellFood(foodDetectionDistance) != null) {

                        Entity meal = being.smellFood(foodDetectionDistance);
                        being.setVelocity(being.getVelocity().add(being.calculateVectorToFood(meal)));


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

    /**
     * Get the world's noise map
     * @return double[][] Noise value for each X,Y in the world
     */

    public double[][] getNoise() {
        return noise;
    }

    /**
     * Get the world's minimum position
     * @return Vector2
     */

    public Vector2 getMinimumPosition() {
        return minimumPosition;
    }

    /**
     * Set the world's minimum position
     * @param minimumPosition Vector2 min position
     */

    public void setMinimumPosition(Vector2 minimumPosition) {
        this.minimumPosition = minimumPosition;
    }

    /**
     * Get the world's maximum position
     * @return Vector2
     */

    public Vector2 getMaximumPosition() {
        return maximumPosition;
    }

    /**
     * Set the world's maximum position
     * @param maximumPosition Vector2 of maximum position
     */

    public void setMaximumPosition(Vector2 maximumPosition) {
        this.maximumPosition = maximumPosition;
    }

    /**
     * Save the world to a file.
     *
     * @see World#toFile(String)
     * @param path Path to save to
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
     * Load the world from a file. The file must exist and be generated by a current version of
     * the application.
     *
     * @param path Path to save file
     * @return World
     * @see World#fromFile(String)
     */

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
