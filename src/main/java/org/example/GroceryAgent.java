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

        madkit.kernel.AbstractAgent.ReturnCode roleResult =
                requestRole("grocery", "main", "groceryAgent");
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

                logOutput("\n[GroceryAgent] Processing: " + text);

                if (text.startsWith("add ")) {
                    String item = text.substring(4).trim();

                    // First, check for healthy alternatives
                    logOutput("[GroceryAgent] Checking for healthier alternatives...");

                    // Send to RecommendationAgent and wait for response
                    sendMessage("grocery", "main", "recommendationAgent",
                            new StringMessage("CHECK:" + item));

                    // Wait a bit for the recommendation
                    pause(300);

                    // Add the item regardless of recommendation
                    groceryList.put(item, LocalDate.now().plusDays(7));
                    purchaseHistory.put(item, LocalDate.now());
                    logOutput(" Added: " + item + " (expires in 7 days)");
                    logOutput("  Expiry Date: " + LocalDate.now().plusDays(7));
                }

                else if (text.startsWith("exp ")) {
                    String[] parts = text.split(" ");
                    if (parts.length == 3) {
                        String item = parts[1].trim();
                        int days = Integer.parseInt(parts[2]);
                        groceryList.put(item, LocalDate.now().plusDays(days));
                        purchaseHistory.put(item, LocalDate.now());
                        logOutput(" Added: " + item + " (expires in " + days + " days)");
                        logOutput("  Expiry Date: " + LocalDate.now().plusDays(days));
                    } else {
                        logOutput(" Usage: exp <item> <days>");
                    }
                }

                else if (text.equals("show")) {
                    logOutput("");
                    logOutput("╔════════════════════════════════════════╗");
                    logOutput("║        Your Grocery List               ║");
                    logOutput("╚════════════════════════════════════════╝");

                    if (groceryList.isEmpty()) {
                        logOutput("  Your list is empty.");
                        logOutput("  Use 'Add Item' to add groceries!");
                    } else {
                        int count = 1;
                        for (Map.Entry<String, LocalDate> entry : groceryList.entrySet()) {
                            String item = entry.getKey();
                            LocalDate expiry = entry.getValue();
                            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                                    LocalDate.now(), expiry);

                            String status = "";
                            if (daysUntilExpiry < 0) {
                                status = "  EXPIRED";
                            } else if (daysUntilExpiry <= 2) {
                                status = "  EXPIRING SOON";
                            }

                            logOutput(String.format("  %d. %s", count++, item));
                            logOutput(String.format("     Expires: %s (%d days)%s",
                                    expiry, daysUntilExpiry, status));
                        }
                        logOutput("");
                        logOutput("  Total items: " + groceryList.size());
                    }
                    logOutput("════════════════════════════════════════");
                }

                else if (text.equals("predict")) {
                    ruleBasedPrediction();
                    expiryCheck();
                }

                else if (text.startsWith("healthy ")) {
                    String item = text.substring(8).trim();
                    logOutput("[GroceryAgent] Consulting RecommendationAgent for: " + item);
                    sendMessage(
                            "grocery", "main", "recommendationAgent",
                            new StringMessage(item)
                    );
                }

                else {
                    logOutput("  Unknown command: " + text);
                    logOutput("  Type 'help' or click the Help button for assistance");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ruleBasedPrediction() {
        logOutput("");
        logOutput("╔════════════════════════════════════════╗");
        logOutput("║      Purchase Predictions              ║");
        logOutput("╚════════════════════════════════════════╝");

        boolean foundSuggestions = false;
        for (String item : purchaseHistory.keySet()) {
            LocalDate lastPurchased = purchaseHistory.get(item);
            long daysSincePurchase = java.time.temporal.ChronoUnit.DAYS.between(
                    lastPurchased, LocalDate.now());

            if (lastPurchased.isBefore(LocalDate.now().minusDays(7))) {
                logOutput("  item " + item);
                logOutput("     Last purchased: " + daysSincePurchase + " days ago");
                logOutput("     Suggestion: Consider adding again");
                foundSuggestions = true;
            }
        }

        if (!foundSuggestions) {
            logOutput("  No suggestions at this time.");
            logOutput("  Keep tracking your purchases!");
        }
        logOutput("════════════════════════════════════════");
    }

    private void expiryCheck() {
        logOutput("");
        logOutput("╔════════════════════════════════════════╗");
        logOutput("║        Expiry Warnings                 ║");
        logOutput("╚════════════════════════════════════════╝");

        boolean foundWarnings = false;
        for (String item : groceryList.keySet()) {
            LocalDate exp = groceryList.get(item);
            long daysUntilExpiry = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), exp);

            if (exp.isBefore(LocalDate.now().plusDays(2))) {
                String urgency = daysUntilExpiry < 0 ? "EXPIRED" : "EXPIRING SOON";
                logOutput("   " + item + " - " + urgency);
                logOutput("     Expiry date: " + exp + " (" + daysUntilExpiry + " days)");

                if (daysUntilExpiry < 0) {
                    logOutput("     Action: Please remove from inventory");
                } else {
                    logOutput("     Action: Use within " + daysUntilExpiry + " day(s)");
                }
                foundWarnings = true;
            }
        }

        if (!foundWarnings) {
            logOutput("All items are fresh!");
            logOutput("No immediate expiry concerns.");
        }
        logOutput("════════════════════════════════════════");
        logOutput("");
    }

    private void logOutput(String message) {
        System.out.println(message);
        // Send message back to UserAgent to display in GUI
        sendMessage("grocery", "main", "user", new StringMessage("LOG:" + message));
    }
}