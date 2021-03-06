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

public class UtilNumbers
{
	private UtilNumbers()
	{

	}

	public static boolean isNumber( String value )
	{
		try
		{
			Integer.parseInt( value );
			return true;
		}
		catch ( NumberFormatException e )
		{
			return false; // String is not a number, auto disqualified
		}
	}
}
