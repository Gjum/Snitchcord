package gjum.minecraft.forge.snitchcord;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.HoverEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnitchAlert {
    public static final Pattern snitchAlertPattern = Pattern.compile("\\s*\\*\\s*([^\\s]*)\\s\\b(entered snitch at|logged out in snitch at|logged in to snitch at)\\b\\s*([^\\s]*)\\s\\[([^\\s]*)\\s([-\\d]*)\\s([-\\d]*)\\s([-\\d]*)\\]");
    public static final Pattern snitchAlertHoverPattern = Pattern.compile("^(?i)\\s*Location:\\s*\\[(\\S+?) (-?[0-9]+) (-?[0-9]+) (-?[0-9]+)\\]\\s*Group:\\s*(\\S+?)\\s*Type:\\s*(Entry|Logging)\\s*(?:Cull:\\s*([0-9]+\\.[0-9]+)h?)?\\s*(?:Previous name:\\s*(\\S+?))?\\s*(?:Name:\\s*(\\S+?))?\\s*");

    public enum Activity {
        ENTER("Enter"), LOGIN("Login"), LOGOUT("Logout"), UNKNOWN("UNKNOWN");

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
    public final String group;
    public final ITextComponent rawMessage;

    public SnitchAlert(String playerName, int x, int y, int z, String activityText, String snitchName, String world, String group, ITextComponent rawMessage) {
        this.playerName = playerName;
        this.pos = new BlockPos(x, y, z);
        this.activityText = activityText;
        this.snitchName = snitchName;
        this.world = world;
        this.group = group;
        this.rawMessage = rawMessage;

        activity = Activity.fromMatch(activityText);
    }

    public static SnitchAlert fromChat(ITextComponent rawMessage) {
        Matcher matcher = snitchAlertPattern.matcher(stripMinecraftFormattingCodes(rawMessage.getUnformattedText()));
        if (!matcher.matches()) {
            return null;
        }

        String group;
        HoverEvent hover = getHoverEvent(rawMessage);
        if (hover == null) {
            group = null;
            SnitchcordMod.logger.error(
                    "[SnitchCord] Error: No hover in snitch alert. The server needs JukeAlert >= v1.6.1.");
        } else {
            String hoverText = hover.getValue().getUnformattedComponentText().replace("\n", " ");
            Matcher hoverMatcher = snitchAlertHoverPattern.matcher(hoverText);
            if (!hoverMatcher.matches()) {
                group = null;
                SnitchcordMod.logger.error(
                        "[SnitchCord] Error: Snitch alert hover regex failed to match hover in snitch alert.");
            } else {
                group = hoverMatcher.group(5);
            }
        }

        String playerName = matcher.group(1);
        String activity = matcher.group(2);
        String snitchName = matcher.group(3);
        String worldName = matcher.group(4);
        int x = Integer.parseInt(matcher.group(5));
        int y = Integer.parseInt(matcher.group(6));
        int z = Integer.parseInt(matcher.group(7));
        return new SnitchAlert(playerName, x, y, z, activity, snitchName, worldName, group, rawMessage);
    }

    private static String stripMinecraftFormattingCodes(String str) {
        return str.replaceAll("(?i)\\u00A7[a-z0-9]", "");
    }

    private static HoverEvent getHoverEvent(ITextComponent rawMessage) {
        List<ITextComponent> siblings = rawMessage.getSiblings();
        if (siblings.size() <= 0) {
            return null;
        }
        ITextComponent hoverComponent = siblings.get(0);
        HoverEvent hover = hoverComponent.getStyle().getHoverEvent();
        if (hover == null) {
            return null;
        }
        return hover;
    }
}
