package org.schors.demu.client.diameter;

import io.vertx.core.json.JsonObject;

public class JsonDiameter {

    private JsonObject json = new JsonObject();
    private JsonObject avps = new JsonObject();

    public JsonDiameter() {
        json.put("session-id", "session-id;12345;abcdef");
        json.put("command-code", 272);
        json.put("avps", avps);
    }

    public JsonDiameter req(boolean request) {
        json.put("is-request", request);
        return this;
    }

    public JsonDiameter res(int result) {
        avps.put("Result-Code", new JsonObject().put("value", result));
        return this;
    }

    public JsonDiameter reqType(int type) {
        avps.put("CC-Request-Type", new JsonObject().put("value", type));
        return this;
    }

    public JsonDiameter avp(String name, Object value) {
        avps.put(name, new JsonObject().put("value", value));
        return this;
    }

    public JsonObject get() {
        return json;
    }

    //CC-Request-Type
    //Result-Code
}
