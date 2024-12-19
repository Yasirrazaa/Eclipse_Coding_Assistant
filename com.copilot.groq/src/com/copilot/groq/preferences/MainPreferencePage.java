package com.copilot.groq.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.copilot.groq.Activator;

public class MainPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MainPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Copilot Assistant Settings");
    }

    @Override
    public void init(IWorkbench workbench) {
        // Initialize workbench
    }

    @Override
    protected void createFieldEditors() {
        // API Settings
        addField(new StringFieldEditor(
            PreferenceConstants.P_API_KEY,
            "API Key:",
            getFieldEditorParent()));

        // UI Settings
        addField(new BooleanFieldEditor(
            PreferenceConstants.P_DARK_THEME,
            "Use dark theme",
            getFieldEditorParent()));
    }
}
