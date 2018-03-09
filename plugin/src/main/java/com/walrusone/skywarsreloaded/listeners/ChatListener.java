package com.walrusone.skywarsreloaded.listeners;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.Maps;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.managers.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.VaultUtils;

public class ChatListener implements Listener {

	private static Map<UUID, Long> chatList = Maps.newHashMap();
	private static Map<UUID, String> toChange = Maps.newHashMap();
	
	@EventHandler
    public void signPlaced(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		UUID uuid = event.getPlayer().getUniqueId();
		if (chatList.containsKey(uuid)) {
			if (Math.abs((System.currentTimeMillis() - chatList.get(uuid))) < 20000) {
				ChatListener.chatList.remove(uuid);
				event.setCancelled(true);
				String[] settings = toChange.get(uuid).split(":");
				GameMap gMap = GameMap.getMap(settings[0]);
				String setting = settings[1];
				String variable = event.getMessage();
				if (gMap != null && setting.equals("display")) {
					gMap.setDisplayName(variable);
					player.sendMessage(new Messaging.MessageFormatter().setVariable("mapname", gMap.getName()).setVariable("displayname", variable).format("maps.name"));
					new BukkitRunnable() {
						@Override
						public void run() {
							gMap.update();
						}
					}.runTask(SkyWarsReloaded.get());
					SkyWarsReloaded.getIC().show(player, gMap.getArenaKey());
				} else if (gMap != null && setting.equalsIgnoreCase("creator")) {
					gMap.setCreator(variable);
					player.sendMessage(new Messaging.MessageFormatter().setVariable("mapname", gMap.getName()).setVariable("creator", variable).format("maps.creator"));
					new BukkitRunnable() {
						@Override
						public void run() {
							gMap.update();
						}
					}.runTask(SkyWarsReloaded.get());
					SkyWarsReloaded.getIC().show(player, gMap.getArenaKey());
				}
				ChatListener.toChange.remove(uuid);
			} else {
				ChatListener.chatList.remove(uuid);
				ChatListener.toChange.remove(uuid);
			}
		}
	}
	
	public static void setTime(UUID uuid, long time) {
		chatList.put(uuid,  time);
	}
	
	public static void setSetting(UUID uuid, String setting) {
		toChange.put(uuid,  setting);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
		if (SkyWarsReloaded.getCfg().formatChat()) {
			GameMap gMap = MatchManager.get().getPlayerMap(event.getPlayer());
			GameMap sMap = MatchManager.get().getSpectatorMap(event.getPlayer());
			if (gMap != null || sMap != null) {
				if (SkyWarsReloaded.getCfg().useExternalChat()) {
				    PlayerStat ps = PlayerStat.getPlayerStats(event.getPlayer());
	    			String prefix = "";
                    if (SkyWarsReloaded.getCfg().addPrefix() && ps != null) {
                        prefix = new Messaging.MessageFormatter()
                                .setVariable("elo", Integer.toString(ps.getElo()))
                                .setVariable("wins", Integer.toString(ps.getWins()))
                                .setVariable("losses", Integer.toString(ps.getLosses()))
                                .setVariable("kills", Integer.toString(ps.getKills()))
                                .setVariable("deaths",Integer.toString(ps.getDeaths()))
                                .setVariable("xp", Integer.toString(ps.getXp()))
                                .format("chat.externalPrefix");
                    }
	            	String format = event.getFormat();
	            	ArrayList<Player> recievers = null;
	            	if (sMap != null && SkyWarsReloaded.getCfg().limitSpecChat()) {
		            		recievers = sMap.getMessageAblePlayers(true);
		            } else if (sMap != null && SkyWarsReloaded.getCfg().limitGameChat() || gMap != null && SkyWarsReloaded.getCfg().limitGameChat()) {
	            	    if (sMap != null) {
                            recievers = sMap.getMessageAblePlayers(false);
                        } else {
                            recievers = gMap.getMessageAblePlayers(false);
                        }
		            }
		            final ArrayList recipents = recievers;
	            	if (recievers != null) {
                        event.getRecipients().removeIf((Player player) -> !recipents.contains(player));
	            	}
	            	event.setFormat(prefix + format);
				} else {
					String prefix = "";
					if (SkyWarsReloaded.getCfg().economyEnabled()) {
						if (VaultUtils.get().getChat() != null) {
							if (VaultUtils.get().getChat().getPlayerPrefix(event.getPlayer()) != null) {
								prefix = VaultUtils.get().getChat().getPlayerPrefix(event.getPlayer());
							}
						}
					}
	    		
					String colorMessage = ChatColor.translateAlternateColorCodes('&', event.getMessage());
					String message;
					if (event.getPlayer().hasPermission("sw.colorchat")) {
						message = colorMessage;
					} else {
						message = ChatColor.stripColor(colorMessage);
	            		while (message.contains("&")) {
	            			message = ChatColor.translateAlternateColorCodes('&', message);
	            			message = ChatColor.stripColor(message);
	            		}
					}
					PlayerStat ps = PlayerStat.getPlayerStats(event.getPlayer());
					String messageToSend;
					if (ps != null) {
                        if (sMap != null) {
                            messageToSend = new Messaging.MessageFormatter()
                                    .setVariable("name", event.getPlayer().getName())
                                    .setVariable("displayname", event.getPlayer().getDisplayName())
                                    .setVariable("elo", Integer.toString(ps.getElo()))
                                    .setVariable("wins", Integer.toString(ps.getWins()))
                                    .setVariable("losses", Integer.toString(ps.getLosses()))
                                    .setVariable("kills", Integer.toString(ps.getKills()))
                                    .setVariable("deaths",Integer.toString(ps.getDeaths()))
                                    .setVariable("xp", Integer.toString(ps.getXp()))
                                    .setVariable("prefix", prefix)
                                    .setVariable("message", message)
                                    .format("chat.specchat");
                        } else {
                            messageToSend = new Messaging.MessageFormatter()
                                    .setVariable("player", event.getPlayer().getName())
                                    .setVariable("displayname", event.getPlayer().getDisplayName())
                                    .setVariable("elo", Integer.toString(ps.getElo()))
                                    .setVariable("wins", Integer.toString(ps.getWins()))
                                    .setVariable("losses", Integer.toString(ps.getLosses()))
                                    .setVariable("kills", Integer.toString(ps.getKills()))
                                    .setVariable("deaths",Integer.toString(ps.getDeaths()))
                                    .setVariable("xp", Integer.toString(ps.getXp()))
                                    .setVariable("prefix", prefix)
                                    .setVariable("message", message)
                                    .format("chat.ingamechat");
                        }
                        if (sMap != null && SkyWarsReloaded.getCfg().limitSpecChat()) {
                            event.setCancelled(true);
                            MatchManager.get().specMessage(sMap, messageToSend);
                        } else if (sMap != null && SkyWarsReloaded.getCfg().limitSpecChat() || gMap != null && SkyWarsReloaded.getCfg().limitGameChat()){
                            event.setCancelled(true);
                            if(sMap != null) {
                                MatchManager.get().message(sMap, messageToSend);
                            } else {
                                MatchManager.get().message(gMap, messageToSend);
                            }
                        } else {
                            event.setFormat(messageToSend);
                        }
                    }
	    		}
			} else {
				if(event.getPlayer().getWorld().equals(SkyWarsReloaded.getCfg().getSpawn().getWorld())) {
					if (SkyWarsReloaded.getCfg().useExternalChat()) { 	
		    			PlayerStat ps = PlayerStat.getPlayerStats(event.getPlayer());
		            	String prefix = "";
		            	if (SkyWarsReloaded.getCfg().addPrefix() && ps != null) {
		            		prefix = new Messaging.MessageFormatter()
    						.setVariable("elo", Integer.toString(ps.getElo()))
    						.setVariable("wins", Integer.toString(ps.getWins()))
    						.setVariable("losses", Integer.toString(ps.getLosses()))
    						.setVariable("kills", Integer.toString(ps.getKills()))
    						.setVariable("deaths",Integer.toString(ps.getDeaths()))
    						.setVariable("xp", Integer.toString(ps.getXp()))
    						.format("chat.externalPrefix");
		            	}
		            	String format = event.getFormat();
		            	if (SkyWarsReloaded.getCfg().limitLobbyChat()) {
			            	World world = event.getPlayer().getWorld();
			            	event.getRecipients().removeIf((Player player) -> !player.getWorld().equals(world));
		            	}
		            	event.setFormat(prefix + format);
					} else {	            	
						String prefix = "";
						if (SkyWarsReloaded.getCfg().economyEnabled()) {
							if (VaultUtils.get().getChat() != null) {
								if (VaultUtils.get().getChat().getPlayerPrefix(event.getPlayer()) != null) {
									prefix = VaultUtils.get().getChat().getPlayerPrefix(event.getPlayer());
								}
							}
						}
		    		
						String colorMessage = ChatColor.translateAlternateColorCodes('&', event.getMessage());
						String message;
						if (event.getPlayer().hasPermission("sw.colorchat")) {
							message = colorMessage;
						} else {
							message = ChatColor.stripColor(colorMessage);
		            		while (message.contains("&")) {
		            			message = ChatColor.translateAlternateColorCodes('&', message);
		            			message = ChatColor.stripColor(message);
		            		}
						}
						PlayerStat ps = PlayerStat.getPlayerStats(event.getPlayer());
						if (ps != null) {
                            if (SkyWarsReloaded.getCfg().limitLobbyChat()) {
                                World world = event.getPlayer().getWorld();
                                event.getRecipients().removeIf((Player player) -> !player.getWorld().equals(world));
                            }
                            event.setFormat(new Messaging.MessageFormatter()
                                    .setVariable("player", event.getPlayer().getName())
                                    .setVariable("displayname", event.getPlayer().getDisplayName())
                                    .setVariable("elo", Integer.toString(ps.getElo()))
                                    .setVariable("wins", Integer.toString(ps.getWins()))
                                    .setVariable("losses", Integer.toString(ps.getLosses()))
                                    .setVariable("kills", Integer.toString(ps.getKills()))
                                    .setVariable("deaths",Integer.toString(ps.getDeaths()))
                                    .setVariable("xp", Integer.toString(ps.getXp()))
                                    .setVariable("prefix", prefix)
                                    .setVariable("message", message)
                                    .format("chat.lobbychat"));
                        }
					}
				}
			}
		}
    }

}
