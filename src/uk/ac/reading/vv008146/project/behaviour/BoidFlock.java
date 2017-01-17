package uk.ac.reading.vv008146.project.behaviour;

import javafx.scene.layout.Pane;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.entities.Food;
import uk.ac.reading.vv008146.project.entities.LivingBeing;
import uk.ac.reading.vv008146.project.entities.Obstacle;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * This class will allow the entities within its list to act as a single flock or herd. Great for setting
 * up living beings to behave in herds rather than as individuals
 */

public class BoidFlock implements Serializable {

    // All entities to consider when applying boid rules
    private List<LivingBeing> flock;

    // World the flock exists in
    private World world;

    private double centreOfMassMovementFactor; // How far towards the flock's CoM to move a boid on each iteration (as a %) - default is 1 to move it 1% of the way
    private double minimumBoidDistance; // Minimum distance between two boids of the flock

    private long goalLastChanged;
    private Vector2 goal;

    private Random rng;

    /**
     * Instantiate a boid flock with a given list of entities. Use the world's min/max position, although
     * these are no longer required to be passed into this method and will be removed from the constructor
     * when refactoring can take place.
     *
     * @param flock List of living beings to include in the flock
     * @param minPosition Maximum position the flock can move to
     * @param maxPosition Minimum position the flock can move to
     * @param world The world the flock is in
     */

    public BoidFlock(List<LivingBeing> flock, Vector2 minPosition, Vector2 maxPosition, World world) {
        this.flock = flock;
        this.world = world;

        this.centreOfMassMovementFactor = 1;
        this.minimumBoidDistance = 55;

        this.rng = new Random();

        this.goal = (new Vector2(rng.nextInt(world.getWidth()), rng.nextInt(world.getHeight())));
        this.goalLastChanged = System.currentTimeMillis();
    }

    /**
     * Add a living being to an existing flock
     * @param e LivingBeing to add to the flock
     */

    public void add(LivingBeing e) {
        this.flock.add(e);
    }

    /**
     * Simulate the flock's behaviour. This will update the positions and speeds of the entities
     * within the flock, according to the rules dictated in a Boids design
     */

    public void simulateFlock() {

        // Only change the goal if defined number of seconds has passed. This allows the boids some time
        // to actually travel to the target point without just jittering in one place
        if(System.currentTimeMillis() - this.goalLastChanged > 1000) {
            this.goal = (new Vector2(rng.nextInt(world.getWidth()), rng.nextInt(world.getHeight())));
            this.goalLastChanged = System.currentTimeMillis();
        }

        for(LivingBeing boid : this.flock) {

            Vector2 CoMVector = this.centreOfMassRule(boid);
            Vector2 velocityVector = this.normaliseVelocityRule(boid).scalarMultiply(rng.nextInt(5));
            Vector2 positionVector = this.randomPositionRule(boid);
            Vector2 meanderVector = this.meanderingRule(boid);

            //boid.setVelocity(boid.getVelocity().add(CoMVector).add(boundingVector).add(meanderVector).add(positionVector).add(velocityVector).add(obstacleAvoidanceVector).add(distanceVector));

            boid.setVelocity(boid.getVelocity().add(CoMVector).add(velocityVector).add(positionVector));

            Vector2 distanceVector = this.maintainDistanceRule(boid);
            boid.setVelocity(boid.getVelocity().add(distanceVector));

            // Only invert the CoM of the flock on 20% of occasions so that they stay together more often than not
            if(this.rng.nextInt(100) > 80) {
                Vector2 explodeVector = this.centreOfMassRule(boid).scalarMultiply(-1);
                boid.setVelocity(boid.getVelocity().add(explodeVector));
            }

            //Vector2 obstacleAvoidanceVector = this.avoidObstaclesRule(boid);
            //boid.setVelocity(boid.getVelocity().add(obstacleAvoidanceVector));

            boid.setPosition(boid.getPosition().add(boid.getVelocity()));

        }

    }

    /**
     * Draws the boids towards the centre of the flock so that they remain reasonably close together
     *
     * @param boid Boid to consider
     * @return Vector2 The offset from the current velocity to allow movement towards the CoM
     */

    private Vector2 centreOfMassRule(LivingBeing boid) {

        Vector2 perceivedCoM = new Vector2(0,0);

        for(LivingBeing e : this.flock) {
            if(!(e.equals(boid))) {
                perceivedCoM = perceivedCoM.add(e.getPosition());
            }
        }

        // Divide by number of boids - 1 (discount the boid we're currently analysing, as we want the
        // CoM for the whole flock) to return a mean average CoM vector
        perceivedCoM = perceivedCoM.scalarDivide(this.flock.size() - 1);

        // We don't want to immediately jump to the CoM, so provide a vector to gradually move towards it
        return perceivedCoM.scalarDivide(100);
    }

    /**
     * This rule stops the boids moving too close together and overlapping each other
     * @param boid Boid to consider
     * @return Vector2 offset from the current velocity
     */

    private Vector2 maintainDistanceRule(LivingBeing boid) {
        // Method will return a vector that we can use to offset the boid's current position, so initalise to 0
        Vector2 offset = new Vector2(0, 0);

        for(LivingBeing e : this.flock) {
            if(!(e.equals(boid))) {
                double distance = e.getPosition().subtract(boid.getPosition()).getMagnitude();

                if(distance < this.minimumBoidDistance) {
                    Vector2 currentDistance = e.getPosition().subtract(boid.getPosition());
                    offset = offset.subtract(currentDistance);
                }
            }
        }

        return offset;

    }

    /**
     * Normalise the velocity of the current boid so that it's roughly the same as the velocity of
     * the rest of the flock
     *
     * @param boid Boid to consider
     * @return Vector2 velocity offset
     */

    private Vector2 normaliseVelocityRule(LivingBeing boid) {

        Vector2 normalisedVelocity = new Vector2(0,0);

        for(LivingBeing e : this.flock) {
            if(!(e.equals(boid))) {
                normalisedVelocity = normalisedVelocity.add(e.getVelocity());
            }
        }

        // Don't count the boid we're currently considering
        normalisedVelocity = normalisedVelocity.scalarDivide(this.flock.size() - 1);

        return normalisedVelocity.subtract(boid.getVelocity()).scalarDivide(16);
    }

    /**
     * Guide the boids either towards food or a random position in the world, so that they aren't
     * just bimbling about in a corner when not near food
     *
     * @param boid Boid to consider
     * @return Vector2 a vector towards the current goal position
     */

    private Vector2 randomPositionRule(LivingBeing boid) {

        if(boid.smellFood(world.getFoodDetectionDistance()) != null) {
            Entity meal = boid.smellFood(world.getFoodDetectionDistance());
            this.goal = meal.getPosition();
        }

        boid.setGoal(this.goal);

        return boid.getGoal().subtract(boid.getPosition());
    }

    /**
     * Guide the boids to avoid obstacles on the map. Boids will only avoid the closest obstacle
     * to them
     *
     * @param boid Boid to consider
     * @return Vector2 Offset of current velocity to avoid the closest detected obstacle
     */

    private Vector2 avoidObstaclesRule(LivingBeing boid) {

        Vector2 avoidOffset = new Vector2(0,0);

        double closestDistance = Double.POSITIVE_INFINITY;
        Obstacle closestObstacle = null;

        for(Entity e : world.getEntities().values()) {
            if(e instanceof Obstacle) {
                if(e.getPosition().subtract(boid.getPosition()).getMagnitude() < 50) {
                    if(e.getPosition().subtract(boid.getPosition()).getMagnitude() < closestDistance) {
                        closestDistance = e.getPosition().subtract(boid.getPosition()).getMagnitude();
                        closestObstacle = (Obstacle) e;
                    }
                }
            }
        }

        if(closestObstacle != null) {
            avoidOffset = closestObstacle.getPosition().subtract(boid.getPosition()).scalarMultiply(-1);
        }

        return avoidOffset;

    }

    /**
     * A rule that should introduce some random movement in each boid so that their movement across
     * the map doesn't look quite so mechanical and perfect.
     *
     * @param boid Boid to consider
     * @return Vector2 offset of current velocity
     */

    private Vector2 meanderingRule(LivingBeing boid) {

        Vector2 mV = new Vector2(rng.nextInt(10), rng.nextInt(10));

        int inversion = rng.nextInt(100);

        if(inversion > 25 && inversion < 50) {
            mV = new Vector2(mV.getX() * -1, mV.getY());
        }

        if(inversion < 25) {
            mV = new Vector2(mV.getX(), mV.getY() * -1);
        }

        if(inversion > 50 && inversion < 75) {
            mV = new Vector2(mV.getX() * -1, mV.getY() * -1);
        }

        try {
            return boid.getGoal().subtract(mV);
        } catch(NullPointerException ex) {
            return mV;
        }
    }

    public Vector2 getGoal() {
        return goal;
    }
}
