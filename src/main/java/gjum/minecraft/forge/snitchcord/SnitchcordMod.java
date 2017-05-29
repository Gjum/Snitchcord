package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(
        modid = SnitchcordMod.MOD_ID,
        name = SnitchcordMod.MOD_NAME,
        version = SnitchcordMod.VERSION,
        guiFactory = "gjum.minecraft.forge.snitchcord.config.ConfigGuiFactory",
        clientSideOnly = true)
public class SnitchcordMod {

    public static final String MOD_ID = "snitchcord";
    public static final String MOD_NAME = "Snitchcord";
    public static final String VERSION = "@VERSION@";
    public static final String BUILD_TIME = "@BUILD_TIME@";

    public static Logger logger;
    private long lastCrash = 0;

    private WebHookLoop webHookLoop;
    private AlertSender alertSender;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        File configFile = event.getSuggestedConfigurationFile();
        logger.info("Loading config from " + configFile);
        SnitchcordConfig.instance.load(configFile);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info(String.format("%s version %s built at %s", MOD_NAME, VERSION, BUILD_TIME));

        MinecraftForge.EVENT_BUS.register(this);
        new KeyHandler();
        webHookLoop = new WebHookLoop();
        webHookLoop.run();
        alertSender = new AlertSender(webHookLoop);
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        try {
            if (!SnitchcordConfig.instance.enabled) return;

            SnitchAlert alert = SnitchAlert.fromChat(event.getMessage());
            if (alert != null) {
                alertSender.pushAlert(alert, SnitchcordConfig.instance);
            }
        } catch (Exception e) {
            if (lastCrash < System.currentTimeMillis() - 5000) {
                lastCrash = System.currentTimeMillis();
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        try {
            if (event.getModID().equals(SnitchcordMod.MOD_ID)) {
                SnitchcordConfig.instance.afterGuiSave();
            }
        } catch (Exception e) {
            if (lastCrash < System.currentTimeMillis() - 5000) {
                lastCrash = System.currentTimeMillis();
                e.printStackTrace();
            }
        }
    }
}
