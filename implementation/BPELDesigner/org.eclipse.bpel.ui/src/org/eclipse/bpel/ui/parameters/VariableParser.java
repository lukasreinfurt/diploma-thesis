package org.eclipse.bpel.ui.parameters;

import org.eclipse.bpel.model.Process;
import org.eclipse.bpel.model.Variable;
import org.eclipse.bpel.model.Variables;
import org.eclipse.bpel.ui.parameters.handler.ParameterHandler;
import org.eclipse.bpel.ui.uiextensionmodel.VariableExtension;
import org.eclipse.bpel.ui.util.ModelHelper;
import org.eclipse.emf.common.util.EList;


/**
 * This class parses the current process in the editor and 
 * saves all variable within the process in a map. During the parsing 
 * the dialog for entering the values of the parameters is built.
 * 
 * @author tolevar
 *
 */
public class VariableParser {
	
	private Process process;
	
	//@hahnml
	private ParameterHandler parameterHandler;
	
	private boolean isParameter = false;
	
	public VariableParser(ParameterHandler parameterHandler, Process process) {
		//@hahnml: Changed to process
		this.process = process;
		this.parameterHandler = parameterHandler;
	}
	
	/**
	 * Parses the current process and puts all the variables in a map
	 * and builds the dialog
	 */
	public void parse() {
		Variables variables = process.getVariables();
		EList<Variable> variableList = variables.getChildren();
		for (Variable variable : variableList) {
			//@hahnml: Changed to the VariableExtension of the UIExtensionModel
			VariableExtension varExt = (VariableExtension)ModelHelper.getExtension(variable);
			if (varExt.isParameter()) {
				
				if (varExt.getDefault() != null) {
					if (!this.parameterHandler.getDefaultValues().containsKey(variable)) {
						this.parameterHandler.addDefaultValue(variable, varExt.getDefault());
					}
				}
				
				//Build the dialog depending on what type the variable is
				this.parameterHandler.buildDialog(variable);
				
				//A parameter variable was found so set the isParameter attribute.
				//The system knows that there are parameter variables and the parameter
				//dialog will be opened
				isParameter = true;
				if (!this.parameterHandler.getParameters().containsKey(variable))
					this.parameterHandler.getParameters().put(variable, null);
				
			}
		}
		this.parameterHandler.setParameter(isParameter);
	}
}
