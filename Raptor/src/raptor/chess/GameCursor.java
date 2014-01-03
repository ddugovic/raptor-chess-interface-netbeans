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

import raptor.chess.pgn.PgnHeader;
import raptor.util.Logger;

/**
 * <p>
 * Decorates a game providing a way to set a cursor on different moves in the
 * games move list. It also provides forward,back,next,previous functionality on
 * the cursor. The game passed into the constructor is considered the master
 * game. The cursor game is the game the cursor is currently at.
 * </p>
 * <p>
 * The only modifications to a game you are allowed to make in this class are
 * moves and roll backs. Where the moves take place , on the cursor or on the
 * master, depends on the mode set.If you try and modify the state of the game
 * other than by making moves an UnsupportedOperationException will be raised.
 * </p>
 * <p>
 * Headers ,Result, and id are always retrieved from the master game. toPgn
 * always returns the pgn for the master game.By default the cursor is the
 * master game until the cursor is positioned on a move.
 * </p>
 * <p>
 * Cursor position 1 is the position after the first move is made.
 * </p>
 */
public class GameCursor implements Game {

	public enum Mode {
		/**
		 * All moves and are made on the cursor game. The move list reflects the
		 * cursor game. Commit replaces the master with the cursor. Revert
		 * reverses all moves made on the cursor game, and positions the cursor
		 * to the cursor position before any moves were made on the cursor game.
		 */
		MakeMovesOnCursor,
		/**
		 * All moves and are made on the master game, the one passed into the
		 * constructor. As moves are made the cursor is not updated. The move
		 * list reflects the master game. Revert and commit are not supported.
		 */
		MakeMovesOnMaster,
		/**
		 * All moves made are on the master game, the one passed into the
		 * constructor. The cursor is set to the last move made on every new
		 * move. The move list always reflects the master game. Revert and
		 * commit are not supported.
		 */
		MakeMovesOnMasterSetCursorToLast
	}

	static final Logger LOG = Logger.getLogger(GameCursor.class);

	protected Game cursor;
	protected int cursorPosition;
	protected int cursorPositionBeforeCursorMoves;
	protected boolean isInCursorSubline = false;
	protected Game master;
	protected Game masterBackup;
	protected Mode mode;

	public GameCursor(Game master, Mode mode) {
		this.master = master;
		setMode(mode);
		setCursorMasterLast();
	}

	public void addState(int state) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public boolean areBothKingsOnBoard() {
		return cursor.areBothKingsOnBoard();
	}

	public boolean canBlackCastleLong() {
		return cursor.canBlackCastleLong();
	}

	public boolean canBlackCastleShort() {
		return cursor.canBlackCastleShort();
	}

	public boolean canCommit() {
		return isInCursorSubline;
	}

	public boolean canRevert() {
		return isInCursorSubline;
	}

	public boolean canWhiteCastleLong() {
		return cursor.canWhiteCastleLong();
	}

	public boolean canWhiteCastleShort() {
		return cursor.canWhiteCastleShort();
	}

	public void clear() {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void clearState(int state) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	/**
	 * Behavior depends on the mode.
	 */
	public void commit() {
		if (canCommit()) {
			switch (mode) {
			case MakeMovesOnMaster:
				throw new UnsupportedOperationException(
						"This operation is not supported in mode " + mode);
			case MakeMovesOnMasterSetCursorToLast:
				throw new UnsupportedOperationException(
						"This operation is not supported in mode " + mode);
			case MakeMovesOnCursor:
				master = cursor;
				setCursorMasterLast();
				break;
			}
		}
	}

	public Game deepCopy(boolean ignoreHashes) {
		return cursor.deepCopy(ignoreHashes);
	}

	public void forceMove(Move move) {
		switch (mode) {
		case MakeMovesOnMaster:
			master.forceMove(move);
			break;
		case MakeMovesOnMasterSetCursorToLast:
			master.forceMove(move);
			setCursorMasterLast();
			break;
		case MakeMovesOnCursor:
			cursor.forceMove(move);
			adjustToCursorMove();
			break;
		}
	}

	public PgnHeader[] getAllHeaders() {
		return master.getAllHeaders();
	}

	public PgnHeader[] getAllNonRequiredHeaders() {
		return master.getAllNonRequiredHeaders();
	}

	public Piece[] getBoard() {
		return cursor.getBoard();
	}

	public byte getCastling(PieceColor color) {
		return cursor.getCastling(color);
	}

	public long getColorBB(PieceColor color) {
		return cursor.getColorBB(color);
	}

	public PieceColor getColorToMove() {
		return cursor.getColorToMove();
	}

	public Game getCursorGame() {
		return cursor;
	}

	public int getCursorPosition() {
		return cursorPosition;
	}

	public byte getDropCount(PieceColor color, PieceType piece) {
		return cursor.getDropCount(color, piece);
	}

	public long getEmptyBB() {
		return cursor.getEmptyBB();
	}

	public Square getEpSquare() {
		return cursor.getEpSquare();
	}

	public String getFenCastle() {
		return cursor.toFenPosition();
	}

	public int getFiftyMoveCount() {
		return cursor.getFiftyMoveCount();
	}

	public int getFullMoveCount() {
		return cursor.getFullMoveCount();
	}

	public int getHalfMoveCount() {
		return cursor.getHalfMoveCount();
	}

	public String getHeader(PgnHeader header) {
		return master.getHeader(header);
	}

	public String getId() {
		return master.getId();
	}

	public Square getInitialEpSquare() {
		return cursor.getInitialEpSquare();
	}

	public Move getLastMove() {
		return cursor.getLastMove();
	}

	public PriorityMoveList getLegalMoves() {
		return cursor.getLegalMoves();
	}

	public Game getMasterGame() {
		return master;
	}

	public int getMasterGameLength() {
		return master.getMoveList().getSize();
	}

	public Mode getMode() {
		return mode;
	}

	public MoveList getMoveList() {
		return master.getMoveList();
	}

	public long getNotColorToMoveBB() {
		return cursor.getNotColorToMoveBB();
	}

	public long getOccupiedBB() {
		return cursor.getOccupiedBB();
	}

	public Piece getPiece(Square square) {
		return cursor.getPiece(square);
	}

	public long getPieceBB(PieceColor color, PieceType piece) {
		return cursor.getPieceBB(color, piece);
	}

	public int getPieceCount(PieceColor color, PieceType piece) {
		return cursor.getPieceCount(color, piece);
	}

	public int[] getPieceJailCounts(PieceColor color) {
		return cursor.getPieceJailCounts(color);
	}

	public PriorityMoveList getPseudoLegalMoves() {
		return cursor.getPseudoLegalMoves();
	}

	public int getRepCount() {
		return cursor.getRepCount();
	}

	public int getRepHash() {
		return cursor.getRepHash();
	}

	public Result getResult() {
		return master.getResult();
	}

	public int getState() {
		return master.getState();
	}

	public Variant getVariant() {
		return master.getVariant();
	}

	public long getZobristGameHash() {
		return cursor.getZobristGameHash();
	}

	public long getZobristPositionHash() {
		return cursor.getZobristPositionHash();
	}

	public boolean hasFirst() {
		return cursorPosition > 0;
	}

	public boolean hasLast() {
		return cursorPosition < getMoveList().getSize();
	}

	public boolean hasNext() {
		return cursorPosition + 1 <= getMoveList().getSize();
	}

	public boolean hasPrevious() {
		return cursorPosition > 0;
	}

	public void incrementRepCount() {
		cursor.incrementRepCount();

	}

	public boolean isCheckmate() {
		return cursor.isCheckmate();
	}

	public boolean isCheckmate(PriorityMoveList moveList) {
		return cursor.isCheckmate(moveList);
	}

	public boolean isInCheck() {
		return cursor.isInCheck();
	}

	public boolean isInCheck(PieceColor color) {
		return cursor.isInCheck(color);
	}

	public boolean isInCheck(PieceColor color, long pieceBB) {
		return cursor.isInCheck(color, pieceBB);
	}

	public boolean isInState(int state) {
		return master.isInState(state);
	}

	public boolean isLegalPosition() {
		return cursor.isLegalPosition();
	}

	public boolean isSettingEcoHeaders() {
		return master.isSettingEcoHeaders();
	}

	public boolean isSettingMoveSan() {
		return master.isSettingMoveSan();
	}

	public boolean isStalemate() {
		return cursor.isStalemate();
	}

	public boolean isStalemate(PriorityMoveList moveList) {
		return cursor.isStalemate(moveList);
	}

	public boolean isWhitesMove() {
		return cursor.isWhitesMove();
	}

	public Move makeDropMove(Piece piece, Square destination) {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.makeDropMove(piece, destination);
		case MakeMovesOnMasterSetCursorToLast:
			Move result = master.makeDropMove(piece, destination);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			Move cursorResult = cursor.makeDropMove(piece, destination);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public Move makeLanMove(String lan) throws IllegalArgumentException {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.makeLanMove(lan);
		case MakeMovesOnMasterSetCursorToLast:
			Move result = master.makeLanMove(lan);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			Move cursorResult = cursor.makeLanMove(lan);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public Move makeMove(Square startSquare, Square endSquare)
			throws IllegalArgumentException {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.makeMove(startSquare, endSquare);
		case MakeMovesOnMasterSetCursorToLast:
			Move result = master.makeMove(startSquare, endSquare);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			Move cursorResult = cursor.makeMove(startSquare, endSquare);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public Move makeMove(Square startSquare, Square endSquare, PieceType promotePiece)
			throws IllegalArgumentException {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.makeMove(startSquare, endSquare, promotePiece);
		case MakeMovesOnMasterSetCursorToLast:
			Move result = master.makeMove(startSquare, endSquare, promotePiece);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			Move cursorResult = cursor.makeMove(startSquare, endSquare,
					promotePiece);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public Move makeSanMove(String shortAlgebraic)
			throws IllegalArgumentException {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.makeSanMove(shortAlgebraic);
		case MakeMovesOnMasterSetCursorToLast:
			Move result = master.makeSanMove(shortAlgebraic);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			Move cursorResult = cursor.makeSanMove(shortAlgebraic);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public boolean move(Move move) {
		switch (mode) {
		case MakeMovesOnMaster:
			return master.move(move);
		case MakeMovesOnMasterSetCursorToLast:
			boolean result = master.move(move);
			setCursorMasterLast();
			return result;
		case MakeMovesOnCursor:
			boolean cursorResult = cursor.move(move);
			adjustToCursorMove();
			return cursorResult;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public void overwrite(Game game, boolean ignoreHashes) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void removeHeader(PgnHeader headerName) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	/**
	 * Behavior depends on the mode.
	 */
	public void revert() {
		if (canRevert()) {
			switch (mode) {
			case MakeMovesOnMaster:
				throw new UnsupportedOperationException(
						"This operation is not supported in mode " + mode);
			case MakeMovesOnMasterSetCursorToLast:
				throw new UnsupportedOperationException(
						"This operation is not supported in mode " + mode);
			case MakeMovesOnCursor:
				master = masterBackup;
				masterBackup = null;
				isInCursorSubline = false;
				setCursor(cursorPositionBeforeCursorMoves);
				break;
			}
		}
	}

	public void rollback() {
		switch (mode) {
		case MakeMovesOnMaster:
			master.rollback();
			cursorPosition--;
			break;
		case MakeMovesOnMasterSetCursorToLast:
			master.rollback();
			cursorPosition--;
			break;
		case MakeMovesOnCursor:
			cursor.rollback();
			cursorPosition--;
			break;
		default:
			throw new IllegalStateException("Invalid mode: " + mode);
		}
	}

	public void setBoard(Piece[] board) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void setCastling(PieceColor color, byte castling) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setColorBB(PieceColor color, long bb) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setColorToMove(PieceColor color) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	/**
	 * Sets the cursor to the master game at the specified half move index. Half
	 * move index is based on the master games move list size.
	 */
	public void setCursor(int halfMoveIndex) {

		if (halfMoveIndex < 0) {
			LOG.info("Half move index must be greater than or equal to 0. "
					+ halfMoveIndex + " setting to 0.");
			halfMoveIndex = 0;
		}
		if (halfMoveIndex > getMoveList().getSize()) {
			LOG
					.info("Half move index must be less than the master games move list size."
							+ halfMoveIndex
							+ " setting to "
							+ getMoveList().getSize());
			halfMoveIndex = getMoveList().getSize();
		}

		if (cursorPosition != halfMoveIndex) {
			cursor = master.deepCopy(true);
			while (cursor.getMoveList().getSize() > halfMoveIndex) {
				cursor.rollback();
			}
		}
		cursorPosition = halfMoveIndex;

	}

	public void setCursorFirst() {
		if (hasFirst()) {
			setCursor(0);
		}
	}

	public void setCursorLast() {
		if (!isInCursorSubline) {
			setCursorMasterLast();
		} else {
			cursorPosition = cursor.getMoveList().getSize();
		}
	}

	public void setCursorMasterLast() {
		cursor = master;
		cursorPosition = getMoveList().getSize();
	}

	public void setCursorNext() {
		if (hasNext()) {
			setCursor(cursorPosition + 1);
		}
	}

	public void setCursorPrevious() {
		if (hasPrevious()) {
			setCursor(cursorPosition - 1);
		}
	}

	public void setDropCount(PieceColor color, PieceType piece, byte count) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setEmptyBB(long emptyBB) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setEpSquare(Square epSquare) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setFiftyMoveCount(short fiftyMoveCount) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setHalfMoveCount(short halfMoveCount) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setHeader(PgnHeader header, String value) {
		master.setHeader(header, value);
	}

	public void setId(String id) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setInitialEpSquare(Square initialEpSquare) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

//	public void setNotColorToMoveBB(long notColorToMoveBB) {
//		throw new UnsupportedOperationException(
//				"This operation is not supported in GameCursor");
//	}

	public void setOccupiedBB(long occupiedBB) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void setPiece(Square square, Piece piece) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public void setPieceBB(PieceColor color, PieceType piece, long bb) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setPieceCount(PieceColor color, PieceType piece, byte count) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setZobristGameHash(long hash) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");

	}

	public void setZobristPositionHash(long hash) {
		throw new UnsupportedOperationException(
				"This operation is not supported in GameCursor");
	}

	public String toFen() {
		return cursor.toFen();
	}

	public String toFenPosition() {
		return cursor.toFenPosition();
	}

	public String toPgn() {
		return master.toPgn();
	}

	protected void adjustToCursorMove() {
		if (!isInCursorSubline) {
			masterBackup = master;
			master = cursor;
			cursorPositionBeforeCursorMoves = cursorPosition;
		}
		cursorPosition = cursor.getMoveList().getSize();
		isInCursorSubline = true;
	}
}
