package uk.ac.reading.vv008146.project;

import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.entities.Food;
import uk.ac.reading.vv008146.project.entities.LivingBeing;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by Ben Magee on 18/10/2016.
 * Contact me: ben@bmagee.com
 */
public class Interface {

    protected World world;
    protected Scanner scanner;

    private void initialiseScanner() {
        this.scanner = new Scanner(System.in);
    }

    public Interface() {
        this.world = new World(2,2,4);
        this.initialiseScanner();
    }

    /**
     * Custom constructor
     * @param world
     */

    public Interface(World world) {
        this.world = world;
        this.initialiseScanner();
    }

    /**
     * Testing main
     * @param args
     */

    public static void main(String[] args) {
        Interface i = new Interface();

        // Option from menu
        String option = "";

        // Select a menu option
        do {
            option = i.menu();

            // add entity
            if(option.equals("A")) {
                try {
                    i.world.addEntity(i.generateRandomPosEntity());
                } catch(Exception ex) {
                    System.out.println("World is full! :(");
                }
            }

            // List entities
            if(option.equals("L")) {
                System.out.println(i.world.getEntities().toString());
            }

            // Display world
            if(option.equals("D")) {
                //i.printWorld();
            }

            // Generate from seed
            if(option.equals("G")) {
                System.out.println("Enter a world seed, using the following format...");
                System.out.println("WORLD_WIDTH WORLD_HEIGHT PERCENT_FOOD PERCENT_OBSTACLES [ANIMAL QUANTITY]*");
                System.out.print("> ");
                String seed = i.scanner.nextLine();
                i.world = World.fromText(seed);
            }

            // 1 step simulation
            if(option.equals("S")) {
                //i.world.simulate();
            }

            // n step simulations
            if(option.equals("V")) {
                System.out.print("Enter number of ticks to simulate:\n> ");
                int steps = Integer.parseInt(i.scanner.nextLine());

                for(int s = 0; s < steps; s++) {
                    // Clear the screen
                    for(int c = 0; c < 100; c++) {
                        System.out.println();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            // World statistics
            if(option.equals("t")) {
                System.out.println("Amount of food objects left in world: " + i.world.foodCount());
                System.out.println("Amount of energy possessed by entities: " + i.world.totalEntityEnergy());
            }

            // Write to file
            if(option.equals("W")) {
                System.out.print("File path: ");
                i.world.toFile(i.scanner.nextLine());
            }

            // Read from file
            if(option.equals("R")) {
                System.out.print("File path: ");
                i.world = World.fromFile(i.scanner.nextLine());
            }

            // Exit on "x"
        } while(!option.equals("x"));
    }

    /**
     * Displays a menu and returns the chosen option
     * @return String chosen option
     */

    private String menu() {
        System.out.println("\n\nPlease select an option: \n\t(A)dd an entity\n\t(G)enerate world from string\n\t(L)ist all entities\n\t(D)isplay world\n\t(S)imulate tick\n\t(V)iew world\n\t(W)rite to file\n\t(R)ead from file\n\tS(t)ats\n\tE(x)it\n");
        System.out.print("> ");
        return scanner.nextLine();
    }

    /**
     * Add an entity to a random position in the world
     */

    protected LivingBeing generateRandomPosEntity() throws Exception {

        if(!world.isFull()) {

            int[] randomPos = world.findRandomEmptyPosition();
            Vector2 randomPosAsVec = new Vector2(randomPos[0], randomPos[1]);

            System.out.print("Please enter a species: ");
            String species = scanner.nextLine();

            System.out.print("Please enter a symbol to represent the entity: ");
            String symbol = scanner.nextLine();

            System.out.print("Pleas enter an energy level: ");
            int energy = Integer.parseInt(scanner.nextLine());

            return new LivingBeing(symbol, randomPosAsVec, this.world);
        } else {
            throw new Exception("World is full");
        }
    }
}
