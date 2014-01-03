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
package raptor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import raptor.chess.EcoInfo;
import raptor.chess.Game;
import raptor.chess.GameConstants;
import raptor.chess.Variant;
import raptor.chess.util.GameUtils;
import raptor.util.Logger;

/**
 * A singleton service which can be used to lookup the opening description and
 * ECO code of the current position in a game.  Caches the 400KB code table.
 * 
 * Currently this service only supports Classic but hopefully others will
 * contribute files to match other variants (bug,zh,suicide,losers,etc).
 */
public class EcoService {

	private static final Logger LOG = Logger.getLogger(EcoService.class);

	private static EcoService singletonInstance;
	
	public static boolean serviceCreated = false;

	public static EcoService getInstance() {
		if (singletonInstance != null)
			return singletonInstance;

		singletonInstance = new EcoService();
		return singletonInstance;
	}

	private Map<Variant, Map<String, EcoInfo>> typeToFenToEco = new HashMap<Variant, Map<String, EcoInfo>>();

	private EcoService() {
		initClassic();
		serviceCreated = true;
	}

	/**
	 * Disposes the EcoService.
	 */
	public void dispose() {
		typeToFenToEco.clear();
	}

	/**
	 * Returns the ECO code for the specified game, null if one could not be
	 * found.
	 */
	public EcoInfo getEcoInfo(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.
		Map<String, EcoInfo> map = typeToFenToEco.get(getAdjustedVariant(game));
		if (map == null) {
			return null;
		}
		return map.get(getFenKey(game, true));
	}

	/**
	 * Returns the ECO code for the specified game, null if one could not be
	 * found.
	 */
	public String getEco(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.
		Map<String, EcoInfo> map = typeToFenToEco.get(getAdjustedVariant(game));
		if (map == null) {
			return null;
		} else {
			EcoInfo info = map.get(getFenKey(game, true));
			return info == null ? null : info.getEcoCode();
		}
	}

	/**
	 * Returns the long description of the opening for the specified game, null
	 * if one could not be found.
	 */
	public String getLongDescription(Game game) {
		// Don't add debug messages in here. It gets called so often they are
		// annoying and really slow it down.
		Map<String, EcoInfo> map = typeToFenToEco.get(getAdjustedVariant(game));
		if (map == null) {
			return null;
		} else {
			EcoInfo info = map.get(getFenKey(game, false));
			return info == null ? null : info.getOpening();
		}
	}

	protected Variant getAdjustedVariant(Game game) {
		if (Variant.isClassic(game.getVariant())) {
			return Variant.classic;
		} else {
			return game.getVariant();
		}
	}

	protected String getFenKey(Game game, boolean includeEP) {
		return game.toFenPosition()
			+ " "
			+ (game.isWhitesMove() ? 'w' : 'b')
			+ " "
			+ game.getFenCastle()
			+ " "
			+ (includeEP ? game.getEpSquare() == GameConstants.Square.EMPTY ? "-"
				: GameUtils.getSan(game.getEpSquare())
				: "-");
	}

	private void initClassic() {
		File file = new File(raptor.Raptor.RESOURCES_DIR + "scidECO.txt");
		typeToFenToEco.put(Variant.classic, parse(file));
	}

	/**
	 * Parses information from an idx file. These files contain FEN on one line
	 * followed by a string on another line. The string can be either eco or
	 * description.
	 * 
	 * @param file
	 *            File containing the ECO information.
	 * @throws IOException
	 *             If something goes wrong during reading.
	 */
	private Map<String, EcoInfo> parse(File file) {
		if (LOG.isDebugEnabled()) {
			LOG.info("parse(" + file.getAbsolutePath() + ")");
		}
		long startTime = System.currentTimeMillis();
		Map<String, EcoInfo> result = new TreeMap<String, EcoInfo>();

		Pattern pattern = Pattern.compile("(\\w+) (.*) (\\w+(?:\\/\\w+){7} [wb] (?:\\-|K?Q?k?q?) (?:\\-|[a-h][36])) \\d+ \\d+");
		Matcher matcher = pattern.matcher("");
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String currentLine;
			while ((currentLine = reader.readLine()) != null) {
				if (matcher.reset(currentLine).matches()) {
					String eco = matcher.group(1);
					String description = matcher.group(2);
					String fen = matcher.group(3);

					// Decrease memory consumption of ECOService by 50%
					//result.put(fen.toString(), new EcoInfo(fen.toString(), eco, description.toString()));
					result.put(fen, new EcoInfo(null, eco, description));
				}
				matcher.reset();
			}
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
		if (LOG.isDebugEnabled()) {
			LOG.info("parse( " + file.getAbsolutePath() + ") executed in "
					+ (System.currentTimeMillis() - startTime) + "ms");
		}
		return result;
	}
}
