/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package joptsimple.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/**
 * Converts command line options to {@link Path} objects and checks the status of the underlying file.
 */
public class PathConverter implements ValueConverter<Path>
{
	private final PathProperties[] pathProperties;

	public PathConverter( PathProperties... pathProperties )
	{
		this.pathProperties = pathProperties;
	}

	@Override
	public Path convert( String value )
	{
		Path path = Paths.get( value );

		if ( pathProperties != null )
			for ( PathProperties each : pathProperties )
				if ( !each.accept( path ) )
					throw new ValueConversionException( message( each.getMessageKey(), path.toString() ) );

		return path;
	}

	private String message( String errorKey, String value )
	{
		ResourceBundle bundle = ResourceBundle.getBundle( "joptsimple.ExceptionMessages" );
		Object[] arguments = new Object[] {value, valuePattern()};
		String template = bundle.getString( PathConverter.class.getName() + "." + errorKey + ".message" );
		return new MessageFormat( template ).format( arguments );
	}

	@Override
	public String valuePattern()
	{
		return null;
	}

	@Override
	public Class<Path> valueType()
	{
		return Path.class;
	}
}
