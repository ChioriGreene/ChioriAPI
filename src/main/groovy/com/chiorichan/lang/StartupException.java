/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.lang;

public class StartupException extends UncaughtException
{
	private static final long serialVersionUID = 1L;

	public StartupException( String msg )
	{
		super( ReportingLevel.E_ERROR, msg );
	}

	public StartupException( String msg, Throwable e ) throws UncaughtException
	{
		super( ReportingLevel.E_ERROR, msg, e, true );
	}

	public StartupException( Throwable e ) throws UncaughtException
	{
		super( ReportingLevel.E_ERROR, e, true );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		report.addException( ReportingLevel.E_ERROR, this );
		return ReportingLevel.E_ERROR;
	}
}
