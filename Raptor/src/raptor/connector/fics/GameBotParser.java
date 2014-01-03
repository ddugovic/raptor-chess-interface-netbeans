package raptor.connector.fics;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import raptor.chat.ChatEvent;
import raptor.chat.ChatType;

public class GameBotParser {
	FicsConnector connector;
	List<String[]> danglingRows = new ArrayList<String[]>(20);

	public GameBotParser(FicsConnector connector) {
		this.connector = connector;
	}

	public String IDENTIFIER = ":GameBot(TD):";

	protected GameBotParseResults createFromDangling(boolean moreGamesExist) {
		GameBotParseResults result = new GameBotParseResults();
		result.setHasNextPage(moreGamesExist);
		result.setPlayerInDb(true);
		String[][] rowsArray = new String[danglingRows.size()][];
		int index = 0;
		for (String[] row : danglingRows) {
			rowsArray[index++] = row;
		}
		result.setRows(rowsArray);
		danglingRows.clear();
		return result;
	}

	public GameBotParseResults parse(ChatEvent event) {
		if (event.getType() == ChatType.TOLD
				&& event.getSource().equals("GameBot")) {
			// eat all of the told gamebots.
			GameBotParseResults results = new GameBotParseResults();
			results.setIncomplete(true);
			return results;
		} else if (connector.getGameBotService().hasGameBotListener()
				&& event.getType() == ChatType.QTELL
				&& event.getMessage().contains("<gb>")) {
			StringTokenizer messageTok = new StringTokenizer(event
					.getMessage(), "\n");
			while (messageTok.hasMoreTokens()) {
				String line = messageTok.nextToken();
				if (line.startsWith(":The player ")
						&& line.endsWith("is not in my database.")) {
					GameBotParseResults results = new GameBotParseResults();
					results.setPlayerInDb(false);
					StringTokenizer wordTok = new StringTokenizer(line, ": ");
					wordTok.nextToken();
					wordTok.nextToken();
					results.setPlayerName(wordTok.nextToken());
					return results;
				}
				StringTokenizer rowTok = new StringTokenizer(line, " :");
				String gbIdentifier = rowTok.nextToken();
				if (gbIdentifier.equals("<gb>")) {
					String gameId = rowTok.nextToken();
					if (gameId.equals("QueryResult")) {
                    } else if (gameId.equals("MoreGamesExist")) {
						return createFromDangling(true);
					} else if (gameId.equals("NoMoreGames")) {
						return createFromDangling(false);
					} else {
						try {
							Long.parseLong(gameId);
							List<String> rowData = new ArrayList<String>(10);
							rowData.add(gameId);
							while (rowTok.hasMoreTokens()) {
								rowData.add(rowTok.nextToken());
							}
							danglingRows.add(rowData
									.toArray(new String[0]));
						} catch (NumberFormatException nfe) {
                        }
					}
				}
			}
			GameBotParseResults results = new GameBotParseResults();
			results.setIncomplete(true);
			return results;
		}
		return null;
	}
}
