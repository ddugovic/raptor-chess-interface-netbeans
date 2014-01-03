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
package raptor.alias;

import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.chess.Game;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardWindowItem;

public class ShowFenAlias extends RaptorAlias {

	public ShowFenAlias() {
		super("fen",
				"Shows the FEN for all of the boards currently being viwed.",
				"showfen fenString. Example: \"showfen\"");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.equalsIgnoreCase("fen")) {
			RaptorWindowItem[] windowItems = Raptor.getInstance().getWindow()
					.getWindowItems(ChessBoardWindowItem.class);

			StringBuilder text = new StringBuilder(400);
			if (windowItems.length > 0) {
				text.append("FEN for opened boards:\n");
				for (RaptorWindowItem item : windowItems) {
					Game game = ((ChessBoardWindowItem) item).getController()
							.getGame();
                    text.append("Game ").append(game.getId()).append("    ").append(game.toFen()).append("\n");
				}
			} else {
				text.append("There are no open boards to display FEN for.");
			}

			return new RaptorAliasResult(null, text.toString());

		}
		return null;
	}
}