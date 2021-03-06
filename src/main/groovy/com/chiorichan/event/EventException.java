/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.event;

import com.chiorichan.lang.ApplicationException;
import com.chiorichan.lang.ExceptionContext;
import com.chiorichan.lang.ExceptionReport;
import com.chiorichan.lang.ReportingLevel;

public class EventException extends ApplicationException
{
	private static final long serialVersionUID = 3532808232324183999L;

	/**
	 * Constructs a new EventException
	 */
	public EventException()
	{
		super( ReportingLevel.E_ERROR );
	}

	/**
	 * Constructs a new EventException with the given message
	 *
	 * @param message
	 *             The message
	 */
	public EventException( String message )
	{
		super( ReportingLevel.E_ERROR, message );
	}

	/**
	 * Constructs a new EventException with the given message
	 *
	 * @param cause
	 *             The exception that caused this
	 * @param message
	 *             The message
	 */
	public EventException( String message, Throwable cause )
	{
		super( ReportingLevel.E_ERROR, message, cause );
	}

	/**
	 * Constructs a new EventException based on the given Exception
	 *
	 * @param cause
	 *             Exception that triggered this Exception
	 */
	public EventException( Throwable cause )
	{
		super( ReportingLevel.E_ERROR, cause );
	}

	@Override
	public ReportingLevel handle( ExceptionReport report, ExceptionContext context )
	{
		return null;
	}
}
