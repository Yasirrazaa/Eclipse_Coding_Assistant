package com.copilot.groq.preferences;

import static com.copilot.groq.preferences.PreferenceConstants.DEFAULT_DARK_THEME;
import static com.copilot.groq.preferences.PreferenceConstants.P_API_KEY;
import static com.copilot.groq.preferences.PreferenceConstants.P_DARK_THEME;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;

import com.copilot.groq.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        // Initialize both configuration and instance scope preferences
        IEclipsePreferences configPrefs = ConfigurationScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();

        // Initialize with empty defaults
        configPrefs.put(P_API_KEY, "");
        try {
            configPrefs.flush();
        } catch (BackingStoreException e) {
            Activator.getDefault().getLog().error("Failed to flush configuration preferences", e);
        }

        store.setDefault(P_API_KEY, "");
        store.setDefault(P_DARK_THEME, DEFAULT_DARK_THEME);
    }
}
