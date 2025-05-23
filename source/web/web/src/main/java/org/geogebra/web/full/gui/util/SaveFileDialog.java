package org.geogebra.web.full.gui.util;

import org.geogebra.common.main.MaterialVisibility;
import org.geogebra.common.main.SaveController;
import org.geogebra.common.move.ggtapi.models.Material;
import org.geogebra.web.full.gui.view.algebra.InputPanelW;
import org.geogebra.web.full.main.AppWFull;
import org.geogebra.web.html5.gui.util.FormLabel;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.html5.util.GlobalHandlerRegistry;
import org.geogebra.web.shared.DialogUtil;
import org.geogebra.web.shared.components.dialog.ComponentDialog;
import org.geogebra.web.shared.components.dialog.DialogData;
import org.gwtproject.core.client.Scheduler;
import org.gwtproject.dom.client.NativeEvent;
import org.gwtproject.user.client.ui.FlowPanel;

import com.himamis.retex.editor.web.MathFieldW;

import elemental2.core.JsDate;
import elemental2.dom.DomGlobal;

public abstract class SaveFileDialog extends ComponentDialog implements
		SaveController.SaveListener, SaveDialogI {
	private FlowPanel contentPanel;
	private FlowPanel inputPanel;
	private InputPanelW titleField;

	/**
	 * Base dialog constructor
	 * @param app - see {@link AppW}
	 * @param dialogData - contains trans keys for title and buttons
	 * @param autoHide - true if dialog should be hidden on background click
	 */
	public SaveFileDialog(AppW app,
			DialogData dialogData, boolean autoHide) {
		super(app, dialogData, autoHide, true);
		addStyleName("saveDialog");
		buildContent();
		initActions();
		DialogUtil.hideOnLogout(app, this);
		setSaveType(app.isWhiteboardActive()
				? Material.MaterialType.ggs : Material.MaterialType.ggb);
	}

	/**
	 * build dialog content
	 */
	public void buildContent() {
		contentPanel = new FlowPanel();
		inputPanel = new FlowPanel();

		inputPanel.setStyleName("mowInputPanelContent");
		inputPanel.addStyleName("emptyState");

		titleField = new InputPanelW("", app, 25, false);
		FormLabel titleLbl = new FormLabel().setFor(titleField.getTextComponent());
		titleLbl.setText(app.getLocalization().getMenu("Title"));
		titleLbl.addStyleName("inputLabel");
		titleField.getTextComponent().getTextBox().getElement().setAttribute(
				"placeholder", app.getLocalization().getMenu("Untitled"));
		titleField.addStyleName("inputText");

		inputPanel.add(titleLbl);
		inputPanel.add(titleField);

		contentPanel.add(inputPanel);
		addDialogContent(contentPanel);
	}

	public FlowPanel getContentPanel() {
		return contentPanel;
	}

	private void initActions() {
		// set focus to input field!
		Scheduler.get().scheduleDeferred(() -> getInputField().getTextComponent().setFocus(true));
		addFocusBlurHandlers();
		addHoverHandlers();
		setOnNegativeAction(app.getSaveController()::cancel);
		Runnable afterSave = () -> app.getSaveController().runAfterSaveCallback(true);
		setOnPositiveAction(() -> {
			if (((AppW) app).getFileManager().saveCurrentLocalIfPossible(app,
					afterSave)) {
				return;
			}
			if (!((AppW) app).getFileManager().isOnlineSavingPreferred()) {
				app.getSaveController().showLocalSaveDialog(afterSave);
			} else {
				if (!app.getLoginOperation().isLoggedIn()) {
					hide();
					((AppWFull) app).getActivity().markSaveProcess(getInputField().getText(),
							getSaveVisibility());
					((AppW) app).getGuiManager().listenToLogin(this::onSave);
					app.getLoginOperation().showLoginDialog();
				} else {
					onSave();
				}
			}
		});
		titleField.getTextComponent().addKeyUpHandler(event -> {
			NativeEvent nativeEvent = event.getNativeEvent();
			// we started handling Ctrl+S in graphics view but then focus moved to this dialog
			// make sure the keyup event doesn't clear selection
			if (MathFieldW.checkCode(nativeEvent, "KeyS")
				&& (nativeEvent.getCtrlKey() || nativeEvent.getMetaKey())) {
				setTitle();
			}
		});
		titleField.addTextComponentInputListener(
				ignore -> setPosBtnDisabled(titleField.getText().isEmpty()));
		GlobalHandlerRegistry globalHandlers = ((AppW) app).getGlobalHandlers();
		globalHandlers.addEventListener(DomGlobal.window, "unload",
				event -> app.getSaveController().cancel());
	}

	private void onSave() {
		app.getSaveController().ensureTypeOtherThan(Material.MaterialType.ggsTemplate);
		app.getSaveController().saveAs(getInputField().getText(),
				getSaveVisibility(), this);
	}

	/**
	 * Sets material type to be saved.
	 *
	 * @param saveType for the dialog.
	 */
	@Override
	public void setSaveType(Material.MaterialType saveType) {
		app.getSaveController().setSaveType(saveType);
	}

	@Override
	public void setDiscardMode() {
		// nothing to do here
		// will be removed from interface with APPS-2066
	}

	protected MaterialVisibility getSaveVisibility() {
		Material activeMaterial = app.getActiveMaterial();
		if (activeMaterial == null) {
			return app.isMebis() ? MaterialVisibility.Private : MaterialVisibility.Shared;
		}

		MaterialVisibility visibility = MaterialVisibility.value(activeMaterial.getVisibility());
		if (visibility == MaterialVisibility.Shared && !sameMaterial(activeMaterial)) {
			return MaterialVisibility.Private;
		}
		return visibility;
	}

	private boolean sameMaterial(Material material) {
		return app.getLoginOperation().owns(material)
				&& material.getTitle().equals(getInputField().getText());
	}

	/**
	 * Add mouse over/ out handlers
	 */
	private void addHoverHandlers() {
		titleField.getTextComponent().getTextBox()
				.addMouseOverHandler(event -> getInputPanel().addStyleName("hoverState"));
		titleField.getTextComponent().getTextBox()
				.addMouseOutHandler(event -> getInputPanel().removeStyleName("hoverState"));
	}

	private void addFocusBlurHandlers() {
		titleField.getTextComponent().getTextBox()
				.addFocusHandler(event -> setFocusState());
		titleField.getTextComponent().getTextBox()
				.addBlurHandler(event -> resetInputField());
	}

	/**
	 * sets the style of InputPanel to focus state
	 */
	protected void setFocusState() {
		getInputPanel().setStyleName("mowInputPanelContent");
		getInputPanel().addStyleName("focusState");
	}

	/**
	 * Resets input style on blur
	 */
	protected void resetInputField() {
		getInputPanel().removeStyleName("focusState");
		getInputPanel().addStyleName("emptyState");
	}

	/**
	 * @return panel holding input with label and error label
	 */
	public FlowPanel getInputPanel() {
		return inputPanel;
	}

	/**
	 * @return input text field
	 */
	public InputPanelW getInputField() {
		return titleField;
	}

	/**
	 * Sets initial title for the material to save.
	 */
	public void setTitle() {
		app.getSaveController().updateSaveTitle(getInputField()
						.getTextComponent(), getDefaultTitle());
		inputPanel.setVisible(shouldInputPanelBeVisible());
		Scheduler.get().scheduleDeferred(() -> getInputField().setFocusAndSelectAll());
	}

	private String getDefaultTitle() {
		// for Mebis users suggest the current date as title
		return app.isMebis() ? DateTimeFormat.format(new JsDate())
				: app.getLocalization().getMenu("Untitled");
	}

	@Override
	public void show() {
		super.show();
		setTitle();
	}

	protected abstract boolean shouldInputPanelBeVisible();
}