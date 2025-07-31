# PR Election Simulator GUI

This project is a GUI-based election simulator designed to demonstrate a proportional representation (PR) election system. It allows for the management of political parties, configuration of election parameters, and visualization of election results. Additionally, it includes a robust user management system with face recognition for voter authentication and a new primary administrator account with enhanced control.

## Features

### 1. User Authentication and Management
- **Voter Registration & Login**: Secure registration and login for voters.
- **Face Recognition**: Biometric authentication for voters using face recognition technology.
- **Admin Accounts**: Dedicated administrator accounts for managing election parameters and user data.
- **Primary Administrator**: A special `superadmin` account with bypass for face verification, designed for initial setup and high-level management. This account can create and delete other admin accounts.

### 2. Party Management
- **Add/Edit/Delete Parties**: Administrators can easily add new political parties, modify existing ones, and remove parties from the system.
- **Party Details**: Each party can have a name, abbreviation, and an associated icon.

### 3. Election Configuration
- **Total Seats**: Configure the total number of seats available in the election.
- **Vote Allocation**: The system automatically calculates and allocates seats based on proportional representation principles.

### 4. Election Results & Analytics
- **Real-time Updates**: View election statistics, including total votes cast and seat distribution.
- **Visualizations**: Graphical representation of results (e.g., bar charts, pie charts) for easy understanding.

## Getting Started

To get a copy of the project up and running on your local machine, follow these steps:

### Prerequisites

- **Java Development Kit (JDK)**: Version 11 or higher.
- **Maven**: Build automation tool.
- **OpenCV**: Required for face recognition features. Ensure it's properly configured for your system.

### Installation and Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/afsanaakter234/Pr-election-simulator-gui.git
   cd Pr-election-simulator-gui
   ```

2. **Set Primary Admin Credentials (Optional but Recommended):**
   For enhanced security, you can set the primary admin username and password using environment variables. If these are not set, the application will default to `superadmin` for the username and `superadminpass` for the password.
   ```bash
   export PRIMARY_ADMIN_USERNAME="your_desired_admin_username"
   export PRIMARY_ADMIN_PASSWORD="your_desired_admin_password"
   ```
   Replace `your_desired_admin_username` and `your_desired_admin_password` with your chosen credentials.

3. **Build and Run the Application:**
   Navigate to the project root directory and execute the following Maven commands:
   ```bash
   mvn clean install
   mvn exec:java
   ```
   This will compile the project, download necessary dependencies, and launch the GUI application.

## Usage

1. **Login**: Upon launching the application, you will be presented with a login screen. Use the primary admin credentials (either default or those you set via environment variables) to access the Admin Dashboard.

2. **Admin Dashboard**: From the Admin Dashboard, you can:
   - **Manage Parties**: Add, edit, or delete political parties.
   - **Configure Election**: Set the total number of seats and recalculate seat distribution.
   - **View Results**: See real-time election results and analytics.
   - **Manage Users**: Add new admin accounts (with face verification) and delete existing admin accounts.

3. **Voter Registration (with Face Verification)**: When creating new admin accounts or regular voter accounts, the system will prompt for face capture for biometric verification.

## Project Structure

- `src/main/java/com/election/simulator/`: Contains the core Java source code.
  - `MainGUI.java`: The main entry point for the GUI application.
  - `auth/`: Handles user authentication, including `AuthService.java` and `FaceRecognitionService.java`.
  - `model/`: Defines data models such as `Voter`, `Party`, `Election`, and `Vote`.
  - `service/`: Contains business logic, including `ElectionService.java`.
  - `admin/`: Includes `AdminDashboardGUI.java` for administrator functionalities.
- `pom.xml`: Maven project configuration file, managing dependencies and build processes.
- `src/main/resources/styles.css`: CSS file for styling the GUI.

## Contributing

Contributions are welcome! Please feel free to fork the repository, create a new branch, and submit pull requests.

## License

This project is licensed under the MIT License - see the `LICENSE` file for details.

## Contact

For any questions or inquiries, please contact [nasrin04090](https://github.com/nasrin04090).


