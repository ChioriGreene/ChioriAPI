/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright 2016 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Right Reserved.
 */
package com.chiorichan.configuration;

/**
 * Various settings for controlling the input and output of a {@link Configuration}
 */
public class ConfigurationOptions
{
	private char pathSeparator = '.';
	private boolean copyDefaults = false;
	private final Configuration configuration;
	
	protected ConfigurationOptions( Configuration configuration )
	{
		this.configuration = configuration;
	}
	
	/**
	 * Returns the {@link Configuration} that this object is responsible for.
	 * 
	 * @return Parent configuration
	 */
	public Configuration configuration()
	{
		return configuration;
	}
	
	/**
	 * Gets the char that will be used to separate {@link ConfigurationSection}s
	 * <p />
	 * This value does not affect how the {@link Configuration} is stored, only in how you access the data. The default value is '.'.
	 * 
	 * @return Path separator
	 */
	public char pathSeparator()
	{
		return pathSeparator;
	}
	
	/**
	 * Sets the char that will be used to separate {@link ConfigurationSection}s
	 * <p />
	 * This value does not affect how the {@link Configuration} is stored, only in how you access the data. The default value is '.'.
	 * 
	 * @param value
	 *            Path separator
	 * @return This object, for chaining
	 */
	public ConfigurationOptions pathSeparator( char value )
	{
		this.pathSeparator = value;
		return this;
	}
	
	/**
	 * Checks if the {@link Configuration} should copy values from its default {@link Configuration} directly.
	 * <p />
	 * If this is true, all values in the default Configuration will be directly copied, making it impossible to distinguish between values that were set and values that are provided by default. As a result,
	 * {@link ConfigurationSection#contains(java.lang.String)} will always return the same value as {@link ConfigurationSection#isSet(java.lang.String)}. The default value is false.
	 * 
	 * @return Whether or not defaults are directly copied
	 */
	public boolean copyDefaults()
	{
		return copyDefaults;
	}
	
	/**
	 * Sets if the {@link Configuration} should copy values from its default {@link Configuration} directly.
	 * <p />
	 * If this is true, all values in the default Configuration will be directly copied, making it impossible to distinguish between values that were set and values that are provided by default. As a result,
	 * {@link ConfigurationSection#contains(java.lang.String)} will always return the same value as {@link ConfigurationSection#isSet(java.lang.String)}. The default value is false.
	 * 
	 * @param value
	 *            Whether or not defaults are directly copied
	 * @return This object, for chaining
	 */
	public ConfigurationOptions copyDefaults( boolean value )
	{
		this.copyDefaults = value;
		return this;
	}
}
