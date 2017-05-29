package gjum.minecraft.forge.snitchcord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import net.minecraft.util.math.BlockPos;

import java.util.regex.Pattern;

public class AlertSender {

    private final WebHookLoop sender;

    public AlertSender(WebHookLoop sender) {
        this.sender = sender;
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

        final String json = formatAlert(alert, config.alertFormat);

        sender.pushJson(json);
    }

    private boolean alertMatchesFilter(SnitchAlert alert, Pattern alertFilter) {
        return alertFilter.matcher(alert.rawMessage.getUnformattedText()).matches();
    }

    private String formatAlert(SnitchAlert alert, String fmt) {
        BlockPos pos = alert.pos;
        BlockPos rPos = new BlockPos(
                (pos.getX() + 5) / 10 * 10,
                (pos.getY() + 5) / 10 * 10,
                (pos.getZ() + 5) / 10 * 10);

        Gson j = new GsonBuilder().create();

        String niceWorld = alert.world;
        if ("world".equals(niceWorld)) niceWorld = "World";
        else if ("world_nether".equals(niceWorld)) niceWorld = "Nether";
        else if ("world_the_end".equals(niceWorld)) niceWorld = "The End";

        return fmt
                .replaceAll("<player>", j.toJson(alert.playerName).replaceAll("^\"|\"$", ""))
                .replaceAll("<snitch>", j.toJson(alert.snitchName).replaceAll("^\"|\"$", ""))

                .replaceAll("<longAction>", alert.activityText)
                .replaceAll("<shortAction>", alert.activity.msg)
                .replaceAll("<nonEnter>", alert.activity != SnitchAlert.Activity.ENTER ? alert.activity.msg : "")
                .replaceAll("<enter>", alert.activity == SnitchAlert.Activity.ENTER ? alert.activity.msg : "")
                .replaceAll("<login>", alert.activity == SnitchAlert.Activity.LOGIN ? alert.activity.msg : "")
                .replaceAll("<logout>", alert.activity == SnitchAlert.Activity.LOGOUT ? alert.activity.msg : "")

                .replaceAll("<world>", niceWorld)
                .replaceAll("<nonWorld>", "World".equals(niceWorld) ? "" : niceWorld)

                .replaceAll("<coords>", String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()))
                .replaceAll("<x>", String.valueOf(pos.getX()))
                .replaceAll("<y>", String.valueOf(pos.getY()))
                .replaceAll("<z>", String.valueOf(pos.getZ()))

                .replaceAll("<roundedCoords>", String.format("%d %d %d", rPos.getX(), rPos.getY(), rPos.getZ()))
                .replaceAll("<rx>", String.valueOf(rPos.getX()))
                .replaceAll("<ry>", String.valueOf(rPos.getY()))
                .replaceAll("<rz>", String.valueOf(rPos.getZ()))

                .replaceAll(" {2,}", " ") // collapse duplicate spaces
//                .replaceAll(" +([\\[ ~])", "$1") // remove unneeded spaces before stuff
//                .replaceAll("([\\[ ~]) +", "$1") // remove unneeded spaces after stuff
                ;
    }
}
