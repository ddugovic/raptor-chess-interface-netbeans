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

public class OSUtils {
	protected static boolean isLikelyLinux = false;
	protected static boolean isLikelyOSX = false;
	protected static boolean isLikelyOSXCocoa = false;
	protected static boolean isLikelyWindows = false;
	protected static boolean isLikelyWindowsXP = false;
	protected static boolean isLikelyWindows7 = false;

	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) {
			isLikelyOSX = true;
			
			if (!System.getProperty("os.version").startsWith("10.4"))
			{
				isLikelyOSXCocoa = true;
			}
		} else if (osName.startsWith("Windows")) {
			isLikelyWindows = true;
			isLikelyWindowsXP = osName.contains("XP");
			isLikelyWindows7 = osName.contains("7");
		} else {
			isLikelyLinux = true;
		}
	}
	
	public static boolean isLikelyOSXCocoa() {
		return isLikelyOSXCocoa;
	}

	public static boolean isLikelyLinux() {
		return isLikelyLinux;
	}

	public static boolean isLikelyOSX() {
		return isLikelyOSX;
	}

	public static boolean isLikelyWindows() {
		return isLikelyWindows;
	}
	
	public static boolean isLikelyWindows7() {
		return isLikelyWindows;
	}

	public static boolean isLikelyWindowsXP() {
		return isLikelyWindowsXP;
	}

	public static void setLikelyWindowsXP(boolean isLikelyWindowsXP) {
		OSUtils.isLikelyWindowsXP = isLikelyWindowsXP;
	}
}
