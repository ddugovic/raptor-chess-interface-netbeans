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
import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;
import raptor.chess.util.SanUtils;

/**
 * Follows FICS crazyhosue rules.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program.
 */
public class CrazyhouseGame extends ClassicGame {
	public CrazyhouseGame() {
		setHeader(PgnHeader.Variant, Variant.crazyhouse.name());
		addState(Game.DROPPABLE_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public CrazyhouseGame deepCopy(boolean ignoreHashes) {
		CrazyhouseGame result = new CrazyhouseGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Overridden to invoke genDropMoves as well as super.getPseudoLegalMoves.
	 * 
	 * 
	 * {@inheritDoc}
	 */
	@Override
	public PriorityMoveList<Move> getPseudoLegalMoves() {
		PriorityMoveList<Move> result = super.getPseudoLegalMoves();
		generatePseudoDropMoves(result);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + "\n" + getDropCountsString();
	}

	/**
	 * Generates all of the pseudo legal drop moves in the position and adds
	 * them to the specified move list.
	 * 
	 * @param moves
	 *            A move list.
	 */
	protected void generatePseudoDropMoves(PriorityMoveList<Move> moves) {

		if (getDropCount(getColorToMove(), PieceType.PAWN) > 0) {
			Piece piece = isWhitesMove() ? Piece.WP : Piece.BP;
			long emptyBB = getEmptyBB() & ~RANK8_OR_RANK1;
			while (emptyBB != 0) {
				Square toSquare = bitscanForward(emptyBB);
				addMove(new Move(toSquare, piece), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), PieceType.KNIGHT) > 0) {
			Piece piece = isWhitesMove() ? Piece.WN : Piece.BN;
			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				Square toSquare = bitscanForward(emptyBB);
				addMove(new Move(toSquare, piece), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), PieceType.BISHOP) > 0) {
			Piece piece = isWhitesMove() ? Piece.WB : Piece.BB;
			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				Square toSquare = bitscanForward(emptyBB);
				addMove(new Move(toSquare, piece), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), PieceType.ROOK) > 0) {
			Piece piece = isWhitesMove() ? Piece.WR : Piece.BR;
			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				Square toSquare = bitscanForward(emptyBB);
				addMove(new Move(toSquare, piece), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}

		if (getDropCount(getColorToMove(), PieceType.QUEEN) > 0) {
			Piece piece = isWhitesMove() ? Piece.WQ : Piece.BQ;
			long emptyBB = getEmptyBB();
			while (emptyBB != 0) {
				Square toSquare = bitscanForward(emptyBB);
				addMove(new Move(toSquare, piece), moves);
				emptyBB = bitscanClear(emptyBB);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Move makeDropMove(Piece piece, Square endSquare)
			throws IllegalArgumentException {
//		// TODO: Validation
//		return new Move(destination, piece);
		Move move = null;

		PriorityMoveList<Move> moves = new PriorityMoveList<Move>();
		generatePseudoDropMoves(moves);
		for (Move pseudoLegal : moves) {
			if (pseudoLegal.isDrop()
					&& pseudoLegal.getPiece() == piece
					&& pseudoLegal.getTo() == endSquare) {
				try {
					forceMove(pseudoLegal);
					if (!isLegalPosition()) {
						throw new IllegalArgumentException("Invalid move (Illegal position): "
							+ piece.ch + "@" + GameUtils.getSan(endSquare) + " \n"
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
					+ piece.ch + "@" + GameUtils.getSan(endSquare) + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Overridden to add in drops and remove all drop moves from pseudoLegals.
	 * 
	 * {@inheritDoc}
	 */
	@Override
	protected Move makeSanMoveOverride(String shortAlgebraic, SanUtils.SanValidations validations, List<Move> pseudoLegals) {
		Move result = null;
		if (SanUtils.isValidDropStrict(validations.getStrictSan())) {
			for (Move move : getPseudoLegalMoves()) {
				if ((move.getMoveCharacteristic() & Move.DROP_CHARACTERISTIC) != 0
						&& move.getPiece().type == SanUtils.sanToPiece(validations
								.getStrictSan().charAt(0))
						&& move.getTo() == Square.getSquare((byte)RANK_FROM_SAN
								.indexOf(validations.getStrictSan().charAt(3)),
								(byte)FILE_FROM_SAN.indexOf(validations
										.getStrictSan().charAt(2)))) {
					result = move;
					move.setSan(shortAlgebraic);
					break;
				}
			}
		} else {
			for (Move move : pseudoLegals) {
				if (move.isDrop()) {
					pseudoLegals.set(pseudoLegals.indexOf(move), null);
				}
			}
		}
		return result;
	}

}
