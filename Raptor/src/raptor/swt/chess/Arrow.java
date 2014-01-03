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
package raptor.swt.chess;

import org.eclipse.swt.graphics.Color;

import raptor.chess.GameConstants.Square;

/**
 * A class representing an arrow between two chess squares. There are two types
 * of arrows: those that fade away and those that do not.
 */
public class Arrow {
	public static final int ANIMATION_STAGES = 2;
	protected Square startSquare;
	protected Square endSquare;
	protected Color color;
	protected int frame;
	protected boolean isFadeAway;

	public Arrow(Square startSquare, Square endSquare, Color color) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		frame = -1;
	}

	public Arrow(Square startSquare, Square endSquare, Color color, boolean isFadeAway) {
		this.startSquare = startSquare;
		this.endSquare = endSquare;
		this.color = color;
		this.isFadeAway = isFadeAway;
		if (!isFadeAway) {
			frame = -1;
		} else {
			frame = ANIMATION_STAGES;
		}
	}

	@Override
	public boolean equals(Object object) {
		Arrow arrow = (Arrow) object;
		return startSquare == arrow.startSquare && endSquare == arrow.endSquare
				&& isFadeAway == arrow.isFadeAway;
	}

	public Color getColor() {
		return color;
	}

	public Square getEndSquare() {
		return endSquare;
	}

	public Square getStartSquare() {
		return startSquare;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setEndSquare(Square endSquare) {
		this.endSquare = endSquare;
	}

	public void setStartSquare(Square startSquare) {
		this.startSquare = startSquare;
	}
}
