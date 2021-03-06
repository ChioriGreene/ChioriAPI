/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.configuration.serialization;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applies to a {@link ConfigurationSerializable} that will delegate all deserialization to another {@link ConfigurationSerializable}.
 */
@Retention( RetentionPolicy.RUNTIME )
@Target( ElementType.TYPE )
public @interface DelegateDeserialization
{
	/**
	 * Which class should be used as a delegate for this classes deserialization
	 *
	 * @return Delegate class
	 */
	Class<? extends ConfigurationSerializable> value();
}
