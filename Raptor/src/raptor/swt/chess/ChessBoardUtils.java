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
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.CoolItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.RaptorWindowItem;
import raptor.action.RaptorAction;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.action.SeparatorAction;
import raptor.action.game.AutoBishopAction;
import raptor.action.game.AutoDrawAction;
import raptor.action.game.AutoKingAction;
import raptor.action.game.AutoKnightAction;
import raptor.action.game.AutoQueenAction;
import raptor.action.game.AutoRookAction;
import raptor.action.game.BackAction;
import raptor.action.game.CastleLongAction;
import raptor.action.game.CastleShortAction;
import raptor.action.game.ClearPremovesAction;
import raptor.action.game.CommitAction;
import raptor.action.game.FirstAction;
import raptor.action.game.ForceUpdateAction;
import raptor.action.game.ForwardAction;
import raptor.action.game.LastAction;
import raptor.action.game.MatchWinnerAction;
import raptor.action.game.MoveListAction;
import raptor.action.game.RematchAction;
import raptor.action.game.RevertAction;
import raptor.action.game.ToggleEngineAnalysisAction;
import raptor.action.game.TryVariation;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Move;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;
import raptor.service.ActionScriptService;
import raptor.service.UCIEngineService;
import raptor.service.XboardEngineService;
import raptor.swt.RaptorCursorRegistry;
import raptor.swt.chess.controller.BughouseSuggestController;
import raptor.swt.chess.controller.ExamineController;
import raptor.swt.chess.controller.InactiveController;
import raptor.swt.chess.controller.ObserveController;
import raptor.swt.chess.controller.ToolBarItemKey;
import raptor.util.ImageUtil;
import raptor.util.Logger;
import raptor.util.RaptorRunnable;

public class ChessBoardUtils implements GameConstants, PreferenceKeys {
	public static final String CHESS_SET_DIR = Raptor.RESOURCES_DIR + "set/";
	public static final int DARK_IMAGE_INDEX = 1;
	public static final int LIGHT_IMAGE_INDEX = 0;
	private static final Logger LOG = Logger.getLogger(ChessBoardUtils.class);
	public static final String[] PIECE_IMAGE_PREFIX = { "", "wp", "wn", "wb", "wr",
		"wq", "wk", "bp", "bn", "bb", "br", "bq", "bk" };
	public static final String PIECE_IMAGE_SUFFIX = ".png";
	public static final String SQUARE_BACKGROUND_DIR = Raptor.RESOURCES_DIR + "square/";
	public static final String SQUARE_BACKGROUND_IMAGE_SUFFIX = ".png";

	public static final Object PGN_PREPEND_SYNCH = new Object();
	private static Random RANDOM = new Random();
	private static HashMap<String, List<Short>> chessSetSizes = new HashMap<String, List<Short>>();
	private static HashMap<String, List<Short>> squareBackgroundSizes = new HashMap<String, List<Short>>();

	public static void addActionsToToolbar(
			final ChessBoardController controller,
			RaptorActionContainer container, ToolBar toolbar,
			boolean isUserWhite) {
		RaptorAction[] toolbarActions = ActionScriptService.getInstance()
				.getActions(container);

		for (RaptorAction action : toolbarActions) {
			ToolItem item = createToolItem(action, controller, toolbar,
					isUserWhite);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Added " + action + " to toolbar " + item);
			}
		}

		if (!Raptor.getInstance().getPreferences().getBoolean(
				PreferenceKeys.BOARD_COOLBAR_MODE)) {
			new ToolItem(toolbar, SWT.SEPARATOR);
		}
	}

	public static void adjustCoolbar(ChessBoard board, ToolBar toolbar) {
		clearCoolbar(board);
		toolbar.pack();
		Point size = toolbar.getSize();
		board.getCoolbar().setVisible(true);
		board.getCoolbar().setLocked(true);
		CoolItem coolItem = new CoolItem(board.getCoolbar(), SWT.NONE);
		coolItem.setControl(toolbar);
		coolItem.setSize(size.x, size.y);
		coolItem.setPreferredSize(size.x, size.y);
		coolItem.setMinimumSize(size);
		board.getControl().layout();
	}

	public static boolean arePiecesSameColor(Piece piece1, Piece piece2) {
		return isWhitePiece(piece1) && isWhitePiece(piece2)
				|| isBlackPiece(piece1) && isBlackPiece(piece2);
	}

	public static void clearCoolbar(ChessBoard board) {
		CoolBar coolbar = board.getCoolbar();
		CoolItem[] items = coolbar.getItems();
		for (CoolItem item : items) {
			if (item.getControl() != null && !item.getControl().isDisposed()) {
				item.getControl().dispose();
			}
			item.dispose();
		}
		board.getCoolbar().setVisible(false);
	}

	public static Move createDropMove(Game game, Square fromSquare, Square toSquare) {
		Piece coloredPiece = ChessBoardUtils.pieceJailSquareToPiece(fromSquare);
		try {
			Move result = game.makeDropMove(coloredPiece, toSquare);
			game.rollback();
			return result;
		} catch (IllegalArgumentException iae) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("IllegalArgumentException in game.makeMove()", iae);
			}
			return null;
		}
	}

	public static Move createMove(Game game, Square fromSquare, Square toSquare) {
		try {
			Move result = game.makeMove(fromSquare, toSquare);
			game.rollback();
			return result;
		} catch (IllegalArgumentException iae) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("IllegalArgumentException in game.makeMove()", iae);
			}
			return null;
		}
	}

	public static Move createMove(Game game, Square fromSquare, Square toSquare,
			PieceType nonColoredPromotionPiece) {
		try {
			Move result = game.makeMove(fromSquare, toSquare,
					nonColoredPromotionPiece);
			game.rollback();
			return result;
		} catch (IllegalArgumentException iae) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("IllegalArgumentException in game.makeMove()", iae);
			}
			return null;
		}
	}

	/**
	 * Returns the image from the users image cache matching the type,width, and
	 * height. If the image is in the localImageRegistry it is returned.
	 * Otherwise the image is loaded from the set and cached.
	 */
	public static Image getChessPieceImage(Piece type, int size) {
		if (type == Piece.EMPTY) {
			return null;
		} else {
			return getChessPieceImage(getChessSetName(), type.getPiece(), size);
		}
	}

	/**
	 * Returns a list sorted ascending, containing integers representing all
	 * of the index sizes the set supports.
	 * @param setName The name of the set.
	 * @return A list of sizes supported in ascending order.
	 */
	public static List<Short> getSetPieceSizes(String setName) {
		List<Short> sizes = chessSetSizes.get(setName);
		if (sizes == null) {
			sizes = new ArrayList<Short>(100);

			File file = new File(CHESS_SET_DIR + setName);
			File[] files = file.listFiles();
			for (File currentFile : files) {
				if (currentFile.isDirectory()) {
					try {
						short size = Short.parseShort(currentFile.getName());
						sizes.add(size);
					} catch (NumberFormatException nfe) {
					}
				}
			}
			Collections.sort(sizes);
			chessSetSizes.put(setName, sizes);
		}
		return sizes;
	}
	
	public static List<Short> getSquareBackgroundSizes(String squareBackgroundName) {
		List<Short> sizes = squareBackgroundSizes.get(squareBackgroundName);
		if (sizes == null) {
			sizes = new ArrayList<Short>(100);

			File file = new File(SQUARE_BACKGROUND_DIR + squareBackgroundName);
			File[] files = file.listFiles();
			for (File currentFile : files) {
				if (currentFile.isDirectory()) {
					try {
						short size = Short.parseShort(currentFile.getName());
						sizes.add(size);
					} catch (NumberFormatException nfe) {
					}
				}
			}
			Collections.sort(sizes);
			squareBackgroundSizes.put(squareBackgroundName, sizes);
		}
		return sizes;
	}

	/**
	 * Returns the image with the specified of the specified name,type,width and
	 * height. If the image is in the localImageRegistry it is returned.
	 * Otherwise the closest size is loaded from the chess set and cached.
	 */
	public static Image getChessPieceImage(String name, Piece type, int size) {
		if (type == Piece.EMPTY) {
			return null;
		} else {
			if (size < 8) {
				size = 8;
			}

			String key = name + "_" + type.ch + "_" + size + "x" + size;
			Image image = Raptor.getInstance().getImageRegistry().get(key);

			if (image == null) {
				//This list is sorted ascending.
				List<Short> supportedSizes = getSetPieceSizes(name);
				
				if (!supportedSizes.contains(size)) {
					//TO DO: make this more of a binary search for speed.
					int lastSize = supportedSizes.get(0);
					boolean foundMatch = false;
					
					for (int currentSize : supportedSizes) {
						if (currentSize > size) {
							size = lastSize;
							foundMatch = true;
							break;
						}
						lastSize = currentSize;
					}
					
					if (!foundMatch) {
						//This will be the largest size found.
						size = lastSize;
					}
				}

				Image result = new Image(Display.getCurrent(), CHESS_SET_DIR
						+ name + "/" + size + "/" + getPieceName(type));
				Raptor.getInstance().getImageRegistry().put(key, result);
				return result;
			} else {
				return image;
			}
		}
	}
	
	/**
	 * Returns the Image for users current background name
	 */
	public static Image getSquareBackgroundImage(String name,
			SquareBackgroundImageEffect effect, boolean isLight, Square squareId,
			int width, int height) {
		if (width <= 0 || height <= 0) {
			width = 10;
			height = 10;
		}
		
		//There are two types of square backgrounds supported.
		//Type 1 (Classic):
		//    Consists of two images a light image and a dark image.
		//    Has an effect, Crop, Random Crop, or Scale.
		//    Crop crops out only what is needed form the image based on width and height.
		//    Random crop crops out random areas of the image, even rotating it 180 degrees.
		//    Scale scales the image to size.
		//    Works great for wood grain and marble.
		//Type 2 (Images for every size):
		//    Type 2 consists of a light image and a dark image for each possible square size 10-200+
		//    It is great for square backgrounds with decorations like borders and diagonal lines.
		//    No scaling or cropping is involved, and using the chess set creator you can create all
		//     of the sizes from svg.

		String key = name + "_" + effect + "_" + isLight + "_" + squareId.index + "_"
				+ width + "x" + height;

		Image result = Raptor.getInstance().getImageRegistry().get(key);
		if (result != null) {
			//The image is already cached so just return it.
			return result;
                }

		//This list is sorted ascending.
		List<Short> supportedSizes = getSquareBackgroundSizes(name);
		
		if (supportedSizes.isEmpty()) { //Type 1
			Image moldImage = getSquareBackgroundMold(name, isLight);
			ImageData moldImageData = moldImage.getImageData();

			// If the image is smaller than the width/height needed then just
			// scale it and ignore the effect.
			if (moldImageData.width < width || moldImageData.height < height) {
				effect = SquareBackgroundImageEffect.Scale;
			}

			switch (effect) {
			case Scale:
				result = new Image(Display.getCurrent(),
						moldImageData.scaledTo(width, height));
				break;
			case Crop:
				result = ImageUtil.cropImage(moldImage, 0, 0, width, height);
				break;
			case RandomCrop:
				int x = RANDOM.nextInt(moldImageData.width - width);
				int y = RANDOM.nextInt(moldImageData.height - height);

				// Add some more randomness by flipping the image 180 degrees.
				// This is safe for most images, including wood grain.
				if (RANDOM.nextBoolean()) {
					result = ImageUtil.cropImage(moldImage, x, y, width, height);
				} else {
					result = ImageUtil.flipAndCrop(moldImage, x, y, width, height);
				}
				break;
			}
		}
		else { //Type 2
			if (!supportedSizes.contains(width)) {
				//TO DO: make this more of a binary search for speed.
				int lastSize = supportedSizes.get(0);
				boolean foundMatch = false;
				
				for (int currentSize : supportedSizes) {
					if (currentSize > width) {
						width = lastSize;
						foundMatch = true;
						break;
					}
					lastSize = currentSize;
				}
				
				if (!foundMatch) {
					//This will be the largest size found.
					width = lastSize;
				}
			}
			result = new Image(Display.getCurrent(), SQUARE_BACKGROUND_DIR
					+ name + "/" + width + "/" + (isLight ? "light.png" : "dark.png"));
			
		}
		Raptor.getInstance().getImageRegistry().put(key, result);
		return result;
	}

	/**
	 * Returns the users current chess set name.
	 */
	public static String getChessSetName() {
		return Raptor.getInstance().getPreferences()
				.getString(BOARD_CHESS_SET_NAME);
	}

	/**
	 * Returns a list of all chess set names.
	 */
	public static String[] getChessSetNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(CHESS_SET_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (File file2 : files) {
			StringTokenizer tok = new StringTokenizer(file2.getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	public static Piece getColoredPiece(PieceType piece, boolean isWhite) {
		switch (piece) {
		case PAWN:
			return isWhite ? Piece.WP : Piece.BP;
		case KNIGHT:
			return isWhite ? Piece.WN : Piece.BN;
		case BISHOP:
			return isWhite ? Piece.WB : Piece.BB;
		case ROOK:
			return isWhite ? Piece.WR : Piece.BR;
		case QUEEN:
			return isWhite ? Piece.WQ : Piece.BQ;
		case KING:
			return isWhite ? Piece.WK : Piece.BK;
		case EMPTY:
			return Piece.EMPTY;
		default:
			throw new IllegalArgumentException("Invalid piece " + piece);
		}
	}

	/**
	 * Returns the cursor for the specified index type.
	 * 
	 * @param type
	 *            The index type.
	 * @return
	 */
	public static Cursor getCursorForPiece(Piece type, int size) {
		String key = getChessSetName() + "_" + type + "_" + size + "x" + size;

		RaptorCursorRegistry registry = Raptor.getInstance().getCursorRegistry();
		Cursor result = registry.get(key);
		if (result == null) {
			ImageData pieceImageData = getChessPieceImage(type, size).getImageData();

			int hotx = pieceImageData.width / 2;
			int hoty = pieceImageData.height / 2;

			result = new Cursor(registry.getDisplay(), pieceImageData, hotx, hoty);
			registry.put(key, result);
		}
		return result;
	}

	/**
	 * Returns the quadrant to use for the specified controller. This should not
	 * be used if its the "other" bughouse board. Use
	 * getQuadrantForController(controller,true) for that.
	 */
	public static Quadrant getQuadrantForController(
			ChessBoardController controller) {
		return getQuadrantForController(controller, false);
	}

	/**
	 * Returns an array of quadrants available for chess boards. This does not
	 * do any filtering on games currently being played or bughouse, it just
	 * returns a list of all possible quadrants boards can be placed at.
	 */
	public static Quadrant[] getQuadrantsAvailableForBoards() {
		String[] boardQuadrants = Raptor.getInstance().getPreferences()
				.getStringArray(PreferenceKeys.APP_CHESS_BOARD_QUADRANTS);

		Quadrant[] result = new Quadrant[boardQuadrants.length];
		for (int i = 0; i < boardQuadrants.length; i++) {
			result[i] = Quadrant.valueOf(boardQuadrants[i]);
		}
		return result;
	}

	/**
	 * Returns the ideal quadrant for this controller. Criteria used for making
	 * this decision include: available quadrants for a chess board, if the game
	 * is bughouse, and if an item can be taken over.
	 */
	public static Quadrant getQuadrantForController(
			ChessBoardController controller, boolean isBughouseOtherBoard) {
		Quadrant[] availableQuadrants = getQuadrantsAvailableForBoards();
		Quadrant result = null;

		if (availableQuadrants.length == 1) {
			result = availableQuadrants[0];
		} else {
			if (Variant.isBughouse(controller.getGame().getVariant())) {
				result = isBughouseOtherBoard ? availableQuadrants[1]
						: availableQuadrants[0];
			} else {
				quadrantLoop: for (Quadrant currentQuadrant : availableQuadrants) {
					// If a board is already open in this quadrant, be nice and
					// open it in another quadrant if possible.
					RaptorWindowItem[] items = Raptor.getInstance().getWindow()
							.getWindowItems(currentQuadrant);

					if (items == null || items.length == 0) {
						result = currentQuadrant;
						break;
					} else {
						for (RaptorWindowItem currentItem : items) {
							if (currentItem instanceof ChessBoardWindowItem) {
								ChessBoardWindowItem chessBoardItem = (ChessBoardWindowItem) currentItem;
								if (chessBoardItem.isTakeOverable()) {
									result = currentQuadrant;
									break quadrantLoop;
								} else {
									continue quadrantLoop;
								}
							}
						}
						result = currentQuadrant;
						break;
					}
				}
				if (result == null) {
					result = availableQuadrants[0];
				}
			}
		}
		return result;
	}

	/**
	 * Returns the Image for users current background name
	 */
	public static Image getSquareBackgroundImage(boolean isLight, Square squareId,
			int width, int height) {
		SquareBackgroundImageEffect effect = SquareBackgroundImageEffect
				.valueOf(Raptor.getInstance().getPreferences()
						.getString(BOARD_SQUARE_BACKGROUND_IMAGE_EFFECT));

		return getSquareBackgroundImage(getSquareBackgroundName(), effect,
				isLight, squareId, width, height);
	}



	/**
	 * Returns the path to the backgrund image name.
	 */
	public static String getSquareBackgroundImageName(
			String squareBackgroundName, boolean isLight) {
		return SQUARE_BACKGROUND_DIR + squareBackgroundName + "/"
				+ (isLight ? "light" : "dark") + SQUARE_BACKGROUND_IMAGE_SUFFIX;
	}

	/**
	 * Returns an image in the default size of the specified background name and
	 * color.
	 */
	public static Image getSquareBackgroundMold(String name, boolean isLight) {
		return Raptor.getInstance().getImage(
				getSquareBackgroundImageName(name, isLight));
	}


	/**
	 * Returns the users current square background name.
	 */
	public static String getSquareBackgroundName() {
		return Raptor.getInstance().getPreferences()
				.getString(BOARD_SQUARE_BACKGROUND_NAME);
	}

	/**
	 * Returns a list of all the square background names.
	 */
	public static String[] getSquareBackgroundNames() {
		List<String> result = new LinkedList<String>();

		File file = new File(SQUARE_BACKGROUND_DIR);
		File[] files = file.listFiles(new FilenameFilter() {

			public boolean accept(File arg0, String arg1) {
				File file = new File(arg0.getAbsolutePath() + "/" + arg1);
				return file.isDirectory() && !file.getName().startsWith(".");
			}

		});

		for (File file2 : files) {
			StringTokenizer tok = new StringTokenizer(file2.getName(), ".");
			result.add(tok.nextToken());
		}

		Collections.sort(result);
		return result.toArray(new String[0]);
	}

	/**
	 * Returns the path to the specified chess index.
	 */
	public static String getPieceName(Piece piece) {
		return PIECE_IMAGE_PREFIX[piece.index] + PIECE_IMAGE_SUFFIX;
	}

	/**
	 * Returns a to move indicator indicating the side to move with the
	 * specified width/height this image will always be a square.
	 */
	public static Image getToMoveIndicatorImage(boolean isSideToMove, int width) {
		if (width < 5) {
			width = 5;
		}

		String key = "toMoveIndicator_" + isSideToMove + "_" + width;
		Image result = Raptor.getInstance().getImageRegistry().get(key);
		if (result == null) {
			Image mold = getToMoveIndicatorImageMold(isSideToMove);
			result = new Image(mold.getDevice(), mold.getImageData().scaledTo(
					width, width));
			Raptor.getInstance().getImageRegistry().put(key, result);
		}
		return result;
	}

	/**
	 * Returns a to move indicator indicating the side to move with the
	 * specified width/height this image will always be a square.
	 */
	public static Image getToMoveIndicatorImageMold(boolean isSideToMove) {
		String key = "toMoveIndicator_" + isSideToMove;

		Image result = Raptor.getInstance().getImageRegistry().get(key);
		if (result == null) {
			result = new Image(Raptor.getInstance().getWindow().getShell()
					.getDisplay(), Raptor.IMAGES_DIR + "circle_"
					+ (isSideToMove ? "green" : "gray") + "30x30.png");
			Raptor.getInstance().getImageRegistry().put(key, result);
		}
		return result;
	}

	/**
	 * Returns the path to the specified chess index in the users image cache.
	 */
	public static String getUserImageCachePieceName(String chessSetName,
			int piece, int width, int height) {
		return Raptor.USER_RAPTOR_HOME_PATH + "/imagecache/" + chessSetName
				+ "_" + PIECE_IMAGE_PREFIX[piece] + "_" + width + "_" + height
				+ ".png";

	}

	public static String halfMoveIndexToDescription(int halfMoveIndex,
			int colorToMove) {
		int fullMoveIndex = halfMoveIndex / 2 + 1;

		return colorToMove == PieceColor.WHITE.index ? fullMoveIndex + ") " : fullMoveIndex
				+ ") ... ";
	}

	public static boolean isBlackPiece(Piece piece) {
		return GameUtils.isBlackPiece(piece);
	}

	public static boolean isJailSquareBlackPiece(Square pieceJailSquare) {
		return isBlackPiece(pieceJailSquareToPiece(pieceJailSquare));
	}

	public static boolean isJailSquareWhitePiece(Square pieceJailSquare) {
		return isWhitePiece(pieceJailSquareToPiece(pieceJailSquare));
	}

	public static boolean isPieceJailSquare(Square pieceJailSquare) {
		return GameUtils.isDropSquare(pieceJailSquare);
	}

	public static boolean isWhitePiece(Piece piece) {
		return GameUtils.isWhitePiece(piece);
	}

	public static String lagToString(long lag) {
		if (lag < 0) {
			lag = 0;
		}
		int seconds = (int) (lag / 1000L);
		int tenths = (int) (lag % 1000) / 100;
		return "Lag " + seconds + "." + tenths + " sec";
	}

	/**
	 * Opens a ChessBoardWindowItem for the specified controller. It is
	 * preferred that you use this method and not create a ChessBoardWindowItem
	 * and add it to RaptorWindow, as it contains code to take-over inactive
	 * window items if they are available. This can greatly increase
	 * performance.
	 * 
	 * This method only handles controllers that are not the "other" bughosue
	 * board. Use openBoard(controller,true) to do that.
	 */
	public static void openBoard(ChessBoardController controller) {
		openBoard(controller, false);
	}

	/**
	 * Opens a ChessBoardWindowItem for the specified controller. It is
	 * preferred that you use this method, as it contains code to take-over
	 * inactive window items if they are available. This can greatly increase
	 * performance.
	 */
	public static void openBoard(final ChessBoardController controller,
			final boolean isBughouseOtherBoard) {
		Raptor.getInstance().getDisplay()
				.asyncExec(new RaptorRunnable(controller.getConnector()) {
					@Override
					public void execute() {
						Quadrant quadrant = getQuadrantForController(
								controller, isBughouseOtherBoard);
						ChessBoardWindowItem item = null;

						if (Raptor
								.getInstance()
								.getPreferences()
								.getBoolean(
										PreferenceKeys.BOARD_TAKEOVER_INACTIVE_GAMES)) {
							item = Raptor
									.getInstance()
									.getWindow()
									.getChessBoardWindowItemToTakeOver(quadrant);
							if (item == null
									&& controller.getGame().getVariant() != Variant.bughouse
									&& controller.getGame().getVariant() != Variant.fischerRandomBughouse) {
								item = Raptor
										.getInstance()
										.getWindow()
										.getChessBoardWindowItemToTakeOver(
												quadrant);
							}
						}

						if (item == null) {
							// No item to take over so create one.
							item = new ChessBoardWindowItem(controller,
									isBughouseOtherBoard);
							Raptor.getInstance().getWindow()
									.addRaptorWindowItem(item);
						} else {
							// Take over the item.
							item.getBoard().hideEngineAnalysisWidget();
							item.getBoard().hideMoveList();
							item.takeOver(controller, isBughouseOtherBoard);
							Raptor.getInstance().getWindow().forceFocus(item);
						}
					}
				});
	}

	public static String pieceCountToString(int count) {
		if (count < 2) {
			return "";
		} else {
			return String.valueOf(count);
		}
	}

	public static PieceType pieceFromColoredPiece(Piece coloredPiece) {
		return coloredPiece.type;
//		switch (coloredPiece) {
//		case EMPTY:
//			return GameConstants.PieceType.EMPTY;
//		case WP:
//		case BP:
//			return GameConstants.PieceType.PAWN;
//		case WN:
//		case BN:
//			return GameConstants.PieceType.KNIGHT;
//		case WB:
//		case BB:
//			return GameConstants.PieceType.BISHOP;
//		case WR:
//		case BR:
//			return GameConstants.PieceType.ROOK;
//		case WQ:
//		case BQ:
//			return GameConstants.PieceType.QUEEN;
//		case WK:
//		case BK:
//			return GameConstants.PieceType.KING;
//		default:
//			throw new IllegalArgumentException("Invalid coloredPiece "
//					+ coloredPiece);
//		}
	}

	public static Piece pieceJailSquareToPiece(Square pieceJailSquare) {
		return GameConstants.PIECES[Piece.WP.index + pieceJailSquare.index - Square.WP_DROP_FROM_SQUARE.index];
	}

	protected static ToolItem createToolItem(final RaptorAction action,
			final ChessBoardController controller, ToolBar toolbar,
			boolean isUserWhite) {
		ToolItem result = null;
		if (action instanceof SeparatorAction) {
			result = new ToolItem(toolbar, SWT.SEPARATOR);
			return result;
		} else if (action instanceof RematchAction) {
			if (controller.getConnector() == null) {
				return null;
			}
			result = new ToolItem(toolbar, SWT.FLAT);
			result.setText(action.getName());
		} else if (action instanceof MatchWinnerAction) {
			if (controller instanceof BughouseSuggestController) {
				return null;
			}
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.MATCH_WINNER, result);

		} else if (action instanceof ToggleEngineAnalysisAction) {
			if (controller instanceof InactiveController
					|| controller instanceof ExamineController
					|| controller instanceof ObserveController) {

				if ((Variant.isClassic(controller.getGame().getVariant()) && UCIEngineService
						.getInstance().getDefaultEngine() != null)
						|| XboardEngineService.getInstance()
								.hasEnginesSupportingVariant(
										controller.getGame().getVariant())
						|| UCIEngineService.getInstance()
								.containsFischerRandomEngines()) {
					result = new ToolItem(toolbar, SWT.CHECK);
					controller.addToolItem(
							ToolBarItemKey.TOGGLE_ANALYSIS_ENGINE, result);

					if (controller.getBoard() != null
							&& controller.getBoard().isShowingEngineAnaylsis()) {
						result.setSelection(true);
					}
				}
			}

			if (result == null) {
				return null;
			}
		} else if (action instanceof AutoDrawAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.AUTO_DRAW, result);
		} else if (action instanceof BackAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.BACK_NAV, result);
		} else if (action instanceof ForwardAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.NEXT_NAV, result);
		} else if (action instanceof FirstAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.FIRST_NAV, result);
		} else if (action instanceof LastAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.LAST_NAV, result);
		} else if (action instanceof RevertAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.REVERT_NAV, result);
		} else if (action instanceof CommitAction) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.COMMIT_NAV, result);
		} else if (action instanceof ClearPremovesAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.CLEAR_PREMOVES, result);
		} else if (action instanceof MoveListAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.MOVE_LIST, result);
		} else if (action instanceof ForceUpdateAction) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.FORCE_UPDATE, result);
		} else if (action instanceof TryVariation) {
			result = new ToolItem(toolbar, SWT.CHECK);
			controller.addToolItem(ToolBarItemKey.TRY_VARIATION, result);
		}else if (action instanceof AutoQueenAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_QUEEN, result);
			int pieceSize = Raptor.getInstance().getPreferences()
					.getInt(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? Piece.WQ
					: Piece.BQ, pieceSize));
		} else if (action instanceof AutoKnightAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KNIGHT, result);
			int pieceSize = Raptor.getInstance().getPreferences()
					.getInt(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? Piece.WN
					: Piece.BN, pieceSize));
		} else if (action instanceof AutoBishopAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_BISHOP, result);
			int pieceSize = Raptor.getInstance().getPreferences()
					.getInt(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? Piece.WB
					: Piece.BB, pieceSize));
		} else if (action instanceof AutoRookAction) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_ROOK, result);
			int pieceSize = Raptor.getInstance().getPreferences()
					.getInt(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? Piece.WR
					: Piece.BR, pieceSize));
		} else if (action instanceof AutoKingAction
				&& controller.getGame().getVariant() == Variant.suicide) {
			result = new ToolItem(toolbar, SWT.RADIO);
			controller.addToolItem(ToolBarItemKey.AUTO_KING, result);
			int pieceSize = Raptor.getInstance().getPreferences()
					.getInt(PreferenceKeys.APP_TOOLBAR_PIECE_SIZE);
			result.setImage(getChessPieceImage("Portable", isUserWhite ? Piece.WK
					: Piece.BK, pieceSize));
		} else if (action instanceof AutoKingAction) {
			return null;
		} else if (action instanceof CastleLongAction
				&& controller.getGame().isInState(Game.FISCHER_RANDOM_STATE)) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.CASTLE_LONG, result);
		} else if (action instanceof CastleLongAction) {
			return null;
		} else if (action instanceof CastleShortAction
				&& controller.getGame().isInState(Game.FISCHER_RANDOM_STATE)) {
			result = new ToolItem(toolbar, SWT.FLAT);
			controller.addToolItem(ToolBarItemKey.CASTLE_SHORT, result);
		} else if (action instanceof CastleShortAction) {
			return null;
		} else {
			result = new ToolItem(toolbar, SWT.FLAT);
		}

		if (StringUtils.isBlank(result.getText())
				&& StringUtils.isBlank(action.getIcon())
				&& result.getImage() == null) {
			result.setText(action.getName());
		} else if (StringUtils.isNotBlank(action.getIcon())
				&& result.getImage() == null) {
			result.setImage(Raptor.getInstance().getIcon(action.getIcon()));
		}

		if (StringUtils.isNotBlank(action.getDescription())) {
			result.setToolTipText(action.getDescription());
		}

		result.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				RaptorAction loadedAction = ActionScriptService.getInstance()
						.getAction(action.getName());
				loadedAction.setChessBoardControllerSource(controller);
				loadedAction.run();
			}
		});
		return result;
	}
}
