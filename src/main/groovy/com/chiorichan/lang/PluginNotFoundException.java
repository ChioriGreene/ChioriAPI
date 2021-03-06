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
 * Thrown when attempting to load an invalid Plugin file
 */
public class PluginNotFoundException extends PluginException
{
	private static final long serialVersionUID = -8242141640709409544L;
	
	/**
	 * Constructs a new InvalidPluginException
	 */
	public PluginNotFoundException()
	{
		
	}
	
	/**
	 * Constructs a new InvalidPluginException with the specified detail message
	 * 
	 * @param message
	 *            TThe detail message is saved for later retrieval by the getMessage() method.
	 */
	public PluginNotFoundException( final String message )
	{
		super( message );
	}
	
	/**
	 * Constructs a new InvalidPluginException with the specified detail message and cause.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the getMessage() method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the getCause() method). (A null value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public PluginNotFoundException( final String message, final Throwable cause )
	{
		super( message, cause );
	}
	
	/**
	 * Constructs a new InvalidPluginException based on the given Exception
	 * 
	 * @param cause
	 *            Exception that triggered this Exception
	 */
	public PluginNotFoundException( final Throwable cause )
	{
		super( cause );
	}
}
