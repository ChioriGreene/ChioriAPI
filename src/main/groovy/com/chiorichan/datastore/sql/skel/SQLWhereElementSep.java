/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.datastore.sql.skel;

/**
 * 
 */
public enum SQLWhereElementSep
{
	AND( "AND" ), OR( "OR" ), NONE( "" );
	
	private String seq;
	
	SQLWhereElementSep( String seq )
	{
		this.seq = seq;
	}
	
	@Override
	public String toString()
	{
		return seq;
	}
}
