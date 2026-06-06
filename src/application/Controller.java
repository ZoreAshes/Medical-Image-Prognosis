package application;

import java.io.IOException;

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private AnchorPane welcomePane;

    @FXML
    private Label loginMessage;

    @FXML
    private TextField usernameText;

    @FXML
    private PasswordField passwordText;

    public void loginButton(ActionEvent e) throws IOException {

        String username = usernameText.getText().trim();
        String password = passwordText.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {

            loginMessage.setText("Please enter Username and Password.");

        }
        else if (username.equals("admin") && password.equals("admin")) {

            switchtoDashBoard(e);

        }
        else {

            loginMessage.setText("Wrong Username or Password!");

        }
    }

    @FXML
    public void cancelbutton(ActionEvent e) {

        System.exit(0);

    }

    @FXML
    public void switchtoDashBoard(ActionEvent event) throws IOException {

        root = FXMLLoader.load(getClass().getResource("DashBoard.fxml"));

        stage = (Stage)((Node)event.getSource()).getScene().getWindow();

        scene = new Scene(root);

        stage.setScene(scene);

        stage.centerOnScreen();

        stage.show();
    }

    @FXML
    public void showLogin(ActionEvent event) {

        TranslateTransition slide = new TranslateTransition();

        slide.setNode(welcomePane);

        slide.setDuration(Duration.seconds(0.6));

        slide.setToX(-700);

        slide.setInterpolator(Interpolator.EASE_BOTH);

        slide.play();

    }

}