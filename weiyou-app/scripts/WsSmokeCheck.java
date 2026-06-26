import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WsSmokeCheck {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java WsSmokeCheck.java <ws-url> <conversationId>");
            System.exit(1);
        }

        String wsUrl = args[0];
        String conversationId = args[1];
        String requestId = "smoke-ws-" + System.currentTimeMillis();

        CountDownLatch connectAckLatch = new CountDownLatch(1);
        CountDownLatch messageAckLatch = new CountDownLatch(1);
        CountDownLatch messageReceiveLatch = new CountDownLatch(1);
        StringBuilder received = new StringBuilder();

        Listener listener = new Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                webSocket.request(1);
                Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                received.append(data);
                if (last) {
                    String payload = received.toString();
                    received.setLength(0);
                    if (payload.contains("\"CONNECT_ACK\"")) {
                        connectAckLatch.countDown();
                    }
                    if (payload.contains("\"MESSAGE_ACK\"")) {
                        messageAckLatch.countDown();
                    }
                    if (payload.contains("\"MESSAGE_RECEIVE\"")) {
                        messageReceiveLatch.countDown();
                    }
                }
                webSocket.request(1);
                return CompletableFuture.completedFuture(null);
            }
        };

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        WebSocket webSocket = client.newWebSocketBuilder().buildAsync(URI.create(wsUrl), listener).join();

        if (!connectAckLatch.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("CONNECT_ACK not received");
        }

        String messagePayload = "{" +
                "\"event\":\"MESSAGE_SEND\"," +
                "\"requestId\":\"" + requestId + "\"," +
                "\"data\":{" +
                "\"conversationId\":" + conversationId + "," +
                "\"msgType\":1," +
                "\"content\":{\"text\":\"hello from java ws smoke\"}" +
                "}}";
        webSocket.sendText(messagePayload, true).join();

        if (!messageAckLatch.await(15, TimeUnit.SECONDS)) {
            throw new IllegalStateException("MESSAGE_ACK not received");
        }
        boolean receiveOk = messageReceiveLatch.await(5, TimeUnit.SECONDS);

        try {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        } catch (Exception ignored) {
        }
        if (receiveOk) {
            System.out.println("WS OK");
        } else {
            System.out.println("WS ACK ONLY");
        }
    }
}
