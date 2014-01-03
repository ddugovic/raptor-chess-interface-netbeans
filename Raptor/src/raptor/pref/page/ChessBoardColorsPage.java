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

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;

public class ChessBoardColorsPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();
	
	public ChessBoardColorsPage() {
		// Use the "flat" layout
		super(GRID);
		setTitle(local.getString("colors"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {

		addField(new ColorFieldEditor(PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR,
				local.getString("chessBColP1"), getFieldEditorParent()));

		addField(new ColorFieldEditor(
				PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR,
				local.getString("chessBColP2"), getFieldEditorParent()));

		ColorFieldEditor defaultMessages = new ColorFieldEditor(
				PreferenceKeys.BOARD_BACKGROUND_COLOR, local.getString("chessBColP3"),
				getFieldEditorParent());
		addField(defaultMessages);

		ColorFieldEditor coordinatesColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_COLOR, local.getString("chessBColP4"),
				getFieldEditorParent());
		addField(coordinatesColor);

		ColorFieldEditor lagLabelColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_CONTROL_COLOR, local.getString("chessBColP5"),
				getFieldEditorParent());
		addField(lagLabelColor);

		ColorFieldEditor lagOver20LabelColor = new ColorFieldEditor(
				PreferenceKeys.BOARD_LAG_OVER_20_SEC_COLOR,
				local.getString("chessBColP6"), getFieldEditorParent());
		addField(lagOver20LabelColor);

		addField(new ColorFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_BACKGROUND_COLOR,
				local.getString("chessBColP7"), getFieldEditorParent()));

		addField(new ColorFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_COLOR,
				local.getString("chessBColP8"), getFieldEditorParent()));

	}
}