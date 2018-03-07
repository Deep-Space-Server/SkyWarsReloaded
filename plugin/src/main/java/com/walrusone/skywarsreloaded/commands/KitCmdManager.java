package com.walrusone.skywarsreloaded.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.walrusone.skywarsreloaded.commands.kits.CreateCmd;
import com.walrusone.skywarsreloaded.commands.kits.EnableCmd;
import com.walrusone.skywarsreloaded.commands.kits.IconCmd;
import com.walrusone.skywarsreloaded.commands.kits.ListCmd;
import com.walrusone.skywarsreloaded.commands.kits.LoadCmd;
import com.walrusone.skywarsreloaded.commands.kits.LockedIconCmd;
import com.walrusone.skywarsreloaded.commands.kits.LoreCmd;
import com.walrusone.skywarsreloaded.commands.kits.NameCmd;
import com.walrusone.skywarsreloaded.commands.kits.PermCmd;
import com.walrusone.skywarsreloaded.commands.kits.PositionCmd;
import com.walrusone.skywarsreloaded.commands.kits.UpdateCmd;
import com.walrusone.skywarsreloaded.utilities.Messaging;
import com.walrusone.skywarsreloaded.utilities.Util;

public class KitCmdManager implements CommandExecutor {
	private List<BaseCmd> kitcmds = new ArrayList<BaseCmd>();

	//Add New Commands Here
	public KitCmdManager() {
		kitcmds.add(new CreateCmd("kit"));
		kitcmds.add(new EnableCmd("kit"));
		kitcmds.add(new IconCmd("kit"));
		kitcmds.add(new LockedIconCmd("kit"));
		kitcmds.add(new LoadCmd("kit"));
		kitcmds.add(new LoreCmd("kit"));
		kitcmds.add(new NameCmd("kit"));
		kitcmds.add(new PositionCmd("kit"));
		kitcmds.add(new PermCmd("kit"));
		kitcmds.add(new UpdateCmd("kit"));
		kitcmds.add(new ListCmd("kit"));
	}

	public boolean onCommand(CommandSender s, Command command, String label, String[] args) { 
		if (args.length == 0 || getCommands(args[0]) == null) {
			s.sendMessage(new Messaging.MessageFormatter().format("helpList.header"));
			sendHelp(kitcmds, s, "1");
			s.sendMessage(new Messaging.MessageFormatter().format("helpList.footer"));
		} else getCommands(args[0]).processCmd(s, args);
		return true;
	}
	
	private void sendHelp(List<BaseCmd> cmds, CommandSender s, String num) {
		int count = 0;
		for (BaseCmd cmd : cmds) {
			if (Util.get().hp(cmd.getType(), s, cmd.cmdName)) {
				count++;
				if (count == 1) {
					s.sendMessage(" ");
					s.sendMessage(new Messaging.MessageFormatter().format("helpList.swkit.header" + num));
				}
				s.sendMessage(new Messaging.MessageFormatter().format("helpList.swkit." + cmd.cmdName));
			}
		}
	}

	private BaseCmd getCommands(String s) {
		BaseCmd cmd = null;
		cmd = getCmd(kitcmds, s);
		return cmd;
	}

	private BaseCmd getCmd(List<BaseCmd> cmds, String s) {
		for (BaseCmd cmd : cmds) {
			if (cmd.cmdName.equalsIgnoreCase(s)) {
				return cmd;
			}
			for (String alias: cmd.alias) {
				if (alias.equalsIgnoreCase(s))
					return cmd;
			}
		}
		return null;
	}
}

