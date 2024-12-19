package com.copilot.groq.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import com.copilot.groq.Activator;
import com.copilot.groq.api.CodeGenerationService;
import com.copilot.groq.util.ChatHistory;
import com.copilot.groq.util.RateLimitManager;

public class GroqAssistantView extends ViewPart {
    public static final String ID = "com.example.myplugin.views.GroqAssistantView";
    private static final RGB DARK_BACKGROUND = new RGB(43, 43, 43);
    private static final RGB TEXT_COLOR = new RGB(204, 204, 204);
    private static final RGB INPUT_BACKGROUND = new RGB(51, 51, 51);
    private static final RGB HEADER_BACKGROUND = new RGB(37, 37, 37);
    private static final RGB ASSISTANT_MESSAGE_BG = new RGB(51, 51, 51);
    private static final RGB USER_MESSAGE_BG = new RGB(43, 43, 43);

    private Composite headerComposite;
    private Text inputText;
    private Button sendButton;
    private Button attachButton;
    private ToolBar toolBar;
    private ScrolledComposite scrolledComposite;
    private Composite chatContainer;
    private Label conversationLabel;
    private CodeGenerationService codeGenerationService;
    private Clipboard clipboard;
    private Label statusLabel;
    private String currentConversationId;
    private ContentProposalAdapter proposalAdapter;
    private List<String> autoCompleteProposals = new ArrayList<>();

    @Override
    public void createPartControl(Composite parent) {
        parent.setLayout(new GridLayout(1, false));
        parent.setBackground(new Color(parent.getDisplay(), DARK_BACKGROUND));

        // Initialize services
        codeGenerationService = new CodeGenerationService();
        clipboard = new Clipboard(parent.getDisplay());

        // Register clipboard disposal
        parent.addDisposeListener(e -> {
            if (clipboard != null && !clipboard.isDisposed()) {
                clipboard.dispose();
            }
        });

        // Initialize chat history with a new UUID
        currentConversationId = UUID.randomUUID().toString();

        createHeader(parent);
        createChatArea(parent);
        createInputArea(parent);

        initializeColors(parent);
        initializeFonts(parent);
        setupAutoCompletion();

        // Initialize chat history
        loadChatHistory();

        // Initialize status
        updateRateLimitStatus();
    }

    private void initializeColors(Composite parent) {
        Display display = parent.getDisplay();
        Color backgroundColor = new Color(display, DARK_BACKGROUND);
        Color highlightColor = new Color(display, new RGB(68, 68, 68));
        Color aiMessageColor = new Color(display, ASSISTANT_MESSAGE_BG);
        Color buttonColor = new Color(display, new RGB(78, 78, 78));
        Color textColor = new Color(display, TEXT_COLOR);
        Color codeBgColor = new Color(display, new RGB(30, 30, 30));
        Color codeTextColor = new Color(display, new RGB(220, 220, 220));
        Color headerBgColor = new Color(display, HEADER_BACKGROUND);

        // Apply colors to main components
        parent.setBackground(backgroundColor);
        chatContainer.setBackground(backgroundColor);
        headerComposite.setBackground(headerBgColor);

        // Apply colors to input area
        inputText.setBackground(codeBgColor);
        inputText.setForeground(textColor);

        // Apply colors to buttons
        sendButton.setBackground(buttonColor);
        sendButton.setForeground(textColor);
        attachButton.setBackground(buttonColor);
        attachButton.setForeground(textColor);

        // Set up highlight color for hover effects
        sendButton.addListener(SWT.MouseEnter, e -> sendButton.setBackground(highlightColor));
        sendButton.addListener(SWT.MouseExit, e -> sendButton.setBackground(buttonColor));
        attachButton.addListener(SWT.MouseEnter, e -> attachButton.setBackground(highlightColor));
        attachButton.addListener(SWT.MouseExit, e -> attachButton.setBackground(buttonColor));

        // Apply colors to message containers
        for (Control control : chatContainer.getChildren()) {
            if (control instanceof Composite) {
                Composite messageComposite = (Composite) control;
                if (messageComposite.getData("type") == "assistant") {
                    messageComposite.setBackground(aiMessageColor);
                    for (Control child : messageComposite.getChildren()) {
                        if (child instanceof StyledText) {
                            child.setForeground(codeTextColor);
                        }
                    }
                }
            }
        }

        // Register color disposal
        parent.addDisposeListener(e -> {
            backgroundColor.dispose();
            highlightColor.dispose();
            aiMessageColor.dispose();
            buttonColor.dispose();
            textColor.dispose();
            codeBgColor.dispose();
            codeTextColor.dispose();
            headerBgColor.dispose();
        });
    }

    private void initializeFonts(Composite parent) {
        Font buttonFont = FontDescriptor.createFrom(parent.getFont()).setStyle(SWT.BOLD).createFont(parent.getDisplay());
        Font codeFont = new Font(parent.getDisplay(), "Consolas", 12, SWT.NORMAL);

        // Apply fonts
        sendButton.setFont(buttonFont);
        attachButton.setFont(buttonFont);
        inputText.setFont(codeFont);

        // Register font disposal
        parent.addDisposeListener(e -> {
            buttonFont.dispose();
            codeFont.dispose();
        });
    }

    private void createHeader(Composite parent) {
        headerComposite = new Composite(parent, SWT.NONE);
        GridLayout headerLayout = new GridLayout(2, false);
        headerLayout.marginWidth = 10;
        headerLayout.marginHeight = 5;
        headerComposite.setLayout(headerLayout);
        headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        headerComposite.setBackground(new Color(parent.getDisplay(), HEADER_BACKGROUND));

        // Left side - Conversation label
        conversationLabel = new Label(headerComposite, SWT.NONE);
        conversationLabel.setText("Conversation-" + getCurrentTimestamp());
        conversationLabel.setForeground(new Color(parent.getDisplay(), TEXT_COLOR));
        conversationLabel.setBackground(headerComposite.getBackground());
        conversationLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));

        // Right side - Toolbar with icons
        toolBar = new ToolBar(headerComposite, SWT.FLAT | SWT.RIGHT);
        toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        toolBar.setBackground(headerComposite.getBackground());

        createToolItem(toolBar, "New Chat", "icons/plus.png");
        createToolItem(toolBar, "History", "icons/history.png");
        createToolItem(toolBar, "Settings", "icons/settings.png");
    }

    private void createChatArea(Composite parent) {
        scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
        GridData scrollData = new GridData(SWT.FILL, SWT.FILL, true, true);
        scrolledComposite.setLayoutData(scrollData);
        scrolledComposite.setBackground(new Color(parent.getDisplay(), DARK_BACKGROUND));
        scrolledComposite.setExpandHorizontal(true);
        scrolledComposite.setExpandVertical(true);

        chatContainer = new Composite(scrolledComposite, SWT.NONE);
        chatContainer.setLayout(new GridLayout(1, false));
        chatContainer.setBackground(scrolledComposite.getBackground());

        scrolledComposite.setContent(chatContainer);

        // Add initial assistant message
        addAssistantMessage("I'm your Copilot assistant for Eclipse. How can I help you?");
    }

    private void createInputArea(Composite parent) {
        Composite inputComposite = new Composite(parent, SWT.NONE);
        GridLayout inputLayout = new GridLayout(3, false);
        inputLayout.marginWidth = 10;
        inputLayout.marginHeight = 10;
        inputComposite.setLayout(inputLayout);
        inputComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false));
        inputComposite.setBackground(new Color(parent.getDisplay(), DARK_BACKGROUND));

        // Attach button
        attachButton = new Button(inputComposite, SWT.PUSH | SWT.FLAT);
        attachButton.setImage(Activator.getImageDescriptor("icons/attach.png").createImage());
        attachButton.setBackground(inputComposite.getBackground());
        attachButton.setToolTipText("Attach code from editor");
        attachButton.addListener(SWT.Selection, e -> attachCodeFromEditor());

        // Input field
        inputText = new Text(inputComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
        GridData inputData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        inputData.heightHint = 60; // Increased height for better visibility
        inputData.minimumHeight = 40; // Set minimum height
        inputText.setLayoutData(inputData);
        inputText.setBackground(new Color(parent.getDisplay(), INPUT_BACKGROUND));
        inputText.setForeground(new Color(parent.getDisplay(), TEXT_COLOR));
        inputText.setMessage("Ask Copilot a question or type '/' for commands");

        // Add input field key listeners
        inputText.addListener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
                if ((e.stateMask & SWT.SHIFT) == 0) {
                    e.doit = false;
                    sendMessage();
                }
            }
        });

        // Send button
        sendButton = new Button(inputComposite, SWT.PUSH | SWT.FLAT);
        sendButton.setImage(Activator.getImageDescriptor("icons/send.png").createImage());
        sendButton.setBackground(inputComposite.getBackground());
        sendButton.setToolTipText("Send message (Enter)");
        sendButton.addListener(SWT.Selection, e -> sendMessage());

        // Add status label
        statusLabel = new Label(inputComposite, SWT.NONE);
        GridData statusData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        statusData.horizontalSpan = 3;
        statusLabel.setLayoutData(statusData);
        statusLabel.setBackground(inputComposite.getBackground());
        statusLabel.setForeground(new Color(parent.getDisplay(), TEXT_COLOR));
        updateRateLimitStatus();
    }

    private void createToolItem(ToolBar toolBar, String tooltip, String iconPath) {
        ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        item.setImage(Activator.getImageDescriptor(iconPath).createImage());
        item.setToolTipText(tooltip);

        switch (tooltip) {
            case "New Chat":
                item.addListener(SWT.Selection, e -> startNewChat());
                break;
            case "History":
                item.addListener(SWT.Selection, e -> showHistory());
                break;
            case "Settings":
                item.addListener(SWT.Selection, e -> openSettings());
                break;
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("MM-dd-HH:mm:ss").format(new Date());
    }

    private void addAssistantMessage(String message) {
        Display.getDefault().asyncExec(() -> {
            Composite messageComposite = new Composite(chatContainer, SWT.NONE);
            GridLayout messageLayout = new GridLayout(1, false);
            messageLayout.marginWidth = 10;
            messageLayout.marginHeight = 10;
            messageComposite.setLayout(messageLayout);
            messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            messageComposite.setBackground(new Color(chatContainer.getDisplay(), ASSISTANT_MESSAGE_BG));

            // Create a composite for the message content
            Composite contentComposite = new Composite(messageComposite, SWT.NONE);
            GridLayout contentLayout = new GridLayout(2, false);
            contentLayout.marginWidth = 0;
            contentLayout.marginHeight = 0;
            contentComposite.setLayout(contentLayout);
            contentComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            contentComposite.setBackground(messageComposite.getBackground());

            // Add text
            StyledText text = new StyledText(contentComposite, SWT.WRAP | SWT.READ_ONLY);
            text.setText(message);
            text.setForeground(new Color(chatContainer.getDisplay(), TEXT_COLOR));
            text.setBackground(contentComposite.getBackground());
            text.setWordWrap(true);

            // Set the font
            text.setFont(new Font(chatContainer.getDisplay(), "Arial", 10, SWT.NORMAL));

            // Configure text layout
            GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            textGridData.widthHint = chatContainer.getSize().x - 60; // Account for margins and copy button
            text.setLayoutData(textGridData);

            // Add copy button
            Button copyButton = new Button(contentComposite, SWT.PUSH);
            copyButton.setImage(Activator.getImageDescriptor("icons/copy.png").createImage());
            copyButton.setToolTipText("Copy to clipboard");
            copyButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
            copyButton.addListener(SWT.Selection, e -> {
                clipboard.setContents(new Object[]{text.getText()}, new Transfer[]{TextTransfer.getInstance()});
                copyButton.setToolTipText("Copied!");
                Display.getDefault().timerExec(2000, () -> {
                    if (!copyButton.isDisposed()) {
                        copyButton.setToolTipText("Copy to clipboard");
                    }
                });
            });

            // Add resize listener to parent composite
            Composite parent = chatContainer.getParent();
            parent.addListener(SWT.Resize, e -> {
                if (!text.isDisposed()) {
                    textGridData.widthHint = chatContainer.getSize().x - 60;
                    text.getParent().layout(true, true);
                    messageComposite.layout(true, true);
                    chatContainer.layout(true, true);
                }
            });

            chatContainer.layout(true, true);
            updateScroll();
        });
    }

    private void addUserMessage(String message) {
        Display.getDefault().asyncExec(() -> {
            Composite messageComposite = new Composite(chatContainer, SWT.NONE);
            GridLayout messageLayout = new GridLayout(1, false);
            messageLayout.marginWidth = 10;
            messageLayout.marginHeight = 10;
            messageComposite.setLayout(messageLayout);
            messageComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            messageComposite.setBackground(new Color(chatContainer.getDisplay(), USER_MESSAGE_BG));

            StyledText text = new StyledText(messageComposite, SWT.WRAP | SWT.READ_ONLY);
            text.setText(message);
            text.setForeground(new Color(chatContainer.getDisplay(), TEXT_COLOR));
            text.setBackground(messageComposite.getBackground());
            text.setWordWrap(true);
            text.setFont(new Font(chatContainer.getDisplay(), "Arial", 10, SWT.NORMAL));

            GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            textGridData.widthHint = chatContainer.getSize().x - 20; // Account for margins
            text.setLayoutData(textGridData);

            // Add resize listener to parent composite
            Composite parent = chatContainer.getParent();
            parent.addListener(SWT.Resize, e -> {
                if (!text.isDisposed()) {
                    textGridData.widthHint = chatContainer.getSize().x - 20;
                    messageComposite.layout(true, true);
                    chatContainer.layout(true, true);
                }
            });

            chatContainer.layout(true, true);
            updateScroll();
        });
    }

    private void updateScroll() {
        scrolledComposite.setMinSize(chatContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        chatContainer.layout(true, true);
        scrolledComposite.getVerticalBar().setSelection(scrolledComposite.getVerticalBar().getMaximum());
    }

    private void sendMessage() {
        String message = inputText.getText().trim();
        if (!message.isEmpty()) {
            setUIEnabled(false);
            addUserMessage(message);
            inputText.setText("");

            // Send to Groq API and handle response
            codeGenerationService.sendToGroq(message)
                .thenAccept(response -> {
                    Display.getDefault().asyncExec(() -> {
                        if (!isDisposed()) {
                            addAssistantMessage(response);
                            setUIEnabled(true);
                            // Save to chat history
                            ChatHistory.addMessageToConversation(currentConversationId, "user", message);
                            ChatHistory.addMessageToConversation(currentConversationId, "assistant", response);
                        }
                    });
                })
                .exceptionally(e -> {
                    handleError(e);
                    return null;
                });
        }
    }

    private void setupAutoCompletion() {
        // Initialize common code snippets and commands
        autoCompleteProposals.addAll(List.of(
            "Generate a Java class for ",
            "Create a unit test for ",
            "Refactor this code to ",
            "Analyze this code and ",
            "Implement a method that ",
            "Fix this bug: ",
            "Optimize this code: ",
            "Add documentation to ",
            "Convert this code to ",
            "Explain how this works: "
        ));

        // Create content proposal adapter
        SimpleContentProposalProvider proposalProvider = new SimpleContentProposalProvider(
            autoCompleteProposals.toArray(new String[0])
        );
        proposalProvider.setFiltering(true);

        // Create key binding for Ctrl+Space
        KeyStroke keyStroke = KeyStroke.getInstance(SWT.CTRL, SWT.SPACE);
        char[] autoActivationChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

        proposalAdapter = new ContentProposalAdapter(
            inputText,
            new TextContentAdapter(),
            proposalProvider,
            keyStroke,  // Ctrl+Space to activate
            autoActivationChars  // Auto-activate on any letter
        );

        // Configure proposal popup
        proposalAdapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
        proposalAdapter.setPopupSize(new Point(300, 200));
        proposalAdapter.setAutoActivationDelay(500); // 500ms delay before showing proposals
    }

    private void loadChatHistory() {
        Display.getDefault().asyncExec(() -> {
            // Clear existing content
            for (Control child : chatContainer.getChildren()) {
                child.dispose();
            }

            // Load and display messages
            List<ChatHistory.Message> messages = ChatHistory.getConversationMessages(currentConversationId);
            for (ChatHistory.Message msg : messages) {
                if ("user".equals(msg.role)) {
                    addUserMessage(msg.content);
                } else {
                    addAssistantMessage(msg.content);
                }
            }

            chatContainer.layout(true, true);
            updateScroll();
        });
    }

    private void setUIEnabled(boolean enabled) {
        Display.getDefault().asyncExec(() -> {
            if (!isDisposed()) {
                inputText.setEnabled(enabled);
                if (!enabled) {
                    statusLabel.setText("Processing request...");
                } else {
                    updateRateLimitStatus();
                }
            }
        });
    }

    private void handleError(Throwable e) {
        String errorMessage;
        if (e.getMessage() != null && e.getMessage().contains("rate limit exceeded")) {
            errorMessage = "Rate limit exceeded. Please wait for about an hour before trying again.\n\n" +
                          "This helps ensure fair usage of the AI service for all users.";
            RateLimitManager.getInstance().handleRateLimit();
        } else {
            errorMessage = "An error occurred:\n" + e.getMessage();
        }

        Display.getDefault().asyncExec(() -> {
            if (!isDisposed()) {
                setUIEnabled(true);
                MessageDialog.openError(
                    getSite().getShell(),
                    "Error",
                    errorMessage
                );
                addAssistantMessage("Error: " + errorMessage);
            }
        });
    }

    private boolean isDisposed() {
        return inputText == null || inputText.isDisposed();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public void setFocus() {
        inputText.setFocus();
    }

    private void updateRateLimitStatus() {
        if (statusLabel == null || statusLabel.isDisposed()) {
            return;
        }

        RateLimitManager rateLimitManager = RateLimitManager.getInstance();
        if (rateLimitManager.isRateLimited()) {
            long seconds = rateLimitManager.getSecondsUntilReset();
            String message = String.format(
                "Rate limited - %d:%02d remaining",
                seconds / 60, seconds % 60);
            statusLabel.setText(message);
            statusLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
        } else {
            statusLabel.setText("Ready");
            statusLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
        }
    }

    public CompletableFuture<Void> processGenerateRequest() {
        String message = inputText.getText().trim();
        if (message.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        setUIEnabled(false);
        addUserMessage(message);
        inputText.setText("");

        return codeGenerationService.generateCode(message)
            .thenAccept(response -> {
                Display.getDefault().asyncExec(() -> {
                    if (!isDisposed()) {
                        addAssistantMessage(response);
                        setUIEnabled(true);
                    }
                });
            })
            .exceptionally(e -> {
                handleError(e);
                return null;
            });
    }

    public CompletableFuture<Void> processAnalyzeRequest() {
        IEditorPart editor = PlatformUI.getWorkbench()
                                     .getActiveWorkbenchWindow()
                                     .getActivePage()
                                     .getActiveEditor();

        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            ISelection selection = textEditor.getSelectionProvider().getSelection();

            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                String selectedText = textSelection.getText();

                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    setUIEnabled(false);
                    String prompt = "Please analyze this code:\n\n" + selectedText;
                    addUserMessage(prompt);

                    return codeGenerationService.analyzeCode(selectedText)
                        .thenAccept(response -> {
                            Display.getDefault().asyncExec(() -> {
                                if (!isDisposed()) {
                                    addAssistantMessage(response);
                                    setUIEnabled(true);
                                }
                            });
                        })
                        .exceptionally(e -> {
                            handleError(e);
                            return null;
                        });
                }
            }
        }

        Display.getDefault().asyncExec(() -> {
            MessageDialog.openInformation(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "No Code Selected",
                "Please select some code in the editor to analyze."
            );
        });

        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> processRefactorRequest() {
        IEditorPart editor = PlatformUI.getWorkbench()
                                     .getActiveWorkbenchWindow()
                                     .getActivePage()
                                     .getActiveEditor();

        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            ISelection selection = textEditor.getSelectionProvider().getSelection();

            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                String selectedText = textSelection.getText();

                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    setUIEnabled(false);
                    String prompt = "Please suggest refactoring for this code:\n\n" + selectedText;
                    addUserMessage(prompt);

                    return codeGenerationService.refactorCode(selectedText)
                        .thenAccept(response -> {
                            Display.getDefault().asyncExec(() -> {
                                if (!isDisposed()) {
                                    addAssistantMessage(response);
                                    setUIEnabled(true);
                                }
                            });
                        })
                        .exceptionally(e -> {
                            handleError(e);
                            return null;
                        });
                }
            }
        }

        Display.getDefault().asyncExec(() -> {
            MessageDialog.openInformation(
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                "No Code Selected",
                "Please select some code in the editor to refactor."
            );
        });

        return CompletableFuture.completedFuture(null);
    }

    private void copyToClipboard(String text) {
        if (clipboard != null && !clipboard.isDisposed()) {
            TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents(new Object[] { text }, new Transfer[] { textTransfer });
        }
    }

    private void downloadConversation() {
        // Create conversation content
        StringBuilder content = new StringBuilder();
        content.append("Conversation: ").append(conversationLabel.getText()).append("\n\n");

        for (Control control : chatContainer.getChildren()) {
            if (control instanceof Composite) {
                Composite messageComposite = (Composite) control;
                StyledText messageText = null;

                for (Control child : messageComposite.getChildren()) {
                    if (child instanceof StyledText) {
                        messageText = (StyledText) child;
                        break;
                    }
                }

                if (messageText != null) {
                    String role = messageComposite.getData("type") == "assistant" ? "Assistant" : "User";
                    content.append(role).append(": ").append(messageText.getText()).append("\n\n");
                }
            }
        }

        // Copy to clipboard
        copyToClipboard(content.toString());
        MessageDialog.openInformation(
            getSite().getShell(),
            "Conversation Downloaded",
            "Conversation has been copied to clipboard"
        );
    }

    private void startNewChat() {
        // Clear the chat container
        for (Control control : chatContainer.getChildren()) {
            control.dispose();
        }

        // Create new conversation ID
        currentConversationId = UUID.randomUUID().toString();
        conversationLabel.setText("Conversation-" + getCurrentTimestamp());

        // Add initial message
        addAssistantMessage("I'm your Copilot assistant for Eclipse. How can I help you?");

        // Save new conversation to history
        ChatHistory.createNewConversation(currentConversationId, conversationLabel.getText());

        chatContainer.layout(true, true);
        updateScroll();
    }

    private void openSettings() {
        // Open Eclipse preferences page for the plugin
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
            shell,
            "com.example.myplugin.preferences.MainPreferencePage",
            new String[] { "com.example.myplugin.preferences.MainPreferencePage" },
            null);
        if (dialog != null) {
            dialog.open();
        }
    }

    private void attachCodeFromEditor() {
        IEditorPart editor = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow()
            .getActivePage()
            .getActiveEditor();

        if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            ISelection selection = textEditor.getSelectionProvider().getSelection();

            if (selection instanceof ITextSelection) {
                ITextSelection textSelection = (ITextSelection) selection;
                String selectedText = textSelection.getText();

                if (selectedText != null && !selectedText.trim().isEmpty()) {
                    String currentText = inputText.getText();
                    String codeBlock = "\n```java\n" + selectedText + "\n```\n";

                    if (currentText.isEmpty()) {
                        inputText.setText(codeBlock);
                    } else {
                        inputText.setText(currentText + "\n" + codeBlock);
                    }

                    inputText.setFocus();
                } else {
                    MessageDialog.openInformation(
                        getSite().getShell(),
                        "No Code Selected",
                        "Please select some code in the editor first."
                    );
                }
            }
        } else {
            MessageDialog.openInformation(
                getSite().getShell(),
                "No Editor Active",
                "Please open and select code in a Java editor."
            );
        }
    }

    private void showHistory() {
        // Clear current chat
        for (Control control : chatContainer.getChildren()) {
            control.dispose();
        }

        // Update conversation label
        conversationLabel.setText("Chat History");

        // Add history entries
        List<String> conversations = ChatHistory.getAllConversations();
        for (String conversationId : conversations) {
            Composite entryComposite = new Composite(chatContainer, SWT.NONE);
            entryComposite.setLayout(new GridLayout(1, false));
            entryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            entryComposite.setBackground(chatContainer.getBackground());

            Label titleLabel = new Label(entryComposite, SWT.NONE);
            titleLabel.setText(ChatHistory.getConversationTitle(conversationId));
            titleLabel.setForeground(new Color(chatContainer.getDisplay(), TEXT_COLOR));
            titleLabel.setBackground(entryComposite.getBackground());
            titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            titleLabel.setFont(new Font(chatContainer.getDisplay(), "Arial", 12, SWT.NORMAL));

            // Add hover effect
            titleLabel.addListener(SWT.MouseEnter, e ->
                titleLabel.setBackground(new Color(chatContainer.getDisplay(), new RGB(60, 60, 60))));
            titleLabel.addListener(SWT.MouseExit, e ->
                titleLabel.setBackground(entryComposite.getBackground()));

            // Add click handler
            final String id = conversationId;
            titleLabel.addListener(SWT.MouseDown, e -> loadConversation(id));

            Label separator = new Label(entryComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            separator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        }

        chatContainer.layout(true, true);
        updateScroll();

        // Disable input while showing history
        setUIEnabled(false);
        inputText.setEnabled(false);
        sendButton.setEnabled(false);
        attachButton.setEnabled(false);
    }

    private void loadConversation(String conversationId) {
        // Clear current chat
        for (Control control : chatContainer.getChildren()) {
            control.dispose();
        }

        // Set current conversation
        currentConversationId = conversationId;
        conversationLabel.setText(ChatHistory.getConversationTitle(conversationId));

        // Load and display messages
        List<ChatHistory.Message> messages = ChatHistory.getConversationMessages(conversationId);
        for (ChatHistory.Message msg : messages) {
            if ("user".equals(msg.role)) {
                addUserMessage(msg.content);
            } else {
                addAssistantMessage(msg.content);
            }
        }

        // Re-enable UI
        setUIEnabled(true);
        inputText.setEnabled(true);
        sendButton.setEnabled(true);
        attachButton.setEnabled(true);

        chatContainer.layout(true, true);
        updateScroll();
    }
}
