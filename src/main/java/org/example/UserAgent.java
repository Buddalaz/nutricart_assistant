package org.example;

import madkit.kernel.Agent;
import madkit.message.StringMessage;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class UserAgent extends Agent {

    @Override
    protected void activate() {
        getLogger().info("UserAgent activating...");

        // Request role (group should already exist, created by CoordinatorAgent)
        madkit.kernel.AbstractAgent.ReturnCode roleResult = requestRole("grocery", "main", "user");
        getLogger().info("Request user role result: " + roleResult);

        // Wait for other agents to join
        getLogger().info("Waiting for other agents to join...");
        pause(2000);

        getLogger().info("UserAgent ready!");
    }

    @Override
    protected void live() {
        // Print menu after everything is initialized
        pause(500);
        printMenu();

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        try {
            while (true) {
                System.out.print(">> ");
                System.out.flush();

                String input = reader.readLine();

                if (input == null) {
                    continue;
                }

                input = input.trim();

                if (input.isEmpty()) {
                    continue;
                }

                if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                    System.out.println("\n👋 Goodbye! Shutting down...");
                    pause(500);
                    System.exit(0);
                    break;
                }

                if (input.equalsIgnoreCase("help") || input.equals("?")) {
                    printMenu();
                    continue;
                }

                // Send message to GroceryAgent
                madkit.kernel.AbstractAgent.ReturnCode sent = sendMessage("grocery", "main", "groceryAgent",
                        new StringMessage(input));

                if (sent != madkit.kernel.AbstractAgent.ReturnCode.SUCCESS) {
                    System.out.println("⚠ Warning: Could not send message! ReturnCode: " + sent);
                    System.out.println("   (Make sure GroceryAgent is running)");
                }

                // Give some time for the response to be processed
                pause(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printMenu() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   Smart Grocery Assistant              ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Commands:");
        System.out.println("  add <item>          - Add item to list");
        System.out.println("  exp <item> <days>   - Add item with expiry");
        System.out.println("  show                - Display grocery list");
        System.out.println("  predict             - Get suggestions");
        System.out.println("  healthy <item>      - Find healthy alternative");
        System.out.println("  help                - Show this menu");
        System.out.println("  exit                - Quit application");
        System.out.println("══════════════════════════════════════════\n");
    }

    @Override
    protected void end() {
        getLogger().info("UserAgent shutting down...");
    }
}