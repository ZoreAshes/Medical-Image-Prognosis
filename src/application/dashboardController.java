package application;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.application.Platform;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.chart.*;

public class dashboardController implements Initializable {

    private static final String API_BASE_URL = "http://127.0.0.1:5000/api";
    private static final String API_PREDICT_URL = API_BASE_URL + "/predict";
    private static final Duration API_TIMEOUT = Duration.ofSeconds(20);
    private static final String IMAGE_STORAGE_DIR = "data/images";
    private static final boolean USE_DATABASE = true;

    @FXML
    private AnchorPane main_form;

    @FXML
    private Button close;

    @FXML
    private Button minimize;

    @FXML
    private Label username;

    @FXML
    private Button home_btn;

    @FXML
    private Button addPatient_btn;

    @FXML
    private Button logout_btn;

    @FXML
    private AnchorPane home_form;

    @FXML
    private Label home_totalPatients;

    @FXML
    private Label home_totalAdmitted;

    @FXML
    private StackPane home_chart;

    @FXML
    private ImageView home_predictionImage;

    @FXML
    private Canvas home_predictionHeatmap;

    @FXML
    private Label home_predictionLabel;

    @FXML
    private Button home_importBtn;

    @FXML
    private Button home_addPatientBtn;

    @FXML
    private AnchorPane addPatient_form;

    @FXML
    private TableView<patientData> addPatient_tableView;

    @FXML
    private TableColumn<patientData, String> addPatient_col_status;

    @FXML
    private TableColumn<patientData, String> addPatient_col_patientID;

    @FXML
    private TableColumn<patientData, String> addPatient_col_name;

    @FXML
    private TableColumn<patientData, Integer> addPatient_col_age;

    @FXML
    private TableColumn<patientData, String> addPatient_col_gender;

    @FXML
    private TableColumn<patientData, String> addPatient_col_phoneNumber;

    @FXML
    private TableColumn<patientData, String> addPatient_col_bloodGroup;

    @FXML
    private TableColumn<patientData, LocalDate> addPatient_col_date;

    @FXML
    private TextField addPatient_search;

    @FXML
    private TextField addPatient_patientID;

    @FXML
    private TextField addPatient_name;

    @FXML
    private ComboBox<String> addPatient_status;

    @FXML
    private TextField addPatient_age;

    @FXML
    private ComboBox<String> addPatient_gender;

    @FXML
    private TextField addPatient_phoneNumber;

    @FXML
    private ComboBox<String> addPatient_bloodGroup;

    @FXML
    private ImageView addPatient_image;

    @FXML
    private Button addPatient_importBtn;

    @FXML
    private Button addPatient_addBtn;

    @FXML
    private Button addPatient_updateBtn;

    @FXML
    private Button addPatient_deleteBtn;

    @FXML
    private Button addPatient_clearBtn;

    @FXML
    private Label home_totalInactivePa;

    @FXML
    private PieChart patientStatusPieChart;

    private Image image;
    private String lastPrediction;
    private String lastConfidence;
    private boolean hasPrognosisColumn;
    private boolean demoMode;
    private final ObservableList<patientData> testPatientList = FXCollections.observableArrayList();
    private String homeStoredImagePath;

    @FXML
    private Button salary_btn;

    @FXML
    private AnchorPane salary_form;

    @FXML
    private TableView<patientData> patientDetails_tableView;

    @FXML
    private TableColumn<patientData, Integer> patientDetails_col_patientID;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_name;

    @FXML
    private TableColumn<patientData, Integer> patientDetails_col_age;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_bloodGroup;

    @FXML
    private TableColumn<patientData, String> patientDetails_col_prognosis;

    @FXML
    private TextField patientDetails_patientID;

    @FXML
    private Label patientDetails_name;

    @FXML
    private Label patientDetails_age;

    @FXML
    private Label patientDetails_bloodGroup;

    @FXML
    private TextField patientDetails_prognosis;

    @FXML
    private ImageView home_xrayPreview;

   public void homePatientTotalPatients() {
        updateHomeCounts();
    }

    public void homeTotalInactive() {
        updateHomeCounts();
    }

    public void homeChart() {

        resetPredictionDisplay();

    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Message");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void addPatientAdd() {

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        try {

            if (addPatient_patientID.getText().isEmpty()
                    || addPatient_name.getText().isEmpty()
                    || addPatient_age.getText().isEmpty()
                    || addPatient_gender.getSelectionModel().getSelectedItem() == null
                    || addPatient_phoneNumber.getText().isEmpty()
                    || addPatient_bloodGroup.getSelectionModel().getSelectedItem() == null
                    || addPatient_status.getSelectionModel().getSelectedItem() == null
                    || getData.path == null || getData.path.equals("")) {

                showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
                return;
            }

            int patientId = Integer.parseInt(addPatient_patientID.getText());
            int age = Integer.parseInt(addPatient_age.getText());

            String prognosisValue = buildPrognosisValue();
            if (!USE_DATABASE) {
                int existingIndex = findTestPatientIndex(patientId);
                if (existingIndex >= 0) {
                    showAlert(Alert.AlertType.ERROR, "Patient ID already exists!");
                    return;
                }

                patientData patient = new patientData(
                        patientId,
                        addPatient_name.getText(),
                        age,
                        addPatient_gender.getValue().toString(),
                        addPatient_phoneNumber.getText(),
                        addPatient_bloodGroup.getValue().toString(),
                        getData.path,
                        date,
                        addPatient_status.getValue().toString(),
                        prognosisValue
                );
                testPatientList.add(patient);
            } else {
                String checkSql = "SELECT patientID FROM patient WHERE patientID = ?";
                String insertSql = "INSERT INTO patient "
                        + "(patientID, name, age, gender, phoneNumber, bloodGroup, image, admissionDate, status) "
                        + "VALUES(?,?,?,?,?,?,?,?,?)";
                String insertSqlWithPrognosis = "INSERT INTO patient "
                        + "(patientID, name, age, gender, phoneNumber, bloodGroup, image, admissionDate, status, prognosis) "
                        + "VALUES(?,?,?,?,?,?,?,?,?,?)";

                try (Connection connect = database.connectDb()) {
                    if (connect == null) {
                        showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                        return;
                    }

                    try (PreparedStatement checkStmt = connect.prepareStatement(checkSql)) {
                        checkStmt.setInt(1, patientId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (rs.next()) {
                                showAlert(Alert.AlertType.ERROR, "Patient ID already exists!");
                                return;
                            }
                        }
                    }

                    String sql = hasPrognosisColumn ? insertSqlWithPrognosis : insertSql;
                    try (PreparedStatement insertStmt = connect.prepareStatement(sql)) {
                        insertStmt.setInt(1, patientId);
                        insertStmt.setString(2, addPatient_name.getText());
                        insertStmt.setInt(3, age);
                        insertStmt.setString(4, addPatient_gender.getValue().toString());
                        insertStmt.setString(5, addPatient_phoneNumber.getText());
                        insertStmt.setString(6, addPatient_bloodGroup.getValue().toString());
                        insertStmt.setString(7, getData.path);
                        insertStmt.setDate(8, sqlDate);
                        insertStmt.setString(9, addPatient_status.getValue().toString());
                        if (hasPrognosisColumn) {
                            insertStmt.setString(10, prognosisValue);
                        }
                        insertStmt.executeUpdate();
                    }
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Patient Added Successfully!");

            addPatientShowListData();
            patientDetailsShowListData();
            loadPatientGraph();
            updateHomeCounts();
            addPatientReset();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to add patient: " + e.getMessage());
        }
    }


    public void addPatientUpdate() {

        LocalDate date = LocalDate.now();
        java.sql.Date sqlDate = java.sql.Date.valueOf(date);

        // String updateSql = "UPDATE patient SET "
        //         + "name = ?, "
        //         + "age = ?, "
        //         + "gender = ?, "
        //         + "phoneNumber = ?, "
        //         + "bloodGroup = ?, "
        //         + "image = ?, "
        //         + "admissionDate = ?, "
        //         + "status = ? "
        //         + "WHERE patientID = ?";
        // String updateSqlWithPrognosis = "UPDATE patient SET "
        //         + "name = ?, "
        //         + "age = ?, "
        //         + "gender = ?, "
        //         + "phoneNumber = ?, "
        //         + "bloodGroup = ?, "
        //         + "image = ?, "
        //         + "admissionDate = ?, "
        //         + "status = ?, "
        //         + "prognosis = ? "
        //         + "WHERE patientID = ?";
        //
        // connect = database.connectDb();

        try {

            if (addPatient_patientID.getText().isEmpty()
                    || addPatient_name.getText().isEmpty()
                    || addPatient_age.getText().isEmpty()
                    || addPatient_gender.getSelectionModel().getSelectedItem() == null
                    || addPatient_phoneNumber.getText().isEmpty()
                    || addPatient_bloodGroup.getSelectionModel().getSelectedItem() == null
                    || addPatient_status.getSelectionModel().getSelectedItem() == null
                    || getData.path == null || getData.path.equals(""))  {

                showAlert(Alert.AlertType.ERROR, "Please fill all blank fields");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to UPDATE Patient ID: "
                    + addPatient_patientID.getText() + "?");

            Optional<ButtonType> option = confirm.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {

                int patientId = Integer.parseInt(addPatient_patientID.getText());
                int age = Integer.parseInt(addPatient_age.getText());
                String prognosisValue = buildPrognosisValue();

                if (!USE_DATABASE) {
                    int existingIndex = findTestPatientIndex(patientId);
                    if (existingIndex < 0) {
                        showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                        return;
                    }

                    patientData patient = new patientData(
                            patientId,
                            addPatient_name.getText(),
                            age,
                            addPatient_gender.getValue().toString(),
                            addPatient_phoneNumber.getText(),
                            addPatient_bloodGroup.getValue().toString(),
                            getData.path,
                            date,
                            addPatient_status.getValue().toString(),
                            prognosisValue
                    );
                    testPatientList.set(existingIndex, patient);
                } else {
                    String updateSql = "UPDATE patient SET "
                            + "name = ?, "
                            + "age = ?, "
                            + "gender = ?, "
                            + "phoneNumber = ?, "
                            + "bloodGroup = ?, "
                            + "image = ?, "
                            + "admissionDate = ?, "
                            + "status = ? "
                            + "WHERE patientID = ?";
                    String updateSqlWithPrognosis = "UPDATE patient SET "
                            + "name = ?, "
                            + "age = ?, "
                            + "gender = ?, "
                            + "phoneNumber = ?, "
                            + "bloodGroup = ?, "
                            + "image = ?, "
                            + "admissionDate = ?, "
                            + "status = ?, "
                            + "prognosis = ? "
                            + "WHERE patientID = ?";

                    String sql = hasPrognosisColumn ? updateSqlWithPrognosis : updateSql;
                    try (Connection connect = database.connectDb()) {
                        if (connect == null) {
                            showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                            return;
                        }
                        try (PreparedStatement stmt = connect.prepareStatement(sql)) {
                            stmt.setString(1, addPatient_name.getText());
                            stmt.setInt(2, age);
                            stmt.setString(3, addPatient_gender.getValue().toString());
                            stmt.setString(4, addPatient_phoneNumber.getText());
                            stmt.setString(5, addPatient_bloodGroup.getValue().toString());
                            stmt.setString(6, getData.path);
                            stmt.setDate(7, sqlDate);
                            stmt.setString(8, addPatient_status.getValue().toString());
                            if (hasPrognosisColumn) {
                                stmt.setString(9, prognosisValue);
                                stmt.setInt(10, patientId);
                            } else {
                                stmt.setInt(9, patientId);
                            }
                            int updated = stmt.executeUpdate();
                            if (updated == 0) {
                                showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                                return;
                            }
                        }
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Patient Updated Successfully!");

                addPatientShowListData();
                patientDetailsShowListData();
                loadPatientGraph();
                updateHomeCounts();
                addPatientReset();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID and Age must be numbers.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update patient: " + e.getMessage());
        }
    }
    public void addPatientDelete() {

        // String deleteSql = "DELETE FROM patient WHERE patientID = ?";
        //
        // connect = database.connectDb();

        try {

            if (addPatient_patientID.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Please enter Patient ID to delete.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation");
            confirm.setHeaderText(null);
            confirm.setContentText("Are you sure you want to DELETE Patient ID: "
                    + addPatient_patientID.getText() + "?");

            Optional<ButtonType> option = confirm.showAndWait();

            if (option.isPresent() && option.get() == ButtonType.OK) {

                int patientId = Integer.parseInt(addPatient_patientID.getText());

                if (!USE_DATABASE) {
                    int existingIndex = findTestPatientIndex(patientId);
                    if (existingIndex < 0) {
                        showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                        return;
                    }
                    testPatientList.remove(existingIndex);
                } else {
                    String deleteSql = "DELETE FROM patient WHERE patientID = ?";
                    try (Connection connect = database.connectDb()) {
                        if (connect == null) {
                            showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                            return;
                        }
                        try (PreparedStatement stmt = connect.prepareStatement(deleteSql)) {
                            stmt.setInt(1, patientId);
                            int deleted = stmt.executeUpdate();
                            if (deleted == 0) {
                                showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                                return;
                            }
                        }
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Patient Deleted Successfully!");

                addPatientShowListData();
                loadPatientGraph();
                updateHomeCounts();
                addPatientReset();
            }

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addPatientReset() {

        addPatient_patientID.setText("");
        addPatient_name.setText("");
        addPatient_age.setText("");
        addPatient_gender.getSelectionModel().clearSelection();
        addPatient_bloodGroup.getSelectionModel().clearSelection();
        addPatient_phoneNumber.setText("");
        addPatient_image.setImage(null);
        addPatient_status.getSelectionModel().clearSelection();

        getData.path = "";
        lastPrediction = null;
        lastConfidence = null;
    }

    public void addPatientInsertImage() {

        FileChooser open = new FileChooser();

        open.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {
            try {
                getData.path = storeImage(file);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Failed to store image: " + e.getMessage());
                return;
            }

            String uri = file.toURI().toString();

            Image addImage = new Image(uri, 101, 127, false, true);
            addPatient_image.setImage(addImage);

            Image homeImage = new Image(uri, 224, 190, false, true);
            home_xrayPreview.setImage(homeImage);

            requestPredictionAsync(file);
        }
    }

    private void requestPredictionAsync(File file) {
        Thread worker = new Thread(() -> {
            try {
                String response = requestPrediction(file);
                String prediction = extractJsonValue(response, "prediction");
                String confidence = extractJsonValue(response, "confidence");
                lastPrediction = prediction;
                lastConfidence = confidence;
                String message = formatPredictionMessage(response);
                Platform.runLater(() -> {
                    if (prediction != null && patientDetails_prognosis != null) {
                        patientDetails_prognosis.setText(buildPrognosisValue());
                    }
                    updatePredictionHeatmap(file, prediction, confidence);
                    showAlert(Alert.AlertType.INFORMATION, message);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR,
                        "Prediction request failed: " + e.getMessage()));
            }
        });
        worker.setDaemon(true);
        worker.start();
    }

    private String requestPrediction(File file) throws Exception {
        String boundary = "----JavaFXBoundary" + UUID.randomUUID();
        byte[] body = buildMultipartBody(file, boundary);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_PREDICT_URL))
                .timeout(API_TIMEOUT)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(API_TIMEOUT)
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return "Prediction failed: HTTP " + response.statusCode() + " - " + response.body();
        }

        return response.body();
    }

    private byte[] buildMultipartBody(File file, String boundary) throws Exception {
        String lineBreak = "\r\n";
        StringBuilder header = new StringBuilder();
        header.append("--").append(boundary).append(lineBreak);
        header.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(file.getName()).append("\"").append(lineBreak);
        header.append("Content-Type: ").append(guessContentType(file.getName())).append(lineBreak);
        header.append(lineBreak);

        byte[] headerBytes = header.toString().getBytes(StandardCharsets.UTF_8);
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] footerBytes = (lineBreak + "--" + boundary + "--" + lineBreak)
                .getBytes(StandardCharsets.UTF_8);

        byte[] body = new byte[headerBytes.length + fileBytes.length + footerBytes.length];
        System.arraycopy(headerBytes, 0, body, 0, headerBytes.length);
        System.arraycopy(fileBytes, 0, body, headerBytes.length, fileBytes.length);
        System.arraycopy(footerBytes, 0, body, headerBytes.length + fileBytes.length, footerBytes.length);
        return body;
    }

    private String guessContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        return "application/octet-stream";
    }

    private String formatPredictionMessage(String json) {
        String prediction = extractJsonValue(json, "prediction");
        String confidence = extractJsonValue(json, "confidence");
        if (prediction == null) {
            return "Prediction response: " + json;
        }
        if (confidence == null) {
            return "Prediction: " + prediction;
        }
        return "Prediction: " + prediction + " (confidence " + confidence + ")";
    }

    private String extractJsonValue(String json, String key) {
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"?([^\",}]+)\"?");
        Matcher matcher = pattern.matcher(json);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String buildPrognosisValue() {
        if (lastPrediction == null || lastPrediction.isEmpty()) {
            return "Pending";
        }
        if (lastConfidence == null || lastConfidence.isEmpty()) {
            return lastPrediction;
        }
        return lastPrediction + " (" + lastConfidence + ")";
    }

    private void resetPredictionDisplay() {
        if (home_predictionImage != null) {
            home_predictionImage.setImage(null);
        }
        if (home_predictionHeatmap != null) {
            GraphicsContext gc = home_predictionHeatmap.getGraphicsContext2D();
            gc.clearRect(0, 0, home_predictionHeatmap.getWidth(), home_predictionHeatmap.getHeight());
        }
        if (home_predictionLabel != null) {
            home_predictionLabel.setText("Import an X-ray to view prediction heatmap");
        }
    }

    private void updatePredictionHeatmap(File file, String prediction, String confidence) {
        if (home_predictionImage == null || home_predictionHeatmap == null || home_predictionLabel == null) {
            return;
        }

        double width = home_predictionHeatmap.getWidth();
        double height = home_predictionHeatmap.getHeight();

        Image image = new Image(file.toURI().toString(), width, height, false, true);
        home_predictionImage.setImage(image);

        double intensity = parseConfidence(confidence);
        Color baseColor = "PNEUMONIA".equalsIgnoreCase(prediction) ? Color.ORANGERED : Color.LIMEGREEN;

        GraphicsContext gc = home_predictionHeatmap.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);
        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        drawHeatSpot(gc, width * 0.5, height * 0.45, Math.min(width, height) * 0.35, baseColor, intensity * 0.55);
        drawHeatSpot(gc, width * 0.7, height * 0.35, Math.min(width, height) * 0.25, baseColor, intensity * 0.4);
        drawHeatSpot(gc, width * 0.35, height * 0.65, Math.min(width, height) * 0.3, baseColor, intensity * 0.45);

        String labelText = prediction == null ? "Prediction unavailable" : "Prediction: " + prediction;
        if (confidence != null && !confidence.isEmpty()) {
            labelText = labelText + " (confidence " + confidence + ")";
        }
        home_predictionLabel.setText(labelText);
        home_predictionLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.35); -fx-padding: 6 10 6 10; -fx-background-radius: 6;");
    }

    private void drawHeatSpot(GraphicsContext gc, double centerX, double centerY, double radius, Color baseColor, double alpha) {
        double clampedAlpha = Math.max(0.05, Math.min(alpha, 0.65));
        RadialGradient gradient = new RadialGradient(
                0,
                0,
                centerX,
                centerY,
                radius,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), clampedAlpha)),
                new Stop(1, Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0))
        );
        gc.setFill(gradient);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
    }

    private double parseConfidence(String confidence) {
        if (confidence == null || confidence.isEmpty()) {
            return 0.4;
        }
        try {
            return Math.max(0.2, Math.min(Double.parseDouble(confidence), 1.0));
        } catch (NumberFormatException e) {
            return 0.4;
        }
    }

    @FXML
    private void homeImportImage() {

        FileChooser open = new FileChooser();

        open.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = open.showOpenDialog(main_form.getScene().getWindow());

        if (file != null) {

            String uri = file.toURI().toString();

            Image image = new Image(uri, 224, 190, false, true);

            home_xrayPreview.setImage(image);

            Image addImage = new Image(uri, 101, 127, false, true);
            addPatient_image.setImage(addImage);

            requestPredictionAsync(file);
        }
    }

    @FXML
    private void homeAddPatientFromImport() {
        if (homeStoredImagePath == null || homeStoredImagePath.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please import an X-ray first.");
            return;
        }

        getData.path = homeStoredImagePath;
        String resolved = resolveImagePath(homeStoredImagePath);
        if (resolved != null && addPatient_image != null) {
            Image image = new Image("file:" + resolved, 101, 127, false, true);
            addPatient_image.setImage(image);
        }

        home_form.setVisible(false);
        addPatient_form.setVisible(true);
        salary_form.setVisible(false);

        addPatient_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
        home_btn.setStyle("-fx-background-color:transparent");
        salary_btn.setStyle("-fx-background-color:transparent");
    }

    private final String[] b_groupList = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};

    public void addPatientBloodGroupList() {
        ObservableList<String> listData = FXCollections.observableArrayList(b_groupList);
        addPatient_bloodGroup.setItems(listData);
    }

    private String[] listGender = {"Male", "Female"};

    public void addPatientGenderList() {
        List<String> listG = new ArrayList<>();

        for (String data : listGender) {
            listG.add(data);
        }

        ObservableList listData = FXCollections.observableArrayList(listG);
        addPatient_gender.setItems(listData);
    }

    public void addPatientStatusList() {

        if (addPatient_status == null) {
            System.out.println("Status ComboBox not injected!");
            return;
        }

        if (!addPatient_status.getItems().isEmpty()) {
            return;
        }

        ObservableList<String> listData = FXCollections.observableArrayList(
                "Active",
                "Inactive"
        );

        addPatient_status.setItems(listData);

    }

    public void addPatientSearch() {
        FilteredList<patientData> filter = new FilteredList<>(addPatientList, e -> true);

        addPatient_search.textProperty().addListener((observable, oldValue, newValue) -> {

            filter.setPredicate(predicatePatientData -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String searchKey = newValue.toLowerCase();

                if (predicatePatientData.getPatientId().toString().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getName().toLowerCase().contains(searchKey)) {
                    return true;
                }else if (predicatePatientData.getStatus().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (Integer.toString(predicatePatientData.getAge()).contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getGender().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getPhoneNum().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getBloodGroup().toLowerCase().contains(searchKey)) {
                    return true;
                } else if (predicatePatientData.getAdmissionDate().toString().contains(searchKey)) {
                    return true;
                } else {
                    return false;
                }
            });
        });

        SortedList<patientData> sortList = new SortedList<>(filter);
        sortList.comparatorProperty().bind(addPatient_tableView.comparatorProperty());
        addPatient_tableView.setItems(sortList);
    }
    private Connection connect;
    private Statement statement;
    private PreparedStatement prepare;
    private ResultSet result;
    public void prepareLinkedInDemoState(File xrayImage) {
        demoMode = true;
        testPatientList.clear();

        String imagePath = xrayImage != null ? xrayImage.getAbsolutePath() : "";
        testPatientList.add(new patientData(
                1001,
                "Alex Morgan",
                34,
                "Male",
                "555-0101",
                "O+",
                imagePath,
                LocalDate.now(),
                "Active",
                "NORMAL (0.92)"
        ));
        testPatientList.add(new patientData(
                1002,
                "Sarah Lee",
                28,
                "Female",
                "555-0102",
                "A+",
                "",
                LocalDate.now().minusDays(2),
                "Active",
                "PNEUMONIA (0.87)"
        ));
        testPatientList.add(new patientData(
                1003,
                "James Carter",
                45,
                "Male",
                "555-0103",
                "B+",
                "",
                LocalDate.now().minusDays(5),
                "Inactive",
                "Pending"
        ));

        addPatientShowListData();
        patientDetailsShowListData();
        loadPatientGraph();
        homePatientTotalPatients();
        homeTotalInactive();

        if (xrayImage != null && xrayImage.isFile()) {
            getData.path = xrayImage.getAbsolutePath();
            String uri = xrayImage.toURI().toString();
            Image preview = new Image(uri, 224, 190, false, true);
            home_xrayPreview.setImage(preview);
            addPatient_image.setImage(new Image(uri, 101, 127, false, true));
            lastPrediction = "NORMAL";
            lastConfidence = "0.92";
            updatePredictionHeatmap(xrayImage, lastPrediction, lastConfidence);
            patientDetails_prognosis.setText(buildPrognosisValue());
        }

        addPatient_patientID.setText("1001");
        addPatient_name.setText("Alex Morgan");
        addPatient_age.setText("34");
        addPatient_phoneNumber.setText("555-0101");
        if (addPatient_gender.getItems().isEmpty()) {
            addPatientGenderList();
        }
        if (addPatient_bloodGroup.getItems().isEmpty()) {
            addPatientBloodGroupList();
        }
        if (addPatient_status.getItems().isEmpty()) {
            addPatientStatusList();
        }
        addPatient_gender.setValue("Male");
        addPatient_bloodGroup.setValue("O+");
        addPatient_status.setValue("Active");

        patientDetails_tableView.getSelectionModel().selectFirst();
        patientDetailsSelect();
    }

    public void showHomePanel() {
        home_form.setVisible(true);
        addPatient_form.setVisible(false);
        salary_form.setVisible(false);
        home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
        addPatient_btn.setStyle("-fx-background-color:transparent");
        salary_btn.setStyle("-fx-background-color:transparent");
    }

    public void showAddPatientPanel() {
        home_form.setVisible(false);
        addPatient_form.setVisible(true);
        salary_form.setVisible(false);
        addPatient_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
        home_btn.setStyle("-fx-background-color:transparent");
        salary_btn.setStyle("-fx-background-color:transparent");
    }

    public void showPatientDetailsPanel() {
        home_form.setVisible(false);
        addPatient_form.setVisible(false);
        salary_form.setVisible(true);
        salary_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
        home_btn.setStyle("-fx-background-color:transparent");
        addPatient_btn.setStyle("-fx-background-color:transparent");
        patientDetailsShowListData();
    }

    public ObservableList<patientData> addPatientListData() {

        if (demoMode || !USE_DATABASE) {
            return testPatientList;
        }

        ObservableList<patientData> listData = FXCollections.observableArrayList();
        String sql = "SELECT * FROM patient";

        try (Connection connect = database.connectDb();
             PreparedStatement prepare = connect != null ? connect.prepareStatement(sql) : null;
             ResultSet result = prepare != null ? prepare.executeQuery() : null) {

            if (connect == null || prepare == null || result == null) {
                showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                return listData;
            }

            while (result.next()) {

                patientData patient = new patientData(
                        result.getInt("patientID"),
                        result.getString("name"),
                        result.getInt("age"),
                        result.getString("gender"),
                        result.getString("phoneNumber"),
                        result.getString("bloodGroup"),
                        result.getString("image"),
                        result.getDate("admissionDate").toLocalDate(),
                        result.getString("status"),
                        getOptionalString(result, "prognosis")
                );

                listData.add(patient);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listData;
    }
    private ObservableList<patientData> addPatientList;

    public void addPatientShowListData() {
        addPatientList = addPatientListData();

        addPatient_col_patientID.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        addPatient_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        addPatient_col_age.setCellValueFactory(new PropertyValueFactory<>("age"));
        addPatient_col_gender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        addPatient_col_phoneNumber.setCellValueFactory(new PropertyValueFactory<>("phoneNum"));
        addPatient_col_bloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        addPatient_col_status.setCellValueFactory(new PropertyValueFactory<>("status"));
        addPatient_col_date.setCellValueFactory(new PropertyValueFactory<>("admissionDate"));

        addPatient_tableView.setItems(addPatientList);
    }

    public void addPatientSelect() {
        patientData selectedPatient = addPatient_tableView.getSelectionModel().getSelectedItem();
        int selectedIndex = addPatient_tableView.getSelectionModel().getSelectedIndex();

        if (selectedPatient == null || selectedIndex < 0) {
            return;
        }

        addPatient_patientID.setText(String.valueOf(selectedPatient.getPatientId()));
        addPatient_name.setText(selectedPatient.getName());
        addPatient_status.setValue(String.valueOf(selectedPatient.getStatus()));
        addPatient_age.setText(String.valueOf(selectedPatient.getAge()));
        addPatient_phoneNumber.setText(selectedPatient.getPhoneNum());
        addPatient_gender.setValue(selectedPatient.getGender());
        addPatient_bloodGroup.setValue(selectedPatient.getBloodGroup());

        getData.path = selectedPatient.getImage();
        String resolvedPath = resolveImagePath(selectedPatient.getImage());
        if (resolvedPath == null) {
            addPatient_image.setImage(null);
        } else {
            String uri = "file:" + resolvedPath;
            Image image = new Image(uri, 101, 127, false, true);
            addPatient_image.setImage(image);
        }
    }

    public void defaultNav() {
        home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
    }

    public void switchForm(ActionEvent event) {

        if (event.getSource() == home_btn) {

            home_form.setVisible(true);
            addPatient_form.setVisible(false);
            salary_form.setVisible(false);

            home_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            addPatient_btn.setStyle("-fx-background-color:transparent");
            salary_btn.setStyle("-fx-background-color:transparent");

            homePatientTotalPatients();
            homeTotalInactive();
            homeChart();
            loadPatientGraph();

        }
        else if (event.getSource() == addPatient_btn || event.getSource() == home_addPatientBtn) {

            home_form.setVisible(false);
            addPatient_form.setVisible(true);
            salary_form.setVisible(false);

            addPatient_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            home_btn.setStyle("-fx-background-color:transparent");
            salary_btn.setStyle("-fx-background-color:transparent");

        }
        else if (event.getSource() == salary_btn) {

            home_form.setVisible(false);
            addPatient_form.setVisible(false);
            salary_form.setVisible(true);

            salary_btn.setStyle("-fx-background-color:linear-gradient(to bottom right, #3a4368, #28966c);");
            home_btn.setStyle("-fx-background-color:transparent");
            addPatient_btn.setStyle("-fx-background-color:transparent");

            patientDetailsShowListData();
        }
    }

    private double x = 0;
    private double y = 0;

    public void logout() {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Message");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to logout?");
        Optional<ButtonType> option = alert.showAndWait();
        try {
            if (option.get().equals(ButtonType.OK)) {

                logout_btn.getScene().getWindow().hide();
                Parent root = FXMLLoader.load(getClass().getResource("/application/Login.fxml"));
                Stage stage = new Stage();
                Scene scene = new Scene(root);

                root.setOnMousePressed((MouseEvent event) -> {
                    x = event.getSceneX();
                    y = event.getSceneY();
                });

                root.setOnMouseDragged((MouseEvent event) -> {
                    stage.setX(event.getScreenX() - x);
                    stage.setY(event.getScreenY() - y);

                    stage.setOpacity(.8);
                });

                root.setOnMouseReleased((MouseEvent event) -> {
                    stage.setOpacity(1);
                });

                stage.initStyle(StageStyle.TRANSPARENT);

                stage.setScene(scene);
                stage.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void close() {
        System.exit(0);
    }

    public void minimize() {
        Stage stage = (Stage) main_form.getScene().getWindow();
        stage.setIconified(true);
    }

    public void patientDetailsShowListData() {
        ObservableList<patientData> listData = patientDetailsListData();

        patientDetails_col_patientID.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        patientDetails_col_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        patientDetails_col_age.setCellValueFactory(new PropertyValueFactory<>("age"));
        patientDetails_col_bloodGroup.setCellValueFactory(new PropertyValueFactory<>("bloodGroup"));
        patientDetails_col_prognosis.setCellValueFactory(new PropertyValueFactory<>("prognosis"));

        patientDetails_tableView.setItems(listData);
    }

    public ObservableList<patientData> patientDetailsListData() {
        if (demoMode || !USE_DATABASE) {
            return testPatientList;
        }

        return addPatientListData();
    }

    public void patientDetailsSelect() {
        patientData selectedPatient = patientDetails_tableView.getSelectionModel().getSelectedItem();
        int selectedIndex = patientDetails_tableView.getSelectionModel().getSelectedIndex();

        if (selectedPatient == null || selectedIndex < 0) {
            return;
        }

        patientDetails_patientID.setText(String.valueOf(selectedPatient.getPatientId()));
        patientDetails_name.setText(selectedPatient.getName());
        patientDetails_age.setText(String.valueOf(selectedPatient.getAge()));
        patientDetails_bloodGroup.setText(selectedPatient.getBloodGroup());
        patientDetails_prognosis.setText(selectedPatient.getPrognosis());
    }

    public void patientDetailsUpdate() {
        if (patientDetails_patientID.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please enter Patient ID.");
            return;
        }

        try {
            int patientId = Integer.parseInt(patientDetails_patientID.getText());
            String prognosisValue = patientDetails_prognosis.getText();

            if (!USE_DATABASE) {
                int existingIndex = findTestPatientIndex(patientId);
                if (existingIndex < 0) {
                    showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                    return;
                }
                patientData current = testPatientList.get(existingIndex);
                patientData updated = new patientData(
                        current.getPatientId(),
                        current.getName(),
                        current.getAge(),
                        current.getGender(),
                        current.getPhoneNum(),
                        current.getBloodGroup(),
                        current.getImage(),
                        current.getAdmissionDate(),
                        current.getStatus(),
                        prognosisValue
                );
                testPatientList.set(existingIndex, updated);
            } else {
                if (!hasPrognosisColumn) {
                    showAlert(Alert.AlertType.ERROR, "Database is missing prognosis column.");
                    return;
                }

                String sql = "UPDATE patient SET prognosis = ? WHERE patientID = ?";

                try (Connection connect = database.connectDb()) {
                    if (connect == null) {
                        showAlert(Alert.AlertType.ERROR, "Database connection failed.");
                        return;
                    }
                    try (PreparedStatement stmt = connect.prepareStatement(sql)) {
                        stmt.setString(1, prognosisValue);
                        stmt.setInt(2, patientId);
                        int updated = stmt.executeUpdate();
                        if (updated == 0) {
                            showAlert(Alert.AlertType.ERROR, "Patient ID not found.");
                            return;
                        }
                    }
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Patient prognosis updated.");
            patientDetailsShowListData();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Patient ID must be a number.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Failed to update prognosis: " + e.getMessage());
        }
    }

    public void patientDetailsReset() {
        patientDetails_patientID.setText("");
        patientDetails_name.setText("");
        patientDetails_age.setText("");
        patientDetails_bloodGroup.setText("");
        patientDetails_prognosis.setText("");
    }

    private String storeImage(File file) throws Exception {
        String filename = UUID.randomUUID().toString().replace("-", "");
        String extension = getFileExtension(file.getName());
        if (!extension.isEmpty()) {
            filename = filename + "." + extension;
        }

        Path dir = Paths.get(IMAGE_STORAGE_DIR);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1);
    }

    private String resolveImagePath(String storedPath) {
        if (storedPath == null || storedPath.isEmpty()) {
            return null;
        }
        Path path = Paths.get(storedPath);
        if (path.isAbsolute()) {
            return path.toString();
        }
        Path base = Paths.get(IMAGE_STORAGE_DIR);
        if (path.startsWith(base)) {
            return path.toAbsolutePath().toString();
        }
        return base.resolve(path).toAbsolutePath().toString();
    }

    private boolean checkColumnExists(String table, String column) {
        try (Connection connection = database.connectDb()) {
            if (connection == null) {
                return false;
            }
            try (ResultSet columns = connection.getMetaData().getColumns(null, null, table, column)) {
                return columns.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private void loadPatientGraph() {
        ObservableList<patientData> patients = addPatientListData();

        int activeCount = 0;
        int inactiveCount = 0;

        for (patientData patient : patients) {
            if (patient.getStatus() == null) {
                continue;
            }
            String status = patient.getStatus().trim().toLowerCase();
            if (status.equals("active")) {
                activeCount++;
            } else if (status.equals("inactive")) {
                inactiveCount++;
            }
        }

        int total = activeCount + inactiveCount;
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

        if (total == 0) {
            pieData.add(new PieChart.Data("No data", 1));
        } else {
            pieData.add(new PieChart.Data(formatSliceLabel("Active", activeCount, total), activeCount));
            pieData.add(new PieChart.Data(formatSliceLabel("Inactive", inactiveCount, total), inactiveCount));
        }

        patientStatusPieChart.setData(pieData);
        patientStatusPieChart.setTitle("Patient Status Distribution");
        patientStatusPieChart.setLegendVisible(true);
        patientStatusPieChart.setLabelsVisible(true);
    }

    private void updateHomeCounts() {
        ObservableList<patientData> patients = addPatientListData();

        int activeCount = 0;
        int inactiveCount = 0;

        for (patientData patient : patients) {
            if (patient.getStatus() == null) {
                continue;
            }
            String status = patient.getStatus().trim().toLowerCase();
            if (status.equals("active")) {
                activeCount++;
            } else if (status.equals("inactive")) {
                inactiveCount++;
            }
        }

        int total = activeCount + inactiveCount;
        home_totalPatients.setText(String.valueOf(total));
        home_totalAdmitted.setText(String.valueOf(activeCount));
        home_totalInactivePa.setText(String.valueOf(inactiveCount));
    }

    private String formatSliceLabel(String label, int count, int total) {
        if (total == 0) {
            return label + ": 0 (0%)";
        }
        double percentage = (count * 100.0) / total;
        return String.format("%s: %d (%.1f%%)", label, count, percentage);
    }

    private String getOptionalString(ResultSet resultSet, String column) {
        try {
            resultSet.findColumn(column);
            return resultSet.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private int findTestPatientIndex(int patientId) {
        for (int i = 0; i < testPatientList.size(); i++) {
            if (testPatientList.get(i).getPatientId() == patientId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        username.setText("Admin");
        defaultNav();

        home_form.setVisible(true);
        addPatient_form.setVisible(false);
        salary_form.setVisible(false);

        homePatientTotalPatients();
        homeTotalInactive();
        homeChart();
        loadPatientGraph();

        addPatientShowListData();
        patientDetailsShowListData();

        hasPrognosisColumn = USE_DATABASE && checkColumnExists("patient", "prognosis");

        addPatientGenderList();
        addPatientBloodGroupList();
        addPatientStatusList();

        addPatientSearch();

        main_form.setOnMousePressed(event -> {
            x = event.getSceneX();
            y = event.getSceneY();
        });

        main_form.setOnMouseDragged(event -> {

            Stage stage = (Stage) main_form.getScene().getWindow();

            stage.setX(event.getScreenX() - x);
            stage.setY(event.getScreenY() - y);
        });
    }

}
