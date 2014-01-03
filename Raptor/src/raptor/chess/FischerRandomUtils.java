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

import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getBitboard;
import static raptor.chess.util.GameUtils.getSquare;
import static raptor.chess.util.ZobristUtils.zobrist;
import raptor.chess.util.GameUtils;

/**
 * Contains utility methods for fischer random chess. Since java doesnt allow
 * multiple inheritance this is used to avoid duplicate code.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program.
 */
public class FischerRandomUtils implements GameConstants {

	/**
	 * Returns true if all the squares between startFile and endFile are empty
	 * (excluding the passed in files).
	 */
	public static boolean emptyBetweenFiles(ClassicGame game, byte rank,
			byte startFile, byte endFile) {
		boolean result = true;
		for (byte file = (byte)(startFile + 1); file < endFile; file++) {
			result = game.getPiece(Square.getSquare(rank, file)) == Piece.EMPTY;
			if (!result) {
				break;
			}
		}
		return result;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void generatePseudoKingCastlingMoves(ClassicGame game,
			long fromBB, PriorityMoveList moves, byte initialKingFile,
			byte initialShortRookFile, byte initialLongRookFile) {
		Square kingSquare = game.getColorToMove() == PieceColor.WHITE ? Square.getSquare((byte)0,
				initialKingFile) : Square.getSquare((byte)7, initialKingFile);
		long kingSquareBB = getBitboard(kingSquare);

		if (game.getColorToMove() == PieceColor.WHITE
				&& (game.getCastling(game.getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, (byte)0, initialKingFile,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, Square.F1, PieceColor.WHITE,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, Square.G1, PieceColor.WHITE,
						initialShortRookFile)
				&& !isCastlePathInCheck(game, kingSquare, Square.G1, PieceColor.WHITE)) {

			moves.appendLowPriority(new Move(kingSquare, Square.G1, Piece.WK,
					PieceColor.WHITE, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == PieceColor.BLACK
				&& (game.getCastling(game.getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, (byte)7, initialKingFile,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, Square.F8, PieceColor.BLACK,
						initialShortRookFile)
				&& isKingEmptyOrRook(game, Square.G8, PieceColor.BLACK,
						initialShortRookFile)
				&& !isCastlePathInCheck(game, kingSquare, Square.G8, PieceColor.BLACK)) {

			moves.appendLowPriority(new Move(kingSquare, Square.G8, Piece.BK,
					PieceColor.BLACK, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == PieceColor.WHITE
				&& (game.getCastling(game.getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, (byte)0, initialLongRookFile,
						initialKingFile)
				&& isKingEmptyOrRook(game, Square.D1, PieceColor.WHITE,
						initialLongRookFile)
				&& isKingEmptyOrRook(game, Square.C1, PieceColor.WHITE,
						initialLongRookFile)
				&& !isCastlePathInCheck(game, kingSquare, Square.C1, PieceColor.WHITE)) {
			moves.appendLowPriority(new Move(kingSquare, Square.C1, Piece.WK,
					PieceColor.WHITE, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}

		if (game.getColorToMove() == PieceColor.BLACK
				&& (game.getCastling(game.getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB
				&& emptyBetweenFiles(game, (byte)7, initialLongRookFile,
						initialKingFile)
				&& isKingEmptyOrRook(game, Square.D8, PieceColor.BLACK,
						initialLongRookFile)
				&& isKingEmptyOrRook(game, Square.C8, PieceColor.BLACK,
						initialLongRookFile)
				&& !isCastlePathInCheck(game, kingSquare, Square.C8, PieceColor.BLACK)) {
			moves.appendLowPriority(new Move(kingSquare, Square.C8, Piece.BK,
					PieceColor.BLACK, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
		}
	}

	/**
	 * Returns true if any of the squares between king startSquare and
	 * kingEndSquare are in check (including the start/end squares).
	 * kingStartSquare can either be < or > kingEndSquare.
	 */
	public static boolean isCastlePathInCheck(Game game, Square kingStartSquare,
			Square kingEndSquare, PieceColor color) {
		// PERF: Maybe pre-generate attack matrix instead of multiple
		// calls to game.isInCheck()
		boolean result = false;
		if (kingStartSquare.compareTo(kingEndSquare) < 0) {
			for (byte square = kingStartSquare.index; !result && square < kingEndSquare.index; square++) {
				result = game.isInCheck(color, getBitboard(SQUARES[square]));
			}
		} else if (kingStartSquare.compareTo(kingEndSquare) > 0) {
			for (byte square = kingEndSquare.index; !result && square < kingStartSquare.index; square++) {
				result = game.isInCheck(color, getBitboard(SQUARES[square]));
			}
		}
		return result;
	}

	/**
	 * Returns true if the specified square is either empty or a king or rook of
	 * the specified color.
	 */
	public static boolean isKingEmptyOrRook(ClassicGame game, Square square,
			PieceColor color, int intitialRookFile) {
		return game.board[square.index].type == PieceType.EMPTY || game.board[square.index].type == PieceType.KING
				&& (game.getColorBB(color) & getBitboard(square)) != 0
				|| game.board[square.index].type == PieceType.ROOK
				&& intitialRookFile == square.file
				&& (game.getColorBB(color) & getBitboard(square)) != 0;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void makeCastlingMove(ClassicGame game, Move move,
			byte initialKingFile, byte initialShortRookFile,
			byte initialLongRookFile) {
		Piece king, rook;
		Square kingFromSquare = move.getColor() == PieceColor.WHITE ? Square.getSquare((byte)0,
				initialKingFile) : Square.getSquare((byte)7, initialKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB, rookFromBB, rookToBB;
		Square rookFromSquare;

		if (move.getColor() == PieceColor.WHITE) {
			king = Piece.WK;
			rook = Piece.WR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)0, initialShortRookFile);
				kingToBB = Square.G1.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.F1.bit;
				updateZobristCastle(game, PieceColor.WHITE, kingFromSquare,
						rookFromSquare, Square.G1, Square.F1);
			} else {
				rookFromSquare = Square.getSquare((byte)0, initialLongRookFile);
				kingToBB = Square.C1.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.D1.bit;
				updateZobristCastle(game, PieceColor.WHITE, kingFromSquare,
						rookFromSquare, Square.C1, Square.D1);
			}
		} else {
			king = Piece.BK;
			rook = Piece.BR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)7, initialShortRookFile);
				kingToBB = Square.G8.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.F8.bit;
				updateZobristCastle(game, PieceColor.BLACK, kingFromSquare,
						rookFromSquare, Square.G8, Square.F8);
			} else {
				rookFromSquare = Square.getSquare((byte)7, initialLongRookFile);
				kingToBB = Square.C8.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.D8.bit;
				updateZobristCastle(game, PieceColor.BLACK, kingFromSquare,
						rookFromSquare, Square.C8, Square.D8);
			}
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			game.setPiece(bitscanForward(kingToBB), king);
			game.setPiece(kingFromSquare, Piece.EMPTY);
			game.xor(move.getColor(), PieceType.KING, kingFromTo);
			game.xor(move.getColor(), kingFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ kingFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			game.setPiece(bitscanForward(rookToBB), rook);
			if (rookFromBB != kingToBB) {
				game.setPiece(rookFromSquare, Piece.EMPTY);
			}
			game.xor(move.getColor(), PieceType.ROOK, rookFromTo);

			game.xor(move.getColor(), rookFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ rookFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ rookFromTo);
		}

		game.setCastling(game.getColorToMove(), CASTLE_NONE);
		game.setEpSquare(Square.EMPTY);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void rollbackCastlingMove(ClassicGame game, Move move,
			byte initialKingFile, byte initialShortRookFile,
			byte initialLongRookFile) {
		Piece king, rook;
		Square kingFromSquare = move.getColor() == PieceColor.WHITE ? Square.getSquare((byte)0,
				initialKingFile) : Square.getSquare((byte)7, initialKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB, rookFromBB, rookToBB;
		Square rookFromSquare;

		if (move.getColor() == PieceColor.WHITE) {
			king = Piece.WK;
			rook = Piece.WR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)0, initialShortRookFile);
				kingToBB = Square.G1.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.F1.bit;
				updateZobristCastle(game, PieceColor.WHITE, kingFromSquare,
						rookFromSquare, Square.G1, Square.F1);
			} else {
				rookFromSquare = Square.getSquare((byte)0, initialLongRookFile);
				kingToBB = Square.C1.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.D1.bit;
				updateZobristCastle(game, PieceColor.WHITE, kingFromSquare,
						rookFromSquare, Square.C1, Square.D1);
			}
		} else {
			king = Piece.BK;
			rook = Piece.BR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)7, initialShortRookFile);
				kingToBB = Square.G8.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.F8.bit;
				updateZobristCastle(game, PieceColor.BLACK, kingFromSquare,
						rookFromSquare, Square.G8, Square.F8);
			} else {
				rookFromSquare = Square.getSquare((byte)7, initialLongRookFile);
				kingToBB = Square.C8.bit;
				rookFromBB = getBitboard(rookFromSquare);
				rookToBB = Square.D8.bit;
				updateZobristCastle(game, PieceColor.BLACK, kingFromSquare,
						rookFromSquare, Square.C8, Square.D8);
			}
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			game.setPiece(bitscanForward(rookToBB), Piece.EMPTY);
			game.setPiece(rookFromSquare, rook);
			game.xor(move.getColor(), PieceType.ROOK, rookFromTo);
			game.xor(move.getColor(), rookFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ rookFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ rookFromTo);
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			if (kingToBB != rookFromBB) {
				game.setPiece(bitscanForward(kingToBB), Piece.EMPTY);
			}
			game.setPiece(kingFromSquare, king);
			game.xor(move.getColor(), PieceType.KING, kingFromTo);
			game.xor(move.getColor(), kingFromTo);
			game.setOccupiedBB(game.getOccupiedBB() ^ kingFromTo);
			game.setEmptyBB(game.getEmptyBB() ^ kingFromTo);
		}

		game.setEpSquareFromPreviousMove();
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	public static void updateCastlingRightsForNonEpNonCastlingMove(
			ClassicGame game, Move move, byte initialShortRookFile,
			byte initialLongRookFile) {
		
		Square shortRookSquare = game.getColorToMove() == PieceColor.BLACK ? Square.getSquare((byte)0,
				initialShortRookFile) : Square.getSquare((byte)7, initialShortRookFile);
		Square longRookSquare = game.getColorToMove() == PieceColor.BLACK ? Square.getSquare((byte)0,
				initialLongRookFile) : Square.getSquare((byte)7, initialLongRookFile);
		
		switch (move.getPiece().type) {
		case KING:
			game.setCastling(game.getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece().type == PieceType.ROOK && move.getFrom() == longRookSquare
					&& game.getColorToMove() == PieceColor.WHITE
					|| move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == longRookSquare
					&& game.getColorToMove() == PieceColor.BLACK) {
				game.setCastling(PieceColor.WHITE, (byte)(game.getCastling(PieceColor.WHITE) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK
					&& move.getFrom() == shortRookSquare
					&& game.getColorToMove() == PieceColor.WHITE
					|| move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == shortRookSquare
					&& game.getColorToMove() == PieceColor.BLACK) {
				game.setCastling(PieceColor.WHITE, (byte)(game.getCastling(PieceColor.WHITE) & CASTLE_LONG));
			} else if (move.getPiece().type == PieceType.ROOK
					&& move.getFrom() == longRookSquare
					&& game.getColorToMove() == PieceColor.BLACK
					|| move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == longRookSquare
					&& game.getColorToMove() == PieceColor.WHITE) {
				game.setCastling(PieceColor.BLACK, (byte)(game.getCastling(PieceColor.BLACK) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK
					&& move.getFrom() == shortRookSquare
					&& game.getColorToMove() == PieceColor.BLACK
					|| move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == shortRookSquare
					&& game.getColorToMove() == PieceColor.WHITE) {
				game.setCastling(PieceColor.BLACK, (byte)(game.getCastling(PieceColor.BLACK) & CASTLE_LONG));
			}
			break;
		}
	}

	/**
	 * Updates the zobrist position hash with the specified castling information
	 */
	public static void updateZobristCastle(ClassicGame game, PieceColor color,
			Square kingStartSquare, Square rookStartSquare, Square kingEndSquare,
			Square rookEndSquare) {
		game.zobristPositionHash ^= zobrist(color, PieceType.KING, kingStartSquare)
				^ zobrist(color, PieceType.KING, kingStartSquare)
				^ zobrist(color, PieceType.ROOK, rookStartSquare)
				^ zobrist(color, PieceType.ROOK, rookEndSquare);
	}
}
