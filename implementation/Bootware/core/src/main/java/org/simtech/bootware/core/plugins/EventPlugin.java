package org.simtech.bootware.core.plugins;

/**
 * Interface that should be implemented by event plugins.
 * <p>
 * There are no special operations that event plugins have to implement, apart
 * from the operations in @see org.simtech.bootware.core.plugins.Plugin
 * Event plugins can implement zero to many handle methods with the handle
 * annotation to react to specific events.
 * For example:
 * <code><pre>
 *   @Handler
 *   public final void handle(final BaseEvent event) {
 *     System.out.println("[" + event.getSeverity() + "] " + event.getMessage());
 *   }
 * </pre></code>
 */
public interface EventPlugin extends Plugin {

}
