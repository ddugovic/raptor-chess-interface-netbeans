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

import raptor.swt.chess.controller.ChessBoardController;
import raptor.swt.chess.layout.ChessBoardLayout;
import org.apache.commons.lang.StringUtils;
import raptor.util.Logger;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.Variant;
import raptor.pref.PreferenceKeys;
import raptor.pref.RaptorPreferenceStore;
import raptor.swt.RaptorLabel;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatUtils;
import raptor.swt.chess.layout.ChessBoardLayout.Field;
import raptor.swt.chess.analysis.UciAnalysisWidget;
import raptor.swt.chess.analysis.XboardAnalysisWidget;

/**
 * A GUI representation of a chess board, and all controls associated with it
 * (e.g. labels,index jail, etc).
 
 ChessBoards have a ChessBoardLayout which lays out their
 components.ChessBoards have a ChessBoardController which manages user
 adjustments and events from a controller. ChessBoards have a move list which
 can be shown or hidden. ChessBoards have a index jail which can be shown or
 hidden.
 */
public class ChessBoard implements GameConstants, PreferenceKeys {

	static final Logger LOG = Logger.getLogger(ChessBoard.class);

	// Using GridLayouts allows dynamic layout of the piece jail.
	protected RaptorLabel blackClockLabel;
	protected RaptorLabel blackLagLabel;
	protected RaptorLabel blackNameRatingLabel;
	protected Composite boardComposite;
	protected Composite gameComposite;
	protected ChessBoardLayout chessBoardLayout;
	protected ChessBoardController controller;
	protected RaptorLabel currentPremovesLabel;
	protected RaptorLabel gameDescriptionLabel;
	protected Composite whitePieceJailComposite;
	protected Composite blackPieceJailComposite;
	protected boolean isWhiteOnTop = false;
	protected boolean isWhitePieceJailOnTop = true;
	protected ChessBoardMoveList moveList;
	protected EngineAnalysisWidget engineAnalysisWidget;
	protected RaptorLabel openingDescriptionLabel;
	protected CoolBar coolbar;

	/**
	 * Piece jail is indexed by the colored index constants in Constants.
	 * The 0th index will always be null. (for the empty index).
	 */
	protected PieceJailChessSquare[] pieceJailSquares = new PieceJailChessSquare[13];

	IPropertyChangeListener propertyChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent arg) {
			if (arg.getProperty().startsWith("board")
					|| arg.getProperty().equals(APP_ZOOM_FACTOR)) {
				updateFromPrefs();
			}
		}
	};

	protected SquareHighlighter squareHighlighter;
	protected ResultDecorator resultDecorator;
	protected ArrowDecorator arrowDecorator;

	protected Composite componentComposite;
	protected SashForm boardMoveListSash;
	protected SashForm analysisSash;
	protected ChessSquare[][] squares = new ChessSquare[8][8];
	protected RaptorLabel statusLabel;
	protected RaptorLabel whiteClockLabel;
	protected RaptorLabel whiteLagLabel;
	protected RaptorLabel whiteNameRatingLabel;

	public ChessBoard() {
	}

	/**
	 * Creates the chess board with the specified parent.
	 */
	public Composite createControls(Composite parent) {
		synchronized (this) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Creating controls");
			}
			long startTime = System.currentTimeMillis();
			RaptorPreferenceStore preferences = Raptor.getInstance().getPreferences();

			componentComposite = new Composite(parent, SWT.DOUBLE_BUFFERED
					| SWT.NO_BACKGROUND);
			componentComposite.setLayout(SWTUtils.createMarginlessGridLayout(1,
					true));
			if (preferences.getBoolean(PreferenceKeys.BOARD_COOLBAR_ON_TOP)) {
				coolbar = new CoolBar(componentComposite, SWT.FLAT
						| SWT.HORIZONTAL);
				coolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
						false));
			}
			analysisSash = new SashForm(componentComposite, SWT.VERTICAL);
			analysisSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));

			if (!preferences.getBoolean(PreferenceKeys.BOARD_COOLBAR_ON_TOP)) {
				coolbar = new CoolBar(componentComposite, SWT.FLAT
						| SWT.HORIZONTAL);
				coolbar.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
						false));
			}

			boardMoveListSash = new SashForm(analysisSash, SWT.HORIZONTAL);
			gameComposite = new Composite(boardMoveListSash, SWT.NONE);
			adjustMoveList();

			createEngineAnalysisWidget();
			engineAnalysisWidget.create(analysisSash);
			analysisSash.setWeights(new int[] { 70, 30 });
			analysisSash.setMaximizedControl(boardMoveListSash);
			engineAnalysisWidget.getControl().setVisible(false);

			boardMoveListSash.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (propertyChangeListener != null) {
						Raptor.getInstance()
								.getPreferences()
								.removePropertyChangeListener(
										propertyChangeListener);
						propertyChangeListener = null;
					}
					if (engineAnalysisWidget != null) {
						engineAnalysisWidget.quit();
						engineAnalysisWidget = null;
					}
					if (controller != null) {
						controller.dispose();
						controller = null;
					}
					if (chessBoardLayout != null) {
						chessBoardLayout.dispose();
						chessBoardLayout = null;
					}
					if (arrowDecorator != null) {
						arrowDecorator.dispose();
						arrowDecorator = null;
					}
					if (squareHighlighter != null) {
						squareHighlighter.dispose();
						squareHighlighter = null;
					}
					if (resultDecorator != null) {
						resultDecorator.dispose();
						resultDecorator = null;
					}
					if (LOG.isInfoEnabled()) {
						LOG.info("Disposed ChessBoard");
					}
				}
			});

			createSquares();
			createPieceJailControls();

			whiteNameRatingLabel = new RaptorLabel(gameComposite, SWT.NONE);
			whiteNameRatingLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (SWTUtils.isRightClick(e)) {
						onNameLabelRightClick(e, whiteNameRatingLabel);
					}
				}
			});
			blackNameRatingLabel = new RaptorLabel(gameComposite, SWT.NONE);
			blackNameRatingLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (SWTUtils.isRightClick(e)) {
						onNameLabelRightClick(e, blackNameRatingLabel);
					}
				}
			});
			whiteClockLabel = new RaptorLabel(gameComposite, SWT.NONE);
			blackClockLabel = new RaptorLabel(gameComposite, SWT.NONE);
			whiteLagLabel = new RaptorLabel(gameComposite, SWT.NONE);
			blackLagLabel = new RaptorLabel(gameComposite, SWT.NONE);
			openingDescriptionLabel = new RaptorLabel(gameComposite, SWT.NONE);
			statusLabel = new RaptorLabel(gameComposite, SWT.NONE);
			gameDescriptionLabel = new RaptorLabel(gameComposite, SWT.NONE);
			currentPremovesLabel = new RaptorLabel(gameComposite, SWT.NONE);

			preferences
					.addPropertyChangeListener(propertyChangeListener);

			// order is important here.
			squareHighlighter = new SquareHighlighter(this);
			arrowDecorator = new ArrowDecorator(this);
			resultDecorator = new ResultDecorator(this);

			adjustChessBoardLayout();

			updateFromPrefs();

			if (LOG.isDebugEnabled()) {
				LOG.debug("Created controls in "
						+ (System.currentTimeMillis() - startTime));
			}
			return getControl();
		}
	}

	public void dispose() {
		boardMoveListSash.dispose();
	}

	public synchronized ArrowDecorator getArrowDecorator() {
		return arrowDecorator;
	}

	public synchronized RaptorLabel getBlackClockLabel() {
		return blackClockLabel;
	}

	public synchronized RaptorLabel getBlackLagLabel() {
		return blackLagLabel;
	}

	public synchronized RaptorLabel getBlackNameRatingLabel() {
		return blackNameRatingLabel;
	}

	/**
	 * Returns the panel containing the chess board. Should really be not be
	 * used by anything outside of the swt.chess package.
	 */
	public synchronized Composite getBoardComposite() {
		return boardComposite;
	}

	public synchronized Composite getGameComposite() {
		return gameComposite;
	}

	public synchronized Composite getWhitePieceJailComposite() {
		return whitePieceJailComposite;
	}

	public synchronized Composite getBlackPieceJailComposite() {
		return blackPieceJailComposite;
	}

	/**
	 * Returns the control representing this chess board.
	 */
	public synchronized Composite getControl() {
		return componentComposite;
	}

	public synchronized ChessBoardController getController() {
		return controller;
	}

	public CoolBar getCoolbar() {
		return coolbar;
	}

	public synchronized RaptorLabel getCurrentPremovesLabel() {
		return currentPremovesLabel;
	}

	public EngineAnalysisWidget getEngineAnalysisWidget() {
		return engineAnalysisWidget;
	}

	public synchronized RaptorLabel getGameDescriptionLabel() {
		return gameDescriptionLabel;
	}

	public synchronized ChessBoardMoveList getMoveList() {
		return moveList;
	}

	public synchronized RaptorLabel getOpeningDescriptionLabel() {
		return openingDescriptionLabel;
	}

	public synchronized PieceJailChessSquare getPieceJailSquare(Piece coloredPiece) {
		return pieceJailSquares[coloredPiece.index];
	}

	/**
	 * Returns the array of LabeledChessSquares representing the index jail squares.
	 * 
	 * @return
	 */
	public synchronized PieceJailChessSquare[] getPieceJailSquares() {
		return pieceJailSquares;
	}

	/**
	 * Returns the result decorator. Used to decorate a games results over the
	 * chess board..
	 */
	public synchronized ResultDecorator getResultDecorator() {
		return resultDecorator;
	}

	/**
	 * Returns the ChessSquare at the specified square. Drop constants in
	 * GameConstants are also supported.
	 */
	public synchronized ChessSquare getSquare(Square square) {

		if (ChessBoardUtils.isPieceJailSquare(square)) {
			return pieceJailSquares[ChessBoardUtils
					.pieceJailSquareToPiece(square).index];
		} else {
			return squares[square.rank][square.file];
		}
	}

	/**
	 * Returns the square highlighter, used to highlight squares on the chess
	 * board.
	 */
	public synchronized SquareHighlighter getSquareHighlighter() {
		return squareHighlighter;
	}

	/**
	 * Returns the ChessSquares being managed in 0 based rank file order.
	 * square[rank][file].
	 */
	public synchronized ChessSquare[][] getSquares() {
		return squares;
	}

	public synchronized RaptorLabel getStatusLabel() {
		return statusLabel;
	}

	public synchronized RaptorLabel getWhiteClockLabel() {
		return whiteClockLabel;
	}

	public synchronized RaptorLabel getWhiteLagLabel() {
		return whiteLagLabel;
	}

	public synchronized RaptorLabel getWhiteNameRatingLabel() {
		return whiteNameRatingLabel;
	}

	public void hideEngineAnalysisWidget() {
		analysisSash.setMaximizedControl(boardMoveListSash);
		engineAnalysisWidget.getControl().setVisible(false);
		engineAnalysisWidget.quit();
	}

	public void hideMoveList() {
		if (moveList != null && moveList.getControl() != null
				&& moveList.getControl().isVisible()) {
			boardMoveListSash.setMaximizedControl(gameComposite);
			moveList.getControl().setVisible(false);
		}
	}

	public boolean isDisposed() {
		return boardMoveListSash.isDisposed();
	}

	public boolean isShowingEngineAnaylsis() {
		return engineAnalysisWidget.getControl().isVisible();
	}

	/**
	 * Returns true if white is on top, false if white is on botton.
	 */
	public boolean isWhiteOnTop() {
		return isWhiteOnTop;
	}

	/**
	 * Returns true if the white indexs index jail is on top. False if it is on
	 * bottom.
	 */
	public boolean isWhitePieceJailOnTop() {
		return isWhitePieceJailOnTop;
	}

	/**
	 * Forces redraws on the dirty squares and on the dirty indexJailSquares.
	 */
	public void redrawPiecesAndArtifacts() {
		for (ChessSquare[] rank : squares) {
			for (ChessSquare square : rank) {
				if (square.isDirty()) {
					square.redraw();
				}
			}
		}
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null && pieceJailSquare.isDirty()) {
				pieceJailSquare.redraw();
			}
		}
	}

	/**
	 * Forces redraws on all of the squares and all of the indexJailSquares.
	 * @param forceRedraw True if every square should be redrawn. False if only dirty squares should be redrawn.
	 */
	public void redrawPiecesAndArtifacts(boolean forceRedraw) {
		if (forceRedraw) {
			for (ChessSquare[] rank : squares) {
				for (ChessSquare square : rank) {
					square.setDirty(true);
				}
			}
			for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
				if (pieceJailSquare != null) {
					pieceJailSquare.setDirty(true);
                                }
			}
		}
		redrawPiecesAndArtifacts();
	}

	/**
	 * Sets the controller managing this ChessBoard.
	 */
	public synchronized void setController(ChessBoardController controller) {
		this.controller = controller;
		if (moveList != null) {
			moveList.setController(controller);
		}
		if (engineAnalysisWidget != null) {
			engineAnalysisWidget.setController(controller);
		}
	}

	/**
	 * Sets the white on top flag. This method does not redraw or relayout. That
	 * is left to the caller.
	 */
	public void setWhiteOnTop(boolean isWhiteOnTop) {
		this.isWhiteOnTop = isWhiteOnTop;
	}

	/**
	 * Sets the index jail on top flag. This method does not redraw or relayout.
	 * That is left to the caller.
	 */
	public void setWhitePieceJailOnTop(boolean isWhitePieceJailOnTop) {
		this.isWhitePieceJailOnTop = isWhitePieceJailOnTop;
	}

	public synchronized void showEngineAnalysisWidget() {
		engineAnalysisWidget.getControl().setVisible(true);
		analysisSash.setMaximizedControl(null);
		engineAnalysisWidget.onShow();
	}

	public synchronized void showMoveList() {

		int width = moveList.getControl().computeSize(350, SWT.DEFAULT).x;
		boardMoveListSash.setWeights(new int[] {
				boardMoveListSash.getSize().x - width, width });
		moveList.getControl().setVisible(true);
		boardMoveListSash.setMaximizedControl(null);
		moveList.forceRedraw();
	}

	/**
	 * Shows the index jail.
	 */
	public void setPieceJailVisible(boolean visible) {
		whitePieceJailComposite.setVisible(visible);
		blackPieceJailComposite.setVisible(visible);
	}

	/**
	 * Unhides the indexs on all of the squares.
	 */
	public synchronized void unhidePieces() {
		for (ChessSquare[] row : squares) {
			for (ChessSquare square : row) {
				square.setHidingPiece(false);
			}
		}
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setHidingPiece(false);
			}
		}
	}

	/**
	 * Updates all control settings to those in
	 * Raptor.getInstance().getPreferences().
	 */
	public void updateFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance().getPreferences();
		updateBoardFromPrefs();

		Color background = preferences.getColor(BOARD_BACKGROUND_COLOR);

		whiteNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		whiteNameRatingLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		whiteNameRatingLabel.setBackground(background);

		blackNameRatingLabel.setFont(preferences
				.getFont(BOARD_PLAYER_NAME_FONT));
		blackNameRatingLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		blackNameRatingLabel.setBackground(background);

		whiteLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		whiteLagLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
		whiteLagLabel.setBackground(background);

		blackLagLabel.setFont(preferences.getFont(BOARD_LAG_FONT));
		blackLagLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
		blackLagLabel.setBackground(background);

		whiteClockLabel.setFont(preferences.getFont(BOARD_CLOCK_FONT));
		whiteClockLabel.setForeground(preferences
				.getColor(BOARD_INACTIVE_CLOCK_COLOR));
		whiteClockLabel.setBackground(background);

		blackClockLabel.setFont(preferences.getFont(BOARD_CLOCK_FONT));
		blackClockLabel.setForeground(preferences
				.getColor(BOARD_INACTIVE_CLOCK_COLOR));
		blackClockLabel.setBackground(background);

		openingDescriptionLabel.setFont(preferences
				.getFont(BOARD_OPENING_DESC_FONT));
		openingDescriptionLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		openingDescriptionLabel.setBackground(background);

		statusLabel.setFont(preferences.getFont(BOARD_STATUS_FONT));
		statusLabel.setForeground(preferences.getColor(BOARD_CONTROL_COLOR));
		statusLabel.setBackground(background);

		gameDescriptionLabel.setFont(preferences
				.getFont(BOARD_GAME_DESCRIPTION_FONT));
		gameDescriptionLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		gameDescriptionLabel.setBackground(background);

		currentPremovesLabel.setFont(preferences.getFont(BOARD_PREMOVES_FONT));
		currentPremovesLabel.setForeground(preferences
				.getColor(BOARD_CONTROL_COLOR));
		currentPremovesLabel.setBackground(background);

		adjustMoveList();
		adjustChessBoardLayout();

		gameComposite.setBackground(preferences
				.getColor(BOARD_BACKGROUND_COLOR));
		controller.refresh();
		boardMoveListSash.layout(true, true);
		boardMoveListSash.redraw();

		engineAnalysisWidget.updateFromPrefs();
	}

	protected void addPersonMenuItems(Menu menu, String person) {
		if (controller != null && controller.getConnector() != null) {
			ChatUtils.addPersonMenuItems(menu, controller.getConnector(),
					person);
		}
	}

	protected void adjustMoveList() {
		String moveListClassName = Raptor.getInstance().getPreferences()
				.getString(PreferenceKeys.BOARD_MOVE_LIST_CLASS);

		ChessBoardMoveList oldMoveList = moveList;

		if (oldMoveList == null
				|| !moveListClassName.equals(chessBoardLayout.getClass()
						.getName())) {

			try {
				moveList = (ChessBoardMoveList) Class
						.forName(moveListClassName).getConstructor()
						.newInstance();

				boolean wasVisible = false;
				if (oldMoveList != null && oldMoveList.getControl() != null
						&& !oldMoveList.getControl().isDisposed()) {
					wasVisible = oldMoveList.getControl().getVisible();
					oldMoveList.getControl().setVisible(false);
					oldMoveList.getControl().dispose();
					hideMoveList();
				}

				moveList.create(boardMoveListSash);
				moveList.setController(getController());

				if (wasVisible) {
					boardMoveListSash.setMaximizedControl(null);
					moveList.getControl().setVisible(true);
				} else {
					boardMoveListSash.setMaximizedControl(gameComposite);
					moveList.getControl().setVisible(false);
				}
			} catch (Throwable t) {
				Raptor.getInstance().onError("Error creating move list.", t);
			}
		}
	}

	/**
	 * Creates the chess board layout to use for this chess board.
	 */
	protected void adjustChessBoardLayout() {
		String layoutClassName = Raptor.getInstance().getPreferences().getString(PreferenceKeys.BOARD_LAYOUT);
		if (chessBoardLayout == null || !layoutClassName.equals(chessBoardLayout.getClass().getName())) {
			ChessBoardLayout oldLayout = chessBoardLayout;

			try {
				chessBoardLayout = (ChessBoardLayout) Class
						.forName(layoutClassName)
						.getConstructor(ChessBoard.class).newInstance(this);

				if (oldLayout != null) {
					gameComposite.setLayout(null);
					oldLayout.dispose();
				}
				gameComposite.setLayout(chessBoardLayout);

				whiteNameRatingLabel.setAlignment(chessBoardLayout.getAlignment(Field.NAME_RATING_LABEL));
				blackNameRatingLabel.setAlignment(chessBoardLayout.getAlignment(Field.NAME_RATING_LABEL));

				whiteLagLabel.setAlignment(chessBoardLayout.getAlignment(Field.LAG_LABEL));
				blackLagLabel.setAlignment(chessBoardLayout.getAlignment(Field.LAG_LABEL));

				whiteClockLabel.setAlignment(chessBoardLayout.getAlignment(Field.CLOCK_LABEL));
				blackClockLabel.setAlignment(chessBoardLayout.getAlignment(Field.CLOCK_LABEL));

				gameDescriptionLabel.setAlignment(chessBoardLayout.getAlignment(Field.GAME_DESCRIPTION_LABEL));
				statusLabel.setAlignment(chessBoardLayout.getAlignment(Field.STATUS_LABEL));
				currentPremovesLabel.setAlignment(chessBoardLayout.getAlignment(Field.CURRENT_PREMOVE_LABEL));
				openingDescriptionLabel.setAlignment(chessBoardLayout.getAlignment(Field.OPENING_DESCRIPTION_LABEL));
				chessBoardLayout.adjustFontSizes();

			} catch (Throwable t) {
				throw new RuntimeException("Error creating chessBoardLayout "
						+ layoutClassName, t);
			}
		}
	}

	protected void createEngineAnalysisWidget() {
		if (!Variant.isClassic(controller.getGame().getVariant())
				&& controller.getGame().getVariant() != Variant.fischerRandom) {
			engineAnalysisWidget = new XboardAnalysisWidget();
			engineAnalysisWidget.setController(controller);
		} else {
			engineAnalysisWidget = new UciAnalysisWidget();
			engineAnalysisWidget.setController(controller);
		}
	}

	protected void createPieceJailControls() {
		whitePieceJailComposite = new Composite(gameComposite, SWT.NONE);
		pieceJailSquares[Piece.WP.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WP, Square.WP_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.WN.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WN, Square.WN_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.WB.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WB, Square.WB_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.WR.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WR, Square.WR_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.WQ.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WQ, Square.WQ_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.WK.index] = new PieceJailChessSquare(
				whitePieceJailComposite, this, Piece.WK, Square.WK_DROP_FROM_SQUARE);

		blackPieceJailComposite = new Composite(gameComposite, SWT.NONE);
		pieceJailSquares[Piece.BP.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BP, Square.BP_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.BN.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BN, Square.BN_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.BB.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BB, Square.BB_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.BR.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BR, Square.BR_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.BQ.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BQ, Square.BQ_DROP_FROM_SQUARE);
		pieceJailSquares[Piece.BK.index] = new PieceJailChessSquare(
				blackPieceJailComposite, this, Piece.BK, Square.BK_DROP_FROM_SQUARE);
	}

	protected void createSquares() {
		boardComposite = new Composite(gameComposite, SWT.NONE);
		boolean isWhiteSquare = true;
		for (byte rank = 0; rank < 8; rank++) {
			isWhiteSquare = !isWhiteSquare;
			for (byte file = 0; file < squares[rank].length; file++) {
				squares[rank][file] = new ChessSquare(boardComposite, this,
						Square.getSquare(rank, file), isWhiteSquare);
				isWhiteSquare = !isWhiteSquare;
			}
		}
	}

	protected void onNameLabelRightClick(MouseEvent e, RaptorLabel label) {
		if (StringUtils.isNotBlank(label.getText()) && getController() != null
				&& getController().getConnector() != null) {

			String name = label.getText().split(" ")[0];

			if (StringUtils.isNotBlank(name)) {
				Menu menu = new Menu(componentComposite.getShell(), SWT.POP_UP);
				addPersonMenuItems(menu, name);
				if (menu.getItemCount() > 0) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Showing popup with " + menu.getItemCount()
								+ " items. " + label.toDisplay(e.x, e.y));
					}
					menu.setLocation(label.toDisplay(e.x, e.y));
					menu.setVisible(true);
					while (!menu.isDisposed() && menu.isVisible()) {
						if (!componentComposite.getDisplay().readAndDispatch()) {
							componentComposite.getDisplay().sleep();
						}
					}
				}
				menu.dispose();
			}
		}
	}

	/**
	 * Updates only the board and index jails from
 Raptor.getInstance().getPreferences().
	 */
	protected void updateBoardFromPrefs() {
		RaptorPreferenceStore preferences = Raptor.getInstance().getPreferences();
		for (ChessSquare[] row : squares) {
			for (ChessSquare square : row) {
				square.clearCache();
			}
		}
		whitePieceJailComposite.setBackground(preferences
				.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		blackPieceJailComposite.setBackground(preferences
				.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
		for (PieceJailChessSquare pieceJailSquare : pieceJailSquares) {
			if (pieceJailSquare != null) {
				pieceJailSquare.setBackground(preferences
						.getColor(BOARD_PIECE_JAIL_BACKGROUND_COLOR));
				pieceJailSquare.clearCache();
			}
		}
	}

}
