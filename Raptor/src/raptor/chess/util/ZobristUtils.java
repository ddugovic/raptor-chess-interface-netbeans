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
package raptor.chess.util;

import java.util.Random;

import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.util.GameUtils;

public final class ZobristUtils extends GameUtils implements GameConstants {

	private static final long[][] ZOBRIST_CASTLE = new long[2][4];
	private static final long[][][] ZOBRIST_DROP_COUNT = new long[2][7][18];
	private static final long[] ZOBRIST_EP = new long[65];
	private static final long[][][] ZOBRIST_POSITION = new long[2][7][64];
	private static final long[] ZOBRIST_TO_MOVE = new long[2];

	static {
		initZobrist();
	}

	public static long zobrist(Piece piece, Square square) {
		return ZOBRIST_POSITION[piece.color.index][piece.type.index][square.index];
	}

	public static long zobrist(PieceColor color, PieceType piece, Square square) {
		return ZOBRIST_POSITION[color.index][piece.index][square.index];
	}

	public static long zobrist(PieceColor colorToMove, Square epSquare,
			int whiteCastling, int blackCastling) {
		return ZOBRIST_TO_MOVE[colorToMove.index] ^ ZOBRIST_EP[epSquare.index]
				^ ZOBRIST_CASTLE[PieceColor.WHITE.index][whiteCastling]
				^ ZOBRIST_CASTLE[PieceColor.BLACK.index][blackCastling];
	}

	public static long zobristDropPieces(Game game) {
		return ZOBRIST_DROP_COUNT[PieceColor.WHITE.index][PieceType.PAWN.index][game.getDropCount(PieceColor.WHITE, PieceType.PAWN)]
				^ ZOBRIST_DROP_COUNT[PieceColor.WHITE.index][PieceType.PAWN.index][game.getDropCount(PieceColor.WHITE,
						PieceType.KNIGHT)]
				^ ZOBRIST_DROP_COUNT[PieceColor.WHITE.index][PieceType.PAWN.index][game.getDropCount(PieceColor.WHITE,
						PieceType.BISHOP)]
				^ ZOBRIST_DROP_COUNT[PieceColor.WHITE.index][PieceType.PAWN.index][game.getDropCount(PieceColor.WHITE,
						PieceType.QUEEN)]
				^ ZOBRIST_DROP_COUNT[PieceColor.WHITE.index][PieceType.PAWN.index][game
						.getDropCount(PieceColor.WHITE, PieceType.ROOK)]
				^ ZOBRIST_DROP_COUNT[PieceColor.BLACK.index][PieceType.PAWN.index][game
						.getDropCount(PieceColor.BLACK, PieceType.PAWN)]
				^ ZOBRIST_DROP_COUNT[PieceColor.BLACK.index][PieceType.PAWN.index][game.getDropCount(PieceColor.BLACK,
						PieceType.KNIGHT)]
				^ ZOBRIST_DROP_COUNT[PieceColor.BLACK.index][PieceType.PAWN.index][game.getDropCount(PieceColor.BLACK,
						PieceType.BISHOP)]
				^ ZOBRIST_DROP_COUNT[PieceColor.BLACK.index][PieceType.PAWN.index][game.getDropCount(PieceColor.BLACK,
						PieceType.QUEEN)]
				^ ZOBRIST_DROP_COUNT[PieceColor.BLACK.index][PieceType.PAWN.index][game
						.getDropCount(PieceColor.BLACK, PieceType.ROOK)];
	}

	public static long zobristHash(Game game) {
		return zobristHashPositionOnly(game)
				^ zobrist(game.getColorToMove(), game.getEpSquare(), game
						.getCastling(PieceColor.WHITE), game.getCastling(PieceColor.BLACK));
	}

	public static long zobristHashPositionOnly(Game game) {
		return zobristPiece(Piece.WP, game)
				^ zobristPiece(Piece.WN, game)
				^ zobristPiece(Piece.WB, game)
				^ zobristPiece(Piece.WR, game)
				^ zobristPiece(Piece.WQ, game)
				^ zobristPiece(Piece.WK, game)
				^ zobristPiece(Piece.BP, game)
				^ zobristPiece(Piece.BN, game)
				^ zobristPiece(Piece.BB, game)
				^ zobristPiece(Piece.BR, game)
				^ zobristPiece(Piece.BQ, game)
				^ zobristPiece(Piece.BK, game);
	}

	private static void initZobrist() {
		Random random = new Random();

		for (int i = 0; i < ZOBRIST_DROP_COUNT.length; i++) {
			for (int j = 0; j < ZOBRIST_DROP_COUNT[i].length; j++) {
				for (int k = 0; k < ZOBRIST_DROP_COUNT[i][j].length; k++) {
					ZOBRIST_DROP_COUNT[i][j][k] = random.nextLong();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_POSITION.length; i++) {
			for (int j = 0; j < ZOBRIST_POSITION[i].length; j++) {
				for (int k = 0; k < ZOBRIST_POSITION[i][j].length; k++) {
					ZOBRIST_POSITION[i][j][k] = random.nextLong();
				}
			}
		}

		for (int i = 0; i < ZOBRIST_TO_MOVE.length; i++) {
			ZOBRIST_TO_MOVE[i] = random.nextLong();
		}

		for (int i = 0; i < ZOBRIST_EP.length; i++) {
			ZOBRIST_EP[i] = random.nextLong();
		}

		for (int i = 0; i < ZOBRIST_CASTLE.length; i++) {
			for (int j = 0; j < ZOBRIST_CASTLE[i].length; j++) {
				ZOBRIST_CASTLE[i][j] = random.nextLong();
			}
		}
	}

	private static long zobristPiece(Piece piece, Game game) {
		int result = 0;
		long current = game.getPieceBB(piece.color, piece.type);
		while (current != 0L) {
			result ^= zobrist(piece, bitscanForward(current));
			current = bitscanClear(current);
		}
		return result;
	}
}
