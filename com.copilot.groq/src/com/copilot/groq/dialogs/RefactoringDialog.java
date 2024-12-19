package com.copilot.groq.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class RefactoringDialog extends Dialog {
    @SuppressWarnings("unused")
    private static final long serialVersionUID = 1L;

    private static final String[] REFACTORING_TYPES = {
        "Extract Method",
        "Extract Class",
        "Rename",
        "Inline",
        "Encapsulate Field",
        "General Improvement"
    };

    private Combo refactoringTypeCombo;
    private String selectedRefactoringType;

    public RefactoringDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 10;
        layout.marginHeight = 10;
        container.setLayout(layout);

        Label label = new Label(container, SWT.NONE);
        label.setText("Select Refactoring Type:");
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        refactoringTypeCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        refactoringTypeCombo.setItems(REFACTORING_TYPES);
        refactoringTypeCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        refactoringTypeCombo.select(0);

        refactoringTypeCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                selectedRefactoringType = refactoringTypeCombo.getText();
            }
        });

        return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select Refactoring Type");
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    public String getSelectedRefactoringType() {
        return selectedRefactoringType != null ? selectedRefactoringType : REFACTORING_TYPES[0];
    }
}
