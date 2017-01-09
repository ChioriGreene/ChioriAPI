/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package com.chiorichan.event;

/**
 * Thrown when a plugin attempts to interact with the server when it is not enabled
 */
@SuppressWarnings( "serial" )
public class IllegalCreatorAccessException extends RuntimeException
{
	
	/**
	 * Creates a new instance of <code>IllegalPluginAccessException</code> without detail message.
	 */
	public IllegalCreatorAccessException()
	{
	}
	
	/**
	 * Constructs an instance of <code>IllegalPluginAccessException</code> with the specified detail message.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public IllegalCreatorAccessException( String msg )
	{
		super( msg );
	}
}
