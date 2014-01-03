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
package raptor.swt.chess;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.SWTUtils;
import raptor.util.Logger;

/**
 * A labeled chess square. Contains a label in the top right. Currently used for
 index jails and drop squares to show the number of indexs.
 */
public class PieceJailChessSquare extends ChessSquare {
	static final Logger LOG = Logger.getLogger(ChessSquare.class);

	protected static PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			PieceJailChessSquare square = (PieceJailChessSquare)e.getSource();
			if (!square.isVisible()) {
				return;
			}
			long startTime = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0;
			Point size = square.getSize();
			e.gc.fillRectangle(0, 0, size.x, size.y);

			int imageSize = square.getImageSize();
			if (square.pieceImage == null) {
				square.pieceImage = square.getChessPieceImage(square.pieceJailPiece, imageSize);
			}

			int pieceImageX = (size.x - imageSize) / 2;
			int pieceImageY = (size.y - imageSize) / 2;

			if (square.piece == Piece.EMPTY || square.isHidingPiece()) {
				switch (square.getPieceJailShadowAlpha()) {
				case 0:
					break;
				case 255:
					e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);
					break;
				default: // PERF: Consumes gigabytes of memory
					e.gc.setAlpha(square.getPieceJailShadowAlpha());
					e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);
					e.gc.setAlpha(255);
				}
			} else {
				e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);

				if (StringUtils.isNotBlank(square.getText())) {
					e.gc.setForeground(square.getPreferences().getColor(
							BOARD_PIECE_JAIL_LABEL_COLOR));
					e.gc.setFont(SWTUtils.getProportionalFont(square.getPreferences()
							.getFont(BOARD_PIECE_JAIL_FONT),
							square.getPieceJailLabelPercentage(), size.y));

					int width = e.gc.getFontMetrics().getAverageCharWidth()
							* square.getText().length() + 2;

					e.gc.drawString(square.getText(), size.x - width, 0, true);
				}
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Drew chess square: " + square.getId() + " in "
						+ (System.currentTimeMillis() - startTime));
			}
		}
	};

	protected String text = "";

	protected Piece pieceJailPiece;

	public PieceJailChessSquare(Composite parent, ChessBoard board, Piece pieceJailPiece, Square id) {
		super(parent, board, id, true);
		this.pieceJailPiece = pieceJailPiece;
		removePaintListener(ChessSquare.paintListener);
		addPaintListener(PieceJailChessSquare.paintListener);
	}

	/**
	 * Creates a ChessSquare not tied to a board. Useful in preferences. Use
	 * with care, this does'nt add any listeners besides the PaointListener and
	 * board will be null.
	 */
	public PieceJailChessSquare(Composite parent, Square id, Piece pieceJailPiece) {
		super(parent, id, true);
		this.pieceJailPiece = pieceJailPiece;
		removePaintListener(ChessSquare.paintListener);
		addPaintListener(PieceJailChessSquare.paintListener);
	}
	
	/**
	 * Updates the cursor for a drag with the specified index.
	 */
	protected void updateCursorForDrag(Piece piece) {
		if (getPreferences().getBoolean(
				PreferenceKeys.BOARD_IS_USING_CROSSHAIRS_CURSOR)) {
			getShell().setCursor(
					Raptor.getInstance().getDisplay()
							.getSystemCursor(SWT.CURSOR_CROSS));
		} else if (piece == Piece.EMPTY){
			int imageSide = getImageSize();
			getShell().setCursor(
					ChessBoardUtils.getCursorForPiece(ChessBoardUtils.pieceJailSquareToPiece(getId()),imageSide));
		} else {
			int imageSide = getImageSize();
			getShell().setCursor(
					ChessBoardUtils.getCursorForPiece(piece, imageSide));
		}
	}

	/**
	 * Returns the current label.
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the label.
	 * 
	 * @param text
	 */
	public void setText(String text) {
		if (!StringUtils.equals(this.text, text)) {
		    this.text = text;
		    isDirty = true;
		}
	}

	protected short getPieceJailLabelPercentage() {
		return (short)Raptor.getInstance().getPreferences().getInt(
				PreferenceKeys.BOARD_PIECE_JAIL_LABEL_PERCENTAGE);
	}

	protected short getPieceJailShadowAlpha() {
		return (short)getPreferences().getInt(BOARD_PIECE_JAIL_SHADOW_ALPHA);
	}
}
