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

public interface GameConstants {
	public static final int MOVE_REP_CACHE_SIZE = 1 << 12;
	public static final int MOVE_REP_CACHE_SIZE_MINUS_1 = MOVE_REP_CACHE_SIZE - 1;

	public static final short MAX_HALF_MOVES_IN_GAME = 600;
	public static final short MAX_LEGAL_MOVES = 600;

//	// bitboard coordinate constants
//	/** @deprecated Not intended for public use. */
//	public static final long A1 = 1L;
//	public static final long B1 = A1 << 1;
//	public static final long C1 = A1 << 2;
//	public static final long D1 = A1 << 3;
//	public static final long E1 = A1 << 4;
//	public static final long F1 = A1 << 5;
//	public static final long G1 = A1 << 6;
//	public static final long H1 = A1 << 7;
//
//	public static final long A2 = A1 << 8;
//	public static final long B2 = A1 << 9;
//	public static final long C2 = A1 << 10;
//	public static final long D2 = A1 << 11;
//	public static final long E2 = A1 << 12;
//	public static final long F2 = A1 << 13;
//	public static final long G2 = A1 << 14;
//	public static final long H2 = A1 << 15;
//
//	public static final long A3 = A1 << 16;
//	public static final long B3 = A1 << 17;
//	public static final long C3 = A1 << 18;
//	public static final long D3 = A1 << 19;
//	public static final long E3 = A1 << 20;
//	public static final long F3 = A1 << 21;
//	public static final long G3 = A1 << 22;
//	public static final long H3 = A1 << 23;
//
//	public static final long A4 = A1 << 24;
//	public static final long B4 = A1 << 25;
//	public static final long C4 = A1 << 26;
//	public static final long D4 = A1 << 27;
//	public static final long E4 = A1 << 28;
//	public static final long F4 = A1 << 29;
//	public static final long G4 = A1 << 30;
//	public static final long H4 = A1 << 31;
//
//	public static final long A5 = A1 << 32;
//	public static final long B5 = A1 << 33;
//	public static final long C5 = A1 << 34;
//	public static final long D5 = A1 << 35;
//	public static final long E5 = A1 << 36;
//	public static final long F5 = A1 << 37;
//	public static final long G5 = A1 << 38;
//	public static final long H5 = A1 << 39;
//
//	public static final long A6 = A1 << 40;
//	public static final long B6 = A1 << 41;
//	public static final long C6 = A1 << 42;
//	public static final long D6 = A1 << 43;
//	public static final long E6 = A1 << 44;
//	public static final long F6 = A1 << 45;
//	public static final long G6 = A1 << 46;
//	public static final long H6 = A1 << 47;
//
//	public static final long A7 = A1 << 48;
//	public static final long B7 = A1 << 49;
//	public static final long C7 = A1 << 50;
//	public static final long D7 = A1 << 51;
//	public static final long E7 = A1 << 52;
//	public static final long F7 = A1 << 53;
//	public static final long G7 = A1 << 54;
//	public static final long H7 = A1 << 55;
//
//	public static final long A8 = A1 << 56;
//	public static final long B8 = A1 << 57;
//	public static final long C8 = A1 << 58;
//	public static final long D8 = A1 << 59;
//	public static final long E8 = A1 << 60;
//	public static final long F8 = A1 << 61;
//	public static final long G8 = A1 << 62;
//	public static final long H8 = A1 << 63;

	// Square constants.
	public static enum Square {
		A1((byte)0), B1((byte)1), C1((byte)2), D1((byte)3),
		E1((byte)4), F1((byte)5), G1((byte)6), H1((byte)7),
		A2((byte)8), B2((byte)9), C2((byte)10), D2((byte)11),
		E2((byte)12), F2((byte)13), G2((byte)14), H2((byte)15),
		A3((byte)16), B3((byte)17), C3((byte)18), D3((byte)19),
		E3((byte)20), F3((byte)21), G3((byte)22), H3((byte)23),
		A4((byte)24), B4((byte)25), C4((byte)26), D4((byte)27),
		E4((byte)28), F4((byte)29), G4((byte)30), H4((byte)31),
		A5((byte)32), B5((byte)33), C5((byte)34), D5((byte)35),
		E5((byte)36), F5((byte)37), G5((byte)38), H5((byte)39),
		A6((byte)40), B6((byte)41), C6((byte)42), D6((byte)43),
		E6((byte)44), F6((byte)45), G6((byte)46), H6((byte)47),
		A7((byte)48), B7((byte)49), C7((byte)50), D7((byte)51),
		E7((byte)52), F7((byte)53), G7((byte)54), H7((byte)55),
		A8((byte)56), B8((byte)57), C8((byte)58), D8((byte)59),
		E8((byte)60), F8((byte)61), G8((byte)62), H8((byte)63),
		EMPTY((byte)64, "-"),
		// Used for droppable games and position setup.
		// Constants used to identify a drop square.
		WP_DROP_FROM_SQUARE((byte)65, "WP_DROP"),
		WN_DROP_FROM_SQUARE((byte)66, "WN_DROP"),
		WB_DROP_FROM_SQUARE((byte)67, "WB_DROP"),
		WR_DROP_FROM_SQUARE((byte)68, "WR_DROP"),
		WQ_DROP_FROM_SQUARE((byte)69, "WQ_DROP"),
		WK_DROP_FROM_SQUARE((byte)70, "WK_DROP"),
		BP_DROP_FROM_SQUARE((byte)71, "BP_DROP"),
		BN_DROP_FROM_SQUARE((byte)72, "BN_DROP"),
		BB_DROP_FROM_SQUARE((byte)73, "BB_DROP"),
		BR_DROP_FROM_SQUARE((byte)74, "BR_DROP"),
		BQ_DROP_FROM_SQUARE((byte)75, "BQ_DROP"),
		BK_DROP_FROM_SQUARE((byte)76, "BK_DROP");
		/** @deprecated */
		public final byte index;
		public final byte rank, file;
		public final long bit;
		public final String san;
		private Square(byte index) {
			this(index,
				String.valueOf(FILE_FROM_SAN.charAt(index % 8))
					+ String.valueOf(RANK_FROM_SAN.charAt(index / 8)));
		}
		private Square(byte index, String san) {
			this.index = index;
			this.rank = (byte)(index / 8);
			this.file = (byte)(index % 8);
			this.bit = index < 64 ? 1L << index : 0L;
			this.san = san;
		}
		public static final Square getSquare(byte rank, byte file) {
			return SQUARES[(rank * 8 + file)];
		}
	}
        public static Square[] SQUARES = Square.values();

	// Castle state constants.
	public static final byte CASTLE_NONE = 0;
	public static final byte CASTLE_SHORT = 1;
	public static final byte CASTLE_LONG = 2;
	public static final byte CASTLE_BOTH = CASTLE_SHORT | CASTLE_LONG;

	// Direction constants.
	public static final short NORTH = 1;
	public static final short SOUTH = 2;
	public static final short EAST = 4;
	public static final short WEST = 8;
	public static final short NORTHEAST = 16;
	public static final short NORTHWEST = 32;
	public static final short SOUTHEAST = 64;
	public static final short SOUTHWEST = 128;

	// Rank bitmaps
	public static final long RANK1 = Square.A1.bit | Square.B1.bit | Square.C1.bit | Square.D1.bit
		| Square.E1.bit | Square.F1.bit | Square.G1.bit | Square.H1.bit;
	public static final long RANK2 = Square.A2.bit | Square.B2.bit | Square.C2.bit | Square.D2.bit
		| Square.E2.bit | Square.F2.bit | Square.G2.bit | Square.H2.bit;
	public static final long RANK3 = Square.A3.bit | Square.B3.bit | Square.C3.bit | Square.D3.bit
		| Square.E3.bit | Square.F3.bit | Square.G3.bit | Square.H3.bit;
	public static final long RANK4 = Square.A4.bit | Square.B4.bit | Square.C4.bit | Square.D4.bit
		| Square.E4.bit | Square.F4.bit | Square.G4.bit | Square.H4.bit;
	public static final long RANK5 = Square.A5.bit | Square.B5.bit | Square.C5.bit | Square.D5.bit
		| Square.E5.bit | Square.F5.bit | Square.G5.bit | Square.H5.bit;
	public static final long RANK6 = Square.A6.bit | Square.B6.bit | Square.C6.bit | Square.D6.bit
		| Square.E6.bit | Square.F6.bit | Square.G6.bit | Square.H6.bit;
	public static final long RANK7 = Square.A7.bit | Square.B7.bit | Square.C7.bit | Square.D7.bit
		| Square.E7.bit | Square.F7.bit | Square.G7.bit | Square.H7.bit;
	public static final long RANK8 = Square.A8.bit | Square.B8.bit | Square.C8.bit | Square.D8.bit
		| Square.E8.bit | Square.F8.bit | Square.G8.bit | Square.H8.bit;

	public static final long RANK8_OR_RANK1 = RANK1 | RANK8;

//	public static final long NOT_RANK1 = ~RANK1;
//	public static final long NOT_RANK2 = ~RANK2;
//	public static final long NOT_RANK3 = ~RANK3;
//	public static final long NOT_RANK4 = ~RANK4;
//	public static final long NOT_RANK5 = ~RANK5;
//	public static final long NOT_RANK6 = ~RANK6;
//	public static final long NOT_RANK7 = ~RANK7;
//	public static final long NOT_RANK8 = ~RANK8;

	// File bitmaps
	public static final long AFILE = Square.A1.bit | Square.A2.bit | Square.A3.bit | Square.A4.bit
		| Square.A5.bit | Square.A6.bit | Square.A7.bit | Square.A8.bit;
	public static final long BFILE = Square.B1.bit | Square.B2.bit | Square.B3.bit | Square.B4.bit
		| Square.B5.bit | Square.B6.bit | Square.B7.bit | Square.B8.bit;
	public static final long CFILE = Square.C1.bit | Square.C2.bit | Square.C3.bit | Square.C4.bit
		| Square.C5.bit | Square.C6.bit | Square.C7.bit | Square.C8.bit;
	public static final long DFILE = Square.D1.bit | Square.D2.bit | Square.D3.bit | Square.D4.bit
		| Square.D5.bit | Square.D6.bit | Square.D7.bit | Square.D8.bit;
	public static final long EFILE = Square.E1.bit | Square.E2.bit | Square.E3.bit | Square.E4.bit
		| Square.E5.bit | Square.E6.bit | Square.E7.bit | Square.E8.bit;
	public static final long FFILE = Square.F1.bit | Square.F2.bit | Square.F3.bit | Square.F4.bit
		| Square.F5.bit | Square.F6.bit | Square.F7.bit | Square.F8.bit;
	public static final long GFILE = Square.G1.bit | Square.G2.bit | Square.G3.bit | Square.G4.bit
		| Square.G5.bit | Square.G6.bit | Square.G7.bit | Square.G8.bit;
	public static final long HFILE = Square.H1.bit | Square.H2.bit | Square.H3.bit | Square.H4.bit
		| Square.H5.bit | Square.H6.bit | Square.H7.bit | Square.H8.bit;

	public static final long NOT_AFILE = ~AFILE;
//	public static final long NOT_BFILE = ~BFILE;
//	public static final long NOT_CFILE = ~CFILE;
//	public static final long NOT_DFILE = ~DFILE;
//	public static final long NOT_EFILE = ~EFILE;
//	public static final long NOT_FFILE = ~FFILE;
//	public static final long NOT_GFILE = ~GFILE;
	public static final long NOT_HFILE = ~HFILE;

	// Piece type constants.
        public static enum PieceType {
            EMPTY((byte)0, "", ' '),
            PAWN((byte)1, "pawn", 'P'),
            KNIGHT((byte)2, "knight", 'N'),
            BISHOP((byte)3, "bishop", 'B'),
            ROOK((byte)4, "rook", 'R'),
            QUEEN((byte)5, "queen", 'Q'),
            KING((byte)6, "king", 'K');
            /** @deprecated */
            public final byte index;
            public final String name;
            public final char ch;
            private PieceType(byte index, String name, char ch) {
                this.index = index;
                this.name = name;
                this.ch = ch;
            }
        }
        public static PieceType[] PIECE_TYPES = PieceType.values();

	// Colored piece constants.
	// *NOTE* These are (were?) not used in the game class,
	// however they are useful for other classes.
        public static enum Piece {
            EMPTY(null, PieceType.EMPTY, ' '),
            WP(PieceColor.WHITE, PieceType.PAWN, '\u2659'),
            WN(PieceColor.WHITE, PieceType.KNIGHT, '\u2658'),
            WB(PieceColor.WHITE, PieceType.BISHOP, '\u2657'),
            WR(PieceColor.WHITE, PieceType.ROOK, '\u2656'),
            WQ(PieceColor.WHITE, PieceType.QUEEN, '\u2655'),
            WK(PieceColor.WHITE, PieceType.KING, '\u2654'),
            BP(PieceColor.BLACK, PieceType.PAWN, '\u265F'),
            BN(PieceColor.BLACK, PieceType.KNIGHT, '\u265E'),
            BB(PieceColor.BLACK, PieceType.BISHOP, '\u265D'),
            BR(PieceColor.BLACK, PieceType.ROOK, '\u265C'),
            BQ(PieceColor.BLACK, PieceType.QUEEN, '\u265B'),
            BK(PieceColor.BLACK, PieceType.KING, '\u265A'),
            // Promoted pieces
            WPX(Piece.WP),
            WNX(Piece.WN),
            WBX(Piece.WB),
            WRX(Piece.WR),
            WQX(Piece.WQ),
            WKX(Piece.WK),
            BPX(Piece.BP),
            BNX(Piece.BN),
            BBX(Piece.BB),
            BRX(Piece.BR),
            BQX(Piece.BQ),
            BKX(Piece.BK);
            public final PieceColor color;
            public final PieceType type;
            /** @deprecated */
            public final byte index;
            public final boolean promoted;
            public final char ch;
            private Piece(PieceColor color, PieceType type, char ch) {
                this.color = color;
                this.type = type;
                this.index = (byte)(type.index + (PieceColor.BLACK.equals(color) ? 6 : 0));
                this.promoted = false;
                this.ch = ch;
            }
            private Piece(Piece piece) {
                this.color = piece.color;
                this.type = piece.type;
                this.index = (byte)(piece.index + 12);
                this.promoted = true;
                this.ch = piece.ch;
            }
            /** @deprecated */
            public Piece getPiece() {
                switch (this) {
                    case WPX: return WP;
                    case WNX: return WN;
                    case WBX: return WB;
                    case WRX: return WR;
                    case WQX: return WQ;
                    case WKX: return WK;
                    case BPX: return BP;
                    case BNX: return BN;
                    case BBX: return BB;
                    case BRX: return BR;
                    case BQX: return BQ;
                    case BKX: return BK;
                }
                return this;
            }
            public Piece getPromotedPiece() {
                switch (this) {
                    case WP: return WPX;
                    case WN: return WNX;
                    case WB: return WBX;
                    case WR: return WRX;
                    case WQ: return WQX;
                    case WK: return WKX;
                    case BP: return BPX;
                    case BN: return BNX;
                    case BB: return BBX;
                    case BR: return BRX;
                    case BQ: return BQX;
                    case BK: return BKX;
                }
                return this;
            }
        };
        public static Piece[] PIECES = Piece.values();

	// Color constants.
        public static enum PieceColor {
            WHITE((byte)0, "White"), BLACK((byte)1, "Black");
            /** @deprecated */
            public final byte index;
            public final String description;
            private PieceColor(byte index, String description) {
                this.index = index;
                this.description = description;
            }
        }
        public static PieceColor[] PIECE_COLORS = PieceColor.values();

	public Piece[] DROPPABLE_PIECES = { Piece.WP, Piece.WN, Piece.WB, Piece.WR, Piece.WQ, Piece.WK,
		Piece.BP, Piece.BN, Piece.BB, Piece.BR, Piece.BQ, Piece.BK };

	public static final String RANK_FROM_SAN = "12345678";

	public static final String FILE_FROM_SAN = "abcdefgh";

	public static final String PIECE_FROM_SAN = " PNBRQK";

//	public static final String[] COLOR_PIECE_TO_CHAR = { "*PNBRQK", "*pnbrqk" };

	public static final String STARTING_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	public static final String STARTING_SUICIDE_POSITION_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w - - 0 1";

//	public static final long[] ZERO_BASED_RANK_INDEX_TO_BB = { RANK1, RANK2,
//			RANK3, RANK4, RANK5, RANK6, RANK7, RANK8 };
//
//	public static final long[] ZERO_BASED_FILE_INDEX_TO_BB = { AFILE, BFILE,
//			CFILE, DFILE, EFILE, FFILE, GFILE, HFILE };
//
//	public static final long BORDER = AFILE | HFILE | RANK1 | RANK8;
//	public static final long NOT_BORDER = ~BORDER;
//
//	public static final long[] KING_START = { E1, E8 };
//	public static final long[] ROOK_START = { A1 | H1, A8 | H8 };

}
