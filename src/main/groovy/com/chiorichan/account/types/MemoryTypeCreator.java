/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.account.types;

import com.chiorichan.ApplicationTerminal;
import com.chiorichan.account.AccountContext;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.lang.AccountResolveResult;
import com.chiorichan.account.lang.AccountResult;
import com.chiorichan.event.EventBus;
import com.chiorichan.event.EventHandler;
import com.chiorichan.event.Listener;
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.permission.PermissionDefault;
import com.chiorichan.permission.event.PermissibleEntityEvent;
import com.chiorichan.permission.event.PermissibleEntityEvent.Action;
import com.chiorichan.tasks.Timings;

import java.util.Arrays;
import java.util.List;

/**
 * Handles Memory Accounts, e.g., Root and None
 */
public class MemoryTypeCreator extends AccountTypeCreator implements Listener
{
	public static final MemoryTypeCreator INSTANCE = new MemoryTypeCreator();

	MemoryTypeCreator()
	{
		super();

		EventBus.instance().registerEvents( this, this );
	}

	@Override
	public AccountContext createAccount( String locId, String acctId )
	{
		AccountContext context = new AccountContextImpl( this, AccountType.SQL, acctId, locId );

		context.setValue( "date", Timings.epoch() );

		return context;
	}

	@Override
	public boolean accountExists( String locId, String acctId )
	{
		return "none".equals( acctId ) || "root".equals( acctId );
	}

	@Override
	public void failedLogin( AccountMeta meta, AccountResult result )
	{
		// Do Nothing
	}

	@Override
	public String getDisplayName( AccountMeta meta )
	{
		return null;
	}

	@Override
	public List<String> getLoginKeys()
	{
		return Arrays.asList( new String[] {} );
	}

	public AccountType getType()
	{
		return AccountType.MEMORY;
	}

	@Override
	public boolean isEnabled()
	{
		return true; // Always
	}

	@Override
	public AccountResolveResult resolveAccount( String locId, String acctId )
	{
		return null;
	}

	@EventHandler
	public void onPermissibleEntityEvent( PermissibleEntityEvent event )
	{
		// We do this to prevent the root account from losing it's OP permission node

		if ( event.getAction() == Action.PERMISSIONS_CHANGED )
			if ( AccountType.isRootAccount( event.getEntity() ) )
			{
				event.getEntity().addPermission( PermissionDefault.OP.getNode(), true, null );
				event.getEntity().setVirtual( true );
			}
	}

	@Override
	public void preLogin( AccountMeta meta, AccountPermissible via, String acctId, Object... credentials )
	{
		// Called before the NONE and ROOT Account logs in
	}

	@Override
	public void reload( AccountMeta account )
	{
		// Do Nothing
	}

	@Override
	public void save( AccountContext context )
	{
		// Do Nothing!
	}

	@Override
	public void successInit( AccountMeta meta, PermissibleEntity entity )
	{
		if ( meta.getContext().creator() == this && AccountType.isRootAccount( meta ) )
		{
			entity.addPermission( PermissionDefault.OP.getNode(), true, null );
			entity.setVirtual( true );
			meta.instance().registerAttachment( ApplicationTerminal.terminal() );
		}

		if ( meta.getContext().creator() == this && AccountType.isNoneAccount( meta ) )
			entity.setVirtual( true );
	}

	@Override
	public void successLogin( AccountMeta meta )
	{
		// Do Nothing
	}
}
