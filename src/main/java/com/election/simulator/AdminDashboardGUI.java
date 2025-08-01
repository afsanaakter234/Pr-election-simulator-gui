package com.election.simulator;

import com.election.simulator.auth.AuthService;
import com.election.simulator.model.Party;
import com.election.simulator.model.Voter;
import com.election.simulator.service.ElectionService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class AdminDashboardGUI extends Application {
    private AuthService authService;
    private ElectionService electionService;
    private Stage primaryStage;
    private TableView<Party> partyTable;
    private ObservableList<Party> partyData;
    private TextField totalSeatsField;
    private Label totalVotesLabel;
    private Label totalSeatsLabel;
    private VBox resultsContainer;

    public AdminDashboardGUI() {
        this.authService = new AuthService();
        this.electionService = new ElectionService();
        this.partyData = FXCollections.observableArrayList();
    }

    public AdminDashboardGUI(AuthService authService, ElectionService electionService) {
        this.authService = authService;
        this.electionService = electionService;
        this.partyData = FXCollections.observableArrayList();
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("PR Election Simulator - Admin Dashboard");
        primaryStage.setMaximized(true);

        // Check admin access
        if (authService.getCurrentVoter() == null || !authService.getCurrentVoter().isAdmin()) {
            showAlert("Access Denied", "Only administrators can access this dashboard.", Alert.AlertType.ERROR);
            primaryStage.close();
            return;
        }

        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        // Header
        VBox header = createHeader();
        mainLayout.setTop(header);

        // Main content with tabs
        TabPane tabPane = createTabPane();
        mainLayout.setCenter(tabPane);

        Scene scene = new Scene(mainLayout, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/styles.css") != null ? 
            getClass().getResource("/styles.css").toExternalForm() : "");
        
        primaryStage.setScene(scene);
        primaryStage.show();

        // Load initial data
        loadPartyData();
        updateElectionStats();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.getStyleClass().add("header");
        header.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Admin Dashboard");
        titleLabel.getStyleClass().add("header-title");

        Label userLabel = new Label("Welcome, " + authService.getCurrentVoter().getFullName());
        userLabel.getStyleClass().add("header-subtitle");

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().addAll("button", "logout-button");
        logoutButton.setOnAction(e -> {
            authService.logout();
            primaryStage.close();
        });

        HBox headerContent = new HBox(20);
        headerContent.setAlignment(Pos.CENTER);
        headerContent.getChildren().addAll(titleLabel, userLabel);

        HBox headerWithLogout = new HBox();
        headerWithLogout.setAlignment(Pos.CENTER_RIGHT);
        headerWithLogout.getChildren().add(logoutButton);

        header.getChildren().addAll(headerContent, headerWithLogout);
        return header;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Party Management Tab
        Tab partyTab = new Tab("Party Management");
        partyTab.setContent(createPartyManagementPane());

        // Election Configuration Tab
        Tab configTab = new Tab("Election Configuration");
        configTab.setContent(createElectionConfigPane());

        // Results & Analytics Tab
        Tab resultsTab = new Tab("Results & Analytics");
        resultsTab.setContent(createResultsPane());

        // User Management Tab
        Tab userTab = new Tab("User Management");
        userTab.setContent(createUserManagementPane());

        tabPane.getTabs().addAll(partyTab, configTab, resultsTab, userTab);
        return tabPane;
    }

    private VBox createPartyManagementPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("container");

        // Title
        Label title = new Label("Political Party Management");
        title.getStyleClass().add("title-label");

        // Controls
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        Button addButton = new Button("Add Party");
        addButton.getStyleClass().addAll("button", "success-button");
        addButton.setOnAction(e -> showAddPartyDialog());

        Button editButton = new Button("Edit Party");
        editButton.getStyleClass().addAll("button", "primary-button");
        editButton.setOnAction(e -> editSelectedParty());

        Button deleteButton = new Button("Delete Party");
        deleteButton.getStyleClass().addAll("button", "danger-button");
        deleteButton.setOnAction(e -> deleteSelectedParty());

        controls.getChildren().addAll(addButton, editButton, deleteButton);

        // Party table
        partyTable = createPartyTable();

        pane.getChildren().addAll(title, controls, partyTable);
        return pane;
    }

    private TableView<Party> createPartyTable() {
        TableView<Party> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        // Icon column
        TableColumn<Party, String> iconCol = new TableColumn<>("Icon");
        iconCol.setCellValueFactory(new PropertyValueFactory<>("iconPath"));
        iconCol.setCellFactory(col -> new TableCell<Party, String>() {
            private final ImageView imageView = new ImageView();
            
            @Override
            protected void updateItem(String iconPath, boolean empty) {
                super.updateItem(iconPath, empty);
                if (empty || iconPath == null || iconPath.isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        Image image = new Image("file:" + iconPath, 32, 32, true, true);
                        imageView.setImage(image);
                        setGraphic(imageView);
                    } catch (Exception e) {
                        // Default icon if image fails to load
                        Label defaultIcon = new Label("🏛️");
                        defaultIcon.setFont(Font.font(24));
                        setGraphic(defaultIcon);
                    }
                }
            }
        });
        iconCol.setPrefWidth(80);

        // Name column
        TableColumn<Party, String> nameCol = new TableColumn<>("Party Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        // Abbreviation column
        TableColumn<Party, String> abbrCol = new TableColumn<>("Abbreviation");
        abbrCol.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));
        abbrCol.setPrefWidth(120);

        // Votes column
        TableColumn<Party, Integer> votesCol = new TableColumn<>("Votes");
        votesCol.setCellValueFactory(new PropertyValueFactory<>("votes"));
        votesCol.setPrefWidth(100);

        // Seats column
        TableColumn<Party, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        seatsCol.setPrefWidth(100);

        table.getColumns().addAll(iconCol, nameCol, abbrCol, votesCol, seatsCol);
        table.setItems(partyData);
        table.setPrefHeight(400);

        return table;
    }

    private VBox createElectionConfigPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("container");

        // Title
        Label title = new Label("Election Configuration");
        title.getStyleClass().add("title-label");

        // Total seats configuration
        VBox seatsConfig = new VBox(10);
        seatsConfig.getStyleClass().add("card");

        Label seatsLabel = new Label("Total Number of Seats:");
        seatsLabel.getStyleClass().add("section-label");

        HBox seatsInput = new HBox(10);
        seatsInput.setAlignment(Pos.CENTER_LEFT);

        totalSeatsField = new TextField();
        totalSeatsField.setText(String.valueOf(electionService.getCurrentElection().getTotalSeats()));
        totalSeatsField.setPrefWidth(100);

        Button updateSeatsButton = new Button("Update Seats");
        updateSeatsButton.getStyleClass().addAll("button", "primary-button");
        updateSeatsButton.setOnAction(e -> updateTotalSeats());

        seatsInput.getChildren().addAll(totalSeatsField, updateSeatsButton);
        seatsConfig.getChildren().addAll(seatsLabel, seatsInput);

        // Election statistics
        VBox statsBox = createElectionStatsBox();

        pane.getChildren().addAll(title, seatsConfig, statsBox);
        return pane;
    }

    private VBox createElectionStatsBox() {
        VBox statsBox = new VBox(10);
        statsBox.getStyleClass().add("stats-card");

        Label statsTitle = new Label("Election Statistics");
        statsTitle.getStyleClass().add("section-label");

        totalVotesLabel = new Label("Total Votes Cast: 0");
        totalVotesLabel.getStyleClass().add("stats-label");

        totalSeatsLabel = new Label("Total Seats: 0");
        totalSeatsLabel.getStyleClass().add("stats-label");

        Button recalculateButton = new Button("Recalculate Seats");
        recalculateButton.getStyleClass().addAll("button", "warning-button");
        recalculateButton.setOnAction(e -> {
            electionService.calculateAndAllocateSeats();
            loadPartyData();
            updateElectionStats();
            showAlert("Success", "Seats have been recalculated based on current votes.", Alert.AlertType.INFORMATION);
        });

        statsBox.getChildren().addAll(statsTitle, totalVotesLabel, totalSeatsLabel, recalculateButton);
        return statsBox;
    }

    private VBox createResultsPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("container");

        // Title
        Label title = new Label("Election Results & Analytics");
        title.getStyleClass().add("title-label");

        // Results container
        resultsContainer = new VBox(20);
        ScrollPane scrollPane = new ScrollPane(resultsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);

        Button refreshButton = new Button("Refresh Results");
        refreshButton.getStyleClass().addAll("button", "success-button");
        refreshButton.setOnAction(e -> updateResultsDisplay());

        pane.getChildren().addAll(title, refreshButton, scrollPane);
        
        // Initial load
        updateResultsDisplay();
        
        return pane;
    }

    private VBox createUserManagementPane() {
        VBox pane = new VBox(20);
        pane.getStyleClass().add("container");

        Label title = new Label("Voter Management");
        title.getStyleClass().add("title-label");

        // Voter table
        TableView<Voter> voterTable = new TableView<>();

        TableColumn<Voter, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Voter, String> fullNameCol = new TableColumn<>("Full Name");
        fullNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));

        TableColumn<Voter, String> nationalIdCol = new TableColumn<>("National ID");
        nationalIdCol.setCellValueFactory(new PropertyValueFactory<>("nationalId"));

        TableColumn<Voter, Boolean> adminCol = new TableColumn<>("Admin");
        adminCol.setCellValueFactory(new PropertyValueFactory<>("admin"));

        voterTable.getColumns().addAll(usernameCol, fullNameCol, nationalIdCol, adminCol);
        voterTable.setItems(FXCollections.observableArrayList(authService.getAllVoters()));

        HBox adminControls = new HBox(10);
        adminControls.setAlignment(Pos.CENTER_LEFT);

        Button addAdminButton = new Button("Add New Admin");
        addAdminButton.getStyleClass().addAll("button", "success-button");
        addAdminButton.setOnAction(e -> showAddAdminDialog());

        Button deleteAdminButton = new Button("Delete Admin");
        deleteAdminButton.getStyleClass().addAll("button", "danger-button");
        deleteAdminButton.setOnAction(e -> deleteSelectedAdmin(voterTable));

        adminControls.getChildren().addAll(addAdminButton, deleteAdminButton);

        pane.getChildren().addAll(title, adminControls, voterTable);
        return pane;
    }

    private void showAddPartyDialog() {
        Dialog<Party> dialog = new Dialog<>();
        dialog.setTitle("Add New Party");
        dialog.setHeaderText("Enter party details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Party Name");
        TextField abbrField = new TextField();
        abbrField.setPromptText("Abbreviation");
        
        Label iconLabel = new Label("No icon selected");
        Button iconButton = new Button("Select Icon");
        final String[] iconPath = {""};
        
        iconButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Party Icon");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                iconPath[0] = selectedFile.getAbsolutePath();
                iconLabel.setText(selectedFile.getName());
            }
        });

        grid.add(new Label("Party Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Abbreviation:"), 0, 1);
        grid.add(abbrField, 1, 1);
        grid.add(new Label("Icon:"), 0, 2);
        grid.add(iconButton, 1, 2);
        grid.add(iconLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (!nameField.getText().trim().isEmpty() && !abbrField.getText().trim().isEmpty()) {
                    Party party = new Party(nameField.getText().trim(), abbrField.getText().trim());
                    if (!iconPath[0].isEmpty()) {
                        party.setIconPath(iconPath[0]);
                    }
                    return party;
                }
            }
            return null;
        });

        Optional<Party> result = dialog.showAndWait();
        result.ifPresent(party -> {
            electionService.addParty(party);
            loadPartyData();
            showAlert("Success", "Party '" + party.getName() + "' has been added successfully.", Alert.AlertType.INFORMATION);
        });
    }

    private void editSelectedParty() {
        Party selectedParty = partyTable.getSelectionModel().getSelectedItem();
        if (selectedParty == null) {
            showAlert("No Selection", "Please select a party to edit.", Alert.AlertType.WARNING);
            return;
        }

        Dialog<Party> dialog = new Dialog<>();
        dialog.setTitle("Edit Party");
        dialog.setHeaderText("Edit party details:");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField(selectedParty.getName());
        TextField abbrField = new TextField(selectedParty.getAbbreviation());
        
        Label iconLabel = new Label(selectedParty.getIconPath() != null && !selectedParty.getIconPath().isEmpty() ? 
            new File(selectedParty.getIconPath()).getName() : "No icon selected");
        Button iconButton = new Button("Select Icon");
        final String[] iconPath = {selectedParty.getIconPath() != null ? selectedParty.getIconPath() : ""};
        
        iconButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Party Icon");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
            );
            File selectedFile = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                iconPath[0] = selectedFile.getAbsolutePath();
                iconLabel.setText(selectedFile.getName());
            }
        });

        grid.add(new Label("Party Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Abbreviation:"), 0, 1);
        grid.add(abbrField, 1, 1);
        grid.add(new Label("Icon:"), 0, 2);
        grid.add(iconButton, 1, 2);
        grid.add(iconLabel, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!nameField.getText().trim().isEmpty() && !abbrField.getText().trim().isEmpty()) {
                    selectedParty.setName(nameField.getText().trim());
                    selectedParty.setAbbreviation(abbrField.getText().trim());
                    if (!iconPath[0].isEmpty()) {
                        selectedParty.setIconPath(iconPath[0]);
                    }
                    return selectedParty;
                }
            }
            return null;
        });

        Optional<Party> result = dialog.showAndWait();
        result.ifPresent(party -> {
            loadPartyData();
            showAlert("Success", "Party '" + party.getName() + "' has been updated successfully.", Alert.AlertType.INFORMATION);
        });
    }

    private void deleteSelectedParty() {
        Party selectedParty = partyTable.getSelectionModel().getSelectedItem();
        if (selectedParty == null) {
            showAlert("No Selection", "Please select a party to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Party");
        alert.setContentText("Are you sure you want to delete the party '" + selectedParty.getName() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            electionService.removeParty(selectedParty);
            loadPartyData();
            showAlert("Success", "Party '" + selectedParty.getName() + "' has been deleted successfully.", Alert.AlertType.INFORMATION);
        }
    }

    private void showAddAdminDialog() {
        Dialog<Voter> dialog = new Dialog<>();
        dialog.setTitle("Add New Admin");
        dialog.setHeaderText("Create new admin account with face verification:");

        ButtonType addButtonType = new ButtonType("Add Admin", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");
        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("National ID");

        Label statusLabel = new Label("Face verification required");
        statusLabel.setStyle("-fx-text-fill: #e67e22;");
        Button captureFaceButton = new Button("Capture Face");
        final boolean[] faceCaptured = {false};

        captureFaceButton.setOnAction(e -> {
            // Simulate face capture process
            new Thread(() -> {
                try {
                    Thread.sleep(2000); // Simulate capture time
                    javafx.application.Platform.runLater(() -> {
                        faceCaptured[0] = true;
                        statusLabel.setText("Face captured successfully!");
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");
                    });
                } catch (InterruptedException ex) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setText("Face capture failed. Please try again.");
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    });
                }
            }).start();
        });

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Full Name:"), 0, 2);
        grid.add(fullNameField, 1, 2);
        grid.add(new Label("National ID:"), 0, 3);
        grid.add(nationalIdField, 1, 3);
        grid.add(new Label("Face Verification:"), 0, 4);
        grid.add(captureFaceButton, 1, 4);
        grid.add(statusLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (!faceCaptured[0]) {
                    showAlert("Registration Error", "Please capture face first.", Alert.AlertType.ERROR);
                    return null;
                }
                String username = usernameField.getText().trim();
                String password = passwordField.getText().trim();
                String fullName = fullNameField.getText().trim();
                String nationalId = nationalIdField.getText().trim();

                if (username.isEmpty() || password.isEmpty() || fullName.isEmpty() || nationalId.isEmpty()) {
                    showAlert("Registration Error", "Please fill all fields.", Alert.AlertType.ERROR);
                    return null;
                }
                return new Voter(username, password, fullName, nationalId, true);
            }
            return null;
        });

        Optional<Voter> result = dialog.showAndWait();
        result.ifPresent(newAdmin -> {
            boolean success = authService.registerVoter(newAdmin.getUsername(), newAdmin.getPassword(), newAdmin.getFullName(), newAdmin.getNationalId(), true);
            if (success) {
                showAlert("Success", "Admin account for '" + newAdmin.getUsername() + "' added successfully.", Alert.AlertType.INFORMATION);
                // Refresh the voter table
                TableView<Voter> voterTable = (TableView<Voter>) ((VBox) ((TabPane) primaryStage.getScene().getRoot()).getTabs().get(3).getContent()).getChildren().get(2);
                voterTable.setItems(FXCollections.observableArrayList(authService.getAllVoters()));
            } else {
                showAlert("Error", "Failed to add admin account. Username or National ID might already exist.", Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteSelectedAdmin(TableView<Voter> voterTable) {
        Voter selectedVoter = voterTable.getSelectionModel().getSelectedItem();
        if (selectedVoter == null) {
            showAlert("No Selection", "Please select an admin account to delete.", Alert.AlertType.WARNING);
            return;
        }

        if (selectedVoter.getUsername().equals("superadmin")) {
            showAlert("Deletion Error", "The primary admin account cannot be deleted.", Alert.AlertType.ERROR);
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Admin Account");
        alert.setContentText("Are you sure you want to delete the admin account for '" + selectedVoter.getUsername() + "'?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = authService.deleteVoter(selectedVoter.getUsername());
            if (success) {
                showAlert("Success", "Admin account for '" + selectedVoter.getUsername() + "' deleted successfully.", Alert.AlertType.INFORMATION);
                voterTable.setItems(FXCollections.observableArrayList(authService.getAllVoters()));
            } else {
                showAlert("Error", "Failed to delete admin account.", Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadPartyData() {
        partyData.clear();
        partyData.addAll(electionService.getParties());
    }

    private void updateElectionStats() {
        totalVotesLabel.setText("Total Votes Cast: " + electionService.getCurrentElection().getTotalVotes());
        totalSeatsLabel.setText("Total Seats: " + electionService.getCurrentElection().getTotalSeats());
    }

    private void updateTotalSeats() {
        try {
            int newSeats = Integer.parseInt(totalSeatsField.getText());
            if (newSeats > 0) {
                electionService.setTotalSeats(newSeats);
                updateElectionStats();
                showAlert("Success", "Total seats updated to " + newSeats + ".", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Invalid Input", "Total seats must be a positive number.", Alert.AlertType.WARNING);
            }
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number for total seats.", Alert.AlertType.ERROR);
        }
    }

    private void updateResultsDisplay() {
        resultsContainer.getChildren().clear();
        electionService.calculateAndAllocateSeats();

        // Pie Chart for Votes
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        electionService.getParties().forEach(party -> {
            pieChartData.add(new PieChart.Data(party.getName() + " (" + party.getVotes() + ")", party.getVotes()));
        });
        PieChart pieChart = new PieChart(pieChartData);
        pieChart.setTitle("Votes Distribution");
        resultsContainer.getChildren().add(pieChart);

        // Bar Chart for Seats
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Seat Allocation");
        xAxis.setLabel("Party");
        yAxis.setLabel("Seats");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Allocated Seats");
        electionService.getParties().forEach(party -> {
            series.getData().add(new XYChart.Data<>(party.getName(), party.getSeats()));
        });
        barChart.getData().add(series);
        resultsContainer.getChildren().add(barChart);

        // Results Table
        TableView<Party> resultsTable = new TableView<>();
        TableColumn<Party, String> nameCol = new TableColumn<>("Party Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Party, Integer> votesCol = new TableColumn<>("Votes");
        votesCol.setCellValueFactory(new PropertyValueFactory<>("votes"));
        TableColumn<Party, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seats"));
        resultsTable.getColumns().addAll(nameCol, votesCol, seatsCol);
        resultsTable.setItems(FXCollections.observableArrayList(electionService.getParties()));
        resultsTable.setPrefHeight(200);
        resultsContainer.getChildren().add(resultsTable);
    }
}

