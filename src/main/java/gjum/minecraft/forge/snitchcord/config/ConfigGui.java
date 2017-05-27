package gjum.minecraft.forge.snitchcord.config;

import gjum.minecraft.forge.snitchcord.SnitchcordMod;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

import java.util.List;

public class ConfigGui extends GuiConfig {

    public ConfigGui(GuiScreen parent) {
        super(parent, getConfigElements(),
                SnitchcordMod.MOD_ID, false, false, SnitchcordMod.MOD_NAME + " config");
    }

    private static List<IConfigElement> getConfigElements() {
        Configuration config = SnitchcordConfig.instance.config;
        return new ConfigElement(config.getCategory(SnitchcordConfig.CATEGORY_MAIN)).getChildElements();
    }

}
