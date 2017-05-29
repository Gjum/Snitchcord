package gjum.minecraft.forge.snitchcord.config;


import gjum.minecraft.forge.snitchcord.SnitchcordMod;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SnitchcordConfig {
    public static final String CATEGORY_MAIN = "Main";

    public static final SnitchcordConfig instance = new SnitchcordConfig();

    public Configuration config;

    public boolean enabled;
    public boolean sendCoords, roundCoords, sendName;
    public String webhookUrl;
    private Property propEnabled, propSendCoords, propRoundCoords, propSendName, propWebhookUrl;

    public Pattern alertTrackFilter, alertIgnoreFilter;
    private Property propAlertTrackFilter, propAlertIgnoreFilter;

    public boolean ignorelistOn;
    public boolean tracklistOn;
    public HashSet<String> ignorelist;
    public HashSet<String> tracklist;
    private Property propIgnorelistOn, propTracklistOn, propIgnorelist, propTracklist;

    private SnitchcordConfig() {
    }

    public void load(File configFile) {
        config = new Configuration(configFile, SnitchcordMod.VERSION);

        syncProperties();
        final ConfigCategory categoryMain = config.getCategory(CATEGORY_MAIN);
        final Set<String> confKeys = categoryMain.keySet();

        config.load();

        if (!config.getDefinedConfigVersion().equals(config.getLoadedConfigVersion())) {
            // clear config from old entries
            // otherwise they would clutter the gui
            final Set<String> unusedConfKeys = categoryMain.keySet();
            unusedConfKeys.removeAll(confKeys);
            for (String confKey : unusedConfKeys) {
                categoryMain.remove(confKey);
            }
        }

        syncProperties();
        syncValues();
    }

    public void afterGuiSave() {
        syncProperties();
        syncValues();
    }

    public void setEnabled(boolean enabled) {
        syncProperties();
        propEnabled.set(enabled);
        syncValues();
    }

    /**
     * no idea why this has to be called so often, ideally the prop* would stay the same,
     * but it looks like they get disassociated from the config sometimes and setting them no longer has any effect
     */
    private void syncProperties() {
        propEnabled = config.get(CATEGORY_MAIN, "enabled", true, "Enable/disable snitch sending");
        propSendCoords = config.get(CATEGORY_MAIN, "send coordinates", true, "Send the coords (exact or rounded)");
        propRoundCoords = config.get(CATEGORY_MAIN, "send rounded coordinates", true, "Round the snitch coordinates to the closest multiple of 10");
        propSendName = config.get(CATEGORY_MAIN, "send snitch name", true, "Send the name of the snitch");
        propWebhookUrl = config.get(CATEGORY_MAIN, "webhook url", "", "Get this from the discord channel settings");

        propAlertIgnoreFilter = config.get(CATEGORY_MAIN, "alert filter: ignore", "", "Regular expression, if an alert matches it will not be sent");
        propAlertTrackFilter = config.get(CATEGORY_MAIN, "alert filter: track", "", "Regular expression, if it's set, only matching alerts  will be sent");

        propIgnorelistOn = config.get(CATEGORY_MAIN, "enable ignorelist",
                true, "Ignore players in ignore list." +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
        propTracklistOn = config.get(CATEGORY_MAIN, "enable tracklist",
                false, "Only show players in track list." +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");

        propIgnorelist = config.get(CATEGORY_MAIN, "ignored players",
                new String[]{}, "If enabled, these players will NOT show up, even if they're also in the track list" +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
        propTracklist = config.get(CATEGORY_MAIN, "tracked players",
                new String[]{}, "If enabled, ONLY these players will show up (unless they're also in the ignore list)" +
                        "\nThis applies to snitch/logout overlays and the proximity ping sound.");
    }

    /**
     * called every time a prop is changed, to apply the new values to the fields and to save the values to the config file
     */
    private void syncValues() {
        enabled = propEnabled.getBoolean();
        sendCoords = propSendCoords.getBoolean();
        roundCoords = propRoundCoords.getBoolean();
        sendName = propSendName.getBoolean();
        webhookUrl = propWebhookUrl.getString();

        try {
            alertIgnoreFilter = null;
            if (propAlertIgnoreFilter.getString().length() > 0)
                alertIgnoreFilter = Pattern.compile(propAlertIgnoreFilter.getString());
        } catch (PatternSyntaxException e) {
            SnitchcordMod.logger.error("Error in filter for ignored alerts: " + e.getMessage());
        }

        try {
            alertTrackFilter = null;
            if (propAlertIgnoreFilter.getString().length() > 0)
                alertTrackFilter = Pattern.compile(propAlertTrackFilter.getString());
        } catch (PatternSyntaxException e) {
            SnitchcordMod.logger.error("Error in filter for tracked alerts: " + e.getMessage());
        }

        ignorelistOn = propIgnorelistOn.getBoolean();
        tracklistOn = propTracklistOn.getBoolean();

        String[] ignorelistArr = propIgnorelist.getStringList();
        ignorelist = new HashSet<>(ignorelistArr.length);
        for (String s : ignorelistArr) {
            ignorelist.add(s.toLowerCase());
        }

        String[] tracklistArr = propTracklist.getStringList();
        tracklist = new HashSet<>(tracklistArr.length);
        for (String s : tracklistArr) {
            tracklist.add(s.toLowerCase());
        }

        if (config.hasChanged()) {
            config.save();
            syncProperties();
            SnitchcordMod.logger.info("Saved " + SnitchcordMod.MOD_NAME + " config.");
        }
    }

}
