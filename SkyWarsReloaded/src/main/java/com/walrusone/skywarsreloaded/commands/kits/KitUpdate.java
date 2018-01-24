package com.walrusone.skywarsreloaded.commands.kits;

import com.walrusone.skywarsreloaded.commands.BaseCmd;
import com.walrusone.skywarsreloaded.objects.GameKit;
import com.walrusone.skywarsreloaded.utilities.Messaging;

public class KitUpdate extends BaseCmd { 
	
	public KitUpdate() {
		forcePlayer = true;
		cmdName = "kitupdate";
		alias = new String[]{"ku", "kitu", "kupdate"};
		argLength = 2; //counting cmdName
	}

	@Override
	public boolean run() {
		GameKit kit = GameKit.getKit(args[1]);
		if (kit == null) {
			player.sendMessage(new Messaging.MessageFormatter().setVariable("kit", args[1]).format("command.no-kit"));
			return true;
		}
		kit.setArmor(player.getInventory().getArmorContents());
		kit.setInventory(player.getInventory().getContents());
		
		GameKit.saveKit(kit);
		
		player.sendMessage(new Messaging.MessageFormatter().setVariable("kit", kit.getColorName()).format("command.kit-update"));
		return true;
	}

}
