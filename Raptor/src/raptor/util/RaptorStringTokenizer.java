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

/**
 * It works just like java.util.StringTokenizer with some differences. You can
 * specify if blocks of delimiters are eaten. For instance:
 * "test1 test2 test3  test4" will return: "test1" "test2" "test3" "" "test4" if
 * not eating blocks of delimiters and it will return "test1" "test2" "test3"
 * "test4" if isEatingBlocksOfDelimiters.
 * 
 * You can also obtain what is left to tokenize by calling getWhatsLeft().
 * PGN-specific features of this class have been moved into SimplePgnParser.
 */
public class RaptorStringTokenizer {

	protected int currentIndex = 0;

	protected String delimiters;

	protected boolean isEatingBlocksOfDelimiters;

	protected String source;

	public RaptorStringTokenizer(String string, String delimiters) {
		this(string, delimiters, false);
	}

	public RaptorStringTokenizer(String string, String delimiters,
			boolean isEatingBlocksOfDelimiters) {
		source = string;
		this.delimiters = delimiters;
		this.isEatingBlocksOfDelimiters = isEatingBlocksOfDelimiters;
	}

	public String getWhatsLeft() {
		if (isEmpty()) {
			return "";
		}
		return source.substring(currentIndex);
	}

	public boolean hasMoreTokens() {
		synchronized (this) {
			if (isEmpty()) {
				return false;
			}
			if (isEatingBlocksOfDelimiters) {
				trimStartingDelimiters();
			}
			return !isEmpty();
		}
	}

	/**
	 * Returns null if there is nothing left.
	 */
	public String nextToken() {
		String result = null;
		synchronized (this) {
			if (isEmpty()) {
				return null;
			} else {
				if (isEatingBlocksOfDelimiters) {
					trimStartingDelimiters();
				}

				int nearestDelimeter = -1;
				for (int i = 0; i < delimiters.length(); i++) {
					int delimiter = source.indexOf(delimiters.charAt(i),
							currentIndex);
					if (nearestDelimeter == -1 || delimiter != -1
							&& delimiter < nearestDelimeter) {
						nearestDelimeter = delimiter;
					}
				}

				if (nearestDelimeter == -1) {
					result = source.substring(currentIndex);
					currentIndex = source.length();
				} else {
					result = source.substring(currentIndex, nearestDelimeter);
					currentIndex = nearestDelimeter + 1;
					if (isEatingBlocksOfDelimiters) {
						// Now trim any delimiters that are at the begining of source.
						trimStartingDelimiters();
					}
				}
			}
		}
		return result;
	}

	private boolean isEmpty() {
		return currentIndex >= source.length();
	}

	private void trimStartingDelimiters() {
		while (!isEmpty()
				&& delimiters.indexOf(source.charAt(currentIndex)) != -1) {
			currentIndex++;
		}
	}

}
