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


/**
 * Represents a worker thread for the scheduler. This gives information about the Thread object for the task, owner of
 * the task and the taskId. </p> Workers are used to execute async tasks.
 */

public interface Worker
{
	
	/**
	 * Returns the taskId for the task being executed by this worker.
	 * 
	 * @return Task id number
	 */
	int getTaskId();
	
	/**
	 * Returns the TaskCreator that owns this task.
	 * 
	 * @return The TaskCreator that owns the task
	 */
	TaskRegistrar getOwner();
	
	/**
	 * Returns the thread for the worker.
	 * 
	 * @return The Thread object for the worker
	 */
	Thread getThread();
	
}
