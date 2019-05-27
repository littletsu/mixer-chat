/*
 * Copyright (C) 2019 Lil Tsukiya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.etzyy.mixer;

import com.mixer.api.MixerAPI;
import com.mixer.api.resource.MixerUser;
import com.mixer.api.resource.chat.methods.ChatSendMethod;
import com.mixer.api.resource.chat.ws.MixerChatConnectable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

/**
 *
 * @author Lil Tsukiya
 */
public class ServerChatListener implements Listener {
    
    MixerAPI mixerAPI;
    MixerChatConnectable connectable;
    MixerUser user;
    
    ServerChatListener(MixerAPI mixerAPIArg, MixerChatConnectable conn, MixerUser arguser) {
        mixerAPI = mixerAPIArg;
        connectable = conn;
        user = arguser;
    }
    
    @EventHandler
    public void onPlayerChat(PlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        if(player.hasPermission("mixer.sendToMixer")) connectable.send(ChatSendMethod.of(String.format("%s", message)));
    }
}
