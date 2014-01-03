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
package raptor.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Game;
import raptor.chess.GameFactory;
import raptor.chess.Variant;

/**
 * Converts resources/ECO.txt into resources/ECOFen.txt
 */
public class ConvertECOTxt {
	public static void main(String args[]) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader("projectFiles/ECO.txt"))) {
			try (FileWriter writer = new FileWriter("resources/ECOFen.txt")) {
				String currentLine;
				int lineNumber = 0;
				while ((currentLine = reader.readLine()) != null) {
					lineNumber++;
					System.err.println("Parsing line number: " + lineNumber);
					if (StringUtils.isBlank(currentLine) || currentLine.startsWith("//"))
						continue;
					String[] tokens = currentLine.split("\\|", 2);
					String moves = tokens[0];
					Game game = GameFactory.createStartingPosition(Variant.classic);
					for (String move : moves.split("\\s+")) {
						try {
							game.makeSanMove(move);
						} catch (IllegalArgumentException iae) {
							move = StringUtils.replaceChars(move, 'B', 'b');
							try {
								game.makeSanMove(move);
							} catch (IllegalArgumentException iae2) {
								move = StringUtils.replaceChars(move, 'b', 'B');
								game.makeSanMove(move);
							}
						}
					}
					String fenPosition = game.toFenPosition() + " "
							+ (game.isWhitesMove() ? 'w' : 'b');
					writer.write(fenPosition + "|");
					if (tokens.length > 1) {
						writer.write(tokens[1] + "|");
					}
					writer.write("\n");
				}
				writer.flush();
			}
		}
	}
}
