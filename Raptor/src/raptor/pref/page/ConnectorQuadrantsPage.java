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
package raptor.pref.page;

import org.apache.commons.lang.WordUtils;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.swt.BugButtonsWindowItem;
import raptor.swt.BugGames;
import raptor.swt.SeekTableWindowItem;
import raptor.swt.chat.ChatConsoleWindowItem;

public class ConnectorQuadrantsPage extends FieldEditorPreferencePage {
	protected String connectorShortName;
	
	protected static L10n local = L10n.getInstance();

	public ConnectorQuadrantsPage(String connectorShortName) {
		super(GRID);
		setTitle(local.getString("quadConn", WordUtils.capitalize(connectorShortName)));
		setPreferenceStore(Raptor.getInstance().getPreferences());
		this.connectorShortName = connectorShortName;
	}

	protected String[][] buildQuadrantArray(Quadrant[] quadrants) {
		String[][] result = new String[quadrants.length][2];
		for (int i = 0; i < quadrants.length; i++) {
			result[i][0] = quadrants[i].name();
			result[i][1] = quadrants[i].name();
		}
		return result;
	}

	@Override
	protected void createFieldEditors() {
		Label textLabel = new Label(getFieldEditorParent(), SWT.WRAP);
		textLabel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 2, 1));
		textLabel
				.setText(WordUtils
						.wrap(local.getString("connQuadrP1"), 70)
						+ "\n\t"
						+ WordUtils.wrap(local.getString("connQuadrP2"), 70));

		Label label = new Label(getFieldEditorParent(), SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				2, 1));
		label.setImage(Raptor.getInstance().getImage(
				Raptor.RESOURCES_DIR + "/images/quadrants.png"));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUG_BUTTONS_QUADRANT, local.getString("connQuadrP3"),
				buildQuadrantArray(BugButtonsWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.MAIN_TAB_QUADRANT, local.getString("connQuadrP4"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.CHANNEL_TAB_QUADRANT, local.getString("connQuadrP5"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PERSON_TAB_QUADRANT, local.getString("connQuadrP6"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.GAME_CHAT_TAB_QUADRANT, local.getString("connQuadrP7"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.PARTNER_TELL_TAB_QUADRANT,
				local.getString("connQuadrP8"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.REGEX_TAB_QUADRANT,
				local.getString("connQuadrP9"),
				buildQuadrantArray(ChatConsoleWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.BUG_WHO_QUADRANT,
				local.getString("connQuadrP10"),
				buildQuadrantArray(BugGames.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));

		addField(new ComboFieldEditor(connectorShortName + "-"
				+ PreferenceKeys.SEEK_TABLE_QUADRANT, local.getString("connQuadrP11"),
				buildQuadrantArray(SeekTableWindowItem.MOVE_TO_QUADRANTS),
				getFieldEditorParent()));
	}
}
