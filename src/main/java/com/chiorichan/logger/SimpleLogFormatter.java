/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class SimpleLogFormatter extends Formatter
{
	public static boolean debugMode = false;
	public static int debugModeHowDeep = 1;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;

	public SimpleLogFormatter()
	{
		dateFormat = new SimpleDateFormat( "MM-dd" );
		timeFormat = new SimpleDateFormat( "HH:mm:ss.SSS" );
	}

	@Override
	public String format( LogRecord record )
	{
		StringBuilder msg = new StringBuilder();
		msg.append( dateFormat.format( record.getMillis() ) );
		msg.append( " " );
		msg.append( timeFormat.format( record.getMillis() ) );
		msg.append( " [" );
		msg.append( record.getLevel().getLocalizedName().toUpperCase() );
		msg.append( "] " );
		msg.append( formatMessage( record ) );

		if ( !msg.toString().endsWith( "\r" ) )
			msg.append( "\n" );

		Throwable ex = record.getThrown();
		if ( ex != null )
		{
			StringWriter writer = new StringWriter();
			ex.printStackTrace( new PrintWriter( writer ) );
			msg.append( writer );
		}

		return msg.toString();
	}
}
