package org.geogebra.common.euclidian;

import java.util.ArrayList;
import java.util.List;

import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.geos.GeoImage;
import org.geogebra.common.main.SelectionManager;
import org.geogebra.common.main.undo.UndoManager;
import org.geogebra.common.plugin.ActionType;

import com.google.j2objc.annotations.Weak;

public class UpdateActionStore {
	private final List<UndoItem> undoItems = new ArrayList<>();

	@Weak
	protected final SelectionManager selection;
	private final UndoManager undoManager;

	/**
	 * Constructor
	 * @param selection {@link SelectionManager}
	 * @param undoManager {@link UndoManager}
	 */
	public UpdateActionStore(SelectionManager selection, UndoManager undoManager) {
		this.selection = selection;
		this.undoManager = undoManager;
	}

	/**
	 * Stores list of geos to items
	 * @param geos to store.
	 * @param moveMode active move mode
	 */
	void store(List<GeoElement> geos, MoveMode moveMode) {
		clear();
		for (GeoElement geo: geos) {
			undoItems.add(new UndoItem(geo, moveMode));
		}
	}

	/**
	 * Store selected geo to items.
	 * @param moveMode active move mode
	 */
	public void storeSelection(MoveMode moveMode) {
		if (undoItems.isEmpty()) {
			store(getGeosToStore(), moveMode);
		}
	}

	private ArrayList<GeoElement> getGeosToStore() {
		ArrayList<GeoElement> geosToStore = new ArrayList<>();
		for (GeoElement geo : selection.getSelectedGeos()) {
			if (geo.hasChangeableParent3D()) {
				geosToStore.add(geo.getChangeableParent3D().getSurface());
				continue;
			}
			if (geo.getParentAlgorithm() != null
					&& !geo.isPointOnPath() && !geo.isPointInRegion()) {
				geosToStore.addAll(geo.getParentAlgorithm().getDefinedAndLabeledInput());
			} else if (geo instanceof GeoImage) {
				geosToStore.addAll(((GeoImage) geo).getDefinedAndLabeledStartPoints());
			}
			geosToStore.add(geo);
		}
		return geosToStore;
	}

	/**
	 * Add single element if not already present
	 * @param geo element to add
	 * @param mode move mode
	 */
	public void addIfNotPresent(GeoElement geo, MoveMode mode) {
		if (undoItems.stream().noneMatch(it -> it.hasGeo(geo))) {
			undoItems.add(new UndoItem(geo, mode));
		}
	}

	/**
	 * Remove all items related to given element.
	 * @param geo element
	 */
	public void remove(GeoElement geo) {
		undoItems.removeIf(it -> it.hasGeo(geo));
	}

	/**
	 * Clear all items.
	 */
	public void clear() {
		undoItems.clear();
	}

	/**
	 * Builds actions from items and stores it in UndoManager
	 */
	public void storeUpdateAction() {
		List<String> actions = new ArrayList<>(undoItems.size());
		List<String> undoActions = new ArrayList<>(undoItems.size());
		List<String> labels = new ArrayList<>(undoItems.size());
		for (UndoItem item: undoItems) {
			actions.add(item.content());
			undoActions.add(item.previousContent());
			labels.add(item.getLabel());
		}
		undoManager.buildAction(ActionType.UPDATE, actions.toArray(new String[0]))
				.withUndo(ActionType.UPDATE, undoActions.toArray(new String[0]))
				.withLabels(labels.toArray(new String[0]))
				.storeAndNotifyUnsaved();
	}

	/**
	 * Store undo
	 * @return if there is items in undo list.
	 */
	public boolean storeUndo() {
		if (!undoItems.isEmpty()) {
			storeUpdateAction();
		}
		return undoItems.isEmpty();
	}

	/**
	 * Store undo
	 * @return if there is items in undo list.
	 */
	public boolean isEmpty() {
		return undoItems.isEmpty();
	}
}
