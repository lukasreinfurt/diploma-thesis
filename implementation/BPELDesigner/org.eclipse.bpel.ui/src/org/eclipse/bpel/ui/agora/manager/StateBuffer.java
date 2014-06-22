package org.eclipse.bpel.ui.agora.manager;

import java.util.HashMap;

import org.simtech.workflow.ode.auditing.agora.BPELStates;

public class StateBuffer {

	private HashMap<String, BPELStates> stateMap = new HashMap<String, BPELStates>();
	
	public void updateStateInBuffer(String xPath, BPELStates state) {
		stateMap.put(xPath, state);
	}
	
	public BPELStates getStateFromBuffer(String xPath) {
		return stateMap.get(xPath);
	}
	
	public boolean containsActivity(String xPath) {
		return stateMap.containsKey(xPath);
	}
	
	public void clearStateBuffer() {
		stateMap.clear();
	}
}
