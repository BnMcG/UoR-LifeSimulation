package uk.ac.reading.vv008146.project.ui;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.LivingBeing;
import uk.ac.reading.vv008146.project.utilities.SpriteLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Ben Magee on 20/12/2016.
 * Contact me: ben@bmagee.com
 */
public class WorldGenerationGUI {

    private WorldStage stage;
    private Scene scene;
    private GridPane root;

    private TextField txtWidth;
    private TextField txtHeight;
    private TextField txtFood;
    private TextField txtObstacles;
    private TextField txtEntities;
    private TextField txtAllowedFood;

    private void setupForm() {

    }


    public WorldGenerationGUI(WorldStage stage) {

        this.stage = stage;
        this.root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(20);
        root.setVgap(20);
        root.setPadding(new Insets(10, 10, 10, 10));

        this.scene = new Scene(root, stage.getWidth(), stage.getHeight());

        //this.pane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());
        //this.pane.setPadding(new Insets(30, 30, 30, 30));

        Label lblWidth = new Label("World width (pixels):");
        root.add(lblWidth, 0, 0);

        txtWidth = new TextField();
        txtWidth.setText("750");
        root.add(txtWidth, 1, 0, 2, 1);


        Label lblHeight = new Label("World height (pixels):");
        root.add(lblHeight, 0, 1);

        txtHeight = new TextField();
        txtHeight.setText("550");
        root.add(txtHeight, 1, 1, 2, 1);


        Label lblFood = new Label("Food Distribution (%):");
        root.add(lblFood, 0, 2);

        txtFood = new TextField();
        txtFood.setText("25");
        root.add(txtFood, 1, 2, 2, 1);

        Label lblObstacles = new Label("Obstacle distribution (%):");
        root.add(lblObstacles, 0, 3);

        txtObstacles = new TextField();
        txtObstacles.setText("5");
        root.add(txtObstacles, 1, 3, 2, 1);


        Label lblEntities = new Label("Enter entities:");
        root.add(lblEntities, 0, 4);

        txtEntities = new TextField();
        txtEntities.setText("name quantity name quantity name quantity");
        root.add(txtEntities, 1, 4, 2, 1);

        Label lblAllowedFod = new Label("Enter allowed food:");
        root.add(lblAllowedFod, 0, 5);

        txtAllowedFood = new TextField();
        txtAllowedFood.setText("foodA foodB foodC");
        root.add(txtAllowedFood, 1, 5, 2, 1);


        Button btnCreate = new Button("Create World");
        btnCreate.setPrefWidth(this.root.getWidth());

        btnCreate.setOnAction(ActionEvent -> {
            setupWorld();
        });


        root.add(btnCreate, 0, 6, 3, 1);

        // gridPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        this.stage.setScene(scene);
    }

    /**
     * This method will setup the world contained in the stage accessible by both this class and the parent
     * window. Once this stage has closed (called after the world is setup), the parent window pulls the world
     * from the stage and sets it as its own world, thus loading the world into the GUI.
     *
     * It's done this way as you cannot (easily? At all) access the parent window and its members even
     * with access to the stage it sits in.
     */

    private void setupWorld() {

        // Convert allowed food textbox into a list of strings by splitting the textbox value at each space
        // and turning it into an array, then passing that array into an ArrayList
        List<String> allowedFoodList = new ArrayList<>(Arrays.asList(txtAllowedFood.getText().split(" ")));

        this.stage.setWorld(World.fromText(txtWidth.getText() + " " + txtHeight.getText() + " " + txtFood.getText() + " " + txtObstacles.getText() + " " + txtEntities.getText(), 100, 100, allowedFoodList));
        this.stage.close();
    }

}
