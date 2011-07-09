package com.herocraftonline.dev.heroes.skill.skills;

import net.minecraft.server.EntityHuman;
import net.minecraft.server.Packet20NamedEntitySpawn;
import net.minecraft.server.Packet29DestroyEntity;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveEffectSkill;

public class SkillSmoke extends ActiveEffectSkill {

    public SkillSmoke(Heroes plugin) {
        super(plugin);
        setName("Smoke");
        setDescription("You completely disappear from view");
        setUsage("/skill smoke");
        setMinArgs(0);
        setMaxArgs(0);
        getIdentifiers().add("skill smoke");
        getNotes().add("Note: Taking damage removes the effect");

        registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        CraftPlayer craftPlayer = (CraftPlayer) hero.getPlayer();
        // Tell all the logged in Clients to Destroy the Entity - Appears Invisible.
        final Player[] players = plugin.getServer().getOnlinePlayers();
        for (Player player : players) {
            CraftPlayer hostilePlayer = (CraftPlayer) player;
            hostilePlayer.getHandle().netServerHandler.sendPacket(new Packet29DestroyEntity(craftPlayer.getEntityId()));
        }
        applyEffect(hero);
        // Kinda ruins the stealthy part, but can be set to null to disable it
        notifyNearbyPlayers(craftPlayer.getLocation(), getUseText(), craftPlayer.getName(), getName());
        return true;
    }

    public class SkillEntityListener extends EntityListener {
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (event.getEntity() instanceof Player) {
                Player player = (Player) event.getEntity();
                Hero hero = plugin.getHeroManager().getHero(player);
                if (hero.getEffects().hasEffect(getName())) {
                    hero.getEffects().expireEffect(getName());
                }
            }
        }
    }

    public class SkillPlayerListener extends PlayerListener {
        @Override
        public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() != Action.PHYSICAL) {
                Player player = event.getPlayer();
                Hero hero = plugin.getHeroManager().getHero(player);
                if (hero.getEffects().hasEffect(getName())) {
                    hero.getEffects().expireEffect(getName());
                }
            }
        }
    }

    @Override
    public void onExpire(Hero hero) {
        Player player = hero.getPlayer();
        EntityHuman entity = ((CraftPlayer) player).getHandle();
        final Player[] players = plugin.getServer().getOnlinePlayers();
        for (Player p : players) {
            if (p.getName().equalsIgnoreCase(player.getName())) {
                continue;
            }
            CraftPlayer hostilePlayer = (CraftPlayer) p;
            hostilePlayer.getHandle().netServerHandler.sendPacket(new Packet20NamedEntitySpawn(entity));
        }
        super.onExpire(hero);
    }
}
