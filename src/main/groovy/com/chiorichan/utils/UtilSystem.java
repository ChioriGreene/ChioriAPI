/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.utils;

import com.chiorichan.AppLoader;
import org.apache.commons.lang3.SystemUtils;
import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;

/**
 * Provides easy access to the server metadata plus operating system and jvm information
 */
public class UtilSystem
{
	private static final String OS = System.getProperty( "os.name" ).toLowerCase();

	/**
	 * Get the Java Binary
	 *
	 * @return the Java Binary location
	 */
	public static String getJavaBinary()
	{
		String path = System.getProperty( "java.home" ) + File.pathSeparator + "bin" + File.pathSeparator;

		if ( UtilSystem.isWindows() )
			if ( new File( path + "javaw.exe" ).isFile() )
				return path + "javaw.exe";
			else if ( new File( path + "java.exe" ).isFile() )
				return path + "java.exe";

		return path + "java";
	}

	/**
	 * Get the Java version, e.g., 1.7.0_80
	 *
	 * @return The Java version number
	 */
	public static String getJavaVersion()
	{
		return System.getProperty( "java.version" );
	}

	/**
	 * Get the JVM name
	 *
	 * @return The JVM name
	 */
	public static String getJVMName()
	{
		// System.getProperty("java.vm.name");
		return ManagementFactory.getRuntimeMXBean().getVmName();
	}

	public static String getProcessID()
	{
		// Confirmed working on Debian Linux, Windows?

		String pid = ManagementFactory.getRuntimeMXBean().getName();

		if ( pid != null && pid.contains( "@" ) )
			pid = pid.substring( 0, pid.indexOf( "@" ) );

		return pid;
	}

	/*
	 * Java and JVM Methods
	 */

	/**
	 * Get the system username
	 *
	 * @return The username
	 */
	public static String getUser()
	{
		return System.getProperty( "user.name" );
	}

	/**
	 * Indicates if we are running as either the root user for Unix-like or Administrator user for Windows
	 *
	 * @return True if Administrator or root
	 */
	public static boolean isAdminUser()
	{
		return "root".equalsIgnoreCase( System.getProperty( "user.name" ) ) || "administrator".equalsIgnoreCase( System.getProperty( "user.name" ) );
	}

	/**
	 * Indicates if we are running Mac OS X
	 *
	 * @return True if we are running on Mac
	 */
	public static boolean isMac()
	{
		try
		{
			return SystemUtils.IS_OS_MAC;
		}
		catch ( NoClassDefFoundError e )
		{
			return OS.indexOf( "mac" ) >= 0;
		}
	}

	/**
	 * Indicates if the provided PID is still running, this method is setup to work with both Windows and Linux, might need tuning for other OS's
	 *
	 * @param pid
	 * @return is the provided PID running
	 */
	public static boolean isPIDRunning( int pid ) throws IOException
	{
		String[] cmds;
		if ( isUnixLikeOS() )
			cmds = new String[] {"sh", "-c", "ps -ef | grep " + pid + " | grep -v grep"};
		else
			cmds = new String[] {"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""};

		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec( cmds );

		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader( inputstream );
		BufferedReader bufferedreader = new BufferedReader( inputstreamreader );
		String line;
		while ( ( line = bufferedreader.readLine() ) != null )
			if ( line.contains( " " + pid + " " ) )
				return true;

		return false;
	}

	/**
	 * Only effects Unix-like OS'es (Linux and Mac OS X)
	 * It's possible to give non-root users access to privileged ports but it's very complicated
	 * for java and a technically a security risk if malicious code was ran
	 * but it would be in our interest to find a way to detect such workaround
	 *
	 * @param port
	 *             The port run we would like to check
	 * @return True if the port is under 1024 and we are not running on the root account
	 */
	public static boolean isPrivilegedPort( int port )
	{
		// Privileged Ports only exist on Linux, Unix, and Mac OS X (I might be missing some)
		if ( !isUnixLikeOS() )
			return false;

		// Privileged Port range from 1 to 1024
		if ( port <= 0 || port > 1024 )
			return false;

		// If we are trying to use a privileged port, We need to be running as root
		return !isAdminUser();
	}

	/**
	 * Indicates if we are running on Solaris OS
	 *
	 * @return True if we are running on Solaris
	 */
	public static boolean isSolaris()
	{
		try
		{
			return SystemUtils.IS_OS_SOLARIS;
		}
		catch ( NoClassDefFoundError e )
		{
			return OS.indexOf( "sunos" ) >= 0;
		}
	}

	/**
	 * Indicates if we are running on an Unix-like Operating System, e.g., Linux or Max OS X
	 *
	 * @return True if we are running on an Unix-like OS.
	 */
	public static boolean isUnixLikeOS()
	{
		try
		{
			return SystemUtils.IS_OS_UNIX;
		}
		catch ( NoClassDefFoundError e )
		{
			return OS.indexOf( "nix" ) >= 0 || OS.indexOf( "nux" ) >= 0 || OS.indexOf( "aix" ) > 0;
		}
	}

	/**
	 * Indicates if we are running on a Windows Operating System
	 *
	 * @return True if we are running on Windows OS
	 */
	public static boolean isWindows()
	{
		try
		{
			return SystemUtils.IS_OS_WINDOWS;
		}
		catch ( NoClassDefFoundError e )
		{
			return OS.indexOf( "win" ) >= 0;
		}
	}

	public static boolean terminatePID( int pid ) throws IOException
	{
		String[] cmds;
		if ( isUnixLikeOS() )
			cmds = new String[] {"sh", "-c", "kill -9 " + pid};
		else
			cmds = new String[] {"cmd", "/c", "taskkill /f /pid " + pid};

		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec( cmds );

		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader( inputstream );
		BufferedReader bufferedreader = new BufferedReader( inputstreamreader );
		String line;
		while ( ( line = bufferedreader.readLine() ) != null )
		{
			// TODO Wait until process returns
		}

		try
		{
			Thread.sleep( 1000 );
		}
		catch ( InterruptedException e )
		{
			e.printStackTrace();
		}

		return !isPIDRunning( pid );
	}

	public static String uptime()
	{
		Duration duration = new Duration( System.currentTimeMillis() - AppLoader.startTime );
		PeriodFormatter formatter = new PeriodFormatterBuilder().appendDays().appendSuffix( " Day(s) " ).appendHours().appendSuffix( " Hour(s) " ).appendMinutes().appendSuffix( " Minute(s) " ).appendSeconds().appendSuffix( " Second(s)" ).toFormatter();
		return formatter.print( duration.toPeriod() );
	}

	private UtilSystem()
	{

	}
}
