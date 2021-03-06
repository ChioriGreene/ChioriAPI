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

import java.sql.SQLException;

import org.apache.commons.codec.digest.DigestUtils;

import com.chiorichan.AppConfig;
import com.chiorichan.account.AccountManager;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.datastore.sql.SQLExecute;
import com.chiorichan.datastore.sql.bases.SQLDatastore;
import com.chiorichan.datastore.sql.query.SQLQuerySelect;
import com.chiorichan.tasks.Timings;

/**
 * Used to authenticate an account using a Username and Password combination
 */
public final class PlainTextAccountAuthenticator extends AccountAuthenticator
{
	class PlainTextAccountCredentials extends AccountCredentials
	{
		PlainTextAccountCredentials( AccountDescriptiveReason result, AccountMeta meta )
		{
			super( PlainTextAccountAuthenticator.this, result, meta );
		}
	}

	private final SQLDatastore db = AppConfig.get().getDatabase();

	PlainTextAccountAuthenticator()
	{
		super( "plaintext" );

		try
		{
			if ( !db.table( "accounts_plaintext" ).exists() )
				db.table( "accounts_plaintext" ).addColumnVar( "acctId", 255 ).addColumnVar( "password", 255 ).addColumnInt( "expires", 12 );
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public AccountCredentials authorize( AccountMeta acct, AccountPermissible perm ) throws AccountException
	{
		/**
		 * Session Logins are not resumed using plain text. See {@link AccountCredentials#makeResumable}
		 */
		throw new AccountException( AccountDescriptiveReason.FEATURE_NOT_IMPLEMENTED, acct );
	}

	@Override
	public AccountCredentials authorize( AccountMeta acct, Object... credentials ) throws AccountException
	{
		if ( credentials.length < 1 || ! ( credentials[0] instanceof String ) )
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, acct );

		String pass = ( String ) credentials[0];

		if ( acct == null )
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, AccountType.ACCOUNT_NONE );

		if ( pass == null || pass.isEmpty() )
			throw new AccountException( AccountDescriptiveReason.EMPTY_CREDENTIALS, acct );

		String acctId = acct.getId();
		String password = null;
		try
		{
			SQLExecute<SQLQuerySelect> select = db.table( "accounts_plaintext" ).select().where( "acctId" ).matches( acctId ).limit( 1 ).execute().result();

			if ( select == null || select.rowCount() < 1 )
				throw new AccountException( AccountDescriptiveReason.PASSWORD_UNSET, acct );

			if ( select.getInt( "expires" ) > -1 && select.getInt( "expires" ) < Timings.epoch() )
				throw new AccountException( AccountDescriptiveReason.EXPIRED_LOGIN, acct );

			password = select.getString( "password" );
		}
		catch ( AccountException e )
		{
			if ( acct.getString( "password" ) != null && !acct.getString( "password" ).isEmpty() )
			{
				// Compatibility with older versions, may soon get removed
				password = acct.getString( "password" );
				setPassword( acct, password, -1 );
				acct.set( "password", null );
			}
			else
				throw e;
		}
		catch ( SQLException e )
		{
			throw new AccountException( AccountDescriptiveReason.INTERNAL_ERROR, e, acct );
		}

		// TODO Encrypt all passwords
		if ( password.equals( pass ) || password.equals( DigestUtils.md5Hex( pass ) ) || DigestUtils.md5Hex( password ).equals( pass ) )
			return new PlainTextAccountCredentials( AccountDescriptiveReason.LOGIN_SUCCESS, acct );
		else
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acct );
	}

	/**
	 * Similar to {@link #setPassword(AccountMeta, String, int)} except password never expires
	 */
	public void setPassword( AccountMeta acct, String password )
	{
		setPassword( acct, password, -1 );
	}

	/**
	 * Sets the Account Password which is stored in a separate table for security
	 *
	 * @param acct
	 *             The Account to set password for
	 * @param password
	 *             The password to set
	 * @param expires
	 *             The password expiration. Use -1 for no expiration
	 * @return True if we successfully set the password
	 */
	public boolean setPassword( AccountMeta acct, String password, int expires )
	{
		try
		{
			if ( db.table( "accounts_plaintext" ).insert().value( "acctId", acct.getId() ).value( "password", password ).value( "expires", expires ).execute().rowCount() < 0 )
			{
				AccountManager.getLogger().severe( "We had an unknown issue inserting password for acctId '" + acct.getId() + "' into the database!" );
				return false;
			}

			// db.queryUpdate( "INSERT INTO `accounts_plaintext` (`acctId`,`password`,`expires`) VALUES ('" + acct.getId() + "','" + password + "','" + expires + "');" );
		}
		catch ( SQLException e )
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
