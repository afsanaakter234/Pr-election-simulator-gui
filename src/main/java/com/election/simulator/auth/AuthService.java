package com.election.simulator.auth;

import com.election.simulator.model.Voter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AuthService {
    private List<Voter> voters;
    private Voter currentVoter;
    private FaceRecognitionService faceRecognitionService;
    private boolean headlessMode = Boolean.parseBoolean(System.getenv("HEADLESS_MODE"));
    private Voter primaryAdmin;

    public AuthService() {
        this.voters = new ArrayList<>();
        if (!headlessMode) {
            this.faceRecognitionService = new FaceRecognitionService();
        }
        // Initialize primary admin if not already present
        if (voters.stream().noneMatch(v -> v.getUsername().equals("superadmin") && v.isAdmin())) {
            primaryAdmin = new Voter("superadmin", "superadminpass", "Primary Administrator", "00000000001", true);
            voters.add(primaryAdmin);
            System.out.println("Primary admin account created: superadmin");
        }
    }

    public boolean registerVoter(String username, String password, String fullName, String nationalId, boolean isAdmin) {
        // Check if username or national ID already exists
        if (voters.stream().anyMatch(v -> v.getUsername().equals(username) || v.getNationalId().equals(nationalId))) {
            System.out.println("Registration failed: Username or National ID already exists.");
            return false;
        }
        
        Voter newVoter = new Voter(username, password, fullName, nationalId, isAdmin);
        voters.add(newVoter);
        System.out.println("Voter registered successfully: " + username);
        return true;
    }

    public boolean registerVoterWithFace(String username, String password, String fullName, String nationalId, boolean isAdmin) {
        // Check if username or national ID already exists
        if (voters.stream().anyMatch(v -> v.getUsername().equals(username) || v.getNationalId().equals(nationalId))) {
            System.out.println("Registration failed: Username or National ID already exists.");
            return false;
        }
        
        if (!headlessMode) {
            System.out.println("\nFace capture required for registration...");
            if (!faceRecognitionService.captureAndStoreFace(nationalId)) {
                System.out.println("Registration failed: Face capture unsuccessful.");
                return false;
            }
        } else {
            System.out.println("Headless mode: Face capture skipped for registration.");
        }
        
        Voter newVoter = new Voter(username, password, fullName, nationalId, isAdmin);
        voters.add(newVoter);
        System.out.println("Voter registered successfully: " + username);
        return true;
    }

    public Voter authenticateVoter(String username, String password) {
        Optional<Voter> voterOptional = voters.stream()
                                          .filter(v -> v.getUsername().equals(username) && v.getPassword().equals(password))
                                          .findFirst();
        return voterOptional.orElse(null);
    }

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
                } else if (!headlessMode && faceRecognitionService.hasFaceData(voter.getNationalId())) {
                    System.out.println("\nFace verification required for admin login...");
                    if (!faceRecognitionService.verifyFace(voter.getNationalId())) {
                        System.out.println("Login failed: Face verification unsuccessful for admin.");
                        return null;
                    }
                } else if (headlessMode) {
                    System.out.println("Headless mode: Admin login successful (face verification skipped): " + username);
                } else {
                    System.out.println("Admin login successful (no face data registered yet): " + username);
                }
            } else {
                if (!headlessMode) {
                    System.out.println("\nFace verification required for voter login...");
                    if (!faceRecognitionService.verifyFace(voter.getNationalId())) {
                        System.out.println("Login failed: Face verification unsuccessful.");
                        return null;
                    }
                } else {
                    System.out.println("Headless mode: Voter login successful (face verification skipped): " + username);
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

    public void logout() {
        currentVoter = null;
        System.out.println("Voter logged out.");
    }

    public Voter getCurrentVoter() {
        return currentVoter;
    }

    public List<Voter> getAllVoters() {
        return new ArrayList<>(voters);
    }

    public boolean deleteVoter(String username) {
        Optional<Voter> voterToDelete = voters.stream()
                                          .filter(v -> v.getUsername().equals(username))
                                          .findFirst();
        if (voterToDelete.isPresent()) {
            voters.remove(voterToDelete.get());
            System.out.println("Voter " + username + " deleted successfully.");
            return true;
        } else {
            System.out.println("Voter " + username + " not found.");
            return false;
        }
    }
}

