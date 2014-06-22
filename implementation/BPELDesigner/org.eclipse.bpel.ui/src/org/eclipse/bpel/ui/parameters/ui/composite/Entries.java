package org.eclipse.bpel.ui.parameters.ui.composite;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Superclass of all underlying entry classes. This class surrounds all
 * other entry classes. All underlying entries are stored in a list. For 
 * building the dialog this super entry class is called with the
 * build method.
 * 
 * @author tolevar
 *
 */
public class Entries extends Composite {
	
	private ArrayList<DialogEntryComponent> entries = new ArrayList<DialogEntryComponent>();

	public Entries(Composite parent, int style) {
		super(parent, style);
		
		this.setLayout(new GridLayout(1, false));
		this.layout();
		
		GridData entriesData = new GridData();
		entriesData.horizontalAlignment = SWT.FILL;
		entriesData.verticalAlignment = SWT.FILL;
		entriesData.grabExcessHorizontalSpace = true;
		entriesData.grabExcessVerticalSpace = true;
		entriesData.horizontalSpan = 2;
		this.setLayoutData(entriesData);
		
		entries.clear();
	}
	
	public void add(DialogEntryComponent component) {
		entries.add(component);
	}
	
	public void remove(DialogEntryComponent component) {
		entries.remove(component);
	}
	
	public DialogEntryComponent getChild(int i) {
		return entries.get(i);
	}
	
	public int getLength() {
		return entries.size();
	}
	
	public void buildEntry(String variableName) {
		Iterator<DialogEntryComponent> iterator = entries.iterator();
		while (iterator.hasNext()) {
			DialogEntryComponent component = (DialogEntryComponent) iterator.next();
			if (component.getVariable().getName().equals(variableName)) {
				component.buildEntry(variableName);
			}
		}
	}
}
