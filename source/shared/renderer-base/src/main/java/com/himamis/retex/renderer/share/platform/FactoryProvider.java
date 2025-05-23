/*
 * This file is part of the ReTeX library - https://github.com/himamis/ReTeX
 *
 * Copyright (C) 2015 Balazs Bencze
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * A copy of the GNU General Public License can be found in the file
 * LICENSE.txt provided with the source distribution of this program (see
 * the META-INF directory in the source jar). This license can also be
 * found on the GNU website at http://www.gnu.org/licenses/gpl.html.
 *
 * If you did not receive a copy of the GNU General Public License along
 * with this program, contact the lead developer, or write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 *
 * Linking this library statically or dynamically with other modules 
 * is making a combined work based on this library. Thus, the terms 
 * and conditions of the GNU General Public License cover the whole 
 * combination.
 * 
 * As a special exception, the copyright holders of this library give you 
 * permission to link this library with independent modules to produce 
 * an executable, regardless of the license terms of these independent 
 * modules, and to copy and distribute the resulting executable under terms 
 * of your choice, provided that you also meet, for each linked independent 
 * module, the terms and conditions of the license of that module. 
 * An independent module is a module which is not derived from or based 
 * on this library. If you modify this library, you may extend this exception 
 * to your version of the library, but you are not obliged to do so. 
 * If you do not wish to do so, delete this exception statement from your 
 * version.
 * 
 */
package com.himamis.retex.renderer.share.platform;

import com.himamis.retex.renderer.share.Configuration;
import com.himamis.retex.renderer.share.platform.box.BoxDecorator;
import com.himamis.retex.renderer.share.platform.box.DefaultBoxDecorator;
import com.himamis.retex.renderer.share.platform.font.FontFactory;
import com.himamis.retex.renderer.share.platform.geom.GeomFactory;
import com.himamis.retex.renderer.share.platform.graphics.GraphicsFactory;
import com.himamis.retex.renderer.share.platform.resources.ResourceLoaderFactory;

public abstract class FactoryProvider {

	private static FactoryProvider INSTANCE;

	private GeomFactory geomFactory;
	private FontFactory fontFactory;
	private GraphicsFactory graphicsFactory;
	private BoxDecorator boxDecorator;

	protected abstract GeomFactory createGeomFactory();

	protected abstract FontFactory createFontFactory();

	protected abstract GraphicsFactory createGraphicsFactory();

	protected BoxDecorator createBoxDecorator() {
		return new DefaultBoxDecorator();
	}

	protected FactoryProvider() {
	}

	public GeomFactory getGeomFactory() {
		if (geomFactory == null) {
			geomFactory = createGeomFactory();
		}
		return geomFactory;
	}

	public FontFactory getFontFactory() {
		if (fontFactory == null) {
			fontFactory = createFontFactory();
		}
		return fontFactory;
	}

	public GraphicsFactory getGraphicsFactory() {
		if (graphicsFactory == null) {
			graphicsFactory = createGraphicsFactory();
		}
		return graphicsFactory;
	}

	public BoxDecorator getBoxDecorator() {
		if (boxDecorator == null) {
			boxDecorator = createBoxDecorator();
		}
		return boxDecorator;
	}

	/**
	 * Overridden in eg FactoryProviderGWT
	 * 
	 * @param msg debug message
	 */
	public void debug(Object msg) {
		if (msg instanceof Throwable) {
			System.out.println("[LaTeX] exception caught");
			((Throwable) msg).printStackTrace(System.out);
		} else {
			System.out.println("[LaTeX] " + msg);
		}
	}

	public static void setInstance(FactoryProvider factory) {
		INSTANCE = factory;
		Configuration.getFontMapping();
	}

	public static FactoryProvider getInstance() {
		return INSTANCE;
	}

	// TODO remove as part of Android / iOS cleanup
	public ResourceLoaderFactory getResourceLoaderFactory() {
		return null;
	}

	// TODO remove as part of Android / iOS cleanup
	protected ResourceLoaderFactory createResourceLoaderFactory() {
		return null;
	}

	public static void debugS(Object message) {
		if (INSTANCE != null) {
			INSTANCE.debug(message);
		} else {
			System.out.println(message);
		}
		
	}

}
