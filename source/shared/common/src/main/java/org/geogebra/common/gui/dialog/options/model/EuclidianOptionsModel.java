package org.geogebra.common.gui.dialog.options.model;

import java.util.Arrays;
import java.util.List;

import org.geogebra.common.annotation.MissingDoc;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GFont;
import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.euclidian.background.BackgroundType;
import org.geogebra.common.geogebra3D.euclidian3D.EuclidianView3D;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.StringTemplate;
import org.geogebra.common.kernel.algos.AlgoFractionText;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.main.App;
import org.geogebra.common.main.Localization;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.common.main.settings.EuclidianSettings3D;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.util.DoubleUtil;
import org.geogebra.common.util.StringUtil;

import com.himamis.retex.editor.share.util.Unicode;

public class EuclidianOptionsModel {
	public static final int MAX_AXES_STYLE_COUNT = 5;
	public static final int X_AXIS = 0;
	public static final int Y_AXIS = 1;
	public static final int Z_AXIS = 2;
	private final App app;
	private EuclidianView view;

	public EuclidianOptionsModel(App app, EuclidianView view) {
		this.app = app;
		this.view = view;
	}

	public static int getAxesStyleLength() {
		return MAX_AXES_STYLE_COUNT;
	}

	public void setView(EuclidianView view) {
		this.view = view;
	}

	public void applyBackgroundColor(int viewIdx, GColor color) {
		if (viewIdx == 1 || viewIdx == 2 || viewIdx == 3) {
			app.getSettings().getEuclidian(viewIdx).setBackground(color);
			return;
		}

		EuclidianSettings settings = view.getSettings();
		if (settings != null) {
			settings.setBackground(color);
			return;
		}

		view.setBackground(view.getBackgroundCommon());
	}

	public void applyAxesColor(GColor col) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setAxesColor(col);
			return;
		}

		view.setAxesColor(col);
	}

	public EuclidianSettings getSettings() {
		if (view == app.getEuclidianView1()) {
			return app.getSettings().getEuclidian(1);
		}
		if (app.hasEuclidianView2EitherShowingOrNot(1)
				&& app.getEuclidianView2(1) == view) {
			return app.getSettings().getEuclidian(2);
		}
		if (app.isEuclidianView3D(view)) {
			return app.getSettings().getEuclidian(3);
		}
		return view.getSettings();
	}

	public void applyGridColor(GColor col) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setGridColor(col);
			return;
		}

		view.setGridColor(col);
	}

	public void applyRulerColor(GColor col) {
		view.getSettings().setBgRulerColor(col);
		view.repaintView();
	}

	public void applyBoldRuler(boolean bold) {
		view.getSettings().setRulerBold(bold);
		view.repaintView();
	}

	public void applyTooltipMode(int mode0) {
		int mode = mode0;
		if (mode == 0) {
			mode = EuclidianStyleConstants.TOOLTIPS_ON;
		} else if (mode == 1) {
			mode = EuclidianStyleConstants.TOOLTIPS_AUTOMATIC;
		} else if (mode == 2) {
			mode = EuclidianStyleConstants.TOOLTIPS_OFF;
		}

		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setAllowToolTips(mode);
			return;
		}

		view.setAllowToolTips(mode);
	}

	public void showAxes(boolean value) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setShowAxes(value);
			return;
		}

		view.setShowAxes(value, true);
	}

	public void applyBoldAxes(boolean isBold) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setBoldAxes(isBold);
			return;
		}

		view.setBoldAxes(isBold);
	}

	public void showGrid(boolean value) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.showGrid(value);
			return;
		}

		view.showGrid(value);
	}

	public void applyBoldGrid(boolean value) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setGridIsBold(value);
			return;
		}

		view.setGridIsBold(value);
	}

	public void applyMouseCoords(boolean value) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setAllowShowMouseCoords(value);
			return;
		}

		view.setAllowShowMouseCoords(value);
	}

	public void applyGridType(int type) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setGridType(type);
			return;
		}

		view.setGridType(type);
		if (type == EuclidianView.GRID_POLAR) {
			view.updateBounds(true, true);
		}
	}

	public void applyAxesStyle(int style) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setAxesLineStyle(style);
			return;
		}

		view.setAxesLineStyle(style);
	}

	public void applyGridStyle(int style) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setGridLineStyle(style);
			return;
		}

		view.setGridLineStyle(style);
	}

	public void applyRulerStyle(int style) {
		view.getSettings().setRulerLineStyle(style);
		view.repaintView();
	}

	public void applyGridManualTick(boolean value) {
		EuclidianSettings settings = getSettings();
		if (settings != null) {
			settings.setAutomaticGridDistance(!value, true);
			return;
		}

		view.setAutomaticGridDistance(!value);
	}

	public void applyAxesRatio(double xval, double yval) {
		if (!(Double.isInfinite(xval) || Double.isNaN(xval)
				|| Double.isInfinite(yval) || Double.isNaN(yval))) {
			// ratio = xval / yval
			// xscale / yscale = ratio
			// => yscale = xscale * xval/yval
			view.setCoordSystem(view.getXZero(), view.getYZero(),
					view.getXscale(), view.getXscale() * xval / yval);
		}
	}

	public void applyLockRatio(double value, IBasicTab basicTab) {
		view.getSettings().setLockedAxesRatio(value);
		basicTab.enableAxesRatio(
				view.isZoomable() && !view.isLockedAxesRatio());
	}

	public void applyMinMax(String text, MinMaxType type, IBasicTab basicTab) {
		NumberValue minMax = app.getKernel().getAlgebraProcessor()
				.evaluateToNumeric(text, false);
		// not parsed to number => return all
		if (minMax == null) {
			basicTab.setMinMaxText(
					view.getXminObject().getLabel(StringTemplate.editTemplate),
					view.getXmaxObject().getLabel(StringTemplate.editTemplate),
					view.getYminObject().getLabel(StringTemplate.editTemplate),
					view.getYmaxObject().getLabel(StringTemplate.editTemplate),
					((EuclidianView3D) view).getZminObject().getLabel(StringTemplate.editTemplate),
					((EuclidianView3D) view).getZmaxObject().getLabel(StringTemplate.editTemplate));
		} else {
			final EuclidianSettings settings = getSettings();
			if (settings instanceof EuclidianSettings3D) {
				((EuclidianSettings3D) settings).setUpdateScaleOrigin(true);
			}

			switch (type) {
			case maxX:
				if (settings != null) {
					settings.setXmaxObject(minMax, true);
				} else {
					view.setXmaxObject(minMax);
				}
				break;
			case maxY:
				if (settings != null) {
					settings.setYmaxObject(minMax, true);
				} else {
					view.setYmaxObject(minMax);
				}

				break;
			case minX:
				if (settings != null) {
					settings.setXminObject(minMax, true);
				} else {
					view.setXminObject(minMax);
				}
				break;
			case minY:
				if (settings != null) {
					settings.setYminObject(minMax, true);
				} else {
					view.setYminObject(minMax);
				}
				break;
			case minZ:
				if (settings != null) {
					((EuclidianSettings3D) settings).setZminObject(minMax, true);
				} else {
					((EuclidianView3D) view).setZminObject(minMax);
				}
				break;
			case maxZ:
				if (settings != null) {
					((EuclidianSettings3D) settings).setZmaxObject(minMax, true);
				} else {
					((EuclidianView3D) view).setZmaxObject(minMax);
				}
				break;
			default:
				break;
			}

			view.setXminObject(view.getXminObject());

			basicTab.enableAxesRatio(
					(view.isZoomable() && !view.isLockedAxesRatio()));

			view.updateBounds(true, true);
		}
	}

	public String[] fillTooltipCombo() {
		Localization loc = app.getLocalization();
		return new String[] { loc.getMenu("On"),
				loc.getMenu("Automatic"), loc.getMenu("Off") };
	}

	public void updateBasicProperties(IBasicTab basicTab) {
		basicTab.updateAxes(view.getAxesColor(),
				view.getShowXaxis() && view.getShowYaxis(), view.areAxesBold());
		basicTab.updateBackgroundColor(getBackgroundColor());
		int ind = view.getAllowToolTips();
		int idx = -1;

		if (ind == EuclidianStyleConstants.TOOLTIPS_ON) {
			idx = 0;
		} else if (ind == EuclidianStyleConstants.TOOLTIPS_AUTOMATIC) {
			idx = 1;
		} else if (ind == EuclidianStyleConstants.TOOLTIPS_OFF) {
			idx = 2;
		}

		basicTab.selectTooltipType(idx);

		basicTab.showMouseCoords(view.getAllowShowMouseCoords());

		basicTab.enableAxesRatio(
				view.isZoomable() && !view.isLockedAxesRatio());
		basicTab.enableLock(view.isZoomable());
		basicTab.updateBounds();

		// need style with bold removed for menu
		for (int i = 0; i < EuclidianStyleConstants
				.getLineStyleOptionsLength(); i++) {
			if (EuclidianView.getBoldAxes(false,
					view.getAxesLineStyle()) == EuclidianStyleConstants
							.getLineStyleOptions(i)) {
				basicTab.selectAxesStyle(i);
				break;
			}
		}
		// cons protocol panel
		basicTab.updateConsProtocolPanel(
				app.showConsProtNavigation(view.getViewID()));
		int fontStyle = view.getSettings().getAxisFontStyle();
		boolean serif = view.getSettings().getAxesLabelsSerif();
		basicTab.updateAxisFontStyle(serif,
				(fontStyle & GFont.BOLD) == GFont.BOLD,
				(fontStyle & GFont.ITALIC) == GFont.ITALIC);
	}

	public void updateGridProperties(IGridTab gridTab) {
		gridTab.updateGrid(view.getGridColor(), view.getShowGrid(),
				view.getGridIsBold(), view.getGridType());
		EuclidianSettings es = view.getSettings();
		gridTab.updateRuler(es.getBackgroundType(), es.getBgRulerColor(),
				es.getRulerLineStyle(), es.isRulerBold());
		gridTab.selectGridStyle(view.getGridLineStyle());

		gridTab.updateGridTicks(view.isAutomaticGridDistance(),
				view.getGridDistances(), view.getGridType());
	}

	public List<String> getAngleOptions() {
		return Arrays.asList(Unicode.PI_STRING + "/12",
				Unicode.PI_STRING + "/6", Unicode.PI_STRING + "/4",
				Unicode.PI_STRING + "/3", Unicode.PI_STRING + "/2");
	}

	public void applyGridTicks(String str, int idx) {
		double value = Double.NaN;
		final String text = str.trim();
		if (!StringUtil.empty(text)) {
			value = app.getKernel().getAlgebraProcessor()
					.evaluateToDouble(text);
		}
		if (value > 0) {
			double[] ticks = view.getGridDistances();
			ticks[idx] = value;
			view.setGridDistances(ticks);
		}
	}

	public void applyGridTickAngle(String text) {
		if (!"".equals(text)) {

			double[] ticks = view.getGridDistances();
			// val = 4 gives 5*PI/12, skip this and go to 6*Pi/2 = Pi/2

			double value = app.getKernel().getAlgebraProcessor()
					.evaluateToDouble(text);
			ticks[2] = getGridTickAngle(value);
			view.setGridDistances(ticks);
		}
	}

	/**
	 *
	 * @param value .
	 * @return the angle
	 */
	public static double getGridTickAngle(double value) {
		return Math.PI
				/ Math.min(360, Math.round(Math.abs(Math.PI / value)));
	}

	/**
	 * Change input value.
	 * @param value - input value
	 */
	public String gridTickToString(Double value) {
		if (DoubleUtil.isEqual(value, Math.PI)) {
			return Unicode.PI_STRING;
		}  else if (DoubleUtil.isEqual(value, Kernel.PI_HALF)) {
			return Unicode.PI_HALF_STRING;
		} else {
			return view.getApplication().getKernel().format(value, StringTemplate.defaultTemplate);
		}
	}

	public String gridAngleToString() {
		double val = view.getGridDistances(2) / Math.PI;
		double[] frac = AlgoFractionText.decimalToFraction(val,
				Kernel.MAX_PRECISION);
		StringBuilder sb = new StringBuilder();
		if (frac[1] < 361) {
			if (!DoubleUtil.isEqual(1, frac[0])) {
				sb.append(Math.round(frac[0]));
			}
			sb.append(Unicode.pi);
			if (!DoubleUtil.isEqual(1, frac[1])) {
				sb.append("/");
				sb.append(Math.round(frac[1]));
			}
		} else {
			sb.append(app.getKernel().format(view.getGridDistances(2),
					StringTemplate.editTemplate));
		}
		return sb.toString();
	}

	public GColor getAxesColor() {
		return view.getAxesColor();
	}

	public GColor getGridColor() {
		return view.getGridColor();
	}

	public GColor getRulerColor() {
		return view.getSettings().getBgRulerColor();
	}

	public GColor getBackgroundColor() {
		return view.getBackgroundCommon();
	}

	public int getAxisFontStyle() {
		return view.getSettings().getAxisFontStyle();
	}

	public void setAxisFontStyle(int fontStyle) {
		view.getSettings().setAxisFontStyle(fontStyle);
		view.updateFonts();
	}

	public void setAxesLabelsSerif(boolean serif) {
		view.getSettings().setAxesLabelsSerif(serif);
		view.updateFonts();
	}

	public void setAxisFontBold(boolean value) {
		int style = getAxisFontStyle();
		if (value) {
			if (style == GFont.PLAIN || style == GFont.ITALIC) {
				style += GFont.BOLD;
				setAxisFontStyle(style);
			}
		} else {
			if (style == GFont.BOLD || style == GFont.ITALIC + GFont.BOLD) {
				style -= GFont.BOLD;
				setAxisFontStyle(style);
			}
		}
	}

	public void setAxisFontItalic(boolean value) {
		int style = getAxisFontStyle();
		if (value) {
			if (style == GFont.PLAIN || style == GFont.BOLD) {
				style += GFont.ITALIC;
				setAxisFontStyle(style);
			}
		} else {
			if (style == GFont.ITALIC || style == GFont.ITALIC + GFont.BOLD) {
				style -= GFont.ITALIC;
				setAxisFontStyle(style);
			}
		}
	}

	public enum MinMaxType {
		minX, maxX, minY, maxY, minZ, maxZ
	}

	public interface IBasicTab {
		@MissingDoc
		void updateAxes(GColor color, boolean isShown, boolean isBold);

		@MissingDoc
		void updateAxisFontStyle(boolean isSerif, boolean isBold,
				boolean isItalic);

		@MissingDoc
		void updateBackgroundColor(GColor color);

		@MissingDoc
		void selectTooltipType(int index);

		@MissingDoc
		void selectAxesStyle(int index);

		/**
		 * Update construction protocol navigation settings.
		 *
		 * @param isVisible
		 *            whether the checkboxes should be enabled
		 */
		void updateConsProtocolPanel(boolean isVisible);

		@MissingDoc
		void showMouseCoords(boolean value);

		@MissingDoc
		void enableLock(boolean zoomable);

		/**
		 * @param minX
		 *            min x
		 * @param maxX
		 *            max x
		 * @param minY
		 *            min y
		 * @param maxY
		 *            max y
		 * @param minZ
		 *            min z
		 * @param maxZ
		 *            max z
		 */
		void setMinMaxText(String minX, String maxX, String minY, String maxY, String minZ,
				String maxZ);
		/**
		 * Update xmin, xmax, ymin, ymax and scale inputs
		 */
		void updateBounds();

		/**
		 * @param value
		 *            axes ratio
		 */
		void enableAxesRatio(boolean value);
	}

	public interface IGridTab {
		/**
		 * Update ruler properties.
		 *
		 * @param bgTypeIdx
		 *            The background type.
		 * @param color
		 *            to set.
		 * @param lineStyle
		 *            The line style.
		 * @param bold
		 *            true if the lines should be bold.
		 */
		void updateRuler(BackgroundType bgTypeIdx, GColor color, int lineStyle, boolean bold);

		@MissingDoc
		void updateGrid(GColor color, boolean isShown, boolean isBold,
				int gridType);
		/**
		 * @param style
		 *            of grid lines
		 */
		void selectGridStyle(int style);

		/**
		 * @param isAutoGrid
		 *            true if auto
		 * @param gridTicks
		 *            grid ticks
		 * @param gridType
		 *            type of grid
		 */
		void updateGridTicks(boolean isAutoGrid, double[] gridTicks,
				int gridType);
	}

	/**
	 * Set ruler type from dropdown.
	 * 
	 * @param type
	 *            the selected index
	 */
	public void applyRulerType(BackgroundType type) {
		EuclidianSettings settings = view.getSettings();
		settings.setBackgroundType(type);
	}
}
