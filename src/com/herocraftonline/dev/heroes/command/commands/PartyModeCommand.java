package com.herocraftonline.dev.heroes.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.command.BaseCommand;
import com.herocraftonline.dev.heroes.party.HeroParty;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;

public class PartyModeCommand extends BaseCommand {

    public PartyModeCommand(Heroes plugin) {
        super(plugin);
        name = "PartyMode";
        description = "Change your parties mode";
        usage = "/party mode <pvp>";
        minArgs = 1;
        maxArgs = 1;
        identifiers.add("party mode");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            Hero hero = plugin.getHeroManager().getHero(player);
            if(hero.getParty() == null) {
                return;
            }
            HeroParty heroParty = hero.getParty();
            if(hero.getParty().getLeader() == player) {
                String argument = args[0].toUpperCase();
                if(argument != "PVP") {
                    return;
                }
                
                if(heroParty.checkMode(argument)) {
                    heroParty.removeMode(argument);
                    heroParty.messageParty("$1 is now on for this party", argument);
                }else {
                    heroParty.addMode(argument);
                    heroParty.messageParty("$1 is now off for this party", argument);
                }
                
            }else {
                Messaging.send(player, "Sorry, you need to be the leader to do that", (String[]) null);

            }
        }
    }

}

