/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.services;

public class RegisteredServiceProvider<T, P extends ServiceProvider> implements Comparable<RegisteredServiceProvider<?, ?>>
{
	private Class<T> service;
	private ObjectContext context;
	private P provider;
	private ServicePriority priority;

	public RegisteredServiceProvider( Class<T> service, P provider, ObjectContext context, ServicePriority priority )
	{
		this.service = service;
		this.context = context;
		this.provider = provider;
		this.priority = priority;
	}

	@Override
	public int compareTo( RegisteredServiceProvider<?, ?> other )
	{
		if ( priority.ordinal() == other.getPriority().ordinal() )
			return 0;
		else
			return priority.ordinal() < other.getPriority().ordinal() ? 1 : -1;
	}

	public ObjectContext getObjectContext()
	{
		return context;
	}

	public ServicePriority getPriority()
	{
		return priority;
	}

	public P getProvider()
	{
		return provider;
	}

	public Class<T> getService()
	{
		return service;
	}
}
