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
package raptor.chess;

import java.util.StringTokenizer;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.chess.util.ZobristUtils;
import raptor.util.Logger;

/**
 * Contains methods to create Games from fen and starting positions.
 */
public class GameFactory implements GameConstants {

	public static final Logger LOG = Logger.getLogger(GameFactory.class);

	/**
	 * Creates a game from fen of the specified type.
	 * 
	 * <pre>
	 * rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1
	 * </pre>
	 * 
	 * @param fen
	 *            The FEN (Forsyth Edwards Notation)
	 * @param gameType
	 *            The game type.
	 * @return The game.
	 */
	public static final Game createFromFen(String fen, Variant variant) {
		try {
			Game result = null;

			switch (variant) {
			case classic:
			case blitz:
			case lightning:
			case standard:
				result = new ClassicGame();
				result.setHeader(PgnHeader.Variant, variant.name());
				break;
			case wild:
				result = new WildGame();
				break;
			case losers:
				result = new LosersGame();
				break;
			case atomic:
				result = new AtomicGame();
				break;
			case suicide:
				result = new SuicideGame();
				break;
			case fischerRandom:
				result = new FischerRandomGame();
				break;
			case bughouse:
				result = new BughouseGame();
				break;
			case crazyhouse:
				result = new CrazyhouseGame();
				break;
			default:
				throw new IllegalArgumentException("Variant " + variant
						+ " is not supported");
			}

			StringTokenizer tok = new StringTokenizer(fen, " ");
			String boardStr = null;
			String toMoveStr = null;
			String castlingInfoStr = null;
			String epSquareStr = null;
			String fiftyMoveRuleCountStr = null;
			String fullMoveCountStr = null;
			if (tok.hasMoreTokens()) {
				boardStr = tok.nextToken();
			}
			if (tok.hasMoreTokens()) {
				toMoveStr = tok.nextToken();
			}
			if (tok.hasMoreTokens()) {
				castlingInfoStr = tok.nextToken();
			}
			if (tok.hasMoreTokens()) {
				epSquareStr = tok.nextToken();
			}
			if (tok.hasMoreTokens()) {
				fiftyMoveRuleCountStr = tok.nextToken();
			}
			if (tok.hasMoreTokens()) {
				fullMoveCountStr = tok.nextToken();
			}

			java.util.Arrays.fill(result.getBoard(), Piece.EMPTY);
			byte square = 56;
			for (byte i = 0; i < boardStr.length(); i++) {
				char ch = fen.charAt(i);
				if (ch == '/') {
					square -= 16;
				} else if (Character.isDigit(ch)) {
					square += Short.parseShort(String.valueOf(ch));
				} else {
					PieceColor pieceColor = Character.isUpperCase(ch) ? PieceColor.WHITE : PieceColor.BLACK;
					int pieceInt = PIECE_FROM_SAN.indexOf(new String(
							new char[] { ch }).toUpperCase().charAt(0));
                                        Piece piece = GameConstants.PIECES[Character.isUpperCase(ch) ? pieceInt : pieceInt + 6];
					long pieceSquare = GameUtils.getBitboard(GameConstants.SQUARES[square]);

					result.setPieceCount(pieceColor, piece.type, (byte)(result
							.getPieceCount(pieceColor, piece.type) + 1));
					result.getBoard()[square] = piece;
					result.setColorBB(pieceColor, result.getColorBB(pieceColor)
							| pieceSquare);
					result.setOccupiedBB(result.getOccupiedBB() | pieceSquare);
					result.setPieceBB(pieceColor, piece.type, result.getPieceBB(
							pieceColor, piece.type)
							| pieceSquare);
					square++;
				}
			}

			if (toMoveStr != null) {
				result.setColorToMove(toMoveStr.equals("w") ? PieceColor.WHITE : PieceColor.BLACK);
			} else {
				result.setColorToMove(PieceColor.WHITE);
			}

			if (castlingInfoStr != null) {
				boolean whiteCastleKSide = castlingInfoStr.indexOf('K') != -1;
				boolean whiteCastleQSide = castlingInfoStr.indexOf('Q') != -1;
				boolean blackCastleKSide = castlingInfoStr.indexOf('k') != -1;
				boolean blackCastleQSide = castlingInfoStr.indexOf('q') != -1;

				result.setCastling(PieceColor.WHITE,
						whiteCastleKSide && whiteCastleQSide ? CASTLE_BOTH
								: whiteCastleKSide ? CASTLE_SHORT
										: whiteCastleQSide ? CASTLE_LONG
												: CASTLE_NONE);
				result.setCastling(PieceColor.BLACK,
						blackCastleKSide && blackCastleQSide ? CASTLE_BOTH
								: blackCastleKSide ? CASTLE_SHORT
										: blackCastleQSide ? CASTLE_LONG
												: CASTLE_NONE);
			} else {
				result.setCastling(PieceColor.WHITE, CASTLE_NONE);
				result.setCastling(PieceColor.BLACK, CASTLE_NONE);
			}

			if (epSquareStr != null && !epSquareStr.equals("-")) {
				result.setEpSquare(GameUtils.getSquare(epSquareStr));
				result.setInitialEpSquare(result.getEpSquare());
			} else {
				result.setEpSquare(Square.EMPTY);
				result.setInitialEpSquare(Square.EMPTY);
			}

			if (fiftyMoveRuleCountStr != null
					&& !fiftyMoveRuleCountStr.equals("-")) {
				result.setFiftyMoveCount(Short.parseShort(fiftyMoveRuleCountStr));
			}

			if (fullMoveCountStr != null && !fullMoveCountStr.equals("-")) {
				short fullMoveCount = Short.parseShort(fullMoveCountStr);
				result
						.setHalfMoveCount((short)(result.getColorToMove() == PieceColor.BLACK ? fullMoveCount * 2 - 1
								: fullMoveCount * 2 - 2));
			}

			result.setEmptyBB(~result.getOccupiedBB());
			//result.setNotColorToMoveBB(~result.getColorBB(result.getColorToMove()));

			if (!result.isLegalPosition()) {
				throw new IllegalArgumentException(
						"Resulting position was illegal for FEN: " + fen + " "
								+ variant);
			}

			result.setZobristPositionHash(ZobristUtils
					.zobristHashPositionOnly(result));
			result.setZobristGameHash(result.getZobristPositionHash()
					^ ZobristUtils.zobrist(result.getColorToMove(), result
							.getEpSquare(), result.getCastling(PieceColor.WHITE), result
							.getCastling(PieceColor.BLACK)));

			result.incrementRepCount();

			if (Variant.isBughouse(variant)
					|| Variant.isCrazyhouse(Variant.bughouse)) {
				// This wont work if setup from a FEN where promotions have
				// occurred.
				// There is no way of telling if a piece was promoted or not.

				result.setDropCount(PieceColor.WHITE, PieceType.PAWN, (byte)(8 - result.getPieceCount(
						PieceColor.BLACK, PieceType.PAWN)));
				result.setDropCount(PieceColor.WHITE, PieceType.KNIGHT, (byte)(2 - result.getPieceCount(
						PieceColor.BLACK, PieceType.KNIGHT)));
				result.setDropCount(PieceColor.WHITE, PieceType.BISHOP, (byte)(2 - result.getPieceCount(
						PieceColor.BLACK, PieceType.BISHOP)));
				result.setDropCount(PieceColor.WHITE, PieceType.ROOK, (byte)(2 - result.getPieceCount(
						PieceColor.BLACK, PieceType.ROOK)));
				result.setDropCount(PieceColor.WHITE, PieceType.QUEEN, (byte)(1 - result.getPieceCount(
						PieceColor.BLACK, PieceType.QUEEN)));
				result.setDropCount(PieceColor.WHITE, PieceType.KING, (byte)(1 - result.getPieceCount(
						PieceColor.BLACK, PieceType.KING)));

				result.setDropCount(PieceColor.BLACK, PieceType.PAWN, (byte)(8 - result.getPieceCount(
						PieceColor.WHITE, PieceType.PAWN)));
				result.setDropCount(PieceColor.BLACK, PieceType.KNIGHT, (byte)(2 - result.getPieceCount(
						PieceColor.WHITE, PieceType.KNIGHT)));
				result.setDropCount(PieceColor.BLACK, PieceType.BISHOP, (byte)(2 - result.getPieceCount(
						PieceColor.WHITE, PieceType.BISHOP)));
				result.setDropCount(PieceColor.BLACK, PieceType.ROOK, (byte)(2 - result.getPieceCount(
						PieceColor.WHITE, PieceType.ROOK)));
				result.setDropCount(PieceColor.BLACK, PieceType.QUEEN, (byte)(1 - result.getPieceCount(
						PieceColor.WHITE, PieceType.QUEEN)));
				result.setDropCount(PieceColor.BLACK, PieceType.KING, (byte)(1 - result.getPieceCount(
						PieceColor.WHITE, PieceType.KING)));

				// If there are any negative values just set them to 0.
				for (PieceColor color : GameConstants.PIECE_COLORS) {
					for (PieceType piece : GameConstants.PIECE_TYPES) {
						if (result.getDropCount(color, piece) < 0) {
							result.setDropCount(color, piece, (byte)0);
							if (LOG.isWarnEnabled()) {
								LOG.warn("Set a zh drop value to 0 because it was less than 0 initially. " + fen);
							}
						}
					}
				}
			}

			if (result.getVariant() == Variant.fischerRandom) {
				/**
				 * Assume its the starting position. That is the only way to
				 * load a FR game from a fen.
				 */
				((FischerRandomGame) result).initialPositionIsSet();
			} else if (result.getVariant() == Variant.fischerRandomBughouse) {
				/**
				 * Assume its the starting position. That is the only way to
				 * load a FR game from a fen.
				 */
				((FischerRandomBughouseGame) result).initialPositionIsSet();

			} else if (result.getVariant() == Variant.fischerRandomCrazyhouse) {
				/**
				 * Assume its the starting position. That is the only way to
				 * load a FR game from a fen.
				 */
				((FischerRandomCrazyhouseGame) result).initialPositionIsSet();

			}
			return result;
		} catch (Throwable t) {
			throw new RuntimeException("Invalid FEN. Variant=" + variant
					+ " FEN=" + fen, t);
		}
	}

	public static final Game createStartingPosition(Variant variant) {
		if (variant == Variant.suicide) {
			return createFromFen(STARTING_SUICIDE_POSITION_FEN, variant);
		} else if (variant == Variant.fischerRandom) {
			throw new IllegalArgumentException(
					"Creating from a starting position for FischerRandom is not currently supported.");
		} else {
			return createFromFen(STARTING_POSITION_FEN, variant);
		}
	}
}