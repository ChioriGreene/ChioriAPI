/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan;

import com.chiorichan.logger.Log;
import com.chiorichan.utils.UtilIO;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Versioning
{
	private static Properties metadata;

	static
	{
		loadMetaData( false );
	}

	/**
	 * Get the server build number
	 * The build number is only set when the server is built on our Jenkins Build Server or by Travis,
	 * meaning this will be 0 for all development builds
	 *
	 * @return The server build number
	 */
	public static String getBuildNumber()
	{
		return metadata.getProperty( "project.build", "0" );
	}

	/**
	 * Get the server copyright, e.g., Copyright (c) 2015 Chiori-chan
	 *
	 * @return The server copyright
	 */
	public static String getCopyright()
	{
		return metadata.getProperty( "project.copyright", "Copyright &copy; 2015 Chiori-chan" );
	}

	/**
	 * Get the developer e-mail address
	 * Suggested use is to report problems
	 *
	 * @return The developer e-mail address
	 */
	public static String getDeveloperContact()
	{
		return metadata.getProperty( "project.email", "me@chiorichan.com" );
	}

	/**
	 * Get the GitHub Branch this was built from, e.g., master
	 * Set by the Gradle build script
	 *
	 * @return The GitHub branch
	 */
	public static String getGitHubBranch()
	{
		return metadata.getProperty( "project.branch", "master" );
	}

	/**
	 * Generates a HTML suitable footer for general server info and exception pages
	 *
	 * @return
	 *         HTML footer string
	 */
	public static String getHTMLFooter()
	{
		return "<small>Running <a href=\"https://github.com/ChioriGreene/ChioriWebServer\">" + getProduct() + "</a> Version " + getVersion() + " (Build #" + getBuildNumber() + ")<br />" + getCopyright() + "</small>";
	}

	/**
	 * Get the server product name, e.g., Chiori-chan's Web Server
	 *
	 * @return The Product Name
	 */
	public static String getProduct()
	{
		return metadata.getProperty( "project.name", "Chiori-chan's Web Server" );
	}

	/*
	 * Operating System Methods
	 */

	/**
	 * Get the server product name without spaces or special characters, e.g., ChioriWebServer
	 *
	 * @return The Product Name Simple
	 */
	public static String getProductSimple()
	{
		return metadata.getProperty( "project.name", "ChioriWebServer" ).replaceAll( " ", "" );
	}

	/**
	 * Get the server version, e.g., 9.2.1 (Milky Berry)
	 *
	 * @return The server version with code name
	 */
	public static String getVersion()
	{
		return metadata.getProperty( "project.version", "Unknown-Version" ) + " (" + metadata.getProperty( "project.codename" ) + ")";
	}

	/**
	 * Get the server version number, e.g., 9.2.1
	 *
	 * @return The server version number
	 */
	public static String getVersionNumber()
	{
		return metadata.getProperty( "project.version", "Unknown-Version" );
	}

	/**
	 * Indicates if we are running a development build of the server
	 *
	 * @return True is we are running in development mode
	 */
	public static boolean isDevelopment()
	{
		return "0".equals( getBuildNumber() ) || AppConfig.get() != null && AppConfig.get().getBoolean( "server.developmentMode", false );
	}

	/**
	 * Loads the server metadata from the file {@value "build.properties"},
	 * which is usually updated by our Gradle build script
	 *
	 * @param force
	 *             Force a metadata reload
	 */
	private static void loadMetaData( boolean force )
	{
		if ( metadata != null && !metadata.isEmpty() && !force )
			return;

		metadata = new Properties();

		InputStream is = null;
		try
		{
			is = AppLoader.class.getClassLoader().getResourceAsStream( "build.properties" );
			if ( is == null )
			{
				Log.get().severe( "This application is missing the `build.properties` file, we will now default to the API build properties file." );
				is = AppLoader.class.getClassLoader().getResourceAsStream( "api.properties" );
			}

			metadata.load( is );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
		finally
		{
			UtilIO.closeQuietly( is );
		}
	}
}
