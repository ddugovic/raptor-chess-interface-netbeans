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
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections.iterators.IteratorChain;

public final class PriorityMoveList<Move> implements Iterable<Move> {
	private List<Move> highPriorityMoves = new ArrayList<Move>(GameConstants.MAX_LEGAL_MOVES);
	private List<Move> lowPriorityMoves = new ArrayList<Move>(GameConstants.MAX_LEGAL_MOVES);

	public void appendHighPriority(Move move) {
		highPriorityMoves.add(move);
	}

	public void appendLowPriority(Move move) {
		lowPriorityMoves.add(move);
	}

	public Iterator<Move> iterator() {
		return new IteratorChain(highPriorityMoves.iterator(), lowPriorityMoves.iterator());
	}

	/** @deprecated */
	public List<Move> asList() {
		List<Move> retVal = new ArrayList<Move>(highPriorityMoves);
		retVal.addAll(lowPriorityMoves);
		return retVal;
	}

	public List<Move> getHighPriorityMoves() {
		return highPriorityMoves;
	}

	public List<Move> getLowPriorityMoves() {
		return lowPriorityMoves;
	}

	public boolean isEmpty() {
		return lowPriorityMoves.isEmpty() && highPriorityMoves.isEmpty();
	}

}
