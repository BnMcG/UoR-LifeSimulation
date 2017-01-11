package uk.ac.reading.vv008146.project.entities;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.ui.SerializableImageView;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.UUID;

/**
 * Created by Ben Magee on 11/10/2016.
 * Contact me: ben@bmagee.com
 */
public abstract class Entity implements Serializable {

    protected transient World world;

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

    public void setSpriteName(String name) {
        this.spriteName = name;
    }

    public String getSpriteName() {
        return spriteName;
    }

    protected void generateUuid() {
        this.uuid = UUID.randomUUID();
    }

    public Entity() {

        this.generateUuid();

        this.setSpriteName("objects/pin.png");
        this.setPosition(new Vector2(0, 0));
        this.energy = 100;
        this.id = 1;
        this.world = new World();

        this.energyDepletionValue = 0.01d;
        this.boundingConstant = 5;

        this.maxSpeed = 1.5;

        // https://en.wikipedia.org/wiki/Ten_percent_law
        this.consumptionEfficiencyPercentage = 0.10;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector2 velocity) {

        if(velocity.getMagnitude() > this.maxSpeed) {
            Vector2 velocityUnit = velocity.scalarDivide(velocity.getMagnitude());
            this.velocity = velocityUnit.scalarMultiply(this.maxSpeed);
        } else {
            this.velocity = velocity;
        }

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

        this.velocity.add(offset);

    }

    public double getEnergyDepletionValue() {
        return energyDepletionValue;
    }

    public void setEnergyDepletionValue(double energyDepletionValue) {
        this.energyDepletionValue = energyDepletionValue;
    }

    public double getConsumptionEfficiencyPercentage() {
        return consumptionEfficiencyPercentage;
    }

    public void setConsumptionEfficiencyPercentage(double consumptionEfficiencyPercentage) {
        this.consumptionEfficiencyPercentage = consumptionEfficiencyPercentage;
    }

    public Entity(String species, Vector2 position, int energy, int id, World world) {

        this.generateUuid();

        this.setPosition(position);
        this.energy = energy;
        this.id = id;
        this.world = world;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Vector2 getPosition() {
        return position;
    }

    public void setPosition(Vector2 position) {
        this.position = position;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Entity{" +
                ", x=" + getPosition().getX() +
                ", y=" + getPosition().getY() +
                '}';
    }

    public String toText() {
        return "Entity{" +
                ", x=" + getPosition().getX() +
                ", y=" + getPosition().getY() +
                ", energy=" + energy +
                ", id=" + id +
                '}';
    }

    /**
     * Generate an entity from user input
     *
     * @param world The world the entity will be added to
     * @return The generated Entity object
     */

    /*public static Entity generateEntity(World world) {
        Scanner inputScanner = new Scanner(System.in);

        System.out.print("Please enter a species: ");
        String species = inputScanner.nextLine();

        System.out.print("Please enter a symbol to represent the entity: ");
        String symbol = inputScanner.nextLine();

        System.out.print("Pleas enter a horizontal position: ");
        int hpos = Integer.parseInt(inputScanner.nextLine());

        System.out.print("Pleas enter a vertical position: ");
        int vpos = Integer.parseInt(inputScanner.nextLine());

        System.out.print("Pleas enter an energy level: ");
        int energy = Integer.parseInt(inputScanner.nextLine());

        System.out.print("Pleas enter a unique ID: ");
        int id = Integer.parseInt(inputScanner.nextLine());

        return new Entity(species, symbol, hpos, vpos, energy, id, world);
    } */

    /**
     * Save an entity instance to the disk so that it can be loaded again later
     *
     * @param path
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
     * @param path
     * @return
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

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }
}
