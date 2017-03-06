/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 * <p>
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 * <p>
 * All Rights Reserved.
 */
package com.chiorichan.account.types;

import com.chiorichan.AppConfig;
import com.chiorichan.account.AccountContext;
import com.chiorichan.account.AccountMeta;
import com.chiorichan.account.AccountPermissible;
import com.chiorichan.account.AccountType;
import com.chiorichan.account.event.AccountLookupEvent;
import com.chiorichan.account.lang.AccountDescriptiveReason;
import com.chiorichan.account.lang.AccountException;
import com.chiorichan.account.lang.AccountResult;
import com.chiorichan.configuration.types.yaml.YamlConfiguration;
import com.chiorichan.event.EventHandler;
import com.chiorichan.lang.ReportingLevel;
import com.chiorichan.lang.UncaughtException;
import com.chiorichan.permission.PermissibleEntity;
import com.chiorichan.permission.Permission;
import com.chiorichan.permission.PermissionDefault;
import com.chiorichan.tasks.Timings;
import com.chiorichan.zutils.ZIO;
import com.chiorichan.zutils.ZObjects;
import com.google.common.collect.Maps;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Accounts that are loaded from File
 */
public class FileTypeCreator extends AccountTypeCreator
{
	public static final AccountTypeCreator INSTANCE = new FileTypeCreator();

	private List<String> accountFields;
	private File accountsDirectory = null;

	private Map<String, AccountContext> preloaded = Maps.newHashMap();

	public FileTypeCreator()
	{
		accountsDirectory = AppConfig.get().getDirectory( "accounts", "accounts" );

		if ( !ZIO.setDirectoryAccess( accountsDirectory ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "This application experienced a problem setting read and write access to directory \"" + ZIO.relPath( accountsDirectory ) + "\"!" );

		accountFields = AppConfig.get().getStringList( "accounts.fileType.fields", new ArrayList<String>() );

		accountFields.add( "username" );

		checkForFiles();
	}

	public void checkForFiles()
	{
		File[] files = accountsDirectory.listFiles();

		if ( files == null )
			return;

		for ( File f : files )
			if ( FileFilterUtils.and( FileFilterUtils.suffixFileFilter( "yaml" ), FileFilterUtils.fileFileFilter() ).accept( f ) )
				if ( !preloaded.containsKey( f.getName() ) )
					preloaded.put( f.getName(), loadFromFile( f ) );
	}

	@Override
	public AccountContext createAccount( String acctId, String siteId )
	{
		AccountContext context = new AccountContextImpl( this, AccountType.SQL, acctId, siteId );

		context.setValue( "date", Timings.epoch() );
		context.setValue( "numloginfailed", 0 );
		context.setValue( "lastloginfail", 0 );
		context.setValue( "actnum", "0" );

		save( context );
		return context;
	}

	@Override
	public boolean exists( String acctId )
	{
		checkForFiles();

		return preloaded.containsKey( acctId );
	}

	@Override
	public void failedLogin( AccountMeta meta, AccountResult result )
	{
		if ( meta != null && meta.containsKey( "file" ) )
			return;

		File file = ( File ) meta.getObject( "file" );

		if ( file == null )
			return;

		YamlConfiguration yser = YamlConfiguration.loadConfiguration( file );

		if ( yser == null )
			return;

		long lastloginfail = Timings.epoch();
		int numloginfail = meta.getInteger( "numloginfail", 0 ) + 1;

		meta.set( "lastloginfail", lastloginfail );
		meta.set( "numloginfail", numloginfail );

		yser.set( "lastloginfail", lastloginfail );
		yser.set( "numloginfail", numloginfail );

		try
		{
			yser.save( file );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public String getDisplayName( AccountMeta meta )
	{
		if ( meta.getString( "fname" ) != null && !meta.getString( "fname" ).isEmpty() && meta.getString( "name" ) != null && !meta.getString( "name" ).isEmpty() )
			return meta.getString( "fname" ) + " " + meta.getString( "name" );

		if ( meta.getString( "name" ) != null && !meta.getString( "name" ).isEmpty() )
			return meta.getString( "name" );

		if ( meta.getString( "email" ) != null && !meta.getString( "email" ).isEmpty() )
			return meta.getString( "email" );

		return null;
	}

	@Override
	public List<String> getLoginKeys()
	{
		return accountFields;
	}

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	public AccountContext loadFromFile( File absoluteFilePath )
	{
		AccountContextImpl context = new AccountContextImpl( this, AccountType.FILE );

		if ( !absoluteFilePath.exists() )
			return null;

		YamlConfiguration yser = YamlConfiguration.loadConfiguration( absoluteFilePath );

		if ( yser == null )
			return null;

		Map<String, Object> contents = Maps.newHashMap();

		// Save the file location for later
		contents.put( "file", absoluteFilePath );

		for ( String key : yser.getKeys( false ) )
			contents.put( key, yser.get( key ) );

		context.setAcctId( yser.getString( "acctId" ) );
		context.setLocationId( yser.getString( "locId" ) );

		context.setValues( contents );

		return context;
	}

	@EventHandler
	public void onAccountLookupEvent( AccountLookupEvent event )
	{
		try
		{
			event.setResult( readAccount( event.getAcctId() ), AccountDescriptiveReason.LOGIN_SUCCESS );
		}
		catch ( AccountException e )
		{
			event.setResult( null, e.getReason() );
		}
	}

	/*
	 * public AccountMeta createAccount( String accountname, String acctId ) throws AccountException
	 * {
	 * if ( acctId == null || acctId.isEmpty() )
	 * throw new AccountException( LoginExceptionReason.emptyUsername );
	 *
	 * checkForFiles();
	 *
	 * for ( Entry<String, AccountMeta> e : preloaded.entrySet() )
	 * {
	 * AccountMeta meta = e.getValue();
	 *
	 * for ( String f : accountFields )
	 * if ( meta.getObject( f ) != null && ( meta.getString( f ).equalsIgnoreCase( acctId ) || meta.getString( f ).equalsIgnoreCase( accountname ) ) )
	 * throw new AccountException( LoginExceptionReason.accountExists );
	 * }
	 *
	 * AccountMeta meta = new AccountMeta();
	 *
	 * String file = acctId + ".yaml";
	 *
	 * YamlConfiguration yser = new YamlConfiguration();
	 *
	 * yser.set( "accountId", acctId.toLowerCase() );
	 * yser.set( "accountname", accountname );
	 * yser.set( "actnum", WebFunc.randomNum( 8 ) );
	 *
	 * try
	 * {
	 * yser.save( new File( accountsDirectory, file ) );
	 * }
	 * catch ( IOException e )
	 * {
	 * e.printStackTrace();
	 * throw new AccountException( e );
	 * }
	 *
	 * for ( String key : yser.getKeys( false ) )
	 * {
	 * meta.set( key, yser.get( key ) );
	 * }
	 *
	 * return meta;
	 * }
	 */

	@Override
	public void preLogin( AccountMeta meta, AccountPermissible via, String acctId, Object... credentials ) throws AccountException
	{
		if ( meta.getInteger( "numloginfail" ) > 5 )
			if ( meta.getInteger( "lastloginfail" ) > Timings.epoch() - 1800 )
				throw new AccountException( AccountDescriptiveReason.UNDER_ATTACK, meta );

		if ( !meta.getString( "actnum" ).equals( "0" ) )
			throw new AccountException( AccountDescriptiveReason.ACCOUNT_NOT_ACTIVATED, meta );
	}

	public AccountContext readAccount( String acctId ) throws AccountException
	{
		AccountContext context = null;

		if ( acctId == null || acctId.isEmpty() )
			throw new AccountException( AccountDescriptiveReason.EMPTY_ID, AccountType.ACCOUNT_NONE );

		checkForFiles();

		for ( AccountContext context1 : preloaded.values() )
		{
			if ( acctId.equals( context1.getAcctId() ) )
			{
				context = context1;
				break;
			}

			for ( String f : accountFields )
				if ( context1.getValues().get( f ) != null && ZObjects.castToString( context1.getValues().get( f ) ).equalsIgnoreCase( acctId ) )
				{
					context = context1;
					break;
				}
		}

		if ( context == null )
			throw new AccountException( AccountDescriptiveReason.INCORRECT_LOGIN, acctId );

		return context;
	}

	@Override
	public void reload( AccountMeta meta ) throws AccountException
	{
		if ( meta == null || !meta.containsKey( "file" ) )
			throw new AccountException( new AccountDescriptiveReason( "There appears to be a problem with this Account Metadata. Missing the `file` key.", ReportingLevel.L_ERROR ), meta );

		YamlConfiguration yser = YamlConfiguration.loadConfiguration( ( File ) meta.getObject( "file" ) );

		if ( yser == null )
			throw new AccountException( new AccountDescriptiveReason( "The file for this Account Meta Data is missing. Might have been deleted.", ReportingLevel.L_ERROR ), meta );

		for ( String key : yser.getKeys( false ) )
			meta.set( key, yser.get( key ) );
	}

	@Override
	public void save( AccountContext context )
	{
		if ( context == null )
			return;

		Map<String, Object> meta = new HashMap<String, Object>( context.meta() == null ? context.getValues() : context.meta().getMeta() );

		if ( !meta.containsKey( "file" ) )
			meta.put( "file", context.getAcctId() + ".yaml" );

		YamlConfiguration yser = new YamlConfiguration();

		yser.set( "acctId", context.getAcctId() );

		for ( String key : meta.keySet() )
			yser.set( key, meta.get( key ) );

		try
		{
			yser.save( new File( accountsDirectory, ( String ) meta.get( "file" ) ) );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	@Override
	public void successInit( AccountMeta meta, PermissibleEntity entity )
	{
		Permission userNode = PermissionDefault.USER.getNode();
		if ( meta.context().creator() == this && !entity.checkPermission( userNode ).isAssigned() )
			entity.addPermission( userNode, true, null );
	}

	@Override
	public void successLogin( AccountMeta meta )
	{
		if ( !meta.containsKey( "file" ) )
			return;

		File file = ( File ) meta.getObject( "file" );

		if ( file == null )
			return;

		YamlConfiguration yser = YamlConfiguration.loadConfiguration( file );

		if ( yser == null )
			return;

		long lastactive = Timings.epoch();
		long lastlogin = Timings.epoch();
		int lastloginfail = 0;
		int numloginfail = 0;

		meta.set( "lastactive", lastactive );
		meta.set( "lastlogin", lastlogin );
		meta.set( "lastloginfail", lastloginfail );
		meta.set( "numloginfail", numloginfail );

		yser.set( "lastactive", lastactive );
		yser.set( "lastlogin", lastlogin );
		yser.set( "lastloginfail", lastloginfail );
		yser.set( "numloginfail", numloginfail );

		try
		{
			yser.save( file );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
		}
	}

	/*
	 * @Override
	 * public List<AccountMeta> getAccounts()
	 * {
	 * checkForFiles();
	 * return new ArrayList<AccountMeta>( preloaded.values() );
	 * }
	 */

	/*
	 * @Override
	 * public boolean isYou( String id )
	 * {
	 * for ( String f : lookupAdapter.accountFields )
	 * {
	 * if ( metaData.getString( f ).equals( id ) )
	 * return true;
	 * }
	 *
	 * return false;
	 * }
	 */
}
