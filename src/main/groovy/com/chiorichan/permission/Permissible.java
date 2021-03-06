/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.permission;

import com.chiorichan.account.AccountType;
import com.chiorichan.permission.lang.PermissionDeniedException;
import com.chiorichan.permission.lang.PermissionDeniedException.PermissionDeniedReason;
import com.chiorichan.utils.UtilObjects;

public abstract class Permissible
{
	/**
	 * Used to reference the PermissibleEntity for the Permissible object.
	 */
	private PermissibleEntity entity = null;

	private boolean checkEntity()
	{
		if ( AccountType.isNoneAccount( entity ) )
		{
			String id = getId();
			if ( UtilObjects.isEmpty( id ) )
				throw new IllegalStateException( "getId() must return a valid (non-empty) entity Id." );
			entity = PermissionManager.instance().getEntity( id );
		}

		if ( entity == null )
			entity = AccountType.ACCOUNT_NONE.getPermissibleEntity();

		return !AccountType.isNoneAccount( entity );
	}

	public final PermissibleEntity getPermissibleEntity()
	{
		checkEntity();
		return entity;
	}

	public final void destroyEntity()
	{
		entity = AccountType.ACCOUNT_NONE.getPermissibleEntity();
	}

	public final boolean isBanned()
	{
		if ( !checkEntity() )
			return false;

		return entity.isBanned();
	}

	public final boolean isWhitelisted()
	{
		if ( !checkEntity() )
			return false;

		return entity.isWhitelisted();
	}

	public final boolean isAdmin()
	{
		if ( !checkEntity() )
			return false;

		return entity.isAdmin();
	}

	/**
	 * Is this permissible on the OP list.
	 *
	 * @return true if OP
	 */
	public final boolean isOp()
	{
		if ( !checkEntity() )
			return false;

		return entity.isOp();
	}

	public final PermissionResult checkPermission( String perm )
	{
		perm = PermissionManager.parseNode( perm );
		return checkPermission( PermissionManager.instance().createNode( perm ) );
	}

	public final PermissionResult checkPermission( String perm, References refs )
	{
		perm = PermissionManager.parseNode( perm );
		return checkPermission( PermissionManager.instance().createNode( perm ), refs );
	}

	public final PermissionResult checkPermission( Permission perm, References refs )
	{
		PermissibleEntity entity = getPermissibleEntity();
		return entity.checkPermission( perm, refs );
	}

	public final PermissionResult checkPermission( String perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm, String... refs )
	{
		return checkPermission( perm, References.format( refs ) );
	}

	public final PermissionResult checkPermission( Permission perm )
	{
		return checkPermission( perm, References.format( "" ) );
	}

	/**
	 * -1, everybody, everyone = Allow All!
	 * 0, op, root | sys.op = OP Only!
	 * admin | sys.admin = Admin Only!
	 */
	public final PermissionResult requirePermission( String req, References refs ) throws PermissionDeniedException
	{
		req = PermissionManager.parseNode( req );
		return requirePermission( PermissionManager.instance().createNode( req ), refs );
	}

	public final PermissionResult requirePermission( String req, String... refs ) throws PermissionDeniedException
	{
		req = PermissionManager.parseNode( req );
		return requirePermission( PermissionManager.instance().createNode( req ), References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, String... refs ) throws PermissionDeniedException
	{
		return requirePermission( req, References.format( refs ) );
	}

	public final PermissionResult requirePermission( Permission req, References refs ) throws PermissionDeniedException
	{
		PermissionResult result = checkPermission( req );

		if ( result.getPermission() != PermissionDefault.EVERYBODY.getNode() )
		{
			if ( result.getEntity() == null || AccountType.isNoneAccount( result.getEntity() ) )
				throw new PermissionDeniedException( PermissionDeniedReason.LOGIN_PAGE );

			if ( !result.isTrue() )
			{
				if ( result.getPermission() == PermissionDefault.OP.getNode() )
					throw new PermissionDeniedException( PermissionDeniedReason.OP_ONLY );

				result.recalculatePermissions( refs );
				if ( result.isTrue() )
					return result;

				throw new PermissionDeniedException( PermissionDeniedReason.DENIED.setPermission( req ) );
			}
		}

		return result;
	}

	/**
	 * Get the unique identifier for this Permissible
	 *
	 * @return String a unique identifier
	 */
	public abstract String getId();
}
