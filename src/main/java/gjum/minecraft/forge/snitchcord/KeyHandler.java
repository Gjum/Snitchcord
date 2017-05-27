package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    private final KeyBinding toggleEnabled = new KeyBinding("Toggle sending snitches to discord", Keyboard.KEY_NONE, SnitchcordMod.MOD_NAME);

    private long lastCrash = 0;

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(toggleEnabled);

        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        try {
            if (toggleEnabled.isPressed()) {
                SnitchcordConfig.instance.setEnabled(!SnitchcordConfig.instance.enabled);
            }
        } catch (Exception e) {
            if (lastCrash < System.currentTimeMillis() - 5000) {
                lastCrash = System.currentTimeMillis();
                e.printStackTrace();
            }
        }
    }
}
