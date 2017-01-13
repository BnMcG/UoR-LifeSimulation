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

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;
import java.util.prefs.Preferences;

/**
 * Created by Ben Magee on 22/11/2016.
 * Contact me: ben@bmagee.com
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

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.preferences = Preferences.userRoot().node("life-simulation");
        this.setupPreferences();
        this.views = new ArrayList<EntityView>();


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

        root.setCenter(worldCanvas);
        root.setTop(menuBar);

        primaryStage.setScene(scene);
        primaryStage.show();

        worldCanvas.layout();

        this.simulationTimer = new AnimationTimer() {

            @Override
            public void handle(long now) {
                if(simulate) {
                    simulatedWorld.simulateOutsideOfGrid();

                    // Sync entities
                    updateEntityViews();

                }
            }
        };
    }

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
            newWorldStage.setHeight(300);
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

        MenuItem saveAsItem = new MenuItem("Save as");
        fileMenu.getItems().add(saveAsItem);

        MenuItem exitItem = new MenuItem("Exit");
        // Fancy fancy lambda expressions
        exitItem.setOnAction(actionEvent -> Platform.exit());
        fileMenu.getItems().add(exitItem);

        // VIEW MENU
        Menu viewMenu = new Menu("View");

        MenuItem displayConfigItem = new MenuItem("Display configuration");
        viewMenu.getItems().add(displayConfigItem);

        MenuItem editConfigItem = new MenuItem("Edit configuration");

        viewMenu.getItems().add(editConfigItem);

        MenuItem displayLifeInfoItem = new MenuItem("Display life form information");
        viewMenu.getItems().add(displayLifeInfoItem);

        MenuItem displayMapInfoItem = new MenuItem("Display map information");
        viewMenu.getItems().add(displayMapInfoItem);

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

        MenuItem modifyCurrentLifeFormParamsItem = new MenuItem("Modify current life form parameters");
        editMenu.getItems().add(modifyCurrentLifeFormParamsItem);

        MenuItem removeCurrentLifeFormItem = new MenuItem("Remove current life form");
        editMenu.getItems().add(removeCurrentLifeFormItem);

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

        MenuItem resetItem = new MenuItem("Reset");
        resetItem.setDisable(true); // Can't reset before running
        simulationMenu.getItems().add(resetItem);

        MenuItem displayMapIterationItem = new MenuItem("Display map at each iteration");
        simulationMenu.getItems().add(displayMapIterationItem);

        // HELP MENU
        Menu helpMenu = new Menu("Help");

        MenuItem appInfoItem = new MenuItem("About application");
        helpMenu.getItems().add(appInfoItem);

        MenuItem authorInfoItem = new MenuItem("About author");
        helpMenu.getItems().add(authorInfoItem);

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

    public static void main(String[] args) {
        launch(args);
    }

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

    private void setupWorldCanvas() {
        this.worldCanvas.getChildren().clear();
        this.worldCanvas.setStyle("-fx-background-color: mediumspringgreen");
        this.setupEntityViews();

        this.simulate = false;
        this.simulationTimer.start();
    }

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
            }

            v.getSprite().setX(e.getPosition().getX());
            v.getSprite().setY(e.getPosition().getY());
        }
    }
}
