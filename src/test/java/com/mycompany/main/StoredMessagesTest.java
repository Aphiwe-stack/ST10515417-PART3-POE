package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the arrays, search, delete and report features (Part 3). */
public class StoredMessagesTest {

    // Test data from the assignment (message 4 uses the developer number)
    private static final String[] RECIPIENTS = {
        "+27834557896", "+27838884567", "+27834484567", "0838884567", "+27838884567"};
    private static final String[] TEXTS = {
        "Did you get the cake?",
        "Where are you? You are late! I have asked you to be on time.",
        "Yohoooo, I am at your gate.",
        "It is dinner time!",
        "Ok, I am leaving without you."};
    // 1 = Sent, 2 = Disregard, 3 = Stored
    private static final int[] CHOICES = {1, 3, 2, 1, 3};

    private Path jsonFile;

    // Fills the arrays with the test data by sending, storing or disregarding each message
    private void loadTestData(int count) {
        for (int i = 0; i < count; i++) {
            new Message(i, RECIPIENTS[i], TEXTS[i]).SentMessage(CHOICES[i]);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        Message.resetState();
        jsonFile = Files.createTempFile("quickchat", ".json");
        Message.setStorageFile(jsonFile);
        Message.setSender("Developer");
    }

    @Test
    public void sentMessagesArrayPopulated() {
        loadTestData(4);
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Did you get the cake?", "It is dinner time!"));
        assertEquals(expected, Message.getSentMessages());
    }

    @Test
    public void disregardedMessagesArrayPopulated() {
        loadTestData(4);
        assertEquals(new ArrayList<>(Arrays.asList("Yohoooo, I am at your gate.")),
                Message.getDisregardedMessages());
    }

    @Test
    public void storedMessagesArrayPopulated() {
        loadTestData(5);
        assertEquals(new ArrayList<>(Arrays.asList(TEXTS[1], TEXTS[4])),
                Message.getStoredMessages());
    }

    @Test
    public void hashAndIdArraysPopulated() {
        loadTestData(5);
        assertEquals(5, Message.getMessageIDs().size());
        assertEquals(5, Message.getMessageHashes().size());
        // First hash: first two ID digits, message number 0, first and last word
        String firstID = Message.getMessageIDs().get(0);
        assertEquals(firstID.substring(0, 2) + ":0:DIDCAKE",
                Message.getMessageHashes().get(0));
    }

    @Test
    public void longestMessageDisplayed() {
        loadTestData(4);
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    @Test
    public void searchByMessageID() {
        loadTestData(4);
        String idOfMessage4 = Message.getMessageIDs().get(3);
        assertEquals("0838884567", Message.searchRecipientByID(idOfMessage4));
        assertEquals("It is dinner time!", Message.searchMessageByID(idOfMessage4));
    }

    @Test
    public void searchByMessageIDNotFound() {
        loadTestData(4);
        assertNull(Message.searchMessageByID("9999999999"));
        assertEquals("No message found with ID 9999999999.", Message.displayByID("9999999999"));
    }

    @Test
    public void searchByRecipient() {
        loadTestData(5);
        ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."));
        assertEquals(expected, Message.searchByRecipient("+27838884567"));
    }

    @Test
    public void deleteMessageUsingHash() {
        loadTestData(5);
        String hashOfMessage2 = Message.getMessageHashes().get(1);
        assertEquals("Message: \"Where are you? You are late! I have asked you to be on time.\" "
                + "successfully deleted.", Message.deleteByHash(hashOfMessage2));
        // The message is gone from the arrays
        assertEquals(new ArrayList<>(Arrays.asList(TEXTS[4])), Message.getStoredMessages());
        assertEquals(4, Message.getMessageIDs().size());
    }

    @Test
    public void deleteUnknownHash() {
        loadTestData(5);
        assertEquals("No stored message found with hash NOPE.", Message.deleteByHash("NOPE"));
        assertEquals(5, Message.getMessageIDs().size());
    }

    @Test
    public void sentMessageCannotBeDeletedFromStoredMenu() {
        loadTestData(5);
        String hashOfSentMessage = Message.getMessageHashes().get(0);
        assertEquals("No stored message found with hash " + hashOfSentMessage + ".",
                Message.deleteByHash(hashOfSentMessage));
        assertEquals(5, Message.getMessageIDs().size());
        assertEquals(2, Message.returnTotalMessages());
    }

    @Test
    public void displayReportShowsSentMessages() {
        loadTestData(4);
        String hash1 = Message.getMessageHashes().get(0);
        String hash4 = Message.getMessageHashes().get(3);
        String expected = "--- Sent Messages Report ---\n"
                + "Message Hash: " + hash1 + "\nRecipient: +27834557896\n"
                + "Message: Did you get the cake?\n\n"
                + "Message Hash: " + hash4 + "\nRecipient: 0838884567\n"
                + "Message: It is dinner time!\n\n";
        assertEquals(expected, Message.sentMessagesReport());
    }

    @Test
    public void storedReportShowsFullDetails() {
        loadTestData(5);
        String report = Message.storedMessagesReport();
        assertTrue(report.contains("Sender: Developer"));
        assertTrue(report.contains("Recipient: +27838884567"));
        assertTrue(report.contains(TEXTS[1]));
        assertTrue(report.contains(TEXTS[4]));
        assertFalse(report.contains(TEXTS[0]));
    }

    @Test
    public void sendersAndRecipientsOfStored() {
        loadTestData(5);
        assertEquals("Sender: Developer | Recipient: +27838884567\n"
                + "Sender: Developer | Recipient: +27838884567\n",
                Message.displaySendersAndRecipients());
    }

    @Test
    public void storedMessagesReadBackFromJson() {
        loadTestData(5);
        ArrayList<String> idsBefore = Message.getMessageIDs();

        // Simulate restarting the program: clear memory, keep the file
        Message.resetState();
        assertEquals(2, Message.loadStoredMessages());
        assertEquals(new ArrayList<>(Arrays.asList(TEXTS[1], TEXTS[4])),
                Message.getStoredMessages());
        assertEquals(idsBefore.get(1), Message.getMessageIDs().get(0));
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                Message.longestStoredMessage());
    }

    @Test
    public void jsonHandlesQuotesInMessages() {
        new Message(0, "+27838884567", "He said \"hi\" today").SentMessage(3);
        Message.resetState();
        Message.loadStoredMessages();
        assertEquals(new ArrayList<>(Arrays.asList("He said \"hi\" today")),
                Message.getStoredMessages());
    }

    @Test
    public void deletedMessageIsRemovedFromJson() {
        loadTestData(5);
        Message.deleteByHash(Message.getMessageHashes().get(1));
        Message.resetState();
        assertEquals(1, Message.loadStoredMessages());
        assertEquals(new ArrayList<>(Arrays.asList(TEXTS[4])), Message.getStoredMessages());
    }

    @Test
    public void messageIDsAreUnique() {
        loadTestData(5);
        ArrayList<String> ids = Message.getMessageIDs();
        assertEquals(ids.size(), new java.util.HashSet<>(ids).size());
        for (String id : ids) {
            assertTrue(id.matches("[0-9]{10}"));
        }
    }
}
