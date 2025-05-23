/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

/*
 * GeoVec2D.java
 *
 * Created on 31. August 2001, 11:34
 */

package org.geogebra.common.kernel.arithmetic;

import static org.geogebra.common.kernel.arithmetic.MyList.isEquation;

import java.util.Set;
import java.util.stream.Stream;

import org.geogebra.common.kernel.Construction;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.arithmetic.vector.VectorPrinterMapBuilder2D;
import org.geogebra.common.kernel.commands.EvalInfo;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoVec2D;
import org.geogebra.common.kernel.printing.printable.vector.PrintableVector;
import org.geogebra.common.kernel.printing.printer.vector.VectorNodeStringifier;
import org.geogebra.common.kernel.printing.printer.vector.VectorPrinterMapBuilder;
import org.geogebra.common.kernel.printing.printer.vector.VectorPrintingMode;

import com.google.j2objc.annotations.Weak;

/**
 * 
 * @author Markus
 */
public class MyVecNode extends ValidExpression
		implements VectorValue, MyVecNDNode, PrintableVector {

	/**
	 * x coordinate
	 */
	protected ExpressionValue x;
	/**
	 * y coordinate
	 */
	protected ExpressionValue y;

	private VectorNodeStringifier stringifier;
	private int mode = Kernel.COORD_CARTESIAN;
	@Weak
	private Kernel kernel;
	private boolean isCASVector;

	/**
	 * Creates new MyVec2D
	 * 
	 * @param kernel
	 *            kernel
	 */
	public MyVecNode(Kernel kernel) {
		this.kernel = kernel;
		VectorPrinterMapBuilder builder = new VectorPrinterMapBuilder2D();
		stringifier = new VectorNodeStringifier(this,
				builder.build(kernel.getApplication().getSettings().getGeneral()));
		stringifier.setPrintingMode(VectorPrintingMode.Cartesian);
	}

	/**
	 * Creates new MyVec2D with coordinates (x,y) as ExpressionNodes. Both
	 * nodes must evaluate to NumberValues.
	 * 
	 * @param kernel
	 *            kernel
	 * @param x
	 *            x-coord
	 * @param y
	 *            y-coord
	 */
	public MyVecNode(Kernel kernel, ExpressionValue x, ExpressionValue y) {
		this(kernel);
		setCoords(x, y);
	}

	@Override
	public MyVecNode deepCopy(Kernel kernel1) {
		MyVecNode ret = new MyVecNode(kernel1, x.deepCopy(kernel1),
				y.deepCopy(kernel1));
		ret.setMode(mode);
		if (isCASVector()) {
			ret.setupCASVector();
		}
		return ret;
	}

	@Override
	public void resolveVariables(EvalInfo info) {
		x.resolveVariables(info);
		y.resolveVariables(info);
	}

	/**
	 * @return x-coord
	 */
	@Override
	public ExpressionValue getX() {
		return x;
	}

	/**
	 * @return y-coord
	 */
	@Override
	public ExpressionValue getY() {
		return y;
	}

	/**
	 * @param r
	 *            radius
	 * @param phi
	 *            phase
	 */
	public void setPolarCoords(ExpressionValue r, ExpressionValue phi) {
		setCoords(r, phi);
		setMode(Kernel.COORD_POLAR);
	}

	/**
	 * @return true if uses polar coordinates
	 */
	public boolean hasPolarCoords() {
		return mode == Kernel.COORD_POLAR;
	}

	private void setCoords(ExpressionValue x, ExpressionValue y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString(StringTemplate tpl) {
		return hasPolarCoords()
				? stringifier.toString(tpl, VectorPrintingMode.Polar) : stringifier.toString(tpl);
	}

	@Override
	public String toValueString(StringTemplate tpl) {
		if (tpl.getStringType().isGiac() && isEquation(x) && isEquation(y)) {
			Traversing.VariableReplacer replacer = new Traversing.VariableReplacer(kernel);
			Stream.of("x", "y", "z").forEach(varName ->
					replacer.addVars(varName, new FunctionVariable(kernel, varName))
			);
			ExpressionValue xCopy = x.deepCopy(kernel).traverse(replacer);
			ExpressionValue yCopy = y.deepCopy(kernel).traverse(replacer);
			Construction cons = kernel.getConstruction();
			boolean suppressLabelsActive = cons.isSuppressLabelsActive();
			cons.setSuppressLabelCreation(true);
			GeoElement[] geos = kernel.getAlgebraProcessor()
					.processEquationIntersect(xCopy, yCopy);
			cons.setSuppressLabelCreation(suppressLabelsActive);
			return geos[0].toValueString(tpl);
		}
		return stringifier.toValueString(tpl);
	}

	@Override
	public String toLaTeXString(boolean symbolic, StringTemplate tpl) {
		return symbolic ? stringifier.toString(tpl) : stringifier.toValueString(tpl);
	}

	/**
	 * interface VectorValue implementation
	 */
	@Override
	public GeoVec2D getVector() {
		GeoVec2D ret;
		if (mode == Kernel.COORD_POLAR) {
			double r = x.evaluateDouble();
			// allow negative radius for US
			double phi = y.evaluateDouble();
			ret = new GeoVec2D(kernel, r * Math.cos(phi), r * Math.sin(phi));
		} else { // CARTESIAN
			ret = new GeoVec2D(kernel, x.evaluateDouble(), y.evaluateDouble());
		}
		ret.setMode(mode);
		return ret;
	}

	@Override
	public boolean isConstant() {
		return x.isConstant() && y.isConstant();
	}

	@Override
	public boolean isLeaf() {
		return true;
	}

	/** POLAR or CARTESIAN */
	@Override
	public int getToStringMode() {
		return mode;
	}

	/** returns all GeoElement objects in the both coordinate subtrees */
	@Override
	public void getVariables(Set<GeoElement> variables, SymbolicMode symbolicMode) {
		x.getVariables(variables, symbolicMode);
		y.getVariables(variables, symbolicMode);
	}

	@Override
	public void setMode(int mode) {
		this.mode = mode;

		if (mode == Kernel.COORD_CARTESIAN) {
			stringifier.setPrintingMode(VectorPrintingMode.Cartesian);
		} else {
			stringifier.setPrintingMode(VectorPrintingMode.Polar);
		}
	}

	// could be vector or point
	@Override
	public ValueType getValueType() {
		return this.mode != Kernel.COORD_COMPLEX ? ValueType.NONCOMPLEX2D
				: ValueType.COMPLEX;
	}

	// could be vector or point
	@Override
	public boolean evaluatesToVectorNotPoint() {
		return isCASVector; // this.mode != Kernel.COORD_COMPLEX;
	}

	@Override
	public boolean isNumberValue() {
		return false;
	}

	@Override
	final public boolean contains(ExpressionValue ev) {
		return ev == this;
	}

	@Override
	public String toOutputValueString(StringTemplate tpl) {
		return toValueString(tpl);
	}

	/**
	 * @return kernel
	 */
	public Kernel getKernel() {
		return kernel;
	}

	@Override
	public ExpressionValue traverse(Traversing t) {
		ExpressionValue v = t.process(this);
		if (v != this) {
			return v;
		}
		x = x.traverse(t);
		y = y.traverse(t);
		return this;
	}

	@Override
	public int getChildCount() {
		return 2;
	}

	@Override
	public ExpressionValue getChild(int index) {
		switch (index) {
		case 0: return x;
		case 1: return y;
		default: return super.getChild(index);
		}
	}

	@Override
	public boolean hasCoords() {
		return true;
	}

	@Override
	public void setupCASVector() {
		isCASVector = true;
		setVectorPrintingMode();
	}

	@Override
	public ExpressionNode wrap() {
		return new ExpressionNode(kernel, this);
	}

	@Override
	public boolean isCASVector() {
		return isCASVector;
	}

	@Override
	public int getDimension() {
		return 2;
	}

	@Override
	public ExpressionValue getUndefinedCopy(Kernel kernel1) {
		return new GeoVec2D(kernel1, Double.NaN, Double.NaN);
	}

	@Override
	public double[] getPointAsDouble() {
		return new double[] { x.evaluateDouble(), y.evaluateDouble(), 0 };
	}

	@Override
	public void replaceChildrenByValues(GeoElement geo) {
		if (x instanceof ReplaceChildrenByValues) {
			((ReplaceChildrenByValues) x).replaceChildrenByValues(geo);
		}
		if (y instanceof ReplaceChildrenByValues) {
			((ReplaceChildrenByValues) y).replaceChildrenByValues(geo);
		}

	}

	@Override
	public ExpressionValue evaluate(StringTemplate tpl) {
		// MyNumberPair used for datafunction -- don't simplify
		if (!(this instanceof MyNumberPair)
				&& (x.evaluatesToList() || y.evaluatesToList())) {
			MyList result = new MyList(kernel);
			ExpressionValue xEval = x.evaluate(tpl);
			ExpressionValue yEval = y.evaluate(tpl);
			int size = 0;
			int maxSize = Integer.MAX_VALUE;
			if (xEval instanceof ListValue) {
				maxSize = size = ((ListValue) xEval).size();
			} else if (x.wrap().containsFreeFunctionVariable(null)) {
				// function -> undo evaluation to keep variables
				xEval = x.deepCopy(kernel);
			}
			if (yEval instanceof ListValue) {
				size = Math.min(((ListValue) yEval).size(), maxSize);
			} else if (y.wrap().containsFreeFunctionVariable(null)) {
				// function -> undo evaluation to keep variables
				yEval = y.deepCopy(kernel);
			}
			for (int idx = 0; idx < size; idx++) {
				MyVecNode el = new MyVecNode(kernel, MyList.get(xEval, idx),
						MyList.get(yEval, idx));
				el.setMode(mode);
				result.addListElement(el);
			}
			return result;
		}
		return super.evaluate(tpl);
	}

	@Override
	public ExpressionValue getZ() {
		return null;
	}

    @Override
	public int getCoordinateSystem() {
		return mode;
	}

	@Override
	public void setVectorPrintingMode() {
		stringifier.setPrintingMode(VectorPrintingMode.Vector);
	}
}
