package com.walrusone.skywarsreloaded.objects;

import java.util.UUID;

import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.objects.PlayerStat;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Util;
import com.walrusone.skywarsreloaded.utilities.VaultUtils;
import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.database.DataStorage;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerStat
{
    
	private static ArrayList<PlayerStat> players;
	private static HashMap<Player, Scoreboard> scoreboards = new HashMap<Player, Scoreboard>();
    private final String uuid;
    private String playername;
    private int wins;
    private int losts;
    private int kills;
    private int deaths;
    private int elo;
    private int xp;
    private String particleEffect;
    private String projectileEffect;
    private String glassColor;
    private String killSound;
    private String winSound;
    private String taunt;
    private boolean initialized;
    private PermissionAttachment perms;
    
    public PlayerStat(final Player player) {
    	this.initialized = false;
        this.uuid = player.getUniqueId().toString();
        this.playername = player.getName();
        this.perms = player.addAttachment(SkyWarsReloaded.get());
        DataStorage.get().loadStats(this);
        if (SkyWarsReloaded.getCfg().economyEnabled()) {
            DataStorage.get().loadperms(this);
        }
        saveStats(uuid);
        if (SkyWarsReloaded.getCfg().getSpawn() != null) {
            if (player.getWorld().equals(SkyWarsReloaded.getCfg().getSpawn().getWorld())) {
                updatePlayer(uuid);
            }
        }
    }
    
    private void saveStats(final String uuid) {
    	new BukkitRunnable() {
            public void run() {
            	if (PlayerStat.getPlayerStats(uuid) == null) {
            		this.cancel();
            	} else if (PlayerStat.getPlayerStats(uuid).isInitialized()) {
            		new BukkitRunnable() {
						@Override
						public void run() {
		            		if (SkyWarsReloaded.getCfg().bungeeMode()) {
		                		Player player = SkyWarsReloaded.get().getServer().getPlayer(UUID.fromString(uuid));
		                		if (player != null) {
		                			boolean joined = MatchManager.get().joinGame(player);
		                			if (!joined) {
		                    			SkyWarsReloaded.get().sendBungeeMsg(player, "Connect", SkyWarsReloaded.getCfg().getBungeeLobby());
		                    		}
		                		}
		            		}
						}
            		}.runTask(SkyWarsReloaded.get());

                    DataStorage.get().saveStats(PlayerStat.getPlayerStats(uuid));
            	} else {
            		saveStats(uuid);
            	}
            }
        }.runTaskLaterAsynchronously(SkyWarsReloaded.get(), 10L);
    }
    
    public static void updatePlayer(final String uuid) {
    	new BukkitRunnable() {
            public void run() {
            	if (PlayerStat.getPlayerStats(uuid) == null) {
            		this.cancel();
            	} else if (PlayerStat.getPlayerStats(uuid).isInitialized()) {
            		final Player player = SkyWarsReloaded.get().getServer().getPlayer(UUID.fromString(uuid));
            		if (player != null) {
                		new BukkitRunnable() {
    						@SuppressWarnings("deprecation")
							@Override
    						public void run() {
    							PlayerStat pStats = PlayerStat.getPlayerStats(player);
    	        		        player.closeInventory();
    	        		        player.setGameMode(GameMode.ADVENTURE);
    	        		        Util.get().setPlayerExperience(player, pStats.getXp());
    	        		        player.setHealth(20);
    	        		        player.setFoodLevel(20);
    	        		        player.setSaturation(20);
    	        		        Util.get().clear(player);
    	        		        player.setFireTicks(0);
    	        		        player.resetPlayerTime();
    	        		        player.resetPlayerWeather();
    	        		        if (SkyWarsReloaded.getCfg().lobbyBoardEnabled()) {
        	        		        getScoreboard(player);
        	        		        player.setScoreboard(getPlayerScoreboard(player));
    	        		        }
    	        		        if (SkyWarsReloaded.getCfg().optionsMenuEnabled()) {
        	        		        player.getInventory().setItem(SkyWarsReloaded.getCfg().getOptionsSlot(), SkyWarsReloaded.getIM().getItem("optionselect"));
    	        		        }
    	        		        if (SkyWarsReloaded.getCfg().joinMenuEnabled() && player.hasPermission("sw.join")) {
        	        		        player.getInventory().setItem(SkyWarsReloaded.getCfg().getJoinSlot(), SkyWarsReloaded.getIM().getItem("joinselect"));
    	        		        }
    	        		        if (SkyWarsReloaded.getCfg().spectateMenuEnabled() && player.hasPermission("sw.spectate")) {
        	        		        player.getInventory().setItem(SkyWarsReloaded.getCfg().getSpectateSlot(), SkyWarsReloaded.getIM().getItem("spectateselect"));
    	        		        }
    	        		        player.updateInventory();
    						}
                		}.runTask(SkyWarsReloaded.get());
            		} else {
            			this.cancel();
            		}
            	} else {
            		updatePlayer(uuid);
            	}
            }
        }.runTaskLaterAsynchronously(SkyWarsReloaded.get(), 10L);
    }
       
    public static ArrayList<PlayerStat> getPlayers() {
        return PlayerStat.players;
    }
    
    public static void setPlayers(final ArrayList<PlayerStat> playerData) {
        PlayerStat.players = playerData;
    }
    
    public static PlayerStat getPlayerStats(final String playerData) {
        for (final PlayerStat pData : getPlayers()) {
            if (pData.getId().equals(playerData)) {
                return pData;
            }
        }
        return null;
    }
    
    public static PlayerStat getPlayerStats(final Player player) {
        String uuid = player.getUniqueId().toString();
    	for (final PlayerStat pData : getPlayers()) {
            if (pData.getId().equals(uuid)) {
                return pData;
            }
        }
        return null;
    }
    
    public static PlayerStat getPlayerStats(final UUID uuid) {
    	for (final PlayerStat pData : getPlayers()) {
            if (pData.getId().equals(uuid.toString())) {
                return pData;
            }
        }
        return null;
    }
    
    public String getId() {
        return this.uuid;
    }
    
    public int getWins() {
        return this.wins;
    }
    
    public void setWins(final int a1) {
        this.wins = a1;
    }
    
    public int getKills() {
        return this.kills;
    }
    
    public int getXp() {
    	return this.xp;
    }
    
    public void setXp(int x) {
    	this.xp = x;
    }
    
    public void setKills(final int a1) {
        this.kills = a1;
    }
    
    public int getDeaths() {
        return this.deaths;
    }
    
    public void setDeaths(final int a1) {
        this.deaths = a1;
    }
        
    public int getElo() {
        return this.elo;
    }
    
    public void setElo(final int a1) {
        this.elo = a1;
    }
    
    public int getLosses() {
        return this.losts;
    }
    
    public void setLosts(final int a1) {
        this.losts = a1;
    }
        
    static {
        PlayerStat.players = new ArrayList<PlayerStat>();
    }
    
    public boolean isInitialized() {
        return this.initialized;
    }
    
    public void setInitialized(final boolean a1) {
        this.initialized = a1;
    }

	public void clear() {
		this.losts = 0;
		this.wins = 0;
		this.kills = 0;
		this.deaths = 0;
		this.elo = 1500;
	}

	public void setParticleEffect(String effect) {
		this.particleEffect = effect;
	}
	
	public String getParticleEffect() {
		return particleEffect;
	}
	
	public void setProjectileEffect(String effect) {
		this.projectileEffect = effect;
	}
	
	public String getProjectileEffect() {
		return projectileEffect;
	}

	public void setGlassColor(String glassC) {
		this.glassColor = glassC;
	}
	
	public String getGlassColor() {
		return this.glassColor;
	}
	
	public void setKillSound(String glassC) {
		this.killSound = glassC;
	}
	
	public String getKillSound() {
		return this.killSound;
	}

	public String getWinSound() {
		return this.winSound;
	}

	public String getPlayerName() {
		return playername;
	}

	public void setWinSound(String string) {
		this.winSound = string;
	}

	public void setTaunt(String string) {
		taunt = string;
	}
	
	public String getTaunt(){
		return taunt;
	}
	
	//Scoreboard Methods
	
	public static void getScoreboard(Player player) {
		Scoreboard scoreboard = scoreboards.get(player);
		if (scoreboard != null) {
            resetScoreboard(player);
        }
		ScoreboardManager manager = SkyWarsReloaded.get().getServer().getScoreboardManager();
		scoreboard = manager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("info", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        scoreboards.put(player, scoreboard);
		updateScoreboard(player);
	}
	
	public static void updateScoreboard(Player player) {
		Scoreboard scoreboard = scoreboards.get(player);
		for (Objective objective: scoreboard.getObjectives()) {
        	if (objective != null) {
                objective.unregister();
            }
    	}
		
		Objective objective = scoreboard.registerNewObjective("info", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        String sb = "scoreboards.lobbyboard.line";
        
        for (int i = 1; i < 17; i++) {
        	if (i == 1) {
    	        String leaderboard = getScoreboardLine(sb + i, player);
    	        objective.setDisplayName(leaderboard);
    		} else {
    			String s = getScoreboardLine(sb + i, player);
    			if (s.length() == 0) {
    				for (int j = 0; j < i; j++) {
    					s = s + " ";
    				}
    			} 
    			if (!s.equalsIgnoreCase("remove")) {
        			Score score = objective.getScore(s);
    				score.setScore(17-i);
    			}
    		}
        }	
	}
		
	private static String getScoreboardLine(String lineNum, Player player) {
		return new Messaging.MessageFormatter()
				.setVariable("elo", "" + PlayerStat.getPlayerStats(player).getElo())
				.setVariable("wins", "" + PlayerStat.getPlayerStats(player).getWins())
				.setVariable("losses", "" + PlayerStat.getPlayerStats(player).getLosses())
				.setVariable("kills", "" + PlayerStat.getPlayerStats(player).getKills())
				.setVariable("deaths", "" + PlayerStat.getPlayerStats(player).getDeaths())
				.setVariable("xp", "" + PlayerStat.getPlayerStats(player).getXp())
				.setVariable("killdeath", String.format("%1$,.2f", ((double)((double)PlayerStat.getPlayerStats(player).getKills()/(double)PlayerStat.getPlayerStats(player).getDeaths()))))
				.setVariable("winloss", String.format("%1$,.2f", ((double)((double)PlayerStat.getPlayerStats(player).getWins()/(double)PlayerStat.getPlayerStats(player).getLosses()))))
				.setVariable("balance", "" + getBalance(player))
				.format(lineNum);
	}
	
	private static double getBalance(Player player) {
		if (SkyWarsReloaded.getCfg().economyEnabled()) {
			return VaultUtils.get().getBalance(player);
		}
		return 0;
	}
	
    private static void resetScoreboard(Player player) {
    	Scoreboard scoreboard = scoreboards.get(player);
    	for (Objective objective: scoreboard.getObjectives()) {
        	if (objective != null) {
                objective.unregister();
            }
    	}
        
        if (scoreboard != null) {
            scoreboard = null;
        }
    }

	public static Scoreboard getPlayerScoreboard(Player player) {
		return scoreboards.get(player);
	}
	
	public PermissionAttachment getPerms() {
		return perms;
	}
	
	public void addPerm(String perm, boolean save) {
		perms.setPermission(perm, true);
		if (save) {
			DataStorage.get().savePerms(this);
		}
	}

	public static void removePlayer(String id) {
		PlayerStat ps = getPlayerStats(id);
		if (ps != null) {
			players.remove(ps);
		}
	}
}
