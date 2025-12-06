package org.example;

import madkit.kernel.Madkit;

public class MainLauncher {

    public static void main(String[] args) {
        // Launch ONE agent that will create the group and launch others
        new Madkit(
                "--launchAgents", "org.example.CoordinatorAgent,false,1",
                "--agentLogLevel", "INFO"
        );
    }
}