package uk.ac.reading.vv008146.project.utilities;

import javafx.scene.image.Image;

/**
 * Created by Ben Magee on 10/01/2017.
 * Contact me: ben@bmagee.com
 */
public class SpriteLoader {

    public static Image load(String name) {
        return new Image(SpriteLoader.class.getResource("/" + name + ".png").toString());
    }

}
