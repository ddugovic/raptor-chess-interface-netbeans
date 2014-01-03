package raptor.connector.ics.chat;

import java.util.regex.Pattern;
import raptor.chat.ChatType;

public class VariablesEventParser extends PatternEventParser {
	public static final Pattern PATTERN = Pattern.compile("Variable settings of (\\w+).*+");

	public VariablesEventParser() {
		super(ChatType.VARIABLES, PATTERN);
	}
}
