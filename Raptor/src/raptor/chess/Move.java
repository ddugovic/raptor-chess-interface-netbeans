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

import java.util.ArrayList;

import raptor.chess.pgn.Arrow;
import raptor.chess.pgn.Comment;
import raptor.chess.pgn.Highlight;
import raptor.chess.pgn.MoveAnnotation;
import raptor.chess.pgn.Nag;
import raptor.chess.pgn.SublineNode;
import raptor.chess.pgn.TimeTakenForMove;
import raptor.chess.util.GameUtils;

public class Move implements GameConstants, Comparable<Move> {
	public static final byte DOUBLE_PAWN_PUSH_CHARACTERISTIC = 4;
	public static final byte DROP_CHARACTERISTIC = 32;
	public static final byte EN_PASSANT_CHARACTERISTIC = 16;
	public static final byte LONG_CASTLING_CHARACTERISTIC = 2;
	public static final byte PROMOTION_CHARACTERISTIC = 8;
	public static final byte SHORT_CASTLING_CHARACTERISTIC = 1;

	/**
	 * May or may not be used. It is obviously not suitable to use this for a
	 * chess engine. That is why it starts out null.
	 */
	protected ArrayList<MoveAnnotation> annotations = null;
	protected Piece capture = Piece.EMPTY;
	protected byte castlingType = CASTLE_NONE;
	protected PieceColor color = PieceColor.WHITE;
	protected Square epSquare = Square.EMPTY;
	// Bytes are used because they take up less space than ints and there is no
	// need for the extra space.
	protected Square from = Square.EMPTY;
	/**
	 * May or may not be used.
	 */
	protected int fullMoveCount = 0;
	/**
	 * May or may not be used.
	 */
	protected int halfMoveCount = 0;
	protected byte lastWhiteCastlingState = CASTLE_NONE;
	protected byte lastBlackCastlingState = CASTLE_NONE;
	protected byte moveCharacteristic = 0;
	protected Piece piece = Piece.EMPTY;

	protected PieceType piecePromotedTo = PieceType.EMPTY;

	protected short previous50MoveCount = 0;

	/**
	 * May or may not be used.
	 */
	protected String san;

	protected Square to = Square.EMPTY;

	/**
	 * Used during rollbacks.
	 */
	protected String previousEcoHeader;

	/**
	 * Used during rollbacks.
	 */
	protected String previousOpeningHeader;

	/**
	 * Constructor for drop moves. From square will be set to the drop square
	 * for the piece.
	 */
	public Move(Square to, Piece piece) {
		this.to = to;
		this.piece = piece;
		this.color = piece.color;
		from = GameUtils.getDropSquareFromColoredPiece(GameUtils
				.getColoredPiece(piece.type, piece.color));
		moveCharacteristic = DROP_CHARACTERISTIC;
	}

	public Move(Square from, Square to, Piece piece, PieceColor color, Piece capture) {
		this.piece = piece;
		this.color = color;
		this.capture = capture;
		this.from = from;
		this.to = to;
	}

	public Move(Square from, Square to, Piece piece, PieceColor color, Piece capture,
			int moveCharacteristic) {
		this.piece = piece;
		this.color = color;
		this.capture = capture;
		this.from = from;
		this.to = to;
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	public Move(Square from, Square to, Piece piece, PieceColor color, Piece capture,
			PieceType piecePromotedTo, Square epSquare, int moveCharacteristic) {
		this.piece = piece;
		this.color = color;
		this.capture = capture;
		this.from = from;
		this.to = to;
		this.piecePromotedTo = piecePromotedTo;
		this.epSquare = epSquare;
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	public void addAnnotation(MoveAnnotation annotation) {
		if (annotations == null) {
			annotations = new ArrayList<MoveAnnotation>(5);
		}
		annotations.add(annotation);
	}

	public MoveAnnotation[] getAnnotations() {
		if (annotations == null) {
			return new MoveAnnotation[0];
		}

		return annotations.toArray(new MoveAnnotation[0]);
	}

	public Arrow[] getArrows() {
		if (annotations == null) {
			return new Arrow[0];
		}

		ArrayList<Arrow> result = new ArrayList<Arrow>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof Arrow) {
				result.add((Arrow) annotation);
			}
		}
		return result.toArray(new Arrow[0]);
	}

	/**
	 * Returns the capture without the promote mask.
	 */
	public Piece getCapture() {
		return capture;
	}

	public PieceColor getCaptureColor() {
		return GameUtils.getOppositeColor(getColor());
	}

	/**
	 * Returns the capture with the promote mask.
	 */
	public Piece getCaptureWithPromoteMask() {
		return capture;
	}

	public PieceColor getColor() {
		return color;
	}

	public Comment[] getComments() {
		if (annotations == null) {
			return new Comment[0];
		}

		ArrayList<Comment> result = new ArrayList<Comment>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof Comment) {
				result.add((Comment) annotation);
			}
		}
		return result.toArray(new Comment[0]);
	}

	public Square getEpSquare() {
		return epSquare;
	}

	public Square getFrom() {
		return from;
	}

	public int getFullMoveCount() {
		return fullMoveCount;
	}

	public int getHalfMoveCount() {
		return halfMoveCount;
	}

	public Highlight[] getHighlights() {
		if (annotations == null) {
			return new Highlight[0];
		}

		ArrayList<Highlight> result = new ArrayList<Highlight>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof Highlight) {
				result.add((Highlight) annotation);
			}
		}
		return result.toArray(new Highlight[0]);
	}

    public String getLan() {
        return isCastleShort() ? "O-O" : isCastleLong() ? "O-O-O"
                : isDrop() ? getPiece().type.ch
                + "@" + GameUtils.getSan(getTo()) : ""
                + GameUtils.getSan(getFrom())
                + "-"
                + GameUtils.getSan(getTo())
                + (isPromotion() ? "="
                + piecePromotedTo.ch : "");
    }

	public byte getLastBlackCastlingState() {
		return lastBlackCastlingState;
	}

	public byte getLastWhiteCastlingState() {
		return lastWhiteCastlingState;
	}

	public int getMoveCharacteristic() {
		return moveCharacteristic;
	}

	public Nag[] getNags() {
		if (annotations == null) {
			return new Nag[0];
		}

		ArrayList<Nag> result = new ArrayList<Nag>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof Nag) {
				result.add((Nag) annotation);
			}
		}
		return result.toArray(new Nag[0]);
	}

	public int getNumAnnotations() {
		return annotations.size();
	}

	public int getNumAnnotationsExcludingSublines() {
		int result = 0;

		for (MoveAnnotation annotation : annotations) {
			if (!(annotation instanceof SublineNode)) {
				result++;
			}
		}
		return result;
	}

	/**
	 * Returns the piece with its promotion mask.
	 * 
	 * @return
	 */
	public Piece getPiece() {
		return piece;
	}

	public PieceType getPiecePromotedTo() {
		return piecePromotedTo;
	}

	public short getPrevious50MoveCount() {
		return previous50MoveCount;
	}

	/**
	 * Used to reset the header during a rollback.
	 * 
	 * @param previousOpeningHeader
	 */
	public String getPreviousEcoHeader() {
		return previousEcoHeader;
	}

	/**
	 * Used to reset the header during a rollback.
	 * 
	 * @param previousOpeningHeader
	 */
	public String getPreviousOpeningHeader() {
		return previousOpeningHeader;
	}

	public String getSan() {
		return san;
	}

	public SublineNode[] getSublines() {
		if (annotations == null) {
			return new SublineNode[0];
		}

		ArrayList<SublineNode> result = new ArrayList<SublineNode>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof SublineNode) {
				result.add((SublineNode) annotation);
			}
		}
		return result.toArray(new SublineNode[0]);
	}

	public TimeTakenForMove[] getTimeTakenForMove() {
		if (annotations == null) {
			return new TimeTakenForMove[0];
		}

		ArrayList<TimeTakenForMove> result = new ArrayList<TimeTakenForMove>(3);
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof TimeTakenForMove) {
				result.add((TimeTakenForMove) annotation);
			}
		}
		return result.toArray(new TimeTakenForMove[0]);
	}

	public Square getTo() {
		return to;
	}

	public boolean hasNag() {
		if (annotations == null) {
			return false;
		}

		boolean result = false;
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof Nag) {
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean hasSubline() {
		if (annotations == null) {
			return false;
		}

		boolean result = false;
		for (MoveAnnotation annotation : annotations) {
			if (annotation instanceof SublineNode) {
				result = true;
				break;
			}
		}
		return result;
	}

	public boolean isCapture() {
		return getCaptureWithPromoteMask() != Piece.EMPTY;
	}

	public boolean isCastleLong() {
		return (moveCharacteristic & LONG_CASTLING_CHARACTERISTIC) != 0;
	}

	public boolean isCastleShort() {
		return (moveCharacteristic & SHORT_CASTLING_CHARACTERISTIC) != 0;
	}

	public boolean isDrop() {
		return (moveCharacteristic & DROP_CHARACTERISTIC) != 0;
	}

	public boolean isEnPassant() {
		return (moveCharacteristic & EN_PASSANT_CHARACTERISTIC) != 0;
	}

	public boolean isPromotion() {
		return piecePromotedTo != PieceType.EMPTY;
	}

	public boolean isWhitesMove() {
		return color == PieceColor.WHITE;
	}

	public void removeAnnotation(MoveAnnotation annotation) {
		if (annotations == null) {
			return;
		}
		annotations.remove(annotation);
	}

	public void setCapture(Piece capture) {
		this.capture = capture;
	}

	public void setColor(PieceColor color) {
		this.color = color;
	}

	public void setEpSquare(Square epSquare) {
		this.epSquare = epSquare;
	}

	public void setFrom(Square from) {
		this.from = from;
	}

	public void setFullMoveCount(int fullMoveCount) {
		this.fullMoveCount = fullMoveCount;
	}

	public void setHalfMoveCount(int halfMoveCount) {
		this.halfMoveCount = halfMoveCount;
	}

	public void setLastBlackCastlingState(int lastBlackCastlingState) {
		this.lastBlackCastlingState = (byte) lastBlackCastlingState;
	}

	public void setLastWhiteCastlingState(int lastWhiteCastlingState) {
		this.lastWhiteCastlingState = (byte) lastWhiteCastlingState;
	}

	public void setMoveCharacteristic(int moveCharacteristic) {
		this.moveCharacteristic = (byte) moveCharacteristic;
	}

	public void setPiece(Piece piece) {
		this.piece = piece;
	}

	public void setPiecePromotedTo(PieceType piecePromotedTo) {
		this.piecePromotedTo = piecePromotedTo;
	}

	public void setPrevious50MoveCount(short previous50MoveCount) {
		this.previous50MoveCount = previous50MoveCount;
	}

	/**
	 * Used to reset the header during a rollback.
	 * 
	 * @param previousOpeningHeader
	 */
	public void setPreviousEcoHeader(String previousEcoHeader) {
		this.previousEcoHeader = previousEcoHeader;
	}

	/**
	 * Used to reset the header during a rollback.
	 * 
	 * @param previousOpeningHeader
	 */
	public void setPreviousOpeningHeader(String previousOpeningHeader) {
		this.previousOpeningHeader = previousOpeningHeader;
	}

	public void setSan(String san) {
		this.san = san;
	}

	public void setTo(Square to) {
		this.to = to;
	}

	@Override
	public String toString() {
		return san != null ? san : getLan();
	}

	@Override
	public int compareTo(Move o) {
		return hashCode() - o.hashCode();
	}
}
