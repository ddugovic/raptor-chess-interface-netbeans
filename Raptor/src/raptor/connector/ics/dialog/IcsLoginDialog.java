/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.connector.ics.dialog;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.util.Logger;

/**
 * The ics login dialog.
 */
public class IcsLoginDialog extends Dialog implements PreferenceKeys {

	private static final Logger LOG = Logger.getLogger(IcsLoginDialog.class);
	private static final String[] PROFILES = {"Primary", "Secondary", "Tertiary"};
	protected Button autoLoginCheckBox;
	protected Button guestLoginCheckBox;
	protected Text handleField;
	protected Label handleLabel;
	protected String lastSelection;
	protected Button loginButton;
	protected Text passwordField;
	protected Label passwordLabel;
	protected Text portField;
	protected Label portLabel;
	protected Combo profile;
	protected Label profileLabel;
	protected String profilePrefix;
	protected Text serverField;
	protected Label serverLabel;
	protected Button simulBugButton;
	protected Composite content;

	protected Button timesealEnabledCheckBox;
	protected String title;
	protected boolean wasLoginPressed;
	protected boolean isShowingSimulBug;
	protected boolean isSimulBugLogin;
	protected boolean isShowingAutoLogin = true;
	protected static L10n local = L10n.getInstance();

	public IcsLoginDialog(String profilePrefix, String title) {
		super(Raptor.getInstance().getWindow().getShell());
		this.profilePrefix = profilePrefix;
		this.title = title;
	}

	@Override
	public Composite createContents(Composite parent) {
		getShell().setText(title);

		content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(2, false));

		profileLabel = new Label(content, SWT.NONE);
		profileLabel.setText(local.getString("icsLoginD0"));
		profile = new Combo(content, SWT.DROP_DOWN | SWT.READ_ONLY);
		profile.add(local.getString("icsLoginD1"));
		profile.add(local.getString("icsLoginD2"));
		profile.add(local.getString("icsLoginD3"));

		profile.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String itemSelected = PROFILES[profile.getSelectionIndex()];
				storeProfile(lastSelection);
				loadFromProfile(itemSelected);
				lastSelection = itemSelected;
			}
		});

		handleLabel = new Label(content, SWT.NONE);
		handleLabel.setText(local.getString("icsLoginD4"));
		handleField = new Text(content, SWT.BORDER);
		handleField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		passwordLabel = new Label(content, SWT.NONE);
		passwordLabel.setText(local.getString("icsLoginD5"));
		passwordField = new Text(content, SWT.BORDER | SWT.PASSWORD);
		passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		serverLabel = new Label(content, SWT.NONE);
		serverLabel.setText(local.getString("icsLoginD6"));
		serverField = new Text(content, SWT.BORDER);
		serverField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		portLabel = new Label(content, SWT.NONE);
		portLabel.setText(local.getString("icsLoginD7"));
		portField = new Text(content, SWT.BORDER);
		portField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		guestLoginCheckBox = new Button(content, SWT.CHECK);
		guestLoginCheckBox.setText(local.getString("icsLoginD8"));
		GridData data = new GridData();
		data.horizontalSpan = 2;
		guestLoginCheckBox.setLayoutData(data);

		timesealEnabledCheckBox = new Button(content, SWT.CHECK);
		timesealEnabledCheckBox.setText(local.getString("icsLoginD9"));
		data = new GridData();
		data.horizontalSpan = 2;
		timesealEnabledCheckBox.setLayoutData(data);

		if (isShowingAutoLogin) {
			autoLoginCheckBox = new Button(content, SWT.CHECK);
			autoLoginCheckBox.setText(local.getString("icsLoginD10"));
			data = new GridData();
			data.horizontalSpan = 2;
			autoLoginCheckBox.setLayoutData(data);
		}

		if (isShowingSimulBug) {
			simulBugButton = new Button(content, SWT.CHECK);
			simulBugButton.setText(local.getString("icsLoginD11"));
			data = new GridData();
			data.horizontalSpan = 2;
			simulBugButton.setLayoutData(data);
		}

		loginButton = new Button(content, SWT.PUSH);
		loginButton.setText(local.getString("icsLoginD12"));
		data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.CENTER;
		loginButton.setLayoutData(data);

		SelectionListener selectionListener = new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (e.widget == guestLoginCheckBox
						|| e.widget == timesealEnabledCheckBox) {
					adjustToCheckBoxControls();
				} else if (e.widget == loginButton) {
					processLogin();
				}
			}
		};
		
		KeyListener keyListener = new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\r' || e.character == '\r') {
					processLogin();
				}
			}
			
		};

		// Commented out because it doesn't allow for enter key login with GTK like I thought it would.
		// In OSX the selection box can't get focus, but in Linux it can which is a pain.
		//profile.addKeyListener(keyListener);
		//profile.addSelectionListener(selectionListener);
		
		handleField.addKeyListener(keyListener);
		passwordField.addKeyListener(keyListener);
		serverField.addKeyListener(keyListener);
		portField.addKeyListener(keyListener);
		guestLoginCheckBox.addKeyListener(keyListener);
		guestLoginCheckBox.addSelectionListener(selectionListener);
		timesealEnabledCheckBox.addKeyListener(keyListener);
		timesealEnabledCheckBox.addSelectionListener(selectionListener);
		
		// if statement needed to allow "another simultaneous connection"
		if (autoLoginCheckBox != null) {
			autoLoginCheckBox.addKeyListener(keyListener);
		}
		
		loginButton.addSelectionListener(selectionListener);

		
		String currentProfile = Raptor.getInstance().getPreferences()
				.getString(profilePrefix + "profile");
		lastSelection = currentProfile;
		loadFromProfile(currentProfile);

		int selectedIndex = 0;
		for (int i = 0; i < PROFILES.length; i++) {
			if (StringUtils.equals(PROFILES[i], currentProfile)) {
				selectedIndex = i;
				break;
			}
		}
		profile.select(selectedIndex);
		adjustToCheckBoxControls();

		content.pack();
		parent.pack();
		return content;
	}
	
	public void processLogin() {
		String handleText = handleField.getText();
		String passwordText = passwordField.getText();

		if (StringUtils.isNotEmpty(handleText)
				&& (handleText.length() < 3 || handleText.length() > 17)) {
			MessageBox box = new MessageBox(content.getShell(),
					SWT.ERROR | SWT.OK);
			box
					.setMessage(local.getString("icsLoginD14"));
			box.setText(local.getString("icsLoginD15"));
			box.open();
		} else if (StringUtils.isNotBlank(handleText)
				&& !handleText.matches("[a-zA-Z]*")) { 
			MessageBox box = new MessageBox(content.getShell(),
					SWT.ERROR | SWT.OK);
			box.setMessage(local.getString("icsLoginD17"));
			box.setText(local.getString("icsLoginD18"));
			box.open();
		} else if (!guestLoginCheckBox.getSelection()
				&& StringUtils.isBlank(handleText)) {
			MessageBox box = new MessageBox(content.getShell(),
					SWT.ERROR | SWT.OK);
			box
					.setMessage(local.getString("icsLoginD19"));
			box.setText(local.getString("icsLoginD20"));
			box.open();
		} else if (!guestLoginCheckBox.getSelection()
				&& StringUtils.isBlank(passwordText)) {
			MessageBox box = new MessageBox(content.getShell(),
					SWT.ERROR | SWT.OK);
			box
					.setMessage(local.getString("icsLoginD21"));
			box.setText(local.getString("icsLoginD22"));
			box.open();
		} else {
			String itemSelected = PROFILES[profile.getSelectionIndex()];
			lastSelection = itemSelected;
			storeProfile(itemSelected);
			try {
				Raptor.getInstance().getPreferences().save();
			} catch (Throwable t) {
			}
			wasLoginPressed = true;

			if (isShowingSimulBug) {
				isSimulBugLogin = simulBugButton.getSelection();
			}
			close();
		}
	}

	public boolean isShowingAutoLogin() {
		return isShowingAutoLogin;
	}

	public void setShowingAutoLogin(boolean isShowingAutoLogin) {
		this.isShowingAutoLogin = isShowingAutoLogin;
	}

	public String getSelectedProfile() {
		return lastSelection;
	}

	public boolean isShowingSimulBug() {
		return isShowingSimulBug;
	}

	public boolean isSimulBugLogin() {
		return isSimulBugLogin;
	}

	@Override
	public int open() {
		return super.open();
	}

	public void setShowingSimulBug(boolean isShowingSimulBug) {
		this.isShowingSimulBug = isShowingSimulBug;
	}

	public void setSimulBugLogin(boolean isSimulBugLogin) {
		this.isSimulBugLogin = isSimulBugLogin;
	}

	public boolean wasLoginPressed() {
		return wasLoginPressed;
	}

	protected void adjustToCheckBoxControls() {
		if (guestLoginCheckBox.getSelection()) {
			passwordField.setText(""); 
			passwordLabel.setEnabled(false);
			passwordField.setEnabled(false);
			handleLabel.setEnabled(true);
			handleField.setEnabled(true);
		} else {
			passwordLabel.setEnabled(true);
			passwordField.setEnabled(true);
			handleLabel.setEnabled(true);
			handleField.setEnabled(true);
		}
	}

	@Override
	protected void initializeBounds() {
		super.initializeBounds();
		Shell shell = getShell();
		Monitor primary = shell.getMonitor();
		Rectangle bounds = primary.getBounds();
		Rectangle rect = shell.getBounds();
		int x = bounds.x + (bounds.width - rect.width) / 2;
		int y = bounds.y + (bounds.height - rect.height) / 2;
		shell.setLocation(x, y);
	}

	protected void loadFromProfile(String profileName) {
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();
		String prefix = profilePrefix + profileName + "-"; 
		handleField.setText(prefs.getString(prefix + "user-name")); 
		serverField.setText(prefs.getString(prefix + "server-url")); 
		portField.setText(prefs.getString(prefix + "port")); 
		passwordField.setText(prefs.getString(prefix + "password")); 
		guestLoginCheckBox.setSelection(prefs.getBoolean(prefix
				+ "is-anon-guest") 
				|| prefs.getBoolean(prefix + "is-named-guest")); 
		timesealEnabledCheckBox.setSelection(prefs.getBoolean(prefix
				+ "timeseal-enabled")); 
		if (autoLoginCheckBox != null) {
			autoLoginCheckBox.setSelection(prefs.getBoolean(profilePrefix
					+ "auto-connect")); 
		}
		LOG.info("Loaded loadFromProfile " + profileName); 
		adjustToCheckBoxControls();
	}

	protected void storeProfile(String profileName) {
		RaptorPreferenceStore prefs = Raptor.getInstance().getPreferences();
		String prefix = profilePrefix + profileName + "-"; 

		prefs.setValue(prefix + "user-name", StringUtils 
				.defaultString(handleField.getText()));
		prefs.setValue(prefix + "server-url", StringUtils 
				.defaultString(serverField.getText()));
		prefs.setValue(prefix + "password", StringUtils 
				.defaultString(passwordField.getText()));

		try {
			prefs.setValue(prefix + "port", Integer.parseInt(StringUtils 
					.defaultString(portField.getText())));
		} catch (NumberFormatException nfe) {
		}

		prefs.setValue(prefix + "is-named-guest", guestLoginCheckBox 
				.getSelection()
				&& StringUtils.isNotBlank(handleField.getText()));
		prefs.setValue(prefix + "is-anon-guest", guestLoginCheckBox 
				.getSelection()
				&& StringUtils.isBlank(handleField.getText()));
		prefs.setValue(prefix + "timeseal-enabled", timesealEnabledCheckBox 
				.getSelection());
		if (autoLoginCheckBox != null) {
			prefs.setValue(profilePrefix + "auto-connect", autoLoginCheckBox 
					.getSelection());
		}

		// Don't store off the the profileName-profile here.
		// It should'nt be stored for fics2/bics2 logins.
		prefs.save();
		LOG.info("Saved " + profileName); 
	}
}

