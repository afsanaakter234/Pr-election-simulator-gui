package com.election.simulator;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import com.election.simulator.auth.AuthService;
import com.election.simulator.auth.FaceRecognitionService;
import com.election.simulator.model.Voter;
import com.election.simulator.service.ElectionService;

public class MainGUI extends Application {
    private AuthService authService;
    private FaceRecognitionService faceService;
    private ElectionService electionService;
    private Stage primaryStage;
    private Voter currentVoter;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize services
        authService = new AuthService();
        faceService = new FaceRecognitionService();
        electionService = new ElectionService();
        

        
        primaryStage.setTitle("PR Election Simulator - Secure Voting System");
        primaryStage.setWidth(800);
        primaryStage.setHeight(600);
        
        showLoginScreen();
        primaryStage.show();
    }
    
    private void showLoginScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f0f8ff;");
        
        // Title
        Label titleLabel = new Label("PR Election Simulator");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label subtitleLabel = new Label("Secure Voting with Face Recognition");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        // Login form
        GridPane loginForm = new GridPane();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setHgap(10);
        loginForm.setVgap(15);
        loginForm.setPadding(new Insets(30));
        loginForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        Label usernameLabel = new Label("Username:");
        usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(250);
        
        Label passwordLabel = new Label("Password:");
        passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefWidth(250);
        
        Button loginButton = new Button("Login with Face Verification");
        loginButton.setPrefWidth(250);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button registerButton = new Button("Register New Voter");
        registerButton.setPrefWidth(250);
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        loginForm.add(loginButton, 0, 2, 2, 1);
        loginForm.add(registerButton, 0, 3, 2, 1);
        
        // Event handlers
        loginButton.setOnAction(e -> handleLogin(usernameField.getText(), passwordField.getText()));
        registerButton.setOnAction(e -> showRegistrationScreen());
        
        root.getChildren().addAll(titleLabel, subtitleLabel, loginForm);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
    
    private void showRegistrationScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f8ff;");
        
        Label titleLabel = new Label("Voter Registration");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        GridPane regForm = new GridPane();
        regForm.setAlignment(Pos.CENTER);
        regForm.setHgap(10);
        regForm.setVgap(15);
        regForm.setPadding(new Insets(30));
        regForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("National ID");
        
        ImageView cameraView = new ImageView();
        cameraView.setFitWidth(320);
        cameraView.setFitHeight(240);
        cameraView.setStyle("-fx-background-color: black;");
        
        Button captureButton = new Button("Capture Face");
        captureButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Button registerButton = new Button("Complete Registration");
        registerButton.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold;");
        registerButton.setDisable(true);
        
        Button backButton = new Button("Back to Login");
        backButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
        
        regForm.add(new Label("Username:"), 0, 0);
        regForm.add(usernameField, 1, 0);
        regForm.add(new Label("Password:"), 0, 1);
        regForm.add(passwordField, 1, 1);
        regForm.add(new Label("Full Name:"), 0, 2);
        regForm.add(fullNameField, 1, 2);
        regForm.add(new Label("National ID:"), 0, 3);
        regForm.add(nationalIdField, 1, 3);
        regForm.add(cameraView, 0, 4, 2, 1); // Add camera view
        regForm.add(captureButton, 0, 5, 2, 1);
        regForm.add(statusLabel, 0, 6, 2, 1);
        regForm.add(registerButton, 0, 7, 2, 1);
        regForm.add(backButton, 0, 8, 2, 1);
        
        final boolean[] faceCaptured = {false};
        
        captureButton.setOnAction(e -> {
            if (nationalIdField.getText().trim().isEmpty()) {
                statusLabel.setText("Please enter National ID first");
                return;
            }
            
            statusLabel.setText("Starting face capture...");
            // Run face capture in background thread
            new Thread(() -> {
                boolean success = faceService.captureAndStoreFaceWithStream(nationalIdField.getText().trim(), cameraView);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setText("Face captured successfully!");
                        statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                        faceCaptured[0] = true;
                        registerButton.setDisable(false);
                    } else {
                        statusLabel.setText("Face capture failed. Please try again.");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    }
                });
            }).start();
        });
        
        registerButton.setOnAction(e -> {
            if (!faceCaptured[0]) {
                statusLabel.setText("Please capture your face first");
                return;
            }
            
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String nationalId = nationalIdField.getText().trim();
            
            if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || nationalId.isEmpty()) {
                statusLabel.setText("Please fill all fields");
                return;
            }
            
            boolean success = authService.registerVoterWithFace(username, password, fullName, nationalId, false);
            if (success) {
                showAlert("Registration Successful", "Voter registered successfully! You can now login.");
                showLoginScreen();
            } else {
                statusLabel.setText("Registration failed. Username may already exist.");
            }
        });
        
        backButton.setOnAction(e -> showLoginScreen());
        
        root.getChildren().addAll(titleLabel, regForm);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
    
    private void handleLogin(String username, String password) {
        if (username.trim().isEmpty() || password.trim().isEmpty()) {
            showAlert("Login Error", "Please enter both username and password");
            return;
        }
        
        // First verify password
        Voter voter = authService.authenticateVoter(username, password);
        if (voter == null) {
            showAlert("Login Failed", "Invalid username or password");
            return;
        }

        currentVoter = voter;

        // If admin and no face data, go to admin dashboard and prompt for face registration
        if (currentVoter.isAdmin() && !faceService.hasFaceData(currentVoter.getNationalId())) {
            showAdminDashboard();
            showAlert("Admin Face Registration", "Welcome, Admin! Please register your face for future secure logins.");
            // You might want to add a specific screen or section in the admin dashboard for face registration
            // For now, we'll just show the alert.
        } else if (currentVoter.isAdmin() && faceService.hasFaceData(currentVoter.getNationalId())) {
            // Admin with face data, proceed with face verification
            showAlert("Face Verification", "Please look at the camera for face verification");

            ImageView cameraView = new ImageView();
            cameraView.setFitWidth(320);
            cameraView.setFitHeight(240);
            cameraView.setStyle("-fx-background-color: black;");

            Stage cameraStage = new Stage();
            cameraStage.setTitle("Face Verification");
            VBox cameraRoot = new VBox(cameraView);
            cameraRoot.setAlignment(Pos.CENTER);
            Scene cameraScene = new Scene(cameraRoot);
            cameraStage.setScene(cameraScene);
            cameraStage.show();

            new Thread(() -> {
                boolean faceVerified = faceService.verifyFaceWithStream(voter.getNationalId(), cameraView);
                javafx.application.Platform.runLater(() -> {
                    cameraStage.close();
                    if (faceVerified) {
                        showAdminDashboard();
                    } else {
                        showAlert("Login Failed", "Face verification failed. Access denied.");
                    }
                });
            }).start();
        } else { // Regular voter
            showAlert("Face Verification", "Please look at the camera for face verification");

            ImageView cameraView = new ImageView();
            cameraView.setFitWidth(320);
            cameraView.setFitHeight(240);
            cameraView.setStyle("-fx-background-color: black;");

            Stage cameraStage = new Stage();
            cameraStage.setTitle("Face Verification");
            VBox cameraRoot = new VBox(cameraView);
            cameraRoot.setAlignment(Pos.CENTER);
            Scene cameraScene = new Scene(cameraRoot);
            cameraStage.setScene(cameraScene);
            cameraStage.show();

            new Thread(() -> {
                boolean faceVerified = faceService.verifyFaceWithStream(voter.getNationalId(), cameraView);
                javafx.application.Platform.runLater(() -> {
                    cameraStage.close();
                    if (faceVerified) {
                        showVotingScreen();
                    } else {
                        showAlert("Login Failed", "Face verification failed. Access denied.");
                    }
                });
            }).start();
        }
    }
    
    private void showAdminDashboard() {
        try {
            AdminDashboardGUI adminDashboard = new AdminDashboardGUI(authService, electionService);
            Stage adminStage = new Stage();
            adminDashboard.start(adminStage);
            primaryStage.close(); // Close the main login window
        } catch (Exception e) {
            showAlert("Error", "Failed to open admin dashboard: " + e.getMessage());
        }
    }
    
    private void showVotingScreen() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f0f8ff;");
        
        Label welcomeLabel = new Label("Welcome, " + currentVoter.getFullName());
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        welcomeLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        Label instructionLabel = new Label("Select your preferred party and verify your identity to cast your vote");
        instructionLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        instructionLabel.setStyle("-fx-text-fill: #7f8c8d;");
        
        VBox votingPanel = new VBox(15);
        votingPanel.setAlignment(Pos.CENTER);
        votingPanel.setPadding(new Insets(30));
        votingPanel.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");
        
        ToggleGroup partyGroup = new ToggleGroup();
        
        RadioButton party1 = new RadioButton("Democratic Party");
        party1.setToggleGroup(partyGroup);
        party1.setUserData("Democratic Party");
        
        RadioButton party2 = new RadioButton("Republican Party");
        party2.setToggleGroup(partyGroup);
        party2.setUserData("Republican Party");
        
        RadioButton party3 = new RadioButton("Independent Party");
        party3.setToggleGroup(partyGroup);
        party3.setUserData("Independent Party");
        
        Button voteButton = new Button("Cast Vote with Face Verification");
        voteButton.setPrefWidth(300);
        voteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        Button logoutButton = new Button("Logout");
        logoutButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;");
        
        voteButton.setOnAction(e -> {
            RadioButton selected = (RadioButton) partyGroup.getSelectedToggle();
            if (selected == null) {
                showAlert("Vote Error", "Please select a party first");
                return;
            }
            
            String selectedParty = (String) selected.getUserData();
            
            showAlert("Face Verification", "Please look at the camera to verify your identity before casting vote");
            
            ImageView cameraView = new ImageView();
            cameraView.setFitWidth(320);
            cameraView.setFitHeight(240);
            cameraView.setStyle("-fx-background-color: black;");

            Stage cameraStage = new Stage();
            cameraStage.setTitle("Face Verification");
            VBox cameraRoot = new VBox(cameraView);
            cameraRoot.setAlignment(Pos.CENTER);
            Scene cameraScene = new Scene(cameraRoot);
            cameraStage.setScene(cameraScene);
            cameraStage.show();
            
            new Thread(() -> {
                boolean faceVerified = faceService.verifyFaceWithStream(currentVoter.getNationalId(), cameraView);
                javafx.application.Platform.runLater(() -> {
                    cameraStage.close();
                    if (faceVerified) {
                        boolean voteSuccess = electionService.castVote(currentVoter.getNationalId(), selectedParty);
                        if (voteSuccess) {
                            showAlert("Vote Cast", "Your vote for " + selectedParty + " has been recorded successfully!");
                            showLoginScreen(); // Return to login after voting
                        } else {
                            showAlert("Vote Error", "You have already voted or there was an error processing your vote.");
                        }
                    } else {
                        showAlert("Verification Failed", "Face verification failed. Vote not cast.");
                    }
                });
            }).start();
        });
        
        logoutButton.setOnAction(e -> {
            currentVoter = null;
            showLoginScreen();
        });
        
        votingPanel.getChildren().addAll(
            new Label("Choose your party:"),
            party1, party2, party3,
            voteButton
        );
        
        root.getChildren().addAll(welcomeLabel, instructionLabel, votingPanel, logoutButton);
        
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}

