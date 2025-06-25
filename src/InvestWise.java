package com.shrouk;

import com.shrouk.models.Asset;
import com.shrouk.models.BankAccount;

import java.io.*;
import java.util.*;

public class InvestWise {
    private static final String USER_DATA_FILE = "users.ser";
    private static HashMap<String, Asset.User> users = new HashMap<>();
    private static Asset.User currentUser = null;

    public static void main(String[] args) {
        loadUserData();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nWelcome to Invest Wise");
            System.out.println("1. Sign Up");
            System.out.println("2. Login");
            System.out.println("3. Add asset");
            System.out.println("4. Edit asset");
            System.out.println("5. Delete asset");
            System.out.println("6. Calculate Zakat");
            System.out.println("7. Connect Bank Account");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1 -> signUp(scanner);
                case 2 -> login(scanner);
                case 3 -> addAsset(scanner);
                case 4 -> editAsset(scanner);
                case 5 -> deleteAsset(scanner);
                case 6 -> calculateZakat(scanner);
                case 7 -> connectBankAccount(scanner);
                case 8 -> {
                    saveUserData();
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static String generateOTP() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    private static void connectBankAccount(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        if (currentUser.getBankAccount() != null) {
            System.out.println("You already have a connected bank account: " + currentUser.getBankAccount());
            System.out.print("Update it? (yes/no): ");
            if (!scanner.nextLine().equalsIgnoreCase("yes")) return;
        }

        System.out.print("Enter Bank Name: ");
        String bankName = scanner.nextLine();
        System.out.print("Enter Account Number: ");
        String accountNumber = scanner.nextLine();
        System.out.print("Enter Account Type: ");
        String accountType = scanner.nextLine();

        String otp = generateOTP();
        System.out.println("OTP: " + otp); // for testing
        System.out.print("Enter the OTP you received: ");
        if (!scanner.nextLine().equals(otp)) {
            System.out.println("Invalid OTP.");
            return;
        }

        System.out.print("Enter Account Balance: ");
        double balance = scanner.nextDouble(); scanner.nextLine();

        BankAccount bankAccount = new BankAccount(bankName, accountNumber, accountType, balance);
        currentUser.setBankAccount(bankAccount);
        saveUserData();
        System.out.println("Bank account connected successfully.");
    }

    private static void calculateZakat(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        System.out.println("Select Zakat Calculation Method:");
        System.out.println("1. Fixed Rate (2.5%)");
        System.out.println("2. Asset-Based Calculation");
        System.out.print("Choose: ");
        int method = scanner.nextInt();
        scanner.nextLine();

        IZakatStrategy strategy;
        if (method == 1) {
            strategy = new FixedRateZakatStrategy();
        } else if (method == 2) {
            strategy = new AssetBasedZakatStrategy();
        } else {
            System.out.println("Invalid choice. Using default method.");
            strategy = new FixedRateZakatStrategy();
        }

        ZakatCalculator calculator = new ZakatCalculator(strategy);
        double totalAssets = currentUser.getPortfolio().getTotalValue();

        BankAccount bank = currentUser.getBankAccount();
        if (bank != null) {
            totalAssets += bank.getBalance();
        }

        System.out.print("Do you have extra cash? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            System.out.print("Enter cash amount: ");
            totalAssets += scanner.nextDouble();
            scanner.nextLine();
        }

        double zakat = calculator.calculateZakat(totalAssets);
        double nisab = calculator.getNisab();

        System.out.println("\n--- Zakat Report ---");
        System.out.printf("Total Zakatable Assets: EGP %.2f%n", totalAssets);
        System.out.printf("Nisab Threshold: EGP %.2f%n", nisab);
        if (zakat > 0) {
            System.out.printf("Zakat Due: EGP %.2f%n", zakat);
        } else {
            System.out.println("Below Nisab, no zakat due.");
        }
    }

    private static void signUp(Scanner scanner) {
        System.out.println("\n--- Sign Up ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("Username already exists. Please choose another.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();

        Asset.User newUser = new Asset.User(username, password, email, phoneNumber);
        users.put(username, newUser);
        saveUserData();

        System.out.println("Registration successful! You can now login.");
    }

    private static void login(Scanner scanner) {
        System.out.println("\n--- Login ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Asset.User user = users.get(username);

        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            System.out.println("Login successful! Welcome, " + username + ".");
            System.out.println("Your email: " + user.getEmail());
        } else {
            System.out.println("Invalid username or password. Please try again.");
        }
    }

    private static void loadUserData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USER_DATA_FILE))) {
            users = (HashMap<String, Asset.User>) ois.readObject();
            System.out.println("User data loaded successfully.");
        } catch (FileNotFoundException e) {
            System.out.println("No existing user data found. Starting with empty database.");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading user data: " + e.getMessage());
        }
    }

    private static void saveUserData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USER_DATA_FILE))) {
            oos.writeObject(users);
            System.out.println("User data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving user data: " + e.getMessage());
        }
    }

    private static void addAsset(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first to add assets.");
            return;
        }

        System.out.println("\n--- Add Asset ---");

        System.out.println("Select asset type:");
        System.out.println("1. Stock");
        System.out.println("2. Real Estate");
        System.out.println("3. Savings");
        System.out.print("Choose: ");
        int typeChoice = scanner.nextInt();
        scanner.nextLine();

        String type;
        switch (typeChoice) {
            case 1 -> type = "Stock";
            case 2 -> type = "Real Estate";
            case 3 -> type = "Savings";
            default -> {
                System.out.println("Invalid type selected.");
                return;
            }
        }

        System.out.print("Enter asset name: ");
        String name = scanner.nextLine();

        System.out.print("Enter asset value: ");
        double value = scanner.nextDouble();
        scanner.nextLine();

        Asset asset = new Asset(type, name, value);
        currentUser.addAsset(asset);
        saveUserData();

        System.out.println("Asset added successfully!");
    }

    private static void editAsset(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        ArrayList<Asset> assets = currentUser.getAssets();
        if (assets.isEmpty()) {
            System.out.println("No assets to edit.");
            return;
        }

        System.out.println("\n--- Edit Asset ---");
        for (int i = 0; i < assets.size(); i++) {
            System.out.println((i + 1) + ". " + assets.get(i));
        }

        System.out.print("Select asset number to edit: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        if (index < 0 || index >= assets.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        System.out.print("Enter new name: ");
        String newName = scanner.nextLine();

        System.out.print("Enter new value: ");
        double newValue = scanner.nextDouble();
        scanner.nextLine();

        currentUser.editAsset(index, newName, newValue);
        saveUserData();
        System.out.println("Asset updated successfully.");
    }

    private static void deleteAsset(Scanner scanner) {
        if (currentUser == null) {
            System.out.println("Please login first.");
            return;
        }

        ArrayList<Asset> assets = currentUser.getAssets();
        if (assets.isEmpty()) {
            System.out.println("No assets to delete.");
            return;
        }

        System.out.println("\n--- Delete Asset ---");
        for (int i = 0; i < assets.size(); i++) {
            System.out.println((i + 1) + ". " + assets.get(i));
        }

        System.out.print("Select asset number to delete: ");
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        if (index < 0 || index >= assets.size()) {
            System.out.println("Invalid selection.");
            return;
        }

        currentUser.deleteAsset(index);
        saveUserData();
        System.out.println("Asset deleted successfully.");
    }
}