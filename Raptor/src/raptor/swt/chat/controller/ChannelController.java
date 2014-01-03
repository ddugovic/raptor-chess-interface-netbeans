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
package raptor.swt.chat.controller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import raptor.Quadrant;
import raptor.Raptor;
import raptor.action.RaptorAction.RaptorActionContainer;
import raptor.chat.ChatEvent;
import raptor.chat.ChatType;
import raptor.connector.Connector;
import raptor.swt.SWTUtils;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chat.ChatUtils;

public class ChannelController extends ChatConsoleController {

	protected String channel;

	public ChannelController(Connector connector, String channel) {
		super(connector);
		this.channel = channel;
	}

	@Override
	public void dispose() {
		connector.setSpeakingChannelTells(channel, false);
		super.dispose();
	}

	public String getChannel() {
		return channel;
	}

	@Override
	public String getName() {
		// TODO: Display actual channel name
		return "Channel " + channel;
	}

	@Override
	public Quadrant getPreferredQuadrant() {
		return Raptor.getInstance().getPreferences().getQuadrant(
				getConnector().getShortName() + "-" + CHANNEL_TAB_QUADRANT);
	}

	@Override
	public String getPrependText(boolean checkButton) {
		if (isIgnoringActions()) {
			return "";
		}

		if (checkButton
				&& isToolItemSelected(ToolBarItemKey.PREPEND_TEXT_BUTTON)) {
			return connector.getChannelTabPrefix(channel);
		} else if (!checkButton) {
			return connector.getChannelTabPrefix(channel);
		} else {
			return "";
		}
	}

	@Override
	public String getPrompt() {
		return connector.getPrompt();
	}

	@Override
	public Control getToolbar(Composite parent) {
		if (toolbar == null) {
			toolbar = SWTUtils.createToolbar(parent);
			ChatUtils.addActionsToToolbar(this,
					RaptorActionContainer.ChannelChatConsole, toolbar);
			adjustAwayButtonEnabled();
		} else {
			toolbar.setParent(parent);
		}
		return toolbar;
	}

	@Override
	public boolean isAcceptingChatEvent(ChatEvent inboundEvent) {
		return inboundEvent.getType() == ChatType.CHANNEL_TELL
				&& StringUtils.equals(inboundEvent.getChannel(), channel)
				|| inboundEvent.getType() == ChatType.OUTBOUND
				&& inboundEvent.getMessage().startsWith(
						connector.getChannelTabPrefix(channel));
	}

	@Override
	public boolean isAwayable() {
		return false;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public boolean isPrependable() {
		return true;
	}

	@Override
	public boolean isSearchable() {
		return true;
	}
}
