package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.ConfigGui;
import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    private final KeyBinding toggleEnabled = new KeyBinding("Toggle sending snitches to discord", Keyboard.KEY_NONE, SnitchcordMod.MOD_NAME);
    private final KeyBinding openMenu = new KeyBinding("Open " + SnitchcordMod.MOD_NAME + " menu", Keyboard.KEY_NONE, SnitchcordMod.MOD_NAME);

    private long lastCrash = 0;

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(toggleEnabled);
        ClientRegistry.registerKeyBinding(openMenu);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        try {
            if (toggleEnabled.isPressed()) {
                SnitchcordConfig.instance.setEnabled(!SnitchcordConfig.instance.enabled);
            }
            if (openMenu.isPressed()) {
                Minecraft.getMinecraft().displayGuiScreen(new ConfigGui(null));
            }
        } catch (Exception e) {
            if (lastCrash < System.currentTimeMillis() - 5000) {
                lastCrash = System.currentTimeMillis();
                e.printStackTrace();
            }
        }
    }
}
