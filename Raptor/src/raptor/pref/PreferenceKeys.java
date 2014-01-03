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
package raptor.pref;

/**
 * Enumerates the preference keys. In order to update entries in the font
 * and color registries, all font properties must end in font and all color
 * properties must end in color.
 * TODO: Unless trying to support Java 1.4, use an Enumerable or enum type.
 */
public interface PreferenceKeys {
	// The following are not preference names but suffixes of preference names.
	public static final String MAIN_TAB_QUADRANT = "main-quadrantv2.0";
	public static final String PARTNER_TELL_TAB_QUADRANT = "partner-quadrantv2.0";
	public static final String PERSON_TAB_QUADRANT = "person-quadrantv2.0";
	public static final String REGEX_TAB_QUADRANT = "regex-quadrantv2.0";
	public static final String SEEK_TABLE_QUADRANT = "seek-table-quadrantv2.0";
	public static final String GAMES_TAB_QUADRANT = "games-tab-quadrantv2.0";
	public static final String GAME_BOT_QUADRANT = "game-bot-quadrantv2.0";
	public static final String BUG_WHO_QUADRANT = "bug-who-quadrantv2.0";
	public static final String BUG_BUTTONS_QUADRANT = "bug-buttons-quadrantv2.0";
	public static final String CHANNEL_TAB_QUADRANT = "channel-quadrantv2.0";
	public static final String GAME_CHAT_TAB_QUADRANT = "game-chat-tab-quadrantv2.0";
	public static final String CHANNEL_REGEX_TAB_INFO = "channel-reg-ex-tab-infov2.0";
	public static final String GAME_COMMANDS = "games-commands";
	public static final String PERSON_COMMANDS = "person-commands";
	public static final String PERSON_QUICK_COMMANDS = "person-quick-commands";
	public static final String CHANNEL_COMMANDS = "channel-commands";
	public static final String KEEP_ALIVE_COMMAND = "keep-alive-command";
	public static final String LEFT_MOUSE_BUTTON_ACTION = "left-mouse-button-action";
	public static final String MIDDLE_MOUSE_BUTTON_ACTION = "middle-mouse-button-action";
	public static final String RIGHT_MOUSE_BUTTON_ACTION = "right-mouse-button-action";
	public static final String MISC1_MOUSE_BUTTON_ACTION = "misc1-mouse-button-action";
	public static final String MISC2_MOUSE_BUTTON_ACTION = "misc2-mouse-button-action";
	public static final String LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION = "left-double-click-mouse-button-action";
	public static final String REGULAR_EXPRESSIONS_TO_BLOCK = "regular-expressions-to-block";
	public static final String REMOVE_BLANK_LINES = "remove-blank-lines";

	// The following are not preferences but prefixes of preference names.
	public static final String PLAYING_CONTROLLER = "board-playing-";
	public static final String OBSERVING_CONTROLLER = "board-observing-";
	public static final String INACTIVE_CONTROLLER = "board-inactive-";

	// Starting from here and on down the constants are only preference names.
	public static final String APP_NAME = "app-name";
	public static final String APP_LOCALE = "app-locale";
	public static final String APP_IS_SHOWING_CHESS_PIECE_UNICODE_CHARS = "board-is-showing-piece-unicode-chars";
	public static final String APP_USER_TAGS = "app-user-tags";
	public static final String APP_ICON_SIZE = "app-icon-size";
	public static final String APP_TOOLBAR_PIECE_SIZE = "app-toolbar-piece-size";
	public static final String APP_OPEN_LINKS_IN_EXTERNAL_BROWSER = "app-open-links-in-external-browser";
	public static final String APP_PING_COLOR = "app-lag-color";
	public static final String APP_PING_FONT = "app-lag-font";
	public static final String APP_SASH_WIDTH = "app-sash-width";
	public static final String APP_SHOW_STATUS_BAR = "app-show-status-bar";
	public static final String APP_SOUND_ENABLED = "sound-enabled";
	public static final String APP_STATUS_BAR_COLOR = "app-starus-bar-color";
	public static final String APP_STATUS_BAR_FONT = "app-status-bar-font";
	public static final String APP_HOME_URL = "app-home-url";
	public static final String APP_LAYOUT = "app-layout";
	public static final String APP_BROWSER_QUADRANT = "app-browser-quadrantv2.0";
	public static final String APP_WINDOW_ITEM_POLL_INTERVAL = "app-window-item-poll-interval";
	public static final String APP_CHESS_BOARD_QUADRANTS = "app-chess-board-quadrants";
	public static final String APP_PGN_RESULTS_QUADRANT = "app-pgn-results-quadrant";
	public static final String APP_LINUX_UNIX_BROWSER_NAME = "app-linux-unix-browser-name";
	public static final String APP_IS_LAUNCHING_HOME_PAGE = "app-is-launching-home-page";
	public static final String APP_IS_LAUNCHING_LOGIN_DIALOG = "app-is-launching-login-dialog";
	public static final String APP_QUAD9_QUAD12345678_SASH_WEIGHTS = "app-quad9-quad2345678-sash-weights";
	public static final String APP_QUAD1_QUAD2345678_SASH_WEIGHTS = "app-quad1-quad2345678-sash-weights";
	public static final String APP_QUAD2_QUAD3_QUAD4_QUAD5_SASH_WEIGHTS = "app-quad2-quad3-quad4-quad5-sash-weights";
	public static final String APP_QUAD2345_QUAD678_SASH_WEIGHTS = "app-quad2345-quad678-sash-weights";
	public static final String APP_QUAD6_QUAD7_SASH_WEIGHTS = "app-quad6-quad7-sash-weights";
	public static final String APP_QUAD67_QUAD8_SASH_WEIGHTS = "app-quad67-quad8-sash-weights";
	public static final String APP_WINDOW_BOUNDS = "app-window-bounds";
	public static final String APP_ZOOM_FACTOR = "app-zoom-factor";
	public static final String APP_PGN_FILE = "app-pgn-file";

	public static final String APP_IS_LOGGING_CONSOLE = "app-is-logging-console";
	public static final String APP_IS_LOGGING_PERSON_TELLS = "app-is-logging-person-tells";
	public static final String APP_IS_LOGGING_CHANNEL_TELLS = "app-is-logging-channel-tells";

	public static final String ACTION_SEPARATOR_SEQUENCE = "action-separator-sequence";

	public static final String BUG_ARENA_PARTNERS_INDEX = "bughouse-arena-partners-index";
	public static final String BUG_ARENA_MAX_PARTNERS_INDEX = "bughouse-arena-max-partners-index";
	public static final String BUG_ARENA_TEAMS_INDEX = "bughouse-arena-teams-index";
	public static final String BUG_ARENA_HI_LOW_INDEX = "bughouse-arena-hi-low-index";
	public static final String BUG_ARENA_TEAMS_IS_RATED = "bughosue-arena-teams-is-rated";
	public static final String BUG_ARENA_SELECTED_TAB = "bughosue-arena-selectedTab";

	public static final String GAMES_TABLE_SELECTED_TAB = "games-table-selected-tab-index";
	public static final String GAMES_TABLE_RATINGS_INDEX = "games-table-ratings-index";
	public static final String GAMES_TABLE_MAX_RATINGS_INDEX = "games-table-max-ratings-index";
	public static final String GAMES_TABLE_RATED_INDEX = "games-table-rated-index";
	public static final String GAMES_TABLE_SHOW_BUGHOUSE = "games-table-show-bughouse";
	public static final String GAMES_TABLE_SHOW_LIGHTNING = "games-table-show-lightning";
	public static final String GAMES_TABLE_SHOW_BLITZ = "games-table-show-blitz";
	public static final String GAMES_TABLE_SHOW_STANDARD = "games-table-show-standard";
	public static final String GAMES_TABLE_SHOW_CRAZYHOUSE = "games-table-show-crazyhouse";
	public static final String GAMES_TABLE_SHOW_EXAMINED = "games-table-show-examined";
	public static final String GAMES_TABLE_SHOW_WILD = "games-table-show-wild";
	public static final String GAMES_TABLE_SHOW_ATOMIC = "games-table-show-atomic";
	public static final String GAMES_TABLE_SHOW_SUICIDE = "games-table-show-suicide";
	public static final String GAMES_TABLE_SHOW_LOSERS = "games-table-show-losers";
	public static final String GAMES_TABLE_SHOW_UNTIMED = "games-table-show-untimed";
	public static final String GAMES_TABLE_SHOW_NONSTANDARD = "games-table-show-nonstandard";
	public static final String GAMES_TABLE_SHOW_PRIVATE = "games-table-show-private";

	public static final String SEEK_OUTPUT_TYPE = "seek-output-type";
	public static final String SEEK_TABLE_RATINGS_INDEX = "seek-table-ratings-index";
	public static final String SEEK_TABLE_SELECTED_TAB = "seek-table-selected-tab-index";
	public static final String SEEK_TABLE_MAX_RATINGS_INDEX = "seek-table-max-ratings-index";
	public static final String SEEK_TABLE_RATED_INDEX = "seek-table-rated-index";
	public static final String SEEK_TABLE_SHOW_COMPUTERS = "seek-table-show-computers";
	public static final String SEEK_TABLE_SHOW_LIGHTNING = "seek-table-show-lightning";
	public static final String SEEK_TABLE_SHOW_BLITZ = "seek-table-show-blitz";
	public static final String SEEK_TABLE_SHOW_STANDARD = "seek-table-show-standard";
	public static final String SEEK_TABLE_SHOW_CRAZYHOUSE = "seek-table-show-crazyhouse";
	public static final String SEEK_TABLE_SHOW_FR = "seek-table-show-fr";
	public static final String SEEK_TABLE_SHOW_WILD = "seek-table-show-wild";
	public static final String SEEK_TABLE_SHOW_ATOMIC = "seek-table-show-atomic";
	public static final String SEEK_TABLE_SHOW_SUICIDE = "seek-table-show-suicide";
	public static final String SEEK_TABLE_SHOW_LOSERS = "seek-table-show-losers";
	public static final String SEEK_TABLE_SHOW_UNTIMED = "seek-table-show-untimed";

	public static final String SEEK_GRAPH_COMPUTER_COLOR = "seek-graph-computer-color";
	public static final String SEEK_GRAPH_MANY_COLOR = "seek-graph-many-color";
	public static final String SEEK_GRAPH_RATED_COLOR = "seek-graph-rated-color";
	public static final String SEEK_GRAPH_UNRATED_COLOR = "seek-graph-unrated-color";

	public static final String BUGHOUSE_PLAYING_OPEN_PARTNER_BOARD = "bughouse-playing-open-partner-board";
	public static final String BUGHOUSE_OBSERVING_OPEN_PARTNER_BOARD = "bughouse-observing-open-partner-board";
	public static final String BUGHOUSE_SPEAK_COUNTDOWN_ON_PARTNER_BOARD = "bughouse-speak-countdown-on-partner-board";
	public static final String BUGHOUSE_SPEAK_PARTNER_TELLS = "bughouse-speak-partner-tells";
	public static final String BUGHOUSE_IS_PLAYING_PARTNERSHIP_OFFERED_SOUND = "bughouse-play-partnership-offered-sound";

	public static final String BUG_BUTTONS_FONT = "bugbuttons-font";

	public static final String BOARD_SHOW_PLAYING_GAME_STATS_ON_GAME_END = "board-show-playing-game-stats-on-game-end";
	public static final String BOARD_ALLOW_MOUSE_WHEEL_NAVIGATION_WHEEL_PLAYING = "board-allow-mouse-wheel-navigation-when-playing";
	public static final String BOARD_PLAY_CHALLENGE_SOUND = "board-play-challenge-sound";
	public static final String BOARD_PLAY_ABORT_REQUEST_SOUND = "board-play-abort-request-sound";
	public static final String BOARD_PLAY_DRAW_OFFER_SOUND = "board-play-draw-offer-sound";
	public static final String BOARD_USER_MOVE_INPUT_MODE = "board-user-input-mode";
	public static final String BOARD_SHOW_BUGHOUSE_SIDE_UP_TIME = "board-show-bughouse-side-up-time";
	public static final String BOARD_PIECE_JAIL_LABEL_PERCENTAGE = "board-piece-jail-label-percentage";
	public static final String BOARD_ACTIVE_CLOCK_COLOR = "board-active-clock-color";
	public static final String BOARD_BACKGROUND_COLOR = "board-background-color";
	public static final String BOARD_COOLBAR_MODE = "board-coolbar-mode";
	public static final String BOARD_COOLBAR_ON_TOP = "board-coolbar-on-top";
	public static final String BOARD_IS_USING_SOLID_BACKGROUND_COLORS = "board-is-using-solid-background-colors";
	public static final String BOARD_LIGHT_SQUARE_SOLID_BACKGROUND_COLOR = "board-light-square-solid-background-color";
	public static final String BOARD_DARK_SQUARE_SOLID_BACKGROUND_COLOR = "board-dark-square-solid-background-color";
	public static final String BOARD_CHESS_SET_NAME = "board-chess-set-name";
	public static final String BOARD_CLOCK_FONT = "board-clock-font";
	public static final String BOARD_CLOCK_SHOW_MILLIS_WHEN_LESS_THAN = "board-clock-show-millis-when-less-than";
	public static final String BOARD_CLOCK_SHOW_SECONDS_WHEN_LESS_THAN = "board-clock-show-seconds-when-less-than";
	public static final String BOARD_COORDINATES_COLOR = "board-coordinates-color";
	public static final String BOARD_COORDINATES_FONT = "board-coordinates-font";
	public static final String BOARD_GAME_DESCRIPTION_FONT = "board-game-description-font";
	public static final String BOARD_INACTIVE_CLOCK_COLOR = "board-inactive-clock-color";
	public static final String BOARD_IS_PLAYING_10_SECOND_COUNTDOWN_SOUNDS = "board-is-playing-10-second-countdown-sounds";
	public static final String BOARD_IS_SHOW_COORDINATES = "board-show-coordinates";
	public static final String BOARD_IS_SHOWING_PIECE_JAIL = "board-is-showing-piece-jail";
	public static final String BOARD_IS_USING_CROSSHAIRS_CURSOR = "board-is-using-crosshairs-cursor";
	public static final String BOARD_LAG_FONT = "board-lag-font";
	public static final String BOARD_LAG_OVER_20_SEC_COLOR = "board-over-20-sec-lag-color";
	public static final String BOARD_LAYOUT = "board-layout";
	public static final String BOARD_OPENING_DESC_FONT = "board-opening-desc-font";
	public static final String BOARD_PIECE_JAIL_BACKGROUND_COLOR = "board-piece-jail-background-color";
	public static final String BOARD_PIECE_JAIL_FONT = "board-piece-jail-font";
	public static final String BOARD_PIECE_JAIL_LABEL_COLOR = "board-piece-jail-label-color";
	public static final String BOARD_PIECE_SIZE_ADJUSTMENT = "board-piece-size-adjustment-percentage";
	public static final String BOARD_PLAY_MOVE_SOUND_WHEN_OBSERVING = "board-play-move-sound-when-observing";
	public static final String BOARD_PLAYER_NAME_FONT = "board-player-name-font";
	public static final String BOARD_PREMOVE_ENABLED = "board-premove-enabled";
	public static final String BOARD_PREMOVES_FONT = "board-premoves-font";
	public static final String BOARD_QUEUED_PREMOVE_ENABLED = "board-queued-premove-enabled";
	public static final String BOARD_SQUARE_BACKGROUND_NAME = "board-square-background-name";
	public static final String BOARD_CONTROL_COLOR = "board-status-color";
	public static final String BOARD_STATUS_FONT = "board-status-font";
	public static final String BOARD_TAKEOVER_INACTIVE_GAMES = "board-takeover-inactive-games";
	public static final String BOARD_PIECE_SHADOW_ALPHA = "board-hiding_alpha";
	public static final String BOARD_PIECE_JAIL_SHADOW_ALPHA = "board-piece-jail-empty-alpha";
	public static final String BOARD_COORDINATES_SIZE_PERCENTAGE = "board-coordinates-size-percentage";
	public static final String BOARD_ANNOUNCE_CHECK_WHEN_OPPONENT_CHECKS_ME = "board-announce-check-when-opponent-checks-me";
	public static final String BOARD_ANNOUNCE_CHECK_WHEN_I_CHECK_OPPONENT = "board-announce-check-when-i-check-opponent";
	public static final String BOARD_SPEAK_MOVES_OPP_MAKES = "board-speak-moves-opp-makes";
	public static final String BOARD_SPEAK_MOVES_I_MAKE = "board-speak-moves-i-make";
	public static final String BOARD_SPEAK_WHEN_OBSERVING = "board-speak-moves-when-observing";
	public static final String BOARD_SPEAK_RESULTS = "board-speak-results";
	public static final String BOARD_IGNORE_OBSERVED_GAMES_IF_PLAYING = "board-ignore-observed-games-if-playing";
	public static final String BOARD_LAST_OPEN_PGN = "board-last-open-pgn";
	public static final String BOARD_MOVE_LIST_CLASS = "board-move-list-class";
	public static final String BOARD_SQUARE_BACKGROUND_IMAGE_EFFECT = "board-square-background-image-effect";
	public static final String BOARD_TRAVERSE_WITH_MOUSE_WHEEL = "board-traverse-with-mouse-wheel";

	public static final String ARROW_OBS_OPP_COLOR = "arrow-opponent-color";
	public static final String ARROW_PREMOVE_COLOR = "arrow-premove-color";
	public static final String ARROW_MY_COLOR = "arrow-my-color";
	public static final String ARROW_OBS_COLOR = "arrow-obs-color";
	public static final String ARROW_SHOW_ON_OBS_AND_OPP_MOVES = "arrow-show-on-obs-moves";
	public static final String ARROW_SHOW_ON_MOVE_LIST_MOVES = "arrow-show-on-move-list-moves";
	public static final String ARROW_SHOW_ON_MY_PREMOVES = "arrow-show-on-my-premoves";
	public static final String ARROW_SHOW_ON_MY_MOVES = "arrow-show-on-my-moves";
	public static final String ARROW_ANIMATION_DELAY = "arrow-animotion-delayv2";
	public static final String ARROW_WIDTH_PERCENTAGE = "arrow-width-percentage";
	public static final String ARROW_FADE_AWAY_MODE = "arrow-fade-away-mode";

	public static final String HIGHLIGHT_PREMOVE_COLOR = "hilight-premove-color";
	public static final String HIGHLIGHT_OBS_OPP_COLOR = "hilight-opponent-color";
	public static final String HIGHLIGHT_MY_COLOR = "hilight-my-color";
	public static final String HIGHLIGHT_OBS_COLOR = "hilight-obs-color";
	public static final String HIGHLIGHT_SHOW_ON_OBS_AND_OPP_MOVES = "hilight-show-on-obs-moves";
	public static final String HIGHLIGHT_SHOW_ON_MOVE_LIST_MOVES = "hilight-show-on-move-list-moves";
	public static final String HIGHLIGHT_SHOW_ON_MY_PREMOVES = "hilight-show-on-my-premoves";
	public static final String HIGHLIGHT_SHOW_ON_MY_MOVES = "hilight-show-on-my-moves";
	public static final String HIGHLIGHT_FADE_AWAY_MODE = "hilight-fade-away-mode";
	public static final String HIGHLIGHT_ANIMATION_DELAY = "highlight-animation-delayv2";
	public static final String HIGHLIGHT_WIDTH_PERCENTAGE = "highlight-width-percentage";

	public static final String RESULTS_COLOR = "results-color";
	public static final String RESULTS_ANIMATION_DELAY = "results-animation-delayv2";
	public static final String RESULTS_WIDTH_PERCENTAGE = "results-width-percentage";
	public static final String RESULTS_FADE_AWAY_MODE = "results-fade-away-mode";
	public static final String RESULTS_FONT = "results-font";
	public static final String RESULTS_IS_SHOWING = "results-is-showing";

	public static final String CHAT_REMOVE_SUB_TAB_MESSAGES_FROM_MAIN_TAB = "chat-remove-sub-tab-messages-from-main-tab";
	public static final String CHAT_UNDERLINE_URLS = "chat-underline-links";
	public static final String CHAT_UNDERLINE_QUOTED_TEXT = "chat-underlineQuotedText";
	public static final String CHAT_CHAT_EVENT_TYPE_COLOR_APPEND_TO = "chat-event-";
	public static final String CHAT_CONSOLE_BACKGROUND_COLOR = "chat-console-background-color";
	public static final String CHAT_INPUT_BACKGROUND_COLOR = "chat-input-background-color";
	public static final String CHAT_INPUT_DEFAULT_TEXT_COLOR = "chat-input-default-text-color";
	public static final String CHAT_INPUT_FONT = "chat-input-font";
	public static final String CHAT_LINK_UNDERLINE_COLOR = "chat-link-underline-color";
	public static final String CHAT_MAX_CONSOLE_CHARS = "chat-max-console-chars";
	public static final String CHAT_OUTPUT_BACKGROUND_COLOR = "chat-output-background-color";
	public static final String CHAT_OUTPUT_FONT = "chat-output-font";
	public static final String CHAT_OUTPUT_TEXT_COLOR = "chat-output-text-color";
	public static final String CHAT_PROMPT_COLOR = "chat-prompt-color";
	public static final String CHAT_QUOTE_UNDERLINE_COLOR = "chat-quote-underline-color";
	public static final String CHAT_TIMESTAMP_CONSOLE = "chat-timestamp-console";
	public static final String CHAT_TIMESTAMP_CONSOLE_FORMAT = "chat-timestamp-console-format";
	public static final String CHAT_UNDERLINE_SINGLE_QUOTES = "chat-underline-single-quotes";
	public static final String CHAT_UNDERLINE_COMMANDS = "chat-under-line-links";
	public static final String CHAT_IS_PLAYING_CHAT_ON_PTELL = "chat-is-playing-chat-on-ptell";
	public static final String CHAT_IS_PLAYING_CHAT_ON_PERSON_TELL = "chat-is-playing-chat-on-person-tell";
	public static final String CHAT_IS_SMART_SCROLL_ENABLED = "chat-is-smart-scroll-enabled";
	public static final String CHAT_OPEN_CHANNEL_TAB_ON_CHANNEL_TELLS = "chat-open-channel-tab-on-channel-tells";
	public static final String CHAT_OPEN_PERSON_TAB_ON_PERSON_TELLS = "chat-open-person-tab-on-direct-tells";
	public static final String CHAT_OPEN_PARTNER_TAB_ON_PTELLS = "chat-open-partner-tab-on-ptells";
	public static final String CHAT_PLAY_NOTIFICATION_SOUND_ON_ARRIVALS = "chat-play-notification-sound-on-arrivals";
	public static final String CHAT_PLAY_NOTIFICATION_SOUND_ON_DEPARTURES = "chat-play-notification-sound-on-departures";
	public static final String CHAT_COMMAND_LINE_SPELL_CHECK = "chat-command-line-spell-check";

	// Connector preferences should always use the short name of the connector
	// followed by the preference.
	public static final String FICS_AUTO_CONNECT = "fics-auto-connect";
	public static final String FICS_KEEP_ALIVE = "fics-keep-alive";
	public static final String FICS_LOGIN_SCRIPT = "fics-login-script";
	public static final String FICS_PROFILE = "fics-profile";
	public static final String FICS_CLOSE_TABS_ON_DISCONNECT = "fics-close-tabs-on-disconnect";
	public static final String FICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP = "fics-show-bug-buttons-on-partnership";
	public static final String BUGHOUSE_SHOW_BUGWHO_ON_PARTNERSHIP = "bughouse-show-bug-who-on-partnership";
	public static final String FICS_GAME_COMMANDS = "fics-games-commands";
	public static final String FICS_CHANNEL_COMMANDS = "fics-channel-commands";
	public static final String FICS_PERSON_COMMANDS = "fics-person-commands";
	public static final String FICS_PERSON_QUICK_COMMANDS = "fics-person-quick-commands";
	public static final String FICS_NO_WRAP_ENABLED = "fics-no-wrap-enabled";
	public static final String FICS_KEEP_ALIVE_COMMAND = "fics-keep-alive-command";
	public static final String FICS_REGULAR_EXPRESSIONS_TO_BLOCK = "fics-"
			+ REGULAR_EXPRESSIONS_TO_BLOCK;
	public static final String FICS_REMOVE_BLANK_LINES = "fics-"
			+ REMOVE_BLANK_LINES;
	public static final String FICS_TIMESEAL_IS_TIMESEAL_2 = "fics-is-timeseal-2";

	public static final String FICS_SEEK_GAME_TYPE = "fics-seek-game-type";
	public static final String FICS_SEEK_MINUTES = "fics-seek-minutes";
	public static final String FICS_SEEK_INC = "fics-seek-inc";
	public static final String FICS_SEEK_MIN_RATING = "fics-seek-min-rating";
	public static final String FICS_SEEK_MAX_RATING = "fics-seek-max-rating";
	public static final String FICS_SEEK_MANUAL = "fics-seek-manual";
	public static final String FICS_SEEK_FORMULA = "fics-seek-formula";
	public static final String FICS_SEEK_RATED = "fics-seek-rated";
	public static final String FICS_SEEK_COLOR = "fics-seek-color";

	public static final String FICS_PRIMARY_IS_ANON_GUEST = "fics-Primary-is-anon-guest";
	public static final String FICS_PRIMARY_IS_NAMED_GUEST = "fics-Primary-is-named-guest";
	public static final String FICS_PRIMARY_PASSWORD = "fics-Primary-password";
	public static final String FICS_PRIMARY_PORT = "fics-Primary-port";
	public static final String FICS_PRIMARY_SERVER_URL = "fics-Primary-server-url";
	public static final String FICS_PRIMARY_TIMESEAL_ENABLED = "fics-Primary-timeseal-enabled";
	public static final String FICS_PRIMARY_USER_NAME = "fics-Primary-user-name";

	public static final String FICS_SECONDARY_IS_ANON_GUEST = "fics-Secondary-is-anon-guest";
	public static final String FICS_SECONDARY_IS_NAMED_GUEST = "fics-Secondary-is-named-guest";
	public static final String FICS_SECONDARY_PASSWORD = "fics-Secondary-password";
	public static final String FICS_SECONDARY_PORT = "fics-Secondary-port";
	public static final String FICS_SECONDARY_SERVER_URL = "fics-Secondary-server-url";
	public static final String FICS_SECONDARY_TIMESEAL_ENABLED = "fics-Secondary-timeseal-enabled";
	public static final String FICS_SECONDARY_USER_NAME = "fics-Secondary-user-name";

	public static final String FICS_TERTIARY_IS_ANON_GUEST = "fics-Tertiary-is-anon-guest";
	public static final String FICS_TERTIARY_IS_NAMED_GUEST = "fics-Tertiary-is-named-guest";
	public static final String FICS_TERTIARY_PASSWORD = "fics-Tertiary-password";
	public static final String FICS_TERTIARY_PORT = "fics-Tertiary-port";
	public static final String FICS_TERTIARY_SERVER_URL = "fics-Tertiary-server-url";
	public static final String FICS_TERTIARY_TIMESEAL_ENABLED = "fics-Tertiary-timeseal-enabled";
	public static final String FICS_TERTIARY_USER_NAME = "fics-Tertiary-user-name";

	public static final String BICS_AUTO_CONNECT = "bics-auto-connect";
	public static final String BICS_KEEP_ALIVE = "bics-keep-alive";
	public static final String BICS_LOGIN_SCRIPT = "bics-login-script";
	public static final String BICS_PROFILE = "bics-profile";
	public static final String BICS_CLOSE_TABS_ON_DISCONNECT = "bics-close-tabs-on-disconnect";
	public static final String BICS_SHOW_BUGBUTTONS_ON_PARTNERSHIP = "bics-show-bug-buttons-on-partnership";
	public static final String BICS_GAME_COMMANDS = "bics-games-commands";
	public static final String BICS_CHANNEL_COMMANDS = "bics-channel-commands";
	public static final String BICS_PERSON_COMMANDS = "bics-person-commands";
	public static final String BICS_PERSON_QUICK_COMMANDS = "bics-person-quick-commands";
	public static final String BICS_KEEP_ALIVE_COMMAND = "bics-keep-alive-command";
	public static final String BICS_REGULAR_EXPRESSIONS_TO_BLOCK = "bics-"
			+ REGULAR_EXPRESSIONS_TO_BLOCK;
	public static final String BICS_REMOVE_BLANK_LINES = "bics-"
			+ REMOVE_BLANK_LINES;

	public static final String BICS_PRIMARY_IS_ANON_GUEST = "bics-Primary-is-anon-guest";
	public static final String BICS_PRIMARY_IS_NAMED_GUEST = "bics-Primary-is-named-guest";
	public static final String BICS_PRIMARY_PASSWORD = "bics-Primary-password";
	public static final String BICS_PRIMARY_PORT = "bics-Primary-port";
	public static final String BICS_PRIMARY_SERVER_URL = "bics-Primary-server-url";
	public static final String BICS_PRIMARY_TIMESEAL_ENABLED = "bics-Primary-timeseal-enabled";
	public static final String BICS_PRIMARY_USER_NAME = "bics-Primary-user-name";

	public static final String BICS_SECONDARY_IS_ANON_GUEST = "bics-Secondary-is-anon-guest";
	public static final String BICS_SECONDARY_IS_NAMED_GUEST = "bics-Secondary-is-named-guest";
	public static final String BICS_SECONDARY_PASSWORD = "bics-Secondary-password";
	public static final String BICS_SECONDARY_PORT = "bics-Secondary-port";
	public static final String BICS_SECONDARY_SERVER_URL = "bics-Secondary-server-url";
	public static final String BICS_SECONDARY_TIMESEAL_ENABLED = "bics-Secondary-timeseal-enabled";
	public static final String BICS_SECONDARY_USER_NAME = "bics-Secondary-user-name";

	public static final String BICS_TERTIARY_IS_ANON_GUEST = "bics-Tertiary-is-anon-guest";
	public static final String BICS_TERTIARY_IS_NAMED_GUEST = "bics-Tertiary-is-named-guest";
	public static final String BICS_TERTIARY_PASSWORD = "bics-Tertiary-password";
	public static final String BICS_TERTIARY_PORT = "bics-Tertiary-port";
	public static final String BICS_TERTIARY_SERVER_URL = "bics-Tertiary-server-url";
	public static final String BICS_TERTIARY_TIMESEAL_ENABLED = "bics-Tertiary-timeseal-enabled";
	public static final String BICS_TERTIARY_USER_NAME = "bics-Tertiary-user-name";

	public static final String SPEECH_PROCESS_NAME = "speech_process_name";

	public static final String SOUND_PROCESS_NAME = "sound_process_name";
}
