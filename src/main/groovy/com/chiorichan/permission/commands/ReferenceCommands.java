/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.permission.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.chiorichan.lang.EnumColor;
import com.chiorichan.permission.PermissionManager;
import com.chiorichan.permission.References;
import com.chiorichan.terminal.TerminalEntity;
import com.chiorichan.terminal.commands.advanced.CommandHandler;
import com.google.common.base.Joiner;

public class ReferenceCommands extends PermissionBaseCommand
{
	@CommandHandler( name = "pex", syntax = "ref <ref>", description = "Print <ref> inheritance info", permission = "permissions.manage.refs" )
	public void refPrintInheritance( TerminalEntity sender, Map<String, String> args )
	{
		References refs = autoCompleteRef( args.get( "ref" ) );
		PermissionManager manager = PermissionManager.instance();
		Collection<String> parentReferences = manager.getRefInheritance( refs.join() );
		if ( parentReferences == null )
		{
			sender.sendMessage( "Specified ref \"" + args.get( "ref" ) + "\" not found." );
			return;
		}

		sender.sendMessage( "Reference " + refs + " inherit:" );
		if ( parentReferences.size() == 0 )
		{
			sender.sendMessage( "nothing :3" );
			return;
		}

		for ( String parentReference : parentReferences )
		{
			// Collection<String> parents = manager.getRefInheritance( parentReference );
			String output = "  " + parentReference;
			if ( parentReferences.size() > 0 )
				output += EnumColor.GREEN + " [" + EnumColor.WHITE + Joiner.on( ", " ).join( parentReferences ) + EnumColor.GREEN + "]";

			sender.sendMessage( output );
		}
	}

	@CommandHandler( name = "pex", syntax = "ref <ref> inherit <parentReferences>", description = "Set <parentReferences> for <ref>", permission = "permissions.manage.refs.inheritance" )
	public void refSetInheritance( TerminalEntity sender, Map<String, String> args )
	{
		References refs = autoCompleteRef( args.get( "ref" ) );
		PermissionManager manager = PermissionManager.instance();
		/*
		 * if ( ReferenceManager.instance().getReferenceById( refs ) == null )
		 * {
		 * sender.sendMessage( "Specified ref \"" + args.get( "ref" ) + "\" not found." );
		 * return;
		 * }
		 *
		 * TODO Check for references once they are front loaded
		 */

		List<String> parents = new ArrayList<String>();
		String parentReferences = args.get( "parentReferences" );
		if ( parentReferences.contains( "," ) )
			for ( String ref : parentReferences.split( "," ) )
			{
				// ref = autoCompleteRef( ref, "parentReferences" );
				if ( !parents.contains( ref ) )
					parents.add( ref.trim() );
			}
		else
			parents.add( parentReferences.trim() );

		manager.setRefInheritance( refs.join(), parents );

		sender.sendMessage( "Reference " + refs + " inherits " + Joiner.on( ", " ).join( parents ) );
	}

	@CommandHandler( name = "pex", syntax = "refs", description = "Print loaded refs", isPrimary = true, permission = "permissions.manage.refs" )
	public void refsTree( TerminalEntity sender, Map<String, String> args )
	{
		PermissionManager manager = PermissionManager.instance();

		sender.sendMessage( "References on server: " );
		for ( String ref : PermissionManager.instance().getReferences() )
		{
			Collection<String> parentReferences = manager.getRefInheritance( ref );
			String output = "  " + ref;
			if ( parentReferences.size() > 0 )
				output += EnumColor.GREEN + " [" + EnumColor.WHITE + Joiner.on( ", " ).join( parentReferences ) + EnumColor.GREEN + "]";

			sender.sendMessage( output );
		}
	}
}
