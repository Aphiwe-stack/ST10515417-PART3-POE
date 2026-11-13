package com.mycompany.main;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents one chat message and holds the parallel arrays that store
 * every message (Parts 2 and 3).
 *
 * @ST10534368
 */
public class Message {

    // Flags used to record what happened to a message
    public static final String FLAG_SENT = "Sent";
    public static final String FLAG_STORED = "Stored";
    public static final String FLAG_DISREGARD = "Disregard";

    // File where stored messages are saved (can be changed for testing)
    private static Path storageFile = Paths.get("messages.json");

    // Arrays holding the text of messages in each category
    private static final ArrayList<String> sentMessages = new ArrayList<>();
    private static final ArrayList<String> disregardedMessages = new ArrayList<>();
    private static final ArrayList<String> storedMessages = new ArrayList<>();

    // Parallel arrays: index i in each list describes the same message
    private static final ArrayList<String> messageIDs = new ArrayList<>();
    private static final ArrayList<String> messageHashes = new ArrayList<>();
    private static final ArrayList<String> senders = new ArrayList<>();
    private static final ArrayList<String> recipients = new ArrayList<>();
    private static final ArrayList<String> messageTexts = new ArrayList<>();
    private static final ArrayList<String> flags = new ArrayList<>();

    // Total messages sent
    private static int totalMessages = 0;

    // Name of the logged-in user, used as the sender
    private static String currentSender = "Unknown";

    private final String messageID;
    private final int messageNumber;
    private final String recipient;
    private final String text;

    // Creates a message with an auto-generated ID
    public Message(int messageNumber, String recipient, String text) {
        this(generateMessageID(), messageNumber, recipient, text);
    }

    // Creates a message with a given ID (used for testing)
    public Message(String messageID, int messageNumber, String recipient, String text) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.text = text;
    }

    // ------------------------------------------------------------------
    // Part 2 methods
    // ------------------------------------------------------------------

    // Generates a unique random ten-digit message ID using string manipulation
    public static String generateMessageID() {
        Random random = new Random();
        String id;
        do {
            String digits = Long.toString(random.nextLong() & Long.MAX_VALUE);
            // Pad with zeros, then keep the last ten characters with substring
            id = ("0000000000" + digits).substring(digits.length());
        } while (messageIDs.contains(id));
        return id;
    }

    // Message ID must be no more than ten characters
    public boolean checkMessageID() {
        return messageID != null && messageID.length() <= 10;
    }

    // Recipient must start with an international code and be a valid length
    public String checkRecipientCell() {
        if (recipient != null && recipient.matches("^\\+[0-9]{1,3}[0-9]{1,10}$")) {
            return "Cell phone number successfully captured.";
        }
        return "Cell phone number is incorrectly formatted or does not contain "
                + "an international code. Please correct the number and try again.";
    }

    // Message must not exceed 250 characters
    public String checkMessageLength() {
        if (text.length() <= 250) {
            return "Message ready to send.";
        }
        int extra = text.length() - 250;
        return "Message exceeds 250 characters by " + extra + "; please reduce the size.";
    }

    // Hash = first two ID digits : message number : first word + last word, in capitals
    public String createMessageHash() {
        String[] words = text.trim().split("\\s+");
        String firstWord = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String lastWord = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");
        String firstTwo = messageID.substring(0, 2);
        return (firstTwo + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    // Handles the user's choice: 1 = send, 2 = disregard, 3 = store
    public String SentMessage(int choice) {
        switch (choice) {
            case 1:
                addToArrays(FLAG_SENT);
                totalMessages++;
                return "Message successfully sent.";
            case 2:
                addToArrays(FLAG_DISREGARD);
                return "Press 0 to delete the message.";
            case 3:
                storeMessage();
                return "Message successfully stored.";
            default:
                return "Invalid option.";
        }
    }

    // Returns all the messages sent while the program is running
    public static String printMessages() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < messageTexts.size(); i++) {
            if (flags.get(i).equals(FLAG_SENT)) {
                output.append(detailsAt(i)).append("\n");
            }
        }
        return output.toString();
    }

    // Returns the total number of messages sent
    public static int returnTotalMessages() {
        return totalMessages;
    }

    // Full details in the required order: ID, Hash, Recipient, Message
    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + createMessageHash()
                + "\nRecipient: " + recipient
                + "\nMessage: " + text + "\n";
    }

    // Adds this message to the stored arrays and saves everything to JSON
    public void storeMessage() {
        addToArrays(FLAG_STORED);
        saveStoredToJson();
    }

    // ------------------------------------------------------------------
    // Part 3 methods: arrays, JSON, search, delete and reports
    // ------------------------------------------------------------------

    // Adds this message to the category array and to every parallel array
    private void addToArrays(String flag) {
        messageIDs.add(messageID);
        messageHashes.add(createMessageHash());
        senders.add(currentSender);
        recipients.add(recipient);
        messageTexts.add(text);
        flags.add(flag);
        categoryList(flag).add(text);
    }

    // Returns the category array that matches a flag
    private static ArrayList<String> categoryList(String flag) {
        if (flag.equals(FLAG_SENT)) {
            return sentMessages;
        } else if (flag.equals(FLAG_STORED)) {
            return storedMessages;
        }
        return disregardedMessages;
    }

    // Formats the full details of the message at one index
    private static String detailsAt(int index) {
        return "Message ID: " + messageIDs.get(index)
                + "\nMessage Hash: " + messageHashes.get(index)
                + "\nSender: " + senders.get(index)
                + "\nRecipient: " + recipients.get(index)
                + "\nMessage: " + messageTexts.get(index) + "\n";
    }

    // Writes all stored messages to the JSON file (whole file rewritten each time).
    // The JSON layout follows the format described at https://www.json.org/
    public static void saveStoredToJson() {
        StringBuilder json = new StringBuilder("[\n");
        boolean first = true;
        for (int i = 0; i < messageTexts.size(); i++) {
            if (!flags.get(i).equals(FLAG_STORED)) {
                continue;
            }
            if (!first) {
                json.append(",\n");
            }
            first = false;
            json.append("  {\n")
                .append("    \"messageID\": \"").append(escape(messageIDs.get(i))).append("\",\n")
                .append("    \"messageHash\": \"").append(escape(messageHashes.get(i))).append("\",\n")
                .append("    \"sender\": \"").append(escape(senders.get(i))).append("\",\n")
                .append("    \"recipient\": \"").append(escape(recipients.get(i))).append("\",\n")
                .append("    \"message\": \"").append(escape(messageTexts.get(i))).append("\"\n")
                .append("  }");
        }
        json.append("\n]\n");
        try {
            Files.write(storageFile, json.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Could not write JSON file: " + e.getMessage());
        }
    }

    // Reads the JSON file into the stored arrays. Returns how many messages were loaded.
    // The file is read with java.nio.file.Files (Oracle, 2025) and each object is matched
    // with a regular expression, because the file layout is fixed by saveStoredToJson().
    public static int loadStoredMessages() {
        if (!Files.exists(storageFile)) {
            return 0;
        }
        String content;
        try {
            content = new String(Files.readAllBytes(storageFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Could not read JSON file: " + e.getMessage());
            return 0;
        }

        String field = "\"((?:[^\"\\\\]|\\\\.)*)\"";
        Pattern entry = Pattern.compile(
                "\\{\\s*\"messageID\"\\s*:\\s*" + field
                + "\\s*,\\s*\"messageHash\"\\s*:\\s*" + field
                + "\\s*,\\s*\"sender\"\\s*:\\s*" + field
                + "\\s*,\\s*\"recipient\"\\s*:\\s*" + field
                + "\\s*,\\s*\"message\"\\s*:\\s*" + field + "\\s*\\}");
        Matcher matcher = entry.matcher(content);

        int loaded = 0;
        while (matcher.find()) {
            String id = unescape(matcher.group(1));
            if (messageIDs.contains(id)) {
                continue; // already in memory
            }
            messageIDs.add(id);
            messageHashes.add(unescape(matcher.group(2)));
            senders.add(unescape(matcher.group(3)));
            recipients.add(unescape(matcher.group(4)));
            messageTexts.add(unescape(matcher.group(5)));
            flags.add(FLAG_STORED);
            storedMessages.add(unescape(matcher.group(5)));
            loaded++;
        }
        return loaded;
    }

    // Sender and recipient of every stored message
    public static String displaySendersAndRecipients() {
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < messageTexts.size(); i++) {
            if (flags.get(i).equals(FLAG_STORED)) {
                output.append("Sender: ").append(senders.get(i))
                      .append(" | Recipient: ").append(recipients.get(i)).append("\n");
            }
        }
        return output.toString();
    }

    // Longest stored message ("" if there are none)
    public static String longestStoredMessage() {
        String longest = "";
        for (String stored : storedMessages) {
            if (stored.length() > longest.length()) {
                longest = stored;
            }
        }
        return longest;
    }

    // Index of a message ID in the parallel arrays (-1 if not found)
    private static int indexOfID(String id) {
        return messageIDs.indexOf(id);
    }

    // Recipient for a message ID, or null if the ID does not exist
    public static String searchRecipientByID(String id) {
        int index = indexOfID(id);
        return index == -1 ? null : recipients.get(index);
    }

    // Message text for a message ID, or null if the ID does not exist
    public static String searchMessageByID(String id) {
        int index = indexOfID(id);
        return index == -1 ? null : messageTexts.get(index);
    }

    // Recipient and message for a message ID, ready to display
    public static String displayByID(String id) {
        int index = indexOfID(id);
        if (index == -1) {
            return "No message found with ID " + id + ".";
        }
        return "Recipient: " + recipients.get(index) + "\nMessage: " + messageTexts.get(index);
    }

    // All sent or stored messages for one recipient
    public static ArrayList<String> searchByRecipient(String recipientNumber) {
        ArrayList<String> found = new ArrayList<>();
        for (int i = 0; i < messageTexts.size(); i++) {
            boolean notDisregarded = !flags.get(i).equals(FLAG_DISREGARD);
            if (notDisregarded && recipients.get(i).equals(recipientNumber)) {
                found.add(messageTexts.get(i));
            }
        }
        return found;
    }

    // Deletes a STORED message using its hash and returns a confirmation message.
    // Only stored messages can be deleted here, so the sent total stays correct.
    public static String deleteByHash(String hash) {
        int index = -1;
        for (int i = 0; i < messageHashes.size() && index == -1; i++) {
            if (messageHashes.get(i).equals(hash) && flags.get(i).equals(FLAG_STORED)) {
                index = i;
            }
        }
        if (index == -1) {
            return "No stored message found with hash " + hash + ".";
        }
        String deletedText = messageTexts.get(index);
        String flag = flags.get(index);

        // Position of this message inside its category array
        int position = 0;
        for (int i = 0; i < index; i++) {
            if (flags.get(i).equals(flag)) {
                position++;
            }
        }
        categoryList(flag).remove(position);

        messageIDs.remove(index);
        messageHashes.remove(index);
        senders.remove(index);
        recipients.remove(index);
        messageTexts.remove(index);
        flags.remove(index);

        if (flag.equals(FLAG_STORED)) {
            saveStoredToJson();
        }
        return "Message: \"" + deletedText + "\" successfully deleted.";
    }

    // Report of all sent messages: hash, recipient and message
    public static String sentMessagesReport() {
        StringBuilder report = new StringBuilder("--- Sent Messages Report ---\n");
        for (int i = 0; i < messageTexts.size(); i++) {
            if (flags.get(i).equals(FLAG_SENT)) {
                report.append("Message Hash: ").append(messageHashes.get(i))
                      .append("\nRecipient: ").append(recipients.get(i))
                      .append("\nMessage: ").append(messageTexts.get(i)).append("\n\n");
            }
        }
        return report.toString();
    }

    // Report with the full details of all stored messages
    public static String storedMessagesReport() {
        StringBuilder report = new StringBuilder("--- Stored Messages Report ---\n");
        for (int i = 0; i < messageTexts.size(); i++) {
            if (flags.get(i).equals(FLAG_STORED)) {
                report.append(detailsAt(i)).append("\n");
            }
        }
        return report.toString();
    }

    // Escapes characters that are not allowed raw inside a JSON string
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    // Reverses escape()
    private static String unescape(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\\' && i + 1 < value.length()) {
                char next = value.charAt(++i);
                switch (next) {
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    default: result.append(next); // \\ and \"
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    // Clears all saved state (used by unit tests)
    public static void resetState() {
        sentMessages.clear();
        disregardedMessages.clear();
        storedMessages.clear();
        messageIDs.clear();
        messageHashes.clear();
        senders.clear();
        recipients.clear();
        messageTexts.clear();
        flags.clear();
        totalMessages = 0;
    }

    // Getters for the arrays (copies, so they cannot be changed from outside)
    public static ArrayList<String> getSentMessages() { return new ArrayList<>(sentMessages); }
    public static ArrayList<String> getDisregardedMessages() { return new ArrayList<>(disregardedMessages); }
    public static ArrayList<String> getStoredMessages() { return new ArrayList<>(storedMessages); }
    public static ArrayList<String> getMessageHashes() { return new ArrayList<>(messageHashes); }
    public static ArrayList<String> getMessageIDs() { return new ArrayList<>(messageIDs); }

    public static void setStorageFile(Path file) { storageFile = file; }
    public static void setSender(String name) { currentSender = name; }

    public String getMessageID() { return messageID; }
    public String getRecipient() { return recipient; }
    public String getText() { return text; }
}
