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

import java.util.Collection;

/**
 * 
 */
public interface SQLSkelGroupBy<T>
{
	T groupBy( Collection<String> columns );
	
	T groupBy( String... columns );
	
	T groupBy( String column );
}
