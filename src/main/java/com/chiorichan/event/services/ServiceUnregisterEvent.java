package com.chiorichan.event.services;

import com.chiorichan.services.RegisteredServiceProvider;
import com.chiorichan.services.ServiceProvider;

/**
 * This event is called when a service is unregistered.
 * <p>
 * Warning: The order in which register and unregister events are called should not be relied upon.
 */
public class ServiceUnregisterEvent<T> extends ServiceEvent<T>
{
	public ServiceUnregisterEvent( RegisteredServiceProvider<T, ServiceProvider> serviceProvider )
	{
		super( serviceProvider );
	}
}