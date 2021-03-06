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
 * SQL Skel for Order By builder methods
 */
public interface SQLSkelOrderBy<T>
{
	T orderBy( Collection<String> columns, String dir );

	T orderBy( Collection<String> columns );

	T orderBy( String column, String dir );

	T orderBy( String column );

	T orderAsc();

	T orderDesc();

	T rand();

	T rand( boolean rand );
}
