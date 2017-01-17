package uk.ac.reading.vv008146.project.ui;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.ac.reading.vv008146.project.World;

/**
 * A simple stage used to share worlds between GUIs.
 */

public class WorldStage extends Stage {

    private World world;

    /**
     * Get the shared world
     * @return World
     */

    public World getWorld() {
        return world;
    }

    /**
     * Set the shared world
     * @param world World
     */

    public void setWorld(World world) {
        this.world = world;
    }

    /**
     * Instantiate a new world.
     * @param w
     */

    public WorldStage(World w) {
        this.world = w;
    }


    /**
     * Instantiate a new world stage with no world.
     * @param style StageStyle
     */
    public WorldStage(StageStyle style) {
        super(style);
    }
}
