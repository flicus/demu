package org.schors.demu.client.diameter;

import io.vertx.core.json.JsonObject;

public interface EventNotifier {
    void notifyRequest(JsonObject request);
    void notifyResponse(JsonObject response);
}
