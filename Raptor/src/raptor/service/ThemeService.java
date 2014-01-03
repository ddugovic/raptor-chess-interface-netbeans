package raptor.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.chat.ChatType;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.Logger;

public class ThemeService {
	private static final Logger LOG = Logger
			.getLogger(ThemeService.class);
	public static boolean serviceCreated = false;
	private static final String THEME_SYSTEM_DIR = Raptor.RESOURCES_DIR + "themes";
	private static final String THEME_USER_DIR = Raptor.USER_RAPTOR_HOME_PATH + "/themes";
	private static final String THEME_NAME = "theme-name";
	private static ThemeService singletonInstance;
	private final Map<String, Theme> themeMap = new HashMap<String, Theme>();

	public static class Theme {
		protected String name;
		protected Properties properties = new Properties();
		protected boolean isSystemTheme;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public Properties getProperties() {
			return properties;
		}

		public void setProperties(Properties properties) {
			this.properties = properties;
		}

		public boolean isSystemTheme() {
			return isSystemTheme;
		}

		public void setSystemTheme(boolean isSystemTheme) {
			this.isSystemTheme = isSystemTheme;
		}
	}

	private ThemeService() {
		init();
		serviceCreated = true;
	}

	protected void loadThemes() {
		long startTime = System.currentTimeMillis();
		themeMap.clear();
		Theme[] systemThemes = loadSystemThemes();
		Theme[] userThemes = loadUserThemes();

		for (Theme theme : systemThemes) {
			themeMap.put(theme.getName(), theme);
		}
		for (Theme theme : userThemes) {
			themeMap.put(theme.getName(), theme);
		}
		LOG.info("Loaded " + (systemThemes.length + userThemes.length) + " in "
				+ (System.currentTimeMillis() - startTime) + "ms");
	}

	protected Theme[] loadSystemThemes() {
		File systemThemeDirectory = new File(THEME_SYSTEM_DIR);
		File[] systemThemes = systemThemeDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				return pathName.getName().endsWith(".properties");
			}
		});

		List<Theme> themes = new ArrayList<Theme>(10);

		if (systemThemes != null) {
			for (File file : systemThemes) {
				Theme theme = loadTheme(file);
				if (theme != null) {
					themes.add(theme);
				}
			}
		}
		return themes.toArray(new Theme[0]);
	}

	protected Theme loadTheme(File file) {
		FileInputStream fileIn = null;
		try {
			fileIn = new FileInputStream(file);
			Properties properties = new Properties();
			properties.load(fileIn);
			Theme result = new Theme();
			result.setName(properties.getProperty(THEME_NAME));
			for (Enumeration<Object> e = properties.keys(); e.hasMoreElements();) {
				String key = (String) e.nextElement();
				if (!StringUtils.equals(key, THEME_NAME)) {
					result.getProperties()
							.put(key, properties.getProperty(key));
				}
			}
			return result;

		} catch (Throwable t) {
			Raptor.getInstance().onError(
					"Error loading theme from file: " + file.getAbsolutePath(),
					t);
			return null;
		} finally {
			try {
				fileIn.close();
			} catch (Throwable t) {
			}
		}
	}

	protected Theme[] loadUserThemes() {
		File userThemeDirectory = new File(THEME_USER_DIR);
		File[] userThemes = userThemeDirectory.listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathName) {
				return pathName.getName().endsWith(".properties");
			}
		});

		List<Theme> themes = new ArrayList<Theme>(10);

		if (userThemes != null) {
			for (File file : userThemes) {
				Theme theme = loadTheme(file);
				if (theme != null) {
					themes.add(theme);
				}
			}
		}
		return themes.toArray(new Theme[0]);
	}

	public Theme importTheme(String fileName) {
		Theme theme = loadTheme(new File(fileName));
		saveTheme(theme);
		return theme;
	}
	

	public void applyTheme(Theme theme) {
		// Remove all channel color presets before applying the theme.
		// This is currently not working but I might fix it later, so its
		// commented out.
		// String[] prefNames = Raptor.getInstance().getPreferences()
		// .preferenceNames();
		// for (String name : prefNames) {
		// if
		// (name.startsWith(PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
		// + ChatType.CHANNEL_TELL)
		// && name.endsWith("-color")) {
		// Raptor.getInstance().getPreferences().(name);
		// }
		// }

		// Apply theme.
		for (String propertyName : theme.getProperties().stringPropertyNames()) {
			Raptor.getInstance()
					.getPreferences()
					.setValue(propertyName,
							theme.getProperties().getProperty(propertyName));
		}

		// Save preferences.
		Raptor.getInstance().getPreferences().save();
	}

	public void saveTheme(Theme theme) {
		saveTheme(theme, THEME_USER_DIR);
		themeMap.put(theme.getName(), theme);
	}

	public void saveTheme(Theme theme, String directory) {
		Properties properties = new Properties();
		properties.put(THEME_NAME, theme.getName());
		for (String key : theme.getProperties().stringPropertyNames()) {
			properties.put(key, theme.getProperties().get(key));
		}
		FileOutputStream fileOut = null;
		try {
			fileOut = new FileOutputStream(directory + "/" + theme.getName()
					+ ".properties", false);
			properties.store(fileOut, "Created in Raptor");
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error saving theme: " + theme, t);
		} finally {
			if (fileOut != null) {
				try {
					fileOut.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	public void exportCurrentTheme(String themeName, String directoryName) {
		Theme theme = getCurrentAsTheme(themeName);
		saveTheme(theme, directoryName);
	}

	public Theme getCurrentAsTheme(String name) {
		RaptorPreferenceStore preferences = Raptor.getInstance().getPreferences();
		Theme theme = new Theme();
		theme.setName(name);
		// Board colors
		theme.getProperties().put(
				PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR,
						preferences.getString(PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_BACKGROUND_COLOR,
						preferences.getString(PreferenceKeys.BOARD_BACKGROUND_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_COORDINATES_COLOR,
						preferences.getString(PreferenceKeys.BOARD_COORDINATES_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR,
						preferences.getString(PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR,
						preferences.getString(PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR,
						preferences.getString(PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_COLOR,
						preferences.getString(PreferenceKeys.BOARD_PIECE_JAIL_LABEL_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_CONTROL_COLOR,
						preferences.getString(PreferenceKeys.BOARD_CONTROL_COLOR));
		theme.getProperties().put(
				PreferenceKeys.BOARD_PIECE_JAIL_SHADOW_ALPHA,
						preferences.getString(PreferenceKeys.BOARD_PIECE_JAIL_SHADOW_ALPHA));
		theme.getProperties().put(
				PreferenceKeys.BOARD_PIECE_SHADOW_ALPHA,
						preferences.getString(PreferenceKeys.BOARD_PIECE_SHADOW_ALPHA));
		theme.getProperties().put(
				PreferenceKeys.ARROW_OBS_OPP_COLOR,
						preferences.getString(PreferenceKeys.ARROW_OBS_OPP_COLOR));
		theme.getProperties().put(
				PreferenceKeys.ARROW_PREMOVE_COLOR,
						preferences.getString(PreferenceKeys.ARROW_PREMOVE_COLOR));
		theme.getProperties().put(
				PreferenceKeys.ARROW_MY_COLOR,
						preferences.getString(PreferenceKeys.ARROW_MY_COLOR));
		theme.getProperties().put(
				PreferenceKeys.ARROW_OBS_COLOR,
						preferences.getString(PreferenceKeys.ARROW_OBS_COLOR));
		theme.getProperties().put(
				PreferenceKeys.HIGHLIGHT_PREMOVE_COLOR,
						preferences.getString(PreferenceKeys.HIGHLIGHT_PREMOVE_COLOR));
		theme.getProperties().put(
				PreferenceKeys.HIGHLIGHT_OBS_OPP_COLOR,
						preferences.getString(PreferenceKeys.HIGHLIGHT_OBS_OPP_COLOR));
		theme.getProperties().put(
				PreferenceKeys.HIGHLIGHT_MY_COLOR,
						preferences.getString(PreferenceKeys.HIGHLIGHT_MY_COLOR));
		theme.getProperties().put(
				PreferenceKeys.HIGHLIGHT_OBS_COLOR,
						preferences.getString(PreferenceKeys.HIGHLIGHT_OBS_COLOR));
		theme.getProperties().put(
				PreferenceKeys.RESULTS_COLOR,
						preferences.getString(PreferenceKeys.RESULTS_COLOR));

		// Chat console colors
		theme.getProperties().put(
				PreferenceKeys.CHAT_CONSOLE_BACKGROUND_COLOR,
						preferences.getString(PreferenceKeys.CHAT_CONSOLE_BACKGROUND_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_INPUT_BACKGROUND_COLOR,
						preferences.getString(PreferenceKeys.CHAT_INPUT_BACKGROUND_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_INPUT_DEFAULT_TEXT_COLOR,
						preferences.getString(PreferenceKeys.CHAT_INPUT_DEFAULT_TEXT_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_LINK_UNDERLINE_COLOR,
						preferences.getString(PreferenceKeys.CHAT_LINK_UNDERLINE_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_OUTPUT_BACKGROUND_COLOR,
						preferences.getString(PreferenceKeys.CHAT_OUTPUT_BACKGROUND_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_OUTPUT_TEXT_COLOR,
						preferences.getString(PreferenceKeys.CHAT_OUTPUT_TEXT_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_PROMPT_COLOR,
						preferences.getString(PreferenceKeys.CHAT_PROMPT_COLOR));
		theme.getProperties().put(
				PreferenceKeys.CHAT_QUOTE_UNDERLINE_COLOR,
						preferences.getString(PreferenceKeys.CHAT_QUOTE_UNDERLINE_COLOR));

		// Set all chat types except channel tells.
		for (ChatType type : ChatType.values()) {
			if (type != ChatType.CHANNEL_TELL) {
				String key = preferences.getKeyForChatTypeColor(type);
				String value = preferences.getString(key);
				if (StringUtils.isNotBlank(value)) {
					theme.getProperties().put(key, value);
				}
			}
		}

		// Set channel tell colors.
		for (String key : preferences.preferenceNames()) {
			if (key.startsWith(PreferenceKeys.CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO
					+ ChatType.CHANNEL_TELL)) {
				String value = preferences.getString(key);
				if (StringUtils.isNotBlank(value)) {
					theme.getProperties().put(key, value);
				}
			}
		}
		return theme;
	}

	public Theme saveCurrentAsTheme(String name) {
		Theme theme = getCurrentAsTheme(name);
		saveTheme(theme);
		return theme;
	}

	public String[] getThemeNames() {
		String[] result = themeMap.keySet().toArray(new String[0]);
		Arrays.sort(result);
		return result;

	}

	public Theme getTheme(String name) {
		return themeMap.get(name);
	}

	public static ThemeService getInstance() {
		if (singletonInstance != null)
			return singletonInstance;

		singletonInstance = new ThemeService();
		return singletonInstance;
	}

	public void dispose() {

	}

	protected void init() {
		loadThemes();
	}
}
