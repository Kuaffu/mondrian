/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package mondrian.spi;

import mondrian.rolap.CellKey;

import java.io.Serializable;
import java.util.*;

/**
 * SegmentBody is the object which contains the cached data of a
 * Segment. They are stored inside a {@link mondrian.spi.SegmentCache}
 * and can be retrieved by a {@link SegmentHeader} key.
 *
 * <p>The segment body objects are immutable and fully serializable.
 *
 * @author LBoudreau
 */
public interface SegmentBody extends Serializable {
    /**
     * Converts contents of this segment into a cellkey/value map. Use only
     * for sparse segments.
     *
     * @return Map containing cell values keyed by their coordinates
     */
    Map<CellKey, Object> getValueMap();

    /**
     * Returns an array of values.
     *
     * <p>Use only for dense segments.</p>
     *
     * @return An array of values
     */
    Object getValueArray();

    /**
     * Returns a bit-set indicating whether values are null. The ordinals in
     * the bit-set correspond to the indexes in the array returned from
     * {@link #getValueArray()}.
     *
     * <p>Use only for dense segments of native values.</p>
     *
     * @return Indicators
     */
    BitSet getNullValueIndicators();

    /**
     * Returns the cached axis value sets to be used as an
     * initializer for the segment's axis.
     *
     * @return An array of SortedSets which was cached previously.
     */
    SortedSet<Comparable>[] getAxisValueSets();

    /**
     * Returns an array of boolean values which identify which
     * axis of the cached segment contained null values.
     *
     * @return An array of boolean values.
     */
    boolean[] getNullAxisFlags();
}

// End SegmentBody.java
