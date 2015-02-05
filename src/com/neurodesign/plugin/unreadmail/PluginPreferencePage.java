package com.neurodesign.plugin.unreadmail;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PluginPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private RadioGroupFieldEditor refreshSpeed;
	private RadioGroupFieldEditor sortType;
	//private RadioGroupFieldEditor sortType;
	
	@Override
	protected Control createContents(Composite parent) {
		Composite entryTable = new Composite(parent, SWT.NULL);

		//Create a data that takes up the extra space in the dialog .
		GridData data = new GridData(GridData.FILL_VERTICAL);
		data.grabExcessHorizontalSpace = true;
		entryTable.setLayoutData(data);

		GridLayout layout = new GridLayout();
		entryTable.setLayout(layout);
		
		Composite prefComposite = new Composite(entryTable,SWT.NONE);

		prefComposite.setLayout(new GridLayout());
		
		//Create a data that takes up the extra space in the dialog.
		prefComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		refreshSpeed = new RadioGroupFieldEditor(Activator.PREF_REFRESH, Activator.PREF_REFRESH_TITLE, 4, Activator.PREF_REFRESH_SPEED_CHOICES, entryTable);				
		
		//Set the editor up to use this page
		refreshSpeed.setPage(this);
		refreshSpeed.setPreferenceStore(getPreferenceStore());
		refreshSpeed.load();
		
		sortType = new RadioGroupFieldEditor(Activator.PREF_SORT, Activator.PREF_SORT_TITLE, 4, Activator.PREF_SORT_CHOICES, entryTable);				
		
		//Set the editor up to use this page
		sortType.setPage(this);
		sortType.setPreferenceStore(getPreferenceStore());
		sortType.load();

		return entryTable;
	}

	@Override
	public void init(IWorkbench arg0) {
		// TODO Auto-generated method stub
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	protected void performDefaults() {
		refreshSpeed.loadDefault();
	}
	
	public boolean performOk() {
		sortType.store();
		refreshSpeed.store();
		return super.performOk();
	}
}
