package gjum.minecraft.forge.snitchcord.config;


import gjum.minecraft.forge.snitchcord.SnitchcordMod;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.Set;

public class SnitchcordConfig {
    public static final String CATEGORY_MAIN = "Main";

    public static final SnitchcordConfig instance = new SnitchcordConfig();

    public Configuration config;

    public boolean enabled;
    public boolean sendCoords, roundCoords, sendName;
    public String webhookUrl;

    private Property propEnabled, propSendCoords, propRoundCoords, propSendName, propWebhookUrl;

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

        if (config.hasChanged()) {
            config.save();
            syncProperties();
            SnitchcordMod.logger.info("Saved " + SnitchcordMod.MOD_NAME + " config.");
        }
    }

}
