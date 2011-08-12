package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.PoisonEffect;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.nijiko.coelho.iConomy.util.Messaging;

public class Antidote extends TargettedSkill {

    public Antidote(Heroes plugin) {
        super(plugin, "Antidote");
        setDescription("Cures your target of poisons");
        setUsage("/skill antidote [target]");
        setArgumentRange(0, 1);
        setIdentifiers(new String[]{"skill antidote"});
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("range", 10);
        return node;
    }
    
    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target instanceof Player) {
            Hero targetHero = getPlugin().getHeroManager().getHero((Player) target);
            boolean cured = false;
            for (Effect effect : targetHero.getEffects()) {
                if (effect instanceof PoisonEffect) {
                    cured = true;
                    targetHero.removeEffect(effect);
                }
            }
            if (!cured) {
                Messaging.send(player, "Your target is not poisoned!");
                return false;
            }
            broadcastExecuteText(hero, target);
            return true;
        }
        Messaging.send(player, "You must target a player!");
        return false;
    }

}
