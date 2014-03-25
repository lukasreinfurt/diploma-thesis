package org.simtech.bootware.core;

public class Main {

	public static void main(String[] args) {

		EventBus eventBus = new EventBus();

		PluginManager pluginManager = new PluginManager();
		pluginManager.registerSharedObject(eventBus);
		pluginManager.loadPlugin("plugins/consoleLogger-0.0.1.jar");
		pluginManager.loadPlugin("plugins/plugin1-0.0.1.jar");

		StateMachine stateMachine = new StateMachine(eventBus, "statemachines/default.scxml");
		stateMachine.run();

		pluginManager.stop();

	}
}
