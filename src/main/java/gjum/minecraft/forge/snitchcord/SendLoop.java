package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

class SendLoop implements Runnable {
    private WebHookSender webHookSender;

    public SendLoop(WebHookSender webHookSender) {
        this.webHookSender = webHookSender;
    }

    @Override
    public void run() {
        while (true) {
            try {
                runLoop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void runLoop() throws IOException {
        byte[] json = webHookSender.popAlertJson();
        if (json == null) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
            }
            return; // jump back up to popAlertJson
        }

        if (SnitchcordConfig.instance.webhookUrl == null || SnitchcordConfig.instance.webhookUrl.length() <= 0)
            return;

        HttpURLConnection connection = (HttpURLConnection) new URL(SnitchcordConfig.instance.webhookUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.addRequestProperty("User-Agent", "Mozilla/4.76");
        connection.setRequestProperty("Content-Length", String.valueOf(json.length));
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        try (OutputStream os = connection.getOutputStream()) {
            os.write(json);
            os.flush();

            if (connection.getResponseCode() < 200 || 300 <= connection.getResponseCode()) {
                SnitchcordMod.logger.error(connection.getResponseCode() + ": " + connection.getResponseMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
