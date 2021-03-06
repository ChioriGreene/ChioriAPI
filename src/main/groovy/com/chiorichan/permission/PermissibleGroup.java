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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chiorichan.permission.event.PermissibleEntityEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public abstract class PermissibleGroup extends PermissibleEntity implements Comparable<PermissibleGroup>
{
	private int rank = -1;
	private int weight = 0;

	public PermissibleGroup( String groupName )
	{
		super( groupName );
	}

	@Override
	public final int compareTo( PermissibleGroup o )
	{
		return getWeight() - o.getWeight();
	}

	// XXX THIS!!! New Ref Groups
	public Map<String, Collection<PermissibleGroup>> getAllParentGroups()
	{
		return Maps.newHashMap();
		// return Collections.unmodifiableMap( groups );
	}

	// TODO Prevent StackOverflow
	public Collection<PermissibleEntity> getChildEntities( boolean recursive, References refs )
	{
		List<PermissibleEntity> children = Lists.newArrayList();
		for ( PermissibleEntity entity : PermissionManager.instance().getEntities() )
			if ( entity.getGroups( refs ).contains( this ) )
				children.add( entity );
		if ( recursive )
			for ( PermissibleGroup group : getChildGroups( true, refs ) )
				children.addAll( group.getChildEntities( true, refs ) );
		return children;
	}

	public Collection<PermissibleEntity> getChildEntities( References refs )
	{
		return getChildEntities( false, refs );
	}

	// TODO Prevent StackOverflow
	public Collection<PermissibleGroup> getChildGroups( boolean recursive, References refs )
	{
		List<PermissibleGroup> children = Lists.newArrayList();
		for ( PermissibleGroup group : PermissionManager.instance().getGroups() )
			if ( group.getGroups( refs ).contains( this ) )
			{
				children.add( group );
				if ( recursive )
					children.addAll( group.getChildGroups( true, refs ) );
			}
		return children;
	}

	public Collection<PermissibleGroup> getChildGroups( References refs )
	{
		return getChildGroups( false, refs );
	}

	// XXX THIS TOO!
	public Map<String, String> getOptions()
	{
		return Maps.newHashMap();
	}

	public Collection<String> getParentGroupsNames( References refs )
	{
		Set<String> result = Sets.newHashSet();
		for ( PermissibleGroup group : getGroups( refs ) )
			result.add( group.getId() );
		return result;
	}

	public int getRank()
	{
		return rank;
	}

	public String getRankLadder()
	{
		return null;// TODO Auto-generated method stub
	}

	public final int getWeight()
	{
		return weight;
	}

	public boolean isRanked()
	{
		return rank >= 0;
	}

	public void setDefault( boolean isDef )
	{
		// TODO Auto-generated method stub
	}

	public void setRank( int rank )
	{
		this.rank = rank;
	}

	public void setRankLadder( String rank )
	{
		// TODO Auto-generated method stub
	}

	public final void setWeight( int weight )
	{
		this.weight = weight;
		PermissionManager.callEvent( new PermissibleEntityEvent( this, PermissibleEntityEvent.Action.WEIGHT_CHANGED ) );
	}
}
