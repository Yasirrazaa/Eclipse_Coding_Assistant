package com.copilot.groq.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class ToggleGroqAssistantHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
        if (window == null) {
            return null;
        }

        IWorkbenchPage page = window.getActivePage();
        if (page == null) {
            return null;
        }

        try {
            // Toggle the view visibility
            String viewId = "com.example.myplugin.views.GroqAssistantView";
            if (page.findView(viewId) == null) {
                page.showView(viewId);
            } else {
                page.hideView(page.findView(viewId));
            }
        } catch (PartInitException e) {
            // Log the error
            e.printStackTrace();
        }

        return null;
    }
}
