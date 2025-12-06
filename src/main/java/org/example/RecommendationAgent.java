package org.example;

import madkit.kernel.Agent;
import madkit.kernel.Message;
import madkit.message.StringMessage;

import java.util.HashMap;
import java.util.Map;

public class RecommendationAgent extends Agent {

    private Map<String, String> healthyAlt = new HashMap<>();

    @Override
    protected void activate() {
        System.out.println("[RecommendationAgent] Activating...");

        // Wait to ensure group is created
        pause(1000);

        // Request role in the existing group
        madkit.kernel.AbstractAgent.ReturnCode roleResult = requestRole("grocery", "main", "recommendationAgent");
        System.out.println("[RecommendationAgent] Request role result: " + roleResult);

        // Initialize healthy alternatives
        healthyAlt.put("white bread", "brown bread");
        healthyAlt.put("sugar", "stevia");
        healthyAlt.put("full cream milk", "low-fat milk");
        healthyAlt.put("rice", "brown rice");
        healthyAlt.put("noodles", "whole-grain noodles");
        healthyAlt.put("chips", "baked chips or nuts");
        healthyAlt.put("soda", "sparkling water");
        healthyAlt.put("butter", "olive oil");

        if (roleResult == madkit.kernel.AbstractAgent.ReturnCode.SUCCESS) {
            System.out.println("[RecommendationAgent] Successfully joined with " + healthyAlt.size() + " alternatives loaded.");
        } else {
            System.out.println("[RecommendationAgent] ERROR: Failed to join group! Result: " + roleResult);
        }
    }

    @Override
    protected void live() {
        try {
            while (true) {
                Message msg = waitNextMessage(1000); // Wait 1 second for messages

                if (msg == null) continue;

                String item = ((StringMessage) msg).getContent().trim();

                System.out.println("\n[RecommendationAgent] Analyzing: " + item);

                String lowerItem = item.toLowerCase();
                if (healthyAlt.containsKey(lowerItem)) {
                    System.out.println("Healthier option: " + healthyAlt.get(lowerItem));
                } else {
                    System.out.println("No healthier alternative found for: " + item);
                    System.out.println("  (Consider checking nutrition labels or consulting a dietitian)");
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}