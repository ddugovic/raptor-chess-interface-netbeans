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

import raptor.util.Logger;
 
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.pref.PreferenceKeys;
import raptor.swt.chess.ChessBoard;

/**
 * This layout adjusts the font sizes to match the viewing area. It displays all
 * labels.
 */
public class RightOrientedFixedLayout extends ChessBoardLayout implements GameConstants {
	public static final int[] BOARD_WIDTH_MARGIN_PERCENTAGES = { 1, 1 };

	public static final int BOTTOM_LABEL_HEIGHT_PERCENTAGE_OF_SCREEN = 3;
	public static final int[] BOTTOM_LABEL_WIDTH_MARGIN_PERCENTAGES = { 1, 1 };
	public static final int[] BUTTOM_LABEL_HEIGHT_MARGIN_PERCENTAGES = { 1, 1 };

	public static final int EAST = 1;

	private static final Logger LOG = Logger.getLogger(RightOrientedFixedLayout.class);

	public static final int NORTH = 0;
	public static final int SOUTH = 1;

	public static final int[] TOP_LABEL_HEIGHT_MARGIN_PERCENTAGES = { 1, 1, };
	public static final int TOP_LABEL_HEIGHT_PERCENTAGE_OF_SCREEN = 3;
	public static final int[] TOP_LABEL_WIDTH_MARGIN_PERCENTAGES = { 1, 1 };
	public static final int WEST = 0;

	protected int boardHeight;
	protected Rectangle boardRect;
	protected Rectangle bottomClockRect;
	protected int bottomLabelHeight;
	protected Rectangle bottomLagRect;
	protected Rectangle bottomNameLabelRect;
	protected Rectangle bottomPieceJailRect;
	protected ControlListener controlListener;
	protected Rectangle currentPremovesLabelRect;
	protected Rectangle gameDescriptionLabelRect;
	protected boolean hasHeightProblem = false;
	protected boolean hasSevereHeightProblem = false;
	protected Rectangle openingDescriptionLabelRect;
	protected int pieceJailSquareSize;
	protected int squareSize;
	protected Rectangle statusLabelRect;
	protected Rectangle topClockRect;
	protected int topLabelHeight;
	protected Rectangle topLagRect;
	protected Rectangle topNameLabelRect;
	protected Rectangle topPieceJailRect;

	public RightOrientedFixedLayout(ChessBoard board) {
		super(board);

		board.getGameComposite().addControlListener(
				controlListener = new ControlListener() {

					public void controlMoved(ControlEvent e) {
					}

					public void controlResized(ControlEvent e) {
						setLayoutData();
					}
				});
	}

	protected Font getFont(String property) {
		return Raptor.getInstance().getPreferences().getFont(property);
	}

	@Override
	public void adjustFontSizes() {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Adjusting font sizes.");
		}
		board.getGameDescriptionLabel().setFont(
				getFont(PreferenceKeys.BOARD_GAME_DESCRIPTION_FONT));
		board.getCurrentPremovesLabel().setFont(
				getFont(PreferenceKeys.BOARD_PREMOVES_FONT));
		board.getStatusLabel().setFont(
				getFont(PreferenceKeys.BOARD_STATUS_FONT));
		board.getOpeningDescriptionLabel().setFont(
				getFont(PreferenceKeys.BOARD_OPENING_DESC_FONT));
		board.getWhiteNameRatingLabel().setFont(
				getFont(PreferenceKeys.BOARD_PLAYER_NAME_FONT));
		board.getBlackNameRatingLabel().setFont(
				getFont(PreferenceKeys.BOARD_PLAYER_NAME_FONT));
		board.getWhiteLagLabel()
				.setFont(getFont(PreferenceKeys.BOARD_LAG_FONT));
		board.getBlackLagLabel()
				.setFont(getFont(PreferenceKeys.BOARD_LAG_FONT));
		board.getWhiteClockLabel().setFont(
				getFont(PreferenceKeys.BOARD_CLOCK_FONT));
		board.getBlackClockLabel().setFont(
				getFont(PreferenceKeys.BOARD_CLOCK_FONT));
	}

	@Override
	public void dispose() {
		super.dispose();
		if (!board.isDisposed()) {
			board.getGameComposite().removeControlListener(controlListener);
		}
		if (LOG.isInfoEnabled()) {
			LOG.info("Disposed RightOrientedLayout");
		}
	}

	@Override
	public int getAlignment(Field field) {
		switch (field) {
		case GAME_DESCRIPTION_LABEL:
			return SWT.LEFT;
		case CURRENT_PREMOVE_LABEL:
			return SWT.LEFT;
		case STATUS_LABEL:
			return SWT.LEFT;
		case OPENING_DESCRIPTION_LABEL:
			return SWT.LEFT;
		case NAME_RATING_LABEL:
			return SWT.LEFT;
		case CLOCK_LABEL:
			return SWT.LEFT;
		case LAG_LABEL:
			return SWT.LEFT;
		case UP_TIME_LABEL:
			return SWT.LEFT | SWT.BORDER;
		default:
			return SWT.NONE;
		}
	}

	@Override
	public String getName() {
		return "Right Oriented Fixed Font";
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {

		if (LOG.isDebugEnabled()) {
			LOG.debug("in layout(" + flushCache + ") " + composite.getSize().x
					+ " " + composite.getSize().y);
		}

		if (flushCache) {
			setLayoutData();
			adjustFontSizes();
		}

		long startTime = System.currentTimeMillis();

		board.getGameDescriptionLabel().setBounds(gameDescriptionLabelRect);
		board.getCurrentPremovesLabel().setBounds(currentPremovesLabelRect);
		board.getStatusLabel().setBounds(statusLabelRect);
		board.getOpeningDescriptionLabel().setBounds(
				openingDescriptionLabelRect);

		layoutChessBoard(boardRect);

		board.getWhiteNameRatingLabel().setBounds(
				board.isWhiteOnTop() ? topNameLabelRect : bottomNameLabelRect);
		board.getWhiteLagLabel().setBounds(
				board.isWhiteOnTop() ? topLagRect : bottomLagRect);
		board.getWhiteClockLabel().setBounds(
				board.isWhiteOnTop() ? topClockRect : bottomClockRect);
		board.getWhitePieceJailComposite().setBounds(
				board.isWhitePieceJailOnTop() ? topPieceJailRect : bottomPieceJailRect);

		board.getBlackNameRatingLabel().setBounds(
				board.isWhiteOnTop() ? bottomNameLabelRect : topNameLabelRect);
		board.getBlackLagLabel().setBounds(
				board.isWhiteOnTop() ? bottomLagRect : topLagRect);
		board.getBlackClockLabel().setBounds(
				board.isWhiteOnTop() ? bottomClockRect : topClockRect);
		board.getBlackPieceJailComposite().setBounds(
				board.isWhitePieceJailOnTop() ? bottomPieceJailRect : topPieceJailRect);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Layout completed in "
					+ (System.currentTimeMillis() - startTime));
		}
	}

	protected void setLayoutData() {

		if (LOG.isDebugEnabled()) {
			LOG.debug("Setting layout data.");
		}

		int width = board.getGameComposite().getSize().x;
		int height = board.getGameComposite().getSize().y;

		hasHeightProblem = false;
		hasSevereHeightProblem = false;

		if (width < height) {
			height = width;
			hasHeightProblem = true;
		}

		int topLabelNorthMargin = height
				* TOP_LABEL_HEIGHT_MARGIN_PERCENTAGES[NORTH] / 100;

		int topLabelSouthMargin = height
				* TOP_LABEL_HEIGHT_MARGIN_PERCENTAGES[SOUTH] / 100;

		topLabelHeight = height * TOP_LABEL_HEIGHT_PERCENTAGE_OF_SCREEN / 100
				+ topLabelNorthMargin + topLabelSouthMargin;

		int bottomLabelNorthMargin = height
				* BUTTOM_LABEL_HEIGHT_MARGIN_PERCENTAGES[NORTH] / 100;
		int bottomLabelSouthMargin = height
				* BUTTOM_LABEL_HEIGHT_MARGIN_PERCENTAGES[SOUTH] / 100;
		bottomLabelHeight = height * BOTTOM_LABEL_HEIGHT_PERCENTAGE_OF_SCREEN
				/ 100 + bottomLabelNorthMargin + bottomLabelSouthMargin;

		int boardWidthPixelsWest = width * BOARD_WIDTH_MARGIN_PERCENTAGES[WEST] / 100;
		int boardWidthPixelsEast = width * BOARD_WIDTH_MARGIN_PERCENTAGES[EAST] / 100;

		squareSize = (height - bottomLabelHeight - topLabelHeight) / 16 * 2;

		while (width < squareSize * 11 + boardWidthPixelsWest
				+ boardWidthPixelsEast) {
			squareSize -= 2;
			hasSevereHeightProblem = true;
		}

		pieceJailSquareSize = squareSize;

		boardHeight = squareSize * 8;

		int topLabelPixelsWest = width
				* TOP_LABEL_WIDTH_MARGIN_PERCENTAGES[WEST] / 100;
		int bottonLabelPixelsWest = width
				* BOTTOM_LABEL_WIDTH_MARGIN_PERCENTAGES[WEST] / 100;

		int gameDescriptionWidth = boardWidthPixelsWest + boardHeight;
		int currentPremovesWidth = width - boardWidthPixelsWest;
		int gameStatusWidth = bottonLabelPixelsWest + boardHeight;
		int openingDescriptionWidth = width - gameStatusWidth;

		gameDescriptionLabelRect = new Rectangle(topLabelPixelsWest, 0,
				gameDescriptionWidth, topLabelHeight);
		currentPremovesLabelRect = new Rectangle(topLabelPixelsWest
				+ gameDescriptionLabelRect.width, 0, currentPremovesWidth,
				topLabelHeight);

		statusLabelRect = new Rectangle(bottonLabelPixelsWest, topLabelHeight
				+ 8 * squareSize, gameStatusWidth, bottomLabelHeight);
		openingDescriptionLabelRect = new Rectangle(bottonLabelPixelsWest
				+ statusLabelRect.width, topLabelHeight + 8 * squareSize,
				openingDescriptionWidth, bottomLabelHeight);

		boardRect = new Rectangle(boardWidthPixelsWest, topLabelHeight,
				8 * squareSize, 8 * squareSize);

		int nameLabelStartX = boardWidthPixelsWest + boardHeight
				+ boardWidthPixelsEast;

		int clockStartX = boardWidthPixelsWest + boardHeight
				+ boardWidthPixelsEast;

		Point clockLabelSize = new Point(width - clockStartX, squareSize);

		Point lagLabelSize = new Point(width - nameLabelStartX,
				(int) (.3 * squareSize));

		Point nameLabelSize = new Point(width - nameLabelStartX, squareSize
				- lagLabelSize.y);

		int nameStartY = topLabelHeight;
		int bottomHeightStart = nameStartY + 4 * squareSize;

		topNameLabelRect = new Rectangle(nameLabelStartX, nameStartY,
				nameLabelSize.x, nameLabelSize.y);

		topLagRect = new Rectangle(nameLabelStartX, nameStartY
				+ topNameLabelRect.height, lagLabelSize.x, lagLabelSize.y);
		topClockRect = new Rectangle(clockStartX, nameStartY + squareSize,
				clockLabelSize.x, clockLabelSize.y);

		bottomNameLabelRect = new Rectangle(nameLabelStartX, bottomHeightStart,
				nameLabelSize.x, nameLabelSize.y);

		bottomLagRect = new Rectangle(nameLabelStartX, bottomHeightStart
				+ bottomNameLabelRect.height, lagLabelSize.x, lagLabelSize.y);
		bottomClockRect = new Rectangle(clockStartX, bottomHeightStart
				+ squareSize, clockLabelSize.x, clockLabelSize.y);

		topPieceJailRect = new Rectangle(clockStartX, topLabelHeight + 2 * squareSize,
				getPieceJailColumns() * pieceJailSquareSize, getPieceJailRows() * pieceJailSquareSize);
		bottomPieceJailRect = new Rectangle(clockStartX, topLabelHeight + 6 * squareSize,
				getPieceJailColumns() * pieceJailSquareSize, getPieceJailRows() * pieceJailSquareSize);
	}

}