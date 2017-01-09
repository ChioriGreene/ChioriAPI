/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package joptsimple;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a group of detected command line options, their arguments, and non-option arguments.
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public class OptionSet
{
	private static Map<String, List<?>> defaultValues( Map<String, AbstractOptionSpec<?>> recognizedSpecs )
	{
		Map<String, List<?>> defaults = new HashMap<>();
		for ( Map.Entry<String, AbstractOptionSpec<?>> each : recognizedSpecs.entrySet() )
			defaults.put( each.getKey(), each.getValue().defaultValues() );
		return defaults;
	}
	private final List<OptionSpec<?>> detectedSpecs;
	private final Map<String, AbstractOptionSpec<?>> detectedOptions;
	private final Map<AbstractOptionSpec<?>, List<String>> optionsToArguments;
	private final Map<String, AbstractOptionSpec<?>> recognizedSpecs;

	private final Map<String, List<?>> defaultValues;

	/*
	 * Package-private because clients don't create these.
	 */
	OptionSet( Map<String, AbstractOptionSpec<?>> recognizedSpecs )
	{
		detectedSpecs = new ArrayList<>();
		detectedOptions = new HashMap<>();
		optionsToArguments = new IdentityHashMap<>();
		defaultValues = defaultValues( recognizedSpecs );
		this.recognizedSpecs = recognizedSpecs;
	}

	void add( AbstractOptionSpec<?> spec )
	{
		addWithArgument( spec, null );
	}

	void addWithArgument( AbstractOptionSpec<?> spec, String argument )
	{
		detectedSpecs.add( spec );

		for ( String each : spec.options() )
			detectedOptions.put( each, spec );

		List<String> optionArguments = optionsToArguments.get( spec );

		if ( optionArguments == null )
		{
			optionArguments = new ArrayList<>();
			optionsToArguments.put( spec, optionArguments );
		}

		if ( argument != null )
			optionArguments.add( argument );
	}

	/**
	 * Gives all declared options as a map of string to {@linkplain OptionSpec}.
	 *
	 * @return the declared options as a map
	 */
	public Map<OptionSpec<?>, List<?>> asMap()
	{
		Map<OptionSpec<?>, List<?>> map = new HashMap<>();

		for ( AbstractOptionSpec<?> spec : recognizedSpecs.values() )
			if ( !spec.representsNonOptions() )
				map.put( spec, valuesOf( spec ) );

		return unmodifiableMap( map );
	}

	private <V> List<V> defaultValueFor( OptionSpec<V> option )
	{
		return defaultValuesFor( option.options().iterator().next() );
	}

	@SuppressWarnings( "unchecked" )
	private <V> List<V> defaultValuesFor( String option )
	{
		if ( defaultValues.containsKey( option ) )
			return unmodifiableList( ( List<V> ) defaultValues.get( option ) );

		return emptyList();
	}

	@Override
	public boolean equals( Object that )
	{
		if ( this == that )
			return true;

		if ( that == null || !getClass().equals( that.getClass() ) )
			return false;

		OptionSet other = ( OptionSet ) that;
		Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments = new HashMap<>( optionsToArguments );
		Map<AbstractOptionSpec<?>, List<String>> otherOptionsToArguments = new HashMap<>( other.optionsToArguments );
		return detectedOptions.equals( other.detectedOptions ) && thisOptionsToArguments.equals( otherOptionsToArguments );
	}

	/**
	 * Tells whether the given option was detected.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent interface methods.
	 * </p>
	 *
	 * <p>
	 * Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[])} default argument value} for an option does not cause this method to return {@code true} if the option was not detected on the command line.
	 * </p>
	 *
	 * @param option
	 *             the option to search for
	 * @return {@code true} if the option was detected
	 * @see #has(String)
	 */
	public boolean has( OptionSpec<?> option )
	{
		return optionsToArguments.containsKey( option );
	}

	/**
	 * Tells whether the given option was detected.
	 *
	 * @param option
	 *             the option to search for
	 * @return {@code true} if the option was detected
	 * @see #has(OptionSpec)
	 */
	public boolean has( String option )
	{
		return detectedOptions.containsKey( option );
	}

	/**
	 * Tells whether there are any arguments associated with the given option.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent interface methods.
	 * </p>
	 *
	 * <p>
	 * Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value} for an option does not cause this method to return {@code true} if the option was not detected on the command line, or if the option can take
	 * an optional argument but did not have one on the command line.
	 * </p>
	 *
	 * @param option
	 *             the option to search for
	 * @return {@code true} if the option was detected and at least one argument was detected for the option
	 * @throws NullPointerException
	 *              if {@code option} is {@code null}
	 * @see #hasArgument(String)
	 */
	public boolean hasArgument( OptionSpec<?> option )
	{
		requireNonNull( option );

		List<String> values = optionsToArguments.get( option );
		return values != null && !values.isEmpty();
	}

	/**
	 * Tells whether there are any arguments associated with the given option.
	 *
	 * @param option
	 *             the option to search for
	 * @return {@code true} if the option was detected and at least one argument was detected for the option
	 * @see #hasArgument(OptionSpec)
	 */
	public boolean hasArgument( String option )
	{
		AbstractOptionSpec<?> spec = detectedOptions.get( option );
		return spec != null && hasArgument( spec );
	}

	@Override
	public int hashCode()
	{
		Map<AbstractOptionSpec<?>, List<String>> thisOptionsToArguments = new HashMap<>( optionsToArguments );
		return detectedOptions.hashCode() ^ thisOptionsToArguments.hashCode();
	}

	/**
	 * Tells whether any options were detected.
	 *
	 * @return {@code true} if any options were detected
	 */
	public boolean hasOptions()
	{
		return ! ( detectedOptions.size() == 1 && detectedOptions.values().iterator().next().representsNonOptions() );
	}

	/**
	 * @return the detected non-option arguments
	 */
	public List<?> nonOptionArguments()
	{
		AbstractOptionSpec<?> spec = detectedOptions.get( NonOptionArgumentSpec.NAME );
		return valuesOf( spec );
	}

	/**
	 * Gives the set of options that were detected, in the form of {@linkplain OptionSpec}s, in the order in which the
	 * options were found on the command line.
	 *
	 * @return the set of detected command line options
	 */
	public List<OptionSpec<?>> specs()
	{
		List<OptionSpec<?>> specs = detectedSpecs;
		specs.removeAll( singletonList( detectedOptions.get( NonOptionArgumentSpec.NAME ) ) );

		return unmodifiableList( specs );
	}

	/**
	 * Gives the argument associated with the given option.
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent interface methods.
	 * </p>
	 *
	 * @param <V>
	 *             represents the type of the arguments the given option accepts
	 * @param option
	 *             the option to search for
	 * @return the argument of the given option; {@code null} if no argument is present, or that option was not
	 *         detected
	 * @throws OptionException
	 *              if more than one argument was detected for the option
	 * @throws NullPointerException
	 *              if {@code option} is {@code null}
	 * @throws ClassCastException
	 *              if the arguments of this option are not of the expected type
	 */
	public <V> V valueOf( OptionSpec<V> option )
	{
		requireNonNull( option );

		List<V> values = valuesOf( option );
		switch ( values.size() )
		{
			case 0:
				return null;
			case 1:
				return values.get( 0 );
			default:
				throw new MultipleArgumentsForOptionException( option );
		}
	}

	/**
	 * Gives the argument associated with the given option. If the option was given an argument type, the argument
	 * will take on that type; otherwise, it will be a {@link String}.
	 *
	 * <p>
	 * Specifying a {@linkplain ArgumentAcceptingOptionSpec#defaultsTo(Object, Object[]) default argument value} for an option will cause this method to return that default value even if the option was not detected on the command line, or if the option
	 * can take an optional argument but did not have one on the command line.
	 * </p>
	 *
	 * @param option
	 *             the option to search for
	 * @return the argument of the given option; {@code null} if no argument is present, or that option was not
	 *         detected
	 * @throws NullPointerException
	 *              if {@code option} is {@code null}
	 * @throws OptionException
	 *              if more than one argument was detected for the option
	 */
	public Object valueOf( String option )
	{
		requireNonNull( option );

		AbstractOptionSpec<?> spec = detectedOptions.get( option );
		if ( spec == null )
		{
			List<?> defaults = defaultValuesFor( option );
			return defaults.isEmpty() ? null : defaults.get( 0 );
		}

		return valueOf( spec );
	}

	/**
	 * <p>
	 * Gives any arguments associated with the given option. If the option was given an argument type, the arguments will take on that type; otherwise, they will be {@link String}s.
	 * </p>
	 *
	 * <p>
	 * This method recognizes only instances of options returned from the fluent interface methods.
	 * </p>
	 *
	 * @param <V>
	 *             represents the type of the arguments the given option accepts
	 * @param option
	 *             the option to search for
	 * @return the arguments associated with the option; an empty list if no such arguments are present, or if the
	 *         option was not detected
	 * @throws NullPointerException
	 *              if {@code option} is {@code null}
	 * @throws OptionException
	 *              if there is a problem converting the option's arguments to the desired type; for
	 *              example, if the type does not implement a correct conversion constructor or method
	 */
	public <V> List<V> valuesOf( OptionSpec<V> option )
	{
		requireNonNull( option );

		List<String> values = optionsToArguments.get( option );
		if ( values == null || values.isEmpty() )
			return defaultValueFor( option );

		AbstractOptionSpec<V> spec = ( AbstractOptionSpec<V> ) option;
		List<V> convertedValues = new ArrayList<>();
		for ( String each : values )
			convertedValues.add( spec.convert( each ) );

		return unmodifiableList( convertedValues );
	}

	/**
	 * <p>
	 * Gives any arguments associated with the given option. If the option was given an argument type, the arguments will take on that type; otherwise, they will be {@link String}s.
	 * </p>
	 *
	 * @param option
	 *             the option to search for
	 * @return the arguments associated with the option, as a list of objects of the type given to the arguments; an
	 *         empty list if no such arguments are present, or if the option was not detected
	 * @throws NullPointerException
	 *              if {@code option} is {@code null}
	 */
	public List<?> valuesOf( String option )
	{
		requireNonNull( option );

		AbstractOptionSpec<?> spec = detectedOptions.get( option );
		return spec == null ? defaultValuesFor( option ) : valuesOf( spec );
	}
}
