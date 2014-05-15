package org.simtech.bootware.core;

/**
 * @author  Lukas Reinfurt
 * @version 1.0.0
 */
public class Main {

	/**
	 * Runs the bootware program.
	 *
	 * @param args Commandline arguments.
	 */
	public static void main(String[] args) {
		EventBus eventBus = new EventBus();

		PluginManager pluginManager = new PluginManager();
		pluginManager.registerSharedObject(eventBus);
		pluginManager.loadPlugin("plugins/event/consoleLogger-1.0.0.jar");

		StateMachine stateMachine = new StateMachine(eventBus);
		stateMachine.run();

		pluginManager.stop();
	}
}
