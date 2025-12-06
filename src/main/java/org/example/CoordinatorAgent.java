package org.example;

import madkit.kernel.Agent;

public class CoordinatorAgent extends Agent {

    @Override
    protected void activate() {
        getLogger().info("CoordinatorAgent starting...");

        // Create the group FIRST
        madkit.kernel.AbstractAgent.ReturnCode groupResult = createGroup("grocery", "main");
        getLogger().info("Create group result: " + groupResult);

        if (groupResult == madkit.kernel.AbstractAgent.ReturnCode.SUCCESS ||
                groupResult == madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP) {

            getLogger().info("Group created successfully. Launching other agents...");

            // Now launch the other agents
            launchAgent(new GroceryAgent());
            launchAgent(new RecommendationAgent());
            launchAgent(new UserAgent());

            getLogger().info("All agents launched!");
        } else {
            getLogger().severe("Failed to create group! Result: " + groupResult);
        }
    }

    @Override
    protected void live() {
        // Just keep this agent alive
        while (true) {
            pause(10000);
        }
    }
}