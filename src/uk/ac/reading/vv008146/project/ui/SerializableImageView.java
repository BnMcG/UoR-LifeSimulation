package uk.ac.reading.vv008146.project.ui;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.Serializable;

/**
 * Created by Ben Magee on 10/01/2017.
 * Contact me: ben@bmagee.com
 */

public class SerializableImageView extends ImageView implements Serializable {

    public SerializableImageView() {
    }

    public SerializableImageView(String url) {
        super(url);
    }

    public SerializableImageView(Image image) {
        super(image);
    }
}
