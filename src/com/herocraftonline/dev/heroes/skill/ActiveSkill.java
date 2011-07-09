package com.herocraftonline.dev.heroes.skill;

import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;

/**
 * A skill that performs an action in direct response to a user command. All skill identifiers <i>must</i>
 * begin with <i>skill</i>, e.g. "skill fireball", in order to be recognized. ActiveSkills define four default settings:
 * mana, cooldown, experience and usage text. Mana is deducted and a cooldown is activated when the
 * {@link #use(Hero, String[])} method returns <code>true</code>. The {@link #execute(CommandSender, String[])}
 * automatically handles class, level, mana and cooldown checks on a player attempting to use a skill and should not be
 * overridden. If all of these checks pass, the <code>use</code> method is called, which should contain the heart of the
 * skill's behavior that is unique to each skill.
 */
public abstract class ActiveSkill extends Skill {

    /**
     * Identifier used to store mana usage setting
     */
    public static final String SETTING_MANA = "mana";

    /**
     * Identifier used to store cooldown duration setting
     */
    public static final String SETTING_COOLDOWN = "cooldown";

    /**
     * Identifier used to store experience award setting
     */
    public static final String SETTING_EXP = "exp";

    /**
     * Identifier used to store usage text setting
     */
    public static final String SETTING_USETEXT = "use-text";

    private String useText;
    private boolean awardExpOnCast = true;

    /**
     * When defining your own constructor, be sure to assign the name, description, usage, argument bounds and
     * identifier fields as defined in {@link BaseCommand}. Remember that each identifier must begin with <i>skill</i>.
     * 
     * @param plugin
     *            the active Heroes instance
     */
    public ActiveSkill(Heroes plugin) {
        super(plugin);
    }

    /**
     * Returns the text to be displayed when the skill is successfully used. This text is pulled from the
     * {@link #SETTING_USETEXT} entry in the skill's configuration during initialization.
     * 
     * @return the usage text
     */
    public String getUseText() {
        return useText;
    }

    /**
     * Changes the stored usage text. This can be used to override the message found in the skill's configuration.
     * 
     * @param useText
     *            the new usage text
     */
    public void setUseText(String useText) {
        this.useText = useText;
    }

    /**
     * Loads and stores the skill's usage text from the configuration. By default, this text is "%hero% used %skill%!"
     * where %hero% and %skill% are replaced with the Hero's and skill's names, respectively.
     */
    @Override
    public void init() {
        String useText = getSetting(null, SETTING_USETEXT, "%hero% used %skill%!");
        useText = useText.replace("%hero%", "$1").replace("%skill%", "$2");
        setUseText(useText);
    }

    /**
     * Creates and returns a <code>ConfigurationNode</code> containing the default usage text. When using additional
     * configuration settings in your skills, be sure to override this method to define them with defaults.
     * 
     * @return a default configuration
     */
    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = Configuration.getEmptyNode();
        node.setProperty(SETTING_USETEXT, "%hero% used %skill%!");
        return node;
    }

    /**
     * Called whenever a command with an identifier registered to this skill is used. This implementation performs all
     * necessary class, level, mana and cooldown checks. This method should <i>not</i> be overridden unless you really
     * know what you're doing. If all checks pass, this method calls {@link #use(Hero, String[])}. If <code>use</code>
     * returns <code>true</code>, this method automatically deducts mana, awards experience and sets a cooldown.
     * 
     * @param sender
     *            the <code>CommandSender</code> issuing the command
     * @param args
     *            the arguments provided with the command
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            String name = this.getName();
            Player player = (Player) sender;
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero == null) {
                Messaging.send(player, "You are not a hero.");
                return;
            }
            HeroClass heroClass = hero.getHeroClass();
            if (!heroClass.hasSkill(name) && !heroClass.hasSkill("*")) {
                Messaging.send(player, "$1s cannot use $2.", heroClass.getName(), name);
                return;
            }
            int level = getSetting(heroClass, SETTING_LEVEL, 1);
            if (hero.getLevel() < level) {
                Messaging.send(player, "You must be level $1 to use $2.", String.valueOf(level), name);
                return;
            }
            int manaCost = getSetting(heroClass, SETTING_MANA, 0);
            if (manaCost > hero.getMana()) {
                Messaging.send(player, "Not enough mana!");
                return;
            }
            Map<String, Long> cooldowns = hero.getCooldowns();
            long time = System.currentTimeMillis();
            int cooldown = getSetting(heroClass, SETTING_COOLDOWN, 0);
            if (cooldown > 0) {
                Long timeUsed = cooldowns.get(name);
                if (timeUsed != null) {
                    if (time < timeUsed + cooldown) {
                        long remaining = timeUsed + cooldown - time;
                        Messaging.send(hero.getPlayer(), "Sorry, $1 still has $2 seconds left on cooldown!", name, Long.toString(remaining / 1000));
                        return;
                    }
                }
            }
            if (use(hero, args)) {
                if (cooldown > 0) {
                    cooldowns.put(name, time);
                }

                if (this.awardExpOnCast) {
                    this.awardExp(hero);
                }

                hero.setMana(hero.getMana() - manaCost);
                if (hero.isVerbose() && manaCost > 0) {
                    Messaging.send(hero.getPlayer(), Messaging.createManaBar(hero.getMana()));
                }
            }
        }
    }

    private void awardExp(Hero hero) {
        HeroClass heroClass = hero.getHeroClass();
        if (heroClass.getExperienceSources().contains(ExperienceType.SKILL)) {
            hero.gainExp(this.getSetting(heroClass, SETTING_EXP, 0), ExperienceType.SKILL);
        }
    }

    /**
     * The heart of any ActiveSkill, this method defines what actually happens when the skill is used. See
     * {@link #execute(CommandSender, String[])} for a brief explanation of the execution process.
     * 
     * @param hero
     *            the {@link Hero} using the skill
     * @param args
     *            the arguments provided with the command
     * @return <code>true</code> if the skill executed properly, <code>false</code> otherwise
     */
    public abstract boolean use(Hero hero, String[] args);

}
