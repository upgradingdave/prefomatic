package com.upgradingdave.prefs;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class PrefsHelper {

	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// DO NOT INSTANTIATE A LOGGER HERE.
	// If you get a log4j logger here, it will not allow PrefsHelper to use an
	// external file to load an external log4j properties file!!
	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// private static Logger log = LoggerHelper.getLogger();
    
        private static Configuration prefomaticConfig; 

	private static Configuration config;
	private static String externalFilePath;
	private static String externalFolderPath;
	
	/**
	 * perfomatic looks at perfomatic.properties to bootstrap itself and 
	 * figure out the name of the os env variable to use. 
	 */
	private static Configuration getPrefomaticConfig(){
	    synchronized (PrefsHelper.class) {
            if (prefomaticConfig == null) {
                try {
                    prefomaticConfig = new PropertiesConfiguration("prefomatic.properties");
                } catch (ConfigurationException e) {
                    // Unfortunately, we can't use log4j here because we need
                    // PrefsHelper to initialize it for us
                    System.out.println(e);
                }
            }
	    }
	    return prefomaticConfig;
	}
	
	public static String getOSEnvVariableName(){
            Configuration prefConfig = getPrefomaticConfig();
            if(prefConfig == null){
                //prefomatic.properties doesn't exist, so there's no
                //custom OS ENV VAR Name defined. So Use default.
                return "CUSTOM_JAVA";
            }
	    return prefConfig.getString("prefomatic.env_var_name", "CUSTOM_JAVA");
	}

	/**
	 * You have to call this method at least once before you can use the
	 * PrefsHelper. In most cases, a startup servlet will call this method, and
	 * then the PrefsHelper.getConfig() can be used everywhere else in the code.
	 * 
	 * @param appName
	 *            PrefsHelper will look for properties files named after you
	 *            application. For example, pass tweetclient here will cause the
	 *            PrefsHelper to look for tweetclient.properties
	 */
	public static Configuration getConfig(String appName) {
		return getConfig(appName, false);
	}

	/**
	 * You will probably need to call this method explicitly very often. Probably
	 * can use getConfig(appname) most of the time. But if you need to
	 * re-initialize the PrefsHelper to use a different properties file, then
	 * you can call this method and set force_reload to true.
	 * 
	 * @param appName
	 * @param force_reload
	 * @return
	 */
	public static Configuration getConfig(String appName, boolean force_reload) {
		synchronized (PrefsHelper.class) {
			if (config == null || force_reload) {
				try {
					config = createConfiguration(appName);
				} catch (ConfigurationException e) {
					// Unfortunately, we can't use log4j here because we need
					// PrefsHelper to initialize it for us
					System.out.println(e);
				}
			}
		}
		return getConfig();
	}

	/**
	 * Returns the path to the external properties file
	 * 
	 * @throws ConfigurationException
	 */
	public static String getExternalPropertiesFilePath()
			throws ConfigurationException {
		if (externalFilePath == null) {
			throw new ConfigurationException(getConfigureExceptionMessage());
		}
		return externalFilePath;
	}

	/**
	 * Returns the external directory path that the PrefsHelper looks in for
	 * properties files.
	 * 
	 * @throws ConfigurationException
	 */
	public static String getExternalPropertiesFolderPath()
			throws ConfigurationException {
		if (externalFolderPath == null) {
			throw new ConfigurationException(getConfigureExceptionMessage());
		}

		File check = new File(externalFolderPath);
		if (!check.exists()) {
			throw new ConfigurationException(
					"Prefomatic OS Environment Variable is set up so that the app folder should be: '"
							+ externalFolderPath
							+ "'. But that folder doesn't exist.");
		}
		return externalFolderPath;
	}

	public static String getConfigureExceptionMessage() {
		return "External Properties file path is not available. Check that environment variable defined in prefomatic.properties is setup";
	}

	/**
	 * Singleton access point to PrefsManager. Use PrefsManager to retrieve
	 * preferences for your app.
	 */
	public static Configuration getConfig() {
		if (config == null) {
			// Unfortunately, we can't use log4j here because we need
			// PrefsHelper to initialize it for us
			System.out
					.println("You must call PrefsHelper.getConfig(String AppName) at least once inside an application. Usually a startup servlet does this");
		}
		return config;
	}

	private static Configuration createConfiguration(String appName)
			throws ConfigurationException {
	    /*
	     * first determine the name of OS ENV VAR
	     */
	    String envVarName = getOSEnvVariableName();
	    
		/*
		 * load external properties file if one exists
		 */
		String propFileName = appName + ".properties";
		String homePath = System.getenv(envVarName);
		String appPath = homePath + "/" + appName;
		Configuration externalConfig = null;
		if (homePath != null && homePath.length() > 0) {
			// Unfortunately, we can't use log4j here because we need
			// PrefsHelper to initialize it for us
			System.out.println("Found os environment variable named "
					+ envVarName + ", with value: " + homePath);
			externalFolderPath = appPath;
			String externalPropFile = appPath + "/" + propFileName;
			try {
				externalFilePath = externalPropFile;
				externalConfig = new PropertiesConfiguration(externalPropFile);
				((PropertiesConfiguration) externalConfig)
						.setReloadingStrategy(new FileChangedReloadingStrategy());
			} catch (ConfigurationException e) {
				// Unfortunately, we can't use log4j here because we need
				// PrefsHelper to initialize it for us
				System.out
						.println("Tried using environment variable, but unable to find "
								+ externalPropFile);
			}

		} else {
			// Unfortunately, we can't use log4j here because we need
			// PrefsHelper to initialize it for us
			System.out.println("Couldn't find OS evironment variable named "
					+ envVarName
					+ ", so will attempt to find properties file on classpath");
		}

		/*
		 * Now load up classpath properties file
		 */
		Configuration classpathConfig = null;
		try {
			classpathConfig = new PropertiesConfiguration(propFileName);
		} catch (ConfigurationException e) {
			// Unfortunately, we can't use log4j here because we need
			// PrefsHelper to initialize it for us
			System.out.println("Unable to find " + propFileName
					+ " file inside classpath");
		}

		/*
		 * I was originally trying to be slick and use "CompositeConfiguration".
		 * But it doesn't work as I'd hoped. So, now if there's an external
		 * file, then it is used. If there's no external file, then classpath is
		 * used. If there's neither, then we throw an error.
		 */
		if (externalConfig != null) {
			return externalConfig;
		}

		if (classpathConfig != null) {
			return classpathConfig;
		} else {
			throw new ConfigurationException(
					"Unable to find properties file named "
							+ propFileName
							+ ". Couldn't find it in classpath or using os env variable");
		}
		
	}
}
