package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Unit tests for the Login class (Part 1). */
public class LoginTest {

    private static final String GOOD_PASS = "Ch&&sec@ke99!";
    private static final String GOOD_CELL = "+27838968976";

    private Login make(String user, String pass, String cell) {
        return new Login(user, pass, cell, "Kyle", "Smith");
    }

    @Test
    public void usernameCorrectlyFormatted() {
        assertEquals("Username successfully captured.",
                make("kyl_1", GOOD_PASS, GOOD_CELL).getUsernameMessage());
    }

    @Test
    public void usernameIncorrectlyFormatted() {
        assertEquals("Username is not correctly formatted; please ensure that your username "
                + "contains an underscore and is no more than five characters in length.",
                make("kyle!!!!!!!", GOOD_PASS, GOOD_CELL).getUsernameMessage());
    }

    @Test
    public void passwordMeetsComplexity() {
        assertEquals("Password successfully captured.",
                make("kyl_1", "Ch&&sec@ke99!", GOOD_CELL).getPasswordMessage());
    }

    @Test
    public void passwordFailsComplexity() {
        assertEquals("Password is not correctly formatted; please ensure that the password "
                + "contains at least eight characters, a capital letter, a number, "
                + "and a special character.",
                make("kyl_1", "password", GOOD_CELL).getPasswordMessage());
    }

    @Test
    public void cellPhoneCorrectlyFormatted() {
        assertEquals("Cell number successfully captured.",
                make("kyl_1", GOOD_PASS, "+27838968976").getCellPhoneMessage());
    }

    @Test
    public void cellPhoneIncorrectlyFormatted() {
        assertEquals("Cell number is incorrectly formatted or does not contain an "
                + "international code; please correct the number and try again.",
                make("kyl_1", GOOD_PASS, "08966553").getCellPhoneMessage());
    }

    @Test
    public void registerSuccess() {
        assertEquals("User has been registered successfully.",
                make("kyl_1", GOOD_PASS, GOOD_CELL).registerUser());
    }

    @Test
    public void loginSuccessful() {
        Login l = make("kyl_1", GOOD_PASS, GOOD_CELL);
        assertTrue(l.loginUser("kyl_1", GOOD_PASS));
        assertEquals("Welcome Kyle, Smith it is great to see you again.",
                l.returnLoginStatus(true));
    }

    @Test
    public void loginFailed() {
        Login l = make("kyl_1", GOOD_PASS, GOOD_CELL);
        assertFalse(l.loginUser("kyl_1", "wrong"));
        assertEquals("Username or password incorrect, please try again.",
                l.returnLoginStatus(false));
    }

    @Test
    public void usernameBooleans() {
        assertTrue(make("kyl_1", GOOD_PASS, GOOD_CELL).checkUserName());
        assertFalse(make("kyle!!!!!!!", GOOD_PASS, GOOD_CELL).checkUserName());
    }

    @Test
    public void passwordBooleans() {
        assertTrue(make("kyl_1", GOOD_PASS, GOOD_CELL).checkPasswordComplexity());
        assertFalse(make("kyl_1", "password", GOOD_CELL).checkPasswordComplexity());
    }

    @Test
    public void cellPhoneBooleans() {
        assertTrue(make("kyl_1", GOOD_PASS, "+27838968976").checkCellPhoneNumber());
        assertFalse(make("kyl_1", GOOD_PASS, "08966553").checkCellPhoneNumber());
    }
}
