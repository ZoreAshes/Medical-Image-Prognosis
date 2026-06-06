package application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javax.imageio.ImageIO;

public class DemoVideoCapture extends Application {

    private static final Path OUTPUT_DIR = Paths.get("demo-video", "frames");

    @Override
    public void start(Stage primaryStage) {
        Platform.runLater(() -> {
            try {
                Files.createDirectories(OUTPUT_DIR);
                captureLogin();
                captureDashboard();
                System.out.println("Demo capture finished.");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Platform.exit();
            }
        });
    }

    private void captureLogin() throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
        Scene scene = new Scene(root, 700, 500);
        scene.getStylesheets().add(getClass().getResource("login.css").toExternalForm());

        Stage stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(scene);
        stage.show();
        Thread.sleep(900);
        saveSnapshot(root, "01_login.png");
        stage.close();
    }

    private void captureDashboard() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/DashBoard.fxml"));
        Parent root = loader.load();
        dashboardController controller = loader.getController();

        Scene scene = new Scene(root, 1100, 600);
        scene.getStylesheets().add(getClass().getResource("dashboardDesign.css").toExternalForm());

        Stage stage = new Stage();
        stage.setScene(scene);
        stage.setTitle("Xpert Demo Capture");
        stage.show();
        Thread.sleep(1200);

        File xray = resolveSampleXray();
        controller.prepareLinkedInDemoState(xray);
        Thread.sleep(800);

        controller.showHomePanel();
        Thread.sleep(900);
        saveSnapshot(root, "02_home_xray.png");

        controller.showAddPatientPanel();
        Thread.sleep(900);
        saveSnapshot(root, "03_add_patient.png");

        controller.showPatientDetailsPanel();
        Thread.sleep(900);
        saveSnapshot(root, "04_patient_details.png");
        stage.close();
    }

    private File resolveSampleXray() {
        Path preferred = Paths.get("data", "images", "05fd131acc3d46aba230d64afbf9a044.jpg");
        if (Files.isRegularFile(preferred)) {
            return preferred.toFile();
        }
        Path imagesDir = Paths.get("data", "images");
        try {
            return Files.list(imagesDir)
                    .filter(path -> {
                        String name = path.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png");
                    })
                    .findFirst()
                    .map(Path::toFile)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private void saveSnapshot(Parent root, String filename) throws Exception {
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        WritableImage image = root.snapshot(params, null);
        Path output = OUTPUT_DIR.resolve(filename);
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output.toFile());
        System.out.println("Saved " + output.toAbsolutePath());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
