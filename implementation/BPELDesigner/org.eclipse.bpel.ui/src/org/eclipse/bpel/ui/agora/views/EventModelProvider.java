package org.eclipse.bpel.ui.agora.views;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds all EventMessage objects during runtime.
 * 
 * @author hahnml
 *
 */
public class EventModelProvider {

	private List<EventMessage> events;
	private IViewListener viewListener;

	public EventModelProvider() {
		events = new ArrayList<EventMessage>();
	}

	public EventMessage find(String id) {
		EventMessage found = null;
		for (EventMessage ref : this.events) {
			if (ref.toString().equals(id)) {
				found = ref;
				continue;
			}
		}
		return found;
	}

	/**
	 * @return A list of all EventMessages of the model.
	 */
	public List<EventMessage> getEvents() {
		return this.events;
	}

	/**
	 * Clears the whole data of the model.
	 */
	public void clear() {
		events.clear();
		
		// Updating the display in the view
		updateView();
	}
	
	
	/**
	 * This method is used to register the view as listener.
	 * 
	 * @param listener The view which should be listen on model changes.
	 */
	public void setViewListener(IViewListener listener){
		this.viewListener = listener;
	}
	
	/**
	 * Adds an EventMessage to the model.
	 * 
	 * @param event to add.
	 */
	public void addEventMessage(EventMessage event){
		events.add(event);
		
		// Updating the display in the view
		updateView();
	}
	
	/**
	 * Adds a list of EventMessage objects to the model
	 * 
	 * @param eventList The list of EventMessage objects to add
	 */
	public void addEventMessageList(List<EventMessage> eventList) {
		events.addAll(eventList);
		
		// Updating the display in the view
		updateView();
	}
	
	/**
	 * Forces the view which is registered as listener to update.
	 */
	private void updateView(){
		if (this.viewListener != null){
			this.viewListener.update();
		}
	}
}
