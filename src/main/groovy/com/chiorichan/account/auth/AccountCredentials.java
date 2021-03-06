/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.account.auth;

import org.apache.commons.lang3.Validate;

import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.lang.ReportingLevel;

/**
 * Provides login credentials to the {@link AccountAuthenticator}
 */
public abstract class AccountCredentials
{
	protected final AccountAuthenticator authenticator;
	protected final AccountDescriptiveReason reason;
	protected final AccountMeta meta;

	AccountCredentials( AccountAuthenticator authenticator, AccountDescriptiveReason reason, AccountMeta meta )
	{
		this.authenticator = authenticator;
		this.reason = reason;
		this.meta = meta;
	}

	public final AccountMeta getAccount()
	{
		return meta;
	}

	public final AccountAuthenticator getAuthenticator()
	{
		return authenticator;
	}

	public final AccountDescriptiveReason getDescriptiveReason()
	{
		return reason;
	}

	/**
	 * Saves persistent variables into the session for later resuming
	 *
	 * @param perm The AccountPermissible to store the login credentials
	 * @throws AccountException If there are issues handling the account
	 */
	public void makeResumable( AccountPermissible perm ) throws AccountException
	{
		Validate.notNull( perm );

		if ( perm.meta() != meta )
			throw new AccountException( new AccountDescriptiveReason( "You can't make an account resumable when it's not logged in.", ReportingLevel.L_DENIED ), meta );

		if ( !reason.getReportingLevel().isSuccess() )
			throw new AccountException( new AccountDescriptiveReason( "You can't make an account resumable if it failed login.", ReportingLevel.L_DENIED ), meta );

		if ( AccountType.isNoneAccount( perm.meta() ) || AccountType.isRootAccount( perm.meta() ) )
			throw new AccountException( new AccountDescriptiveReason( "You can't make the 'none' nor 'root' accounts resumable.", ReportingLevel.L_SECURITY ), meta );

		if ( "token".equals( perm.getVariable( "auth" ) ) && perm.getVariable( "token" ) != null )
			AccountAuthenticator.TOKEN.deleteToken( perm.getVariable( "acctId" ), perm.getVariable( "token" ) );

		perm.setVariable( "auth", "token" );
		perm.setVariable( "acctId", meta.getId() );
		perm.setVariable( "token", AccountAuthenticator.TOKEN.issueToken( meta ) );
	}
}
