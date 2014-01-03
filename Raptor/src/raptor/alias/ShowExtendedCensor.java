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
package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.swt.chat.ChatConsoleController;

public class ShowExtendedCensor extends RaptorAlias {
	public ShowExtendedCensor() {
		super("=extcensor", "Displays all of the people on extended censor. ",
				"'=extcensor'" + "Example: '=extcensor'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "=extcensor")) {
			String[] users = controller.getConnector()
					.getPeopleOnExtendedCensor();

			StringBuilder output = new StringBuilder(2000);
            output.append("Extended Censor List: (").append(users.length).append(" user(s)):\n");
			int count = 0;
			for (int i = 0; i < users.length; i++) {
				output.append(StringUtils.rightPad(users[i], 20));
				count++;
				if (count == 3) {
					output.append("\n");
					count = 0;
				}
			}
			return new RaptorAliasResult(null, output.toString());

		} else {
			return null;
		}
	}
}
