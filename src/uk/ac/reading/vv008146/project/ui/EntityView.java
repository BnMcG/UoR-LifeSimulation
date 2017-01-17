package uk.ac.reading.vv008146.project.ui;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.utilities.SpriteLoader;

/**
 * EntityView links an entity with a sprite within the world
 */

public class EntityView {

    protected ImageView sprite;
    protected Entity entity;

    /**
     * Declare an EntityView for the given entity.
     * @param e Entity to link to
     */

    public EntityView(Entity e) {

        this.entity = e;

        this.sprite = new ImageView(SpriteLoader.load(e.getSpriteName()));
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(25);
        this.sprite.setX(e.getPosition().getX());
        this.sprite.setY(e.getPosition().getY());
    }

    /**
     * Get an ImageView of the entity's sprite to display in the world
     * @return ImageView Entity's sprite
     */

    public ImageView getSprite() {
        return sprite;
    }

    /**
     * Get the entity represented by this view
     * @return Entity
     */

    public Entity getEntity() {
        return entity;
    }

    /**
     * Set the entity represented by this view.
     * @param entity Entity to represent
     */

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
