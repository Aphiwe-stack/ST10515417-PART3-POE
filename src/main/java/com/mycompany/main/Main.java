package com.mycompany.main;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * QuickChat console application (Parts 1, 2 and 3).
 *
 * @ST10534368
 */
public class Main {

    // Scanner for user input
    static Scanner input = new Scanner(System.in);

    // The registered user
    static Login login;

    // How many messages the user may enter, and how many have been entered so far
    static int maxMessages = 0;
    static int messagesEntered = 0;

    // Registers the user, repeating until the details are valid
    public static void register() {
        while (true) {
            System.out.print("Enter your first name: ");
            String firstName = input.nextLine().trim();
            System.out.print("Enter your last name: ");
            String lastName = input.nextLine().trim();
            System.out.print("Enter your username: ");
            String username = input.nextLine().trim();
            System.out.print("Enter your password: ");
            String password = input.nextLine();
            System.out.print("Enter your cell phone number (e.g. +27831234567): ");
            String cell = input.nextLine().trim();

            login = new Login(username, password, cell, firstName, lastName);

            // Show the message for each check
            System.out.println(login.getUsernameMessage());
            System.out.println(login.getPasswordMessage());
            System.out.println(login.getCellPhoneMessage());
            String result = login.registerUser();
            System.out.println(result);

            if (result.equals("User has been registered successfully.")) {
                break;
            }
            System.out.println("Please register again.\n");
        }
    }

    // Logs in, repeating until the details are correct
    public static void doLogin() {
        boolean loggedIn = false;
        while (!loggedIn) {
            System.out.print("Enter your username to login: ");
            String user = input.nextLine().trim();
            System.out.print("Enter your password to login: ");
            String pass = input.nextLine();
            loggedIn = login.loginUser(user, pass);
            System.out.println(login.returnLoginStatus(loggedIn));
        }
    }

    // Reads a whole number, repeating until the input is valid
    public static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(input.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    // Lets the user enter the messages they still have left
    public static void sendMessages() {
        int remaining = maxMessages - messagesEntered;
        if (remaining <= 0) {
            System.out.println("You have already entered all " + maxMessages + " messages.");
            return;
        }

        for (int i = 0; i < remaining; i++) {
            int messageNumber = messagesEntered;
            System.out.println("\n--- Message " + (messagesEntered + 1) + " of " + maxMessages + " ---");

            // Recipient
            String recipient;
            while (true) {
                System.out.print("Enter recipient cell number (with international code): ");
                recipient = input.nextLine().trim();
                String result = new Message(messageNumber, recipient, "x").checkRecipientCell();
                System.out.println(result);
                if (result.equals("Cell phone number successfully captured.")) {
                    break;
                }
            }

            // Message text
            String text;
            while (true) {
                System.out.print("Enter your message (max 250 characters): ");
                text = input.nextLine();
                if (text.trim().isEmpty()) {
                    System.out.println("Message cannot be empty.");
                } else if (text.length() > 250) {
                    System.out.println("Please enter a message of less than 250 characters.");
                    System.out.println(new Message(messageNumber, recipient, text).checkMessageLength());
                } else {
                    System.out.println("Message sent");
                    break;
                }
            }

            Message message = new Message(messageNumber, recipient, text);
            System.out.println("Message ID generated: " + message.getMessageID());

            // Send, disregard or store
            int choice;
            while (true) {
                choice = readInt("1) Send Message  2) Disregard Message  3) Store Message to send later: ");
                if (choice >= 1 && choice <= 3) {
                    break;
                }
                System.out.println("Please choose 1, 2 or 3.");
            }

            System.out.println(message.SentMessage(choice));
            if (choice == 1) {
                System.out.println(message.getDetails());
            } else if (choice == 2) {
                // Wait for 0 to delete the message
                while (readInt("") != 0) {
                    System.out.println("Press 0 to delete the message.");
                }
                System.out.println("Message deleted.");
            }
            messagesEntered++;
        }
        System.out.println("Total messages sent: " + Message.returnTotalMessages());
    }

    // Sub-menu for the stored messages (Part 3)
    public static void storedMessagesMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Stored Messages ---");
            System.out.println("1) Display sender and recipient of all stored messages");
            System.out.println("2) Display the longest stored message");
            System.out.println("3) Search for a message ID");
            System.out.println("4) Search for all messages to a recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display a report of all stored messages");
            System.out.println("7) Back to main menu");
            int option = readInt("Choose an option: ");

            switch (option) {
                case 1:
                    String list = Message.displaySendersAndRecipients();
                    System.out.println(list.isEmpty() ? "No stored messages." : list);
                    break;
                case 2:
                    String longest = Message.longestStoredMessage();
                    System.out.println(longest.isEmpty() ? "No stored messages."
                            : "Longest stored message: " + longest);
                    break;
                case 3:
                    System.out.print("Enter the message ID: ");
                    System.out.println(Message.displayByID(input.nextLine().trim()));
                    break;
                case 4:
                    System.out.print("Enter the recipient cell number: ");
                    ArrayList<String> found = Message.searchByRecipient(input.nextLine().trim());
                    if (found.isEmpty()) {
                        System.out.println("No messages found for that recipient.");
                    }
                    for (String text : found) {
                        System.out.println("\"" + text + "\"");
                    }
                    break;
                case 5:
                    System.out.print("Enter the message hash: ");
                    System.out.println(Message.deleteByHash(input.nextLine().trim().toUpperCase()));
                    break;
                case 6:
                    System.out.println(Message.storedMessagesReport());
                    break;
                case 7:
                    back = true;
                    break;
                default:
                    System.out.println("Invalid option, please choose 1 to 7.");
            }
        }
    }

    public static void main(String[] args) {
        register();
        doLogin();
        Message.setSender(login.getFullName());

        // Read previously stored messages from the JSON file into the arrays
        int loaded = Message.loadStoredMessages();
        System.out.println("\nWelcome to QuickChat.");
        if (loaded > 0) {
            System.out.println(loaded + " stored message(s) loaded from file.");
        }

        // The number of messages must be at least one
        while (maxMessages < 1) {
            maxMessages = readInt("How many messages do you wish to enter? ");
            if (maxMessages < 1) {
                System.out.println("Please enter a number greater than zero.");
            }
        }

        // Menu loop runs until the user quits
        boolean running = true;
        while (running) {
            System.out.println("\n1) Send Messages\n2) Show recently sent messages\n3) Quit\n4) Stored Messages");
            int option = readInt("Choose an option: ");
            switch (option) {
                case 1:
                    sendMessages();
                    break;
                case 2:
                    System.out.println("Coming Soon.");
                    break;
                case 3:
                    System.out.println("Goodbye.");
                    running = false;
                    break;
                case 4:
                    storedMessagesMenu();
                    break;
                default:
                    System.out.println("Invalid option, please choose 1, 2, 3 or 4.");
            }
        }
    }
}
