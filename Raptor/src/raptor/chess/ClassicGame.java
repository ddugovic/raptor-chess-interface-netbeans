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

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.diagonalMove;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getOppositeColor;
import static raptor.chess.util.GameUtils.getString;
import static raptor.chess.util.GameUtils.kingMove;
import static raptor.chess.util.GameUtils.knightMove;
import static raptor.chess.util.GameUtils.moveOne;
import static raptor.chess.util.GameUtils.orthogonalMove;
import static raptor.chess.util.GameUtils.pawnCapture;
import static raptor.chess.util.GameUtils.pawnDoublePush;
import static raptor.chess.util.GameUtils.pawnEpCapture;
import static raptor.chess.util.GameUtils.pawnSinglePush;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnUtils;
import raptor.chess.util.GameUtils;
import raptor.chess.util.SanUtils;
import raptor.chess.util.ZobristUtils;
import raptor.service.EcoService;
import raptor.util.Logger;

/**
 * Implements Classic game rules and provides protected methods so it can easily
 * be subclassed to override behavior for variants.
 * TODO: This class suffers from feature envy (envious of GameUtils.class).
 */
public class ClassicGame implements Game {
	private static final Logger LOG = Logger.getLogger(ClassicGame.class);

	protected Piece[] board = new Piece[64];
	protected byte[] castling = new byte[2];
	protected long[] colorBB = new long[2];
	protected PieceColor colorToMove;

	protected byte[][] dropCounts = new byte[2][7];
	protected long emptyBB;
	protected Square epSquare = Square.EMPTY;
	protected short fiftyMoveCount;
	protected short halfMoveCount;
	protected String id;
	protected Square initialEpSquare = Square.EMPTY;
	protected byte[] moveRepHash = new byte[MOVE_REP_CACHE_SIZE];
	protected MoveList moves = new MoveList();
	//protected long notColorToMoveBB;
	protected long occupiedBB;
	protected Map<PgnHeader, String> pgnHeaderMap = new EnumMap<PgnHeader, String>(PgnHeader.class);
	protected long[][] pieceBB = new long[2][7];
	protected byte[][] pieceCounts = new byte[2][7];
	protected int state;
	protected long zobristGameHash;
	protected long zobristPositionHash;

	public ClassicGame() {
		Arrays.fill(board, Piece.EMPTY);
		setHeader(PgnHeader.Variant, Variant.classic.name());
		setHeader(PgnHeader.Result, Result.ON_GOING.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	public void addState(int state) {
		setState(this.state | state);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean areBothKingsOnBoard() {
		return getPieceBB(PieceColor.WHITE, PieceType.KING) != 0L && getPieceBB(PieceColor.BLACK, PieceType.KING) != 0L;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBlackCastleLong() {
		return (castling[PieceColor.BLACK.index] & CASTLE_LONG) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canBlackCastleShort() {
		return (castling[PieceColor.BLACK.index] & CASTLE_SHORT) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canWhiteCastleLong() {
		return (castling[PieceColor.WHITE.index] & CASTLE_LONG) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean canWhiteCastleShort() {
		return (castling[PieceColor.WHITE.index] & CASTLE_SHORT) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public void clear() {
		board = new Piece[64]; Arrays.fill(board, Piece.EMPTY);
		castling = new byte[2];
		colorBB = new long[2];
		colorToMove = PieceColor.WHITE;
		dropCounts = new byte[2][7];
		emptyBB = 0L;
		epSquare = Square.EMPTY;
		fiftyMoveCount = 0;
		halfMoveCount = 0;
		initialEpSquare = Square.EMPTY;
		zobristGameHash = 0L;
		zobristPositionHash = 0L;
		moveRepHash = new byte[MOVE_REP_CACHE_SIZE];
		//notColorToMoveBB = 0L;
		occupiedBB = 0L;
		pieceBB = new long[2][7];
		pieceCounts = new byte[2][7];
		moves = new MoveList();
		setHeader(PgnHeader.Result, Result.ON_GOING.getDescription());
	}

	/**
	 * {@inheritDoc}
	 */
	public void clearState(int state) {
		setState(this.state & ~state);
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassicGame deepCopy(boolean ignoreHashes) {
		ClassicGame result = new ClassicGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public void forceMove(Move move) {		
		move.setLastWhiteCastlingState(getCastling(PieceColor.WHITE));
		move.setLastBlackCastlingState(getCastling(PieceColor.BLACK));
		setSan(move);
		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			makeEPMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			makeCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			makeDropMove(move);
			break;
		default:
			makeNonEpNonCastlingMove(move);
			break;
		}

		PieceColor oppToMove = getOppositeColor(colorToMove);

		move.setPrevious50MoveCount(fiftyMoveCount);
		if (move.isCapture()) {
			decrementPieceCount(oppToMove, move.getCaptureWithPromoteMask().type);
			incrementDropCount(colorToMove, move.getCaptureWithPromoteMask());
			setFiftyMoveCount((short)0);
		} else if (move.isDrop()) {
			incrementPieceCount(colorToMove, move.getPiece().type);
			decrementDropCount(colorToMove, move.getPiece());
			setFiftyMoveCount((short)0);
		} else if (move.getPiece().type == PieceType.PAWN) {
			setFiftyMoveCount((short)0);
		} else {
			setFiftyMoveCount((short)(fiftyMoveCount + 1));
		}

		setColorToMove(oppToMove);
		//setNotColorToMoveBB(~getColorBB(colorToMove));
		setHalfMoveCount((short)(halfMoveCount + 1));

		moves.append(move);

		updateZobristHash();
		incrementRepCount();
		
		move.setFullMoveCount((halfMoveCount - 1) / 2 + 1);
		move.setHalfMoveCount(halfMoveCount);
		
		if (move.getFullMoveCount() < 19) {
			updateEcoHeaders(move);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PgnHeader[] getAllHeaders() {
		return pgnHeaderMap.keySet().toArray(new PgnHeader[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public PgnHeader[] getAllNonRequiredHeaders() {
		List<PgnHeader> result = new ArrayList<PgnHeader>(pgnHeaderMap.size());
		for (PgnHeader key : pgnHeaderMap.keySet()) {
			if (!key.isRequired()) {
				result.add(key);
			}
		}
		return result.toArray(new PgnHeader[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public Piece[] getBoard() {
		return board;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte getCastling(PieceColor color) {
		return castling[color.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getColorBB(PieceColor color) {
		return colorBB[color.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public PieceColor getColorToMove() {
		return colorToMove;
	}

	/**
	 * {@inheritDoc}
	 */
	public byte getDropCount(PieceColor color, PieceType piece) {
		return dropCounts[color.index][piece.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getEmptyBB() {
		return emptyBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public Square getEpSquare() {
		return epSquare;
	}

	public String getFenCastle() {
		String whiteCastlingFen = getCastling(PieceColor.WHITE) == CASTLE_NONE ? ""
				: getCastling(PieceColor.WHITE) == CASTLE_BOTH ? "KQ"
						: getCastling(PieceColor.WHITE) == CASTLE_SHORT ? "K" : "Q";
		String blackCastlingFen = getCastling(PieceColor.BLACK) == CASTLE_NONE ? ""
				: getCastling(PieceColor.BLACK) == CASTLE_BOTH ? "kq"
						: getCastling(PieceColor.BLACK) == CASTLE_SHORT ? "k" : "q";

		return StringUtils.isBlank(whiteCastlingFen) && StringUtils.isBlank(blackCastlingFen) ? "-"
				: whiteCastlingFen + blackCastlingFen;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFiftyMoveCount() {
		return fiftyMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getFullMoveCount() {
		return halfMoveCount / 2 + 1;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getHalfMoveCount() {
		return halfMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getHeader(PgnHeader header) {
		return pgnHeaderMap.get(header);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getId() {
		return id;
	}

	/**
	 * {@inheritDoc}
	 */
	public Square getInitialEpSquare() {
		return initialEpSquare;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move getLastMove() {
		if (moves.getSize() != 0) {
			return moves.get(moves.getSize() - 1);
		} else {
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public PriorityMoveList<Move> getLegalMoves() {
		PriorityMoveList<Move> result = new PriorityMoveList<Move>();
		PriorityMoveList<Move> pseudoLegals = getPseudoLegalMoves();

		for (Move move : pseudoLegals.getHighPriorityMoves()) {
			forceMove(move);
			if (isLegalPosition()) {
				result.appendHighPriority(move);
			}
			rollback();
		}

		for (Move move : pseudoLegals.getLowPriorityMoves()) {
			forceMove(move);
			if (isLegalPosition()) {
				result.appendLowPriority(move);
			}
			rollback();
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public MoveList getMoveList() {
		return moves;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getNotColorToMoveBB() {
		return ~getColorBB(getColorToMove());
	}

	/**
	 * {@inheritDoc}
	 */
	public long getOccupiedBB() {
		return occupiedBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public Piece getPiece(Square square) {
		return board[square.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public long getPieceBB(PieceColor color, PieceType piece) {
		return pieceBB[color.index][piece.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPieceCount(PieceColor color, PieceType piece) {
		return pieceCounts[color.index][piece.index];
	}

	/**
	 * {@inheritDoc}
	 */
	public int[] getPieceJailCounts(PieceColor color) {
		int totalKings = 0;
		int totalPawns = 0;
		int totalKnights = 0;
		int totalBishops = 0;
		int totalRooks = 0;
		int totalQueens = 0;

		for (byte square = 0; square < board.length; square++) {
			Piece piece = board[square];
			if (piece != Piece.EMPTY
					&& (GameUtils.getBitboard(SQUARES[square]) & getColorBB(color)) != 0) {
				if (piece.promoted) {
					totalPawns++;
				} else {
					switch (piece.type) {
					case PAWN:
						totalPawns++;
						break;
					case KNIGHT:
						totalKnights++;
						break;
					case BISHOP:
						totalBishops++;
						break;
					case ROOK:
						totalRooks++;
						break;
					case QUEEN:
						totalQueens++;
						break;
					case KING:
						totalKings++;
						break;
					}
				}
			}
		}

		// Assume starting position had 8 pawns, 2 knights, etc.
		int[] result = new int[7];
		result[PieceType.PAWN.index] = 8 - totalPawns;
		result[PieceType.KNIGHT.index] = 2 - totalKnights;
		result[PieceType.BISHOP.index] = 2 - totalBishops;
		result[PieceType.ROOK.index] = 2 - totalRooks;
		result[PieceType.QUEEN.index] = 1 - totalQueens;
		result[PieceType.KING.index] = 1 - totalKings;
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public PriorityMoveList<Move> getPseudoLegalMoves() {
		PriorityMoveList<Move> result = new PriorityMoveList<Move>();
		generatePseudoQueenMoves(result);
		generatePseudoKnightMoves(result);
		generatePseudoBishopMoves(result);
		generatePseudoRookMoves(result);
		generatePseudoPawnMoves(result);
		generatePseudoKingMoves(result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRepCount() {
		return moveRepHash[getRepHash()];
	}

	/**
	 * {@inheritDoc}
	 */
	public int getRepHash() {
		// Doesn't consider meta-data such as castling options
		return (int) (zobristPositionHash & MOVE_REP_CACHE_SIZE_MINUS_1);
	}

	/**
	 * {@inheritDoc}
	 */
	public Result getResult() {
		return Result.get(getHeader(PgnHeader.Result));
	}

	/**
	 * {@inheritDoc}
	 */
	public int getState() {
		return state;
	}

	/**
	 * {@inheritDoc}
	 */
	public Variant getVariant() {
		Variant result = Variant.classic;
		String variant = getHeader(PgnHeader.Variant);
		if (StringUtils.isNotBlank(variant)) {
			try {
				result = Variant.valueOf(variant);
			} catch (IllegalArgumentException iae) {
			}
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getZobristGameHash() {
		return zobristGameHash;
	}

	/**
	 * {@inheritDoc}
	 */
	public long getZobristPositionHash() {
		return zobristPositionHash;
	}

	/**
	 * {@inheritDoc}
	 */
	public void incrementRepCount() {
		moveRepHash[getRepHash()]++;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCheckmate() {
		return isCheckmate(getLegalMoves());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCheckmate(PriorityMoveList<Move> moveList) {
		return moveList.isEmpty() && isInCheck(colorToMove);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck() {
		return isInCheck(colorToMove, getPieceBB(colorToMove, PieceType.KING));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck(PieceColor color) {
		return isInCheck(color, getPieceBB(color, PieceType.KING));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInCheck(PieceColor color, long kingBB) {
		Square kingSquare = bitscanForward(kingBB);
		PieceColor oppositeColor = getOppositeColor(color);

		return getPieceBB(oppositeColor, PieceType.KING) != 0L
				&& !(pawnCapture(oppositeColor, getPieceBB(oppositeColor, PieceType.PAWN), kingBB) == 0L
				&& (orthogonalMove(kingSquare, emptyBB, occupiedBB) & (getPieceBB(
						oppositeColor, PieceType.ROOK) | getPieceBB(oppositeColor, PieceType.QUEEN))) == 0L
				&& (diagonalMove(kingSquare, emptyBB, occupiedBB) & (getPieceBB(
						oppositeColor, PieceType.BISHOP) | getPieceBB(oppositeColor,
						PieceType.QUEEN))) == 0L
				&& (kingMove(kingSquare) & getPieceBB(oppositeColor, PieceType.KING)) == 0L && (knightMove(kingSquare) & getPieceBB(
				oppositeColor, PieceType.KNIGHT)) == 0L);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isInState(int state) {
		return (this.state & state) != 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLegalPosition() {
		return areBothKingsOnBoard()
				&& !isInCheck(getOppositeColor(getColorToMove()));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSettingEcoHeaders() {
		return isInState(UPDATING_ECO_HEADERS_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSettingMoveSan() {
		return isInState(UPDATING_SAN_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStalemate() {
		return isStalemate(getLegalMoves());
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isStalemate(PriorityMoveList<Move> moveList) {
		return moveList.isEmpty() && !isInCheck(colorToMove);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isWhitesMove() {
		return colorToMove == PieceColor.WHITE;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeDropMove(Piece piece, Square destination)
			throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not supported in classical");
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeLanMove(String lan) throws IllegalArgumentException {
		Move move = null;

		PriorityMoveList<Move> legals = getLegalMoves();

		for (Move candidate : legals) {
			if (candidate.getLan().equals(lan)) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: " + lan + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(Square startSquare, Square endSquare)
			throws IllegalArgumentException {
		Move move = null;

		for (Move pseudoLegal : getPseudoLegalMoves()) {
			if (pseudoLegal.getFrom() == startSquare
					&& pseudoLegal.getTo() == endSquare) {
				try {
					forceMove(pseudoLegal);
					if (!isLegalPosition()) {
						throw new IllegalArgumentException("Invalid move (Illegal position): "
							+ GameUtils.getSan(startSquare) + " " + GameUtils.getSan(endSquare) + " \n"
							+ toString());
					}
					move = pseudoLegal;
				} finally {
					rollback();
				}
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ GameUtils.getSan(startSquare) + " " + GameUtils.getSan(endSquare) + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Makes a move using the start/end square and the specified promotion
	 * piece.
	 * 
	 * @param startSquare
	 *            The start square.
	 * @param endSquare
	 *            The end square.
	 * @param promotionPiece
	 *            The non colored piece constant representing the promoted
	 *            piece.
	 * @return The move made.
	 * @throws IllegalArgumentException
	 *             Thrown if the move is illegal.
	 */
	public Move makeMove(Square startSquare, Square endSquare, PieceType promotePiece)
			throws IllegalArgumentException {
		Move move = null;

		for (Move pseudoLegal : getPseudoLegalMoves()) {
			if (pseudoLegal.getFrom() == startSquare
					&& pseudoLegal.getTo() == endSquare
					&& pseudoLegal.getPiecePromotedTo() == promotePiece) {
				try {
					forceMove(pseudoLegal);
					if (isLegalPosition()) {
						move = pseudoLegal;
						break;
					}
				} finally {
					rollback();
				}
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ GameUtils.getSan(startSquare) + "-" + GameUtils.getSan(endSquare) + "="
					+ promotePiece.ch + "\n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * {@inheritDoc}
	 */
	public Move makeSanMove(String shortAlgebraic)
			throws IllegalArgumentException {
		SanUtils.SanValidations validations = SanUtils.getValidations(shortAlgebraic);
		List<Move> pseudoLegals = new ArrayList<Move>(getPseudoLegalMoves().asList());
		
		Move result = makeSanMoveOverride(shortAlgebraic, validations, pseudoLegals);
		if (result == null) {
			// Examples:
			// e4 (a pawn move to e4).
			// e8=Q (a pawn promotion without a capture).
			// de=Q (a pawn promotion from a capture).
			// ed (e pawn captures d pawn).
			// Ne3 (a Knight moving to e3).
			// N5e3 (disambiguity for two knights which can move to e3, the 5th
			// rank
			// knight is the one that should move).
			// Nfe3 (disambiguity for two knights which can move to e3, the
			// knight
			// on the f file is the one that should move).
			// Nf1e3 (disambiguity for three knights which cam move to e3, the
			// f1
			// knight is the one that should move).
			if (!validations.isValidStrict()) {
				throw new IllegalArgumentException("Invalid short algebraic: "
						+ shortAlgebraic);
			}

			PieceType candidatePromotedPiece = PieceType.EMPTY;

			if (validations.isCastleKSideStrict()) {
				for (Move move : pseudoLegals) {
					if (move != null
							&& (move.getMoveCharacteristic() & Move.SHORT_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else if (validations.isCastleQSideStrict()) {
				for (Move move : pseudoLegals) {
					if (move != null
							&& (move.getMoveCharacteristic() & Move.LONG_CASTLING_CHARACTERISTIC) != 0) {
						result = move;
						break;
					}
				}
			} else {
				MoveList matches = new MoveList(10);
				if (validations.isPromotion()) {
					char pieceChar = validations.getStrictSan().charAt(
							validations.getStrictSan().length() - 1);
					candidatePromotedPiece = SanUtils.sanToPiece(pieceChar);
				}

				if (validations.isPawnMove()) {
					PieceType candidatePieceMoving = PieceType.PAWN;
					if (validations.isEpOrAmbigPxStrict()
							|| validations.isAmbigPxPromotionStrict()) {

						Square end = Square.getSquare((byte)GameConstants.RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(2)),
								(byte)GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));

						byte startRank = (byte)(end.rank
								+ (colorToMove == PieceColor.WHITE ? -1 : +1));

						if (startRank > 7 || startRank < 0) {
							throw new IllegalArgumentException(
									"Invalid short algebraic: "
											+ shortAlgebraic);
						}

						Square start = Square.getSquare(startRank,
								(byte)GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(0)));

						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece().type == candidatePieceMoving
									&& move.isCapture()
									&& move.getFrom() == start
									&& move.getTo() == end
									&& move.getPiecePromotedTo() == candidatePromotedPiece) {
								matches.append(move);
							}
						}
					} else {
						// handle captures
						if (validations.isPxStrict()
								|| validations.isPxPPromotionStrict()) {
							byte startFile = (byte)GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											0));
							byte endFile = (byte)GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											1));

							for (Move move : pseudoLegals) {
								if (move != null
										&& move.getPiece().type == candidatePieceMoving
										&& move.getFrom().file == startFile
										&& move.getTo().file == endFile
										&& move.isCapture()
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
						// handle non captures.
						else {
							Square end = Square.getSquare((byte)GameConstants.RANK_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											1)), (byte)GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan().charAt(
											0)));

							for (Move move : pseudoLegals) {
								if (move != null
										&& move.getPiece().type == candidatePieceMoving
										&& !move.isCapture()
										&& move.getTo() == end
										&& move.getPiecePromotedTo() == candidatePromotedPiece) {
									matches.append(move);
								}
							}
						}
					}
				} else {
					PieceType candidatePieceMoving = SanUtils.sanToPiece(validations
							.getStrictSan().charAt(0));
					Square end = Square.getSquare((byte)GameConstants.RANK_FROM_SAN
							.indexOf(validations.getStrictSan().charAt(
									validations.getStrictSan().length() - 1)),
							(byte)GameConstants.FILE_FROM_SAN
									.indexOf(validations.getStrictSan()
											.charAt(
													validations.getStrictSan()
															.length() - 2)));

					if (validations.isDisambigPieceRankStrict()) {
						byte startRank = (byte)RANK_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece().type == candidatePieceMoving
									&& move.getTo() == end
									&& move.getFrom().rank == startRank) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceFileStrict()) {
						byte startFile = (byte)FILE_FROM_SAN.indexOf(validations
								.getStrictSan().charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece().type == candidatePieceMoving
									&& move.getTo() == end
									&& move.getFrom().file == startFile) {
								matches.append(move);
							}
						}
					} else if (validations.isDisambigPieceRankFileStrict()) {
						Square startSquare = Square.getSquare((byte)GameConstants.RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(2)),
								(byte)GameConstants.FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(1)));
						FILE_FROM_SAN.indexOf(validations.getStrictSan()
								.charAt(1));
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece().type == candidatePieceMoving
									&& move.getTo() == end
									&& move.getFrom() == startSquare) {
								matches.append(move);
							}
						}
					} else {
						for (Move move : pseudoLegals) {
							if (move != null
									&& move.getPiece().type == candidatePieceMoving
									&& move.getTo() == end) {
								matches.append(move);
							}
						}
					}
				}
				result = testForSanDisambiguationFromCheck(shortAlgebraic,
						matches);
			}
		}

		if (result == null) {
			throw new IllegalArgumentException("Illegal move " + shortAlgebraic
					+ "\n " + toString());
		}

		result.setSan(shortAlgebraic);
		if (!move(result)) {
			throw new IllegalArgumentException("Illegal move: " + result);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean move(Move move) {
		// first make the move.
		forceMove(move);
		if (!isLegalPosition()) {
			rollback();
			return false;
		}
		return true;
	}

	/**
	 * Copys the information from this game into the passed in game.
	 */
	public void overwrite(Game game, boolean ignoreHashes) {
		ClassicGame gameToOverwrite = (ClassicGame) game;
		gameToOverwrite.id = id;
		gameToOverwrite.state = state;
		gameToOverwrite.pgnHeaderMap = new EnumMap<PgnHeader, String>(pgnHeaderMap);

		gameToOverwrite.moves = moves.deepCopy();
		gameToOverwrite.halfMoveCount = halfMoveCount;
		System.arraycopy(colorBB, 0, gameToOverwrite.colorBB, 0,
				gameToOverwrite.colorBB.length);
		for (int i = 0; i < pieceBB.length; i++) {
			System.arraycopy(pieceBB[i], 0, gameToOverwrite.pieceBB[i], 0,
					pieceBB[i].length);
		}
		System.arraycopy(board, 0, gameToOverwrite.board, 0, board.length);
		gameToOverwrite.occupiedBB = occupiedBB;
		gameToOverwrite.emptyBB = emptyBB;
		//gameToOverwrite.notColorToMoveBB = notColorToMoveBB;
		System.arraycopy(castling, 0, gameToOverwrite.castling, 0,
				castling.length);
		gameToOverwrite.initialEpSquare = initialEpSquare;
		gameToOverwrite.epSquare = epSquare;
		gameToOverwrite.colorToMove = colorToMove;
		gameToOverwrite.fiftyMoveCount = fiftyMoveCount;
		for (int i = 0; i < pieceCounts.length; i++) {
			System.arraycopy(pieceCounts[i], 0, gameToOverwrite.pieceCounts[i],
					0, pieceCounts[i].length);
		}
		for (int i = 0; i < dropCounts.length; i++) {
			System.arraycopy(dropCounts[i], 0, gameToOverwrite.dropCounts[i],
					0, dropCounts[i].length);
		}
		gameToOverwrite.zobristPositionHash = zobristPositionHash;
		gameToOverwrite.zobristGameHash = zobristGameHash;

		if (!ignoreHashes) {
			System.arraycopy(moveRepHash, 0, gameToOverwrite.moveRepHash, 0,
					moveRepHash.length);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeHeader(PgnHeader headerName) {
		pgnHeaderMap.remove(headerName);
	}

	/**
	 * {@inheritDoc}
	 */
	public void rollback() {
		Move move = moves.removeLast();
		decrementRepCount();

		switch (move.getMoveCharacteristic()) {
		case Move.EN_PASSANT_CHARACTERISTIC:
			rollbackEpMove(move);
			break;
		case Move.SHORT_CASTLING_CHARACTERISTIC:
		case Move.LONG_CASTLING_CHARACTERISTIC:
			rollbackCastlingMove(move);
			break;
		case Move.DROP_CHARACTERISTIC:
			rollbackDropMove(move);
			break;
		default:
			rollbackNonEpNonCastlingMove(move);
			break;
		}

		PieceColor oppositeToMove = getOppositeColor(colorToMove);

		if (move.isCapture()) {
			incrementPieceCount(colorToMove, move
					.getCaptureWithPromoteMask().type);
			decrementDropCount(oppositeToMove, move.getCaptureWithPromoteMask());
		} else if (move.isDrop()) {
			decrementPieceCount(oppositeToMove, move.getPiece().type);
			incrementDropCount(oppositeToMove, move.getPiece());
		}

		setColorToMove(oppositeToMove);
		//setNotColorToMoveBB(~getColorBB(colorToMove));
		setHalfMoveCount((short)(halfMoveCount - 1));

		setFiftyMoveCount(move.getPrevious50MoveCount());
		setCastling(PieceColor.WHITE, move.getLastWhiteCastlingState());
		setCastling(PieceColor.BLACK, move.getLastBlackCastlingState());

		updateZobristHash();

		rollbackEcoHeaders(move);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setBoard(Piece[] board) {
		this.board = board;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCastling(PieceColor color, byte castling) {
		this.castling[color.index] = castling;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setColorBB(PieceColor color, long bb) {
		colorBB[color.index] = bb;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setColorToMove(PieceColor color) {
		colorToMove = color;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDropCount(PieceColor color, PieceType piece, byte count) {
//		if ((piece & PROMOTED_MASK) != 0) {
//			piece = PAWN;
//		}
		dropCounts[color.index][piece.index] = count;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEmptyBB(long emptyBB) {
		this.emptyBB = emptyBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEpSquare(Square epSquare) {
		this.epSquare = epSquare;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setFiftyMoveCount(short fiftyMoveCount) {
		this.fiftyMoveCount = fiftyMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHalfMoveCount(short halfMoveCount) {
		this.halfMoveCount = halfMoveCount;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setHeader(PgnHeader header, String value) {
		pgnHeaderMap.put(header, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setInitialEpSquare(Square initialEpSquare) {
		this.initialEpSquare = initialEpSquare;
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	public void setNotColorToMoveBB(long notColorToMoveBB) {
//		//this.notColorToMoveBB = notColorToMoveBB;
//	}

	/**
	 * {@inheritDoc}
	 */
	public void setOccupiedBB(long occupiedBB) {
		this.occupiedBB = occupiedBB;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPiece(Square square, Piece piece) {
		board[square.index] = piece;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPieceBB(PieceColor color, PieceType piece, long bb) {
		pieceBB[color.index][piece.index] = bb;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPieceCount(PieceColor color, PieceType piece, byte count) {
		pieceCounts[color.index][piece.index] = count;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setZobristGameHash(long hash) {
		zobristGameHash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setZobristPositionHash(long hash) {
		zobristPositionHash = hash;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toFen() {
		// 

		StringBuilder result = new StringBuilder(77);
		result.append(toFenPosition());

		result.append(isWhitesMove() ? " w" : " b");

		result.append(' ').append(getFenCastle());
		result.append(' ').append(GameUtils.getSan(getEpSquare()));
		result.append(' ').append(getFiftyMoveCount());
		result.append(' ').append(getFullMoveCount());

		if (LOG.isDebugEnabled()) {
			LOG.debug(result);
		}
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toFenPosition() {
		StringBuilder result = new StringBuilder(77);
		for (byte rank = 7; rank > -1; rank--) {
			int consecutiveEmpty = 0;
			for (byte file = 0; file < 8; file++) {
				Square square = Square.getSquare(rank, file);
				Piece piece = getPiece(square);
				if (piece == Piece.EMPTY) {
					consecutiveEmpty++;
				} else {
					if (consecutiveEmpty > 0) {
						result.append(consecutiveEmpty);
						consecutiveEmpty = 0;
					}
					char ch = GameUtils.getPieceRepresentation(piece, false);
					result.append(piece.color == PieceColor.WHITE ? ch : Character.toLowerCase(ch));
				}
			}
			if (consecutiveEmpty > 0)
				result.append(consecutiveEmpty);
			if (rank > 0)
				result.append("/");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug(result);
		}
		return result.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toPgn() {
		StringBuilder builder = new StringBuilder(2500);

		// Set all of the required headers.
		for (PgnHeader requiredHeader : PgnHeader.REQUIRED_HEADERS) {
			String headerValue = getHeader(requiredHeader);
			if (StringUtils.isBlank(headerValue)) {
				headerValue = PgnHeader.UNKNOWN_VALUE;
				setHeader(requiredHeader, headerValue);
			}
		}

		List<PgnHeader> pgnHeaders = new ArrayList<PgnHeader>(pgnHeaderMap.keySet());
		Collections.sort(pgnHeaders);

		for (PgnHeader header : pgnHeaders) {
			PgnUtils.getHeaderLine(builder, header.name(), getHeader(header));
			builder.append('\n');
		}
		builder.append('\n');

		boolean nextMoveRequiresNumber = true;
		int charsInCurrentLine = 0;

		// TO DO: add breaking up lines in comments.
		for (int i = 0; i < halfMoveCount; i++) {
			int charsBefore = builder.length();
			nextMoveRequiresNumber = PgnUtils.getMove(builder, moves
					.get(i), nextMoveRequiresNumber);
			charsInCurrentLine += builder.length() - charsBefore;

			if (charsInCurrentLine > 75) {
				charsInCurrentLine = 0;
				builder.append('\n');
			} else {
				builder.append(' ');
			}
		}

		builder.append(getResult().getDescription());
		if (LOG.isDebugEnabled()) {
			LOG.debug(builder);
		}
		return builder.toString();
	}

	/**
	 * Returns a dump of the game class suitable for debugging. Quite a lot of
	 * information is produced and its an expensive operation, use with care.
	 */
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder(1000);

		result.append(getString(new String[]{"emptyBB", "occupiedBB",
			"notColorToMoveBB", "color[WHITE]", "color[BLACK]"},
			new long[]{emptyBB, occupiedBB, getNotColorToMoveBB(),
				getColorBB(PieceColor.WHITE), getColorBB(PieceColor.BLACK)})).append("\n\n");

		result.append(getString(new String[]{"[WHITE][PAWN]",
			"[WHITE][KNIGHT]", "[WHITE][BISHOP]", "[WHITE][ROOK]",
			"[WHITE][QUEEN]", "[WHITE][KING]"}, new long[]{
				getPieceBB(PieceColor.WHITE, PieceType.PAWN), getPieceBB(PieceColor.WHITE, PieceType.KNIGHT),
				getPieceBB(PieceColor.WHITE, PieceType.BISHOP), getPieceBB(PieceColor.WHITE, PieceType.ROOK),
				getPieceBB(PieceColor.WHITE, PieceType.QUEEN), getPieceBB(PieceColor.WHITE, PieceType.KING)})).append("\n\n");

		result.append(getString(new String[]{"[BLACK][PAWN]",
			"[BLACK][KNIGHT]", "[BLACK][BISHOP]", "[BLACK][ROOK]",
			"[BLACK][QUEEN]", "[BLACK][KING]"}, new long[]{
				getPieceBB(PieceColor.BLACK, PieceType.PAWN), getPieceBB(PieceColor.BLACK, PieceType.KNIGHT),
				getPieceBB(PieceColor.BLACK, PieceType.BISHOP), getPieceBB(PieceColor.BLACK, PieceType.ROOK),
				getPieceBB(PieceColor.BLACK, PieceType.QUEEN), getPieceBB(PieceColor.BLACK, PieceType.KING)})).append("\n\n");

		for (byte rank = 7; rank > -1; rank--) {
			for (byte file = 0; file < 8; file++) {
				Square square = Square.getSquare(rank, file);
				Piece piece = getPiece(square);
				char ch = GameUtils.getPieceRepresentation(piece, false);
				result.append("|").append(piece.color == PieceColor.WHITE ? ch : Character.toLowerCase(ch));
			}
			result.append("|   ");

			switch (rank) {
			case 7:
				result.append("To Move: ").append(colorToMove.description).append(" " + "Last Move: ").append(moves.getSize() == 0 ? "" : moves.getLast());
				break;
			case 6:
				result.append(getPieceCountsString());
				break;
			case 5:
				result.append("Moves: ").append(halfMoveCount).append(" EP: ").append(GameUtils.getSan(epSquare)).append(" Castle: ").append(getFenCastle());
				break;
			case 4:
				result.append("FEN: ").append(toFen());
				break;
			case 3:
				result.append("State: ").append(state).append(" Variant=").append(getHeader(PgnHeader.Variant)).append(" Result=").append(getResult());
				break;
			case 2:
				result.append("Event: ").append(getHeader(PgnHeader.Event)).append(" Site=").append(getHeader(PgnHeader.Site)).append(" Date=").append(getHeader(PgnHeader.Date));
				break;
			case 1:
				result.append("WhiteName: ").append(getHeader(PgnHeader.White)).append(" BlackName=").append(getHeader(PgnHeader.Black)).append(" WhiteTime=").append(getHeader(PgnHeader.WhiteRemainingMillis)).append(" whiteLag=").append(getHeader(PgnHeader.WhiteLagMillis)).append(" BlackTime=").append(getHeader(PgnHeader.BlackRemainingMillis)).append(" blackLag=").append(getHeader(PgnHeader.BlackLagMillis));
				break;
			default:
				result.append("initialWhiteClock: ").append(getHeader(PgnHeader.WhiteClock)).append(" initialBlackClocks=").append(getHeader(PgnHeader.BlackClock));
				break;
			}
			result.append('\n');
		}

		String legalMovesString = Arrays.toString(getLegalMoves().asList().toArray(new Move[0]));
		result.append('\n');
		result.append(WordUtils.wrap("\nLegals=" + legalMovesString, 80, "\n", true));
		result.append(WordUtils.wrap("\nMovelist=" + moves, 80, "\n", true));

		List<String> squaresWithPromoteMasks = new LinkedList<String>();
		for (byte square = 0; square < board.length; square++) {
			if (getPiece(SQUARES[square]).promoted) {
				squaresWithPromoteMasks.add(GameUtils.getSan(SQUARES[square]));
			}
		}
		result.append("\nSquares with promote masks: ").append(squaresWithPromoteMasks);
		return result.toString();
	}

	/**
	 * Currently places captures and promotions ahead of non captures.
	 */
	protected void addMove(Move move, PriorityMoveList<Move> moves) {
		if (move.isCapture() || move.isPromotion()) {
			moves.appendHighPriority(move);
		} else {
			moves.appendLowPriority(move);
		}
	}

	/**
	 * Decrements the drop count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	protected void decrementDropCount(PieceColor color, Piece piece) {
		if (piece.promoted) {
			dropCounts[color.index][PieceType.PAWN.index]--;
		} else {
			dropCounts[color.index][piece.type.index]--;
		}
	}

	/**
	 * Decrements the piece count for the specified piece. This method handles
	 * promotion masks as well.
	 * 
	 * @param color
	 *            WHITE or BLACK.
	 * @param piece
	 *            The un-colored piece constant.
	 */
	protected void decrementPieceCount(PieceColor color, PieceType piece) {
//		if ((piece & PROMOTED_MASK) != 0) {
//			piece &= NOT_PROMOTED_MASK;
//		}
		pieceCounts[color.index][piece.index]--;
	}

	/**
	 * Decrements the current positions repetition count.
	 */
	protected void decrementRepCount() {
		moveRepHash[getRepHash()]--;
	}

	/**
	 * Generates all of the pseudo legal bishop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoBishopMoves(PriorityMoveList<Move> moves) {
		long fromBB = getPieceBB(colorToMove, PieceType.BISHOP);

		while (fromBB != 0) {
			Square fromSquare = bitscanForward(fromBB);

			long toBB = diagonalMove(fromSquare, emptyBB, occupiedBB)
					& getNotColorToMoveBB();

			while (toBB != 0) {
				Square toSquare = bitscanForward(toBB);

				Piece capture = getPiece(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPiece(fromSquare), colorToMove,
						capture), moves);
				toBB = bitscanClear(toBB);
			}
			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal king castling moves in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList<Move> moves) {
		// The king destination square isnt checked, its checked when legal
		// getMoves() are checked.

		if (colorToMove == PieceColor.WHITE
				&& (getCastling(colorToMove) & CASTLE_SHORT) != 0
				&& fromBB == Square.E1.bit && getPiece(Square.G1) == Piece.EMPTY
				&& GameUtils.isWhitePiece(this, Square.H1)
				&& getPiece(Square.H1).type == PieceType.ROOK && getPiece(Square.F1) == Piece.EMPTY
				&& !isInCheck(PieceColor.WHITE, Square.E1.bit) && !isInCheck(PieceColor.WHITE, Square.F1.bit)) {
			moves
					.appendLowPriority(new Move(Square.E1, Square.G1, Piece.WK,
                            colorToMove, Piece.EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (colorToMove == PieceColor.WHITE
				&& (getCastling(colorToMove) & CASTLE_LONG) != 0
				&& fromBB == Square.E1.bit && GameUtils.isWhitePiece(this, Square.A1)
				&& getPiece(Square.A1).type == PieceType.ROOK && getPiece(Square.D1) == Piece.EMPTY
				&& getPiece(Square.C1) == Piece.EMPTY && getPiece(Square.B1) == Piece.EMPTY
				&& !isInCheck(PieceColor.WHITE, Square.E1.bit) && !isInCheck(PieceColor.WHITE, Square.D1.bit)) {
			moves
					.appendLowPriority(new Move(Square.E1, Square.C1, Piece.WK,
                            colorToMove, Piece.EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (colorToMove == PieceColor.BLACK
				&& (getCastling(colorToMove) & CASTLE_SHORT) != 0
				&& fromBB == Square.E8.bit && !GameUtils.isWhitePiece(this, Square.H8)
				&& getPiece(Square.H8).type == PieceType.ROOK && getPiece(Square.G8) == Piece.EMPTY
				&& getPiece(Square.F8) == Piece.EMPTY && !isInCheck(PieceColor.BLACK, Square.E8.bit)
				&& !isInCheck(PieceColor.BLACK, Square.F8.bit)) {
			moves
					.appendLowPriority(new Move(Square.E8, Square.G8, Piece.BK,
                            colorToMove, Piece.EMPTY,
							Move.SHORT_CASTLING_CHARACTERISTIC));

		}

		if (colorToMove == PieceColor.BLACK
				&& (getCastling(colorToMove) & CASTLE_LONG) != 0
				&& !GameUtils.isWhitePiece(this, Square.A8)
				&& getPiece(Square.A8).type == PieceType.ROOK && fromBB == Square.E8.bit
				&& getPiece(Square.D8) == Piece.EMPTY && getPiece(Square.C8) == Piece.EMPTY
				&& getPiece(Square.B8) == Piece.EMPTY && !isInCheck(PieceColor.BLACK, Square.E8.bit)
				&& !isInCheck(PieceColor.BLACK, Square.D8.bit)) {
			moves
					.appendLowPriority(new Move(Square.E8, Square.C8, Piece.BK,
                            colorToMove, Piece.EMPTY,
							Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Generates all of the pseudo legal king moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKingMoves(PriorityMoveList<Move> moves) {
		long fromBB = getPieceBB(colorToMove, PieceType.KING);
		Square fromSquare = bitscanForward(fromBB);
		long toBB = kingMove(fromSquare) & getNotColorToMoveBB();

		generatePseudoKingCastlingMoves(fromBB, moves);

		while (toBB != 0) {
			Square toSquare = bitscanForward(toBB);

			Piece piece = isWhitesMove() ? Piece.WK : Piece.BK;
			Piece capture = getPiece(toSquare);

			addMove(new Move(fromSquare, toSquare, piece, colorToMove,
					capture), moves);
			toBB = bitscanClear(toBB);
			toSquare = bitscanForward(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal knight moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoKnightMoves(PriorityMoveList<Move> moves) {

		long fromBB = getPieceBB(colorToMove, PieceType.KNIGHT);

		while (fromBB != 0) {
			Square fromSquare = bitscanForward(fromBB);

			long toBB = knightMove(fromSquare) & getNotColorToMoveBB();

			while (toBB != 0) {
				Square toSquare = bitscanForward(toBB);
				Piece capture = getPiece(toSquare);

				addMove(new Move(fromSquare, toSquare,
						getPiece(fromSquare), colorToMove,
						capture), moves);

				toBB = bitscanClear(toBB);
				toSquare = bitscanForward(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal pawn captures in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnCaptures(Square fromSquare, long fromBB, PieceColor oppositeColor, PriorityMoveList<Move> moves) {

		Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
		long toBB = pawnCapture(colorToMove, fromBB, getColorBB(oppositeColor));

		while (toBB != 0L) {
			Square toSquare = bitscanForward(toBB);
			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						getPiece(toSquare), PieceType.KNIGHT,
						Square.EMPTY, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						getPiece(toSquare), PieceType.BISHOP,
						Square.EMPTY, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						getPiece(toSquare), PieceType.QUEEN, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						getPiece(toSquare), PieceType.ROOK, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						getPiece(toSquare)), moves);
			}
			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal double pawn pushes in the position and
	 * adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnDoublePush(Square fromSquare, long fromBB,
			PieceColor oppositeColor, int epModifier, PriorityMoveList<Move> moves) {

		Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
		long toBB = pawnDoublePush(colorToMove, fromBB, emptyBB);

		while (toBB != 0) {
			Square toSquare = bitscanForward(toBB);
			addMove(new Move(fromSquare, toSquare, piece, colorToMove,
					Piece.EMPTY, PieceType.EMPTY, SQUARES[toSquare.index + epModifier],
					Move.DOUBLE_PAWN_PUSH_CHARACTERISTIC), moves);
			toBB = bitscanClear(toBB);
		}

	}

	/**
	 * Generates all of the pseudo En-Passant moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnEPCaptures(Square fromSquare, long fromBB,
			PieceColor oppositeColor, PriorityMoveList<Move> moves) {
		if (epSquare != Square.EMPTY) {

			Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
			long toBB = pawnEpCapture(colorToMove, fromBB, getPieceBB(
					oppositeColor, PieceType.PAWN), getBitboard(epSquare));

			if (toBB != 0) {
				Square toSquare = bitscanForward(toBB);

				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						piece, PieceType.EMPTY, Square.EMPTY,
						Move.EN_PASSANT_CHARACTERISTIC), moves);
			}
		}
	}

	/**
	 * Generates all of the pseudo legal pawn moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnMoves(PriorityMoveList<Move> moves) {
		long pawnsBB = getPieceBB(colorToMove, PieceType.PAWN);
		PieceColor oppositeColor;
		int epModifier;

		if (colorToMove == PieceColor.WHITE) {
			oppositeColor = PieceColor.BLACK;
			epModifier = -8;
		} else {
			oppositeColor = PieceColor.WHITE;
			epModifier = +8;
		}

		while (pawnsBB != 0) {
			Square fromSquare = bitscanForward(pawnsBB);
			long fromBB = getBitboard(fromSquare);

			generatePseudoPawnEPCaptures(fromSquare, fromBB, oppositeColor, moves);
			generatePseudoPawnCaptures(fromSquare, fromBB, oppositeColor, moves);
			generatePseudoPawnSinglePush(fromSquare, fromBB, oppositeColor, moves);
			generatePseudoPawnDoublePush(fromSquare, fromBB, oppositeColor, epModifier, moves);

			pawnsBB = bitscanClear(pawnsBB);
		}
	}

	/**
	 * Generates all of the pseudo legal single push pawn moves in the position
	 * and adds them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoPawnSinglePush(Square fromSquare, long fromBB,
			PieceColor oppositeColor, PriorityMoveList<Move> moves) {

		Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
		long toBB = pawnSinglePush(colorToMove, fromBB, emptyBB);

		while (toBB != 0) {
			Square toSquare = bitscanForward(toBB);

			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						Piece.EMPTY, PieceType.KNIGHT, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						Piece.EMPTY, PieceType.BISHOP, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						Piece.EMPTY, PieceType.QUEEN, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						Piece.EMPTY, PieceType.ROOK, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, piece, colorToMove,
						Piece.EMPTY), moves);
			}

			toBB = bitscanClear(toBB);
		}
	}

	/**
	 * Generates all of the pseudo legal queen moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoQueenMoves(PriorityMoveList<Move> moves) {
		long fromBB = getPieceBB(colorToMove, PieceType.QUEEN);

		while (fromBB != 0) {
			Square fromSquare = bitscanForward(fromBB);

			long toBB = (orthogonalMove(fromSquare, emptyBB,
                    occupiedBB) | diagonalMove(fromSquare, emptyBB,
                    occupiedBB))
					& getNotColorToMoveBB();

			while (toBB != 0) {
				Square toSquare = bitscanForward(toBB);

				Piece capture = getPiece(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPiece(fromSquare), colorToMove,
						capture), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	/**
	 * Generates all of the pseudo legal rook moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoRookMoves(PriorityMoveList<Move> moves) {
		long fromBB = getPieceBB(colorToMove, PieceType.ROOK);

		while (fromBB != 0) {
			Square fromSquare = bitscanForward(fromBB);

			long toBB = orthogonalMove(fromSquare, emptyBB, occupiedBB)
					& getNotColorToMoveBB();

			while (toBB != 0) {
				Square toSquare = bitscanForward(toBB);

				Piece capture = getPiece(toSquare);
				addMove(new Move(fromSquare, toSquare,
						getPiece(fromSquare), colorToMove,
						capture), moves);
				toBB = bitscanClear(toBB);
			}

			fromBB = bitscanClear(fromBB);
		}
	}

	protected String getDropCountsString() {
		return "Drop counts [WP=" + getDropCount(PieceColor.WHITE, PieceType.PAWN) + " WN="
				+ getDropCount(PieceColor.WHITE, PieceType.KNIGHT) + " WB="
				+ getDropCount(PieceColor.WHITE, PieceType.BISHOP) + " WR="
				+ getDropCount(PieceColor.WHITE, PieceType.ROOK) + " WQ="
				+ getDropCount(PieceColor.WHITE, PieceType.QUEEN) + " WK="
				+ getDropCount(PieceColor.WHITE, PieceType.KING) + "][BP="
				+ getDropCount(PieceColor.BLACK, PieceType.PAWN) + " BN="
				+ getDropCount(PieceColor.BLACK, PieceType.KNIGHT) + " BB="
				+ getDropCount(PieceColor.BLACK, PieceType.BISHOP) + " BR="
				+ getDropCount(PieceColor.BLACK, PieceType.ROOK) + " BQ="
				+ getDropCount(PieceColor.BLACK, PieceType.QUEEN) + " BK="
				+ getDropCount(PieceColor.BLACK, PieceType.KING) + "]";
	}

	protected String getPieceCountsString() {
		return "Piece counts [WP=" + getPieceCount(PieceColor.WHITE, PieceType.PAWN) + " WN="
				+ getPieceCount(PieceColor.WHITE, PieceType.KNIGHT) + " WB="
				+ getPieceCount(PieceColor.WHITE, PieceType.BISHOP) + " WR="
				+ getPieceCount(PieceColor.WHITE, PieceType.ROOK) + " WQ="
				+ getPieceCount(PieceColor.WHITE, PieceType.QUEEN) + " WK="
				+ getPieceCount(PieceColor.WHITE, PieceType.KING) + "][BP="
				+ getPieceCount(PieceColor.BLACK, PieceType.PAWN) + " BN="
				+ getPieceCount(PieceColor.BLACK, PieceType.KNIGHT) + " BB="
				+ getPieceCount(PieceColor.BLACK, PieceType.BISHOP) + " BR="
				+ getPieceCount(PieceColor.BLACK, PieceType.ROOK) + " BQ="
				+ getPieceCount(PieceColor.BLACK, PieceType.QUEEN) + " BK="
				+ getPieceCount(PieceColor.BLACK, PieceType.KING) + "]";
	}

	/**
	 * Returns true if the specified color has at least one drop piece.
	 * 
	 * @param color
	 *            THe color to check,
	 * @return True if the color has a drop piece, otherwise false.
	 */
	protected boolean hasNonPawnDropPiece(PieceColor color) {
		boolean result = false;
		for (int i = 2; i < dropCounts[color.index].length; i++) {
			if (dropCounts[color.index][i] > 0) {
				result = true;
				break;
			}
		}
		return result;
	}

	/**
	 * Increments the drop count. This method handles incrementing pieces with a
	 * promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	protected void incrementDropCount(PieceColor color, Piece piece) {
		if (piece.promoted) {
			dropCounts[color.index][PieceType.PAWN.index]++;
		} else {
			dropCounts[color.index][piece.type.index]++;
		}
	}

	/**
	 * Increments the piece count. This method handles incrementing pieces with
	 * a promote mask.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The uncolored piece constant.
	 */
	protected void incrementPieceCount(PieceColor color, PieceType piece) {
//		if ((piece & PROMOTED_MASK) != 0) {
//			piece &= NOT_PROMOTED_MASK;
//		}
		pieceCounts[color.index][piece.index]++;
	}

	protected void makeCastlingMove(Move move) {
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == PieceColor.WHITE) {
			kingFromBB = Square.E1.bit;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = Square.G1.bit;
				rookFromBB = Square.H1.bit;
				rookToBB = Square.F1.bit;
				updateZobristPOCastleKsideWhite();
			} else {
				kingToBB = Square.C1.bit;
				rookFromBB = Square.A1.bit;
				rookToBB = Square.D1.bit;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			kingFromBB = Square.E8.bit;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = Square.G8.bit;
				rookFromBB = Square.H8.bit;
				rookToBB = Square.F8.bit;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = Square.C8.bit;
				rookFromBB = Square.A8.bit;
				rookToBB = Square.D8.bit;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), Piece.EMPTY);
		setPiece(bitscanForward(kingToBB), isWhitesMove() ? Piece.WK : Piece.BK);
		setPiece(bitscanForward(rookFromBB), Piece.EMPTY);
		setPiece(bitscanForward(rookToBB), isWhitesMove() ? Piece.WR : Piece.BR);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), PieceType.KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(occupiedBB ^ kingFromTo);
		setEmptyBB(emptyBB ^ kingFromTo);

		xor(move.getColor(), PieceType.ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(occupiedBB ^ rookFromTo);
		setEmptyBB(emptyBB ^ rookFromTo);

		setCastling(colorToMove, CASTLE_NONE);

		setEpSquare(Square.EMPTY);
	}

	/**
	 * Makes a drop move.
	 * 
	 * @param move
	 */
	protected void makeDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		PieceColor oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece().type, toBB);
		setOccupiedBB(occupiedBB ^ toBB);
		setEmptyBB(emptyBB ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), move.getPiece().getPiece());
		setEpSquare(Square.EMPTY);
	}

	protected void makeEPMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		long captureBB = colorToMove == PieceColor.WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);

		Square captureSquare = bitscanForward(captureBB);

		xor(move.getColor(), move.getPiece().type, fromToBB);
		xor(move.getColor(), fromToBB);
		setOccupiedBB(occupiedBB ^ fromToBB);
		setEmptyBB(emptyBB ^ fromToBB);

		xor(move.getCaptureColor(), move.getPiece().type, captureBB);
		xor(move.getCaptureColor(), captureBB);
		setOccupiedBB(occupiedBB ^ captureBB);
		setEmptyBB(emptyBB ^ captureBB);

		setPiece(move.getFrom(), Piece.EMPTY);
		setPiece(move.getTo(), move.getPiece());
		setPiece(captureSquare, Piece.EMPTY);

		updateZobristEP(move, captureSquare);
		setEpSquare(Square.EMPTY);
	}

	protected void makeNonEpNonCastlingMove(Move move) {		
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		PieceColor oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(occupiedBB ^ fromBB);
			setEmptyBB(emptyBB ^ fromBB);

			xor(oppositeColor, move.getCaptureWithPromoteMask().type, toBB);
			xor(oppositeColor, toBB);
			updateZobristPOCapture(move, oppositeColor);
		} else {
			setOccupiedBB(occupiedBB ^ fromToBB);
			setEmptyBB(emptyBB ^ fromToBB);
			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece().type, fromBB);

			xor(move.getColor(), move.getPiecePromotedTo(), toBB);

			setPiece(move.getTo(), GameUtils.getColoredPiece(move.getPiecePromotedTo(), move.getColor()).getPromotedPiece());
			setPiece(move.getFrom(), Piece.EMPTY);

			// capture is handled in forceMove.
			// promoted piece never has a promote mask only captures do.
			// Promotes do not effect drop pieces.
			decrementPieceCount(colorToMove, move.getPiece().type);
			incrementPieceCount(colorToMove, move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece().type, fromToBB);

			setPiece(move.getTo(), move.getPiece());
			setPiece(move.getFrom(), Piece.EMPTY);
		}

		updateCastlingRightsForNonEpNonCastlingMove(move);

		setEpSquare(move.getEpSquare());
	}

	/**
	 * A method that makeSanMove invokes with the SanValidations it created. If
	 * a move can be made it should be returned. This method is provided so
	 * subclasses can enhance utilize the SanValidations without having to
	 * override makeSanMove and run the SAN validations again.
	 * 
	 * This method may also set certain pseudoLegals to ignore to null.
	 */
	protected Move makeSanMoveOverride(String shortAlgebraic, SanUtils.SanValidations validations, List<Move> pseudoLegals) {
		return null;
	}

	protected void rollbackCastlingMove(Move move) {
		Piece king, rook;
		long kingFromBB, kingToBB, rookFromBB, rookToBB;

		if (move.getColor() == PieceColor.WHITE) {
			king = Piece.WK;
			rook = Piece.WR;
			kingFromBB = Square.E1.bit;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = Square.G1.bit;
				rookFromBB = Square.H1.bit;
				rookToBB = Square.F1.bit;
				updateZobristPOCastleKsideWhite();

			} else {
				kingToBB = Square.C1.bit;
				rookFromBB = Square.A1.bit;
				rookToBB = Square.D1.bit;
				updateZobristPOCastleQsideWhite();
			}
		} else {
			king = Piece.BK;
			rook = Piece.BR;
			kingFromBB = Square.E8.bit;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				kingToBB = Square.G8.bit;
				rookFromBB = Square.H8.bit;
				rookToBB = Square.F8.bit;
				updateZobristPOCastleKsideBlack();
			} else {
				kingToBB = Square.C8.bit;
				rookFromBB = Square.A8.bit;
				rookToBB = Square.D8.bit;
				updateZobristPOCastleQsideBlack();
			}
		}

		setPiece(bitscanForward(kingFromBB), king);
		setPiece(bitscanForward(kingToBB), Piece.EMPTY);
		setPiece(bitscanForward(rookFromBB), rook);
		setPiece(bitscanForward(rookToBB), Piece.EMPTY);

		long kingFromTo = kingToBB | kingFromBB;
		long rookFromTo = rookToBB | rookFromBB;

		xor(move.getColor(), PieceType.KING, kingFromTo);
		xor(move.getColor(), kingFromTo);
		setOccupiedBB(occupiedBB ^ kingFromTo);
		setEmptyBB(emptyBB ^ kingFromTo);

		xor(move.getColor(), PieceType.ROOK, rookFromTo);
		xor(move.getColor(), rookFromTo);
		setOccupiedBB(occupiedBB ^ rookFromTo);
		setEmptyBB(emptyBB ^ rookFromTo);

		setEpSquareFromPreviousMove();
	}

	protected void rollbackDropMove(Move move) {
		long toBB = getBitboard(move.getTo());
		PieceColor oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), toBB);
		xor(move.getColor(), move.getPiece().type, toBB);
		setOccupiedBB(occupiedBB ^ toBB);
		setEmptyBB(emptyBB ^ toBB);

		updateZobristDrop(move, oppositeColor);

		setPiece(move.getTo(), Piece.EMPTY);
		setEpSquareFromPreviousMove();
	}

	protected void rollbackEcoHeaders(Move move) {
		if (isSettingEcoHeaders()) {
			if (StringUtils.isNotBlank(move.getPreviousEcoHeader())) {
				setHeader(PgnHeader.ECO, move.getPreviousEcoHeader());
			} else {
				removeHeader(PgnHeader.ECO);
			}
			if (StringUtils.isNotBlank(move.getPreviousOpeningHeader())) {
				setHeader(PgnHeader.Opening, move.getPreviousOpeningHeader());
			} else {
				removeHeader(PgnHeader.Opening);
			}
		}
	}

	protected void rollbackEpMove(Move move) {
		PieceColor oppositeColor = getOppositeColor(colorToMove);
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		long captureBB = oppositeColor == PieceColor.WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);
		Square captureSquare = bitscanForward(captureBB);

		xor(oppositeColor, move.getPiece().type, fromToBB);
		xor(oppositeColor, fromToBB);
		setOccupiedBB(occupiedBB ^ fromToBB);
		setEmptyBB(emptyBB ^ fromToBB);
		setEmptyBB(emptyBB ^ captureBB);
		setOccupiedBB(occupiedBB ^ captureBB);

		xor(colorToMove, move.getCaptureWithPromoteMask().type, captureBB);
		xor(colorToMove, captureBB);

		setPiece(move.getTo(), Piece.EMPTY);
		setPiece(move.getFrom(), move.getPiece());
		setPiece(captureSquare, move.getCaptureWithPromoteMask());

		updateZobristEP(move, captureSquare);
		setEpSquareFromPreviousMove();
	}

	protected void rollbackNonEpNonCastlingMove(Move move) {
		PieceColor oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(occupiedBB ^ fromBB);
			setEmptyBB(emptyBB ^ fromBB);

			xor(oppositeColor, move.getCaptureWithPromoteMask().type, toBB);
			xor(oppositeColor, toBB);

			updateZobristPOCapture(move, oppositeColor);

		} else {
			setOccupiedBB(occupiedBB ^ fromToBB);
			setEmptyBB(emptyBB ^ fromToBB);

			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion()) {
			xor(move.getColor(), move.getPiece().type, fromBB);
			xor(move.getColor(), move.getPiecePromotedTo(), toBB);

			// capture is handled in rollback.
			// promoted pieces never have a promote mask.
			// Promotions do not change drop counts.
			incrementPieceCount(move.getColor(), move.getPiece().type);
			decrementPieceCount(move.getColor(), move.getPiecePromotedTo());
		} else {
			xor(move.getColor(), move.getPiece().type, fromToBB);
		}

		setPiece(move.getFrom(), move.getPiece());
		setPiece(move.getTo(), move.getCaptureWithPromoteMask());

		setEpSquareFromPreviousMove();
	}

	protected void setEpSquareFromPreviousMove() {
		switch (moves.getSize()) {
		case 0:
			setEpSquare(initialEpSquare);
			break;
		default:
			setEpSquare(moves.getLast().getEpSquare());
			break;
		}
	}

	/**
	 * Should be called before the move is made to update the san field.
	 */
	protected void setSan(Move move) {
		if (isSettingMoveSan() && move.getSan() == null) {
			// TO DO: possible add + or ++ for check/checkmate
			String shortAlgebraic = null;

			if (move.isDrop()) {
				move.setSan(move.getPiece().type.ch + "@"
						+ GameUtils.getSan(move.getTo()));
			} else if (move.isCastleShort()) {
				shortAlgebraic = "O-O";
			} else if (move.isCastleLong()) {
				shortAlgebraic = "O-O-O";
			} else if (move.getPiece().type == PieceType.PAWN
					&& (move.getMoveCharacteristic() & Move.EN_PASSANT_CHARACTERISTIC) != 0) // e.p.
			// is
			// optional but
			// the x is
			// required.
			// (pawn eps
			// are never
			// unambiguous)
			{
				shortAlgebraic = SanUtils.squareToFileSan(move.getFrom()) + "x"
						+ SanUtils.squareToSan(move.getTo());
			} else if (move.getPiece().type == PieceType.PAWN && move.isCapture()) // Possible
			// formats ed
			// ed5 edQ
			// (pawn captures
			// can be
			// ambiguous)
			{
				PieceColor oppositeColorToMove = GameUtils.getOppositeColor(colorToMove);
				long fromBB = getPieceBB(colorToMove, PieceType.PAWN);
				int movesFound = 0;
				while (fromBB != 0) {
					Square fromSquare = bitscanForward(fromBB);

					long allPawnCapturesBB = pawnCapture(colorToMove,
							getBitboard(fromSquare),
							getColorBB(oppositeColorToMove));

					while (allPawnCapturesBB != 0) {
						Square toSquare = bitscanForward(allPawnCapturesBB);
						if (toSquare.file == move.getTo().file) {
							movesFound++;
						}
						allPawnCapturesBB = bitscanClear(allPawnCapturesBB);
					}
					fromBB = bitscanClear(fromBB);
				}

				if (movesFound > 1) {
					shortAlgebraic = SanUtils.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtils.squareToSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ move.getPiecePromotedTo().ch : "");
				} else {
					shortAlgebraic = SanUtils.squareToFileSan(move.getFrom())
							+ "x"
							+ SanUtils.squareToFileSan(move.getTo())
							+ (move.isPromotion() ? "="
									+ move.getPiecePromotedTo().ch : "");
				}
			} else if (move.getPiece().type == PieceType.PAWN) // e4 (pawn moves
			// are never
			// ambiguous)
			{
				shortAlgebraic = SanUtils.squareToSan(move.getTo())
						+ (move.isPromotion() ? "="
								+ move.getPiecePromotedTo().ch : "");
			} else {
				long fromBB = getPieceBB(colorToMove, move.getPiece().type);
				long toBB = getBitboard(move.getTo());

				int sameFilesFound = 0;
				int sameRanksFound = 0;
				int matchesFound = 0;

				if (move.getPiece().type != PieceType.KING) {
					while (fromBB != 0) {
						Square fromSquare = bitscanForward(fromBB);
						long resultBB = 0;

						switch (move.getPiece().type) {
						case KNIGHT:
							resultBB = knightMove(fromSquare) & toBB;
							break;
						case BISHOP:
							resultBB = diagonalMove(fromSquare, emptyBB,
                                    occupiedBB)
									& getNotColorToMoveBB() & toBB;
							break;
						case ROOK:
							resultBB = orthogonalMove(fromSquare, emptyBB,
                                    occupiedBB)
									& getNotColorToMoveBB() & toBB;
							break;
						case QUEEN:
							resultBB = orthogonalMove(fromSquare, emptyBB,
                                    occupiedBB)
									& getNotColorToMoveBB()
									& toBB
									| diagonalMove(fromSquare, emptyBB,
                                    occupiedBB)
									& getNotColorToMoveBB() & toBB;
							break;
						}

						if (resultBB != 0) {
							Square toSquare = bitscanForward(resultBB);

							if (toSquare == move.getTo()) {
								matchesFound++;
								if (fromSquare.file == move.getFrom().file) {
									sameFilesFound++;
								}
								if (fromSquare.rank == move.getFrom().rank) {
									sameRanksFound++;
								}
							}
						}
						fromBB = bitscanClear(fromBB);
					}
				}

				shortAlgebraic = String.valueOf(move.getPiece().type.ch);
				boolean hasHandledAmbiguity = false;
				if (sameRanksFound > 1) {
					shortAlgebraic += SanUtils.squareToFileSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (sameFilesFound > 1) {
					shortAlgebraic += SanUtils.squareToRankSan(move.getFrom());
					hasHandledAmbiguity = true;
				}
				if (matchesFound > 1 && !hasHandledAmbiguity) {
					shortAlgebraic += SanUtils.squareToFileSan(move.getFrom());
				}

				shortAlgebraic += (move.isCapture() ? "x" : "")
						+ SanUtils.squareToSan(move.getTo());
			}

			move.setSan(shortAlgebraic);
		}
	}

	protected void setState(int state) {
		this.state = state;
	}

	/**
	 * If the match list contains no ambiguity after taking disambiguity by
	 * check into consideration the move is returned. Otherwise an
	 * IllegalArgumentException is raised
	 */
	protected Move testForSanDisambiguationFromCheck(String shortAlgebraic,
			MoveList matches) throws IllegalArgumentException {
		Move result = null;
		if (matches.getSize() == 0) {
			throw new IllegalArgumentException("Invalid move " + shortAlgebraic
					+ "\n" + toString());
		} else if (matches.getSize() == 1) {
			result = matches.get(0);
		} else {
			// now do legality checking on whats left.
			Square kingSquare = bitscanForward(getPieceBB(colorToMove, PieceType.KING));
			PieceColor cachedColorToMove = colorToMove;
			int matchesCount = 0;

			if (kingSquare != Square.EMPTY) { // Now trim illegals
				for (int i = 0; i < matches.getSize(); i++) {
					Move current = matches.get(i);

					// Needed for FR.
					if (current.isCastleLong() || current.isCastleShort()) {
						continue;
					}
					synchronized (this) {
						try {
							forceMove(current);
							if (current.getPiece().type == PieceType.KING) {
								Square newKingCoordinates = GameUtils
										.bitscanForward(getPieceBB(
												cachedColorToMove, PieceType.KING));
								if (!isInCheck(cachedColorToMove, GameUtils
										.getBitboard(newKingCoordinates))) {
									result = current;
									matchesCount++;
								} else {
								}
							} else {
								if (!isInCheck(cachedColorToMove,
										getBitboard(kingSquare))) {
									result = current;
									matchesCount++;
								} else {
								}
							}
							rollback();
						} catch (IllegalArgumentException ie) {
						}
					}
				}
			}

			if (matchesCount == 0) {
				throw new IllegalArgumentException("Invalid move "
						+ shortAlgebraic + "\n" + toString());
			} else if (matchesCount > 1) {
				throw new IllegalArgumentException("Ambiguous move "
						+ shortAlgebraic + "\n" + toString());
			}
		}
		return result;
	}

	/**
	 * Provided so it can be easily implemented for Fischer Random type of
	 * games.
	 */
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {		
		switch (move.getPiece().type) {
		case KING:
			setCastling(colorToMove, CASTLE_NONE);
			break;
		default:
			if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A1
					&& colorToMove == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A1 && colorToMove == PieceColor.BLACK) {
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H1
					&& colorToMove == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H1 && colorToMove == PieceColor.BLACK) {
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & CASTLE_LONG));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A8
					&& colorToMove == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A8 && colorToMove == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H8
					&& colorToMove == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H8 && colorToMove == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & CASTLE_LONG));
			}
			break;
		}
	}

	protected void updateEcoHeaders(Move move) {
		if (isSettingEcoHeaders()) {
			move.setPreviousEcoHeader(getHeader(PgnHeader.ECO));
			move.setPreviousOpeningHeader(getHeader(PgnHeader.Opening));

			EcoInfo eco = EcoService.getInstance().getEcoInfo(this);
			if (eco != null) {
				setHeader(PgnHeader.ECO, eco.getEcoCode());
				setHeader(PgnHeader.Opening, eco.getOpening());
			}

		}
	}

	protected void updateZobristDrop(Move move, PieceColor oppositeColor) {
		zobristPositionHash ^= ZobristUtils.zobrist(move.getPiece().getPiece(), move.getTo());
	}

	protected void updateZobristEP(Move move, Square captureSquare) {
		zobristPositionHash ^= ZobristUtils.zobrist(move.getColor(), PieceType.PAWN, move.getFrom())
				^ ZobristUtils.zobrist(move.getColor(), PieceType.PAWN, move.getTo())
				^ ZobristUtils.zobrist(move.getCaptureColor(), PieceType.PAWN, captureSquare);
	}

	protected void updateZobristHash() {
		zobristGameHash = zobristPositionHash
				^ ZobristUtils.zobrist(colorToMove, epSquare, getCastling(PieceColor.WHITE),
						getCastling(PieceColor.BLACK));
	}

	protected void updateZobristPOCapture(Move move, PieceColor oppositeColor) {
		zobristPositionHash ^= ZobristUtils.zobrist(move.getColor(),
				move.isPromotion() ? move.getPiecePromotedTo()
						: move.getPiece().type
						, move.getTo())
				^ ZobristUtils.zobrist(oppositeColor, move.getCaptureWithPromoteMask().type,
						move.getTo())
				^ ZobristUtils.zobrist(move.getColor(), move.getPiece().type,
						move.getFrom());
	}

	protected void updateZobristPOCastleKsideBlack() {
		zobristPositionHash ^= ZobristUtils.zobrist(Piece.BK, Square.E8)
				^ ZobristUtils.zobrist(Piece.BK, Square.G8)
				^ ZobristUtils.zobrist(Piece.BR, Square.H8)
				^ ZobristUtils.zobrist(Piece.BR, Square.F8);
	}

	protected void updateZobristPOCastleKsideWhite() {
		zobristPositionHash ^= ZobristUtils.zobrist(Piece.WK, Square.E1)
				^ ZobristUtils.zobrist(Piece.WK, Square.G1)
				^ ZobristUtils.zobrist(Piece.WR, Square.H1)
				^ ZobristUtils.zobrist(Piece.WR, Square.F1);
	}

	protected void updateZobristPOCastleQsideBlack() {
		zobristPositionHash ^= ZobristUtils.zobrist(Piece.BK, Square.E8)
				^ ZobristUtils.zobrist(Piece.BK, Square.C8)
				^ ZobristUtils.zobrist(Piece.BR, Square.A8)
				^ ZobristUtils.zobrist(Piece.BR, Square.D8);
	}

	protected void updateZobristPOCastleQsideWhite() {
		zobristPositionHash ^= ZobristUtils.zobrist(Piece.WK, Square.E1)
				^ ZobristUtils.zobrist(Piece.WK, Square.C1)
				^ ZobristUtils.zobrist(Piece.WR, Square.A1)
				^ ZobristUtils.zobrist(Piece.WR, Square.D1);
	}

	protected void updateZobristPONoCapture(Move move, PieceColor oppositeColor) {
		zobristPositionHash ^= ZobristUtils.zobrist(move.getColor(),
				move.isPromotion() ? move.getPiecePromotedTo()
						: move.getPiece().type
						, move.getTo())
				^ ZobristUtils.zobrist(move.getColor(), move.getPiece().type,
						move.getFrom());
	}

	/**
	 * Exclusive bitwise ors the games piece bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param piece
	 *            The non-colored piece type.
	 * @param bb
	 *            The bitmap to XOR.
	 */
	protected void xor(PieceColor color, PieceType piece, long bb) {
		pieceBB[color.index][piece.index] ^= bb;
	}

	/**
	 * Exclusive bitwise ors the games color bitboard with the specified
	 * bitboard.
	 * 
	 * @param color
	 *            WHITE or BLACK
	 * @param bb
	 *            The bitmap to XOR.
	 */
	protected void xor(PieceColor color, long bb) {
		colorBB[color.index] ^= bb;
	}

}
