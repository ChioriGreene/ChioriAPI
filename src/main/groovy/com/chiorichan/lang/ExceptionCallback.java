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

/**
 * Provides a callback to when a registered exception is thrown
 */
public interface ExceptionCallback
{
	/**
	 * Called for each registered Exception Callback for handling.
	 *
	 * @param cause   The thrown exception
	 * @param context The thrown context
	 * @return The resulting ErrorReporting level. Returning NULL will, if possible, try the next best matching EvalCallback
	 */
	ReportingLevel callback( Throwable cause, ExceptionReport report, ExceptionContext context );
}
