package raptor.chess;

import static raptor.chess.util.GameUtils.bitscanClear;
import static raptor.chess.util.GameUtils.bitscanForward;
import static raptor.chess.util.GameUtils.getBitboard;
import raptor.chess.pgn.PgnHeader;

public class WildGame extends ClassicGame {

	private byte whiteKingFile, whiteLongRookFile, whiteShortRookFile;
	
	private byte blackKingFile, blackLongRookFile, blackShortRookFile;

	public WildGame() {
		setHeader(PgnHeader.Variant, Variant.wild.name());
		addState(Game.FISCHER_RANDOM_STATE);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public WildGame deepCopy(boolean ignoreHashes) {
		WildGame result = new WildGame();
		result.whiteLongRookFile = whiteLongRookFile;
		result.whiteShortRookFile = whiteShortRookFile;
		result.blackLongRookFile = blackLongRookFile;
		result.blackShortRookFile = blackShortRookFile;
		result.blackKingFile = blackKingFile;
		result.whiteKingFile = whiteKingFile;
		overwrite(result, ignoreHashes);
		return result;
	}
	
	public void initialPositionIsSet() {
		whiteKingFile = bitscanForward(getPieceBB(PieceColor.WHITE, PieceType.KING)).file;
		long rookBB = getPieceBB(PieceColor.WHITE, PieceType.ROOK);
		byte firstRook = bitscanForward(rookBB).file;
		rookBB = bitscanClear(rookBB);
		byte secondRook = bitscanForward(rookBB).file;
		if (firstRook < whiteKingFile) {
			whiteLongRookFile = whiteKingFile == 4 ? 
					firstRook : secondRook;
			whiteShortRookFile = whiteKingFile == 4 ? 
					secondRook : firstRook;
		} else {
			whiteLongRookFile = whiteKingFile == 4 ? 
					secondRook : firstRook;
			whiteShortRookFile = firstRook;
		}
		
		blackKingFile = bitscanForward(getPieceBB(PieceColor.BLACK, PieceType.KING)).file;
		rookBB = getPieceBB(PieceColor.BLACK, PieceType.ROOK);
		firstRook = bitscanForward(rookBB).file;
		rookBB = bitscanClear(rookBB);
		secondRook = bitscanForward(rookBB).file;
		if (firstRook < blackKingFile) {
			blackLongRookFile = blackKingFile == 4 ? 
					firstRook : secondRook;
			blackShortRookFile = blackKingFile == 4 ? 
					secondRook : firstRook;
		} else {
			blackLongRookFile = blackKingFile == 4 ? 
					secondRook : firstRook;
			blackShortRookFile = firstRook;
		}
	}
	
	protected void generatePseudoKingCastlingMoves(long fromBB,
			PriorityMoveList moves) {
		
		Square kingSquare = getColorToMove() == PieceColor.WHITE ? Square.getSquare((byte)0,
				whiteKingFile) : Square.getSquare((byte)7, blackKingFile);
		long kingSquareBB = getBitboard(kingSquare);
		
		if (getColorToMove() == PieceColor.WHITE
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB ) {
			
			if (whiteKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, (byte)0, whiteKingFile,
					whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.F1, PieceColor.WHITE,
							whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.G1, PieceColor.WHITE,
							whiteShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							Square.G1, PieceColor.WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, Square.G1, Piece.WK,
						PieceColor.WHITE, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, (byte)0, whiteShortRookFile,
					whiteKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.C1, PieceColor.WHITE,
							whiteShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.B1, PieceColor.WHITE,
							whiteShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							Square.C1, PieceColor.WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, Square.B1, Piece.WK,
						PieceColor.WHITE, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
			}
			
		}
		
		if (getColorToMove() == PieceColor.WHITE
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB ) {
			
			if (whiteKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, (byte)0, whiteLongRookFile,
					whiteKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.D1, PieceColor.WHITE,
							whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.C1, PieceColor.WHITE,
							whiteLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							Square.C1, PieceColor.WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, Square.C1, Piece.WK,
						PieceColor.WHITE, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, (byte)0, whiteKingFile,
					whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.E1, PieceColor.WHITE,
							whiteLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.F1, PieceColor.WHITE,
							whiteLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this, kingSquare,
							Square.F1, PieceColor.WHITE)) {
				moves.appendLowPriority(new Move(kingSquare, Square.F1, Piece.WK,
						PieceColor.WHITE, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
		}	
		
		if (getColorToMove() == PieceColor.BLACK
				&& (getCastling(getColorToMove()) & CASTLE_SHORT) != 0
				&& fromBB == kingSquareBB) {
			
			if (blackKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, (byte)7,
							blackKingFile, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.F8,
							PieceColor.BLACK, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.G8,
							PieceColor.BLACK, blackShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, Square.G8, PieceColor.BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, Square.G8, Piece.BK,
						PieceColor.BLACK, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
				
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, (byte)7,
							blackShortRookFile, blackKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.C8,
							PieceColor.BLACK, blackShortRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.B8,
							PieceColor.BLACK, blackShortRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, Square.B8, PieceColor.BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, Square.B8, Piece.BK,
						PieceColor.BLACK, Piece.EMPTY, Move.SHORT_CASTLING_CHARACTERISTIC));
			}
		}
		if (getColorToMove() == PieceColor.BLACK
				&& (getCastling(getColorToMove()) & CASTLE_LONG) != 0
				&& fromBB == kingSquareBB) {
			
			if (blackKingFile == 4
					&& FischerRandomUtils.emptyBetweenFiles(this, (byte)7,
							blackLongRookFile, blackKingFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.D8,
							PieceColor.BLACK, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.C8,
							PieceColor.BLACK, blackLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, Square.C8, PieceColor.BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, Square.C8, Piece.BK,
						PieceColor.BLACK, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
			else if (FischerRandomUtils.emptyBetweenFiles(this, (byte)7,
					        blackKingFile, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.E8,
							PieceColor.BLACK, blackLongRookFile)
					&& FischerRandomUtils.isKingEmptyOrRook(this, Square.F8,
							PieceColor.BLACK, blackLongRookFile)
					&& !FischerRandomUtils.isCastlePathInCheck(this,
							kingSquare, Square.F8, PieceColor.BLACK)) {
				moves.appendLowPriority(new Move(kingSquare, Square.F8, Piece.BK,
						PieceColor.BLACK, Piece.EMPTY, Move.LONG_CASTLING_CHARACTERISTIC));
			}
		}
		
	}
	
	public void makeCastlingMove(Move move) {
		Piece king = Piece.EMPTY, rook = Piece.EMPTY;
		Square kingFromSquare = move.getColor() == PieceColor.WHITE ? Square.getSquare((byte)0,
				whiteKingFile) : Square.getSquare((byte)7, blackKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB = 0, rookFromBB = 0, rookToBB = 0;
		Square rookFromSquare = Square.EMPTY;

		if (move.getColor() == PieceColor.WHITE) {
			king = Piece.WK;
			rook = Piece.WR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)0, whiteShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = Square.G1.bit;
					rookToBB = Square.F1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.G1, Square.F1);
				}
				else {
					kingToBB = Square.B1.bit;
					rookToBB = Square.C1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.B1, Square.C1);
				}
				
			}
			else {
				rookFromSquare = Square.getSquare((byte)0, whiteLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = Square.C1.bit;
					rookToBB = Square.D1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.C1, Square.D1);
				}
				else {
					kingToBB = Square.F1.bit;
					rookToBB = Square.E1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.F1, Square.E1);
				}
			}
		}
		else if (move.getColor() == PieceColor.BLACK) {
			king = Piece.BK;
			rook = Piece.BR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)7, blackShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = Square.G8.bit;
					rookToBB = Square.F8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.G8, Square.F8);
				}
				else {
					kingToBB = Square.B8.bit;
					rookToBB = Square.C8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.B8, Square.C8);
				}
				
			}
			else {
				rookFromSquare = Square.getSquare((byte)7, blackLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = Square.C8.bit;
					rookToBB = Square.D8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.C8, Square.D8);
				}
				else {
					kingToBB = Square.F8.bit;
					rookToBB = Square.E8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.F8, Square.E8);
				}
			}
		}
		
		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			setPiece(bitscanForward(kingToBB), king);
			setPiece(kingFromSquare, Piece.EMPTY);
			xor(move.getColor(), PieceType.KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), rook);
			if (rookFromBB != kingToBB) {
				setPiece(rookFromSquare, Piece.EMPTY);
			}
			xor(move.getColor(), PieceType.ROOK, rookFromTo);

			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		setCastling(getColorToMove(), CASTLE_NONE);
		setEpSquare(Square.EMPTY);
	}
	
	protected void rollbackCastlingMove(Move move) {
		Piece king = Piece.EMPTY, rook = Piece.EMPTY;
		Square kingFromSquare = move.getColor() == PieceColor.WHITE ? Square.getSquare((byte)0,
				whiteKingFile) : Square.getSquare((byte)7, blackKingFile);
		long kingFromBB = getBitboard(kingFromSquare);
		long kingToBB = 0, rookFromBB = 0, rookToBB = 0;
		Square rookFromSquare = Square.EMPTY;

		if (move.getColor() == PieceColor.WHITE) {
			king = Piece.WK;
			rook = Piece.WR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)0, whiteShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = Square.G1.bit;
					rookToBB = Square.F1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.G1, Square.F1);
				}
				else {
					kingToBB = Square.B1.bit;
					rookToBB = Square.C1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.B1, Square.C1);
				}
				
			}
			else {
				rookFromSquare = Square.getSquare((byte)0, whiteLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (whiteKingFile == 4) {
					kingToBB = Square.C1.bit;
					rookToBB = Square.D1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.C1, Square.D1);
				}
				else {
					kingToBB = Square.F1.bit;
					rookToBB = Square.E1.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.WHITE, kingFromSquare,
							rookFromSquare, Square.F1, Square.E1);
				}
			}
		}
		else if (move.getColor() == PieceColor.BLACK) {
			king = Piece.BK;
			rook = Piece.BR;
			if (move.getMoveCharacteristic() == Move.SHORT_CASTLING_CHARACTERISTIC) {
				rookFromSquare = Square.getSquare((byte)7, blackShortRookFile);
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = Square.G8.bit;
					rookToBB = Square.F8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.G8, Square.F8);
				}
				else {
					kingToBB = Square.B8.bit;
					rookToBB = Square.C8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.B8, Square.C8);
				}
				
			}
			else {
				rookFromSquare = Square.getSquare((byte)7, blackLongRookFile);				
				rookFromBB = getBitboard(rookFromSquare);
				if (blackKingFile == 4) {
					kingToBB = Square.C8.bit;
					rookToBB = Square.D8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.C8, Square.D8);
				}
				else {
					kingToBB = Square.F8.bit;
					rookToBB = Square.E8.bit;
					FischerRandomUtils.updateZobristCastle(this, PieceColor.BLACK, kingFromSquare,
							rookFromSquare, Square.F8, Square.E8);
				}
			}
		}
		
		if (rookFromBB != rookToBB) {
			long rookFromTo = rookToBB | rookFromBB;
			setPiece(bitscanForward(rookToBB), Piece.EMPTY);
			setPiece(rookFromSquare, rook);
			xor(move.getColor(), PieceType.ROOK, rookFromTo);
			xor(move.getColor(), rookFromTo);
			setOccupiedBB(getOccupiedBB() ^ rookFromTo);
			setEmptyBB(getEmptyBB() ^ rookFromTo);
		}

		if (kingToBB != kingFromBB) {
			long kingFromTo = kingToBB | kingFromBB;
			if (kingToBB != rookFromBB) {
				setPiece(bitscanForward(kingToBB), Piece.EMPTY);
			}
			setPiece(kingFromSquare, king);
			xor(move.getColor(), PieceType.KING, kingFromTo);
			xor(move.getColor(), kingFromTo);
			setOccupiedBB(getOccupiedBB() ^ kingFromTo);
			setEmptyBB(getEmptyBB() ^ kingFromTo);
		}

		setEpSquareFromPreviousMove();
	}
	
	protected void updateCastlingRightsForNonEpNonCastlingMove(Move move) {
		switch (move.getPiece().type) {
		case KING:
			setCastling(getColorToMove(), CASTLE_NONE);
			break;
		default:
			if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A1
					&& getColorToMove() == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A1 && getColorToMove() == PieceColor.BLACK) {				
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & 
						(whiteKingFile == 4 ? CASTLE_SHORT : CASTLE_LONG)));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H1
					&& getColorToMove() == PieceColor.WHITE || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H1 && getColorToMove() == PieceColor.BLACK) {
				setCastling(PieceColor.WHITE, (byte)(getCastling(PieceColor.WHITE) & 
						(whiteKingFile == 4 ? CASTLE_LONG : CASTLE_SHORT)));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.A8
					&& getColorToMove() == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.A8 && getColorToMove() == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & 
						(blackKingFile == 4 ? CASTLE_SHORT : CASTLE_LONG)));
			} else if (move.getPiece().type == PieceType.ROOK && move.getFrom() == Square.H8
					&& getColorToMove() == PieceColor.BLACK || move.getCaptureWithPromoteMask().type == PieceType.ROOK
					&& move.getTo() == Square.H8 && getColorToMove() == PieceColor.WHITE) {
				setCastling(PieceColor.BLACK, (byte)(getCastling(PieceColor.BLACK) & 
						(blackKingFile == 4 ? CASTLE_LONG : CASTLE_SHORT)));
			}
			break;
		}
	}

}
