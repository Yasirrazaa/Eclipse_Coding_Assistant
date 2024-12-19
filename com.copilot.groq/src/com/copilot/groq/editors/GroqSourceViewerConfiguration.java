package com.copilot.groq.editors;

import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

public class GroqSourceViewerConfiguration extends TextSourceViewerConfiguration {

    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        ContentAssistant assistant = new ContentAssistant();
        // assistant.setContentAssistProcessor(new GroqCompletionProcessor(), IDocument.DEFAULT_CONTENT_TYPE);

        // Configure auto-activation
        assistant.enableAutoActivation(false); // Disable auto-activation
        assistant.setAutoActivationDelay(0); // Immediate activation
        assistant.enableAutoInsert(true);
        assistant.enablePrefixCompletion(true);
        assistant.setShowEmptyList(false);
        assistant.setRepeatedInvocationMode(true);
        assistant.setStatusLineVisible(true);
        assistant.setStatusMessage("Groq AI Suggestions Available");
        assistant.setEmptyMessage("No suggestions available");

        // Set colors for proposals
        Display display = Display.getDefault();
        assistant.setProposalSelectorBackground(new Color(display, new RGB(45, 45, 45)));
        assistant.setProposalSelectorForeground(new Color(display, new RGB(220, 220, 220)));

        return assistant;
    }
}
