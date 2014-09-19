package org.simtech.bootware.eclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * Handles the triggering of the bootware shutdown operation with a menu button.
 */
public class TriggerBootwareShutdownHandler extends AbstractHandler {

	public TriggerBootwareShutdownHandler() {}

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {

		// When the button is clicked, trigger the shutdown process in a new thread,
		// so that the Eclipse GUI won't be blocked.
		final Thread t = new Thread(new Runnable() {
			public void run() {
				ShutdownTrigger.trigger();
			}
		});
		t.start();

		return null;
	}

}
