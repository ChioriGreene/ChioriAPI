/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package joptsimple;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static joptsimple.internal.Reflection.findConverter;

import java.util.List;

/**
 * <p>
 * Specification of a command line's non-option arguments.
 * </p>
 *
 * <p>
 * Instances are returned from {@link OptionParser} methods to allow the formation of parser directives as sentences in a "fluent interface" language. For example:
 * </p>
 *
 * <pre>
 *   <code>
 *   OptionParser parser = new OptionParser();
 *   parser.nonOptions( "files to be processed" ).<strong>ofType( File.class )</strong>;
 *   </code>
 * </pre>
 *
 * <p>
 * If no methods are invoked on an instance of this class, then that instance's option will treat the non-option arguments as {@link String}s.
 * </p>
 *
 * @param <V>
 *             represents the type of the non-option arguments
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
public class NonOptionArgumentSpec<V> extends AbstractOptionSpec<V>
{
	static final String NAME = "[arguments]";

	private ValueConverter<V> converter;
	private String argumentDescription = "";

	NonOptionArgumentSpec()
	{
		this( "" );
	}

	NonOptionArgumentSpec( String description )
	{
		super( asList( NAME ), description );
	}

	@Override
	public boolean acceptsArguments()
	{
		return false;
	}

	@Override
	public String argumentDescription()
	{
		return argumentDescription;
	}

	@Override
	public String argumentTypeIndicator()
	{
		return argumentTypeIndicatorFrom( converter );
	}

	@Override
	protected final V convert( String argument )
	{
		return convertWith( converter, argument );
	}

	@Override
	public List<?> defaultValues()
	{
		return emptyList();
	}

	/**
	 * <p>
	 * Specifies a description for the non-option arguments that this spec represents. This description is used when generating help information about the parser.
	 * </p>
	 *
	 * @param description
	 *             describes the nature of the argument of this spec's option
	 * @return self, so that the caller can add clauses to the fluent interface sentence
	 */
	public NonOptionArgumentSpec<V> describedAs( String description )
	{
		argumentDescription = description;
		return this;
	}

	@Override
	void handleOption( OptionParser parser, ArgumentList arguments, OptionSet detectedOptions, String detectedArgument )
	{

		detectedOptions.addWithArgument( this, detectedArgument );
	}

	@Override
	public boolean isRequired()
	{
		return false;
	}

	/**
	 * <p>
	 * Specifies a type to which the non-option arguments are to be converted.
	 * </p>
	 *
	 * <p>
	 * JOpt Simple accepts types that have either:
	 * </p>
	 *
	 * <ol>
	 * <li>a public static method called {@code valueOf} which accepts a single argument of type {@link String} and whose return type is the same as the class on which the method is declared. The {@code java.lang} primitive wrapper classes have such
	 * methods.</li>
	 *
	 * <li>a public constructor which accepts a single argument of type {@link String}.</li>
	 * </ol>
	 *
	 * <p>
	 * This class converts arguments using those methods in that order; that is, {@code valueOf} would be invoked before a one-{@link String}-arg constructor would.
	 * </p>
	 *
	 * <p>
	 * Invoking this method will trump any previous calls to this method or to {@link #withValuesConvertedBy(ValueConverter)}.
	 * </p>
	 *
	 * @param <T>
	 *             represents the runtime class of the desired option argument type
	 * @param argumentType
	 *             desired type of arguments to this spec's option
	 * @return self, so that the caller can add clauses to the fluent interface sentence
	 * @throws NullPointerException
	 *              if the type is {@code null}
	 * @throws IllegalArgumentException
	 *              if the type does not have the standard conversion methods
	 */
	@SuppressWarnings( "unchecked" )
	public <T> NonOptionArgumentSpec<T> ofType( Class<T> argumentType )
	{
		converter = ( ValueConverter<V> ) findConverter( argumentType );
		return ( NonOptionArgumentSpec<T> ) this;
	}

	@Override
	public boolean representsNonOptions()
	{
		return true;
	}

	@Override
	public boolean requiresArgument()
	{
		return false;
	}

	/**
	 * <p>
	 * Specifies a converter to use to translate non-option arguments into Java objects. This is useful when converting to types that do not have the requisite factory method or constructor for {@link #ofType(Class)}.
	 * </p>
	 *
	 * <p>
	 * Invoking this method will trump any previous calls to this method or to {@link #ofType(Class)}.
	 *
	 * @param <T>
	 *             represents the runtime class of the desired non-option argument type
	 * @param aConverter
	 *             the converter to use
	 * @return self, so that the caller can add clauses to the fluent interface sentence
	 * @throws NullPointerException
	 *              if the converter is {@code null}
	 */
	@SuppressWarnings( "unchecked" )
	public final <T> NonOptionArgumentSpec<T> withValuesConvertedBy( ValueConverter<T> aConverter )
	{
		if ( aConverter == null )
			throw new NullPointerException( "illegal null converter" );

		converter = ( ValueConverter<V> ) aConverter;
		return ( NonOptionArgumentSpec<T> ) this;
	}
}
