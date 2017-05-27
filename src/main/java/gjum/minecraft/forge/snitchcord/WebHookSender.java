package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public class WebHookSender {

    private final Thread thread;
    private final LinkedList<byte[]> alertQueue = new LinkedList<>();

    public WebHookSender() {
        thread = new Thread(new SendLoop(this));
        thread.start();
    }

    public void pushAlert(SnitchAlert alert, SnitchcordConfig config) {
        String msg = alert.playerName;

        if (alert.activity != SnitchAlert.Activity.ENTER) {
            msg += " " + alert.activity;
        }

        if (config.sendCoords) {
            BlockPos pos = alert.pos;
            if (config.roundCoords) {
                msg += " around ";
                pos = new BlockPos(
                        (pos.getX() + 5) / 10 * 10,
                        (pos.getY() + 5) / 10 * 10,
                        (pos.getZ() + 5) / 10 * 10);
            } else {
                msg += " at ";
            }
            msg += String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ());
        }

        if (config.sendName) {
            msg += " " + alert.snitchName;
        }

        final byte[] json = ("{\"content\":\""
                + msg
                + "\"}"
        ).getBytes(StandardCharsets.UTF_8);

        pushAlertJson(json);
        thread.interrupt();
    }

    private synchronized void pushAlertJson(byte[] json) {
        alertQueue.add(json);
    }

    public synchronized byte[] popAlertJson() {
        return alertQueue.poll();
    }

}
