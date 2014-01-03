package raptor.chess.pgn.chesspresso;

import java.io.IOException;
import chesspresso.game.Game;
import chesspresso.move.Move;
import chesspresso.pgn.PGNReader;
import chesspresso.pgn.PGNSyntaxError;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import raptor.chess.GameFactory;
import raptor.chess.Variant;
import raptor.chess.pgn.AbstractPgnParser;
import raptor.chess.pgn.Comment;
import raptor.chess.pgn.PgnHeader;
import raptor.chess.pgn.PgnParserListener;
import raptor.chess.pgn.TimeTakenForMove;
import raptor.swt.PgnProcessingDialog.ChesspressoPgnProgressListener;

/**
 * PGN parser what act like an interface to Chesspresso's PGNReader
 */
public class ChesspressoPgnParser extends AbstractPgnParser {
	
	private PGNReader reader;
	
	public ChesspressoPgnParser(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	public ChesspressoPgnParser(InputStream fileReader) {
		reader = new PGNReader(fileReader, null);
	}

	@Override
	public int getLineNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void parse() {		
		Game chessprGame = null;
		try {
			chessprGame = reader.parseGame();
		} catch (PGNSyntaxError|IOException e) {
		}		
		do {
			for (PgnParserListener listener: listeners) {
				((ChesspressoPgnListener)listener).
				gameParsed(chessprGame, 0);
				
			}
			try {
				chessprGame = reader.parseGame();
			} catch (PGNSyntaxError e) {
				for (PgnParserListener listener: listeners) {
					((ChesspressoPgnListener)listener).error(e);
				}
			} catch (RuntimeException e) {
				for (PgnParserListener listener: listeners) {					
					PGNSyntaxError er = new PGNSyntaxError(0,e.getMessage(),"",0,"");
					((ChesspressoPgnListener)listener).error(er);													
				}
			} catch (IOException e) {
			}
		} while(chessprGame != null);
	}

	/**
	 * Convert game object from Chesspresso's representation to Raptor's
	 * @param selectedGame Chesspresso game object to convert
	 * @return Equivalent Raptor game object
	 */
	public static raptor.chess.Game convertToRaptorGame(Game selectedGame) {
		raptor.chess.Game raptorGame = GameFactory.createStartingPosition(Variant.classic);
		raptorGame.setHeader(PgnHeader.White, selectedGame.getWhite());
		raptorGame.setHeader(PgnHeader.Black, selectedGame.getBlack());
		raptorGame.setHeader(PgnHeader.WhiteElo, selectedGame.getWhiteEloStr());
		raptorGame.setHeader(PgnHeader.BlackElo, selectedGame.getBlackEloStr());
		raptorGame.setHeader(PgnHeader.Date, selectedGame.getDate());
		raptorGame.setHeader(PgnHeader.ECO, selectedGame.getECO());
		raptorGame.setHeader(PgnHeader.WhiteClock, selectedGame.getTag("WhiteClock"));
		raptorGame.setHeader(PgnHeader.BlackClock, selectedGame.getTag("BlackClock"));
		raptorGame.setHeader(PgnHeader.TimeControl, selectedGame.getTag("TimeControl"));
		raptorGame.setHeader(PgnHeader.WhiteLagMillis, selectedGame.getTag("WhiteLagMillis"));
		raptorGame.setHeader(PgnHeader.BlackLagMillis, selectedGame.getTag("BlackLagMillis"));
		raptorGame.setHeader(PgnHeader.WhiteOnTop, selectedGame.getTag("WhiteOnTop"));
		raptorGame.setHeader(PgnHeader.Event, selectedGame.getTag("Event"));
		raptorGame.setHeader(PgnHeader.BlackRemainingMillis, selectedGame.getTag("BlackRemainingMillis"));
		raptorGame.setHeader(PgnHeader.WhiteRemainingMillis, selectedGame.getTag("WhiteRemainingMillis"));
		
		selectedGame.gotoStart();
		Move[] mvs = selectedGame.getMainLine();
		selectedGame.gotoStart();
		for (Move move: mvs) {		
			selectedGame.goForward();
			String comment = selectedGame.getComment();
			raptorGame.makeSanMove(move.getSAN());		
			
			// don't count time for the first moves
			if (selectedGame.getCurrentMoveNumber() == 1)
				continue;
			
			// time taken for me is without '%' char because these chars are emitted
			if (comment != null && comment.startsWith("[emt"))
				raptorGame.getLastMove().addAnnotation(new TimeTakenForMove(comment));
			else if (comment != null)
				raptorGame.getLastMove().addAnnotation(new Comment(comment));
		}
		return raptorGame;
	}

	

}
