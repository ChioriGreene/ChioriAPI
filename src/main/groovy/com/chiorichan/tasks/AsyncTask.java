/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.tasks;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

class AsyncTask extends Task
{
	private final LinkedList<Worker> workers = new LinkedList<Worker>();
	private final Map<Integer, Task> runners;
	
	AsyncTask( final Map<Integer, Task> runners, final TaskRegistrar creator, final Runnable task, final int id, final long delay )
	{
		super( creator, task, id, delay );
		this.runners = runners;
	}
	
	@Override
	public boolean isSync()
	{
		return false;
	}
	
	@Override
	public void run()
	{
		final Thread thread = Thread.currentThread();
		synchronized ( workers )
		{
			if ( getPeriod() == -2 )
			{
				// Never continue running after cancelled.
				// Checking this with the lock is important!
				return;
			}
			workers.add( new Worker()
			{
				@Override
				public Thread getThread()
				{
					return thread;
				}
				
				@Override
				public int getTaskId()
				{
					return AsyncTask.this.getTaskId();
				}
				
				@Override
				public TaskRegistrar getOwner()
				{
					return AsyncTask.this.getOwner();
				}
			} );
		}
		Throwable thrown = null;
		try
		{
			super.run();
		}
		catch ( final Throwable t )
		{
			thrown = t;
			// throw new UnhandledException( String.format( "TaskCreator %s generated an exception while executing task %s", getOwner().getDescription().getFullName(), getTaskId() ), thrown );
		}
		finally
		{
			// Cleanup is important for any async task, otherwise ghost tasks are everywhere
			synchronized ( workers )
			{
				try
				{
					final Iterator<Worker> workers = this.workers.iterator();
					boolean removed = false;
					while ( workers.hasNext() )
					{
						if ( workers.next().getThread() == thread )
						{
							workers.remove();
							removed = true; // Don't throw exception
							break;
						}
					}
					if ( !removed )
					{
						throw new IllegalStateException( String.format( "Unable to remove worker %s on task %s for %s", thread.getName(), getTaskId(), getOwner().getName() ), thrown ); // We
						// don't want to lose the original exception, if any
					}
				}
				finally
				{
					if ( getPeriod() < 0 && workers.isEmpty() )
					{
						// At this spot, we know we are the final async task being executed!
						// Because we have the lock, nothing else is running or will run because delay < 0
						runners.remove( getTaskId() );
					}
				}
			}
		}
	}
	
	LinkedList<Worker> getWorkers()
	{
		return workers;
	}
	
	@Override
	boolean cancel0()
	{
		synchronized ( workers )
		{
			// Synchronizing here prevents race condition for a completing task
			setPeriod( -2L );
			if ( workers.isEmpty() )
			{
				runners.remove( getTaskId() );
			}
		}
		return true;
	}
}
