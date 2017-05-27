package gjum.minecraft.forge.snitchcord;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnitchAlert {
    public static final Pattern snitchAlertPattern = Pattern.compile("\\s*\\*\\s*([^\\s]*)\\s\\b(entered snitch at|logged out in snitch at|logged in to snitch at)\\b\\s*([^\\s]*)\\s\\[([^\\s]*)\\s([-\\d]*)\\s([-\\d]*)\\s([-\\d]*)\\]");

    public enum Activity {
        ENTER("enter"), LOGIN("login"), LOGOUT("logout"), UNKNOWN("UNKNOWN");

        public final String msg;

        Activity(String msg) {
            this.msg = msg;
        }

        public static Activity fromMatch(String activityText) {
            return "entered snitch at".equals(activityText) ? ENTER :
                    "logged in to snitch at".equals(activityText) ? LOGIN :
                            "logged out in snitch at".equals(activityText) ? LOGOUT :
                                    UNKNOWN;
        }
    }

    public final String playerName;
    public final BlockPos pos;
    public final String snitchName;
    public final String activityText;
    public final Activity activity;
    public final String world;
    public final ITextComponent rawMessage;

    public SnitchAlert(String playerName, int x, int y, int z, String activityText, String snitchName, String world, ITextComponent rawMessage) {
        this.playerName = playerName;
        this.pos = new BlockPos(x, y, z);
        this.activityText = activityText;
        this.snitchName = snitchName;
        this.world = world;
        this.rawMessage = rawMessage;

        activity = Activity.fromMatch(activityText);
    }

    public static SnitchAlert fromChat(ITextComponent chatTextComponent) {
        Matcher matcher = snitchAlertPattern.matcher(chatTextComponent.getUnformattedText());
        if (!matcher.matches()) {
            return null;
        }

        String playerName = matcher.group(1);
        String activity = matcher.group(2);
        String snitchName = matcher.group(3);
        String worldName = matcher.group(4);
        int x = Integer.parseInt(matcher.group(5));
        int y = Integer.parseInt(matcher.group(6));
        int z = Integer.parseInt(matcher.group(7));
        return new SnitchAlert(playerName, x, y, z, activity, snitchName, worldName, chatTextComponent);
    }

}
