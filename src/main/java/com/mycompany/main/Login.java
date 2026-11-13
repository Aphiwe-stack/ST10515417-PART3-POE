package com.mycompany.main;

/**
 * Handles registration and login (Part 1).
 *
 * @ST10534368
 */
public class Login {

    // Details entered by the user when registering
    private final String username;
    private final String password;
    private final String cellPhone;
    private final String firstName;
    private final String lastName;

    // Constructor stores the registration details
    public Login(String username, String password, String cellPhone,
                 String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.cellPhone = cellPhone;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    // Username must contain an underscore and be no more than 5 characters
    public boolean checkUserName() {
        return username != null
                && username.contains("_")
                && username.length() <= 5;
    }

    // Password: at least 8 characters, a capital letter, a number, a special character
    public boolean checkPasswordComplexity() {
        return password != null
                && password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[0-9].*")
                && password.matches(".*[^a-zA-Z0-9].*");
    }

    // Cell number: international code (+ and 1-3 digits) followed by up to 10 digits.
    // Regex adapted from the Oracle Java Tutorials, "Regular Expressions",
    // https://docs.oracle.com/javase/tutorial/essential/regex/ (Oracle, 2025)
    // and the E.164 international number format.
    public boolean checkCellPhoneNumber() {
        return cellPhone != null && cellPhone.matches("^\\+[0-9]{1,3}[0-9]{1,10}$");
    }

    // Message for the username check
    public String getUsernameMessage() {
        if (checkUserName()) {
            return "Username successfully captured.";
        }
        return "Username is not correctly formatted; please ensure that your username "
                + "contains an underscore and is no more than five characters in length.";
    }

    // Message for the password check
    public String getPasswordMessage() {
        if (checkPasswordComplexity()) {
            return "Password successfully captured.";
        }
        return "Password is not correctly formatted; please ensure that the password "
                + "contains at least eight characters, a capital letter, a number, "
                + "and a special character.";
    }

    // Message for the cell phone check
    public String getCellPhoneMessage() {
        if (checkCellPhoneNumber()) {
            return "Cell number successfully captured.";
        }
        return "Cell number is incorrectly formatted or does not contain an "
                + "international code; please correct the number and try again.";
    }

    // Returns the registration message
    public String registerUser() {
        if (!checkUserName()) {
            return getUsernameMessage();
        }
        if (!checkPasswordComplexity()) {
            return getPasswordMessage();
        }
        if (!checkCellPhoneNumber()) {
            return getCellPhoneMessage();
        }
        return "User has been registered successfully.";
    }

    // Verifies the login details match the registered details
    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username.equals(enteredUsername) && password.equals(enteredPassword);
    }

    // Full name of the user, used as the message sender
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Returns the login status message
    public String returnLoginStatus(boolean loginSuccessful) {
        if (loginSuccessful) {
            return "Welcome " + firstName + ", " + lastName
                    + " it is great to see you again.";
        }
        return "Username or password incorrect, please try again.";
    }
}
