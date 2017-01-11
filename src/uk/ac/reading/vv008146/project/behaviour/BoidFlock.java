package uk.ac.reading.vv008146.project.behaviour;

import javafx.scene.layout.Pane;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.entities.Food;
import uk.ac.reading.vv008146.project.entities.LivingBeing;
import uk.ac.reading.vv008146.project.entities.Obstacle;

import java.util.List;
import java.util.Random;

/**
 * Created by Ben Magee on 20/12/2016.
 * Contact me: ben@bmagee.com
 *
 * This class will allow the entities within its list to act as a single flock or herd. Great for setting
 * up living beings to behave in herds rather than as individuals
 */
public class BoidFlock {

    // All entities to consider when applying boid rules
    private List<LivingBeing> flock;

    // World the flock exists in
    private World world;

    private double centreOfMassMovementFactor; // How far towards the flock's CoM to move a boid on each iteration (as a %) - default is 1 to move it 1% of the way
    private double minimumBoidDistance; // Minimum distance between two boids of the flock
    private double boundingConstant; // How quickly boids will return back inside the bounds if they travel outside

    private double maximumSpeed; // Max speed of the boids

    private Vector2 minimumPosition; // The minimum X/Y values the boids can travel to
    private Vector2 maximumPosition; // The maximum X/Y values that the boids can travel to

    private Vector2 meanderingVector;

    private long goalLastChanged;
    private long lastSimulation;
    private Vector2 goal;

    private Random rng;

    public BoidFlock(List<LivingBeing> flock, Vector2 minPosition, Vector2 maxPosition, World world) {
        this.flock = flock;
        this.world = world;

        this.centreOfMassMovementFactor = 0.005;
        this.minimumBoidDistance = 50;
        this.boundingConstant = 2.5;

        this.maximumSpeed = 1.5;

        this.minimumPosition = minPosition;
        this.maximumPosition = maxPosition;

        this.rng = new Random();
        this.meanderingRule(null);

        this.goal = (new Vector2(rng.nextInt(world.getWidth()), rng.nextInt(world.getHeight())));
        this.goalLastChanged = System.currentTimeMillis();

        this.lastSimulation = 0;

    }

    public void add(LivingBeing e) {
        this.flock.add(e);
    }

    public void simulateFlock() {

        if(System.currentTimeMillis() - this.goalLastChanged > 15000) {
            this.goal = (new Vector2(rng.nextInt(world.getWidth()), rng.nextInt(world.getHeight())));
            this.goalLastChanged = System.currentTimeMillis();
        }

        for(LivingBeing boid : this.flock) {

            Vector2 CoMVector = this.centreOfMassRule(boid).scalarMultiply(0.5);
            Vector2 boundingVector = this.boundingRule(boid);
            Vector2 velocityVector = this.normaliseVelocityRule(boid);
            Vector2 positionVector = this.randomPositionRule(boid);
            Vector2 obstacleAvoidanceVector = this.avoidObstaclesRule(boid);
            //Vector2 meanderVector = this.meanderingRule(boid);
            Vector2 distanceVector = this.maintainDistanceRule(boid).scalarMultiply(rng.nextInt(100));

            boid.setVelocity(boid.getVelocity().add(CoMVector).add(boundingVector).add(velocityVector).add(positionVector).add(obstacleAvoidanceVector).add(distanceVector));

            // Limit the maximum speed
            boid.setVelocity(speedLimitationRule(boid));

            boid.setPosition(boid.getPosition().add(boid.getVelocity()));
        }

    }

    private Vector2 centreOfMassRule(LivingBeing boid) {

        Vector2 perceivedCoM = new Vector2(0,0);

        for(LivingBeing e : this.flock) {
            if(!(e.equals(boid))) {
                perceivedCoM = perceivedCoM.add(e.getPosition());
            }
        }

        // Divide by number of boids - 1 (discount the boid we're currently analyisng, as we want the
        // CoM for the whole flock) to return a mean average CoM vector
        perceivedCoM = perceivedCoM.scalarDivide(this.flock.size() - 1);

        // We don't want to immediately jump to the CoM, so provide a vector to gradually move towards it
        return perceivedCoM.scalarDivide(1000).scalarMultiply(this.centreOfMassMovementFactor);
    }

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

    private Vector2 normaliseVelocityRule(LivingBeing boid) {

        Vector2 normalisedVelocity = new Vector2(0,0);

        for(LivingBeing e : this.flock) {
            if(!(e.equals(boid))) {
                normalisedVelocity = normalisedVelocity.add(e.getVelocity());
            }
        }

        // Don't count the boid we're currently considering
        normalisedVelocity = normalisedVelocity.scalarDivide(this.flock.size() - 1);

        return normalisedVelocity.subtract(boid.getVelocity()).scalarDivide(32);
    }

    private Vector2 boundingRule(LivingBeing boid) {
        Vector2 offset = new Vector2(0,0);

        if(boid.getPosition().getX() < this.minimumPosition.getX()) {
            offset.setX(this.boundingConstant);
        } else if(boid.getPosition().getX() > this.maximumPosition.getX()) {
            offset.setX(this.boundingConstant*-1);
        }

        if(boid.getPosition().getY() < this.minimumPosition.getY()) {
            offset.setY(this.boundingConstant);
        } else if(boid.getPosition().getY() > this.maximumPosition.getY()) {
            offset.setY(this.boundingConstant*-1);
        }

        return offset;
    }

    private Vector2 speedLimitationRule(LivingBeing boid) {
        Vector2 max = boid.getVelocity();

        if(boid.getVelocity().getMagnitude() > this.maximumSpeed) {
            Vector2 velocityUnit = boid.getVelocity().scalarDivide(boid.getVelocity().getMagnitude());
            max = (velocityUnit.scalarMultiply(this.maximumSpeed));
        }

        return max;
    }

    private Vector2 randomPositionRule(LivingBeing boid) {

        if(boid.smellFood(world.getFoodDetectionDistance()) != null) {
            Food food = boid.smellFood(world.getFoodDetectionDistance());
            boid.setGoal(food.getPosition());
        } else {
            boid.setGoal(this.goal);
        }

        return boid.getGoal().subtract(boid.getPosition()).scalarDivide(1000).scalarMultiply(this.centreOfMassMovementFactor).scalarMultiply(2);
    }

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
            avoidOffset = closestObstacle.getPosition().subtract(boid.getPosition()).scalarDivide(100).scalarMultiply(-1);
        }

        return avoidOffset;

    }

    private Vector2 meanderingRule(LivingBeing boid) {

        this.meanderingVector = new Vector2(rng.nextInt(10)/100, rng.nextInt(10)/100);

        int inversion = rng.nextInt(100);

        if(inversion > 25 && inversion < 50) {
            this.meanderingVector = new Vector2(this.meanderingVector.getX() * -1, this.meanderingVector.getY());
        }

        if(inversion < 25) {
            this.meanderingVector = new Vector2(this.meanderingVector.getX(), this.meanderingVector.getY() * -1);
        }

        if(inversion > 50 && inversion < 75) {
            this.meanderingVector = new Vector2(this.meanderingVector.getX() * -1, this.meanderingVector.getY() * -1);
        }

        try {
            return boid.getGoal().subtract(this.meanderingVector);
        } catch(NullPointerException ex) {
            return this.meanderingVector;
        }
    }

    public Vector2 getGoal() {
        return goal;
    }
}
