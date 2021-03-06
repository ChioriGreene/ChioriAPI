/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.configuration.apache;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ApacheDirectiveComparator implements Comparator<ApacheDirective>
{
	private static final List<String> KEY_ORDER = new LinkedList<String>()
	{
		{
			add( "Directory" );
			add( "DirectoryMatch" );
			add( "Files" );
			add( "FilesMatch" );
			add( "Location" );
			add( "LocationMatch" );
			add( "If" );
		}
	};

	@Override
	public int compare( ApacheDirective left, ApacheDirective right )
	{
		int li = KEY_ORDER.indexOf( left.getKey() );
		int ri = KEY_ORDER.indexOf( right.getKey() );

		if ( li == ri )
			return 0;
		if ( li < 0 )
			return -1;
		if ( ri < 0 )
			return 1;
		if ( li < ri )
			return -1;
		if ( ri < li )
			return 1;
		return 0;
	}
}
