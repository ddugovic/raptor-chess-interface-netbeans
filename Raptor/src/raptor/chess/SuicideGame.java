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

import java.util.List;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.chess.util.SanUtils;
import raptor.util.Logger;

/**
 * Follows FICS suicide chess rules.
 */
public class SuicideGame extends LosersGame {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(SuicideGame.class);

	public SuicideGame() {
		setHeader(PgnHeader.Variant, Variant.suicide.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SuicideGame deepCopy(boolean ignoreHashes) {
		SuicideGame result = new SuicideGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Pawns can promote to a king in suicide. This method is overridden to
	 * supply that functionality.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void generatePseudoPawnCaptures(Square fromSquare, long fromBB, PieceColor oppositeColor, PriorityMoveList moves) {
		Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
		long toBB = GameUtils.pawnCapture(getColorToMove(), fromBB, getColorBB(oppositeColor));

		while (toBB != 0L) {
			Square toSquare = GameUtils.bitscanForward(toBB);
			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare), PieceType.KNIGHT,
						Square.EMPTY, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare), PieceType.BISHOP,
						Square.EMPTY, Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare), PieceType.QUEEN, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare), PieceType.ROOK, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare), PieceType.KING, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
			} else {
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						getPiece(toSquare)), moves);
			}
			toBB = GameUtils.bitscanClear(toBB);
		}
	}

	/**
	 * Pawns can promote to a king in suicide. This method is overridden to
	 * supply that functionality.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public void generatePseudoPawnSinglePush(Square fromSquare, long fromBB,
			PieceColor oppositeColor, PriorityMoveList moves) {
		Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
		long toBB = GameUtils.pawnSinglePush(getColorToMove(), fromBB, getEmptyBB());

		while (toBB != 0) {
			Square toSquare = GameUtils.bitscanForward(toBB);

			if ((toBB & RANK8_OR_RANK1) != 0L) {
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY, PieceType.KNIGHT, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY, PieceType.BISHOP, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY, PieceType.QUEEN, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY, PieceType.ROOK, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY, PieceType.KING, Square.EMPTY,
						Move.PROMOTION_CHARACTERISTIC), moves);

			} else {
				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						Piece.EMPTY), moves);
			}
			toBB = GameUtils.bitscanClear(toBB);
		}
	}

	/**
	 * All positions reachable by pseudo-legal moves are legal in suicide.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public boolean isLegalPosition() {
		return true;
	}

	/**
	 * Needs to be overridden to support promotions to king.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected Move makeSanMoveOverride(String shortAlgebraic, SanUtils.SanValidations validations, List<Move> pseudoLegals) {
		String san = validations.getStrictSan();

		Move result = null;

		if (san.charAt(san.length() - 1) == PieceType.KING.ch) {
			MoveList matches = new MoveList(10);

			if (SanUtils.isValidSuicidePPromotionStrict(san)) {
				Square toSquare = Square.getSquare((byte)RANK_FROM_SAN.indexOf(san
						.charAt(1)), (byte)FILE_FROM_SAN.indexOf(san.charAt(0)));

				for (Move move : pseudoLegals) {
					if (move.getTo() == toSquare
							&& move.getPiece().type == PieceType.PAWN
							&& move.isPromotion()
							&& move.getPiecePromotedTo() == PieceType.KING) {
						matches.append(move);
					}
				}
			} else if (SanUtils.isValidSuicidePxPromotionStrict(san)) {
				int fromFile = FILE_FROM_SAN.indexOf(san.charAt(0));
				int toFile = FILE_FROM_SAN.indexOf(san.charAt(1));

				for (Move move : pseudoLegals) {
					if (move.getTo().file == toFile
							&& move.getFrom().file == fromFile
							&& move.getPiece().type == PieceType.PAWN && move.isPromotion()
							&& move.getPiecePromotedTo() == PieceType.KING) {
						matches.append(move);
					}
				}
			} else if (SanUtils.isValidSuicideAmbigPxPromotion(san)) {
				int fromFile = FILE_FROM_SAN.indexOf(san.charAt(0));
				Square toSquare = Square.getSquare((byte)RANK_FROM_SAN.indexOf(san
						.charAt(2)), (byte)FILE_FROM_SAN.indexOf(san.charAt(1)));
				for (Move move : pseudoLegals) {
					if (move.getTo() == toSquare
							&& move.getFrom().file == fromFile
							&& move.getPiece().type == PieceType.PAWN && move.isPromotion()
							&& move.getPiecePromotedTo() == PieceType.KING) {
						matches.append(move);
					}
				}
			}
			result = testForSanDisambiguationFromCheck(shortAlgebraic, matches);
			if (result == null) {
				throw new IllegalArgumentException("Illegal move: " + shortAlgebraic);
			} else {
				result.setSan(shortAlgebraic);
			}
		}
		return result;
	}

//	/**
//	 * Castling isn't permitted in suicide. This method is overridden to remove
//	 * it.
//	 * 
//	 * 
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void generatePseudoKingCastlingMoves(long fromBB, PriorityMoveList moves) {
//	}

	/**
	 * There can be more than one king in suicide. So have to override
	 * generatePseudoKingMove to check all kings.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected void generatePseudoKingMoves(PriorityMoveList moves) {
		Piece piece = isWhitesMove() ? Piece.WK : Piece.BK;
		long fromBB = getPieceBB(getColorToMove(), PieceType.KING);
		while (fromBB != 0) {
			Square fromSquare = GameUtils.bitscanForward(fromBB);
			long toBB = GameUtils.kingMove(fromSquare) & getNotColorToMoveBB();

			generatePseudoKingCastlingMoves(fromBB, moves);

			while (toBB != 0) {
				Square toSquare = GameUtils.bitscanForward(toBB);

				Piece capture = getPiece(toSquare);

				addMove(new Move(fromSquare, toSquare, piece, getColorToMove(),
						capture), moves);
				toBB = GameUtils.bitscanClear(toBB);
				toSquare = GameUtils.bitscanForward(toBB);
			}
			fromBB = GameUtils.bitscanClear(fromBB);
		}
	}

	/**
	 * There is no disambiguation from check in suicide. So just throw an
	 * exception on more than 1 match.
	 * 
	 *{@inheritDoc}
	 */
	@Override
	protected Move testForSanDisambiguationFromCheck(String shortAlgebraic,
			MoveList matches) throws IllegalArgumentException {
		Move result = null;
		if (matches.isEmpty()) {
			throw new IllegalArgumentException("Invalid move " + shortAlgebraic
					+ "\n" + toString());
		} else if (matches.getSize() == 1) {
			result = matches.get(0);
		} else {
			throw new IllegalArgumentException("Ambiguous move "
					+ shortAlgebraic + "\n" + toString());
		}
		return result;
	}
}
