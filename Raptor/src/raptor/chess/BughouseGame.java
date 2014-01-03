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

/**
 * An implementation of a bughouse game. This approach involves linking two
 * bughouse games together. And setting the others droppable piece counts as
 * pieces are captured.
 * 
 * 
 * NOTE: the xoring for the zobrist is broken. It would need to be fixed to rely
 * on that for a computer program. Also this wont work for bgpn without some
 * changes.
 */
public class BughouseGame extends CrazyhouseGame {

	protected BughouseGame otherBoard;

	public BughouseGame() {
		super();
		setHeader(PgnHeader.Variant, Variant.bughouse.name());
	}

	// Leave this here. It will be needed when bgpn is implemented.
	/**
	 * Decrements the drop count of the other game.
	 */
	// @Override
	// protected void decrementDropCount(int color, int piece) {
	// piece = piece & PROMOTED_MASK;
	// if (otherBoard != null) {
	// otherBoard.dropCounts[color][piece] = otherBoard.dropCounts[color][piece]
	// - 1;
	// }
	// }
	/**
	 * @param ignoreHashes
	 *            Whether to include copying hash tables.
	 * @return An deep clone copy of this Game object.
	 */
	@Override
	public BughouseGame deepCopy(boolean ignoreHashes) {
		BughouseGame result = new BughouseGame();
		overwrite(result, ignoreHashes);
		return result;
	}

	public BughouseGame getOtherBoard() {
		return otherBoard;
	}

	// Leave this here. It will be needed when bgpn is implemented.
	/**
	 * Increments the drop count of the other game.
	 */
	// @Override
	// protected void incrementDropCount(int color, int piece) {
	// if ((piece & PROMOTED_MASK) != 0) {
	// piece = PAWN;
	// } else {
	// if (otherBoard != null) {
	// otherBoard.dropCounts[color][piece] = otherBoard.dropCounts[color][piece]
	// + 1;
	// }
	// }
	// }
	public void setOtherBoard(BughouseGame bughouseGame) {
		otherBoard = bughouseGame;
	}

}
