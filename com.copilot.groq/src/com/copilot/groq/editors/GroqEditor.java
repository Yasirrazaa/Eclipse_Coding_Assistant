package com.copilot.groq.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class GroqEditor extends TextEditor {
    private GroqSourceViewerConfiguration configuration;

    public GroqEditor() {
        super();
        configuration = new GroqSourceViewerConfiguration();
        setSourceViewerConfiguration(configuration);
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setDocumentProvider(new org.eclipse.ui.editors.text.TextFileDocumentProvider());
    }
}