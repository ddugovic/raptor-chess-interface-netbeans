package raptor.pref.page;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.engine.xboard.XboardEngine;
import raptor.international.L10n;
import raptor.service.XboardEngineService;

public class XboardEnginesPage extends PreferencePage {
	
	protected Combo enginesCombo;
	protected Text userNameText;
	protected Text processLocationText;
	protected Label engineName;
	protected Button pickFileButton;
	protected Button defaultButton;
	protected Button deleteButton;
	protected XboardEngine currentEngine;
	protected Composite parent;
	protected boolean isBuildingEnginesCombo = false;
	
	protected static L10n local = L10n.getInstance();
	
	public XboardEnginesPage() {
		super();
		setPreferenceStore(Raptor.getInstance().getPreferences());
		setTitle(local.getString("xboardEng"));
	}

	@Override
	protected Control createContents(Composite parent) {
		this.parent = parent;
		parent = new Composite(parent, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2,
				1));
		label
				.setText(WordUtils
						.wrap(local.getString("xboardEngP1"),
								70));
		Label enginesLabel = new Label(parent, SWT.LEFT);
		enginesLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		enginesLabel.setText(local.getString("engines"));

		enginesCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		enginesCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		enginesCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!isBuildingEnginesCombo) {
					if (currentEngine != null) {
						currentEngine.quit();
					}
					loadEngine(XboardEngineService.getInstance().getXboardEngine(
							enginesCombo.getText()));
				}
			}
		});

		Label processLabel = new Label(parent, SWT.LEFT);
		processLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		processLabel.setText(local.getString("engLoc"));

		Composite processComposite = new Composite(parent, SWT.NONE);
		processComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));
		processComposite.setLayout(new GridLayout(2, false));

		processLocationText = new Text(processComposite, SWT.SINGLE
				| SWT.BORDER);
		processLocationText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER,
				true, false, 1, 1));

		pickFileButton = new Button(processComposite, SWT.PUSH);
		pickFileButton.setText(local.getString("selLoc"));
		pickFileButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		pickFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog fd = new FileDialog(getShell(), SWT.OPEN);
				fd.setText(local.getString("xboardEngP2"));
				fd.setFilterPath("");
				final String selected = fd.open();
				if (!StringUtils.isBlank(selected)) {
					try {
						processLocationText.setText(new File(selected)
								.getCanonicalPath());
					} catch (IOException ioe) {
						Raptor.getInstance().onError(local.getString("errGetFl"),
								ioe);
					}

					if (currentEngine != null) {
						currentEngine.quit();
					}
					currentEngine = new XboardEngine();
					currentEngine.setProcessPath(processLocationText.getText());
					currentEngine.setEngineName(userNameText.getText());
					loadEngine(currentEngine);
				}
			}
		});

		defaultButton = new Button(parent, SWT.CHECK);
		defaultButton.setText(local.getString("defEng"));
		defaultButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 2, 1));

		Label userNameLabel = new Label(parent, SWT.LEFT);
		userNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		userNameLabel.setText(local.getString("nickname"));

		userNameText = new Text(parent, SWT.SINGLE | SWT.BORDER);
		userNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		Label engineNameHeaderLabel = new Label(parent, SWT.LEFT);
		engineNameHeaderLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false, 1, 1));
		engineNameHeaderLabel.setText(local.getString("engName"));

		engineName = new Label(parent, SWT.LEFT);
		engineName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1));

		deleteButton = new Button(parent, SWT.LEFT);
		deleteButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false, 1, 1));
		deleteButton.setText(local.getString("delEng"));
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onDelete();
			}
		});

		reload();
		return parent;
	}
	
	protected void loadEngine(XboardEngine engine) {
		if (!engine.isConnected()) {
			if (!engine.connect()) {
				Raptor
						.getInstance()
						.alert(local.getString("engTlbAlert"));
				return;
			}
		}
		currentEngine = engine;
		engineName.setText(StringUtils.defaultString(currentEngine
				.getEngineName(), ""));
		defaultButton.setSelection(engine.isDefault());
		processLocationText.setText(engine.getProcessPath());
		userNameText.setText(engine.getEngineName());
	}

	protected void onDelete() {
		if (StringUtils.isNotBlank(userNameText.getText())) {
			if (currentEngine != null) {
				currentEngine.quit();
			}
			XboardEngineService.getInstance().deleteConfiguration(
					userNameText.getText());
			reload();
		}
	}

	protected void onSave() {
		if (StringUtils.isBlank(processLocationText.getText())) {
			Raptor.getInstance().alert(local.getString("engLocEmpt"));
		} else if (StringUtils.isBlank(userNameText.getText())) {
			Raptor.getInstance().alert(local.getString("nickLocEmpt"));
		} else if (currentEngine == null) {
			Raptor.getInstance().alert(local.getString("engTlbAlert3"));
		} else {
			if (currentEngine != null) {
				currentEngine.quit();
			} else {
				currentEngine = new XboardEngine();
			}
			currentEngine.setProcessPath(processLocationText.getText());
			currentEngine.setEngineName(userNameText.getText());
			currentEngine.setDefault(defaultButton.getSelection());

			XboardEngineService.getInstance().saveConfiguration(currentEngine);
			reload();
		}
	}

	@Override
	protected void performApply() {
		onSave();
		super.performApply();
	}

	protected void reload() {
		isBuildingEnginesCombo = true;
		XboardEngine[] currentEngines = XboardEngineService.getInstance()
				.getXboardEngines();
		enginesCombo.removeAll();
		for (XboardEngine engine : currentEngines) {
			enginesCombo.add(engine.getEngineName());
		}

		XboardEngine defaultEngine = XboardEngineService.getInstance()
				.getDefaultEngine();
		if (defaultEngine != null) {
			for (int i = 0; i < enginesCombo.getItemCount(); i++) {
				if (enginesCombo.getItem(i).equals(defaultEngine.getEngineName())) {
					enginesCombo.select(i);
					loadEngine(defaultEngine);
					break;
				}
			}
		}
		isBuildingEnginesCombo = false;
	}

}
