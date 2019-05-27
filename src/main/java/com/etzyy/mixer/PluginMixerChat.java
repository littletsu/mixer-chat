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
import com.mixer.api.resource.chat.MixerChat;
import com.mixer.api.resource.chat.events.IncomingMessageEvent;
import com.mixer.api.resource.chat.events.UserJoinEvent;
import com.mixer.api.resource.chat.methods.AuthenticateMessage;
import com.mixer.api.resource.chat.methods.ChatSendMethod;
import com.mixer.api.resource.chat.replies.AuthenticationReply;
import com.mixer.api.resource.chat.replies.ReplyHandler;
import com.mixer.api.resource.chat.ws.MixerChatConnectable;
import com.mixer.api.services.impl.ChatService;
import com.mixer.api.services.impl.UsersService;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Lil Tsukiya
 */
public class PluginMixerChat extends JavaPlugin 
{
    //a
    private File customConfigFile;
    private FileConfiguration customConfig;
    
    @Override
    public void onEnable()   
    {
        createCustomConfig();
        
        String Key = customConfig.getString("key");
        String ID = customConfig.getString("id");
        String JoinedStreamMessage = customConfig.getString("joined-stream-message");
        
        Logger log = getLogger();
        MixerAPI mixerAPI = new MixerAPI(Key, ID);
        MixerUser user = null;
        MixerChat chat = null;
        MixerChatConnectable chatConnectable = null;
        
        try {
            user = mixerAPI.use(UsersService.class).getCurrent().get();
            chat = mixerAPI.use(ChatService.class).findOne(user.channel.id).get();
            chatConnectable = chat.connectable(mixerAPI);
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(PluginMixerChat.class.getName()).log(Level.SEVERE, null, ex);
            getServer().getPluginManager().disablePlugin(this);
        }
        
        final MixerChatConnectable connectable = chatConnectable;
        final MixerUser finaluser = user;
        
        if (chatConnectable.connect()) {
            chatConnectable.send(AuthenticateMessage.from(user.channel, user, chat.authkey), new ReplyHandler<AuthenticationReply>() {
                @Override
                public void onSuccess(AuthenticationReply reply) {
                    log.info(String.format("Succesfully connected to Mixer API as %s", finaluser.username));
                }
                @Override
                public void onFailure(Throwable var1) {
                    
                }
            });
        }
        
        connectable.on(UserJoinEvent.class, event -> {
            Bukkit.broadcastMessage(String.format("%s[MIXER]%s: %s%s", ChatColor.AQUA, ChatColor.RESET, ChatColor.YELLOW, JoinedStreamMessage.replace("=username=", event.data.username)));
        });
        
        connectable.on(IncomingMessageEvent.class, event -> {
            
            Bukkit.broadcastMessage(String.format("%s[MIXER]%s (%s): %s", ChatColor.AQUA, ChatColor.RESET, event.data.userName, event.data.message.message.get(0).text));
        });
        
        Bukkit.getPluginManager().registerEvents(new ServerChatListener(mixerAPI, connectable, finaluser), this);
    }
    
    @Override
    public void onDisable() 
    {
    
    }
    
    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "mixer-data.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("mixer-data.yml", false);
         }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
