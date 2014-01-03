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

import raptor.script.ChatEventScript;
import raptor.service.ScriptService;
import raptor.swt.chat.ChatConsoleController;

public class ActivateScriptAlias extends RaptorAlias {
	public ActivateScriptAlias() {
		super("+script", "Activates an existing regular expression script. ",
				"'+script scriptName'" + "Example: '+script mySuperCoolScript'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWithIgnoreCase(command, "+script")) {
			String[] tokens = command.split(" +", 2);
			String scriptName = tokens[1];
			ChatEventScript script = ScriptService.getInstance()
					.getChatEventScript(scriptName);
			if (script == null) {
				return new RaptorAliasResult("",
					"Script '"
						+ scriptName
						+ "' not found. Type \"=scripts\" to get a list of all available scripts.");
			}
			if (!script.isActive()) {
				script.setActive(true);
				ScriptService.getInstance().save(script);
				return new RaptorAliasResult("", "Script '" + scriptName
					+ "' is now active.");
			} else {
				return new RaptorAliasResult("", "Script '" + scriptName
					+ "' is already active.");
			}
		}
		return null;
	}
}
