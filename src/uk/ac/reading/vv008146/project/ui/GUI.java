package uk.ac.reading.vv008146.project.ui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import uk.ac.reading.vv008146.project.World;
import uk.ac.reading.vv008146.project.entities.Entity;
import uk.ac.reading.vv008146.project.entities.GraveMarker;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * GUI is the main window of the application, used to display a simulated world
 */

public class GUI extends Application {

    private MediaPlayer mp;

    private Stage primaryStage;
    private Pane worldCanvas;
    private MenuBar menuBar;
    private AnimationTimer simulationTimer;

    private Preferences preferences;

    private boolean simulate;
    private boolean showDebuggingArtifacts;

    private World simulatedWorld;

    private List<EntityView> views;

    private Random rng;

    /**
     * Open the GUI
     * @param primaryStage Stage to open the GUI onto
     * @throws Exception Some exceptions may be thrown.
     */

    @Override
    public void start(Stage primaryStage) throws Exception {

        // preferences used to access first-run settings and whatnot
        this.preferences = Preferences.userRoot().node("life-simulation");
        this.setupPreferences();
        this.views = new ArrayList<EntityView>();
        this.rng = new Random();

        // Set the window title
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Life");

        this.setupMenus();
        this.setupMedia();
        this.setupCanvas();

        // Disable simulation initially
        this.simulate = false;

        // Setup a border pane to add UI elements to
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 800, 600);

        // Set the menu bar and canvas position
        root.setCenter(worldCanvas);
        root.setTop(menuBar);

        // Show the GUI
        primaryStage.setScene(scene);
        primaryStage.show();

        // Lay everything out correctly
        worldCanvas.layout();

        this.simulationTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                // If the world is  simulating, update it
                if(simulate) {
                    simulatedWorld.simulateOutsideOfGrid();

                    // Sync entities
                    updateEntityViews();

                }
            }
        };
    }

    /**
     * Setup the menus in the application, as well as ActionEvent listeners to
     * control what each item does.
     */

    private void setupMenus() {
        // Setup menu
        this.menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        // FILE MENU
        Menu fileMenu = new Menu("File");

        MenuItem newConfigItem = new MenuItem("New configuration");
        newConfigItem.setOnAction(actionEvent -> {
            WorldStage newWorldStage = new WorldStage(simulatedWorld);
            newWorldStage.setWidth(500);
            newWorldStage.setHeight(400);
            newWorldStage.setResizable(false);
            newWorldStage.setTitle("World Creation");
            newWorldStage.initModality(Modality.APPLICATION_MODAL);
            newWorldStage.initOwner(primaryStage);

            new WorldGenerationGUI(newWorldStage);

            newWorldStage.setOnHidden(ActionEvent -> {

                if(newWorldStage.getWorld() != null) {
                    this.simulatedWorld = newWorldStage.getWorld();
                    this.setupWorldCanvas();

                } else {
                    System.err.println("Epic fail!");
                }

            });

            newWorldStage.show();
        });

        fileMenu.getItems().add(newConfigItem);

        MenuItem openConfigItem = new MenuItem("Open configuration");
        fileMenu.getItems().add(openConfigItem);
        openConfigItem.setOnAction(ActionEvent -> {
            Preferences preferences = Preferences.userRoot().node("life-simulation");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open World");
            fileChooser.setInitialDirectory(new File(preferences.get("settings-directory", ".")));
            File file = fileChooser.showOpenDialog(primaryStage);

            if(file != null) {
                this.simulatedWorld = World.load(file.getPath());
                this.setupWorldCanvas();
            }

        });

        MenuItem saveItem = new MenuItem("Save");
        fileMenu.getItems().add(saveItem);
        saveItem.setOnAction(ActionEvent -> {
            Preferences preferences = Preferences.userRoot().node("life-simulation");
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open World");
            fileChooser.setInitialDirectory(new File(preferences.get("settings-directory", ".")));
            this.simulatedWorld.save(fileChooser.showSaveDialog(primaryStage).getPath());
        });

        MenuItem exitItem = new MenuItem("Exit");
        // Fancy fancy lambda expressions
        exitItem.setOnAction(actionEvent -> Platform.exit());
        fileMenu.getItems().add(exitItem);

        // VIEW MENU
        Menu viewMenu = new Menu("View");

        MenuItem debugArtifactsItem = new MenuItem("Show debugging artifacts");

        viewMenu.getItems().add(debugArtifactsItem);
        debugArtifactsItem.setOnAction(actionEvent -> {
            this.showDebuggingArtifacts = !this.showDebuggingArtifacts;

            String lbl1 = "Show debugging artifacts";
            String lbl2 = "Hide debugging artifacts";

            if(debugArtifactsItem.getText().equals(lbl1)) {
                debugArtifactsItem.setText(lbl2);
            } else {
                debugArtifactsItem.setText(lbl1);
            }

            double[][] noise = simulatedWorld.getNoise();

            for(int x = 0; x < simulatedWorld.getWidth(); x++) {
                for(int y = 0; y < simulatedWorld.getHeight(); y++) {

                    if(noise[x][y] > 0.6) {
                        Rectangle r = new Rectangle(x, y, 1, 1);
                        r.setFill(Color.DARKGREEN);
                        r.setOpacity(0.5);
                        worldCanvas.getChildren().add(r);
                    }

                    if(noise[x][y] < -0.7) {
                        Rectangle r = new Rectangle(x, y, 1, 1);
                        r.setFill(Color.DEEPSKYBLUE);
                        r.setOpacity(0.5);
                        worldCanvas.getChildren().add(r);
                    }
                }
            }
        });

        // EDIT MENU
        Menu editMenu = new Menu("Edit");

        MenuItem addNewLifeFormItem = new MenuItem("New life form");

        addNewLifeFormItem.setOnAction(actionEvent -> {
            Stage newEntityStage = new Stage();
            newEntityStage.setWidth(500);
            newEntityStage.setHeight(550);
            newEntityStage.setResizable(false);
            newEntityStage.setTitle("Entity Creation");
            newEntityStage.initModality(Modality.APPLICATION_MODAL);
            newEntityStage.initOwner(primaryStage);

            //newEntityStage.setX(primaryStage.getX() - (newEntityStage.getWidth() + 20));
            //newEntityStage.setY(primaryStage.getY());

            new EntityGenerationGUI(newEntityStage);

            newEntityStage.show();
        });

        editMenu.getItems().add(addNewLifeFormItem);

        MenuItem addNewFoodItem = new MenuItem("New food item");

        addNewFoodItem.setOnAction(actionEvent -> {
            Stage newFoodStage = new Stage();
            newFoodStage.setWidth(500);
            newFoodStage.setHeight(300);
            newFoodStage.setResizable(false);
            newFoodStage.setTitle("Food Creation");
            newFoodStage.initModality(Modality.APPLICATION_MODAL);
            newFoodStage.initOwner(primaryStage);

            new FoodGenerationGUI(newFoodStage);

            newFoodStage.show();
        });

        editMenu.getItems().add(addNewFoodItem);


        // SIMULATION MENU
        Menu simulationMenu = new Menu("Simulation");

        MenuItem pauseItem = new MenuItem("Pause");
        pauseItem.setDisable(true); // Can't pause before simulation starts

        MenuItem runItem = new MenuItem("Run");
        runItem.setOnAction(actionEvent -> {
            this.simulate = true;
            pauseItem.setDisable(false);
            runItem.setDisable(true);
            mp.play();
        });

        pauseItem.setOnAction(actionEvent -> {
            this.simulate = false;
            pauseItem.setDisable(true);
            runItem.setDisable(false);
            mp.pause();
        });

        simulationMenu.getItems().add(runItem);

        simulationMenu.getItems().add(pauseItem);

        // HELP MENU
        Menu helpMenu = new Menu("Help");

        MenuItem resetFirstRun = new MenuItem("Reset first-run flag");

        resetFirstRun.setOnAction(ActionEvent -> {
            this.preferences.putBoolean("first-run", true);
            Alert resetFirstRunAlert = new Alert(Alert.AlertType.INFORMATION);
            resetFirstRunAlert.setTitle("Flag Reset");
            resetFirstRunAlert.setHeaderText(null);
            resetFirstRunAlert.setContentText("First run flag has been reset. First run configuration will be launched when the application next opens.");
            resetFirstRunAlert.showAndWait();
        });

        helpMenu.getItems().add(resetFirstRun);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(viewMenu);
        menuBar.getMenus().add(editMenu);
        menuBar.getMenus().add(simulationMenu);
        menuBar.getMenus().add(helpMenu);
    }

    /**
     * Queue the music, DJ!
     */

    private void setupMedia() {

        String[] musicTracks = {
                "guitar-and-ukulele.mp3",
                "swinging-country.mp3",
                "country-road.mp3"
        };

        String[] christmasMusicTracks = {
                "country-jingle-bells.mp3",
                "carol-bells.mp3",
        };

        try {
            mp = new MediaPlayer(new Media(getClass().getResource("/music/" + musicTracks[new Random().nextInt(musicTracks.length)]).toURI().toString()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        Runnable changeTrack = new Runnable() {

            @Override
            public void run() {
                try {
                    mp = new MediaPlayer(new Media(getClass().getResource("/music/" + musicTracks[new Random().nextInt(musicTracks.length - 1)]).toURI().toString()));
                    mp.setOnEndOfMedia(this);

                    if(simulate) {
                        mp.play();
                    }

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
        };

        mp.setOnEndOfMedia(changeTrack);

    }

    /**
     * Sets up the pane which will be used to display all the entities in the world
     * using EntityViews.
     */

    private void setupCanvas() {
        // Setup pane to simulate world in
        this.worldCanvas = new Pane();

        // Setup some placeholder information before the world is generated
        worldCanvas.setStyle("-fx-background-color: dimgray");
        Label placeholderText = new Label("They say it all started out with a big bang.\nBut, what I wonder is, was it a big bang or did it just seem big\nbecause there wasn't anything else to drown it out at the time?\n\n- Karl Pilkington");
        placeholderText.setFont(new Font(20));
        placeholderText.setTextFill(Color.WHITE);
        placeholderText.setTextAlignment(TextAlignment.CENTER);
        placeholderText.setLayoutX(110);
        placeholderText.setLayoutY(100);
        worldCanvas.getChildren().add(placeholderText);
    }

    /**
     * Start the application
     * @param args Command line arguments
     */

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Determine whether or not the user has run the application before, and if not define some settings
     */

    private void setupPreferences() {
        // Determine if user has run the application before
        if(this.preferences.getBoolean("first-run", true)) {
            // First run of application

            // Ask the user where to save entities
            TextInputDialog settingsDirDialog = new TextInputDialog("");
            settingsDirDialog.setTitle("First Time Run Configuration");
            settingsDirDialog.setHeaderText("Simulation Settings Directory");
            settingsDirDialog.setContentText("Enter simulation data destination:");

            Optional<String> directory = settingsDirDialog.showAndWait();

            directory.ifPresent(directoryPath -> {
                this.preferences.put("settings-directory", directoryPath);
                this.preferences.putBoolean("first-run", false);
            });
        }
    }

    /**
     * Setup the entities in the world with EntityViews so that they can be displayed in the GUI.
     * This method should only be run once a new world has been initialized.
     */

    private void setupEntityViews() {

        this.views.clear();

        for(Entity e : simulatedWorld.getEntities().values()) {
            EntityView view = new EntityView(e);
            this.views.add(view);
        }

        for(EntityView v : this.views) {
            this.worldCanvas.getChildren().add(v.getSprite());
        }
    }

    /**
     * Setup the Pane used for drawing the world, in preparation for some simulations to be done.
     * Starts the simulation timer, but does not start the simulation (simulate flag is still false)
     */

    private void setupWorldCanvas() {
        this.worldCanvas.getChildren().clear();
        this.worldCanvas.setStyle("-fx-background-color: mediumspringgreen");
        this.setupEntityViews();

        this.simulate = false;
        this.simulationTimer.start();
    }

    /**
     * Update the position and display of each EntityView with respect to the entity that it represents.
     */

    private void updateEntityViews() {

        ListIterator<EntityView> viewListIterator = this.views.listIterator();

        while(viewListIterator.hasNext()) {
            EntityView v = viewListIterator.next();

            Entity e = this.simulatedWorld.getEntities().get(v.getEntity().getUuid());

            if(e == null) {
                // This entity has been removed from the world. Probably dead or eaten
                viewListIterator.remove();
                continue;
            }

            if(e.getEnergy() <= 0) {
                v.getSprite().setVisible(false);

                // Decide whether or not to respawn this entity (probably food)
                if(rng.nextInt(1000) > 998) {
                    e.setEnergy(15);
                    v.getSprite().setVisible(true);
                }

            }

            v.getSprite().setX(e.getPosition().getX());
            v.getSprite().setY(e.getPosition().getY());
        }
    }
}
