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
 */

package com.himamis.retex.editor.share.editor;

import com.himamis.retex.editor.share.event.ClickListener;
import com.himamis.retex.editor.share.event.FocusListener;
import com.himamis.retex.editor.share.event.KeyListener;
import com.himamis.retex.editor.share.meta.MetaModel;
import com.himamis.retex.renderer.share.TeXIcon;

/**
 * Formula input field.
 */
public interface MathField {

	/**
	 * Update the image of the currently edited formula.
	 * @param icon icon
	 */
	void setTeXIcon(TeXIcon icon);

	/**
	 * show keyboard
	 *
	 * @return true if keyboard was hidden previously
	 */
	boolean showKeyboard();

	/**
	 * Show copy and paste buttons (mobile only).
	 */
	void showCopyPasteButtons();

	/**
	 * Focus this input field.
	 */
	void requestViewFocus();

	/**
	 * Set a focus listener.
	 * @param focusListener focus listener
	 */
	void setFocusListener(FocusListener focusListener);

	/**
	 * Set a pointer event listener.
	 * @param clickListener pointer event listener
	 */
	void setClickListener(ClickListener clickListener);

	/**
	 * Set a keyboard event listener.
	 * @param keyListener keyboard event listener
	 */
	void setKeyListener(KeyListener keyListener);

	/**
	 * Mark the component for repaint.
	 */
	void repaint();

	/**
	 * Invalidate layout (mobile, desktop).
	 */
	void requestLayout();

	/**
	 * @return whether this has a parent component
	 */
	boolean hasParent();

	/**
	 * @return whether this input field has focus
	 */
	boolean hasFocus();

	/**
	 * @return model describing all available math components (functions, arrays)
	 */
	MetaModel getMetaModel();

	/**
	 * Hide copy and paste buttons.
	 */
	void hideCopyPasteButtons();

	/**
	 * scroll the view
	 *
	 * @param dx
	 *            x distance from current call to last call
	 * @param dy
	 *            y distance from current call to last call
	 */
	void scroll(int dx, int dy);

	/**
	 * Fire input change event.
	 */
	void fireInputChangedEvent();

	/**
	 * Paste from system keyboard.
	 */
	void paste();

	/**
	 * Copy to system keyboard.
	 */
	void copy();

	/**
	 * TODO remove this
	 * @return whether an old Firefox hack is needed (always false)
	 */
	boolean useCustomPaste();

	/**
	 * Parse input as math formula in editor format.
	 * @param str formula in editor format
	 */
	default void parse(String str) {
		getInternal().parse(str);
	}

	/**
	 * @return the cross-platform representation of this field
	 */
	MathFieldInternal getInternal();

	/**
	 * Remove focus and call blur handler.
	 */
	default void blur() {
		// implemented in web
	}
}
