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


package mondrian.olap.type;

import mondrian.olap.*;

/**
 * The type of an expression which represents a Dimension.
 *
 * @author jhyde
 * @since Feb 17, 2005
 */
public class DimensionType implements Type {
    private final Dimension dimension;
    private final String digest;

    public static final DimensionType Unknown = new DimensionType(null);

    /**
     * Creates a type representing a dimension.
     *
     * @param dimension Dimension that values of this type must belong to, or
     *   null if the dimension is unknown
     */
    public DimensionType(Dimension dimension) {
        this.dimension = dimension;
        StringBuilder buf = new StringBuilder("DimensionType<");
        if (dimension != null) {
            buf.append("dimension=").append(dimension.getUniqueName());
        }
        buf.append(">");
        this.digest = buf.toString();
    }

    public static DimensionType forDimension(Dimension dimension) {
        return new DimensionType(dimension);
    }

    public static DimensionType forType(Type type) {
        return new DimensionType(type.getDimension());
    }

    public boolean usesDimension(Dimension dimension, boolean definitely) {
        // REVIEW: Should be '!definitely'?
        return this.dimension == dimension
            || (definitely && this.dimension == null);
    }

    public boolean usesHierarchy(Hierarchy hierarchy, boolean definitely) {
        // If hierarchy belongs to this type's dimension, we might use it.
        return hierarchy.getDimension() == this.dimension
            && !definitely;
    }

    public Hierarchy getHierarchy() {
        return dimension == null
            ? null
            : dimension.getHierarchies().length > 1
            ? getHierarchyWithDefaultName()
            : dimension.getHierarchies()[0];
    }

    private Hierarchy getHierarchyWithDefaultName() {
      for ( Hierarchy hierarchy : dimension.getHierarchies() ) {
        if ( Util.equalName( hierarchy.getName(), dimension.getName() ) ) {
          return hierarchy;
        }
      }
      return null;
    }

    public Level getLevel() {
        return null;
    }

    public Dimension getDimension() {
        return dimension;
    }

    public int hashCode() {
        return digest.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj instanceof DimensionType) {
            DimensionType that = (DimensionType) obj;
            return Util.equals(this.getDimension(), that.getDimension());
        }
        return false;
    }

    public String toString() {
        return digest;
    }

    public Type computeCommonType(Type type, int[] conversionCount) {
        if (conversionCount != null && type instanceof HierarchyType) {
            HierarchyType hierarchyType = (HierarchyType) type;
            if (Util.equals(hierarchyType.getDimension(), dimension)) {
                ++conversionCount[0];
                return this;
            }
            return null;
        }
        if (!(type instanceof DimensionType)) {
            return null;
        }
        DimensionType that = (DimensionType) type;
        if (this.getDimension() != null
            && this.getDimension().equals(that.getDimension()))
        {
            return new DimensionType(
                this.getDimension());
        }
        return DimensionType.Unknown;
    }

    public boolean isInstance(Object value) {
        return value instanceof Dimension
            && (dimension == null
                || value.equals(dimension));
    }

    public int getArity() {
        return 1;
    }
}

// End DimensionType.java
