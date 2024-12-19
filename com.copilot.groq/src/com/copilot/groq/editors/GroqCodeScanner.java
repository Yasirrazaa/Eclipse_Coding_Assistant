package com.copilot.groq.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class GroqCodeScanner extends RuleBasedScanner {
    private Color keywordColor;
    private Color defaultColor;

    public GroqCodeScanner() {
        initializeColors();
        initializeRules();
    }

    private void initializeColors() {
        Display display = Display.getCurrent();
        keywordColor = new Color(display, new RGB(127, 0, 85));  // Purple for keywords
        defaultColor = new Color(display, new RGB(0, 0, 0));     // Black for default text
    }

    private void initializeRules() {
        List<IRule> rules = new ArrayList<>();

        // Set default token
        setDefaultReturnToken(new Token(new TextAttribute(defaultColor)));

        setRules(rules.toArray(new IRule[0]));
    }

    public void dispose() {
        if (keywordColor != null) {
			keywordColor.dispose();
		}
        if (defaultColor != null) {
			defaultColor.dispose();
		}
    }
}
