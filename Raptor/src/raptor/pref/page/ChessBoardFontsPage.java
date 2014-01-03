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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FontFieldEditor;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;

public class ChessBoardFontsPage extends FieldEditorPreferencePage {
	
	protected static L10n local = L10n.getInstance();
	
	public ChessBoardFontsPage() {
		super(GRID);
		setTitle(local.getString("fonts"));
		setPreferenceStore(Raptor.getInstance().getPreferences());
	}

	@Override
	protected void createFieldEditors() {
		addField(new FontFieldEditor(PreferenceKeys.BOARD_CLOCK_FONT,
				local.getString("chessBFontsP1"), getFieldEditorParent()));

		FontFieldEditor coordinatesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_COORDINATES_FONT, local.getString("chessBFontsP2"),
				getFieldEditorParent());
		addField(coordinatesFont);

		FontFieldEditor gameDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_GAME_DESCRIPTION_FONT,
				local.getString("chessBFontsP3"), getFieldEditorParent());
		addField(gameDescriptionFont);

		FontFieldEditor lagFont = new FontFieldEditor(
				PreferenceKeys.BOARD_LAG_FONT, local.getString("chessBFontsP4"),
				getFieldEditorParent());
		addField(lagFont);

		FontFieldEditor lastMoveFont = new FontFieldEditor(
				PreferenceKeys.BOARD_STATUS_FONT, local.getString("chessBFontsP5"),
				getFieldEditorParent());
		addField(lastMoveFont);

		FontFieldEditor openingDescriptionFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, local.getString("chessBFontsP6"),
				getFieldEditorParent());
		addField(openingDescriptionFont);

		FontFieldEditor pieceJailFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PIECE_JAIL_FONT,
				local.getString("chessBFontsP7"), getFieldEditorParent());
		addField(pieceJailFont);

		FontFieldEditor playerNameFont = new FontFieldEditor(
				PreferenceKeys.BOARD_PLAYER_NAME_FONT,
				local.getString("chessBFontsP8"), getFieldEditorParent());
		addField(playerNameFont);

		FontFieldEditor premovesFont = new FontFieldEditor(
				PreferenceKeys.BOARD_OPENING_DESC_FONT, local.getString("chessBFontsP9"),
				getFieldEditorParent());
		addField(premovesFont);
	}
}