package uk.ac.reading.vv008146.project.ui;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Shape;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.utilities.SpriteLoader;

/**
 * Created by Ben Magee on 22/11/2016.
 * Contact me: ben@bmagee.com
 */
public class EntityView {

    protected ImageView sprite;
    protected Entity entity;

    public EntityView(String spriteName, Entity entity) {
        this.sprite = new ImageView(SpriteLoader.load(spriteName));
        this.sprite.setX(entity.getPosition().getX());
        this.sprite.setY(entity.getPosition().getY());
        this.entity = entity;
    }

    public EntityView(Entity e) {
        this.sprite = new ImageView(SpriteLoader.load(e.getSpriteName()));
        this.sprite.setPreserveRatio(true);
        this.sprite.setFitWidth(25);
        this.sprite.setX(e.getPosition().getX());
        this.sprite.setY(e.getPosition().getY());
    }

    public ImageView getSprite() {
        return sprite;
    }

    public void setSprite(ImageView sprite) {
        this.sprite = sprite;
    }

    public Entity getEntity() {
        return entity;
    }
    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
