package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.effects.RootEffect;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillRoot extends TargettedSkill {

    public SkillRoot(Heroes plugin) {
        super(plugin, "Root");
        setDescription("Roots your target in place");
        setUsage("/skill root <target>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill root" });
        
        setTypes(SkillType.MOVEMENT, SkillType.DEBUFF, SkillType.SILENCABLE, SkillType.EARTH, SkillType.HARMFUL);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 5000);
        return node;
    }


    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();

        long duration = getSetting(hero.getHeroClass(), Setting.DURATION.node(), 5000);
        RootEffect rEffect = new RootEffect(this, duration);
        
        if (target instanceof Player) {
            plugin.getHeroManager().getHero((Player) target).addEffect(rEffect);
        } else if (target instanceof Creature) {
            plugin.getHeroManager().addCreatureEffect((Creature) target, rEffect);
        } else {
            Messaging.send(player, "Invalid target!");
            return false;
        }
        
        broadcastExecuteText(hero, target);
        return true;
    }
}
