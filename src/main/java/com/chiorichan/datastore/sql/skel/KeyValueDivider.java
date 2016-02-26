/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.datastore.sql.skel;

/**
 * 
 */
public enum KeyValueDivider
{
	EQUAL( "=" ), NOT( "NOT" ), LIKE( "LIKE" ), LESS( "<" ), MORE( ">" );
	
	private String seq;
	
	KeyValueDivider( String seq )
	{
		this.seq = seq;
	}
	
	@Override
	public String toString()
	{
		return seq;
	}
}
