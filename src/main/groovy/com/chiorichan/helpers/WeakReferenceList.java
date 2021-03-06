/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.helpers;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Implements a self garbage collecting List, that is Thread-safe
 */
public class WeakReferenceList<V> implements Iterable<V>
{
	private static final ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();
	
	static
	{
		new CleanupThread().start();
	}
	
	private final List<GarbageReference<V>> list = Lists.newCopyOnWriteArrayList();
	
	public void clear()
	{
		list.clear();
	}
	
	public V get( int index )
	{
		return list.get( index ).get();
	}
	
	public void remove( int index )
	{
		list.remove( index );
	}
	
	@SuppressWarnings( "unchecked" )
	public V[] toArray()
	{
		return ( V[] ) toSet().toArray();
	}
	
	public int size()
	{
		return list.size();
	}
	
	public boolean contains( V value )
	{
		for ( GarbageReference<V> ref : list )
			if ( ref.get() == value )
				return true;
		return false;
	}
	
	public void remove( V value )
	{
		for ( GarbageReference<V> ref : list )
			if ( ref.get() == value )
				list.remove( ref );
	}
	
	public Set<V> toSet()
	{
		Set<V> values = Sets.newHashSet();
		for ( GarbageReference<V> v : list )
			values.add( v.get() );
		return values;
	}
	
	@Override
	public Iterator<V> iterator()
	{
		return toSet().iterator();
	}
	
	public void addAll( Iterable<V> values )
	{
		for ( V v : values )
			add( v );
	}
	
	public void add( V value )
	{
		Validate.notNull( value );
		
		GarbageReference<V> reference = new GarbageReference<V>( value, list );
		list.add( reference );
	}
	
	static class GarbageReference<V> extends WeakReference<V>
	{
		final List<GarbageReference<V>> list;
		
		GarbageReference( V value, List<GarbageReference<V>> list )
		{
			super( value, referenceQueue );
			this.list = list;
		}
	}
	
	static class CleanupThread extends Thread
	{
		CleanupThread()
		{
			setPriority( Thread.MAX_PRIORITY );
			setName( "GarbageCollectingList-CleanupThread" );
			// setDaemon( true );
		}
		
		@Override
		public void run()
		{
			while ( true )
			{
				try
				{
					GarbageReference<?> ref;
					while ( true )
					{
						ref = ( GarbageReference<?> ) referenceQueue.remove();
						ref.list.remove( ref );
					}
				}
				catch ( InterruptedException e )
				{
					// ignore
				}
			}
		}
	}
}
