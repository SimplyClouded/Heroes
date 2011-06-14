package com.herocraftonline.dev.heroes.command.skill.skills;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.inventory.ItemStack;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.command.skill.ActiveEffectSkill;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.persistence.HeroEffects;

public class SkillSuperheat extends ActiveEffectSkill{

    private BlockListener playerListener = new SkillPlayerListener();

    public SkillSuperheat(Heroes plugin) {
        super(plugin);
        name = "Superheat";
        description = "Your pickaxe becomes superheated";
        usage = "/skill superheat";
        minArgs = 0;
        maxArgs = 0;
        identifiers.add("skill superheat");

        registerEvent(Type.PLAYER_INTERACT, playerListener, Priority.Normal);
    }


    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        String playerName = player.getName();
        applyEffect(hero);

        notifyNearbyPlayers(player.getLocation(), useText, playerName, name);
        return true;
    }

    public class SkillPlayerListener extends BlockListener {

        @Override
        public void onBlockBreak(BlockBreakEvent event) {
            Block block = event.getBlock();
            Player player = event.getPlayer();
            Hero hero = plugin.getHeroManager().getHero(player);
            HeroEffects effects = hero.getEffects();
            if (effects.hasEffect(name)) {
                switch(block.getType()){
                    case IRON_ORE:
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(),new ItemStack(Material.IRON_INGOT));
                        break;
                    case GOLD_ORE:
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(),new ItemStack(Material.GOLD_INGOT));
                        break;
                    case SAND:
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(),new ItemStack(Material.GLASS));
                        break;
                    case COBBLESTONE:
                        event.setCancelled(true);
                        block.setType(Material.AIR);
                        block.getWorld().dropItem(block.getLocation(),new ItemStack(Material.STONE));
                        break;
                }
            }
        }
    }

}