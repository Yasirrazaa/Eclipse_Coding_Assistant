package com.copilot.groq.views;

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import com.copilot.groq.Activator;
import com.copilot.groq.api.CodeGenerationService;

public class CodeGenerationView extends ViewPart {
    public static final String ID = "com.example.myplugin.views.CodeGenerationView";

    private Text promptText;
    private StyledText resultText;
    private CodeGenerationService codeGenerationService;

    public CodeGenerationView() {
        codeGenerationService = new CodeGenerationService();
    }

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));

        // Title
        Label titleLabel = new Label(parent, SWT.NONE);
        titleLabel.setText("Code Generation");
        FontData[] fontData = titleLabel.getFont().getFontData();
        fontData[0].setHeight(12);
        fontData[0].setStyle(SWT.BOLD);
        Font titleFont = new Font(parent.getDisplay(), fontData[0]);
        titleLabel.setFont(titleFont);
        titleLabel.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

        // Prompt input
        promptText = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        promptText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).hint(SWT.DEFAULT, 100).create());
        promptText.setMessage("Enter your code generation prompt here...");

        // Generate button
        Button generateButton = new Button(parent, SWT.PUSH);
        generateButton.setText("Generate Code");
        generateButton.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
        generateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                handleGenerateCode();
            }
        });

        // Result area
        resultText = new StyledText(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        resultText.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
        resultText.setEditable(false);
    }

    private void handleGenerateCode() {
        String prompt = promptText.getText();
        Job job = new Job("Generating Code") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    CompletableFuture<String> generatedCodeFuture = codeGenerationService.generateCode(prompt);
                    generatedCodeFuture.thenAccept(generatedCode -> {
                        Display.getDefault().asyncExec(() -> {
                            resultText.setText(generatedCode);
                        });
                    });
                    return Status.OK_STATUS;
                } catch (Exception ex) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Failed to generate code", ex);
                }
            }
        };
        job.schedule();
    }

    @Override
    public void setFocus() {
        if (promptText != null && !promptText.isDisposed()) {
            promptText.setFocus();
        }
    }
}
