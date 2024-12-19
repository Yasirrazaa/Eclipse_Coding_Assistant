package com.copilot.groq.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.copilot.groq.views.GroqAssistantView;

public class AnalyzeCodeHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                try {
                    GroqAssistantView view = (GroqAssistantView) page.showView(GroqAssistantView.ID);
                    if (view != null) {
                        view.processAnalyzeRequest()
                            .exceptionally(throwable -> {
                                // Log error or show error dialog
                                return null;
                            });
                    }
                } catch (PartInitException e) {
                    throw new ExecutionException("Failed to open Groq Assistant view", e);
                }
            }
        }
        return null;
    }
}
