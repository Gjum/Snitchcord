package gjum.minecraft.forge.snitchcord;

import com.google.gson.GsonBuilder;
import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.util.math.BlockPos;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class WebHookSender {

    private final Thread thread;
    private final LinkedList<byte[]> alertQueue = new LinkedList<>();

    public WebHookSender() {
        thread = new Thread(new SendLoop(this));
        thread.start();
    }

    public void pushAlert(SnitchAlert alert, SnitchcordConfig config) {
        if (config.alertTrackFilter != null && !alertMatchesFilter(alert, config.alertTrackFilter))
            return;

        if (config.alertIgnoreFilter != null && alertMatchesFilter(alert, config.alertIgnoreFilter))
            return;

        if (config.tracklistOn && !config.tracklist.contains(alert.playerName.toLowerCase()))
            return;

        if (config.ignorelistOn && config.ignorelist.contains(alert.playerName.toLowerCase()))
            return;

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

        final String json = ("{\"content\":"
                + new GsonBuilder().create().toJson(msg)
                + "}"
        );

        pushAlertJson(json.getBytes(StandardCharsets.UTF_8));
        thread.interrupt();
    }

    private boolean alertMatchesFilter(SnitchAlert alert, Pattern alertFilter) {
        return alertFilter.matcher(alert.rawMessage.getUnformattedText()).matches();
    }

    private synchronized void pushAlertJson(byte[] json) {
        alertQueue.add(json);
    }

    public synchronized byte[] popAlertJson() {
        return alertQueue.poll();
    }

}
