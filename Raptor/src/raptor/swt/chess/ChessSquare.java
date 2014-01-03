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

import raptor.util.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.SWTUtils;

/**
 * A class representing a chess square on a chess board. This class can also be
 used to represent a index jail or drop square.
 */
public class ChessSquare extends Canvas implements GameConstants, PreferenceKeys {
	public static final String CLICK_INITIATOR = "CLICK_INITIATOR";
	public static final String DRAG_INITIATOR = "DRAG_INITIATOR";
	public static final String DROP_HANDLED = "DROP_HANDLED";
	public static final String LAST_DROP_TIME = "LAST_DROP_TIME";
	static final Logger LOG = Logger.getLogger(ChessSquare.class);

	protected ChessBoard board;
	/**
	 * Forces a layout when the size of the square changes.
	 */
	protected ControlListener controlListener = new ControlListener() {

		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			clearCache();
		}
	};
	/**
	 * Handles drags and drops.
	 */
	protected static Listener dndListener;
	static {
		dndListener = new Listener() {
			public void handleEvent(Event e) {
				ChessSquare square = (ChessSquare) e.widget;
				if (!square.getPreferences().getString(
						PreferenceKeys.BOARD_USER_MOVE_INPUT_MODE).equals(
						UserMoveInputMode.DragAndDrop.toString())) {
					return;
				}
				switch (e.type) {
				case SWT.MouseDown: {
					if (e.button != 1) {
						return;
					}
					ChessBoard board = square.board;
					Composite control = board.getControl();
					Long lastDropTime = (Long) control.getData(LAST_DROP_TIME);

					// In windows a mouse down event gets sent right after the mouse
					// up and produce a ghost on the drop square. This is an attempt
					// to fix it.
					if (lastDropTime != null
							&& ((System.currentTimeMillis() - lastDropTime) < 50)) {
						return;
					}

					if (square.piece != Piece.EMPTY
							&& board.getController().canUserInitiateMoveFrom(square.id)) {
						control.setData(DRAG_INITIATOR, square);
						control.setData(DROP_HANDLED, false);
						square.updateCursorForDrag(square.piece);
						board.controller.userInitiatedMove(square.id);
					} else if (square.piece == Piece.EMPTY
							&& board.getController()
									.canUserInitiateMoveFromEmptySquare(square.id)) {
						control.setData(DRAG_INITIATOR, square);
						control.setData(DROP_HANDLED, false);
						square.updateCursorForDrag(square.piece);
						board.controller.userInitiatedMove(square.id);
					} else {
						control.setData(DRAG_INITIATOR, null);
						control.setData(DROP_HANDLED, false);
					}
					break;
				}
				case SWT.MouseUp: {
					if (e.button != 1) {
						return;
					}

					ChessBoard board = square.board;
					Composite control = board.getControl();
					ChessSquare dragSource = (ChessSquare) control.getData(DRAG_INITIATOR);
					if (dragSource == null) {
						return;
					}
					square.updateCursorForDragEnd();

					ChessSquare dragEnd = square.getSquareCursorIsAt();
					if (dragEnd == null || dragEnd == dragSource) {
						board.controller.userCancelledMove(dragSource.id);
						control.setData(LAST_DROP_TIME, System.currentTimeMillis());
						control.setData(DROP_HANDLED, false);
					} else {
						board.controller.userMadeMove(dragSource.id, dragEnd.id);
						control.setData(DROP_HANDLED, true);
					}
					control.setData(DRAG_INITIATOR, null);
					control.setData(CLICK_INITIATOR, null);
					control.setData(LAST_DROP_TIME, System.currentTimeMillis());
					break;
				}
				}
			}
		};
	}
	protected Square id;
	protected boolean isHidingPiece;
	protected boolean isLight;
	protected boolean isDirty;

	static Listener mouseWheelListener;
	static {
		mouseWheelListener = new Listener() {
			protected long lastWheel;
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseWheel:
					ChessSquare square = (ChessSquare) event.widget;
					long currentTime;
					if (square.getPreferences().getBoolean(PreferenceKeys.BOARD_TRAVERSE_WITH_MOUSE_WHEEL) &&
						(currentTime = System.currentTimeMillis()) - lastWheel >= 100) {
						square.board.getController().userMouseWheeled(event.count);
						lastWheel = currentTime;
					}
					break;
				}
			}
		};
	}

	static MouseListener mouseListener;
	static {
		mouseListener = new MouseListener() {
			public void mouseDoubleClick(MouseEvent e) {
				if (e.button == 1) {
					ChessSquare square = (ChessSquare) e.widget;
					square.board.controller.userPressedMouseButton(
							MouseButtonAction.LeftDoubleClick, square.id);
				}
			}

			public void mouseDown(MouseEvent e) {
				ChessSquare square = (ChessSquare) e.widget;
				square.board.controller.userPressedMouseButton(
						MouseButtonAction.buttonFromEvent(e), square.id);
			}

			public void mouseUp(MouseEvent e) {
				ChessSquare square = (ChessSquare) e.widget;
				if (!square.getPreferences().getString(
						PreferenceKeys.BOARD_USER_MOVE_INPUT_MODE).equals(
						UserMoveInputMode.ClickClickMove.toString())) {
					return;
				}
				if (e.button == 1) {
					ChessBoard board = square.board;
					Composite control = board.getControl();
					Long lastDropTime = (Long) control.getData(LAST_DROP_TIME);
					long currentTime = System.currentTimeMillis();
					if (lastDropTime == null || currentTime - lastDropTime > 50) {
						ChessSquare initiator = (ChessSquare) control.getData(CLICK_INITIATOR);

						if (initiator == null) {// Start of move
							if (board.controller.canUserInitiateMoveFrom(square.id)) {
								control.setData(CLICK_INITIATOR, square);
								board.controller.userInitiatedMove(square.id);
							}
						} else {
							if (square == initiator) {// Clicked on same square twice.
								board.controller.userCancelledMove(initiator.id);
								control.setData(CLICK_INITIATOR, null);
								control.setData(LAST_DROP_TIME, currentTime);
							} else {// A valid move
								board.controller.userMadeMove(initiator.id, square.id);
								control.setData(CLICK_INITIATOR, null);
								control.setData(LAST_DROP_TIME, currentTime);
							}
						}
					}
				}
			}
		};
	}

	static PaintListener paintListener = new PaintListener() {
		public void paintControl(PaintEvent e) {
			ChessSquare square = (ChessSquare) e.getSource();
			long startTime = LOG.isDebugEnabled() ? System.currentTimeMillis() : 0;

			Point size = square.getSize();
			if (square.isUsingSolidBackgroundColors()) {
				e.gc.setBackground(square.getSolidBackgroundColor());
				e.gc.fillRectangle(0, 0, size.x, size.y);
			} else {
				Image backgroundImage = square.getBackgrondImage(square.isLight, size.x, size.y);
				if (backgroundImage != null) {
					e.gc.drawImage(backgroundImage, 0, 0);
				}
			}

			int imageSize = square.getImageSize();
			if (square.pieceImage == null && square.piece != Piece.EMPTY) {
				square.pieceImage = square.getChessPieceImage(square.piece, imageSize);
			}
			if (square.pieceImage != null) {
				int pieceImageX = (size.x - imageSize) / 2;
				int pieceImageY = (size.y - imageSize) / 2;
				if (square.isHidingPiece) {
					switch (square.getHidingAlpha()) {
					case 0:
						break;
					case 255:
						e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);
						break;
					default: // PERF: Consumes gigabytes of memory
						e.gc.setAlpha(square.getHidingAlpha());
						e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);
						e.gc.setAlpha(255);
					}
				} else {
					e.gc.drawImage(square.pieceImage, pieceImageX, pieceImageY);
				}
			}

			String fileLabel = square.getFileLabel();
			if (fileLabel != null) {
				RaptorPreferenceStore preferences = square.getPreferences();
				e.gc.setForeground(
						preferences.getColor(BOARD_COORDINATES_COLOR));
				e.gc.setFont(SWTUtils.getProportionalFont(
						preferences.getFont(BOARD_COORDINATES_FONT),
						square.getCoordinatesSizePercentage(), size.y));

				// Doesn't include the stem on the g-file
//				int fontHeight = e.gc.getFontMetrics().getAscent()
//						+ e.gc.getFontMetrics().getDescent();
				int fontHeight = e.gc.getFontMetrics().getHeight();

				e.gc.drawString(fileLabel, size.x
						- e.gc.getFontMetrics().getAverageCharWidth() - 2,
						size.y - fontHeight, true);
			}

			String rankLabel = square.getRankLabel();
			if (rankLabel != null) {
				RaptorPreferenceStore preferences = square.getPreferences();
				e.gc.setForeground(
						preferences.getColor(BOARD_COORDINATES_COLOR));
				e.gc.setFont(SWTUtils.getProportionalFont(
						preferences.getFont(BOARD_COORDINATES_FONT),
						square.getCoordinatesSizePercentage(), size.y));

				e.gc.drawString(rankLabel, 0, 0, true);
			}

			if (LOG.isDebugEnabled()) {
				LOG.debug("Drew chess square: " + square.getId() + " in "
						+ (System.currentTimeMillis() - startTime));
			}
		}
	};
	protected Piece piece;
	protected Image pieceImage;

	/**
	 * Creates a ChessSquare tied to the specified board.
	 * 
	 * @param id
	 *		The square id. An integer representing the squares index in
	 *		GameConstants (e.g.
	 *		GameConstants.SQUARE_A1,GameConstants.SQUARE_A2, etc).
	 *		Drop/Piece Jail squares should use the constants
	 *		(GameConstants
	 *		.BP_DROP_FROM_SQUARE,GameConstants.BN_DROP_FROM_SQUARE,etc).
	 * @param isLight
	 *		True if this is a square with a light background, false if its
	 *		a square with a dark background.
	 */
	public ChessSquare(Composite parent, ChessBoard chessBoard, Square id,
			boolean isLight) {
		super(parent, SWT.DOUBLE_BUFFERED | SWT.NO_BACKGROUND);
		board = chessBoard;
		this.id = id;
		this.isLight = isLight;
		addPaintListener(paintListener);
		addControlListener(controlListener);
		addMouseListener(mouseListener);
		addListener(SWT.MouseWheel, mouseWheelListener);
		addListener(SWT.MouseDown, dndListener);
		addListener(SWT.MouseUp, dndListener);

	}

	/**
	 * Creates a ChessSquare not tied to a board. Useful in preferences. Use
	 * with care, this does'nt add any listeners besides the PaointListener and
	 * board will be null.
	 */
	public ChessSquare(Composite parent, Square id, boolean isLight) {
		super(parent, SWT.DOUBLE_BUFFERED);
		this.id = id;
		this.isLight = isLight;
		addPaintListener(paintListener);
	}

	/**
	 * Clears any cached images this square is maintaining. Useful when swapping
	 * out chess sets or square backgrounds.
	 */
	public void clearCache() {
		pieceImage = null;
	}

	/**
	 * An integer representing the squares index in GameConstants (e.g.
	 * GameConstants.SQUARE_A1,GameConstants.SQUARE_A2, etc). Drop/Piece Jail
	 * squares should use the constants (GameConstants
	 * .BP_DROP_FROM_SQUARE,GameConstants.BN_DROP_FROM_SQUARE,etc).
	 */
	public Square getId() {
		return id;
	}

	/**
	 * An integer representing the colored index type in GameConstants. (e.g.
	 * GameConstants.WP,GameConstants.WN,GameConstants.WQ,GameConstants.EMPTY,
	 * etc).
	 */
	public Piece getPiece() {
		return piece;
	}

	/**
	 * Returns true if this square is hiding its index, otherwise false. This is
	 * useful during drag operations when the board is refreshed.
	 */
	public boolean isHidingPiece() {
		return isHidingPiece;
	}

	/**
	 * Returns true if this square has a light background, false otherwise.
	 * 
	 * @return
	 */
	public boolean isLight() {
		return isLight;
	}

	/**
	 * Sets whether or not the index is being hidden. This is useful during dnd
	 * operations when you want to prevent board refreshes.
	 * 
	 * @param isHidingPiece
	 *		True if the index should be hidden, false otherwise.
	 */
	public void setHidingPiece(boolean isHidingPiece) {
		if (this.isHidingPiece != isHidingPiece) {
			this.isHidingPiece = isHidingPiece;
			isDirty = true;
		}
	}

	/**
	 * Sets the colored chess index. This method does not redraw after the index
 is set. It is up to the caller to invoke that.
	 * 
	 * @param piece
	 *		An integer representing the colored index type in
		GameConstants. (e.g.
		GameConstants.WP,GameConstants.WN,GameConstants
		.WQ,GameConstants.EMPTY, etc).
	 */
	public void setPiece(Piece piece) {
		if (this.piece != piece) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Setting piece in square " + id + " " + piece);
			}
			this.piece = piece;
			pieceImage = null;
			isDirty = true;
		}
	}

	/**
	 * Returns true if solid background colors should be used, false if a
	 * background image should be used.
	 */
	protected boolean isUsingSolidBackgroundColors() {
		return getPreferences().getBoolean(
				BOARD_IS_USING_SOLID_BACKGROUND_COLORS);
	}

	/**
	 * Returns the solid background color to use for this square.
	 */
	protected Color getSolidBackgroundColor() {
		return getPreferences().getColor(
				isLight ? BOARD_LIGHT_SQUARE_SOLID_BACKGROUND_COLOR
						: BOARD_DARK_SQUARE_SOLID_BACKGROUND_COLOR);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected Image getBackgrondImage(boolean isLight, int width, int height) {
		return ChessBoardUtils.getSquareBackgroundImage(isLight, id, width,
				height);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected Image getChessPieceImage(Piece piece, int size) {
		return ChessBoardUtils.getChessPieceImage(piece, size);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected short getCoordinatesSizePercentage() {
		return (short)Raptor.getInstance().getPreferences()
				.getInt(BOARD_COORDINATES_SIZE_PERCENTAGE);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected String getFileLabel() {
		if (isShowingCoordinates() && !ChessBoardUtils.isPieceJailSquare(id)) {
			if (board.isWhiteOnTop) {
				if ((GameUtils.getBitboard(id) & GameConstants.RANK8) != 0) {
					return String.valueOf(GameConstants.FILE_FROM_SAN.charAt(id.file));
				}
			} else {
				if ((GameUtils.getBitboard(id) & GameConstants.RANK1) != 0) {
					return String.valueOf(GameConstants.FILE_FROM_SAN.charAt(id.file));
				}
			}
		}
		return null;
	}

	protected int getHidingAlpha() {
		return getPreferences().getInt(BOARD_PIECE_SHADOW_ALPHA);
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected int getImageSize() {
		double imageSquareSideAdjustment = getPreferences().getDouble(
				BOARD_PIECE_SIZE_ADJUSTMENT);

		int imageSize = (int) (getSize().x * (1.0 - imageSquareSideAdjustment));
		if (imageSize % 2 != 0) {
			imageSize -= 1;
		}

		return imageSize;
	}

	/**
	 * Provided so it can easily be overridden.By default returns the preference
	 * store in Raptor.getInstance.
	 */
	protected RaptorPreferenceStore getPreferences() {
		return Raptor.getInstance().getPreferences();
	}

	protected String getRankLabel() {
		if (Raptor.getInstance().getPreferences()
				.getBoolean(BOARD_IS_SHOW_COORDINATES)
				&& !ChessBoardUtils.isPieceJailSquare(id)) {
			if (board.isWhiteOnTop) {
				if ((GameUtils.getBitboard(id) & GameConstants.HFILE) != 0) {
					return String.valueOf(GameConstants.RANK_FROM_SAN.charAt(id.rank));
				}
			} else {
				if ((GameUtils.getBitboard(id) & GameConstants.AFILE) != 0) {
					return String.valueOf(GameConstants.RANK_FROM_SAN.charAt(id.rank));
				}
			}
		}
		return null;
	}

	/**
	 * Returns the square the cursor is at if its ChessBoard is equal to this
	 * ChessSquares ChessBoard. Otherwise returns null.
	 */
	protected ChessSquare getSquareCursorIsAt() {
		Control control = getDisplay().getCursorControl();

		while (control != null && !(control instanceof ChessSquare)) {
			control = control.getParent();
		}

		ChessSquare result = null;

		if (control instanceof ChessSquare) {
			result = (ChessSquare) control;
			if (result.board != board) {
				result = null;
			}
		}
		return result;
	}

	/**
	 * Provided so it can easily be overridden.By default uses the
	 * RaptorPreferenceStore setting.
	 */
	protected boolean isShowingCoordinates() {
		return Raptor.getInstance().getPreferences()
				.getBoolean(BOARD_IS_SHOW_COORDINATES);
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
		} else {
			int imageSize = getImageSize();
			getShell().setCursor(
					ChessBoardUtils.getCursorForPiece(piece, imageSize));
		}
	}

	/**
	 * Updates the cursor after a drop is finished.
	 */
	protected void updateCursorForDragEnd() {
		getShell().setCursor(
				Raptor.getInstance().getCursorRegistry().getDefaultCursor());
	}

	public ChessBoard getChessBoard() {
		return board;
	}

	public boolean isDirty() {
		return isDirty;
	}

	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}

	public void redraw() {
		isDirty = false;
		super.redraw();
	}
}
