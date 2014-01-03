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
import static raptor.chess.util.GameUtils.getOppositeColor;
import static raptor.chess.util.GameUtils.moveOne;

import java.util.EnumMap;
import java.util.Map;

import raptor.chess.pgn.PgnHeader;
import raptor.chess.util.GameUtils;

/**
 * A chess game which follows FICS atomic rules. Type help atomic on fics for
 * the rules.
 */
public class AtomicGame extends ClassicGame {

	public AtomicGame() {
		super();
		setHeader(PgnHeader.Variant, Variant.atomic.name());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AtomicGame deepCopy(boolean ignoreHashes) {
		AtomicGame result = new AtomicGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	/**
	 * Atomic chess doesn't require both kings to be on the board. {@inheritDoc}
	 */
	public boolean areBothKingsOnBoard() {
		return getPieceBB(getOppositeColor(getColorToMove()), PieceType.KING) != 0L;
	}

	/**
	 * Kings are allowed to touch. {@inheritDoc}
	 */
	@Override
	public boolean isInCheck(PieceColor color, long kingBB) {
		// If the king exploded the king is in check (for checkmate purposes).
		if (kingBB == 0L) {
			return true;
		}

		// You are not in check if the capture would explode the opponents king
		// (that is, if the kings are adjacent).
		boolean result = super.isInCheck(color, kingBB);
		if (result) {
			// from square,piece,and color are irrelevant since color is -1.
			Square kingSquare = bitscanForward(kingBB);
			for (Piece piece : getAtomicExplosionInfo(kingSquare, Square.EMPTY, Piece.EMPTY, color).values()) {
				if (piece.type == PieceType.KING) {
					result = false;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Adds the atomic move including the explosion info if its a capture.
	 * {@inheritDoc}
	 */
	@Override
	protected void addMove(Move move, PriorityMoveList moves) {
		if (move.isCapture()) {
			move = new AtomicMove(move);
			((AtomicMove)move).setAtomicExplosionInfo(getAtomicExplosionInfo(move.getTo(),
					move.getFrom(), getPiece(move.getFrom()),
					getColorToMove()));
		}
		super.addMove(move, moves);
	}

	/**
	 * Returns an array of AtomicExplosionInfo for the capture.
	 * 
	 * @param toSquare
	 *            The target capture square.
	 * @param fromSquare
	 *            The square the piece moved from. Can be -1 to ignore from
	 *            square (i.e. when using this method in isInCheck).
	 * @param pieceMoving
	 *            THe piece moving
	 * @param pieceMovingColor
	 *            THe color of the piece moving.
	 * @return An array of AtomicExplosionInfo detailing all of the exploded
	 *         pieces. THe piece being captured isn't exploded, but the piece
	 *         moving is.
	 */
	protected Map<GameConstants.Square, GameConstants.Piece> getAtomicExplosionInfo(Square toSquare,
			Square fromSquare, Piece pieceMoving, PieceColor pieceMovingColor) {
		Map<GameConstants.Square, GameConstants.Piece> explosionInfo =
			new EnumMap<GameConstants.Square, GameConstants.Piece>(GameConstants.Square.class);

		// Explode the piece moving on its destination square.
		if (fromSquare != null) {
			explosionInfo.put(toSquare, pieceMoving);
		}

		// Check all 8 squares around to square.
		// If the piece is not a pawn explode it.
		for (byte rank = (byte)(toSquare.rank - 1); rank <= (byte)(toSquare.rank + 1); rank++) {
			for (byte file = (byte)(toSquare.file - 1); file <= (byte)(toSquare.file + 1); file++) {
				if ((rank != toSquare.rank || file != toSquare.file) && GameUtils.isInBounds(rank, file)) {
					Square square = Square.getSquare(rank, file);
					if (square != fromSquare && board[square.index] != Piece.EMPTY
							&& board[square.index].type != PieceType.PAWN) {
						explosionInfo.put(square, board[square.index]);
					}
				}
			}
		}
		return explosionInfo;
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void makeEPMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		long captureBB = getColorToMove() == PieceColor.WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);

		Square captureSquare = bitscanForward(captureBB);

		xor(move.getColor(), move.getPiece().type, fromToBB);
		xor(move.getColor(), fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);

		xor(move.getCaptureColor(), move.getPiece().type, captureBB);
		xor(move.getCaptureColor(), captureBB);
		setOccupiedBB(getOccupiedBB() ^ captureBB);
		setEmptyBB(getEmptyBB() ^ captureBB);

		setPiece(move.getFrom(), Piece.EMPTY);
		setPiece(move.getTo(), Piece.EMPTY);
		setPiece(captureSquare, Piece.EMPTY);

		// Don't decrement/increment for piece captured. That will be done in forceMove.
		Map<GameConstants.Square, GameConstants.Piece> atomicExplosionInfo;
		if (move instanceof AtomicMove
			&& (atomicExplosionInfo = ((AtomicMove)move).getAtomicExplosionInfo()) != null) {
			for (Square square : atomicExplosionInfo.keySet()) {
				Piece piece = atomicExplosionInfo.get(square);
				long squareBB = getBitboard(square);
				xor(piece.color, piece.type, getBitboard(square));
				xor(piece.color, squareBB);
				setPiece(square, Piece.EMPTY);
				decrementPieceCount(piece.color, piece.type);
				incrementDropCount(piece.color, piece);

				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

		updateZobristEP(move, captureSquare);
		setEpSquare(Square.EMPTY);
	}

	/**
	 * Overridden to handle explosions. {@inheritDoc}
	 */
	@Override
	protected void makeNonEpNonCastlingMove(Move move) {
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;
		PieceColor oppositeColor = getOppositeColor(move.getColor());

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCaptureWithPromoteMask().type, toBB);
			xor(oppositeColor, toBB);
			updateZobristPOCapture(move, oppositeColor);

			xor(move.getColor(), move.getPiece().type, fromToBB);
			setPiece(move.getTo(), Piece.EMPTY);
			setPiece(move.getFrom(), Piece.EMPTY);

			// Don't decrement/increment for piece captured. That will be done in forceMove.
			Map<GameConstants.Square, GameConstants.Piece> atomicExplosionInfo;
			if (move instanceof AtomicMove
				&& (atomicExplosionInfo = ((AtomicMove)move).getAtomicExplosionInfo()) != null) {
				for (Square square : atomicExplosionInfo.keySet()) {
					Piece piece = atomicExplosionInfo.get(square);
					long squareBB = getBitboard(square);
					xor(piece.color, piece.type, getBitboard(square));
					xor(piece.color, squareBB);
					setPiece(square, Piece.EMPTY);
					decrementPieceCount(piece.color, piece.type);
					incrementDropCount(piece.color, piece);

					setOccupiedBB(getOccupiedBB() ^ squareBB);
					setEmptyBB(getEmptyBB() ^ squareBB);
				}
			}

		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);
			updateZobristPONoCapture(move, oppositeColor);
		}

		if (move.isPromotion() && !move.isCapture()) {
			if (move.isCapture()) {
				xor(move.getColor(), move.getPiece().type, fromBB);
			}

			xor(move.getColor(), move.getPiecePromotedTo(), toBB);

			setPiece(move.getTo(), GameUtils.getColoredPiece(move.getPiecePromotedTo(), move.getColor()).getPromotedPiece());
			setPiece(move.getFrom(), Piece.EMPTY);

			// capture is handled in forceMove.
			// promoted piece never has a promote mask only captures do.
			// Promotes do not effect drop pieces.
			decrementPieceCount(getColorToMove(), move.getPiece().type);
			incrementPieceCount(getColorToMove(), move.getPiecePromotedTo());
		} else if (!move.isCapture()) {
			xor(move.getColor(), move.getPiece().type, fromToBB);

			setPiece(move.getTo(), move.getPiece());
			setPiece(move.getFrom(), Piece.EMPTY);
		}

		switch (move.getPiece().type) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A1
					&& getColorToMove() == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A1 && getColorToMove() == PieceColor.BLACK) {
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H1
					&& getColorToMove() == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H1 && getColorToMove() == PieceColor.BLACK) {
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & CASTLE_LONG));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A8
					&& getColorToMove() == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A8 && getColorToMove() == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & CASTLE_SHORT));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H8
					&& getColorToMove() == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H8 && getColorToMove() == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & CASTLE_LONG));
			}
			break;
		}

		setEpSquare(move.getEpSquare());
	}

	/**
	 * Overridden to handle rolling back explosions. {@inheritDoc}
	 */
	@Override
	protected void rollbackEpMove(Move move) {
		PieceColor oppositeColor = getOppositeColor(getColorToMove());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		// Don't decrement/increment for piece captured. That will be done in forceMove.
		Map<GameConstants.Square, GameConstants.Piece> atomicExplosionInfo;
		if (move instanceof AtomicMove
			&& (atomicExplosionInfo = ((AtomicMove)move).getAtomicExplosionInfo()) != null) {
			for (Square square : atomicExplosionInfo.keySet()) {
				Piece piece = atomicExplosionInfo.get(square);
				long squareBB = getBitboard(square);
				xor(piece.color, piece.type, getBitboard(square));
				xor(piece.color, squareBB);
				setPiece(square, piece);
				incrementPieceCount(piece.color, piece.type);
				decrementDropCount(piece.color, piece);

				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

		long captureBB = oppositeColor == PieceColor.WHITE ? moveOne(SOUTH, toBB)
				: moveOne(NORTH, toBB);
		Square captureSquare = bitscanForward(captureBB);

		xor(oppositeColor, move.getPiece().type, fromToBB);
		xor(oppositeColor, fromToBB);
		setOccupiedBB(getOccupiedBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ fromToBB);
		setEmptyBB(getEmptyBB() ^ captureBB);
		setOccupiedBB(getOccupiedBB() ^ captureBB);

		xor(getColorToMove(), move.getCaptureWithPromoteMask().type, captureBB);
		xor(getColorToMove(), captureBB);

		setPiece(move.getTo(), Piece.EMPTY);
		setPiece(move.getFrom(), move.getPiece());
		setPiece(captureSquare, move.getCaptureWithPromoteMask());

		updateZobristEP(move, captureSquare);
		setEpSquareFromPreviousMove();
	}

	/**
	 * Overridden to handle rolling back explosions. {@inheritDoc}
	 */
	@Override
	protected void rollbackNonEpNonCastlingMove(Move move) {
		PieceColor oppositeColor = getOppositeColor(move.getColor());
		long fromBB = getBitboard(move.getFrom());
		long toBB = getBitboard(move.getTo());
		long fromToBB = fromBB ^ toBB;

		// Don't decrement/increment for piece captured. That will be done in forceMove.
		Map<GameConstants.Square, GameConstants.Piece> atomicExplosionInfo;
		if (move instanceof AtomicMove
			&& (atomicExplosionInfo = ((AtomicMove)move).getAtomicExplosionInfo()) != null) {
			for (Square square : atomicExplosionInfo.keySet()) {
				Piece piece = atomicExplosionInfo.get(square);
				long squareBB = getBitboard(square);
				xor(piece.color, piece.type,
						getBitboard(square));
				xor(piece.color, squareBB);
				setPiece(square, piece);
				incrementPieceCount(piece.color, piece.type);
				decrementDropCount(piece.color, piece);
				setOccupiedBB(getOccupiedBB() ^ squareBB);
				setEmptyBB(getEmptyBB() ^ squareBB);
			}
		}

		xor(move.getColor(), fromToBB);

		if (move.isCapture()) {
			setOccupiedBB(getOccupiedBB() ^ fromBB);
			setEmptyBB(getEmptyBB() ^ fromBB);

			xor(oppositeColor, move.getCaptureWithPromoteMask().type, toBB);
			xor(oppositeColor, toBB);

			updateZobristPOCapture(move, oppositeColor);

		} else {
			setOccupiedBB(getOccupiedBB() ^ fromToBB);
			setEmptyBB(getEmptyBB() ^ fromToBB);

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
}
