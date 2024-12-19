package com.copilot.groq;

import static com.copilot.groq.preferences.PreferenceConstants.P_API_KEY;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "com.example.myplugin";
    private static Activator plugin;
    private ScopedPreferenceStore preferenceStore;

    public Activator() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        initializePreferences();
    }

    private void initializePreferences() {
        IPreferenceStore store = getPreferenceStore();
        if (store.getString(P_API_KEY).isEmpty()) {
            store.setDefault(P_API_KEY, "");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    public static Activator getDefault() {
        return plugin;
    }

    public static org.eclipse.jface.resource.ImageDescriptor getImageDescriptor(String path) {
        return org.eclipse.jface.resource.ImageDescriptor.createFromURL(
            getDefault().getBundle().getEntry(path)
        );
    }

    public static String getApiKey() {
        String apiKey = getDefault().getPreferenceStore().getString(P_API_KEY);
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("Groq API key not configured. Please set it in Window > Preferences > Groq Assistant.");
        }
        return apiKey;
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        if (preferenceStore == null) {
            // Try configuration scope first for persistence across workspaces
            preferenceStore = new ScopedPreferenceStore(ConfigurationScope.INSTANCE, PLUGIN_ID);

            // Debug logging
            getLog().log(new Status(IStatus.INFO, PLUGIN_ID,
                "Initializing preference store. Config scope API key: " + preferenceStore.getString(P_API_KEY)));

            // If no value exists in configuration scope, check instance scope
            if (preferenceStore.getString(P_API_KEY).isEmpty()) {
                IEclipsePreferences instancePrefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
                String instanceApiKey = instancePrefs.get(P_API_KEY, "");

                // Debug logging
                getLog().log(new Status(IStatus.INFO, PLUGIN_ID,
                    "Instance scope API key: " + instanceApiKey));

                if (!instanceApiKey.isEmpty()) {
                    // Migrate from instance to configuration scope
                    preferenceStore.setValue(P_API_KEY, instanceApiKey);
                    try {
                        preferenceStore.save();
                        getLog().log(new Status(IStatus.INFO, PLUGIN_ID,
                            "Successfully migrated API key to configuration scope"));
                    } catch (IOException e) {
                        getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,
                            "Failed to migrate API key to configuration scope", e));
                    }
                }
            }
        }
        return preferenceStore;
    }

    public static void log(Exception e) {
        getDefault().getLog().log(new org.eclipse.core.runtime.Status(org.eclipse.core.runtime.IStatus.ERROR, PLUGIN_ID, e.getMessage(), e));
    }
}
