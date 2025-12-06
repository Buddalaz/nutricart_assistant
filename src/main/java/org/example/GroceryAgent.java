package org.example;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class GroceryAgent extends Agent {
    private Map<String, LocalDate> groceryList = new HashMap<>();
    private Map<String, LocalDate> purchaseHistory = new HashMap<>();

    @Override
    protected void activate() {
        System.out.println("[GroceryAgent] Activating...");

        // Request role (group should already exist)
        madkit.kernel.AbstractAgent.ReturnCode roleResult = requestRole("grocery", "main", "groceryAgent");
        System.out.println("[GroceryAgent] Request role result: " + roleResult);

        if (roleResult == madkit.kernel.AbstractAgent.ReturnCode.SUCCESS) {
            System.out.println("[GroceryAgent] Successfully joined and ready!");
        } else {
            System.out.println("[GroceryAgent] ERROR: Failed to join! Result: " + roleResult);
        }
    }

    @Override
    protected void live() {
        try {
            while (true) {
                Message msg = waitNextMessage(1000);

                if (msg == null) continue;

                String text = ((StringMessage) msg).getContent();

                System.out.println("\n[GroceryAgent] Processing: " + text);

                if (text.startsWith("add ")) {
                    String item = text.substring(4).trim();
                    groceryList.put(item, LocalDate.now().plusDays(7));
                    purchaseHistory.put(item, LocalDate.now());
                    System.out.println("Added: " + item + " (expires in 7 days)");
                }

                else if (text.startsWith("exp ")) {
                    String[] parts = text.split(" ");
                    if (parts.length == 3) {
                        String item = parts[1].trim();
                        int days = Integer.parseInt(parts[2]);
                        groceryList.put(item, LocalDate.now().plusDays(days));
                        purchaseHistory.put(item, LocalDate.now());
                        System.out.println("Added: " + item + " (expires in " + days + " days)");
                    } else {
                        System.out.println("Usage: exp <item> <days>");
                    }
                }

                else if (text.equals("show")) {
                    System.out.println("======= Your Grocery List =======");
                    if (groceryList.isEmpty()) {
                        System.out.println("  (empty)");
                    } else {
                        groceryList.forEach((item, expiry) ->
                                System.out.println("  • " + item + " → expires: " + expiry));
                    }
                    System.out.println();
                }

                else if (text.equals("predict")) {
                    ruleBasedPrediction();
                    expiryCheck();
                }

                else if (text.startsWith("healthy ")) {
                    String item = text.substring(8).trim();
                    System.out.println("[GroceryAgent] Forwarding to RecommendationAgent: " + item);
                    sendMessage(
                            "grocery", "main", "recommendationAgent",
                            new StringMessage(item)
                    );
                }

                else {
                    System.out.println("✗ Unknown command: " + text);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ruleBasedPrediction() {
        System.out.println("======= Prediction Suggestions ======");

        boolean foundSuggestions = false;
        for (String item : purchaseHistory.keySet()) {
            LocalDate lastPurchased = purchaseHistory.get(item);
            if (lastPurchased.isBefore(LocalDate.now().minusDays(7))) {
                System.out.println("  • You bought " + item + " last week. Add again?");
                foundSuggestions = true;
            }
        }

        if (!foundSuggestions) {
            System.out.println("  (no suggestions)");
        }
        System.out.println();
    }

    private void expiryCheck() {
        System.out.println("===========Expiry Warnings===========");

        boolean foundWarnings = false;
        for (String item : groceryList.keySet()) {
            LocalDate exp = groceryList.get(item);
            if (exp.isBefore(LocalDate.now().plusDays(2))) {
                System.out.println("  ⚠ " + item + " expires soon (" + exp + ")");
                foundWarnings = true;
            }
        }

        if (!foundWarnings) {
            System.out.println("  (no items expiring soon)");
        }
        System.out.println();
    }
}