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
import static raptor.chess.util.GameUtils.getSan;
import raptor.chess.pgn.PgnHeader;

/**
 * Fischer Random Chess game.
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program.
 */
public class FischerRandomGame extends ClassicGame {

	protected byte initialLongRookFile;
	protected byte initialShortRookFile;
	protected byte initialKingFile;

	public FischerRandomGame() {
		setHeader(PgnHeader.Variant, Variant.fischerRandom.name());
		addState(Game.FISCHER_RANDOM_STATE);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public FischerRandomGame deepCopy(boolean ignoreHashes) {
		FischerRandomGame result = new FischerRandomGame();
		result.initialLongRookFile = initialLongRookFile;
		result.initialShortRookFile = initialShortRookFile;
		result.initialKingFile = initialKingFile;
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * This method should be invoked after the initial position is setup. It
	 * handles setting castling information used later on during the game.
	 */
	public void initialPositionIsSet() {
		/* This code tries to guess the initial rook files even if the positions isn't starting
		   there is a great possibility of a mistake in a middlegame, but this eliminates 
		   castling bug in most cases. */
		PieceColor sideToCheck;
		if ((getCastling(PieceColor.WHITE) & CASTLE_BOTH) != 0 
				&& getPieceCount(PieceColor.WHITE, PieceType.ROOK) == 2) 
			sideToCheck = PieceColor.WHITE;
		else
			sideToCheck = PieceColor.BLACK;
		
		initialKingFile = bitscanForward(getPieceBB(sideToCheck, PieceType.KING)).file;
		long rookBB = getPieceBB(sideToCheck, PieceType.ROOK);
		byte firstRook = bitscanForward(rookBB).file;
		rookBB = bitscanClear(rookBB);
		byte secondRook = bitscanForward(rookBB).file;
		if (firstRook < initialKingFile) {
			initialLongRookFile = firstRook;
			initialShortRookFile = secondRook;
		} else {
			initialLongRookFile = secondRook;
			initialShortRookFile = firstRook;
		}
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		FischerRandomUtils.generatePseudoKingCastlingMoves(this, fromBB, moves,
				initialKingFile, initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void makeCastlingMove(Move move) {
		FischerRandomUtils.makeCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}
	
	/**
	 * Overridden to suppress board castling triggers. 
	 */
	@Override
	public Move makeMove(Square startSquare, Square endSquare)
		throws IllegalArgumentException {
		Move move = null;

		PriorityMoveList<Move> legals = getLegalMoves();

		for (Move candidate : legals) {
			if (candidate.getFrom() == startSquare
					&& candidate.getTo() == endSquare && 
					!(candidate.isCastleLong() || candidate.isCastleShort())) {
				move = candidate;
			}
		}

		if (move == null) {
			throw new IllegalArgumentException("Invalid move: "
					+ getSan(startSquare) + " " + getSan(endSquare) + " \n"
					+ toString());
		} else {
			forceMove(move);
		}

		return move;
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void rollbackCastlingMove(Move move) {
		FischerRandomUtils.rollbackCastlingMove(this, move, initialKingFile,
				initialShortRookFile, initialLongRookFile);
	}

	/**
	 * Overridden to handle special FR castling rules.
	 */
	@Override
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		FischerRandomUtils.updateCastlingRightsForNonEpNonCastlingMove(this,
				move, initialShortRookFile, initialLongRookFile);
	}

}
