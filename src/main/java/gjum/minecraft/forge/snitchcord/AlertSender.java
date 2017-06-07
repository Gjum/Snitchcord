package gjum.minecraft.forge.snitchcord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gjum.minecraft.forge.snitchcord.SnitchcordMod;
import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;
import gjum.minecraft.forge.snitchcord.FmtTemplateToken;
import net.minecraft.util.math.BlockPos;

import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

public class AlertSender {

    private final WebHookLoop sender;

    public AlertSender(WebHookLoop sender) {
        this.sender = sender;
    }

    public void pushAlert(SnitchAlert alert, SnitchcordConfig config) throws Exception {
        if (config.alertTrackFilter != null && !alertMatchesFilter(alert, config.alertTrackFilter))
            return;

        if (config.alertIgnoreFilter != null && alertMatchesFilter(alert, config.alertIgnoreFilter))
            return;

        if (config.tracklistOn && !config.tracklist.contains(alert.playerName.toLowerCase()))
            return;

        if (config.ignorelistOn && config.ignorelist.contains(alert.playerName.toLowerCase()))
            return;

        final String json;
        try {
            json = formatAlert(alert, config.alertFormat);
        } catch (Exception e) {
            throw e;
        }
        sender.pushJson(json);
    }

    private boolean alertMatchesFilter(SnitchAlert alert, Pattern alertFilter) {
        return alertFilter.matcher(alert.rawMessage.getUnformattedText()).matches();
    }

    private String formatAlert(SnitchAlert alert, String fmt) throws Exception {
        try {
            List<FmtTemplateToken> parsedTokens = parseStage2(parseStage1(fmt));
            fmt = substituteTokens(parsedTokens, alert);
        } catch (Exception e) {
            throw e;
        }
        SnitchcordMod.logger.info(String.format("[SnitchCord] Parsed alert format: '%s'.", fmt));
        return fmt;
    }

    private List<FmtTemplateToken> parseStage1(String fmt) {
        List<FmtTemplateToken> basicTokens = new ArrayList<FmtTemplateToken>();
        boolean seenOpeningChar = false;
        boolean seenClosingChar = false;
        boolean inFmtKey = false;
        StringBuilder fmtKey = new StringBuilder();
        for (String ch : fmt.split("")) {
            FmtTemplateToken token = new FmtTemplateToken();
            if (
                    (!ch.equals(">") && seenClosingChar) ||
                    (ch.equals(">") && !seenClosingChar)) {
                inFmtKey = false;
                token.type = "FMTKEY";
                token.content = fmtKey.toString();
                basicTokens.add(token);
                fmtKey.setLength(0);
                if (!ch.equals(">")) {
                    token = new FmtTemplateToken();
                    token.type = "TEXT";
                    token.content = ch;
                    basicTokens.add(token);
                }
                seenOpeningChar = false;
                seenClosingChar = false;
                continue;
            }
            if (ch.equals("<")) {
                if (seenOpeningChar) {
                    token.type = "TEXT";
                    token.content = "<";
                    basicTokens.add(token);
                    seenOpeningChar = false;
                    inFmtKey = false;
                } else {
                    seenOpeningChar = true;
                    inFmtKey = true;
                }
            } else if (ch.equals(">")) {
                if (seenClosingChar) {
                    token.type = "TEXT";
                    token.content = ">";
                    basicTokens.add(token);
                    seenClosingChar = false;
                } else {
                    seenClosingChar = true;
                }
            } else {
                if (inFmtKey) {
                    fmtKey.append(ch);
                } else {
                    token.type = "TEXT";
                    token.content = ch;
                    basicTokens.add(token);
                }
                seenOpeningChar = false;
                seenClosingChar = false;
            }
        }
        return basicTokens;
    }

    private List<FmtTemplateToken> parseStage2(List<FmtTemplateToken> basicTokens) throws Exception {
        List<FmtTemplateToken> tokens = new ArrayList<FmtTemplateToken>();
        for (FmtTemplateToken token : basicTokens) {
            if (token.type.equals("TEXT") || !token.content.contains(":")) {
                tokens.add(token);
                continue;
            }
            boolean seenColon = false;
            boolean danglingColon = false;
            List<String> texts = new ArrayList<String>();
            StringBuilder text = new StringBuilder();
            for (String ch : token.content.split("")) {
                danglingColon = false;
                if (ch.equals(":")) {
                    if (seenColon) {
                        text.append(":");
                        seenColon = false;
                    } else {
                        seenColon = true;
                        danglingColon = true;
                    }
                } else {
                    if (seenColon) {
                        texts.add(text.toString());
                        text.setLength(0);
                    }
                    text.append(ch);
                    seenColon = false;
                }
            }
            texts.add(text.toString());
            text.setLength(0);
            if (danglingColon) {
                texts.add("");
            }
            if (texts.size() == 1) {
                token.content = text.toString();
                tokens.add(token);
            } else if (texts.size() == 3) {
                token.contentPrefix = texts.get(0);
                token.content = texts.get(1);
                token.contentPostfix = texts.get(2);
                tokens.add(token);
            } else if (texts.size() == 2) {
                SnitchcordMod.logger.error(String.format(
                        "[SnitchCord] Error: Incorrect alert format: Too few prefix/postfix separators in format key <%s>. " +
                        "You must use two, e.g. '<prefix:key:postfix>'.", token.content));
                throw new Exception();
            } else {
                SnitchcordMod.logger.error(String.format(
                        "[SnitchCord] Error: Incorrect alert format: Too many prefix/postfix separators in format key <%s>. " +
                        "If you'd like to use a regular ':' in a format key, type '::'.", token.content));
                throw new Exception();
            }
        }
        return tokens;
    }

    private String substituteTokens(List<FmtTemplateToken> tokens, SnitchAlert alert) throws Exception {
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

        String dateFormat = "HH:mm:ss";

        StringBuilder parsedFmt = new StringBuilder();
        for (FmtTemplateToken token : tokens) {
            if (token.type.equals("TEXT")) {
                parsedFmt.append(token.content);
                continue;
            }
            switch (token.content) {
                case "time":
                    parsedFmt.append(new SimpleDateFormat(dateFormat).format(new Date()));
                    break;
                case "timeUTC":
                    DateFormat df = new SimpleDateFormat(dateFormat);
                    df.setTimeZone(TimeZone.getTimeZone("UTC"));
                    parsedFmt.append(df.format(new Date()));
                    break;
                case "player":
                    parsedFmt.append(j.toJson(alert.playerName).replaceAll("^\"|\"$", ""));
                    break;
                case "snitch":
                    parsedFmt.append(j.toJson(alert.snitchName).replaceAll("^\"|\"$", ""));
                    break;

                case "longAction":
                    parsedFmt.append(alert.activityText);
                    break;
                case "shortAction":
                    parsedFmt.append(alert.activity.msg);
                    break;
                case "nonEnter":
                    parsedFmt.append(alert.activity != SnitchAlert.Activity.ENTER ? token.contentPrefix + alert.activity.msg + token.contentPostfix : "");
                    break;
                case "enter":
                    parsedFmt.append(alert.activity == SnitchAlert.Activity.ENTER ? token.contentPrefix + alert.activity.msg + token.contentPostfix : "");
                    break;
                case "login":
                    parsedFmt.append(alert.activity == SnitchAlert.Activity.LOGIN ? token.contentPrefix + alert.activity.msg + token.contentPostfix : "");
                    break;
                case "logout":
                    parsedFmt.append(alert.activity == SnitchAlert.Activity.LOGOUT ? token.contentPrefix + alert.activity.msg + token.contentPostfix : "");
                    break;

                case "world":
                    parsedFmt.append(niceWorld);
                    break;
                case "nonWorld":
                    parsedFmt.append("World".equals(niceWorld) ? "" : token.contentPrefix + niceWorld + token.contentPostfix);
                    break;

                case "coords":
                    parsedFmt.append(String.format("%d %d %d", pos.getX(), pos.getY(), pos.getZ()));
                    break;
                case "x":
                    parsedFmt.append(String.valueOf(pos.getX()));
                    break;
                case "y":
                    parsedFmt.append(String.valueOf(pos.getY()));
                    break;
                case "z":
                    parsedFmt.append(String.valueOf(pos.getZ()));
                    break;

                case "roundedCoords":
                    parsedFmt.append(String.format("%d %d %d", rPos.getX(), rPos.getY(), rPos.getZ()));
                    break;
                case "rx":
                    parsedFmt.append(String.valueOf(rPos.getX()));
                    break;
                case "ry":
                    parsedFmt.append(String.valueOf(rPos.getY()));
                    break;
                case "rz":
                    parsedFmt.append(String.valueOf(rPos.getZ()));
                    break;
                default:
                    SnitchcordMod.logger.error(String.format(
                            "[SnitchCord] Error: Incorrect alert format: Unrecognized format key <%s>. ", token.content));
                    throw new Exception();
            }
        }
        return parsedFmt.toString();
    }
}
