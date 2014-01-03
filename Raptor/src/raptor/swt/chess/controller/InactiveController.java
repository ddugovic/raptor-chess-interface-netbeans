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
package raptor.swt.chess.controller;

import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.GameCursor;
import raptor.chess.Move;
import raptor.chess.MoveList;
import raptor.chess.Variant;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.TimeTakenForMove;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.swt.SWTUtils;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.ChessSquare;
import raptor.swt.chess.MouseButtonAction;
import raptor.swt.chess.movelist.TextAreaMoveList;
import raptor.util.Logger;
import raptor.util.RaptorStringUtils;

/**
 * This controller is used when a game is no longer active. It allows the user
 * to play around with the position and traverser the move list. However it is
 * not backed by a connector, so the users actions do not do anything to a
 * connector.
 */
public class InactiveController extends ChessBoardController {
	static final Logger LOG = Logger.getLogger(ChessBoardController.class);
	protected GameCursor cursor;
	protected String title;
	protected ToolBar toolbar;
	protected boolean userMadeAdjustment = false;
	protected boolean canBeTakenOver;
	private boolean variationMode;

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * isReusable will be set to the users preference
	 * BOARD_TAKEOVER_INACTIVE_GAMES
	 */
	public InactiveController(Game game) {
		this(game, "Inactive", Raptor.getInstance().getPreferences()
				.getBoolean(PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES));
	}

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * isReusable will be set to the users preference
	 * BOARD_TAKEOVER_INACTIVE_GAMES
	 */
	public InactiveController(Game game, Connector connector) {
		super(new GameCursor(game, GameCursor.Mode.MakeMovesOnCursor),
				connector);
		cursor = (GameCursor) getGame();
		title = "Inactive";
		canBeTakenOver = Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES);
	}

	/**
	 * You can set the PgnHeader WhiteOnTop to toggle if white should be
	 * displayed on top or not.
	 * 
	 * @param canBeTakenOver
	 *            Means if another game needs to be displayed, it can take over
	 *            this game and remove it from being displayed. This may not
	 *            always be desirable for instance when viewing a PGN game.
	 */
	public InactiveController(Game game, String title, boolean canBeTakenOver) {
		super(new GameCursor(game, GameCursor.Mode.MakeMovesOnCursor));
		cursor = (GameCursor) getGame();
		this.title = title;
		this.canBeTakenOver = canBeTakenOver;
	}

	public void adjustForIllegalMove(String move) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("adjustForIllegalMove ");
		}

		refresh();
		onPlayIllegalMoveSound();
		board.getStatusLabel().setText("Illegal Move: " + move);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isDisposed()) {
			board.getGameDescriptionLabel().setText(
					title == null ? "Inactive "
							+ getGame().getHeader(PgnHeader.Event) : title);
		}
	}

	/**
	 * Adjusts the game status label. If the game is not in an active state, the
	 * status label sets itself to getResultDescription in the game. If the game
	 * is in an active state, the status is set to the last move.
	 * 
	 * This method is provided so it can easily be overridden.
	 */
	@Override
	public void adjustGameStatusLabel() {
		if (userMadeAdjustment || variationMode) {
			if (getGame().getMoveList().getSize() > 0) {
				Move lastMove = getGame().getMoveList().getLast();
				int moveNumber = getGame().getFullMoveCount();

				board.getStatusLabel().setText(
					L10n.getInstance().getString("chessBCont0")
						+ moveNumber
						+ (lastMove.isWhitesMove() ? ". " : "... ")
						+ GameUtils.convertSanToUseUnicode(lastMove.toString(),
							!getGame().isWhitesMove()));
			} else {
				board.getStatusLabel().setText("");
			}
		} else {
			String result = getGame().getHeader(PgnHeader.ResultDescription);
			if (result != null) {
				board.getStatusLabel().setText(result);
			}
		}
	}

	/**
	 * Inactive games can be taken over by other games that need to be displayed
	 * for efficiency. This is an optimization feature. See the constructors for
	 * more information on how this is set.
	 */
	public boolean canBeTakenOver() {
		return canBeTakenOver;
	}

	@Override
	public boolean canUserInitiateMoveFrom(Square squareId) {		
		if (!isDisposed()) {			
			if (ChessBoardUtils.isPieceJailSquare(squareId)) {
				if (getGame().isInState(Game.DROPPABLE_STATE)) {
					Piece pieceType = ChessBoardUtils
							.pieceJailSquareToPiece(squareId);
					return getGame().isWhitesMove()
							&& ChessBoardUtils.isWhitePiece(pieceType)
							|| !getGame().isWhitesMove()
							&& ChessBoardUtils.isBlackPiece(pieceType);
				}
			} else if (getGame().getPiece(squareId) == Piece.EMPTY) {
				return false;
			} else {
				return getGame().isWhitesMove()
						&& ChessBoardUtils.isWhitePiece(board.getSquare(
								squareId).getPiece())
						|| !getGame().isWhitesMove()
						&& ChessBoardUtils.isBlackPiece(board.getSquare(
								squareId).getPiece());
			}
		}
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (toolbar != null && !toolbar.isDisposed()) {
			toolbar.setVisible(false);
			SWTUtils.clearToolbar(toolbar);
			toolbar = null;
		}
	}

	public void enableDisableNavButtons() {
		setToolItemEnabled(ToolBarItemKey.NEXT_NAV, getCursor().hasNext());
		setToolItemEnabled(ToolBarItemKey.BACK_NAV, getCursor().hasPrevious());
		setToolItemEnabled(ToolBarItemKey.FIRST_NAV, getCursor().hasFirst());
		setToolItemEnabled(ToolBarItemKey.LAST_NAV, getCursor().hasLast());
		setToolItemEnabled(ToolBarItemKey.REVERT_NAV, getCursor().canRevert());
		setToolItemEnabled(ToolBarItemKey.COMMIT_NAV, getCursor().canCommit());
	}

	@Override
	public String getTitle() {
		return title == null ? "Inactive" : title;
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);

		if (toolbar == null) {
			toolbar = SWTUtils.createToolbar(isCoolbarMode ? getBoard().getCoolbar()
					: parent);
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.InactiveChessBoard, toolbar, true);

			if (getGame().getVariant() == Variant.suicide) {
				setToolItemSelected(ToolBarItemKey.AUTO_KING, true);
			} else {
				setToolItemSelected(ToolBarItemKey.AUTO_QUEEN, true);
			}
			enableDisableNavButtons();

			if (isCoolbarMode) {
				ChessBoardUtils.adjustCoolbar(getBoard(), toolbar);
			}
		} else {
			if (!isCoolbarMode) {
				toolbar.setParent(parent);
			}
		}

		if (isCoolbarMode) {
			return null;
		} else {
			return toolbar;
		}
	}

	@Override
	public void init() {
		board.getArrowDecorator().removeAllArrows();
		board.getSquareHighlighter().removeAllHighlights();

		board.setWhiteOnTop(RaptorStringUtils.getBooleanValue(game
				.getHeader(PgnHeader.WhiteOnTop)));

		if (getGame().isInState(Game.DROPPABLE_STATE)) {
			board.setWhitePieceJailOnTop(board.isWhiteOnTop());
		} else {
			board.setWhitePieceJailOnTop(!board.isWhiteOnTop());
		}
		board.getMoveList().updateToGame();
		getCursor().setCursorMasterLast();

		if (getPreferences().getBoolean(PreferenceKeys.BOARD_COOLBAR_MODE)) {
			getToolbar(null);
		}

		refresh();
		fireItemChanged();
	}

	@Override
	public void onBack() {
		if (board.getMoveList() instanceof TextAreaMoveList) {
			((TextAreaMoveList)board.getMoveList()).arrangeForVarMode(
					getCursor().getCursorPosition()-1);
		}
		
		board.getResultDecorator().setDecoration(null);
		getCursor().setCursorPrevious();
		refresh();
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onCommit() {
		getCursor().commit();
		refresh();
	}

	@Override
	public void onFirst() {
		board.getResultDecorator().setDecoration(null);
		getCursor().setCursorFirst();
		refresh();
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onForward() {
		board.getResultDecorator().setDecoration(null);
		getCursor().setCursorNext();
		refresh();
		addDecorationsForLastMoveListMove();
	}

	@Override
	public void onLast() {
		board.getResultDecorator().setDecoration(null);
		getCursor().setCursorLast();
		refresh();
		addDecorationsForLastMoveListMove();
	}

	/**
	 * In droppable games this shows a menu of the pieces available for
	 * dropping. In bughouse the menu includes the premove drop features which
	 * drops a move when the piece becomes available.
	 */
	public void onPopupMenu(final Square square) {
		if (isDisposed()) {
			return;
		}

		if (!ChessBoardUtils.isPieceJailSquare(square)
				&& getGame().isInState(Game.DROPPABLE_STATE)
				&& getGame().getPiece(square) == Piece.EMPTY) {
			final PieceColor color = getGame().getColorToMove();
			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			if (getGame().getDropCount(color, PieceType.PAWN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PieceType.PAWN, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PieceType.PAWN, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, PieceType.KNIGHT) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PieceType.KNIGHT, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PieceType.KNIGHT, color)),
								square);
					}
				});
			}
			if (getGame().getDropCount(color, PieceType.BISHOP) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PieceType.BISHOP, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PieceType.BISHOP, color)),
								square);
					}
				});
			}
			if (getGame().getDropCount(color, PieceType.ROOK) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PieceType.ROOK, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PieceType.ROOK, color)), square);
					}
				});
			}
			if (getGame().getDropCount(color, PieceType.QUEEN) > 0) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText(GameUtils.getPieceRepresentation(GameUtils
						.getColoredPiece(PieceType.QUEEN, color))
						+ "@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						userMadeMove(GameUtils
								.getDropSquareFromColoredPiece(GameUtils
										.getColoredPiece(PieceType.QUEEN, color)), square);
					}
				});
			}

			menu.setLocation(board.getSquare(square).toDisplay(10, 10));
			menu.setVisible(true);
			while (!menu.isDisposed() && menu.isVisible()) {
				if (!board.getControl().getDisplay().readAndDispatch()) {
					board.getControl().getDisplay().sleep();
				}
			}
			menu.dispose();
		}
	}

	@Override
	public void onRevert() {
		getCursor().revert();
		refresh();
		addDecorationsForLastMoveListMove();
	}

	public void onSave() {
		FileDialog fd = new FileDialog(board.getControl().getShell(), SWT.SAVE);
		fd.setText("Save To PGN");
		fd.setFilterPath("");
		String[] filterExt = { "*.pgn", "*.*" };
		fd.setFilterExtensions(filterExt);
		final String selected = fd.open();

		if (selected != null) {
			String pgn = getCursor().toPgn();
			FileWriter writer = null;

			try {
				writer = new FileWriter(selected);
				writer.write(pgn);
				writer.flush();
			} catch (IOException ioe) {
				Raptor.getInstance().onError("Error saving pgn file.", ioe);
			} finally {
				try {
					writer.close();
				} catch (Throwable t) {
				}
			}
		}
	}

	@Override
	public void refresh() {
		if (isDisposed()) {
			return;
		}
		board.getEngineAnalysisWidget().updateToGame();	
		
		//if (!variationMode)
		board.getMoveList().select(getCursor().getCursorPosition());			
		
		board.getMoveList().updateToGame();
		enableDisableNavButtons();
		super.refresh();		
	}

	/**
	 * Sets the toolbar. Useful when controllers are swapping out.
	 * 
	 * @param toolbar
	 */
	public void setToolbar(ToolBar toolbar) {
		this.toolbar = toolbar;
	}

	@Override
	public void userCancelledMove(Square fromSquare) {
		if (!isDisposed()) {
			LOG.debug("moveCancelled" + getGame().getId() + " " + fromSquare);
			board.unhidePieces();
			refresh();
			onPlayIllegalMoveSound();
		}
	}

	@Override
	public void userInitiatedMove(Square square) {
		if (!isDisposed()) {
			LOG.debug("moveInitiated" + getGame().getId() + " " + square + " ");
			userMadeAdjustment = true;
			board.getResultDecorator().setDecoration(null);
			board.getSquare(square).setHidingPiece(true);
			board.getSquare(square).redraw();
		}
	}
	
	@Override
	public Game getGame() {
		return variationMode && board.getMoveList() instanceof TextAreaMoveList ?
				((TextAreaMoveList)board.getMoveList()).getCurrVariation().getVarGame()
				: game;
	}
	
	public Game getGame(boolean allowVarGame) {
		return (variationMode && allowVarGame) && board.getMoveList() instanceof TextAreaMoveList ?
				((TextAreaMoveList)board.getMoveList()).getCurrVariation().getVarGame()
				: game;
	}

	@Override
	public void userMadeMove(Square fromSquare, Square toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("userMadeMove " + getGame().getId() + " "
					+ GameUtils.getSan(fromSquare) + " "
					+ GameUtils.getSan(toSquare));
		}
		board.unhidePieces();
		removeAllMoveDecorations();

		if (fromSquare == toSquare
				|| ChessBoardUtils.isPieceJailSquare(toSquare)) {
			if (LOG.isDebugEnabled()) {
				LOG
						.debug("User tried to make a move where from square == to square or toSquar was the piece jail.");
			}
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("Processing user move..");
		}
		
		Move move = null;
		if (GameUtils.isPromotion(getGame(), fromSquare, toSquare)) {
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare,
					getAutoPromoteSelection());
		} else if (ChessBoardUtils.isPieceJailSquare(fromSquare)) {
			move = ChessBoardUtils.createDropMove(getGame(), fromSquare, toSquare);
		} else {
			move = ChessBoardUtils.createMove(getGame(), fromSquare, toSquare);
		}

		if (move == null) {
			adjustForIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare, toSquare));
		} else {
			addDecorationsForMove(move, true);
			boolean moveMade = getGame().move(move);
			if (variationMode && board.getMoveList() instanceof TextAreaMoveList) {
				((TextAreaMoveList)board.getMoveList()).variationMove(move);
			}
			
			if (moveMade) {
				refresh();
				onPlayMoveSound();
			} else {
				Raptor.getInstance().onError(
						"Game.move returned false for a move that should have been legal.Move: "
								+ move + ".",
						new Throwable(getGame().toString()));
				adjustForIllegalMove(move.toString());
			}
		}		
	}

	@Override
	public void userMouseWheeled(int count) {
		if (count < 0) {
			onForward();
		} else {
			onBack();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void userPressedMouseButton(MouseButtonAction button, Square square) {
		InactiveMouseAction action = InactiveMouseAction
				.valueOf(getPreferences().getString(
						INACTIVE_CONTROLLER + button.getPreferenceSuffix()));

		if (action == null) {
			Raptor.getInstance().onError(
					"INACTIVE_CONTROLLER was null. This should never happn. "
							+ button.toString() + " " + square);
			return;
		}

		switch (action) {
		case None:
			break;
		case PopupMenu:
			onPopupMenu(square);
			break;
		case Rematch:
			if (connector != null) {
				connector.onRematch();
			}
			break;
		}
	}

	/**
	 * Invoked when the move list is clicked on. THe halfMoveNumber is the move
	 * selected.
	 * 
	 * The default implementation does nothing. It can be overridden to provide
	 * functionality.
	 */
	@Override
	public void userSelectedMoveListMove(int halfMoveNumber) {		
		board.getResultDecorator().setDecoration(null);
		
		getCursor().setCursor(halfMoveNumber);
		
		enableDisableNavButtons();
		refresh();
		addDecorationsForLastMoveListMove();
	}
	
	public void gotoMove(int halfMoveNumber) {
		board.getResultDecorator().setDecoration(null);
		getCursor().setCursor(halfMoveNumber);
		enableDisableNavButtons();
		refresh();
		addDecorationsForLastMoveListMove(false);
	}
	
	/**
	 * Over-ridden to provide support for viewing pgn files.
	 */
	@Override
	protected long[] getRemainingTimes() {
		long whiteTime = 0;
		long blackTime = 0;

		int inc = 0;

		if (getCursor().getCursorPosition() == getCursor().getMasterGameLength()
				&& StringUtils.isNotBlank(getGame().getHeader(
						PgnHeader.WhiteRemainingMillis))
				&& StringUtils.isNotBlank(getGame().getHeader(
						PgnHeader.BlackRemainingMillis))) {

			whiteTime = Long.parseLong(getGame().getHeader(
					PgnHeader.WhiteRemainingMillis));
			blackTime = Long.parseLong(getGame().getHeader(
					PgnHeader.BlackRemainingMillis));
		} else {
			String timeControl = getGame().getHeader(PgnHeader.TimeControl);
			if (StringUtils.isNotBlank(timeControl)) {
				StringTokenizer tok = new StringTokenizer(
						timeControl, "+ ");
				try {
					whiteTime = blackTime = Integer.parseInt(tok.nextToken()) * 1000;

					if (whiteTime == 0) {
						whiteTime = blackTime = 10000;
					}
					if (tok.hasMoreTokens()) {
						inc = Integer.parseInt(tok.nextToken()) * 1000;
					}
				} catch (NumberFormatException nfe) {
					LOG
							.error(
									"Error obtaining initial clock times. This could hvae been because of an invalid TimeControl pgn header.",
									nfe);
				}

				MoveList moveList = getCursor().getCursorGame().getMoveList();
				for (int i = 0; i < moveList.getSize(); i++) {
					Move move = moveList.get(i);
					if (move.isWhitesMove()) {
						TimeTakenForMove[] annotations = move
								.getTimeTakenForMove();
						if (annotations != null && annotations.length != 0) {
							whiteTime -= annotations[0].getMilliseconds();
							if (inc != 0) {
								whiteTime += inc;
							}
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("No time found for move " + move);
							}
						}
					} else {
						TimeTakenForMove[] annotations = move
								.getTimeTakenForMove();
						if (annotations != null && annotations.length != 0) {
							blackTime -= annotations[0].getMilliseconds();
							if (inc != 0) {
								blackTime += inc;
							}
						} else {
							if (LOG.isDebugEnabled()) {
								LOG.debug("No time found for move " + move);
							}
						}
					}
				}
			}
		}

		return new long[] { whiteTime, blackTime };
	}

	protected void onPlayIllegalMoveSound() {
		SoundService.getInstance().playSound("illegalMove");
	}
	
	@Override
	protected void adjustBoard() {
		if (isDisposed()) {
			return;
		}
		for (ChessSquare[] rank : board.getSquares()) {
			for (ChessSquare boardSquare : rank) {
				Square square = boardSquare.getId();
				boardSquare.setPiece(getGame().getPiece(square));
			}
		}
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	public void setVariationMode(boolean variationMode) {
		if (board.getMoveList() instanceof TextAreaMoveList)
			((TextAreaMoveList)board.getMoveList()).setVariationMode(variationMode);
		this.variationMode = variationMode;
	}

	public GameCursor getCursor() {
		return variationMode && board.getMoveList() instanceof TextAreaMoveList ? 
				((GameCursor)((TextAreaMoveList)board.getMoveList()).getCurrVariation().getVarGame())
				: cursor;
	}
}
