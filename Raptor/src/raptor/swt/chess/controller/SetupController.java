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

import raptor.util.Logger;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;

import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.util.GameUtils;
import raptor.connector.Connector;
import raptor.pref.PreferenceKeys;
import raptor.service.SoundService;
import raptor.service.GameService.GameServiceAdapter;
import raptor.service.GameService.GameServiceListener;
import raptor.swt.SWTUtils;
import raptor.swt.chess.ChessBoardUtils;
import raptor.swt.chess.MouseButtonAction;
import raptor.swt.chess.PieceJailChessSquare;
import raptor.util.RaptorRunnable;

/**
 * A controller used when setting up a position. When the controller receives a
 * setupGameBecameExamined call from the GameService on the backing controller,
 * it changes the controller over to an examine controller.
 * 
 * All moves and adjustments made by this controller are sent directly to the
 * backing connector
 */
public class SetupController extends ChessBoardController {
	static final Logger LOG = Logger.getLogger(SetupController.class);

	protected GameServiceListener listener = new GameServiceAdapter() {

		@Override
		public void gameInactive(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								onPlayGameEndSound();
								InactiveController inactiveController = new InactiveController(
										game,getConnector());
								getBoard().setController(inactiveController);

								inactiveController.setBoard(board);
								inactiveController
										.setItemChangedListeners(itemChangedListeners);

								// Set the listeners to null so they wont
								// get
								// cleared and disposed
								setItemChangedListeners(null);

								// board.clearCoolbar();
								connector.getGameService()
										.removeGameServiceListener(listener);

								// Clear the cool bar and init the inactive
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								inactiveController.init();

								unexamineOnDispose = false;
								SetupController.this.dispose();
							}
						});
			}
		}

		@Override
		public void gameStateChanged(final Game game, final boolean isNewMove) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								setGame(game);
								setupPositionUpdated();
							}
						});
			}
		}

		@Override
		public void illegalMove(Game game, final String move) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}
								setupOnIllegalMove(move);
							}
						});
			}
		}

		@Override
		public void setupGameBecameExamined(final Game game) {
			if (!isDisposed() && game.getId().equals(getGame().getId())) {
				board.getControl().getDisplay().asyncExec(
						new RaptorRunnable(getConnector()) {
							@Override
							public void execute() {
								if (isDisposed()) {
									return;
								}

								ExamineController examineController = new ExamineController(
										game, connector);
								getBoard().setController(examineController);
								examineController
										.setItemChangedListeners(itemChangedListeners);
								examineController.setBoard(board);
								board.setWhitePieceJailOnTop(true);
								connector.getGameService()
										.removeGameServiceListener(listener);

								// Set the listeners to null so they wont get
								// cleared and disposed
								setItemChangedListeners(null);

								// Clear the cool bar and init the
								// examineController
								// controller.
								ChessBoardUtils.clearCoolbar(getBoard());
								examineController.init();

								unexamineOnDispose = false;
								SetupController.this.dispose();
							}
						});
			}
		}
	};
	protected ToolBar toolbar;
	protected boolean unexamineOnDispose = true;
	protected boolean isWhiteOnTopInit;

	public SetupController(Game game, boolean isWhiteOnTop, Connector connector) {
		super(game, connector);
		isWhiteOnTopInit = isWhiteOnTop;
	}

	public SetupController(Game game, Connector connector) {
		this(game, false, connector);
	}

	@Override
	public void adjustGameDescriptionLabel() {
		if (!isDisposed()) {
			board.getGameDescriptionLabel().setText(
					"Setting up a chess position");
		}
	}

	@Override
	public boolean canUserInitiateMoveFrom(Square squareId) {
		return true;
	}

	@Override
	public void dispose() {
		try {
			connector.getGameService().removeGameServiceListener(listener);
			if (unexamineOnDispose && getGame().isInState(Game.ACTIVE_STATE)) {
				connector.onUnexamine(getGame());
			}

			if (toolbar != null && !toolbar.isDisposed()) {
				toolbar.setVisible(false);
				SWTUtils.clearToolbar(toolbar);
				toolbar = null;
			}
			super.dispose();
		} catch (Throwable t) {
		}// Eat it its prob a disposed exception
	}

	@Override
	public Connector getConnector() {
		return connector;
	}

	@Override
	public String getTitle() {
		return "Setup " + getGame().getId();
				/*connector.getShortName() + "(Setup " + getGame().getId() + ")";*/
	}

	@Override
	public Control getToolbar(Composite parent) {
		boolean isCoolbarMode = getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE);
		if (toolbar == null) {
			toolbar = SWTUtils.createToolbar(isCoolbarMode ? getBoard().getCoolbar()
					: parent);
			ChessBoardUtils.addActionsToToolbar(this,
					RaptorActionContainer.SetupChessBoard, toolbar, true);
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

		board.setWhiteOnTop(isWhiteOnTopInit);
		board.setWhitePieceJailOnTop(isWhiteOnTopInit);

		if (getPreferences().getBoolean(PreferenceKeys.BOARD_COOLBAR_MODE)) {
			getToolbar(null);
		}

		board.getControl().layout(true, true);
		refresh();
		connector.getGameService().addGameServiceListener(listener);
		fireItemChanged();
	}

	public void setupOnIllegalMove(String move) {
		SoundService.getInstance().playSound("illegalMove");
		if (move != null) {
			board.getStatusLabel().setText("Illegal Move: " + move);
		}

		refreshBoard();
	}

	public void setupPositionUpdated() {
		if (LOG.isDebugEnabled()) {
			LOG.info("besetupPositionUpdated " + getGame().getId() + " ...");
		}
		long startTime = System.currentTimeMillis();

		refresh();
		onPlayMoveSound();

		if (LOG.isDebugEnabled()) {
			LOG.info("examinePositionUpdate in " + getGame().getId() + "  "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	@Override
	public void userCancelledMove(Square fromSquare) {
		removeAllMoveDecorations();
		refresh();
	}

	@Override
	public void userInitiatedMove(Square square) {
		if (!ChessBoardUtils.isPieceJailSquare(square)) {
			board.getSquare(square).setPiece(GameConstants.Piece.EMPTY);
		}
		board.redrawPiecesAndArtifacts();
	}

	@Override
	public void userMadeMove(Square fromSquare, Square toSquare) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Move made " + getGame().getId() + " " + fromSquare + " "
					+ toSquare);
		}

		if (fromSquare == toSquare
				|| ChessBoardUtils.isPieceJailSquare(toSquare)) {
			refresh();
			SoundService.getInstance().playSound("illegalMove");
			return;
		}

		Move move = null;
		if (ChessBoardUtils.isPieceJailSquare(fromSquare)) {
			move = ChessBoardUtils.createDropMove(getGame(), fromSquare, toSquare);
		} else {
			move = new Move(fromSquare, toSquare, getGame().getPiece(fromSquare),
					getGame().getColorToMove(), getGame().getPiece(toSquare));
		}
		// Always make the move first. It appears faster this way to the user.
		connector.makeMove(getGame(), move);

		if (move != null) {
			refreshForMove(move);
		} else {
			setupOnIllegalMove(GameUtils.getPseudoSan(getGame(), fromSquare,
					toSquare));
		}
	}

	@Override
	public void userMouseWheeled(int count) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void userPressedMouseButton(MouseButtonAction button, Square square) {

		switch (button) {
		case Right:
			onPopupMenu(square);
			break;
		}
	}

	@Override
	protected void adjustClockColors() {
		if (!isDisposed()) {
			if (getGame().getColorToMove() == PieceColor.WHITE) {
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			} else {
				board.getBlackClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_ACTIVE_CLOCK_COLOR));
				board.getWhiteClockLabel().setForeground(
						getPreferences().getColor(
								PreferenceKeys.BOARD_INACTIVE_CLOCK_COLOR));
			}
		}
	}

	/**
	 * Adjusts the contents of the index jail based on the game state.
	 */
	@Override
	protected void adjustPieceJail() {
		if (isDisposed()) {
			return;
		}

		for (int i = 0; i < DROPPABLE_PIECES.length; i++) {
			PieceJailChessSquare square = board.getPieceJailSquares()[DROPPABLE_PIECES[i].index];
			square.setPiece(DROPPABLE_PIECES[i]);
			square.setText("");
			square.redraw();
		}
	}

	protected void adjustToDropMove(Move move, boolean isRedrawing) {
		removeAllMoveDecorations();
		addDecorationsForMove(move, true);
		board.getSquare(move.getTo()).setPiece(move.getPiece());

		if (isRedrawing) {
			board.redrawPiecesAndArtifacts(false);
		}
	}

	protected void onPlayGameEndSound() {
		// SoundService.getInstance().playSound("obsGameEnd");
	}

	protected void onPlayGameStartSound() {
		// SoundService.getInstance().playSound("gameStart");
	}

	protected void onPlayMoveSound() {
		SoundService.getInstance().playSound("move");
	}

	/**
	 * Provides a menu the user can use to drop and clear indexs.
	 */
	protected void onPopupMenu(final Square square) {
		if (!ChessBoardUtils.isPieceJailSquare(square)) {

			Menu menu = new Menu(board.getControl().getShell(), SWT.POP_UP);

			if (getGame().getPiece(square) != Piece.EMPTY) {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("Clear " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						connector.onSetupClearSquare(getGame(), square);
					}
				});
			} else {
				MenuItem item = new MenuItem(menu, SWT.PUSH);
				item.setText("White P@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WP);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White N@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WN);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White B@ " + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WB);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White R@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WR);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White Q@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WQ);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("White K@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.WK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.SEPARATOR);

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black P@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BP);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black N@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BN);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black B@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BB);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black R@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BR);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black Q@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BQ);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
					}
				});

				item = new MenuItem(menu, SWT.PUSH);
				item.setText("Black K@" + GameUtils.getSan(square));
				item.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event e) {
						Move move = new Move(square, Piece.BK);
						adjustToDropMove(move, true);
						connector.makeMove(getGame(), move);
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
}
