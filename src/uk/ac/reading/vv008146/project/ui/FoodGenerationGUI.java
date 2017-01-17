package uk.ac.reading.vv008146.project.ui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.Food;
import uk.ac.reading.vv008146.project.entities.LivingBeing;

import java.util.prefs.Preferences;

/**
 * FoodGenerationGUI is a window that is used by the user to create food items
 */

public class FoodGenerationGUI {

    private Stage stage;
    private Scene scene;
    private GridPane root;

    /**
     * Create a GUI to generate food within.
     * @param stage Stage to create GUI within.
     */

    public FoodGenerationGUI(Stage stage) {
        this.stage = stage;
        this.root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(20);
        root.setVgap(20);
        root.setPadding(new Insets(25, 25, 25, 25));

        this.scene = new Scene(root, stage.getWidth(), stage.getHeight());

        Label lblName = new Label("Food name: ");
        root.add(lblName, 0, 0);

        TextField txtName = new TextField();
        root.add(txtName, 1, 0, 2, 1);

        Label lblEnergy = new Label("Energy: ");
        root.add(lblEnergy, 0, 1);

        TextField txtEnergy = new TextField();
        txtEnergy.setText("100");
        root.add(txtEnergy, 1, 1, 2, 1);

        // Sprite selection
        Label lblSprite = new Label("Sprite: ");
        root.add(lblSprite, 0, 2);

        ChoiceBox choiceSprite = new ChoiceBox(FXCollections.observableArrayList(
                "apple-red", "candy", "lollipop", "grapes", "lemon", "pepper", "pineapple", "mushroom", "donut"
        ));
        choiceSprite.setPrefWidth(root.getWidth());

        root.add(choiceSprite, 1, 2, 2, 1);

        CheckBox chkPoision = new CheckBox("Poisonous?");
        root.add(chkPoision, 0, 3);

        Button btnCreate = new Button("Create Food Item");
        btnCreate.setPrefWidth(this.root.getWidth());

        btnCreate.setOnAction(ActionEvent -> {

            Preferences preferences = Preferences.userRoot().node("life-simulation");

            // Create a new entity
            Food food = new Food("food/" + choiceSprite.getValue(), new Vector2(0,0), Integer.parseInt(txtEnergy.getText()), new World(1,1,1));

            if(chkPoision.isSelected()) {
                food.setPoisonous(true);
            }

            food.save(preferences.get("settings-directory", ".") + "/" + txtName.getText().toLowerCase() + ".food");
            this.stage.close();
        });


        root.add(btnCreate, 0, 4, 3, 1);

        this.stage.setScene(scene);
    }

}
