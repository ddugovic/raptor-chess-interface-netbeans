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
package raptor.swt.chess.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

import raptor.chess.GameConstants.Piece;
import raptor.chess.Variant;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.ChessBoard;
import raptor.swt.chess.ChessSquare;
import raptor.swt.chess.PieceJailChessSquare;

public abstract class ChessBoardLayout extends Layout implements PreferenceKeys {

	public static enum Field {
		CLOCK_LABEL, CURRENT_PREMOVE_LABEL, GAME_DESCRIPTION_LABEL, LAG_LABEL, NAME_RATING_LABEL, OPENING_DESCRIPTION_LABEL, STATUS_LABEL, UP_TIME_LABEL
	}

	protected ChessBoard board;
	protected int pieceJailColumns = 3, pieceJailRows = 2;

	public ChessBoardLayout(ChessBoard board) {
		super();
		this.board = board;
	}

	public abstract void adjustFontSizes();

	public void dispose() {
	}

	public abstract int getAlignment(Field field);

	public ChessBoard getBoard() {
		return board;
	}

	public abstract String getName();

	@Override
	protected Point computeSize(Composite composite, int hint, int hint2,
			boolean flushCache) {
		return composite.getSize();
	}

	protected void layoutChessBoard(Rectangle boardRect) {
		board.getBoardComposite().setBounds(boardRect);

                GridLayout boardLayout = new GridLayout(8, true);
                boardLayout.horizontalSpacing = boardLayout.verticalSpacing = 0;
                boardLayout.marginHeight = boardLayout.marginWidth = 0;
                board.getBoardComposite().setLayout(boardLayout);

		ChessSquare[][] squares = board.getSquares();
		ChessSquare prevSquare = null;
		for (byte rank = 0; rank < 8; rank++) {
			for (byte file = 7; file > -1; file--) {
				ChessSquare square = squares[rank][file];
				if (prevSquare != null) {
					if (board.isWhiteOnTop())
						square.moveBelow(prevSquare);
					else
						square.moveAbove(prevSquare);
				}
				prevSquare = square;
			}
		}

		for (ChessSquare[] rank : board.getSquares()) {
			for (ChessSquare boardSquare : rank) {
				if (boardSquare != null)
					boardSquare.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
		}

		// TODO: Dynamic piece jail layout based on:
		// a) Are piece drops legal (crazyhouse/bughouse/setup) - pawn first (ergonomic)
		// b1) Can king be captured (atomic/suicide) - display king
		// b2) Standard/other - display without king
		if (Variant.isClassic(board.getController().getGame().getVariant())) {
			pieceJailColumns = 5;
			board.getPieceJailSquare(Piece.WN).moveAbove(board.getPieceJailSquare(Piece.WP));
			board.getPieceJailSquare(Piece.WB).moveAbove(board.getPieceJailSquare(Piece.WN));
			board.getPieceJailSquare(Piece.WR).moveAbove(board.getPieceJailSquare(Piece.WB));
			board.getPieceJailSquare(Piece.WQ).moveAbove(board.getPieceJailSquare(Piece.WR));
			board.getPieceJailSquare(Piece.WK).setVisible(false);
			board.getPieceJailSquare(Piece.BN).moveAbove(board.getPieceJailSquare(Piece.BP));
			board.getPieceJailSquare(Piece.BB).moveAbove(board.getPieceJailSquare(Piece.BN));
			board.getPieceJailSquare(Piece.BR).moveAbove(board.getPieceJailSquare(Piece.BB));
			board.getPieceJailSquare(Piece.BQ).moveAbove(board.getPieceJailSquare(Piece.BR));
			board.getPieceJailSquare(Piece.BK).setVisible(false);
		}

                GridLayout whiteLayout = new GridLayout(getPieceJailColumns(), true);
                whiteLayout.horizontalSpacing = whiteLayout.verticalSpacing = 0;
                whiteLayout.marginHeight = whiteLayout.marginWidth = 0;
                board.getWhitePieceJailComposite().setLayout(whiteLayout);
                GridLayout blackLayout = new GridLayout(getPieceJailColumns(), true);
                blackLayout.horizontalSpacing = blackLayout.verticalSpacing = 0;
                blackLayout.marginHeight = blackLayout.marginWidth = 0;
		board.getBlackPieceJailComposite().setLayout(blackLayout);
		for (PieceJailChessSquare jailSquare : board.getPieceJailSquares()) {
			if (jailSquare != null)
				jailSquare.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		}
	}

	/**
	 * @return the pieceJailColumns
	 */
	protected int getPieceJailColumns() {
		return pieceJailColumns;
	}

	/**
	 * @return the pieceJailRows
	 */
	protected int getPieceJailRows() {
		return pieceJailRows;
	}
}
