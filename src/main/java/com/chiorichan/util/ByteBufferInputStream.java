/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package com.chiorichan.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wraps a ByteBuffer in an InputStream for reading
 */
public class ByteBufferInputStream extends InputStream
{
	ByteBuffer buf;
	
	public ByteBufferInputStream( ByteBuffer buf )
	{
		this.buf = buf;
	}
	
	public int read() throws IOException
	{
		if ( !buf.hasRemaining() )
		{
			return -1;
		}
		return buf.get() & 0xFF;
	}
	
	public int read( byte[] bytes, int off, int len ) throws IOException
	{
		if ( !buf.hasRemaining() )
		{
			return -1;
		}
		
		len = Math.min( len, buf.remaining() );
		buf.get( bytes, off, len );
		return len;
	}
}
