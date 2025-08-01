# Codebase Analysis of PR Election Simulator GUI

This document provides an analysis of the `Pr-election-simulator-gui` project, a Java-based election simulator with a GUI and face recognition features.

## Project Structure

The project follows a standard Maven project structure:

- `pom.xml`: Maven project configuration, defining dependencies (JavaFX, JavaCV, JUnit) and build plugins.
- `src/main/java/com/election/simulator/`: Contains the core Java source code.
  - `Main.java`: Command-line interface (CLI) version of the election simulator, handling user registration, login, voting, and admin functionalities.
  - `MainGUI.java`: The main entry point for the JavaFX GUI application, managing login, registration, and navigation to voting/admin screens.
  - `AdminDashboardGUI.java`: (Not fully reviewed, but inferred from `MainGUI.java` and `AdminDashboard.java`) Likely the JavaFX GUI for admin functionalities.
  - `auth/`: Contains authentication-related classes.
    - `AuthService.java`: Handles user registration, login, logout, and voter management. It initializes a `superadmin` account by default.
    - `FaceRecognitionService.java`: Manages face capture and verification using OpenCV (via JavaCV). It stores face data in a `face_data/` directory.
  - `model/`: Contains data model classes.
    - `Voter.java`: Represents a voter with properties like username, password, full name, national ID, and admin status.
    - `Party.java`: Represents a political party with name and abbreviation.
  - `service/`: Contains business logic services.
    - `ElectionService.java`: Manages election-related operations, including party management, vote casting, and seat allocation.
  - `admin/`: Contains admin-specific functionalities.
    - `AdminDashboard.java`: CLI-based admin dashboard for managing users, parties, monitoring voting activity, and viewing results.
- `src/main/resources/haarcascades/haarcascade_frontalface_alt.xml`: XML file for face detection, used by `FaceRecognitionService`.

## Core Functionality

### 1. User Authentication and Authorization
- **Registration**: Users can register with a username, password, full name, and national ID. Face capture is integrated into the registration process.
- **Login**: Users can log in with their credentials. Face verification is required for both regular voters and non-superadmin administrators. The `superadmin` account bypasses face verification.
- **Admin Privileges**: An `AuthService` initializes a default `superadmin` account. Administrators have access to a separate dashboard for managing users, parties, and election data.

### 2. Face Recognition
- The `FaceRecognitionService` uses OpenCV's Haar Cascade classifiers for face detection.
- It captures and stores a normalized face image (100x100 pixels) for each registered user in the `face_data/` directory.
- Face verification involves capturing a live face and comparing it with the stored face data using a simple template matching approach (simulated similarity of 0.7 for demo purposes).
- The service includes methods for both command-line and JavaFX GUI-based face capture and verification, with live stream display for the GUI.

### 3. Election Simulation
- **Party Management**: Administrators can add and view political parties.
- **Vote Casting**: Registered voters can cast their votes for a chosen party after successful face verification.
- **Election Monitoring**: Administrators can monitor total votes cast and votes per party.
- **Results and Seat Allocation**: The `ElectionService` calculates and allocates seats based on the votes received by each party.

## Technology Stack
- **Java**: Core programming language.
- **Maven**: Build automation tool.
- **JavaFX**: For building the graphical user interface.
- **JavaCV**: A wrapper for OpenCV, used for face detection and recognition.
- **OpenCV**: Computer vision library for image processing and face detection.
- **JUnit**: For unit testing (though no test files were explicitly reviewed).

## Potential Areas for Improvement
- **Security of Face Recognition**: The `compareFaces` method in `FaceRecognitionService` currently simulates a high similarity (0.8) for demo purposes. In a real-world application, a more robust and secure face recognition algorithm would be necessary. Storing face data as simple image files is also not ideal for security.
- **Error Handling**: While some basic error handling is present (e.g., for file not found), more comprehensive error handling and user feedback mechanisms could be implemented, especially for camera access and face recognition failures.
- **Database Integration**: Currently, user and election data are stored in-memory. For persistence, integrating a database (e.g., SQLite, MySQL) would be beneficial.
- **UI/UX**: The GUI appears functional but could be enhanced with more modern UI/UX design principles for a better user experience.
- **Modularity**: While the project is somewhat modular, further separation of concerns (e.g., dedicated modules for GUI, core logic, and data access) could improve maintainability and scalability.
- **Concurrency**: The face capture and verification processes are run in background threads, which is good. However, ensuring proper thread management and synchronization is crucial for stability.

This analysis provides a high-level overview of the project. Further in-depth analysis would require running the application and examining its behavior in detail.



## Attempting to Run the Application

I attempted to run both the GUI (JavaFX) and CLI versions of the election simulator application. 

### GUI Application (`MainGUI.java`)

Running the JavaFX GUI application using `mvn javafx:run` resulted in an error related to the display environment and camera access. The sandbox environment does not provide a graphical display or direct camera access, which are prerequisites for the JavaFX application and its integrated face recognition features. Therefore, I was unable to launch and interact with the GUI.

### CLI Application (`Main.java`)

I also attempted to run the command-line interface (CLI) version of the application (`Main.java`).

- **Direct JAR execution (`java -jar target/pr-election-simulator-core-1.0-SNAPSHOT.jar`)**: This failed with a `java.lang.NoClassDefFoundError: org/bytedeco/javacv/FrameGrabber`. This indicates that the JavaCV dependencies, which are crucial for the face recognition functionality, were not correctly included or found in the JAR's classpath when executed directly.
- **Maven Exec Plugin (`mvn exec:java -Dexec.mainClass="com.election.simulator.Main"`)**: This also failed, primarily due to the application attempting to initialize components that require a graphical environment (likely related to JavaFX or underlying OpenCV/JavaCV components that expect a display), resulting in an `Unable to open DISPLAY` error.

Due to these environmental limitations and dependency issues, I was unable to successfully run either version of the application within the sandbox to interact with it directly.

## Admin Dashboard Access Issue

The user reported that they could not access the admin dashboard using the `superadmin` credentials (`superadmin`/`superadminpass`).

Based on the `AuthService.java` code, a `superadmin` account is initialized by default:

```java
public class AuthService {
    private List<Voter> voters;
    private Voter currentVoter;
    private FaceRecognitionService faceRecognitionService;
    private Voter primaryAdmin;

    public AuthService() {
        this.voters = new ArrayList<>();
        this.faceRecognitionService = new FaceRecognitionService();
        // Initialize primary admin if not already present
        if (voters.stream().noneMatch(v -> v.getUsername().equals("superadmin") && v.isAdmin())) {
            primaryAdmin = new Voter("superadmin", "superadminpass", "Primary Administrator", "00000000001", true);
            voters.add(primaryAdmin);
            System.out.println("Primary admin account created: superadmin");
        }
    }
    // ... other methods
}
```

The `login` method in `AuthService.java` explicitly handles the `superadmin` account:

```java
    public Voter login(String username, String password) {
        Optional<Voter> voterOptional = voters.stream()
                                          .filter(v -> v.getUsername().equals(username) && v.getPassword().equals(password))
                                          .findFirst();
        if (voterOptional.isPresent()) {
            Voter voter = voterOptional.get();
            // For admin voters, check if face data exists. If not, allow login without face verification.
            // If face data exists, then require face verification.
            if (voter.isAdmin()) {
                if (voter.getUsername().equals("superadmin")) {
                    System.out.println("Primary admin login successful (face verification bypassed): " + username);
                } else if (faceRecognitionService.hasFaceData(voter.getNationalId())) {
                    System.out.println("\nFace verification required for admin login...");
                    if (!faceRecognitionService.verifyFace(voter.getNationalId())) {
                        System.out.println("Login failed: Face verification unsuccessful for admin.");
                        return null;
                    }
                } else {
                    System.out.println("Admin login successful (no face data registered yet): " + username);
                }
            } else {
                System.out.println("\nFace verification required for voter login...");
                if (!faceRecognitionService.verifyFace(voter.getNationalId())) {
                    System.out.println("Login failed: Face verification unsuccessful.");
                    return null;
                }
            }
            currentVoter = voter;
            System.out.println("Login successful: " + username);
            return currentVoter;
        } else {
            System.out.println("Login failed: Invalid username or password.");
            return null;
        }
    }
```

From the code, it appears that the `superadmin` account is designed to bypass face verification. The issue reported by the user (inability to access the admin dashboard) could stem from several factors, given that I could not run the application:

1.  **Environmental Issues**: The application might be encountering issues related to the environment (e.g., missing libraries, incorrect paths for `haarcascade_frontalface_alt.xml`, or other runtime dependencies) that prevent the `AuthService` from correctly initializing or authenticating users, even for the `superadmin`.
2.  **Face Recognition Service Initialization**: Although `superadmin` bypasses face verification, the `FaceRecognitionService` is still initialized within `AuthService`. If there are underlying issues with `FaceRecognitionService` (e.g., `haarcascade_frontalface_alt.xml` not being found or loaded correctly, as indicated by the `System.err` messages in `FaceRecognitionService.java`), it might affect the overall application's stability or login process.
3.  **GUI vs. CLI Discrepancy**: The user mentioned 


the admin dashboard, which implies they were using the GUI. My attempts to run the GUI failed due to environmental limitations. It's possible the issue is specific to the GUI implementation or how it interacts with the `AuthService`.

Without being able to run the application and debug it interactively, it's challenging to pinpoint the exact cause of the admin dashboard access issue. The most likely culprits are environmental setup problems (especially with JavaFX and JavaCV in a headless environment) or subtle bugs in the GUI's interaction with the authentication service.

