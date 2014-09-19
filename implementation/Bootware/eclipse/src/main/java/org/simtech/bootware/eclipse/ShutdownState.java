package org.simtech.bootware.eclipse;

public final class ShutdownState {

	private static Boolean shuttingDown;

	static {
		shuttingDown = false;
	}

	private ShutdownState() {}

	public static void set(final Boolean state) {
		shuttingDown = state;
	}

	public static Boolean get() {
		return shuttingDown;
	}
}
