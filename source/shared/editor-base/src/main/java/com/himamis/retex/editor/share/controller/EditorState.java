package com.himamis.retex.editor.share.controller;

import java.util.function.Predicate;

import com.himamis.retex.editor.share.editor.EditorFeatures;
import com.himamis.retex.editor.share.editor.MathFieldInternal;
import com.himamis.retex.editor.share.meta.MetaCharacter;
import com.himamis.retex.editor.share.meta.MetaModel;
import com.himamis.retex.editor.share.meta.Tag;
import com.himamis.retex.editor.share.model.MathArray;
import com.himamis.retex.editor.share.model.MathCharacter;
import com.himamis.retex.editor.share.model.MathComponent;
import com.himamis.retex.editor.share.model.MathContainer;
import com.himamis.retex.editor.share.model.MathFunction;
import com.himamis.retex.editor.share.model.MathSequence;
import com.himamis.retex.editor.share.serializer.GeoGebraSerializer;
import com.himamis.retex.editor.share.serializer.ScreenReaderSerializer;
import com.himamis.retex.renderer.share.platform.FactoryProvider;

public class EditorState {

	private final MetaModel metaModel;
	private final SelectAllHandler selectAll;
	private MathSequence rootComponent;

	/**
	 * The Container in which the cursor is currently placed
	 */
	private MathSequence currentField;
	/**
	 * The index of the cursor in the current Container
	 */
	private int currentOffset;

	private MathComponent currentSelStart;
	private MathComponent currentSelEnd;
	private MathComponent selectionAnchor;

	/**
	 *
	 * @param metaModel {@link MetaModel}
	 */
	public EditorState(MetaModel metaModel) {
		this.metaModel = metaModel;
		selectAll = new SelectAllHandler(this);
	}

	public MathSequence getRootComponent() {
		return rootComponent;
	}

	public void setRootComponent(MathSequence rootComponent) {
		this.rootComponent = rootComponent;
	}

	public MathSequence getCurrentField() {
		return currentField;
	}

	public void setCurrentField(MathSequence currentField) {
		this.currentField = currentField;
	}

	public int getCurrentOffset() {
		return currentOffset;
	}

	public void setCurrentOffset(int currentOffset) {
		this.currentOffset = Math.max(currentOffset, 0);
	}

	/**
	 * Increase current offset.
	 */
	public void incCurrentOffset() {
		currentOffset++;
	}

	/**
	 * Increase current offset by an amount.
	 * @param size number to add to current offset
	 */
	public void addCurrentOffset(int size) {
		currentOffset += size;
	}

	/**
	 * Decrease current offset.
	 */
	public void decCurrentOffset() {
		if (currentOffset > 0) {
			currentOffset--;
		}
	}

	/**
	 * @param mathComponent
	 *            new argument
	 */
	public void addArgument(MathComponent mathComponent) {
		if (currentField.addArgument(currentOffset, mathComponent)) {
			incCurrentOffset();
		}
	}

	/**
	 * Add character, consider unicode surrogates
	 * @param mathComponent
	 *            new argument
	 */
	public void addArgument(MetaCharacter mathComponent) {
		currentOffset = currentOffset + currentField.addArgument(currentOffset, mathComponent);
	}

	public MetaModel getMetaModel() {
		return metaModel;
	}

	public MathComponent getSelectionStart() {
		return currentSelStart;
	}

	/**
	 * @return current offset or selection start
	 */
	public int getCurrentOffsetOrSelection() {
		if (currentSelStart != null && currentSelStart.getParent() == currentField) {
			return currentSelStart.getParentIndex();
		}
		return currentOffset;
	}

	public MathComponent getSelectionEnd() {
		return currentSelEnd;
	}

	public void setSelectionStart(MathComponent selStart) {
		currentSelStart = selStart;
	}

	public void setSelectionEnd(MathComponent selEnd) {
		currentSelEnd = selEnd;
	}

	/**
	 * Extends selection from current cursor position
	 * 
	 * @param left
	 *            true to go to the left from cursor
	 */
	public void extendSelection(boolean left) {
		MathComponent cursorField = getCursorField(left);
		if (cursorField == null) {
			return;
		}

		extendSelection(cursorField);
		if (left && currentField.size() == currentOffset) {
			currentOffset--;
		}
	}

	/**
	 * Extends selection to include a field
	 * 
	 * @param cursorField
	 *            newly selected field
	 */
	public void extendSelection(MathComponent cursorField) {
		if (cursorField == null) {
			return;
		}

		if (selectionAnchor == null) {
			if (isGrandparentProtected(cursorField.getParent())
					&& ",".equals(cursorField.toString())) {
				return;
			}
			currentSelStart = cursorField;
			currentSelEnd = cursorField;
			anchor(true);
			return;
		}

		currentSelStart = selectionAnchor;
		// go from selection start to the root until we find common root
		MathContainer commonParent = currentSelStart.getParent();
		while (commonParent != null && !contains(commonParent, cursorField)) {
			currentSelStart = currentSelStart.getParent();
			commonParent = currentSelStart.getParent();
			if (commonParent.isRenderingOwnPlaceholders() && !isMatrix(commonParent)) {
				currentSelStart = commonParent;
				commonParent = currentSelStart.getParent();
			}
		}
		if (commonParent == null) {
			commonParent = rootComponent;
		}

		currentSelEnd = cursorField;
		// special case: start is inside end -> select single component
		if (currentSelEnd == commonParent
				|| commonParent instanceof MathFunction
					&& ((MathFunction) commonParent).getName() == Tag.FRAC) {
			currentSelStart = commonParent;
			currentSelEnd = commonParent;
			return;
		}

		// go from selection end to the root
		while (currentSelEnd != null
				&& commonParent.indexOf(currentSelEnd) < 0) {
			currentSelEnd = currentSelEnd.getParent();
		}

		// swap start and end when necessary
		int to = commonParent.indexOf(currentSelEnd);
		int from = commonParent.indexOf(currentSelStart);
		if (from > to) {
			int swapIdx = from;
			from = to;
			to = swapIdx;
			MathComponent swap = currentSelStart;
			currentSelStart = currentSelEnd;
			currentSelEnd = swap;
		}
		if (isGrandparentProtected(commonParent)) {
			terminateSelectionAtComma(commonParent, from, to);
		}

	}

	private boolean isMatrix(MathContainer container) {
		return container instanceof MathArray && ((MathArray) container).hasTag(Tag.MATRIX);
	}

	private void terminateSelectionAtComma(MathContainer commonParent, int from, int to) {
		for (int j = from; j <= to; j++) {
			if (commonParent.isFieldSeparator(j)) {
				if (j == from) {
					currentSelEnd = currentSelStart = null;
				} else {
					currentSelEnd = commonParent.getArgument(j - 1);
				}
				if (commonParent == currentField) {
					currentOffset = j;
				}
			}
		}
	}

	private boolean isGrandparentProtected(MathContainer commonParent) {
		return commonParent != null
				&& commonParent.getParent() != null
				&& commonParent.getParent().getParent() != null
				&& commonParent.getParent().getParent().isProtected();
	}

	/**
	 * Select the whole formula
	 */
	public void selectAll() {
		selectAll.execute();
	}

	/**
	 * Select from cursor position to end of current sub-formula
	 */
	public void selectToStart() {
		extendSelection(getCursorField(false));
		extendSelection(getCurrentField().getArgument(0));
	}

	/**
	 * Select from cursor position to start of current sub-formula
	 */
	public void selectToEnd() {
		extendSelection(getCursorField(true));
		extendSelection(getCurrentField().getArgument(getCurrentField().size() - 1));
	}

	/**
	 * @param left whether to search left
	 * @return field directly left or right to the caret
	 */
	public MathComponent getCursorField(boolean left) {
		return getCurrentField().getArgument(
				Math.max(0, Math.min(getCurrentOffset() + (left ? 0 : -1),
						getCurrentField().size() - 1)));
	}

	private static boolean contains(MathContainer commonParent,
			MathComponent cursorField0) {
		MathComponent cursorField = cursorField0;
		while (cursorField != null) {
			if (cursorField == commonParent) {
				return true;
			}
			cursorField = cursorField.getParent();
		}
		return false;
	}

	/**
	 * Reset selection start/end/anchor pointers (NOT the caret)
	 */
	public void resetSelection() {

		selectionAnchor = null;

		currentSelEnd = null;
		currentSelStart = null;

	}

	/**
	 * @return true part of expression is selected
	 */
	public boolean hasSelection() {
		return currentSelStart != null;
	}

	/**
	 * Update selection anchor (starting point of selection by drag)
	 * 
	 * @param start
	 *            whether to anchor the start or the end of selection
	 */
	public void anchor(boolean start) {
		this.selectionAnchor = start ? this.currentSelStart
				: this.currentSelEnd;
	}

	/**
	 * Move cursor to the start of the selection.
	 */
	public void cursorToSelectionStart() {
		if (this.currentSelStart != null) {
			currentField = getClosestSequenceAncestor(currentSelStart);
			currentOffset = currentSelEnd == currentField ? 0 : currentSelStart.getParentIndex();
		}
	}

	/**
	 * Move cursor to the end of the selection.
	 */
	public void cursorToSelectionEnd() {
		if (currentSelEnd != null) {
			currentField = getClosestSequenceAncestor(currentSelEnd);
			currentOffset = currentSelEnd == currentField
					? rootComponent.size() : currentSelEnd.getParentIndex() + 1;
		}
	}

	public MathComponent getSelectionAnchor() {
		return selectionAnchor;
	}

	/**
	 * @return whether cursor is between quotes
	 */
	public boolean isInsideQuotes() {
		MathContainer fieldParent = currentField;
		while (fieldParent != null) {
			if (fieldParent instanceof MathArray
					&& ((MathArray) fieldParent).getOpenKey() == '"') {
				return true;
			}
			fieldParent = fieldParent.getParent();
		}
		return false;
	}

	/**
	 * @param er expression reader
	 * @param editorFeatures editor features
	 * @return description of cursor position
	 */
	public String getDescription(ExpressionReader er, EditorFeatures editorFeatures) {
		MathComponent prev = currentField.getArgument(currentOffset - 1);
		MathComponent next = currentField.getArgument(currentOffset);
		StringBuilder sb = new StringBuilder();
		if (currentField.getParent() == null) {
			if (prev == null) {
				return er
						.localize("start of formula %0",
								ScreenReaderSerializer.fullDescription(
										currentField, er.getAdapter()))
						.trim();
			}
			if (next == null) {
				return er
						.localize("end of formula %0",
								ScreenReaderSerializer.fullDescription(
										currentField, er.getAdapter()))
						.trim();
			}
		}
		if (next == null && prev == null) {
			return describeParent(ExpRelation.EMPTY, currentField.getParent(),
					er);
		}
		if (next == null) {
			sb.append(
					describeParent(ExpRelation.END_OF, currentField.getParent(),
							er));
			sb.append(" ");
		}
		if (prev != null) {
			sb.append(describePrev(prev, er, editorFeatures));
		} else {
			sb.append(describeParent(ExpRelation.START_OF,
					currentField.getParent(),
					er));
		}
		sb.append(" ");

		if (next != null) {
			sb.append(describeNext(next, er));
		} else if (endOfFunctionName()) {
			sb.append(
					er.localize(ExpRelation.BEFORE.toString(),
							er.getAdapter().parenthesis("(")));
		}
		return sb.toString().trim();
	}

	/**
	 * @return number of comma symbols before cursor
	 */
	public int countCommasBeforeCurrent() {
		int commas = 0;
		for (int i = 0; i < currentOffset; i++) {
			if (currentField.isFieldSeparator(i)) {
				commas++;
			}
		}
		return commas;
	}

	/**
	 * @return number of comma symbols after cursor
	 */
	public int countCommasAfterCurrent() {
		int commas = 0;
		for (int i = currentOffset; i < currentField.size(); i++) {
			if (currentField.isFieldSeparator(i)) {
				commas++;
			}
		}
		return commas;
	}

	private boolean endOfFunctionName() {
		return currentField.getParent() instanceof MathFunction
				&& currentField.getParent().hasTag(Tag.APPLY)
				&& currentField.getParentIndex() == 0;
	}

	private String describePrev(MathComponent parent, ExpressionReader er,
			EditorFeatures editorFeatures) {
		if (parent instanceof MathFunction
				&& Tag.SUPERSCRIPT == ((MathFunction) parent).getName()) {
			return er.localize(ExpRelation.AFTER.toString(), er.power(
					GeoGebraSerializer.serialize(currentField
							.getArgument(currentField.indexOf(parent) - 1), editorFeatures),
					GeoGebraSerializer
							.serialize(
									((MathFunction) parent).getArgument(0), editorFeatures)));
		}
		if (parent instanceof MathCharacter) {
			StringBuilder sb = new StringBuilder();
			int i = currentField.indexOf(parent);
			while (MathFieldInternal.appendChar(sb, currentField, i, MathCharacter::isCharacter)) {
				i--;
			}
			if (sb.length() > 0 && !isInsideQuotes()) {
				try {
					return er.localize(ExpRelation.AFTER.toString(),
							mathExpression(sb.reverse().toString(), er));
				} catch (Exception e) {
					FactoryProvider.getInstance()
							.debug("Invalid: " + sb.reverse());
				}
			}
		}
		return describe(ExpRelation.AFTER, parent, er);
	}

	private String describeNext(MathComponent parent, ExpressionReader er) {
		if (parent instanceof MathCharacter) {
			StringBuilder sb = new StringBuilder();
			int i = currentField.indexOf(parent);
			while (MathFieldInternal.appendChar(sb, currentField, i, MathCharacter::isCharacter)) {
				i++;
			}
			if (sb.length() > 0 && !isInsideQuotes()) {
				try {
					return er.localize(ExpRelation.BEFORE.toString(),
							mathExpression(sb.toString(), er));
				} catch (Exception e) {
					// no math alt text, fall back to reading as is
				}
			}
		}
		return describe(ExpRelation.BEFORE, parent, er);
	}

	private String mathExpression(String math, ExpressionReader er) {
		StringBuilder sb = new StringBuilder(math.length());
		for (int i = 0; i < math.length(); i++) {
			sb.append(er.getAdapter().convertCharacter(math.charAt(i)));
		}
		return sb.toString();
	}

	private static String describe(ExpRelation pattern, MathComponent prev,
			ExpressionReader er) {
		String name = describe(pattern, prev, -1, er);
		if (name != null) {
			return er.localize(pattern.toString(), name);
		}
		return er.localize(pattern.toString(),
				ScreenReaderSerializer.fullDescription(prev, er.getAdapter()));
	}

	private static String describe(ExpRelation pattern, MathComponent prev,
			int index, ExpressionReader er) {
		if (prev instanceof MathFunction) {
			switch (((MathFunction) prev).getName()) {
			case FRAC:
				return new String[] { "fraction", "numerator",
						"denominator" }[index + 1];
			case NROOT:
				return new String[] { "root", "index", "radicand" }[index + 1];
			case SQRT:
				return "square root";
			case CBRT:
				return "cube root";
			case SUPERSCRIPT:
				return "superscript";
			case ABS:
				return "absolute value";
			case APPLY:
				if ((index == 1 && pattern == ExpRelation.START_OF)
						|| (index == ((MathFunction) prev).size() - 1
								&& pattern == ExpRelation.END_OF)) {
					return "parentheses";
				}
				return index >= 0 ? "" : "function";
			default:
				return "function";
			}
		}
		if (prev instanceof MathArray) {
			if (((MathArray) prev).getOpenKey() == '"') {
				return "quotes";
			}
			switch (pattern) {
			case AFTER:
				return er.getAdapter().parenthesis(")");
			case BEFORE:
				return er.getAdapter().parenthesis("(");
			default: return "parentheses";
			}
		}
		return null;
	}

	private String describeParent(ExpRelation pattern, MathContainer parent,
			ExpressionReader er) {
		if (parent instanceof MathFunction) {
			String name = describe(pattern, parent,
					parent.indexOf(currentField), er);
			if (name != null && name.isEmpty()) {
				return "";
			}
			return er.localize(pattern.toString(), name);
		}

		return describe(pattern, parent, er);
	}

	/**
	 *
	 * @return whether current field is inside a fraction or not.
	 */
	public boolean isInFraction() {
		MathContainer parent = currentField.getParent();
		return parent != null && parent.hasTag(Tag.FRAC);
	}

	/**
	 * @return Whether fractions inside this function are disallowed
	 */
	public boolean isPreventingNestedFractions() {
		MathContainer parent = currentField.getParent();
		return parent instanceof MathFunction
				&& ((MathFunction) parent).isPreventingNestedFractions();
	}

	/**
	 * @return Whether the current field is inside a recurring decimal or not
	 */
	public boolean isInRecurringDecimal() {
		MathContainer parent = currentField.getParent();
		return parent != null && parent.hasTag(Tag.RECURRING_DECIMAL);
	}

	/**
	 *
	 * @return whether current field is inside a sub/superscript or not.
	 */
	public boolean isInScript() {
		return hasParent(parent -> parent.hasTag(Tag.SUBSCRIPT)
				|| parent.hasTag(Tag.SUPERSCRIPT));
	}

	/**
	 * @return whether current field is inside an input
	 */
	public boolean isInHighlightedPlaceholder() {
		return hasParent(MathContainer::isRenderingOwnPlaceholders);
	}

	private boolean hasParent(Predicate<MathContainer> check) {
		MathContainer parent = currentField.getParent();
		while (parent != null) {
			if (check.test(parent)) {
				return true;
			}
			parent = parent.getParent();
		}
		return false;
	}

	/**
	 * Select the topmost ancestor that's not root or root's child.
	 */
	public void selectUpToRootComponent() {
		while (currentSelStart != null && currentSelStart.getParent() != null
				&& currentSelStart.getParent().getParent() != getRootComponent()) {
			anchor(true);
			currentSelStart = currentSelStart.getParent();
		}

		setSelectionEnd(currentSelStart);
	}

	/**
	 * @param left whether to collapse to the left
	 * @return whether selection was collapsed
	 */
	public boolean updateCursorFromSelection(boolean left) {
		if (left && currentSelStart != null) {
			cursorToSelectionStart();
			return true;
		} else if (currentSelEnd != null) {
			cursorToSelectionEnd();
			return true;
		}
		return false;
	}

	private MathSequence getClosestSequenceAncestor(MathComponent comp) {
		MathComponent current = comp;
		while (current.getParent() != null && !(current instanceof MathSequence)) {
			current = current.getParent();
		}
		return current instanceof MathSequence ? (MathSequence) current : rootComponent;
	}

	public MathComponent getComponentLeftOfCursor() {
		return currentOffset > 0 ? currentField.getArgument(currentOffset - 1) : null;
	}
}
