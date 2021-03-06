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
package raptor.connector.ics;

import java.util.StringTokenizer;
import raptor.chess.GameConstants;
import raptor.connector.ics.game.message.B1Message;

public class B1Parser implements GameConstants {
	public static final String B1_START = "<b1>";

	public static byte[] buildPieceHoldingsArray(String s) {
		byte[] result = new byte[6];

		for (char ch : s.toCharArray()) {
			switch (ch) {
			case 'P':
			case 'p':
				result[PieceType.PAWN.index] += 1;
				break;

			case 'N':
			case 'n':
				result[PieceType.KNIGHT.index] += 1;
				break;

			case 'B':
			case 'b':
				result[PieceType.BISHOP.index] += 1;
				break;

			case 'R':
			case 'r':
				result[PieceType.ROOK.index] += 1;
				break;

			case 'Q':
			case 'q':
				result[PieceType.QUEEN.index] += 1;
				break;
			case 'K':
			case 'k':
				result[PieceType.KING.index] += 1;
				break;
			default:
				throw new IllegalArgumentException("Invalid piece " + ch);
			}
		}

		return result;
	}

	public B1Message parse(String message) {
		if (message.startsWith(B1_START)) {
			StringTokenizer tok = new StringTokenizer(message, " {}><-\n");
			B1Message result = new B1Message();

			tok.nextToken();
			tok.nextToken();
			result.gameId = tok.nextToken();
			tok.nextToken();
			String whiteHoldings = tok.nextToken();
			result.whiteHoldings = buildPieceHoldingsArray(whiteHoldings
					.substring(1, whiteHoldings.length() - 1));
			tok.nextToken();
			String blackHoldings = tok.nextToken();
			result.blackHoldings = buildPieceHoldingsArray(blackHoldings
					.substring(1, blackHoldings.length() - 1));
			return result;
		}
		return null;
	}
}
