/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package com.chiorichan.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * Wraps a ByteBuffer in an OutputStream for writing
 */
public class ByteBufferOutputStream extends OutputStream
{
	ByteBuffer buf;
	
	public ByteBufferOutputStream( ByteBuffer buf )
	{
		this.buf = buf;
	}
	
	public void write( int b ) throws IOException
	{
		buf.put( ( byte ) b );
	}
	
	public void write( byte[] bytes, int off, int len ) throws IOException
	{
		buf.put( bytes, off, len );
	}
}
