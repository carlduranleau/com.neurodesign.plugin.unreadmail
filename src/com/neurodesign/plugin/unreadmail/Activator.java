package com.neurodesign.plugin.unreadmail;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.neurodesign.plugin.unreadmail";

	// The shared instance
	private static Activator plugin;

	// Preferences
	public static final String PREF_SORT = "sorttype";
	public static final String PREF_SORT_TITLE = "Ordre de tri";
	public static final String [][] PREF_SORT_CHOICES = {{"Croissant", "asc"}, {"Décroissant", "desc"}};
	public static final String PREF_SORT_DEFAULT = "desc";

	public static final String PREF_REFRESH = "refreshspeed";
	public static final String PREF_REFRESH_TITLE = "Vitesse de rafraichissement (en secondes)";
	public static final String [][] PREF_REFRESH_SPEED_CHOICES = {{"10", "10"}, {"30", "30"}, {"60", "60"}, {"300", "300"}};
	public static final String PREF_REFRESH_DEFAULT = "10";
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(PREF_SORT, PREF_SORT_DEFAULT);
		store.setDefault(PREF_REFRESH, PREF_REFRESH_DEFAULT);
	}
	
	public String getRefreshSpeed () {
		return getPreferenceStore().getString(PREF_REFRESH);
	}

	public String getSortType () {
		return getPreferenceStore().getString(PREF_SORT);
	}
}
