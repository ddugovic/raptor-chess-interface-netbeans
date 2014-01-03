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

import java.util.ArrayList;
import java.util.List;

import raptor.util.Logger;
 
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Transform;

import raptor.Raptor;
import raptor.chess.GameConstants;
import raptor.chess.GameConstants.Square;
import raptor.chess.util.GameUtils;
import raptor.pref.PreferenceKeys;

/**
 * A class which manages GUI arrow decorations between squares on a chess board.
 * Currently drop or piece jail squares are not supported.
 * 
 * All arrow segments are from the perspective of the white pieces being placed
 * on the bottom.
 */
public class ArrowDecorator {
	/**
	 * Contains the code to draw an arrow segment on a square. Arrow segments
	 * are always drawn from the perspective of what being on the bottom. The
	 * rotate180 will return the segment to use if white is on the top.
	 */
	protected static enum ArrowSegment {

		UpwardTurnLeft, UpwardTurnRight, DownwardTurnLeft, DownwardTurnRight, DiagIncreasing, DiagDecreasing, Horizontal, Vertical, DiagNorthEastCorner, DiagNorthWestCorner, DiagSouthEastCorner, DiagSouthWestCorner, OriginVerticalUp, OriginVerticalDown, OriginHorizontalLeft, OriginHorizontalRight, OriginDiagIncreasingLeft, OriginDiagIncreasingRight, OriginDiagDecreasingLeft, OriginDiagDecreasingRight, DestinationVerticalUp, DestinationVerticalDown, DestinationHorizontalLeft, DestinationHorizontalRight, DestinationDiagIncreasingLeft, DestinationDiagIncreasingRight, DestinationDiagDecreasingLeft, DestinationDiagDecreasingRight;

		private ArrowSegment() {

		}

		public void draw(ChessSquare square, Color color, int width, GC gc) {
			switch (this) {
			case UpwardTurnLeft:
				drawUpwardTurnLeft(square, color, width, gc);
				break;
			case UpwardTurnRight:
				drawUpwardTurnRight(square, color, width, gc);
				break;
			case DownwardTurnLeft:
				drawDownwardTurnLeft(square, color, width, gc);
				break;
			case DownwardTurnRight:
				drawDownwardTurnRight(square, color, width, gc);
				break;
			case DiagIncreasing:
				drawDiagIncreasing(square, color, width, gc);
				break;
			case DiagDecreasing:
				drawDiagDecreasing(square, color, width, gc);
				break;
			case Horizontal:
				drawHorizontal(square, color, width, gc);
				break;
			case Vertical:
				drawVertical(square, color, width, gc);
				break;
			case DiagNorthEastCorner:
				drawDiagNorthEastCorner(square, color, width, gc);
				break;
			case DiagNorthWestCorner:
				drawDiagNorthWestCorner(square, color, width, gc);
				break;
			case DiagSouthEastCorner:
				drawDiagSouthEastCorner(square, color, width, gc);
				break;
			case DiagSouthWestCorner:
				drawDiagSouthWestCorner(square, color, width, gc);
				break;
			case OriginVerticalUp:
				drawOriginVerticalUp(square, color, width, gc);
				break;
			case OriginVerticalDown:
				drawOriginVerticalDown(square, color, width, gc);
				break;
			case OriginHorizontalLeft:
				drawOriginHorizontalLeft(square, color, width, gc);
				break;
			case OriginHorizontalRight:
				drawOriginHorizontalRight(square, color, width, gc);
				break;
			case OriginDiagIncreasingLeft:
				drawOriginDiagIncreasingLeft(square, color, width, gc);
				break;
			case OriginDiagIncreasingRight:
				drawOriginDiagIncreasingRight(square, color, width, gc);
				break;
			case OriginDiagDecreasingLeft:
				drawOriginDiagDecreasingLeft(square, color, width, gc);
				break;
			case OriginDiagDecreasingRight:
				drawOriginDiagDecreasingRight(square, color, width, gc);
				break;
			case DestinationVerticalUp:
				drawDestinationVerticalUp(square, color, width, gc);
				break;
			case DestinationVerticalDown:
				drawDestinationVerticalDown(square, color, width, gc);
				break;
			case DestinationHorizontalLeft:
				drawDestinationHorizontalLeft(square, color, width, gc);
				break;
			case DestinationHorizontalRight:
				drawDestinationHorizontalRight(square, color, width, gc);
				break;
			case DestinationDiagIncreasingLeft:
				drawDestinationDiagIncreasingLeft(square, color, width, gc);
				break;
			case DestinationDiagIncreasingRight:
				drawDestinationDiagIncreasingRight(square, color, width, gc);
				break;
			case DestinationDiagDecreasingLeft:
				drawDestinationDiagDecreasingLeft(square, color, width, gc);
				break;
			case DestinationDiagDecreasingRight:
				drawDestinationDiagDecreasingRight(square, color, width, gc);
				break;
			}
		}

		public ArrowSegment rotate180() {
			switch (this) {
			case UpwardTurnLeft:
				return DownwardTurnRight;
			case UpwardTurnRight:
				return DownwardTurnLeft;
			case DownwardTurnLeft:
				return UpwardTurnRight;
			case DownwardTurnRight:
				return UpwardTurnLeft;
			case DiagIncreasing:
				return DiagIncreasing;
			case DiagDecreasing:
				return DiagDecreasing;
			case Horizontal:
				return Horizontal;
			case Vertical:
				return Vertical;
			case DiagNorthEastCorner:
				return DiagSouthWestCorner;
			case DiagNorthWestCorner:
				return DiagSouthEastCorner;
			case DiagSouthEastCorner:
				return DiagNorthWestCorner;
			case DiagSouthWestCorner:
				return DiagNorthEastCorner;
			case OriginVerticalUp:
				return OriginVerticalDown;
			case OriginVerticalDown:
				return OriginVerticalUp;
			case OriginHorizontalLeft:
				return OriginHorizontalRight;
			case OriginHorizontalRight:
				return OriginHorizontalLeft;
			case OriginDiagIncreasingLeft:
				return OriginDiagIncreasingRight;
			case OriginDiagIncreasingRight:
				return OriginDiagIncreasingLeft;
			case OriginDiagDecreasingLeft:
				return OriginDiagDecreasingRight;
			case OriginDiagDecreasingRight:
				return OriginDiagDecreasingLeft;
			case DestinationVerticalUp:
				return DestinationVerticalDown;
			case DestinationVerticalDown:
				return DestinationVerticalUp;
			case DestinationHorizontalLeft:
				return DestinationHorizontalRight;
			case DestinationHorizontalRight:
				return DestinationHorizontalLeft;
			case DestinationDiagIncreasingLeft:
				return DestinationDiagIncreasingRight;
			case DestinationDiagIncreasingRight:
				return DestinationDiagIncreasingLeft;
			case DestinationDiagDecreasingLeft:
				return DestinationDiagDecreasingRight;
			case DestinationDiagDecreasingRight:
				return DestinationDiagDecreasingLeft;
			default:
				throw new IllegalArgumentException("Invalid ArrowSegment: "
						+ this);
			}
		}

		private void drawDestinationDiagDecreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, squareSide);
			tr.rotate(-135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagDecreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.rotate(45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagIncreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, 0);
			gc.setTransform(tr);
			tr.rotate(135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationDiagIncreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(0, squareSide);
			gc.setTransform(tr);
			tr.rotate(-45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDiagDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationHorizontalLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(180);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationHorizontalRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(0, squareSide / 2.0F);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationVerticalDown(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, 0);
			gc.setTransform(tr);

			tr.rotate(90);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDestinationVerticalUp(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, squareSide);
			gc.setTransform(tr);

			tr.rotate(-90);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getDestinationPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawDiagDecreasing(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;
			int halfWidth = getHalfWidth(width);

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, halfWidth, squareSide - halfWidth,
					squareSide, squareSide, squareSide, squareSide,
					squareSide - halfWidth, halfWidth, 0, 0, 0 });
		}

		private void drawDiagIncreasing(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;
			int halfWidth = getHalfWidth(width);

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, squareSide - halfWidth,
					squareSide - halfWidth, 0, squareSide, 0, squareSide,
					halfWidth, halfWidth, squareSide, 0, squareSide });
		}

		private void drawDiagNorthEastCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;
			int halfWidth = getHalfWidth(width);

			gc.setBackground(color);
			gc.fillPolygon(new int[] { squareSide - halfWidth, 0, squareSide,
					halfWidth, squareSide, 0 });
		}

		private void drawDiagNorthWestCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int halfWidth = getHalfWidth(width);
			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, halfWidth, halfWidth, 0, 0, 0 });
		}

		private void drawDiagSouthEastCorner(ChessSquare square, Color color,
				int width, GC gc) {

			int squareSide = square.getSize().x;
			int halfWidth = getHalfWidth(width);

			gc.setBackground(color);
			gc
					.fillPolygon(new int[] { squareSide - halfWidth,
							squareSide, squareSide, squareSide - halfWidth,
							squareSide, squareSide, });
		}

		private void drawDiagSouthWestCorner(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().x;
			int halfWidth = getHalfWidth(width);

			gc.setBackground(color);
			gc.fillPolygon(new int[] { 0, squareSide - halfWidth, halfWidth,
					squareSide, 0, squareSide, });
		}

		private void drawDownwardTurnLeft(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(-halfSquareSide, -halfSquareSide - halfWidth + i,
						squareSide, squareSide, 0, -95);
				gc.drawArc(-halfSquareSide - halfWidth + i, -halfSquareSide,
						squareSide, squareSide, 0, -95);
			}
		}

		private void drawDownwardTurnRight(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(halfSquareSide, -halfSquareSide - halfWidth + i,
						squareSide, squareSide, 270, -95);
				gc.drawArc(halfSquareSide + -halfWidth + i, -halfSquareSide,
						squareSide, squareSide, 270, -95);
			}
		}

		private void drawHorizontal(ChessSquare square, Color color, int width,
				GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setBackground(color);
			gc.fillRectangle(0, halfSquareSide - halfWidth, squareSide, width);
		}

		private void drawOriginDiagDecreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());

			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(-135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagDecreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagIncreasingLeft(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(135);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginDiagIncreasingRight(ChessSquare square,
				Color color, int width, GC gc) {
			int squareSide = square.getSize().x;

			Transform tr = new Transform(gc.getDevice());
			tr.translate(squareSide / 2.0F, squareSide / 2.0F);
			gc.setTransform(tr);
			tr.rotate(-45);
			gc.setTransform(tr);

			gc.setBackground(color);
			gc.fillPolygon(getOriginDiagPolygon(squareSide, width));

			tr.dispose();
			gc.setTransform(null);
		}

		private void drawOriginHorizontalLeft(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);
			gc.fillRectangle(0, halfSquareSide - halfWidth, halfSquareSide + 1,
					width);

		}

		private void drawOriginHorizontalRight(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);
			gc.fillRectangle(halfSquareSide, halfSquareSide - halfWidth,
					halfSquareSide + 1, width);
		}

		private void drawOriginVerticalDown(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.fillRectangle(halfSquareSide - halfWidth, halfSquareSide + 1,
					width, halfSquareSide);
		}

		private void drawOriginVerticalUp(ChessSquare square, Color color,
				int width, GC gc) {
			gc.setBackground(color);
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);
			gc.fillRectangle(halfSquareSide - halfWidth, 0, width,
					halfSquareSide + 1);

		}

		private void drawUpwardTurnLeft(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(-halfSquareSide, halfSquareSide - halfWidth + i,
						squareSide, squareSide, 90, -95);
				gc.drawArc(-halfSquareSide - halfWidth + i, halfSquareSide,
						squareSide, squareSide, 90, -95);
			}
		}

		private void drawUpwardTurnRight(ChessSquare square, Color color,
				int width, GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setForeground(color);
			for (int i = 0; i < width; i++) {
				gc.drawArc(halfSquareSide, halfSquareSide - halfWidth + i,
						squareSide, squareSide, 180, -95);
				gc.drawArc(halfSquareSide + -halfWidth + i, halfSquareSide,
						squareSide, squareSide, 180, -95);
			}
		}

		private void drawVertical(ChessSquare square, Color color, int width,
				GC gc) {
			int squareSide = square.getSize().y;
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			gc.setBackground(color);
			gc.fillRectangle(halfSquareSide - halfWidth, 0, width, squareSide);
		}

		private int getArrowBaseHeight(int squareSide, int width) {
			int result = width * 3;
			if (result % 2 != 0) {
				result++;
			}

			if (result < 10) {
				result = 10;
			}
			return result;
		}

		private int getArrowRectWidth(int squareSide) {
			int result = (int) (25.0 / 100.0 * squareSide);
			if (result % 2 != 0) {
				result++;
			}
			return result;
		}

		/**
		 * Creates the destination polygon with the origin at 0,0
		 */
		private int[] getDestinationPolygon(int squareSide, int width) {
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			int arrowRectWidth = getArrowRectWidth(squareSide);
			int arrowBaseHeight = getArrowBaseHeight(squareSide, width);

			int halfArrowBaseHeight = arrowBaseHeight / 2;

			return new int[] { 0, -halfWidth, arrowRectWidth, -halfWidth,
					arrowRectWidth, -halfArrowBaseHeight, halfSquareSide, -1,
					halfSquareSide, +1, arrowRectWidth, halfArrowBaseHeight,
					arrowRectWidth, halfWidth, 0, halfWidth };
		}

		/**
		 * Creates the diag destination polygon with the origin at 0,0
		 */
		private int[] getDiagDestinationPolygon(int squareSide, int width) {
			int halfWidth = getHalfWidth(width);

			int c2 = pythag(squareSide, squareSide);
			int halfSquareSide = getHalfSquareSide(c2);

			int arrowRectWidth = getArrowRectWidth(c2);
			int arrowBaseHeight = getArrowBaseHeight(squareSide, width);

			int halfArrowBaseHeight = arrowBaseHeight / 2;

			return new int[] { 0, -halfWidth, arrowRectWidth, -halfWidth,
					arrowRectWidth, -halfArrowBaseHeight, halfSquareSide, -1,
					halfSquareSide, +1, arrowRectWidth, halfArrowBaseHeight,
					arrowRectWidth, halfWidth, 0, halfWidth };
		}

		private int getHalfSquareSide(int squareSide) {
			int result = squareSide / 2;
			if (result % 2 != 0) {
				result++;
			}
			return result;
		}

		private int getHalfWidth(int width) {
			int result = width / 2;
			if (result % 2 == 0) {
				result--;
			}
			return result;
		}

		private int[] getOriginDiagPolygon(int squareSide, int width) {
			int halfWidth = getHalfWidth(width);
			int halfSquareSide = getHalfSquareSide(squareSide);

			int c2 = pythag(halfSquareSide + halfWidth, halfSquareSide
					+ halfWidth);

			return new int[] { 0, -halfWidth, c2, -halfWidth, c2, halfWidth, 0,
					halfWidth };
		}

		private int pythag(int a, int b) {
			return (int) Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
		}
	}

	/**
	 * Ties an arrow to an arrow segment.
	 */
	protected static class ArrowSpec {
		Arrow arrow;

		ArrowSegment segment;

		public ArrowSpec(Arrow arrow, ArrowSegment segment) {
			this.arrow = arrow;
			this.segment = segment;
		}
	}

	protected class SquareArrowDecorator implements PaintListener {
		protected ChessSquare square;
		protected List<ArrowSpec> specs = new ArrayList<ArrowSpec>(10);

		public SquareArrowDecorator(ChessSquare square) {
			this.square = square;
			square.addPaintListener(this);
		}

		public void addArrowSpec(ArrowSpec spec) {
			specs.add(spec);
		}

		public void clear(boolean isForcing) {
			if (isForcing) {
				specs.clear();
			} else {
				for (int i = 0; i < specs.size(); i++) {
					if (!specs.get(i).arrow.isFadeAway) {
						specs.remove(i);
						i--;
					}
				}
			}
		}

		public void paintControl(final PaintEvent e) {
			// Don't put log statements in here it gets called quite often.
			for (ArrowSpec spec : specs) {
				if (spec.arrow.frame == -1) {
					int width = (int) (Raptor.getInstance().getPreferences()
							.getInt(PreferenceKeys.ARROW_WIDTH_PERCENTAGE) / 100.0 * square
							.getSize().x);
					if (width % 2 != 0) {
						width++;
					}
					ArrowSegment segment = board.isWhiteOnTop() ? spec.segment
							.rotate180() : spec.segment;

					segment.draw(square, spec.arrow.color, width, e.gc);
				} else if (spec.arrow.frame != 0) {
					int width = (int) (Raptor.getInstance().getPreferences()
							.getInt(PreferenceKeys.ARROW_WIDTH_PERCENTAGE) / 100.0 * square
							.getSize().x);
					if (width % 2 != 0) {
						width++;
					}

					e.gc.setAdvanced(true);
				    e.gc.setAlpha((int) (255.0/Arrow.ANIMATION_STAGES * spec.arrow.frame));

					ArrowSegment segment = board.isWhiteOnTop() ? spec.segment
							.rotate180() : spec.segment;

					segment.draw(square, spec.arrow.color, width, e.gc);

					e.gc.setAlpha(255);
				}
			}
		}

		public void remove(Arrow arrow, boolean isForced) {
			if (!arrow.isFadeAway || isForced) {
				ArrowSpec specToRemove = null;

				for (ArrowSpec spec : specs) {
					if (arrow == spec.arrow) {
						specToRemove = spec;
						
					}
				}
				if (specToRemove != null) {
					specs.remove(specToRemove);
				}
			}
		}
	}

	static final Logger LOG = Logger.getLogger(ArrowDecorator.class);

	protected ChessBoard board;

	protected SquareArrowDecorator[] decorators = new SquareArrowDecorator[64];

	public ArrowDecorator(ChessBoard board) {
		this.board = board;
		for (int i = 0; i < 64; i++) {
			decorators[i] = new SquareArrowDecorator(board.getSquare(GameConstants.SQUARES[i]));
		}
	}

	/**
	 * Draws the specified arrow. Currently arrows are not supported to or from
	 * drop squares.
	 */
	public void addArrow(final Arrow arrow) {
		if (arrow.startSquare == arrow.endSquare
				|| GameUtils.isDropSquare(arrow.startSquare)
				|| GameUtils.isDropSquare(arrow.endSquare)) {
			return;
		} else if (arrow.startSquare.index < arrow.endSquare.index) {
			addDecoratorsForArrowStartLessThanEnd(arrow);
		} else {
			addDecoratorsForArrowStartGreaterThanEnd(arrow);
		}
		redrawSquares(false);
		if (arrow.isFadeAway) {
			Raptor.getInstance().getDisplay().timerExec(
					Raptor.getInstance().getPreferences().getInt(
							PreferenceKeys.ARROW_ANIMATION_DELAY),
					new Runnable() {
						public void run() {
							arrow.frame--;
							redrawSquares(true);
							if (arrow.frame != 0) {
								Raptor
										.getInstance()
										.getDisplay()
										.timerExec(
												Raptor
														.getInstance()
														.getPreferences()
														.getInt(
																PreferenceKeys.ARROW_ANIMATION_DELAY),
												this);
							} else {
								removeArrow(arrow, true);
							}
						}
					});
		}
	}

	/**
	 * Returns true if the arrow is currently being used in the decorator.
	 * 
	 * @param arrow
	 *            The arrow
	 * @return The result.
	 */
	public boolean containsArrow(Arrow arrow) {
		boolean result = false;
		outer: for (SquareArrowDecorator decorator : decorators) {
			for (ArrowSpec spec : decorator.specs) {
				result = spec.arrow.equals(arrow);
				if (result) {
					break outer;
				}
			}
		}
		return result;
	}

	public void dispose() {
		if (decorators != null) {
			removeAllArrows();
			if (!board.getControl().isDisposed()) {
				for (int i = 0; i < decorators.length; i++) {
					decorators[i].square.removePaintListener(decorators[i]);
					decorators[i] = null;
				}
			}
			decorators = null;
			board = null;
		}
	}

	/**
	 * Removes all non fade away arrows on the chess board.
	 */
	public void removeAllArrows() {
		removeAllArrows(true);
	}

	/**
	 * Removes an arrow.
	 */
	public void removeArrow(Arrow arrow) {
		removeArrow(arrow, false);
	}

	protected void addDecoratorsForArrowStartGreaterThanEnd(Arrow arrow) {
		int fileDelta = arrow.startSquare.file
				- arrow.endSquare.file;
		if (fileDelta == 0) {
			addDecoratorsForStartGreaterThanEndEqualFiles(arrow);
		} else {
			int rankDelta = arrow.startSquare.rank
					- arrow.endSquare.rank;
			if (rankDelta == 0) {
				addDecoratorsForStartGreaterThanEndEqualRanks(arrow);
			} else if (Math.abs(rankDelta) != Math.abs(fileDelta)) {
				addDecoratorsForStartGreaterThanEndKnightMoves(arrow);
			} else {
				addDecoratorsForStartGreaterThanEndDiagMoves(arrow);
			}
		}
	}

	protected void addDecoratorsForArrowStartLessThanEnd(Arrow arrow) {
		int fileDelta = arrow.startSquare.file
				- arrow.endSquare.file;
		if (fileDelta == 0) {
			addDecoratorsForStartLessThaneEndEqualFiles(arrow);
		} else {
			int rankDelta = arrow.startSquare.rank
					- arrow.endSquare.rank;
			if (rankDelta == 0) {
				addDecoratorsForStartLessThanEndEuqlRanks(arrow);
			} else if (Math.abs(rankDelta) != Math.abs(fileDelta)) {
				addDecoratorsForStartLessThanEndKnightMoves(arrow);
			} else {
				addDecoratorsForStartLessThanEndDiagMoves(arrow);
			}
		}
	}

	protected void addDecoratorsForStartGreaterThanEndDiagMoves(Arrow arrow) {
		if (arrow.startSquare.file > arrow.endSquare.file) {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginDiagIncreasingLeft));

			byte startRank = (byte)(arrow.startSquare.rank - 1);
			byte startFile = (byte)(arrow.startSquare.file - 1);
			byte endRank = arrow.endSquare.rank;
			byte endFile = arrow.endSquare.file;

			while (startRank > endRank && startFile > endFile) {

				decorators[Square.getSquare(startRank, startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagIncreasing));

				decorators[Square.getSquare((byte)(startRank + 1), startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagSouthEastCorner));
				decorators[Square.getSquare(startRank, (byte)(startFile + 1)).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagNorthWestCorner));
				startRank--;
				startFile--;
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationDiagIncreasingLeft));

			decorators[Square.getSquare((byte)(startRank + 1), startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagSouthEastCorner));
			decorators[Square.getSquare(startRank, (byte)(startFile + 1)).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagNorthWestCorner));

		} else { // getFile(startSquare) < getFile(endSquare)

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginDiagDecreasingRight));

			byte startRank = (byte)(arrow.startSquare.rank - 1);
			byte startFile = (byte)(arrow.startSquare.file + 1);
			byte endRank = arrow.endSquare.rank;
			byte endFile = arrow.endSquare.file;
			while (startRank > endRank && startFile < endFile) {

				decorators[Square.getSquare(startRank, startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagDecreasing));

				decorators[Square.getSquare((byte)(startRank + 1), startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagSouthWestCorner));
				decorators[Square.getSquare(startRank, (byte)(startFile - 1)).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagNorthEastCorner));
				startRank--;
				startFile++;
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationDiagDecreasingRight));

			decorators[Square.getSquare((byte)(startRank + 1), startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagSouthWestCorner));
			decorators[Square.getSquare(startRank, (byte)(startFile - 1)).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagNorthEastCorner));
		}
	}

	protected void addDecoratorsForStartGreaterThanEndEqualFiles(Arrow arrow) {

		decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.OriginVerticalDown));

		byte startRank = arrow.endSquare.rank;
		byte endRank = arrow.startSquare.rank;
		byte file = arrow.startSquare.file;

		for (byte rank = (byte)(startRank + 1); rank < endRank; rank++) {
			decorators[Square.getSquare(rank, file).index]
					.addArrowSpec(new ArrowSpec(arrow, ArrowSegment.Vertical));
		}

		decorators[arrow.endSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.DestinationVerticalDown));

	}

	protected void addDecoratorsForStartGreaterThanEndEqualRanks(Arrow arrow) {
		decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.OriginHorizontalLeft));

		byte startFile = arrow.endSquare.file;
		byte endFile = arrow.startSquare.file;
		byte rank = arrow.startSquare.rank;

		for (byte file = (byte)(startFile + 1); file < endFile; file++) {
			decorators[Square.getSquare(rank, file).index]
					.addArrowSpec(new ArrowSpec(arrow, ArrowSegment.Horizontal));
		}

		decorators[arrow.endSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.DestinationHorizontalLeft));
	}

	protected void addDecoratorsForStartGreaterThanEndKnightMoves(Arrow arrow) {
		if (arrow.startSquare.file < arrow.endSquare.file) {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginVerticalDown));

			byte startRank = (byte)(arrow.startSquare.rank - 1);
			byte endRank = arrow.endSquare.rank;

			while (startRank > endRank) {
				decorators[Square.getSquare(startRank--, arrow.startSquare.file).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Vertical));
			}

			decorators[Square.getSquare(startRank, arrow.startSquare.file).index].addArrowSpec(new ArrowSpec(
					arrow, ArrowSegment.DownwardTurnRight));

			byte startFile = (byte)(arrow.startSquare.file + 1);
			while (startFile < arrow.endSquare.file) {
				decorators[Square.getSquare(startRank, startFile++).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Horizontal));
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationHorizontalRight));

		} else {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginVerticalDown));

			byte startRank = (byte)(arrow.startSquare.rank - 1);
			byte endRank = arrow.endSquare.rank;

			while (startRank > endRank) {
				decorators[Square.getSquare(startRank--, arrow.startSquare.file).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Vertical));
			}

			decorators[Square.getSquare(startRank, arrow.startSquare.file).index].addArrowSpec(new ArrowSpec(
					arrow, ArrowSegment.DownwardTurnLeft));

			byte startFile = (byte)(arrow.startSquare.file - 1);
			while (startFile > arrow.endSquare.file) {
				decorators[Square.getSquare(startRank, startFile--).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Horizontal));
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationHorizontalLeft));
		}
	}

	protected void addDecoratorsForStartLessThaneEndEqualFiles(Arrow arrow) {

		decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.OriginVerticalUp));

		byte startRank = arrow.startSquare.rank;
		byte endRank = arrow.endSquare.rank;
		byte file = arrow.startSquare.file;

		for (byte rank = (byte)(startRank + 1); rank < endRank; rank++) {
			decorators[Square.getSquare(rank, file).index]
					.addArrowSpec(new ArrowSpec(arrow, ArrowSegment.Vertical));
		}

		decorators[arrow.endSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.DestinationVerticalUp));

	}

	protected void addDecoratorsForStartLessThanEndDiagMoves(Arrow arrow) {
		if (arrow.startSquare.file > arrow.endSquare.file) {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginDiagDecreasingLeft));

			byte startRank = (byte)(arrow.startSquare.rank + 1);
			byte startFile = (byte)(arrow.startSquare.file - 1);
			byte endRank = arrow.endSquare.rank;
			byte endFile = arrow.endSquare.file;

			while (startRank < endRank && startFile > endFile) {

				decorators[Square.getSquare(startRank, startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagDecreasing));

				decorators[Square.getSquare(startRank, (byte)(startFile + 1)).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagSouthWestCorner));
				decorators[Square.getSquare((byte)(startRank - 1), startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagNorthEastCorner));
				startRank++;
				startFile--;
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationDiagDecreasingLeft));

			decorators[Square.getSquare(startRank, (byte)(startFile + 1)).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagSouthWestCorner));
			decorators[Square.getSquare((byte)(startRank - 1), startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagNorthEastCorner));

		} else { // getFile(startSquare) < getFile(endSquare)

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginDiagIncreasingRight));

			byte startRank = (byte)(arrow.startSquare.rank + 1);
			byte startFile = (byte)(arrow.startSquare.file + 1);
			byte endRank = arrow.endSquare.rank;
			byte endFile = arrow.endSquare.file;

			while (startRank < endRank && startFile < endFile) {

				decorators[Square.getSquare(startRank, startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagIncreasing));

				decorators[Square.getSquare(startRank, (byte)(startFile - 1)).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagSouthEastCorner));
				decorators[Square.getSquare((byte)(startRank - 1), startFile).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.DiagNorthWestCorner));
				startRank++;
				startFile++;
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationDiagIncreasingRight));

			decorators[Square.getSquare(startRank, (byte)(startFile - 1)).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagSouthEastCorner));
			decorators[Square.getSquare((byte)(startRank - 1), startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DiagNorthWestCorner));
		}
	}

	protected void addDecoratorsForStartLessThanEndEuqlRanks(Arrow arrow) {
		decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.OriginHorizontalRight));

		byte startFile = arrow.startSquare.file;
		byte endFile = arrow.endSquare.file;
		byte rank = arrow.startSquare.rank;

		for (byte file = (byte)(startFile + 1); file < endFile; file++) {
			decorators[Square.getSquare(rank, file).index]
					.addArrowSpec(new ArrowSpec(arrow, ArrowSegment.Horizontal));
		}

		decorators[arrow.endSquare.index].addArrowSpec(new ArrowSpec(arrow,
				ArrowSegment.DestinationHorizontalRight));
	}

	protected void addDecoratorsForStartLessThanEndKnightMoves(Arrow arrow) {
		if (arrow.startSquare.file < arrow.endSquare.file) {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginVerticalUp));

			byte startRank = (byte)(arrow.startSquare.rank + 1);
			byte endRank = arrow.endSquare.rank;

			while (startRank < endRank) {
				decorators[Square.getSquare(startRank++, arrow.startSquare.file).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Vertical));
			}

			decorators[Square.getSquare(startRank, arrow.startSquare.file).index].addArrowSpec(new ArrowSpec(
					arrow, ArrowSegment.UpwardTurnRight));

			byte startFile = (byte)(arrow.startSquare.file + 1);
			while (startFile < arrow.endSquare.file) {
				decorators[Square.getSquare(startRank, startFile++).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Horizontal));
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationHorizontalRight));

		} else {

			decorators[arrow.startSquare.index].addArrowSpec(new ArrowSpec(arrow,
					ArrowSegment.OriginVerticalUp));

			byte startRank = (byte)(arrow.startSquare.rank + 1);
			byte endRank = arrow.endSquare.rank;

			while (startRank < endRank) {
				decorators[Square.getSquare(startRank++, arrow.startSquare.file).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Vertical));
			}

			decorators[Square.getSquare(startRank, arrow.startSquare.file).index].addArrowSpec(new ArrowSpec(
					arrow, ArrowSegment.UpwardTurnLeft));

			byte startFile = (byte)(arrow.startSquare.file - 1);
			while (startFile > arrow.endSquare.file) {
				decorators[Square.getSquare(startRank, startFile--).index]
						.addArrowSpec(new ArrowSpec(arrow,
								ArrowSegment.Horizontal));
			}

			decorators[Square.getSquare(startRank, startFile).index]
					.addArrowSpec(new ArrowSpec(arrow,
							ArrowSegment.DestinationHorizontalLeft));
		}
	}

	/**
	 * Redraws all squares that have arrow segments.
	 * @param forceUpdate If true, the square is redrawn, else it is just set to dirty.
	 */
	protected void redrawSquares(boolean forceUpdate) {
		if (decorators == null) {
			return;
		}
		// Use for loops here with int. If you dont you can get concurrent
		// modification errors.
		for (int i = 0; i < decorators.length; i++) {
			if (forceUpdate) {
				decorators[i].square.redraw();
			}
			else {
			    decorators[i].square.setDirty(true);
			}
			if (!decorators[i].specs.isEmpty()) {
			}
		}
	}

	/**
	 * Removes all non fade away arrows on the chess board.
	 */
	protected void removeAllArrows(boolean isForcing) {
		if (decorators == null) {
			return;
		}
		
		for (SquareArrowDecorator decorator : decorators) {
			decorator.clear(isForcing);
		}
	}

	/**
	 * Removes an arrow.
	 */
	protected void removeArrow(Arrow arrow, boolean isForced) {
		if (decorators == null) {
			return;
		}
		
		for (SquareArrowDecorator decorator : decorators) {
			decorator.remove(arrow, isForced);
			decorator.square.setDirty(true);
		}
		board.redrawPiecesAndArtifacts();
	}
}
