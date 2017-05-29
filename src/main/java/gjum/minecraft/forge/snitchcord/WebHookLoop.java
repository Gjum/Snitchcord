package gjum.minecraft.forge.snitchcord;

import gjum.minecraft.forge.snitchcord.config.SnitchcordConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

class WebHookLoop extends Thread {
    private final LinkedList<byte[]> alertQueue = new LinkedList<>();

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

    public synchronized void pushAlertJson(String json) {
        alertQueue.add(json.getBytes(StandardCharsets.UTF_8));
        interrupt();
    }

    private synchronized byte[] popAlertJson() {
        return alertQueue.poll();
    }

    private void runLoop() throws IOException {
        byte[] json = popAlertJson();
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
