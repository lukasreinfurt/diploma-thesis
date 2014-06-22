package org.eclipse.bpel.ui.preferences;

import java.util.ArrayList;

import org.eclipse.bpel.model.terms.BPELTerms;
import org.eclipse.bpel.ui.BPELUIPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class implements a preference page for all SimTech preferences. It is
 * added under the BPEL preference group.
 * 
 * @author hahnml
 * 
 */
public class SimTechPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	ArrayList<Button> fButtons = new ArrayList<Button>();
	ArrayList<Text> fTexts = new ArrayList<Text>();

	Button fSendRequestsButton;
	Button fUseExtIteration;

	Label fActiveMQURLLabel;
	Text fActiveMQURLText;

	Label fInstanceStartWaitingTimeLabel;
	Text fInstanceStartWaitingTimeText;
	
	static String PREFERENCE = "preference";

	@Override
	protected Control createContents(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.verticalSpacing = 10;
		result.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		result.setLayoutData(data);

		createSendRequestContents(result);
		
		createUseExtIterationContents(result);
		
		createActiveMQURLContents(result);
		
		createWaitingTimeContents(result);

		initializeValues();

		return result;
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		super.performDefaults();
		initializeDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		storeValues();
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	protected void performApply() {
		performOk();
	}

	/**
	 * Initializes states of the controls using default values in the preference
	 * store.
	 */
	private void initializeDefaults() {
		fActiveMQURLText.setText("tcp://localhost:61616");
		fSendRequestsButton.setSelection(true);
		fUseExtIteration.setSelection(false);
		fInstanceStartWaitingTimeText.setText("200");
	}

	/**
	 * Initializes states of the controls from the preference store.
	 */
	private void initializeValues() {
		IPreferenceStore store = BPELUIPlugin.INSTANCE.getPreferenceStore();

		for (Button b : fButtons) {
			String pref = (String) b.getData(PREFERENCE);
			if (pref != null) {
				b.setSelection(store.getBoolean(pref));
			}
		}

		for (Text t : fTexts) {
			String pref = (String) t.getData(PREFERENCE);
			if (pref != null) {
				t.setText(store.getString(pref));
			}
		}
	}

	/**
	 * Stores the values of the controls back to the preference store.
	 */
	private void storeValues() {
		IPreferenceStore store = BPELUIPlugin.INSTANCE.getPreferenceStore();

		for (Button b : fButtons) {
			String pref = (String) b.getData(PREFERENCE);
			if (pref != null) {
				store.setValue(pref, b.getSelection());
			}
		}

		for (Text t : fTexts) {
			String pref = (String) t.getData(PREFERENCE);
			if (pref != null) {
				store.setValue(pref, t.getText());
			}
		}

		BPELTerms.getDefault().savePluginPreferences();
	}

	/**
	 * This method creates the ActiveMQ preference contents
	 */
	private void createActiveMQURLContents(Composite parent) {
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		fActiveMQURLLabel = new Label(parent, SWT.NONE);
		fActiveMQURLLabel.setText("ActiveMQ URL: ");
		fActiveMQURLText = new Text(parent, SWT.BORDER);
		fActiveMQURLText
				.setToolTipText("Please enter the URL of the ActiveMQ server you want to connect to");
		fActiveMQURLText.setData(PREFERENCE, "ACTIVE_MQ_URL");
		fActiveMQURLText.setLayoutData(data);
		fTexts.add(fActiveMQURLText);
	}

	/**
	 * This method creates the preference contents, which allow to choose
	 * whether request to a connected SimTech Auditing Application will be send
	 * or not.
	 */
	private void createSendRequestContents(Composite parent) {
		GridData data = new GridData();
		data.horizontalSpan = 4;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		fSendRequestsButton = new Button(parent, SWT.CHECK);
		fSendRequestsButton
				.setText(" Enable sending requests to a connected SimTech Auditing Application");
		fSendRequestsButton.setLayoutData(data);
		fSendRequestsButton.setData(PREFERENCE, "SEND_REQUESTS");
		fButtons.add(fSendRequestsButton);
	}
	
	/**
	 * This method creates the preference contents, which allow to choose
	 * whether the extended iteration (snapshot wizard) should be used
	 * or not.
	 */
	private void createUseExtIterationContents(Composite parent) {
		GridData data = new GridData();
		data.horizontalSpan = 4;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		fUseExtIteration = new Button(parent, SWT.CHECK);
		fUseExtIteration
				.setText(" Use the extended iteration to reload a snapshot.");
		fUseExtIteration.setLayoutData(data);
		fUseExtIteration.setData(PREFERENCE, "USE_EXT_ITERATION");
		fButtons.add(fUseExtIteration);
	}
	
	/**
	 * This method creates the waiting time contents, which allow to specify
	 * how long a process instance will wait after it is instantiated to start execution.
	 */
	private void createWaitingTimeContents(Composite parent) {
		GridData data = new GridData();
		data.horizontalSpan = 3;
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = SWT.FILL;
		
		fInstanceStartWaitingTimeLabel = new Label(parent, SWT.NONE);
		fInstanceStartWaitingTimeLabel.setText("Instance Waiting Time (in ms): ");
		fInstanceStartWaitingTimeText = new Text(parent, SWT.BORDER);
		fInstanceStartWaitingTimeText
				.setToolTipText("Please enter the time to wait between instantiation and execution of a process model");
		fInstanceStartWaitingTimeText.setData(PREFERENCE, "INSTANCE_WAITING_TIME");
		fInstanceStartWaitingTimeText.setLayoutData(data);
		fTexts.add(fInstanceStartWaitingTimeText);
	}
	
}
