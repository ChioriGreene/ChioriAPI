/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Chiori Greene a.k.a. Chiori-chan <me@chiorichan.com>
 * All Rights Reserved
 */
package org.yaml.snakeyaml.events;

import org.yaml.snakeyaml.error.Mark;

/**
 * Marks the end of a sequence.
 * 
 * @see SequenceStartEvent
 */
public final class SequenceEndEvent extends CollectionEndEvent {

    public SequenceEndEvent(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    @Override
    public boolean is(Event.ID id) {
        return ID.SequenceEnd == id;
    }
}
