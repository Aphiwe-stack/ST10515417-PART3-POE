package com.mycompany.main;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for the Message class (Part 2). */
public class MessageTest {

    // Test data from the assignment
    private static final String[] RECIPIENTS = {"+27718693002", "08575975889"};
    private static final String[] TEXTS = {
        "Hi Mike, can you join us for dinner tonight?",
        "Hi Keegan, did you receive the payment?"
    };
    private static final String[] IDS = {"0012345678", "0187654321"};
    private static final String[] EXPECTED_HASHES = {"00:0:HITONIGHT", "01:1:HIPAYMENT"};

    @BeforeEach
    public void setUp() throws IOException {
        Message.resetState();
        // Use a temporary file so the tests never touch the real messages.json
        Message.setStorageFile(Files.createTempFile("quickchat", ".json"));
    }

    @Test
    public void messageLengthSuccess() {
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]);
        assertEquals("Message ready to send.", m.checkMessageLength());
    }

    @Test
    public void messageLengthFailure() {
        String longText = "a".repeat(260);
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], longText);
        assertEquals("Message exceeds 250 characters by 10; please reduce the size.",
                m.checkMessageLength());
    }

    @Test
    public void recipientSuccess() {
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]);
        assertEquals("Cell phone number successfully captured.", m.checkRecipientCell());
    }

    @Test
    public void recipientFailure() {
        Message m = new Message(IDS[1], 1, RECIPIENTS[1], TEXTS[1]);
        assertEquals("Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.",
                m.checkRecipientCell());
    }

    @Test
    public void messageHashFirstMessage() {
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]);
        assertEquals("00:0:HITONIGHT", m.createMessageHash());
    }

    @Test
    public void messageHashesInLoop() {
        for (int i = 0; i < TEXTS.length; i++) {
            Message m = new Message(IDS[i], i, RECIPIENTS[i], TEXTS[i]);
            assertEquals(EXPECTED_HASHES[i], m.createMessageHash());
        }
    }

    @Test
    public void messageIdIsCreated() {
        Message m = new Message(0, RECIPIENTS[0], TEXTS[0]);
        assertTrue(m.checkMessageID());
        assertEquals(10, m.getMessageID().length());
        assertTrue(m.getMessageID().matches("[0-9]{10}"));
    }

    @Test
    public void sendMessage() {
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]);
        assertEquals("Message successfully sent.", m.SentMessage(1));
    }

    @Test
    public void disregardMessage() {
        Message m = new Message(IDS[1], 1, RECIPIENTS[1], TEXTS[1]);
        assertEquals("Press 0 to delete the message.", m.SentMessage(2));
    }

    @Test
    public void storeMessage() {
        Message m = new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]);
        assertEquals("Message successfully stored.", m.SentMessage(3));
    }

    @Test
    public void totalMessagesReturned() {
        new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]).SentMessage(1);
        new Message(IDS[1], 1, RECIPIENTS[1], TEXTS[1]).SentMessage(2);
        assertEquals(1, Message.returnTotalMessages());
    }

    @Test
    public void printMessagesShowsSentOnly() {
        new Message(IDS[0], 0, RECIPIENTS[0], TEXTS[0]).SentMessage(1);
        String printed = Message.printMessages();
        assertTrue(printed.contains("Message ID: 0012345678"));
        assertTrue(printed.contains("Message Hash: 00:0:HITONIGHT"));
        assertTrue(printed.contains(TEXTS[0]));
    }
}
