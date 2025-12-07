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

        // Initialize healthy alternatives database
        healthyAlt.put("white bread", "brown bread (whole grain)");
        healthyAlt.put("sugar", "stevia or honey");
        healthyAlt.put("full cream milk", "low-fat milk or almond milk");
        healthyAlt.put("rice", "brown rice or quinoa");
        healthyAlt.put("noodles", "whole-grain noodles");
        healthyAlt.put("chips", "baked chips, nuts, or veggie sticks");
        healthyAlt.put("soda", "sparkling water or fresh juice");
        healthyAlt.put("butter", "olive oil or avocado");
        healthyAlt.put("white pasta", "whole wheat pasta");
        healthyAlt.put("ice cream", "frozen yogurt or fruit sorbet");
        healthyAlt.put("cookies", "oatmeal cookies or fruit");
        healthyAlt.put("candy", "dark chocolate or dried fruit");
        healthyAlt.put("mayonnaise", "greek yogurt or hummus");
        healthyAlt.put("cream cheese", "cottage cheese or neufchatel");
        healthyAlt.put("bacon", "turkey bacon or tempeh bacon");

        madkit.kernel.AbstractAgent.ReturnCode roleResult =
                requestRole("grocery", "main", "recommendationAgent");
        System.out.println("[RecommendationAgent] Request role result: " + roleResult);

        if (roleResult == madkit.kernel.AbstractAgent.ReturnCode.SUCCESS) {
            System.out.println("[RecommendationAgent] Successfully joined with " +
                    healthyAlt.size() + " alternatives loaded!");
        } else {
            System.out.println("[RecommendationAgent] ERROR: Failed to join! Result: " + roleResult);
        }
    }

    @Override
    protected void live() {
        try {
            while (true) {
                Message msg = waitNextMessage(1000);

                if (msg == null) continue;

                String item = ((StringMessage) msg).getContent().trim();

                logOutput("");
                logOutput("╔════════════════════════════════════════╗");
                logOutput("║    Healthy Alternative Analysis        ║");
                logOutput("╚════════════════════════════════════════╝");
                logOutput("  Analyzing: " + item);
                logOutput("");

                String lowerItem = item.toLowerCase();
                if (healthyAlt.containsKey(lowerItem)) {
                    logOutput("  ✓ Healthier Alternative Found!");
                    logOutput("");
                    logOutput("  Original:    " + item);
                    logOutput("  Recommended: " + healthyAlt.get(lowerItem));
                    logOutput("");
                    logOutput("  💡 Health Benefits:");
                    logOutput("     • Lower in calories or sugar");
                    logOutput("     • More nutrients and fiber");
                    logOutput("     • Better for long-term health");
                } else {
                    logOutput("  ℹ️  No specific alternative found for: " + item);
                    logOutput("");
                    logOutput("  💡 General Tips:");
                    logOutput("     • Check nutrition labels");
                    logOutput("     • Look for whole grain options");
                    logOutput("     • Choose items with less sugar");
                    logOutput("     • Consider organic alternatives");
                    logOutput("     • Consult with a nutritionist");
                }
                logOutput("════════════════════════════════════════");
                logOutput("");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logOutput(String message) {
        System.out.println(message);
    }
}