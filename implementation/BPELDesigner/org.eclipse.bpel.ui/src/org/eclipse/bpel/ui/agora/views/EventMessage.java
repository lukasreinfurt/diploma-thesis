package org.eclipse.bpel.ui.agora.views;

import org.simtech.workflow.ode.auditing.agora.BPELStates;

/**
 * Realizes the data structure of an event to display it in the auditing view.
 * 
 * @author hahnml, tolevar
 *
 */
public class EventMessage {

	private String eventType = null;
	private String source = null;
	private String elementName = null;
	private Long timestamp = null;
	private BPELStates state = null;
	private Object messageObject = null;

	public EventMessage(String eventType, String source, String elementName, Long timestamp,
			BPELStates state, Object messageObject) {
		super();
		this.eventType = eventType;
		this.source = source;
		this.setElementName(elementName);
		this.timestamp = timestamp;
		this.state = state;
		this.setMessageObject(messageObject);
	}

	public EventMessage(String eventType, String source, String elementName, Long timestamp, Object messageObject) {
		super();
		this.eventType = eventType;
		this.source = source;
		this.setElementName(elementName);
		this.timestamp = timestamp;
		this.setMessageObject(messageObject);
	}

	public EventMessage() {
		
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getEventType() {
		return eventType;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public BPELStates getState() {
		return state;
	}

	public void setState(BPELStates state) {
		this.state = state;
	}

	public void setMessageObject(Object messageObject) {
		this.messageObject = messageObject;
	}

	public Object getMessageObject() {
		return messageObject;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementName() {
		return elementName;
	}

}
