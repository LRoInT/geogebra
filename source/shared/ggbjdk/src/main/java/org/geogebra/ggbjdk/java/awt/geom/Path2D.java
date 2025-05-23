/*
 * Copyright (c) 2006, 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.geogebra.ggbjdk.java.awt.geom;

import java.util.Arrays;

import org.geogebra.common.awt.GAffineTransform;
import org.geogebra.common.awt.GPathIterator;
import org.geogebra.common.awt.GPoint2D;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.awt.GRectangle2D;
import org.geogebra.common.awt.GShape;
import org.geogebra.ggbjdk.sun.awt.geom.Curve;

/**
 * The {@code Path2D} class provides a simple, yet flexible shape which
 * represents an arbitrary geometric path. It can fully represent any path which
 * can be iterated by the {@link GPathIterator} interface including all of its
 * segment types and winding rules and it implements all of the basic hit
 * testing methods of the {@link Shape} interface.
 * <p>
 * Use {@link Path2D.Double} for data that
 * requires the accuracy or range of double precision.
 * <p>
 * {@code Path2D} provides exactly those facilities required for basic
 * construction and management of a geometric path and implementation of the
 * above interfaces with little added interpretation. If it is useful to
 * manipulate the interiors of closed geometric shapes beyond simple hit testing
 * then the {@link Area} class provides additional capabilities specifically
 * targeted at closed figures. While both classes nominally implement the
 * {@code Shape} interface, they differ in purpose and together they provide two
 * useful views of a geometric shape where {@code Path2D} deals primarily with a
 * trajectory formed by path segments and {@code Area} deals more with
 * interpretation and manipulation of enclosed regions of 2D geometric space.
 * <p>
 * The {@link GPathIterator} interface has more detailed descriptions of the
 * types of segments that make up a path and the winding rules that control how
 * to determine which regions are inside or outside the path.
 *
 * @author Jim Graham
 * @since 1.6
 */
public abstract class Path2D implements Shape {
    /**
	 * An even-odd winding rule for determining the interior of a path.
	 *
	 * @see GPathIterator#WIND_EVEN_ODD
	 * @since 1.6
	 */
	public static final int WIND_EVEN_ODD = GPathIterator.WIND_EVEN_ODD;

    /**
	 * A non-zero winding rule for determining the interior of a path.
	 *
	 * @see GPathIterator#WIND_NON_ZERO
	 * @since 1.6
	 */
	public static final int WIND_NON_ZERO = GPathIterator.WIND_NON_ZERO;

    // For code simplicity, copy these constants to our namespace
    // and cast them to byte constants for easy storage.
	private static final byte SEG_MOVETO = (byte) GPathIterator.SEG_MOVETO;
	private static final byte SEG_LINETO = (byte) GPathIterator.SEG_LINETO;
	private static final byte SEG_QUADTO = (byte) GPathIterator.SEG_QUADTO;
	private static final byte SEG_CUBICTO = (byte) GPathIterator.SEG_CUBICTO;
	private static final byte SEG_CLOSE = (byte) GPathIterator.SEG_CLOSE;

    transient byte[] pointTypes;
    transient int numTypes;
    transient int numCoords;
    transient int windingRule;

    static final int INIT_SIZE = 20;
    static final int EXPAND_MAX = 500;

    /**
     * Constructs a new empty {@code Path2D} object.
     * It is assumed that the package sibling subclass that is
     * defaulting to this constructor will fill in all values.
     *
     * @since 1.6
     */
    /* private protected */
    Path2D() {
    }

    /**
     * Constructs a new {@code Path2D} object from the given
     * specified initial values.
     * This method is only intended for internal use and should
     * not be made public if the other constructors for this class
     * are ever exposed.
     *
     * @param rule the winding rule
     * @param initialTypes the size to make the initial array to
     *                     store the path segment types
     * @since 1.6
     */
    /* private protected */
    Path2D(int rule, int initialTypes) {
        setWindingRule(rule);
        this.pointTypes = new byte[initialTypes];
    }

    abstract double[] cloneCoordsDouble(GAffineTransform at);
    abstract void append(double x, double y);
    abstract GPoint2D getPoint(int coordindex);
    abstract void needRoom(boolean needMove, int newCoords);
    abstract int pointCrossings(double px, double py);
    abstract int rectCrossings(double rxmin, double rymin,
                               double rxmax, double rymax);



    /**
     * The {@code Double} class defines a geometric path with
     * coordinates stored in double precision floating point.
     *
     * @since 1.6
     */
	public static class Double extends Path2D {
        transient double doubleCoords[];

        /**
         * Constructs a new empty double precision {@code Path2D} object
         * with a default winding rule of {@link #WIND_NON_ZERO}.
         *
         * @since 1.6
         */
        public Double() {
            this(WIND_NON_ZERO, INIT_SIZE);
        }

        /**
         * Constructs a new empty double precision {@code Path2D} object
         * with the specified winding rule to control operations that
         * require the interior of the path to be defined.
         *
         * @param rule the winding rule
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Double(int rule) {
            this(rule, INIT_SIZE);
        }

        /**
         * Constructs a new empty double precision {@code Path2D} object
         * with the specified winding rule and the specified initial
         * capacity to store path segments.
         * This number is an initial guess as to how many path segments
         * are in the path, but the storage is expanded as needed to store
         * whatever path segments are added to this path.
         *
         * @param rule the winding rule
         * @param initialCapacity the estimate for the number of path segments
         *                        in the path
         * @see #WIND_EVEN_ODD
         * @see #WIND_NON_ZERO
         * @since 1.6
         */
        public Double(int rule, int initialCapacity) {
            super(rule, initialCapacity);
            doubleCoords = new double[initialCapacity * 2];
        }

        /**
         * Constructs a new double precision {@code Path2D} object
         * from an arbitrary {@link Shape} object.
         * All of the initial geometry and the winding rule for this path are
         * taken from the specified {@code Shape} object.
         *
         * @param s the specified {@code Shape} object
         * @since 1.6
         */
        public Double(Shape s) {
            this(s, null);
        }

        /**
         * Constructs a new double precision {@code Path2D} object
         * from an arbitrary {@link Shape} object, transformed by an
         * {@link AffineTransform} object.
         * All of the initial geometry and the winding rule for this path are
         * taken from the specified {@code Shape} object and transformed
         * by the specified {@code AffineTransform} object.
         *
         * @param s the specified {@code Shape} object
         * @param at the specified {@code AffineTransform} object
         * @since 1.6
         */
        public Double(GShape s, GAffineTransform at) {
            if (s instanceof Path2D) {
                Path2D p2d = (Path2D) s;
                setWindingRule(p2d.windingRule);
                this.numTypes = p2d.numTypes;
                this.pointTypes = Arrays.copyOf(p2d.pointTypes,
                                                p2d.pointTypes.length);
                this.numCoords = p2d.numCoords;
                this.doubleCoords = p2d.cloneCoordsDouble(at);
            } else {
                GPathIterator pi = s.getPathIterator(at);
                setWindingRule(pi.getWindingRule());
                this.pointTypes = new byte[INIT_SIZE];
                this.doubleCoords = new double[INIT_SIZE * 2];
                append(pi, false);
            }
        }

        @Override
		double[] cloneCoordsDouble(GAffineTransform at) {
            double ret[];
            if (at == null) {
                ret = Arrays.copyOf(this.doubleCoords,
                                    this.doubleCoords.length);
            } else {
                ret = new double[doubleCoords.length];
                at.transform(doubleCoords, 0, ret, 0, numCoords / 2);
            }
            return ret;
        }

        @Override
		void append(double x, double y) {
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }

        @Override
		GPoint2D getPoint(int coordindex) {
            return new GPoint2D(doubleCoords[coordindex],
                                      doubleCoords[coordindex+1]);
        }

        @Override
		void needRoom(boolean needMove, int newCoords) {
            if (needMove && numTypes == 0) {
                throw new IllegalPathStateException("missing initial moveto "+
                                                    "in path definition");
            }
            int size = pointTypes.length;
            if (numTypes >= size) {
                int grow = size;
                if (grow > EXPAND_MAX) {
                    grow = EXPAND_MAX;
                } else if (grow == 0) {
                    grow = 1;
                }
                pointTypes = Arrays.copyOf(pointTypes, size+grow);
            }
            size = doubleCoords.length;
            if (numCoords + newCoords > size) {
                int grow = size;
                if (grow > EXPAND_MAX * 2) {
                    grow = EXPAND_MAX * 2;
                }
                if (grow < newCoords) {
                    grow = newCoords;
                }
                doubleCoords = Arrays.copyOf(doubleCoords, size+grow);
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final synchronized void moveTo(double x, double y) {
            if (numTypes > 0 && pointTypes[numTypes - 1] == SEG_MOVETO) {
                doubleCoords[numCoords-2] = x;
                doubleCoords[numCoords-1] = y;
            } else {
                needRoom(false, 2);
                pointTypes[numTypes++] = SEG_MOVETO;
                doubleCoords[numCoords++] = x;
                doubleCoords[numCoords++] = y;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final synchronized void lineTo(double x, double y) {
            needRoom(true, 2);
            pointTypes[numTypes++] = SEG_LINETO;
            doubleCoords[numCoords++] = x;
            doubleCoords[numCoords++] = y;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final synchronized void quadTo(double x1, double y1,
                                              double x2, double y2)
        {
            needRoom(true, 4);
            pointTypes[numTypes++] = SEG_QUADTO;
            doubleCoords[numCoords++] = x1;
            doubleCoords[numCoords++] = y1;
            doubleCoords[numCoords++] = x2;
            doubleCoords[numCoords++] = y2;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final synchronized void curveTo(double x1, double y1,
                                               double x2, double y2,
                                               double x3, double y3)
        {
            needRoom(true, 6);
            pointTypes[numTypes++] = SEG_CUBICTO;
            doubleCoords[numCoords++] = x1;
            doubleCoords[numCoords++] = y1;
            doubleCoords[numCoords++] = x2;
            doubleCoords[numCoords++] = y2;
            doubleCoords[numCoords++] = x3;
            doubleCoords[numCoords++] = y3;
        }

        @Override
		int pointCrossings(double px, double py) {
            double movx, movy, curx, cury, endx, endy;
            double coords[] = doubleCoords;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1; i < numTypes; i++) {
                switch (pointTypes[i]) {
				case GPathIterator.SEG_MOVETO:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
				case GPathIterator.SEG_LINETO:
                    crossings +=
                        Curve.pointCrossingsForLine(px, py,
                                                    curx, cury,
                                                    endx = coords[ci++],
                                                    endy = coords[ci++]);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_QUADTO:
                    crossings +=
                        Curve.pointCrossingsForQuad(px, py,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_CUBICTO:
                    crossings +=
                        Curve.pointCrossingsForCubic(px, py,
                                                     curx, cury,
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     coords[ci++],
                                                     endx = coords[ci++],
                                                     endy = coords[ci++],
                                                     0);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_CLOSE:
                    if (cury != movy) {
                        crossings +=
                            Curve.pointCrossingsForLine(px, py,
                                                        curx, cury,
                                                        movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    break;
                }
            }
            if (cury != movy) {
                crossings +=
                    Curve.pointCrossingsForLine(px, py,
                                                curx, cury,
                                                movx, movy);
            }
            return crossings;
        }

        @Override
		int rectCrossings(double rxmin, double rymin,
                          double rxmax, double rymax)
        {
            double coords[] = doubleCoords;
            double curx, cury, movx, movy, endx, endy;
            curx = movx = coords[0];
            cury = movy = coords[1];
            int crossings = 0;
            int ci = 2;
            for (int i = 1;
                 crossings != Curve.RECT_INTERSECTS && i < numTypes;
                 i++)
            {
                switch (pointTypes[i]) {
				case GPathIterator.SEG_MOVETO:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    // Count should always be a multiple of 2 here.
                    // assert((crossings & 1) != 0);
                    movx = curx = coords[ci++];
                    movy = cury = coords[ci++];
                    break;
				case GPathIterator.SEG_LINETO:
                    endx = coords[ci++];
                    endy = coords[ci++];
                    crossings =
                        Curve.rectCrossingsForLine(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   endx, endy);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_QUADTO:
                    crossings =
                        Curve.rectCrossingsForQuad(crossings,
                                                   rxmin, rymin,
                                                   rxmax, rymax,
                                                   curx, cury,
                                                   coords[ci++],
                                                   coords[ci++],
                                                   endx = coords[ci++],
                                                   endy = coords[ci++],
                                                   0);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_CUBICTO:
                    crossings =
                        Curve.rectCrossingsForCubic(crossings,
                                                    rxmin, rymin,
                                                    rxmax, rymax,
                                                    curx, cury,
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    coords[ci++],
                                                    endx = coords[ci++],
                                                    endy = coords[ci++],
                                                    0);
                    curx = endx;
                    cury = endy;
                    break;
				case GPathIterator.SEG_CLOSE:
                    if (curx != movx || cury != movy) {
                        crossings =
                            Curve.rectCrossingsForLine(crossings,
                                                       rxmin, rymin,
                                                       rxmax, rymax,
                                                       curx, cury,
                                                       movx, movy);
                    }
                    curx = movx;
                    cury = movy;
                    // Count should always be a multiple of 2 here.
                    // assert((crossings & 1) != 0);
                    break;
                }
            }
            if (crossings != Curve.RECT_INTERSECTS &&
                (curx != movx || cury != movy))
            {
                crossings =
                    Curve.rectCrossingsForLine(crossings,
                                               rxmin, rymin,
                                               rxmax, rymax,
                                               curx, cury,
                                               movx, movy);
            }
            // Count should always be a multiple of 2 here.
            // assert((crossings & 1) != 0);
            return crossings;
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final void append(GPathIterator pi, boolean connect) {
            double coords[] = new double[6];
            while (!pi.isDone()) {
                switch (pi.currentSegment(coords)) {
                case SEG_MOVETO:
                    if (!connect || numTypes < 1 || numCoords < 1) {
                        moveTo(coords[0], coords[1]);
                        break;
                    }
                    if (pointTypes[numTypes - 1] != SEG_CLOSE &&
                        doubleCoords[numCoords-2] == coords[0] &&
                        doubleCoords[numCoords-1] == coords[1])
                    {
                        // Collapse out initial moveto/lineto
                        break;
                    }
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_LINETO:
                    lineTo(coords[0], coords[1]);
                    break;
                case SEG_QUADTO:
                    quadTo(coords[0], coords[1],
                           coords[2], coords[3]);
                    break;
                case SEG_CUBICTO:
                    curveTo(coords[0], coords[1],
                            coords[2], coords[3],
                            coords[4], coords[5]);
                    break;
                case SEG_CLOSE:
                    closePath();
                    break;
                }
                pi.next();
                connect = false;
            }
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final void transform(GAffineTransform at) {
            at.transform(doubleCoords, 0, doubleCoords, 0, numCoords / 2);
        }

        /**
         * {@inheritDoc}
         * @since 1.6
         */
        @Override
		public final synchronized Rectangle2D getBounds2D() {
            double x1, y1, x2, y2;
            int i = numCoords;
            if (i > 0) {
                y1 = y2 = doubleCoords[--i];
                x1 = x2 = doubleCoords[--i];
                while (i > 0) {
                    double y = doubleCoords[--i];
                    double x = doubleCoords[--i];
                    if (x < x1) {
						x1 = x;
					}
                    if (y < y1) {
						y1 = y;
					}
                    if (x > x2) {
						x2 = x;
					}
                    if (y > y2) {
						y2 = y;
					}
                }
            } else {
                x1 = y1 = x2 = y2 = 0.0;
            }
            return new Rectangle2D.Double(x1, y1, x2 - x1, y2 - y1);
        }

        /**
		 * {@inheritDoc}
		 * <p>
		 * The iterator for this class is not multi-threaded safe, which means
		 * that the {@code Path2D} class does not guarantee that modifications
		 * to the geometry of this {@code Path2D} object do not affect any
		 * iterations of that geometry that are already in process.
		 *
		 * @param at
		 *            an {@code AffineTransform}
		 * @return a new {@code GPathIterator} that iterates along the boundary
		 *         of this {@code Shape} and provides access to the geometry of
		 *         this {@code Shape}'s outline
		 * @since 1.6
		 */
        @Override
		public final GPathIterator getPathIterator(GAffineTransform at) {
            if (at == null) {
                return new CopyIterator(this);
            }
			return new TxIterator(this, at);
        }

        /**
         * Creates a new object of the same class as this object.
         *
         * @return     a clone of this instance.
         * @exception  OutOfMemoryError    if there is not enough memory.
         * @see        java.lang.Cloneable
         * @since      1.6
         */
		@Override
		public final Object duplicate() {
            // Note: It would be nice to have this return Path2D
            // but one of our subclasses (GeneralPath) needs to
            // offer "public Object clone()" for backwards
            // compatibility so we cannot restrict it further.
            // REMIND: Can we do both somehow?
            return new Path2D.Double(this);
        }




        /**
         * Reads the default serializable fields from the
         * {@code ObjectInputStream} followed by an explicit
         * serialization of the path segments stored in this
         * path.
         * <p>
         * There are no default serializable fields as of 1.6.
         * <p>
         * The serial data for this object is described in the
         * writeObject method.
         *
         * @since 1.6
         */
//        private void readObject(java.io.ObjectInputStream s)
//            throws java.lang.ClassNotFoundException, java.io.IOException
//        {
//            super.readObject(s, true);
//        }

        static class CopyIterator extends Path2D.Iterator {
            double doubleCoords[];

            CopyIterator(Path2D.Double p2dd) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
            }

            @Override
			public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    System.arraycopy(doubleCoords, pointIdx,
                                     coords, 0, numCoords);
                }
                return type;
            }
        }

        static class TxIterator extends Path2D.Iterator {
            double doubleCoords[];
            GAffineTransform affine;

            TxIterator(Path2D.Double p2dd, GAffineTransform at) {
                super(p2dd);
                this.doubleCoords = p2dd.doubleCoords;
                this.affine = at;
            }

            @Override
			public int currentSegment(double[] coords) {
                int type = path.pointTypes[typeIdx];
                int numCoords = curvecoords[type];
                if (numCoords > 0) {
                    affine.transform(doubleCoords, pointIdx,
                                     coords, 0, numCoords / 2);
                }
                return type;
            }
        }

    }

    /**
     * Adds a point to the path by moving to the specified
     * coordinates specified in double precision.
     *
     * @param x the specified X coordinate
     * @param y the specified Y coordinate
     * @since 1.6
     */
    public abstract void moveTo(double x, double y);

    /**
     * Adds a point to the path by drawing a straight line from the
     * current coordinates to the new specified coordinates
     * specified in double precision.
     *
     * @param x the specified X coordinate
     * @param y the specified Y coordinate
     * @since 1.6
     */
    public abstract void lineTo(double x, double y);

    /**
     * Adds a curved segment, defined by two new points, to the path by
     * drawing a Quadratic curve that intersects both the current
     * coordinates and the specified coordinates {@code (x2,y2)},
     * using the specified point {@code (x1,y1)} as a quadratic
     * parametric control point.
     * All coordinates are specified in double precision.
     *
     * @param x1 the X coordinate of the quadratic control point
     * @param y1 the Y coordinate of the quadratic control point
     * @param x2 the X coordinate of the final end point
     * @param y2 the Y coordinate of the final end point
     * @since 1.6
     */
    public abstract void quadTo(double x1, double y1,
                                double x2, double y2);

    /**
     * Adds a curved segment, defined by three new points, to the path by
     * drawing a B&eacute;zier curve that intersects both the current
     * coordinates and the specified coordinates {@code (x3,y3)},
     * using the specified points {@code (x1,y1)} and {@code (x2,y2)} as
     * B&eacute;zier control points.
     * All coordinates are specified in double precision.
     *
     * @param x1 the X coordinate of the first B&eacute;zier control point
     * @param y1 the Y coordinate of the first B&eacute;zier control point
     * @param x2 the X coordinate of the second B&eacute;zier control point
     * @param y2 the Y coordinate of the second B&eacute;zier control point
     * @param x3 the X coordinate of the final end point
     * @param y3 the Y coordinate of the final end point
     * @since 1.6
     */
    public abstract void curveTo(double x1, double y1,
                                 double x2, double y2,
                                 double x3, double y3);

    /**
     * Closes the current subpath by drawing a straight line back to
     * the coordinates of the last {@code moveTo}.  If the path is already
     * closed then this method has no effect.
     *
     * @since 1.6
     */
    public final synchronized void closePath() {
        if (numTypes == 0 || pointTypes[numTypes - 1] != SEG_CLOSE) {
            needRoom(true, 0);
            pointTypes[numTypes++] = SEG_CLOSE;
        }
    }

    /**
     * Appends the geometry of the specified {@code Shape} object to the
     * path, possibly connecting the new geometry to the existing path
     * segments with a line segment.
     * If the {@code connect} parameter is {@code true} and the
     * path is not empty then any initial {@code moveTo} in the
     * geometry of the appended {@code Shape}
     * is turned into a {@code lineTo} segment.
     * If the destination coordinates of such a connecting {@code lineTo}
     * segment match the ending coordinates of a currently open
     * subpath then the segment is omitted as superfluous.
     * The winding rule of the specified {@code Shape} is ignored
     * and the appended geometry is governed by the winding
     * rule specified for this path.
     *
     * @param s the {@code Shape} whose geometry is appended
     *          to this path
     * @param connect a boolean to control whether or not to turn an initial
     *                {@code moveTo} segment into a {@code lineTo} segment
     *                to connect the new geometry to the existing path
     * @since 1.6
     */
    public final void append(GShape s, boolean connect) {
        append(s.getPathIterator(null), connect);
    }

    /**
	 * Appends the geometry of the specified GPathIterator object to the path,
	 * possibly connecting the new geometry to the existing path segments with a
	 * line segment. If the {@code connect} parameter is {@code true} and the
	 * path is not empty then any initial {@code moveTo} in the geometry of the
	 * appended {@code Shape} is turned into a {@code lineTo} segment. If the
	 * destination coordinates of such a connecting {@code lineTo} segment match
	 * the ending coordinates of a currently open subpath then the segment is
	 * omitted as superfluous. The winding rule of the specified {@code Shape}
	 * is ignored and the appended geometry is governed by the winding rule
	 * specified for this path.
	 *
	 * @param pi
	 *            the {@code GPathIterator} whose geometry is appended to this
	 *            path
	 * @param connect
	 *            a boolean to control whether or not to turn an initial
	 *            {@code moveTo} segment into a {@code lineTo} segment to
	 *            connect the new geometry to the existing path
	 * @since 1.6
	 */
    public abstract void append(GPathIterator pi, boolean connect);

    /**
     * Returns the fill style winding rule.
     *
     * @return an integer representing the current winding rule.
     * @see #WIND_EVEN_ODD
     * @see #WIND_NON_ZERO
     * @see #setWindingRule
     * @since 1.6
     */
    public final synchronized int getWindingRule() {
        return windingRule;
    }

    /**
     * Sets the winding rule for this path to the specified value.
     *
     * @param rule an integer representing the specified
     *             winding rule
     * @exception IllegalArgumentException if
     *          {@code rule} is not either
     *          {@link #WIND_EVEN_ODD} or
     *          {@link #WIND_NON_ZERO}
     * @see #getWindingRule
     * @since 1.6
     */
    public final void setWindingRule(int rule) {
        if (rule != WIND_EVEN_ODD && rule != WIND_NON_ZERO) {
            throw new IllegalArgumentException("winding rule must be "+
                                               "WIND_EVEN_ODD or "+
                                               "WIND_NON_ZERO");
        }
        windingRule = rule;
    }

    /**
     * Returns the coordinates most recently added to the end of the path
     * as a {@link GPoint2D} object.
     *
     * @return a {@code GPoint2D} object containing the ending coordinates of
     *         the path or {@code null} if there are no points in the path.
     * @since 1.6
     */
    public final synchronized GPoint2D getCurrentPoint() {
        int index = numCoords;
        if (numTypes < 1 || index < 1) {
            return null;
        }
        if (pointTypes[numTypes - 1] == SEG_CLOSE) {
        loop:
            for (int i = numTypes - 2; i > 0; i--) {
                switch (pointTypes[i]) {
                case SEG_MOVETO:
                    break loop;
                case SEG_LINETO:
                    index -= 2;
                    break;
                case SEG_QUADTO:
                    index -= 4;
                    break;
                case SEG_CUBICTO:
                    index -= 6;
                    break;
                case SEG_CLOSE:
                    break;
                }
            }
        }
        return getPoint(index - 2);
    }

    /**
     * Resets the path to empty.  The append position is set back to the
     * beginning of the path and all coordinates and point types are
     * forgotten.
     *
     * @since 1.6
     */
    public final synchronized void reset() {
        numTypes = numCoords = 0;
    }

    /**
     * Transforms the geometry of this path using the specified
     * {@link AffineTransform}.
     * The geometry is transformed in place, which permanently changes the
     * boundary defined by this object.
     *
     * @param at the {@code AffineTransform} used to transform the area
     * @since 1.6
     */
    public abstract void transform(GAffineTransform at);

    /**
     * Returns a new {@code Shape} representing a transformed version
     * of this {@code Path2D}.
     * Note that the exact type and coordinate precision of the return
     * value is not specified for this method.
     * The method will return a Shape that contains no less precision
     * for the transformed geometry than this {@code Path2D} currently
     * maintains, but it may contain no more precision either.
     * If the tradeoff of precision vs. storage size in the result is
     * important then the convenience constructors in the
     * Path2D.Double(Shape, AffineTransform)
     * subclasses should be used to make the choice explicit.
     *
     * @param at the {@code AffineTransform} used to transform a
     *           new {@code Shape}.
     * @return a new {@code Shape}, transformed with the specified
     *         {@code AffineTransform}.
     * @since 1.6
     */
    public final synchronized Shape createTransformedShape(GAffineTransform at) {
        Path2D p2d = (Path2D) duplicate();
        if (at != null) {
            p2d.transform(at);
        }
        return p2d;
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    @Override
	public final GRectangle getBounds() {
        return getBounds2D().getBounds();
    }

    /**
	 * Tests if the specified coordinates are inside the closed boundary of the
	 * specified {@link GPathIterator}.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#contains(double, double)} method.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param x
	 *            the specified X coordinate
	 * @param y
	 *            the specified Y coordinate
	 * @return {@code true} if the specified coordinates are inside the
	 *         specified {@code GPathIterator}; {@code false} otherwise
	 * @since 1.6
	 */
    public static boolean contains(GPathIterator pi, double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            /* N * 0.0 is 0.0 only if N is finite.
             * Here we know that both x and y are finite.
             */
            int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 1);
            int cross = Curve.pointCrossingsForPath(pi, x, y);
            return ((cross & mask) != 0);
        }
		/* Either x or y was infinite or NaN.
		 * A NaN always produces a negative response to any test
		 * and Infinity values cannot be "inside" any path so
		 * they should return false as well.
		 */
		return false;
    }

    /**
	 * Tests if the specified {@link GPoint2D} is inside the closed boundary of
	 * the specified {@link GPathIterator}.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#contains(GPoint2D)} method.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param p
	 *            the specified {@code Point2D}
	 * @return {@code true} if the specified coordinates are inside the
	 *         specified {@code GPathIterator}; {@code false} otherwise
	 * @since 1.6
	 */
    public static boolean contains(GPathIterator pi, GPoint2D p) {
        return contains(pi, p.getX(), p.getY());
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    @Override
	public final boolean contains(double x, double y) {
        if (x * 0.0 + y * 0.0 == 0.0) {
            /* N * 0.0 is 0.0 only if N is finite.
             * Here we know that both x and y are finite.
             */
            if (numTypes < 2) {
                return false;
            }
            int mask = (windingRule == WIND_NON_ZERO ? -1 : 1);
            return ((pointCrossings(x, y) & mask) != 0);
        }
		/* Either x or y was infinite or NaN.
		 * A NaN always produces a negative response to any test
		 * and Infinity values cannot be "inside" any path so
		 * they should return false as well.
		 */
		return false;
    }

    /**
     * {@inheritDoc}
     * @since 1.6
     */
    @Override
	public final boolean contains(GPoint2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
	 * Tests if the specified rectangular area is entirely inside the closed
	 * boundary of the specified {@link GPathIterator}.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#contains(double, double, double, double)} method.
	 * <p>
	 * This method object may conservatively return false in cases where the
	 * specified rectangular area intersects a segment of the path, but that
	 * segment does not represent a boundary between the interior and exterior
	 * of the path. Such segments could lie entirely within the interior of the
	 * path if they are part of a path with a {@link #WIND_NON_ZERO} winding
	 * rule or if the segments are retraced in the reverse direction such that
	 * the two sets of segments cancel each other out without any exterior area
	 * falling between them. To determine whether segments represent true
	 * boundaries of the interior of the path would require extensive
	 * calculations involving all of the segments of the path and the winding
	 * rule and are thus beyond the scope of this implementation.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param x
	 *            the specified X coordinate
	 * @param y
	 *            the specified Y coordinate
	 * @param w
	 *            the width of the specified rectangular area
	 * @param h
	 *            the height of the specified rectangular area
	 * @return {@code true} if the specified {@code GPathIterator} contains the
	 *         specified rectangular area; {@code false} otherwise.
	 * @since 1.6
	 */
    public static boolean contains(GPathIterator pi,
                                   double x, double y, double w, double h)
    {
        if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinite values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }
        int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 2);
        int crossings = Curve.rectCrossingsForPath(pi, x, y, x+w, y+h);
        return (crossings != Curve.RECT_INTERSECTS &&
                (crossings & mask) != 0);
    }

    /**
	 * Tests if the specified {@link Rectangle2D} is entirely inside the closed
	 * boundary of the specified {@link GPathIterator}.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#contains(GRectangle2D)} method.
	 * <p>
	 * This method object may conservatively return false in cases where the
	 * specified rectangular area intersects a segment of the path, but that
	 * segment does not represent a boundary between the interior and exterior
	 * of the path. Such segments could lie entirely within the interior of the
	 * path if they are part of a path with a {@link #WIND_NON_ZERO} winding
	 * rule or if the segments are retraced in the reverse direction such that
	 * the two sets of segments cancel each other out without any exterior area
	 * falling between them. To determine whether segments represent true
	 * boundaries of the interior of the path would require extensive
	 * calculations involving all of the segments of the path and the winding
	 * rule and are thus beyond the scope of this implementation.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param r
	 *            a specified {@code Rectangle2D}
	 * @return {@code true} if the specified {@code GPathIterator} contains the
	 *         specified {@code Rectangle2D}; {@code false} otherwise.
	 * @since 1.6
	 */
    public static boolean contains(GPathIterator pi, Rectangle2D r) {
        return contains(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method object may conservatively return false in
     * cases where the specified rectangular area intersects a
     * segment of the path, but that segment does not represent a
     * boundary between the interior and exterior of the path.
     * Such segments could lie entirely within the interior of the
     * path if they are part of a path with a {@link #WIND_NON_ZERO}
     * winding rule or if the segments are retraced in the reverse
     * direction such that the two sets of segments cancel each
     * other out without any exterior area falling between them.
     * To determine whether segments represent true boundaries of
     * the interior of the path would require extensive calculations
     * involving all of the segments of the path and the winding
     * rule and are thus beyond the scope of this implementation.
     *
     * @since 1.6
     */
    @Override
	public final boolean contains(double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinite values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }
        int mask = (windingRule == WIND_NON_ZERO ? -1 : 2);
        int crossings = rectCrossings(x, y, x+w, y+h);
        return (crossings != Curve.RECT_INTERSECTS &&
                (crossings & mask) != 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method object may conservatively return false in
     * cases where the specified rectangular area intersects a
     * segment of the path, but that segment does not represent a
     * boundary between the interior and exterior of the path.
     * Such segments could lie entirely within the interior of the
     * path if they are part of a path with a {@link #WIND_NON_ZERO}
     * winding rule or if the segments are retraced in the reverse
     * direction such that the two sets of segments cancel each
     * other out without any exterior area falling between them.
     * To determine whether segments represent true boundaries of
     * the interior of the path would require extensive calculations
     * involving all of the segments of the path and the winding
     * rule and are thus beyond the scope of this implementation.
     *
     * @since 1.6
     */
    @Override
	public final boolean contains(GRectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
	 * Tests if the interior of the specified {@link GPathIterator} intersects
	 * the interior of a specified set of rectangular coordinates.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#intersects(double, double, double, double)} method.
	 * <p>
	 * This method object may conservatively return true in cases where the
	 * specified rectangular area intersects a segment of the path, but that
	 * segment does not represent a boundary between the interior and exterior
	 * of the path. Such a case may occur if some set of segments of the path
	 * are retraced in the reverse direction such that the two sets of segments
	 * cancel each other out without any interior area between them. To
	 * determine whether segments represent true boundaries of the interior of
	 * the path would require extensive calculations involving all of the
	 * segments of the path and the winding rule and are thus beyond the scope
	 * of this implementation.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param x
	 *            the specified X coordinate
	 * @param y
	 *            the specified Y coordinate
	 * @param w
	 *            the width of the specified rectangular coordinates
	 * @param h
	 *            the height of the specified rectangular coordinates
	 * @return {@code true} if the specified {@code GPathIterator} and the
	 *         interior of the specified set of rectangular coordinates
	 *         intersect each other; {@code false} otherwise.
	 * @since 1.6
	 */
    public static boolean intersects(GPathIterator pi,
                                     double x, double y, double w, double h)
    {
        if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinite values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }
        int mask = (pi.getWindingRule() == WIND_NON_ZERO ? -1 : 2);
        int crossings = Curve.rectCrossingsForPath(pi, x, y, x+w, y+h);
        return (crossings == Curve.RECT_INTERSECTS ||
                (crossings & mask) != 0);
    }

    /**
	 * Tests if the interior of the specified {@link GPathIterator} intersects
	 * the interior of a specified {@link Rectangle2D}.
	 * <p>
	 * This method provides a basic facility for implementors of the
	 * {@link Shape} interface to implement support for the
	 * {@link Shape#intersects(GRectangle2D)} method.
	 * <p>
	 * This method object may conservatively return true in cases where the
	 * specified rectangular area intersects a segment of the path, but that
	 * segment does not represent a boundary between the interior and exterior
	 * of the path. Such a case may occur if some set of segments of the path
	 * are retraced in the reverse direction such that the two sets of segments
	 * cancel each other out without any interior area between them. To
	 * determine whether segments represent true boundaries of the interior of
	 * the path would require extensive calculations involving all of the
	 * segments of the path and the winding rule and are thus beyond the scope
	 * of this implementation.
	 *
	 * @param pi
	 *            the specified {@code GPathIterator}
	 * @param r
	 *            the specified {@code Rectangle2D}
	 * @return {@code true} if the specified {@code GPathIterator} and the
	 *         interior of the specified {@code Rectangle2D} intersect each
	 *         other; {@code false} otherwise.
	 * @since 1.6
	 */
    public static boolean intersects(GPathIterator pi, Rectangle2D r) {
        return intersects(pi, r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method object may conservatively return true in
     * cases where the specified rectangular area intersects a
     * segment of the path, but that segment does not represent a
     * boundary between the interior and exterior of the path.
     * Such a case may occur if some set of segments of the
     * path are retraced in the reverse direction such that the
     * two sets of segments cancel each other out without any
     * interior area between them.
     * To determine whether segments represent true boundaries of
     * the interior of the path would require extensive calculations
     * involving all of the segments of the path and the winding
     * rule and are thus beyond the scope of this implementation.
     *
     * @since 1.6
     */
    @Override
	public final boolean intersects(double x, double y, double w, double h) {
        if (java.lang.Double.isNaN(x+w) || java.lang.Double.isNaN(y+h)) {
            /* [xy]+[wh] is NaN if any of those values are NaN,
             * or if adding the two together would produce NaN
             * by virtue of adding opposing Infinite values.
             * Since we need to add them below, their sum must
             * not be NaN.
             * We return false because NaN always produces a
             * negative response to tests
             */
            return false;
        }
        if (w <= 0 || h <= 0) {
            return false;
        }
        int mask = (windingRule == WIND_NON_ZERO ? -1 : 2);
        int crossings = rectCrossings(x, y, x+w, y+h);
        return (crossings == Curve.RECT_INTERSECTS ||
                (crossings & mask) != 0);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method object may conservatively return true in
     * cases where the specified rectangular area intersects a
     * segment of the path, but that segment does not represent a
     * boundary between the interior and exterior of the path.
     * Such a case may occur if some set of segments of the
     * path are retraced in the reverse direction such that the
     * two sets of segments cancel each other out without any
     * interior area between them.
     * To determine whether segments represent true boundaries of
     * the interior of the path would require extensive calculations
     * involving all of the segments of the path and the winding
     * rule and are thus beyond the scope of this implementation.
     *
     * @since 1.6
     */
    @Override
	public final boolean intersects(GRectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The iterator for this class is not multi-threaded safe,
     * which means that this {@code Path2D} class does not
     * guarantee that modifications to the geometry of this
     * {@code Path2D} object do not affect any iterations of
     * that geometry that are already in process.
     *
     * @since 1.6
     */
    @Override
	public final GPathIterator getPathIterator(GAffineTransform at,
                                              double flatness)
    {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }

    /**
     * Creates a new object of the same class as this object.
     *
     * @return     a clone of this instance.
     * @exception  OutOfMemoryError            if there is not enough memory.
     * @see        java.lang.Cloneable
     * @since      1.6
     */
	public abstract Object duplicate();
        // Note: It would be nice to have this return Path2D
        // but one of our subclasses (GeneralPath) needs to
        // offer "public Object clone()" for backwards
        // compatibility so we cannot restrict it further.
        // REMIND: Can we do both somehow?

    /*
     * Support fields and methods for serializing the subclasses.
     */
	// private static final byte SERIAL_STORAGE_FLT_ARRAY = 0x30;
	// private static final byte SERIAL_STORAGE_DBL_ARRAY = 0x31;
	//
	// private static final byte SERIAL_SEG_FLT_MOVETO = 0x40;
	// private static final byte SERIAL_SEG_FLT_LINETO = 0x41;
	// private static final byte SERIAL_SEG_FLT_QUADTO = 0x42;
	// private static final byte SERIAL_SEG_FLT_CUBICTO = 0x43;
	//
	// private static final byte SERIAL_SEG_DBL_MOVETO = 0x50;
	// private static final byte SERIAL_SEG_DBL_LINETO = 0x51;
	// private static final byte SERIAL_SEG_DBL_QUADTO = 0x52;
	// private static final byte SERIAL_SEG_DBL_CUBICTO = 0x53;
	//
	// private static final byte SERIAL_SEG_CLOSE = 0x60;
	// private static final byte SERIAL_PATH_END = 0x61;

//    final void writeObject(java.io.ObjectOutputStream s, boolean isdbl)
//        throws java.io.IOException
//    {
//        s.defaultWriteObject();
//
//        float fCoords[];
//        double dCoords[];
//
//        if (isdbl) {
//            dCoords = ((Path2D.Double) this).doubleCoords;
//            fCoords = null;
//        } else {
//            fCoords = ((Path2D.Float) this).floatCoords;
//            dCoords = null;
//        }
//
//        int numTypes = this.numTypes;
//
//        s.writeByte(isdbl
//                    ? SERIAL_STORAGE_DBL_ARRAY
//                    : SERIAL_STORAGE_FLT_ARRAY);
//        s.writeInt(numTypes);
//        s.writeInt(numCoords);
//        s.writeByte((byte) windingRule);
//
//        int cindex = 0;
//        for (int i = 0; i < numTypes; i++) {
//            int npoints;
//            byte serialtype;
//            switch (pointTypes[i]) {
//            case SEG_MOVETO:
//                npoints = 1;
//                serialtype = (isdbl
//                              ? SERIAL_SEG_DBL_MOVETO
//                              : SERIAL_SEG_FLT_MOVETO);
//                break;
//            case SEG_LINETO:
//                npoints = 1;
//                serialtype = (isdbl
//                              ? SERIAL_SEG_DBL_LINETO
//                              : SERIAL_SEG_FLT_LINETO);
//                break;
//            case SEG_QUADTO:
//                npoints = 2;
//                serialtype = (isdbl
//                              ? SERIAL_SEG_DBL_QUADTO
//                              : SERIAL_SEG_FLT_QUADTO);
//                break;
//            case SEG_CUBICTO:
//                npoints = 3;
//                serialtype = (isdbl
//                              ? SERIAL_SEG_DBL_CUBICTO
//                              : SERIAL_SEG_FLT_CUBICTO);
//                break;
//            case SEG_CLOSE:
//                npoints = 0;
//                serialtype = SERIAL_SEG_CLOSE;
//                break;
//
//            default:
//                // Should never happen
//                throw new Error("unrecognized path type");
//            }
//            s.writeByte(serialtype);
//            while (--npoints >= 0) {
//                if (isdbl) {
//                    s.writeDouble(dCoords[cindex++]);
//                    s.writeDouble(dCoords[cindex++]);
//                } else {
//                    s.writeFloat(fCoords[cindex++]);
//                    s.writeFloat(fCoords[cindex++]);
//                }
//            }
//        }
//        s.writeByte(SERIAL_PATH_END);
//    }

//    final void readObject(java.io.ObjectInputStream s, boolean storedbl)
//        throws java.lang.ClassNotFoundException, java.io.IOException
//    {
//        s.defaultReadObject();
//
//        // The subclass calls this method with the storage type that
//        // they want us to use (storedbl) so we ignore the storage
//        // method hint from the stream.
//        s.readByte();
//        int nT = s.readInt();
//        int nC = s.readInt();
//        try {
//            setWindingRule(s.readByte());
//        } catch (IllegalArgumentException iae) {
//            throw new java.io.InvalidObjectException(iae.getMessage());
//        }
//
//        pointTypes = new byte[(nT < 0) ? INIT_SIZE : nT];
//        if (nC < 0) {
//            nC = INIT_SIZE * 2;
//        }
//        if (storedbl) {
//            ((Path2D.Double) this).doubleCoords = new double[nC];
//        } else {
//            ((Path2D.Float) this).floatCoords = new float[nC];
//        }
//
//    PATHDONE:
//        for (int i = 0; nT < 0 || i < nT; i++) {
//            boolean isdbl;
//            int npoints;
//            byte segtype;
//
//            byte serialtype = s.readByte();
//            switch (serialtype) {
//            case SERIAL_SEG_FLT_MOVETO:
//                isdbl = false;
//                npoints = 1;
//                segtype = SEG_MOVETO;
//                break;
//            case SERIAL_SEG_FLT_LINETO:
//                isdbl = false;
//                npoints = 1;
//                segtype = SEG_LINETO;
//                break;
//            case SERIAL_SEG_FLT_QUADTO:
//                isdbl = false;
//                npoints = 2;
//                segtype = SEG_QUADTO;
//                break;
//            case SERIAL_SEG_FLT_CUBICTO:
//                isdbl = false;
//                npoints = 3;
//                segtype = SEG_CUBICTO;
//                break;
//
//            case SERIAL_SEG_DBL_MOVETO:
//                isdbl = true;
//                npoints = 1;
//                segtype = SEG_MOVETO;
//                break;
//            case SERIAL_SEG_DBL_LINETO:
//                isdbl = true;
//                npoints = 1;
//                segtype = SEG_LINETO;
//                break;
//            case SERIAL_SEG_DBL_QUADTO:
//                isdbl = true;
//                npoints = 2;
//                segtype = SEG_QUADTO;
//                break;
//            case SERIAL_SEG_DBL_CUBICTO:
//                isdbl = true;
//                npoints = 3;
//                segtype = SEG_CUBICTO;
//                break;
//
//            case SERIAL_SEG_CLOSE:
//                isdbl = false;
//                npoints = 0;
//                segtype = SEG_CLOSE;
//                break;
//
//            case SERIAL_PATH_END:
//                if (nT < 0) {
//                    break PATHDONE;
//                }
//                throw new StreamCorruptedException("unexpected PATH_END");
//
//            default:
//                throw new StreamCorruptedException("unrecognized path type");
//            }
//            needRoom(segtype != SEG_MOVETO, npoints * 2);
//            if (isdbl) {
//                while (--npoints >= 0) {
//                    append(s.readDouble(), s.readDouble());
//                }
//            } else {
//                while (--npoints >= 0) {
//                    append(s.readFloat(), s.readFloat());
//                }
//            }
//            pointTypes[numTypes++] = segtype;
//        }
//        if (nT >= 0 && s.readByte() != SERIAL_PATH_END) {
//            throw new StreamCorruptedException("missing PATH_END");
//        }
//    }

    static abstract class Iterator implements GPathIterator {
        int typeIdx;
        int pointIdx;
        Path2D path;

        static final int curvecoords[] = {2, 2, 4, 6, 0};

        Iterator(Path2D path) {
            this.path = path;
        }

        @Override
		public int getWindingRule() {
            return path.getWindingRule();
        }

        @Override
		public boolean isDone() {
            return (typeIdx >= path.numTypes);
        }

        @Override
		public void next() {
            int type = path.pointTypes[typeIdx++];
            pointIdx += curvecoords[type];
        }
    }
    
	@Override
	public boolean intersects(int x, int y, int w, int h) {
		return intersects((double)x, (double)y, (double)w, (double)h);
	}

	@Override
	public boolean contains(int x, int y) {
		return contains((double)x, (double)y);
	}

}
