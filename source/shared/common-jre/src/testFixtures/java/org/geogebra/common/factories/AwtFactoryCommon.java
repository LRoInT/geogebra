package org.geogebra.common.factories;

import org.geogebra.common.awt.GAlphaComposite;
import org.geogebra.common.awt.GBufferedImage;
import org.geogebra.common.awt.GBufferedImageCommon;
import org.geogebra.common.awt.GColor;
import org.geogebra.common.awt.GFont;
import org.geogebra.common.awt.GFontRenderContext;
import org.geogebra.common.awt.GGradientPaint;
import org.geogebra.common.awt.GGraphics2D;
import org.geogebra.common.awt.GGraphicsCommon;
import org.geogebra.common.awt.GPaint;
import org.geogebra.common.awt.GRectangle;
import org.geogebra.common.awt.MyImage;
import org.geogebra.common.awt.TextLayoutCommon;
import org.geogebra.common.awt.font.GTextLayout;
import org.geogebra.common.gui.font.GFontCommon;
import org.geogebra.ggbjdk.factories.AwtFactoryHeadless;

/**
 * Class used for testing.
 */
public class AwtFactoryCommon extends AwtFactoryHeadless {

    @Override
    public GBufferedImage newBufferedImage(int pixelWidth, int pixelHeight, double pixelRatio) {
		return new GBufferedImageCommon(pixelWidth, pixelHeight);
    }

    @Override
    public GBufferedImage createBufferedImage(int width, int height, boolean transparency) {
        return new GBufferedImageCommon(width, height);
    }

    @Override
    public GTextLayout newTextLayout(String string, GFont font, GFontRenderContext frc) {
		return new TextLayoutCommon(font, string);
    }

    @Override
    public GAlphaComposite newAlphaComposite(double alpha) {
        return null;
    }

	@Override
	public GGradientPaint newGradientPaint(double x, double y, GColor bg2,
			double x2, double i, GColor bg) {
		return null;
	}

    @Override
    public GPaint newTexturePaint(GBufferedImage subimage, GRectangle rect) {
        return new GTexturePaintCommon(subimage, rect);
    }

    @Override
    public GPaint newTexturePaint(MyImage subimage, GRectangle rect) {
        return null;
    }

    @Override
    public GFont newFont(String name, int style, int size) {
        return new GFontCommon(size);
    }
    
    public static class GTexturePaintCommon implements GPaint {

        public final GBufferedImage subImage;
        public final GRectangle rect;

        /**
         * @param subimage image
         * @param rect rectangle
         */
        public GTexturePaintCommon(GBufferedImage subimage, GRectangle rect) {
            this.subImage = subimage;
            this.rect = rect;
        }
    }

    @Override
    public GGraphics2D getSVGGraphics(int width, int height) {
        return new GGraphicsCommon();
    }

    @Override
    public GGraphics2D getPDFGraphics(int width, int height) {
        return new GGraphicsCommon();
    }
}
