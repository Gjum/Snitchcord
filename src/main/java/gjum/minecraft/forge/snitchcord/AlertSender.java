package gjum.minecraft.forge.snitchcord;

import com.google.gson.GsonBuilder;
import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Pattern;

public class AlertSender {

    private final WebHookLoop sender;

    public AlertSender() {
        sender = new WebHookLoop();
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

        sender.pushAlertJson(json);
    }

    private boolean alertMatchesFilter(SnitchAlert alert, Pattern alertFilter) {
        return alertFilter.matcher(alert.rawMessage.getUnformattedText()).matches();
    }
}
