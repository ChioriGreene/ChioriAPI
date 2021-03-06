/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.chiorichan.AppConfig;
import com.chiorichan.lang.EnumColor;
import com.chiorichan.utils.UtilStrings;

public class DefaultLogFormatter extends Formatter
{
	public static boolean debugMode = false;
	public static int debugModeHowDeep = 1;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;
	private boolean useColor;

	private boolean formatConfigLoaded = false;

	public DefaultLogFormatter()
	{
		this( true );
	}

	public DefaultLogFormatter( boolean useColor )
	{
		this.useColor = useColor;
		dateFormat = new SimpleDateFormat( "MM-dd" );
		timeFormat = new SimpleDateFormat( "HH:mm:ss.SSS" );
	}

	@Override
	public String format( LogRecord record )
	{
		if ( AppConfig.get().isConfigLoaded() && !formatConfigLoaded )
		{
			dateFormat = new SimpleDateFormat( AppConfig.get().getString( "console.dateFormat", "MM-dd" ) );
			timeFormat = new SimpleDateFormat( AppConfig.get().getString( "console.timeFormat", "HH:mm:ss.SSS" ) );
			formatConfigLoaded = true;
		}

		String style = AppConfig.get().isConfigLoaded() ? AppConfig.get().getString( "console.style", "&r&7[&d%ct&7] %dt %tm [%lv&7]&f" ) : "&r&7%dt %tm [%lv&7]&f";

		Throwable ex = record.getThrown();

		if ( style.contains( "%ct" ) )
		{
			String threadName = Thread.currentThread().getName();

			if ( threadName.length() > 10 )
				threadName = threadName.substring( 0, 2 ) + ".." + threadName.substring( threadName.length() - 6 );
			else if ( threadName.length() < 10 )
				threadName = threadName + UtilStrings.repeat( " ", 10 - threadName.length() );

			style = style.replaceAll( "%ct", threadName );
		}

		style = style.replaceAll( "%dt", dateFormat.format( record.getMillis() ) );
		style = style.replaceAll( "%tm", timeFormat.format( record.getMillis() ) );

		int howDeep = debugModeHowDeep;

		if ( debugMode )
		{
			StackTraceElement[] var1 = Thread.currentThread().getStackTrace();

			for ( StackTraceElement var2 : var1 )
				if ( !var2.getClassName().toLowerCase().contains( "java" ) && !var2.getClassName().toLowerCase().contains( "sun" ) && !var2.getClassName().toLowerCase().contains( "log" ) && !var2.getMethodName().equals( "sendMessage" ) && !var2.getMethodName().equals( "sendRawMessage" ) )
				{
					howDeep--;

					if ( howDeep <= 0 )
					{
						style += " " + var2.getClassName() + "$" + var2.getMethodName() + ":" + var2.getLineNumber();
						break;
					}
				}
		}

		if ( style.contains( "%lv" ) )
			style = style.replaceAll( "%lv", EnumColor.fromLevel( record.getLevel() ) + record.getLevel().getLocalizedName().toUpperCase() );

		style += " " + formatMessage( record );

		if ( !style.endsWith( "\r" ) )
			style += "\n";

		if ( ex != null )
		{
			StringWriter writer = new StringWriter();
			ex.printStackTrace( new PrintWriter( writer ) );
			style += writer;
		}

		if ( !useColor )
			return EnumColor.removeAltColors( style );
		else
			return EnumColor.transAltColors( style );
	}

	public boolean useColor()
	{
		return useColor;
	}
}
