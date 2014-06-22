package org.eclipse.bpel.ui.parameters.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.ui.agora.manager.ProcessManager;
import org.eclipse.bpel.ui.parameters.VariableParser;
import org.eclipse.bpel.ui.parameters.ui.ParameterDialog;
import org.eclipse.bpel.ui.parameters.ui.composite.BooleanEntry;
import org.eclipse.bpel.ui.parameters.ui.composite.DateTimeEntry;
import org.eclipse.bpel.ui.parameters.ui.composite.DialogEntryComponent;
import org.eclipse.bpel.ui.parameters.ui.composite.Entries;
import org.eclipse.bpel.ui.parameters.ui.composite.FileEntry;
import org.eclipse.bpel.ui.parameters.ui.composite.NumberEntry;
import org.eclipse.bpel.ui.parameters.ui.composite.RMFileEntry;
import org.eclipse.bpel.ui.parameters.ui.composite.StringEntry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * The <b>ParameterHandler</b> handles the communication between the
 * {@link ParameterDialog}, {@link VariableParser} and the BPEL editor. The
 * <b>ParameterHandler</b> holds all variable parameters and handles the event
 * if the ok or cancel button in the dialog is pressed. The
 * <b>ParameterHandler</b> provides some methods for manipulating the
 * corresponding WSDL file for a BPEL process
 * 
 * 
 * @author tolevar
 * 
 * @hahnml: The WSDL file is no longer changed and contains a standard generic
 *          message type to insert the parameters and their values.
 * 
 */
public class ParameterHandler implements Listener {

	private Map<Variable, String[]> parameters = null;
	private List<List<Map<Variable, String>>> cartProductElements = null;

	private ProcessManager manager = null;

	private ParameterDialog dialog = null;

	private boolean noParameter = true;

	private Entries entries;

	private Map<Variable, String> defaultValues = null;
	
	public ParameterHandler() {
		parameters = new HashMap<Variable, String[]>();
		cartProductElements = new ArrayList<List<Map<Variable, String>>>();
		defaultValues = new HashMap<Variable, String>();
	}

	/**
	 * Initializes the dialog by parsing the BPEL process and building the
	 * corresponding entries for the dialog. If there isn't a parameter variable
	 * then no dialog is opened
	 * 
	 * @return true if there are parameter variable else false
	 */
	public boolean initDialog(ProcessManager manager) {
		this.manager = manager;

		Shell parent = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		dialog = new ParameterDialog(parent, SWT.NULL, this);
		dialog.buildShell();
		
		entries = new Entries(dialog.getDialogShell(), SWT.BORDER);

		VariableParser parser = new VariableParser(this, manager.getProcess());
		parser.parse();

		if (isParameter()) {
			for (Variable variable : getParameters().keySet()) {
				entries.buildEntry(variable.getName());
			}
			return true;
		} else {
			return false;
		}
	}

	public Entries getEntries() {
		return entries;
	}

	public Map<Variable, String[]> getParameters() {
		return parameters;
	}

	public List<List<Map<Variable, String>>> getCartProductElements() {
		return cartProductElements;
	}

	public ParameterDialog getDialog() {
		return dialog;
	}

	private boolean isParameter() {
		return noParameter;
	}

	public void setParameter(boolean noParameter) {
		this.noParameter = noParameter;
	}
	
	/**
	 * @param variable
	 */
	public void buildDialog(Variable variable) {
		if (variable.getType().getName().equalsIgnoreCase("string")) {
			entries.add(new StringEntry(entries, SWT.NONE, variable, dialog));
		} else if (variable.getType().getName().equalsIgnoreCase("boolean")) {
			entries.add(new BooleanEntry(entries, SWT.NONE, variable));
		} else if (variable.getType().getName().equalsIgnoreCase("datetime")) {
			entries.add(new DateTimeEntry(entries, SWT.NONE, variable, dialog));
		} else if (variable.getType().getName().equalsIgnoreCase("int")
				|| variable.getType().getName().equalsIgnoreCase("integer")
				|| variable.getType().getName().equalsIgnoreCase("decimal")
				|| variable.getType().getName().equalsIgnoreCase("float")
				|| variable.getType().getName().equalsIgnoreCase("double")
				|| variable.getType().getName().equalsIgnoreCase("short")) {
			entries.add(new NumberEntry(entries, SWT.NONE, variable, dialog));
		} else if (variable.getType().getName().equalsIgnoreCase("file")) {
			entries.add(new FileEntry(entries, SWT.NONE, variable, dialog));
		} else if (variable.getType().getName().equalsIgnoreCase("rmFile")) {
			entries.add(new RMFileEntry(entries, SWT.NONE, variable, dialog));
		} else {
			System.out
					.println("For this parameter type there isn't an entry implemented yet");
		}
	}

	public static List<List<String>> cartProduct(List<List<String>> sets) {
		List<List<String>> cartesian_product = new ArrayList<List<String>>();
		List<String> cartesian_product_element;
		int n = 1;
		Iterator<?> sets_it = sets.iterator();
		// loop to get cardinality of Cartesian product
		while (sets_it.hasNext()) {
			List<?> set = (List<?>) sets_it.next();
			n *= set.size();
		}
		// loop to create all elements of Cartesian product
		for (int i = 0; i < n; i++) {
			int j = 1;
			cartesian_product_element = new ArrayList<String>();
			sets_it = sets.iterator();
			// loop that collects one element of each class
			while (sets_it.hasNext()) {
				List<?> set = (List<?>) sets_it.next();
				cartesian_product_element.add(String.valueOf(set.get((i / j)
						% set.size())));
				j *= set.size();
			}
			cartesian_product.add(cartesian_product_element);
		}
		return cartesian_product;
	}

	@Override
	public void handleEvent(Event event) {
		// If the OK button is clicked the values for the corresponding
		// variables are filled in the map
		if (event.widget == getDialog().getOkButton()) {
			for (int i = 0; i < entries.getLength(); i++) {
				DialogEntryComponent entry = entries.getChild(i);
				String[] values = entry.getValues();
				String varName = entry.getVariableName();
				for (Variable variable : parameters.keySet()) {
					if (variable.getName().equals(varName)) {
						parameters.put(variable, values);
					}
				}
			}

			getDialog().getDialogShell().close();
			getDialog().getDialogShell().dispose();

			// Calculate the Cartesian product and start the workflow with the
			// given product and the variable list
			List<List<String>> cart = new ArrayList<List<String>>();

			ArrayList<Variable> variableList = new ArrayList<Variable>(
					parameters.keySet());
			Map<Variable, String[]> map = parameters;

			for (Variable variable : variableList) {
				List<String> values = Arrays.asList(map.get(variable));
				cart.add(values);
			}

			List<List<String>> product = cartProduct(cart);

			// Start the monitor manager
			manager.prepareAndStartProcessInstance(product, variableList);
		} else if (event.widget == getDialog().getCancelButton()) {
			getDialog().getDialogShell().close();
			getDialog().getDialogShell().dispose();
		}
		parameters.clear();
	}

	/**
	 * Adds a new parameter variable to the ParameterHandler
	 * 
	 * @param variable
	 */
	public void addParameterVariable(Variable variable) {
		this.parameters.put(variable, null);
	}
	
	public void removeDefaultValue(Variable variable) {
		defaultValues.remove(variable);
	}
	
	public void addDefaultValue(Variable variable, String defaultValue) {
		defaultValues.put(variable, defaultValue);
	}
	
	public Map<Variable, String> getDefaultValues() {
		return defaultValues;
	}

	public String getDefaultValue(Variable variable) {
		return defaultValues.containsKey(variable)?defaultValues.get(variable):"";
	}
	
	/**
	 * Removes a parameter variable from the ParameterHandler
	 * 
	 * @param variable
	 */
	public void removeParameterVariable(Variable variable) {
		this.parameters.remove(variable);
	}

}
