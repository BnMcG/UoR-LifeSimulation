package uk.ac.reading.vv008146.project.ui;

import javafx.stage.Stage;
import javafx.stage.StageStyle;
import uk.ac.reading.vv008146.project.World;

/**
 * Created by Ben Magee on 10/01/2017.
 * Contact me: ben@bmagee.com
 */
public class WorldStage extends Stage {

    private World world;

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public WorldStage(World w) {
        this.world = w;
    }

    public WorldStage(StageStyle style) {
        super(style);
    }
}
