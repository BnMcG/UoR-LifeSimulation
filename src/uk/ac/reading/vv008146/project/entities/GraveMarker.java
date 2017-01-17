package uk.ac.reading.vv008146.project.entities;

import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;

/**
 * GraveMarker class will mark where an entity died. Not currently used.
 */

public class GraveMarker extends Entity {

    /**
     * Declare a grave marker at the given position in the given world.
     *
     * @param position Vector2 where to position grave
     * @param world World which world to position grave marker in
     */

    public GraveMarker(Vector2 position, World world) {

        this.setUuid();

        this.setSpriteName("objects/crossbones");
        this.setPosition(position);
        this.energy = 0;
        this.world = world;

        this.setVelocity(new Vector2(0,0));
    }
}
