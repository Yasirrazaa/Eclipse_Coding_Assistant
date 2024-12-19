package com.copilot.groq.completion;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;

public class GroqJavaCompletionProposalComputer implements IJavaCompletionProposalComputer {

    @Override
    public void sessionStarted() {
        // No initialization needed
    }

    @Override
    public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        // Return an empty list to disable code completion
        return java.util.Collections.emptyList();
    }

    @Override
    public List<IContextInformation> computeContextInformation(ContentAssistInvocationContext context, IProgressMonitor monitor) {
        // Return an empty list to disable context information
        return java.util.Collections.emptyList();
    }

    @Override
    public String getErrorMessage() {
        // Return null if no error occurred during the last call to computeCompletionProposals() or computeContextInformation()
        return null;
    }

    @Override
    public void sessionEnded() {
        // No cleanup needed
    }
}
