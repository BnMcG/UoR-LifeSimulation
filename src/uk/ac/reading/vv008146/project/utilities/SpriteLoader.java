package uk.ac.reading.vv008146.project.utilities;

import javafx.scene.image.Image;

/**
 * A utility class which loads sprites from the JAR given the appropriate name.
 */

public class SpriteLoader {

    /**
     * Load a sprite from the JAR as a JavaFX image.
     *
     * @param name Sprite name (including directory, excluding type extension)
     * @return Image Loaded image
     */

    public static Image load(String name) {
        return new Image(SpriteLoader.class.getResource("/" + name + ".png").toString());
    }

}
