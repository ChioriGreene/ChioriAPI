/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.account;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import org.apache.commons.lang3.Validate;

import com.chiorichan.permission.PermissibleEntity;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public final class AccountInstance implements Account
{
	/**
	 * Tracks permissibles that are referencing this account
	 */
	private final Set<AccountAttachment> permissibles = Collections.newSetFromMap( new WeakHashMap<AccountAttachment, Boolean>() );

	/**
	 * Account MetaData
	 */
	private final AccountMeta metadata;

	AccountInstance( AccountMeta metadata )
	{
		Validate.notNull( metadata );
		this.metadata = metadata;
	}

	int countAttachments()
	{
		return permissibles.size();
	}

	public Collection<AccountAttachment> getAttachments()
	{
		return Collections.unmodifiableSet( permissibles );
	}

	@Override
	public String getDisplayName()
	{
		return metadata.getDisplayName();
	}

	@Override
	public PermissibleEntity getPermissibleEntity()
	{
		return meta().getPermissibleEntity();
	}

	@Override
	public String getId()
	{
		return metadata.getId();
	}

	public Collection<String> getIpAddresses()
	{
		Set<String> ips = Sets.newHashSet();
		for ( AccountAttachment perm : getAttachments() )
			ips.add( perm.getIpAddress() );
		return ips;
	}

	@Override
	public AccountLocation getLocation()
	{
		return meta().getLocation();
	}

	/**
	 *
	 * @param key
	 *             Metadata key.
	 * @return String
	 *         Returns an empty string if no result.
	 */
	public String getString( String key )
	{
		return getString( key, "" );
	}

	/**
	 * Get a string from the Metadata with a default value
	 *
	 * @param key
	 *             Metadata key.
	 * @param def
	 *             Default value to return if no result.
	 * @return String
	 */
	public String getString( String key, String def )
	{
		if ( !metadata.containsKey( key ) )
			return def;

		return metadata.getString( key );
	}

	@Override
	public AccountInstance instance()
	{
		return this;
	}

	@Override
	public boolean isInitialized()
	{
		return true;
	}

	@Override
	public AccountMeta meta()
	{
		return metadata;
	}

	public void registerAttachment( AccountAttachment attachment )
	{
		if ( !permissibles.contains( attachment ) )
			permissibles.add( attachment );
	}

	@Override
	public String toString()
	{
		return "Account{" + metadata.toString() + ",Attachments{" + Joiner.on( "," ).join( getAttachments() ) + "}}";
	}

	public void unregisterAttachment( AccountAttachment attachment )
	{
		permissibles.remove( attachment );
	}
}
