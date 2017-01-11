package uk.ac.reading.vv008146.project.ui;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import uk.ac.reading.vv008146.project.Vector2;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.LivingBeing;
import uk.ac.reading.vv008146.project.utilities.SpriteLoader;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.prefs.Preferences;

/**
 * Created by Ben Magee on 20/12/2016.
 * Contact me: ben@bmagee.com
 */
public class EntityGenerationGUI {

    private Stage stage;
    private Scene scene;
    private GridPane root;

    private void setupForm() {

    }


    public EntityGenerationGUI(Stage stage) {
        this.stage = stage;
        this.root = new GridPane();
        root.setAlignment(Pos.CENTER);
        root.setHgap(20);
        root.setVgap(20);
        root.setPadding(new Insets(25, 25, 25, 25));

        this.scene = new Scene(root, stage.getWidth(), stage.getHeight());

        //this.pane.setPrefSize(this.stage.getWidth(), this.stage.getHeight());
        //this.pane.setPadding(new Insets(30, 30, 30, 30));

        Label lblName = new Label("Species name: ");
        root.add(lblName, 0, 0);

        TextField txtName = new TextField();
        root.add(txtName, 1, 0, 2, 1);

        Label lblTag = new Label("Tag (displayed below sprite): ");
        root.add(lblTag, 0, 1);

        TextField txtTag = new TextField();
        root.add(txtTag, 1, 1, 2, 1);


        Label lblEnergyDepletion = new Label("Energy Depletion Coefficient: ");
        root.add(lblEnergyDepletion, 0, 2);

        TextField txtEnergyDepletion = new TextField();
        txtEnergyDepletion.setText("0.01");
        root.add(txtEnergyDepletion, 1, 2, 2, 1);


        Label lblConsumptionEfficiency = new Label("Consumption efficiency percentage: ");
        root.add(lblConsumptionEfficiency, 0, 3);

        TextField txtConsumptionEfficiency = new TextField();
        txtConsumptionEfficiency.setText("0.1");
        root.add(txtConsumptionEfficiency, 1, 3, 2, 1);


        Label lblMaxSpeed = new Label("Maximum speed: ");
        root.add(lblMaxSpeed, 0, 4);

        TextField txtMaxSpeed = new TextField();
        txtMaxSpeed.setText("10");
        root.add(txtMaxSpeed, 1, 4, 2, 1);


        Label lblEnergy = new Label("Energy: ");
        root.add(lblEnergy, 0, 5);

        TextField txtEnergy = new TextField();
        txtEnergy.setText("100");
        root.add(txtEnergy, 1, 5, 2, 1);


        Label lblAttackPower = new Label("Attack Power: ");
        root.add(lblAttackPower, 0, 6);

        TextField txtAttackPower = new TextField();
        txtAttackPower.setText("3");
        root.add(txtAttackPower, 1, 6, 2, 1);

        // Herbivore/carnivore/omnivore selection
        ToggleGroup eatingGroup = new ToggleGroup();
        RadioButton radioHerbivore = new RadioButton("Herbivore?");
        RadioButton radioCarnivore = new RadioButton("Carnivore?");
        RadioButton radioOmnivore = new RadioButton("Omnivore?");

        radioHerbivore.setToggleGroup(eatingGroup);
        radioCarnivore.setToggleGroup(eatingGroup);
        radioOmnivore.setToggleGroup(eatingGroup);

        root.add(radioHerbivore, 0, 7);
        root.add(radioCarnivore, 1, 7);
        root.add(radioOmnivore,  2, 7);

        // Sprite selection
        Label lblSprite = new Label("Sprite: ");
        root.add(lblSprite, 0, 8);

        ChoiceBox choiceSprite = new ChoiceBox(FXCollections.observableArrayList(
                "ram", "tiger", "men-santa", "men-snow"
        ));
        choiceSprite.setPrefWidth(root.getWidth());

        root.add(choiceSprite, 1, 8, 2, 1);

        CheckBox chkHerd = new CheckBox("Herd animal?");
        root.add(chkHerd, 0, 9);

        Button btnCreate = new Button("Create Entity");
        btnCreate.setPrefWidth(this.root.getWidth());

        btnCreate.setOnAction(ActionEvent -> {

            Preferences preferences = Preferences.userRoot().node("life-simulation");

            // Create a new entity
            LivingBeing being = new LivingBeing("animals/" + choiceSprite.getValue(), new Vector2(0, 0), new World(1,1,1));
            being.setVelocity(new Vector2(0,0));
            being.save(preferences.get("settings-directory", ".") + "/" + txtName.getText().toLowerCase() + ".entity");

        });


        root.add(btnCreate, 0, 10, 3, 1);

        // gridPane.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        this.stage.setScene(scene);
    }

}
