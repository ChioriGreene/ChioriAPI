/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.account.event;

import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.event.Cancellable;
import com.chiorichan.event.Conditional;
import com.chiorichan.event.EventException;
import com.chiorichan.event.RegisteredListener;

/**
 * Stores details for Users attempting to log in
 */
public class AccountPreLoginEvent extends AccountEvent implements Conditional, Cancellable
{
	private AccountDescriptiveReason reason = AccountDescriptiveReason.DEFAULT;
	private final AccountPermissible via;
	private final Object[] credentials;

	public AccountPreLoginEvent( AccountMeta meta, AccountPermissible accountPermissible, String acctId, Object[] credentials )
	{
		super( meta, accountPermissible );
		via = accountPermissible;
		this.credentials = credentials;
	}

	@Override
	public boolean conditional( RegisteredListener context ) throws EventException
	{
		// If the result returned is an error then we skip the remaining EventListeners
		return reason.getReportingLevel().isSuccess();
	}

	/**
	 * Notifies the user that log has failed with the given reason
	 *
	 * @param reason
	 *            fail message to display to the user
	 */
	public void fail( final AccountDescriptiveReason reason )
	{
		this.reason = reason;
	}

	public AccountPermissible getAttachment()
	{
		return via;
	}

	public Object[] getCredentials()
	{
		return credentials;
	}

	/**
	 * Gets the current result of the login, as an enum
	 *
	 * @return Current AccountResult of the login
	 */
	public AccountDescriptiveReason getDescriptiveReason()
	{
		return reason;
	}

	@Override
	public boolean isCancelled()
	{
		return reason == AccountDescriptiveReason.CANCELLED_BY_EVENT;
	}

	@Override
	public void setCancelled( boolean cancel )
	{
		reason = AccountDescriptiveReason.CANCELLED_BY_EVENT;
	}

	/**
	 * Sets the new result of the login
	 *
	 * @param reason
	 *            reason to set
	 */
	public void setDescriptiveReason( final AccountDescriptiveReason reason )
	{
		this.reason = reason;
	}

	/**
	 * Allows the User to log in
	 */
	public void success()
	{
		reason = AccountDescriptiveReason.DEFAULT;
	}
}
