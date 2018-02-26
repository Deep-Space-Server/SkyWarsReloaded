package com.walrusone.skywarsreloaded.menus.gameoptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.walrusone.skywarsreloaded.SkyWarsReloaded;
import com.walrusone.skywarsreloaded.enums.MatchState;
import com.walrusone.skywarsreloaded.enums.Vote;
import com.walrusone.skywarsreloaded.game.GameMap;
import com.walrusone.skywarsreloaded.game.PlayerCard;
import com.walrusone.skywarsreloaded.managers.MatchManager;
import com.walrusone.skywarsreloaded.menus.IconMenu;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Util;

public abstract class GameOption {

	protected ArrayList<String> itemList;
	protected ArrayList<Vote> voteList;
	protected IconMenu iconMenu;
	protected GameMap gameMap;
	protected String key;
	
	protected abstract void doSlotNine(Player player);
	protected abstract void doSlotEleven(Player player);
	protected abstract void doSlotThriteen(Player player);
	protected abstract void doSlotFifteen(Player player);
	protected abstract void doSlotSeventeen(Player player);
	public  abstract void setCard(PlayerCard pCard, Vote vote);
	public abstract Vote getVote(PlayerCard pCard);
	public abstract Vote getRandomVote();
	protected abstract void updateScoreboard();
	protected abstract Vote getDefault();
	public abstract void completeOption();
	
	protected void createMenu(String key, String name) {
		this.key = key;
		ArrayList<Inventory> invs = new ArrayList<Inventory>();
		Inventory inv = Bukkit.createInventory(null, 36, new Messaging.MessageFormatter().format(name));
		inv.clear();
		inv.setItem(9, SkyWarsReloaded.getIM().getItem(itemList.get(0)));
		inv.setItem(11, SkyWarsReloaded.getIM().getItem(itemList.get(1)));
		inv.setItem(13, SkyWarsReloaded.getIM().getItem(itemList.get(2)));
		inv.setItem(15, SkyWarsReloaded.getIM().getItem(itemList.get(3)));
		inv.setItem(17, SkyWarsReloaded.getIM().getItem(itemList.get(4)));
		invs.add(inv);
		
		SkyWarsReloaded.getIC().create(key, invs, new IconMenu.OptionClickEventHandler() {
			@Override
	        public void onOptionClick(IconMenu.OptionClickEvent event) {
				String itemName = event.getName();
				if (itemName.equalsIgnoreCase(SkyWarsReloaded.getNMS().getItemName(SkyWarsReloaded.getIM().getItem("exitMenuItem")))) {
		            new VotingMenu(event.getPlayer());
		            return;
		        }
				final GameMap gMap = MatchManager.get().getPlayerMap(event.getPlayer());
		    	if (gameMap == null || !gMap.equals(gameMap)) {
		    		return;
		    	}
				if (gameMap.getMatchState() == MatchState.WAITINGSTART) {
					int slot = event.getSlot();
					if (slot == 9) {
						doSlotNine(event.getPlayer());
					} else if (slot == 11) {
						doSlotEleven(event.getPlayer());
					} else if (slot == 13) {
						doSlotThriteen(event.getPlayer());
					} else if (slot == 15) {
						doSlotFifteen(event.getPlayer());
					} else if (slot == 17) {
						doSlotSeventeen(event.getPlayer());
					}
				}
			}
		});
		iconMenu = SkyWarsReloaded.getIC().getMenu(key);
	}
	
	public void restore() {
		Inventory inv = iconMenu.getInventory(0);
		inv.setItem(9, SkyWarsReloaded.getIM().getItem(itemList.get(0)));
		inv.setItem(11, SkyWarsReloaded.getIM().getItem(itemList.get(1)));
		inv.setItem(13, SkyWarsReloaded.getIM().getItem(itemList.get(2)));
		inv.setItem(15, SkyWarsReloaded.getIM().getItem(itemList.get(3)));
		inv.setItem(17, SkyWarsReloaded.getIM().getItem(itemList.get(4)));
		updateScoreboard();
	}
	
	public void setVote(Player player, Vote vote) {
		for (PlayerCard pCard: gameMap.getPlayerCards()) {
			if (pCard.getUUID() != null && pCard.getUUID().equals(player.getUniqueId())) {
				setCard(pCard, vote);
			}
		}
	}
	
	private HashMap<Vote, Integer> getVotes(boolean getRandom) {
    	HashMap <Vote, Integer> votes = new HashMap<Vote, Integer>();
		votes.put(voteList.get(0), 0);
		votes.put(voteList.get(1), 0);
		votes.put(voteList.get(2), 0);
		votes.put(voteList.get(3), 0);
		votes.put(voteList.get(4), 0);
		
		for (PlayerCard pCard: gameMap.getPlayerCards()) {
			Player player = pCard.getPlayer();
			if (player != null) {
				Vote vote = getVote(pCard);
				if (vote != null) {
					if ((vote == Vote.TIMERANDOM || vote == Vote.WEATHERRANDOM || vote == Vote.MODIFIERRANDOM || vote == Vote.CHESTRANDOM) && getRandom) {
						vote = getRandomVote();
					}
					int multiplier = Util.get().getMultiplier(player);
					votes.put(vote, votes.get(vote) + (1 * multiplier));
				}
			}	
		}
		return votes;
    }
	
	public void updateVotes() {
		HashMap <Vote, Integer> votes = getVotes(false);
			
		for (Vote vote: votes.keySet()) {
			if (vote == voteList.get(0)) {
				updateSlot(votes, vote, 0, 9, itemList);
			} else if (vote == voteList.get(1)) {
				updateSlot(votes, vote, 1, 11, itemList);
			} else if (vote == voteList.get(2)) {
				updateSlot(votes, vote, 2, 13, itemList);
			} else if (vote == voteList.get(3)) {
				updateSlot(votes, vote, 3, 15, itemList);
			} else if (vote == voteList.get(4)) {
				updateSlot(votes, vote, 4, 17, itemList);
			}
		}
		updateScoreboard();
	}
	
	private void updateSlot(HashMap <Vote, Integer> votes, Vote vote, int count, int slot, ArrayList<String> itemList) {
		ItemStack item = SkyWarsReloaded.getIM().getItem(itemList.get(count));
		item.setAmount(votes.get(vote) == 0 ? 1 : votes.get(vote));
		ItemMeta itemMeta = item.getItemMeta();
		List<String> lores = itemMeta.getLore();
		lores.add(" ");
		lores.add(new Messaging.MessageFormatter().setVariable("number", "" + votes.get(vote)).format("game.vote-display"));
		itemMeta.setLore(lores);
		item.setItemMeta(itemMeta);
		iconMenu.getInventory(0).setItem(slot, item);
	}
	
	public Vote getVoted() {
	   	HashMap <Vote, Integer> votes = getVotes(true);	
		int highest = 0;
		Vote voted = null;
		for (Vote vote: votes.keySet()) {
			if (votes.get(vote) >= highest) {
				highest = votes.get(vote);
				voted = vote;
			}
		}
		if (highest == 0) {
			voted = getDefault();
		}
		return voted;
	}
	
	protected String getVoteString(Vote vote) {
		switch(vote) {
		case CHESTRANDOM: return new Messaging.MessageFormatter().format("items.chest-random");
		case CHESTBASIC: return new Messaging.MessageFormatter().format("items.chest-basic");
		case CHESTNORMAL: return new Messaging.MessageFormatter().format("items.chest-normal");
		case CHESTOP: return new Messaging.MessageFormatter().format("items.chest-op");
		case CHESTSCAVENGER: return new Messaging.MessageFormatter().format("items.chest-scavenger");
		case TIMERANDOM: return new Messaging.MessageFormatter().format("items.time-random");
		case TIMEDAWN: return new Messaging.MessageFormatter().format("items.time-dawn");
		case TIMENOON: return new Messaging.MessageFormatter().format("items.time-noon");
		case TIMEDUSK: return new Messaging.MessageFormatter().format("items.time-dusk");
		case TIMEMIDNIGHT: return new Messaging.MessageFormatter().format("items.time-midnight");
		case WEATHERRANDOM: return new Messaging.MessageFormatter().format("items.weather-random");
		case WEATHERSUN: return new Messaging.MessageFormatter().format("items.weather-sunny");
		case WEATHERRAIN: return new Messaging.MessageFormatter().format("items.weather-rain");
		case WEATHERTHUNDER: return new Messaging.MessageFormatter().format("items.weather-storm");
		case WEATHERSNOW: return new Messaging.MessageFormatter().format("items.weather-snow");
		case MODIFIERRANDOM: return new Messaging.MessageFormatter().format("items.modifier-random");
		case MODIFIERSPEED: return new Messaging.MessageFormatter().format("items.modifier-speed");
		case MODIFIERJUMP: return new Messaging.MessageFormatter().format("items.modifier-jump");
		case MODIFIERSTRENGTH: return new Messaging.MessageFormatter().format("items.modifier-strength");
		case MODIFIERNONE: return new Messaging.MessageFormatter().format("items.modifier-none");
		case HEALTHRANDOM: return new Messaging.MessageFormatter().format("items.health-random");
		case HEALTHFIVE: return new Messaging.MessageFormatter().format("items.health-five");
		case HEALTHTEN: return new Messaging.MessageFormatter().format("items.health-ten");
		case HEALTHFIFTEEN: return new Messaging.MessageFormatter().format("items.health-fifteen");
		case HEALTHTWENTY: return new Messaging.MessageFormatter().format("items.health-twenty");
		default: return "";
		}
	}
	
	public String getKey() {
		return key;
	}
	
    public World getWorld(GameMap gameMap) {
        World mapWorld;
		mapWorld = SkyWarsReloaded.get().getServer().getWorld(gameMap.getName() + "_" + gameMap.getMapCount());
		return mapWorld;
    }
}


