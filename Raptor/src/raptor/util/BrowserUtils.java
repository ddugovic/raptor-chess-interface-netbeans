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

import java.lang.reflect.Method;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Shell;

import raptor.util.Logger;
 

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.BrowserWindowItem;

/**
 * Utilities for dealing with browsers.
 */
public class BrowserUtils {
	private static final Logger LOG = Logger.getLogger(BrowserUtils.class);

	public static String getWatchBotJavascript(String player) {
		return "<html><body>"
				+ "<form action=\"http://mekk.waw.pl/mk/watchbot/search/player\" method=\"post\">"
				+ "<input id=\"player\" name=\"player\" size=\"15\" type=\"text\" value=\""
				+ player + "\" />" + "<select name=\"color\" id=\"color\">"
				+ "<option value=\"\">Any color</option>"
				+ "<option value=\"white\">White</option>"
				+ "<option value=\"black\">Black</option>" + "</select>"
				+ "</form>" + "<script>" + "document.forms[0].submit();"
				+ "</script>" + "</body></html>";
	}

	/**
	 * Opens the link in an external browser. Code taken from: Bare Bones
	 * Browser Launch Version 1.5 (December 10, 2005) By Dem Pilafian Supports:
	 * Mac OS X, GNU/Linux, Unix, Windows XP Public Domain Software -- Free to
	 * Use as You Like
	 * 
	 * @param url
	 *            The url to open.
	 */
	public static void openExternalUrl(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class<?> fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // assume Unix or Linux
				String prefBrowser = Raptor.getInstance().getPreferences()
						.getString(PreferenceKeys.APP_LINUX_UNIX_BROWSER_NAME);

				if (StringUtils.isBlank(prefBrowser)) {
					String[] browsers = { "firefox", "opera", "konqueror",
							"epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length
							&& browser == null; count++) {
						if (Runtime.getRuntime().exec(
								new String[] { "which", browsers[count] })
								.waitFor() == 0) {
							browser = browsers[count];
						}
					}
					if (browser == null) {
						throw new Exception("Could not find web browser");
					} else {
						new ProcessBuilder(browser, url).start();
					}
				} else {
					new ProcessBuilder(prefBrowser, url).start();
				}
			}
		} catch (Exception e) {
			LOG.error("Error occured launching browser:", e);
		}
	}

	/**
	 * This checks the users preferences and opens the browser either internally
	 * or externally depending on how its set. It will also check to see if a
	 * browser is currently in use. If it is it will use that browser to display
	 * the url.
	 */
	public static void openHtml(String html) {
		if (!StringUtils.isNotBlank(html)) {
			return; // is blank
		}

		BrowserWindowItem item = Raptor.getInstance().getWindow()
				.getBrowserWindowItem();
		if (item == null) {
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new BrowserWindowItem(html, true));
		} else {
			item.setHTML(html);

			Raptor.getInstance().getWindow().forceFocus(item);
		}
	}
	
	public static boolean internalBrowserSupported() {
		boolean res = false;
		Shell shell = new Shell(Raptor.getInstance().getDisplay());
		Browser browser = null;
		try {
			browser = new Browser(shell, SWT.NONE);
		} catch (SWTError e) {
		  
		}
		if (browser != null) {
			res = true;
			browser.close();
                }
		shell.dispose();
		return res;
	}

	public static void openInternalUrl(String url) {
		BrowserWindowItem item = Raptor.getInstance().getWindow()
				.getBrowserWindowItem();
		if (item == null) {
			Raptor.getInstance().getWindow().addRaptorWindowItem(
					new BrowserWindowItem(url));
		} else {
			item.setUrl(url);
			Raptor.getInstance().getWindow().forceFocus(item);
		}
	}

	/**
	 * This checks the users preferences and opens the browser either internally
	 * or externally depending on how its set. It will also check to see if a
	 * browser is currently in use. If it is it will use that browser to display
	 * the url.
	 */
	public static void openUrl(String url) {
		if (StringUtils.isNotBlank(url)) {
			if (Raptor.getInstance().getPreferences().getBoolean(
					PreferenceKeys.APP_OPEN_LINKS_IN_EXTERNAL_BROWSER)) {
				openExternalUrl(url);
		} else {
				openInternalUrl(url);
			}
		}
	}
}
