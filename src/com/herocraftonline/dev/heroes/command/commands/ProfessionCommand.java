package com.herocraftonline.dev.heroes.command.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.ClassChangeEvent;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.command.BasicInteractiveCommand;
import com.herocraftonline.dev.heroes.command.BasicInteractiveCommandState;
import com.herocraftonline.dev.heroes.command.CommandHandler;
import com.herocraftonline.dev.heroes.command.InteractiveCommandState;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Properties;

public class ProfessionCommand extends BasicInteractiveCommand {

    private final Heroes plugin;
    private Map<Player, HeroClass> pendingClassSelections = new HashMap<Player, HeroClass>();
    private Map<Player, Boolean> pendingClassCostStatus = new HashMap<Player, Boolean>();

    public ProfessionCommand(Heroes plugin) {
        super("Choose Profession Class");
        this.plugin = plugin;
        this.setStates(new InteractiveCommandState[] { new StateA(), new StateB() });
        setDescription("Selects a new path or specialization as your profession");
        setUsage("/hero profession §9<type>");
    }

    @Override
    public String getCancelIdentifier() {
        return "hero cancel";
    }

    @Override
    public void onCommandCancelled(CommandSender executor) {
        if (!(executor instanceof Player)) {
            return;
        }
        pendingClassSelections.remove(executor);
        pendingClassCostStatus.remove(executor);
    }

    class StateA extends BasicInteractiveCommandState {

        public StateA() {
            super("hero prof", "hero profession");
            this.setArgumentRange(1, 1);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            if (!(executor instanceof Player)) {
                return false;
            }

            Properties props = Heroes.properties;
            Player player = (Player) executor;
            Hero hero = plugin.getHeroManager().getHero(player);
            HeroClass currentClass = hero.getSecondClass();
            HeroClass newClass = plugin.getClassManager().getClass(args[0]);

            if (newClass == null) {
                Messaging.send(player, "Class not found.");
                return false;
            }

            if (newClass.equals(currentClass) || newClass.equals(hero.getHeroClass())) {
                Messaging.send(player, "You are already set as this Class.");
                return false;
            }

            if (!newClass.isSecondary()) {
                Messaging.send(player, "That is not a secondary Class!");
                return false;
            }

            if (currentClass != null) {
                if (!hero.getHeroClass().isDefault() && hero.isMaster(currentClass) && currentClass.getParents().isEmpty() && props.lockAtHighestTier) {
                    Messaging.send(player, "You have mastered your class and can not choose a new one!");
                    return false;
                }

                if (!hero.isMaster(currentClass) && props.lockPathTillMaster) {
                    Messaging.send(player, "You must master this class before swapping to another.");
                    return false;
                }
            }

            if (!newClass.hasNoParents()) {
                for (HeroClass parentClass : newClass.getStrongParents()) {
                    if (!hero.isMaster(parentClass)) {
                        Messaging.send(player, "$1 requires you to master: $2", newClass.getName(), newClass.getStrongParents().toString().replace("[", "").replace("]", ""));
                        return false;
                    }
                }
                boolean masteredOne = false;
                for (HeroClass parentClass : newClass.getWeakParents()) {
                    if (hero.isMaster(parentClass)) {
                        masteredOne = true;
                        break;
                    }
                }

                if (!masteredOne && !newClass.getWeakParents().isEmpty()) {
                    Messaging.send(player, "$1 requires you to master one of: $2", newClass.getName(), newClass.getWeakParents().toString().replace("[", "").replace("]", ""));
                    return false;
                }
            }

            if (!newClass.isDefault() && !CommandHandler.hasPermission(player, "heroes.classes." + newClass.getName().toLowerCase())) {
                Messaging.send(player, "You don't have permission for $1.", newClass.getName());
                return false;
            }

            double cost = newClass.getCost();
            if (currentClass == null && Heroes.properties.firstSwitchFree) {
                cost = 0;
            } else if (hero.getExperience(newClass) > 0) {
                cost = Heroes.properties.oldProfSwapCost;
            }

            boolean chargePlayer = true;
            if (hero.isMaster(newClass) && props.swapMasterFree) {
                chargePlayer = false;
            } else if (!props.iConomy || Heroes.econ == null || cost <= 0) {
                chargePlayer = false;
            }

            pendingClassCostStatus.put(player, chargePlayer);

            Messaging.send(executor, "You have chosen...");
            Messaging.send(executor, "$1: $2", newClass.getName(), newClass.getDescription().toLowerCase());
            String skills = newClass.getSkillNames().toString();
            skills = skills.substring(1, skills.length() - 1);
            Messaging.send(executor, "$1: $2", "Skills", skills);
            if (chargePlayer) {
                Messaging.send(executor, "$1: $2", "Fee", Heroes.econ.format(cost));
            }
            Messaging.send(executor, "Please §8/hero confirm §7 or §8/hero cancel §7this selection.");

            pendingClassSelections.put(player, newClass);
            return true;
        }

    }

    class StateB extends BasicInteractiveCommandState {

        public StateB() {
            super("hero confirm");
            this.setArgumentRange(0, 0);
        }

        @Override
        public boolean execute(CommandSender executor, String identifier, String[] args) {
            if (!(executor instanceof Player)) {
                return false;
            }

            Player player = (Player) executor;
            Hero hero = plugin.getHeroManager().getHero(player);
            HeroClass currentClass = hero.getSecondClass();
            HeroClass newClass = pendingClassSelections.get(player);
            Properties prop = Heroes.properties;

            ClassChangeEvent event = new ClassChangeEvent(hero, currentClass, newClass);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }

            if (prop.resetExpOnClassChange || prop.resetProfMasteryOnClassChange) {
                if (currentClass != null) {
                    if (!hero.isMaster(currentClass) || (prop.resetProfMasteryOnClassChange)) {
                        hero.setExperience(currentClass, 0);
                    }
                }
            }

            double cost = newClass.getCost();
            if (currentClass == null && Heroes.properties.firstSwitchFree) {
                cost = 0;
            } else if (hero.getExperience(newClass) > 0) {
                cost = Heroes.properties.oldProfSwapCost;
            }

            if (pendingClassCostStatus.get(player)) {
                if (Heroes.econ.has(player.getName(), cost)) {
                    Heroes.econ.withdrawPlayer(player.getName(), cost);
                    Messaging.send(hero.getPlayer(), "The Gods are pleased with your offering of $1.", Heroes.econ.format(cost));
                } else {
                    Messaging.send(hero.getPlayer(), "You're unable to meet the offering of $1 to become $2.", Heroes.econ.format(cost), newClass.getName());
                    return false;
                }
            }

            hero.changeHeroClass(newClass, true);
            Messaging.send(player, "Welcome to the path of the $1!", newClass.getName());

            plugin.getHeroManager().saveHero(hero);
            return true;
        }
    }
}
