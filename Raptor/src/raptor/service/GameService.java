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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import raptor.chess.Game;

/**
 * A class which manages active games that belong to a connector.
 */
public class GameService {

	public static class GameInfo {
		public static enum GameInfoCategory {
			blitz, lightning, untimed, examined, standard, wild, atomic, crazyhouse, bughouse, losers, suicide, nonstandard
		}

        protected String id;
		protected String whiteName;
		protected String blackName;
		protected String whiteElo;
		protected String blackElo;
		protected boolean isRated;
		protected boolean isWhitesMove;
		protected boolean isBeingExamined;
		protected boolean isPrivate;
		protected GameInfoCategory category;
		protected short moveNumber;
		protected int time;
		protected int inc;

		public String getBlackElo() {
			return blackElo;
		}

		public String getBlackName() {
			return blackName;
		}

		public GameInfoCategory getCategory() {
			return category;
		}

		public String getId() {
			return id;
		}

		public int getInc() {
			return inc;
		}

		public short getMoveNumber() {
			return moveNumber;
		}

		public int getTime() {
			return time;
		}

		public String getWhiteElo() {
			return whiteElo;
		}

		public String getWhiteName() {
			return whiteName;
		}

		public boolean isBeingExamined() {
			return isBeingExamined;
		}

		public boolean isPrivate() {
			return isPrivate;
		}

		public boolean isRated() {
			return isRated;
		}

		public boolean isWhitesMove() {
			return isWhitesMove;
		}

		public void setBeingExamined(boolean isBeingExamined) {
			this.isBeingExamined = isBeingExamined;
		}

		public void setBlackElo(String blackElo) {
			this.blackElo = blackElo;
		}

		public void setBlackName(String blackName) {
			this.blackName = blackName;
		}

		public void setCategory(GameInfoCategory category) {
			this.category = category;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setInc(int inc) {
			this.inc = inc;
		}

		public void setMoveNumber(short moveNumber) {
			this.moveNumber = moveNumber;
		}

		public void setPrivate(boolean isPrivate) {
			this.isPrivate = isPrivate;
		}

		public void setRated(boolean isRated) {
			this.isRated = isRated;
		}

		public void setTime(int time) {
			this.time = time;
		}

		public void setWhiteElo(String whiteElo) {
			this.whiteElo = whiteElo;
		}

		public void setWhiteName(String whiteName) {
			this.whiteName = whiteName;
		}

		public void setWhitesMove(boolean isWhitesMove) {
			this.isWhitesMove = isWhitesMove;
		}
	}

	/**
	 * An adapter class which provides default implementations for the
	 * GameServiceListener interface.
	 */
	public static class GameServiceAdapter implements GameServiceListener {
		public void droppablePiecesChanged(Game game) {
		}

		public void examinedGameBecameSetup(Game game) {
		}

		public void gameCreated(Game game) {
		}

		public void gameInactive(Game game) {
		}

		public void gameInfoChanged() {

		}

		public void gameMovesAdded(Game game) {
		}

		public void gameStateChanged(Game game, boolean isNewMove) {
		}

		public void illegalMove(Game game, String move) {
		}

		public void observedGameBecameExamined(Game game) {
		}

		public void offerIssued(Offer offer) {
		}

		public void offerReceived(Offer offer) {
		}

		public void offerRemoved(Offer offer) {
		}

		public void setupGameBecameExamined(Game game) {
		}
	}

	public static interface GameServiceListener {

		/**
		 * Invoked when the drop pieces you are holding change in droppable
		 * chess games.
		 * 
		 * @param game
		 */
		public void droppablePiecesChanged(Game game);

		/**
		 * Invoked when an examined game becomes a setup game.
		 */
		public void examinedGameBecameSetup(Game game);

		/**
		 * Invoked when a game is created.
		 */
		public void gameCreated(Game game);

		/**
		 * Invoked when a game becomes inactive, i.e. no longer active on the
		 * connector. After this method is invoked on all listeners the game is
		 * removed from the GameService.
		 */
		public void gameInactive(Game game);

		/**
		 * Invoked when the gameInfo information changed;
		 */
		public void gameInfoChanged();

		/**
		 * Invoked when the starting moves for this game are added to a game.
		 * Usually occurs when a game is being observed or examined.
		 */
		public void gameMovesAdded(Game game);

		/**
		 * Invoked when the state of a game changes. This can be from a move
		 * being made or from something external in the Connector.
		 */
		public void gameStateChanged(Game game, boolean isNewMove);

		/**
		 * Invoked when a user makes a move on a connector that is invalid.
		 */
		public void illegalMove(Game game, String move);

		/**
		 * Invoked when a user becomes an examiner of an observed game.
		 */
		public void observedGameBecameExamined(Game game);

		/**
		 * Invoked when an offer is issued.
		 */
		public void offerIssued(Offer offer);

		/**
		 * Invoked when an offer is received.
		 */
		public void offerReceived(Offer offer);

		/**
		 * Invoked when an offer is removed.
		 */
		public void offerRemoved(Offer offer);

		/**
		 * Invoked when a game which was previously in setup mode has entered
		 * examine mode.
		 */
		public void setupGameBecameExamined(Game game);
	}

	public static class Offer {
		public static enum OfferType {
			match, partner, draw, abort, adjourn, takeback
		}

        protected boolean isReceiving;
		protected String source;
		protected String description;
		protected String id;
		protected String command;
		protected OfferType type;
		protected boolean isDeclinable;
		protected String declineCommand;
		protected String declineAllCommand;
		protected String declineDescription;

		public String getCommand() {
			return command;
		}

		public String getDeclineAllCommand() {
			return declineAllCommand;
		}

		public String getDeclineCommand() {
			return declineCommand;
		}

		public String getDeclineDescription() {
			return declineDescription;
		}

		public String getDescription() {
			return description;
		}

		public String getId() {
			return id;
		}

		public String getSource() {
			return source;
		}

		public OfferType getType() {
			return type;
		}

		public boolean isDeclinable() {
			return isDeclinable;
		}

		public boolean isReceiving() {
			return isReceiving;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public void setDeclinable(boolean isDeclinable) {
			this.isDeclinable = isDeclinable;
		}

		public void setDeclineAllCommand(String declineAllCommand) {
			this.declineAllCommand = declineAllCommand;
		}

		public void setDeclineCommand(String declineCommand) {
			this.declineCommand = declineCommand;
		}

		public void setDeclineDescription(String declineDescription) {
			this.declineDescription = declineDescription;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setReceiving(boolean isReceiving) {
			this.isReceiving = isReceiving;
		}

		public void setSource(String source) {
			this.source = source;
		}

		public void setType(OfferType type) {
			this.type = type;
		}
	}

	protected HashMap<String, Game> gameMap = new HashMap<String, Game>();
	protected List<Offer> offers = new ArrayList<Offer>(10);
	protected List<GameInfo> gameInfo = new ArrayList<GameInfo>(400);

	protected List<GameServiceListener> listeners = Collections
			.synchronizedList(new ArrayList<GameServiceListener>(20));

	public GameInfo[] getGameInfos() {
		return gameInfo.toArray(new GameInfo[0]);
	}

	public void addGame(Game game) {
		gameMap.put(game.getId(), game);
	}

	public void addGameServiceListener(GameServiceListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		gameMap.clear();
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireDroppablePiecesChanged(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.droppablePiecesChanged(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireExaminedGameBecameSetup(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.examinedGameBecameSetup(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireGameCreated(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameCreated(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 * 
	 * Invoked when the game is no longer active. After notifying all of the
	 * listeners the game will be removed from this GameService since it is no
	 * longer updatable by the connector.
	 */
	public void fireGameInactive(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameInactive(game);
				}
			}
			removeGame(game);
		}
	}

	public void fireGameInfoChanged(GameInfo[] gameInfos) {
		synchronized (gameInfo) {
			gameInfo.clear();
            Collections.addAll(gameInfo, gameInfos);
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameInfoChanged();
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireGameMovesAdded(String gameId) {		
		Game game = getGame(gameId);		
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameMovesAdded(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireGameStateChanged(String gameId, boolean isNewMove) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.gameStateChanged(game, isNewMove);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireIllegalMove(String gameId, String move) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.illegalMove(game, move);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireObservedGameBecameExamined(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.observedGameBecameExamined(game);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 * 
	 * @param offer
	 *            The offer issued.
	 */
	public void fireOfferIssued(Offer offer) {
		offers.add(offer);
		synchronized (listeners) {
			for (GameServiceListener listener : listeners) {
				listener.offerIssued(offer);
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 * 
	 * @param offer
	 *            The offer received.
	 */
	public void fireOfferReceived(Offer offer) {
		offers.add(offer);
		synchronized (listeners) {
			for (GameServiceListener listener : listeners) {
				listener.offerReceived(offer);
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 * 
	 * @param id
	 *            The id of the challange to remove.
	 */
	public void fireOfferRemoved(String id) {
		Offer foundChallenge = null;
		for (Offer challenge : offers) {
			if (challenge.getId().equals(id)) {
				foundChallenge = challenge;
				break;
			}
		}
		if (foundChallenge != null) {
			offers.remove(foundChallenge);
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.offerRemoved(foundChallenge);
				}
			}
		}
	}

	/**
	 * This method should only be invoked from a connector.
	 */
	public void fireSetupGameBecameExamined(String gameId) {
		Game game = getGame(gameId);
		if (game != null) {
			synchronized (listeners) {
				for (GameServiceListener listener : listeners) {
					listener.setupGameBecameExamined(game);
				}
			}
		}
	}

	/**
	 * Returns an array of all active games in the game service.
	 */
	public Game[] getAllActiveGames() {
		List<Game> result = new ArrayList<Game>(5);
		for (Game game : gameMap.values()) {
			if (game.isInState(Game.ACTIVE_STATE)) {
				result.add(game);
			}
		}
		return result.toArray(new Game[0]);
	}

	/**
	 * Returns the game with the specified id.
	 */
	public Game getGame(String gameId) {
		return gameMap.get(gameId);
	}

	/**
	 * Returns the number of games this game service is managing.
	 */
	public int getGameCount() {
		return gameMap.values().size();
	}

	public Offer[] getOffers() {
		return offers.toArray(new Offer[0]);
	}

	public boolean isManaging(String gameId) {
		return gameMap.containsKey(gameId);
	}

	/**
	 * Removes a game from the game service.
	 */
	public void removeGame(Game game) {
		gameMap.remove(game.getId());
	}

	/**
	 * Removes a game service listener.
	 */
	public void removeGameServiceListener(GameServiceListener listener) {
		listeners.remove(listener);
	}
}
