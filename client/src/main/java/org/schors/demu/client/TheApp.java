package org.schors.demu.client;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.apache.log4j.Logger;
import org.jdiameter.api.ApplicationId;
import org.schors.demu.client.diameter.Client;
import org.schors.demu.client.diameter.EventNotifier;
import org.schors.demu.client.diameter.FakeDiameterClient;
import org.schors.demu.client.diameter.GUIConfiguration;

import java.util.List;
import java.util.Optional;

public class TheApp extends AbstractVerticle implements EventNotifier {

    private static ApplicationId appId = ApplicationId.createByAuthAppId(0, 4L);
    Logger logger = Logger.getLogger(TheApp.class);
    private Vertx vertx;
    private Client diameterClient = new Client();
    private SQLClient sqlClient;

    @Override
    public void init(Vertx vertx, Context context) {
        super.init(vertx, context);
        this.vertx = vertx;
    }

    @Override
    public void start() throws Exception {
        GUIConfiguration guiConfiguration = new GUIConfiguration(TheApp.class.getClassLoader().getResourceAsStream("config-client.xml"));
        diameterClient.init(guiConfiguration, "Client", appId, this, vertx);
        diameterClient.start();



        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        SockJSHandler sockJSHandler = SockJSHandler.create(vertx);

        BridgeOptions bridgeOptions = new BridgeOptions()
                .addInboundPermitted(new PermittedOptions()
                        .setAddress("diameter.api")
                        .setMatch(new JsonObject().put("action", "start").put("target", "session")))
                .addInboundPermitted(new PermittedOptions()
                        .setAddress("diameter.api")
                        .setMatch(new JsonObject().put("action", "init").put("target", "session")))
                .addInboundPermitted(new PermittedOptions()
                        .setAddress("config")
                        .setMatch(new JsonObject().put("action", "get")))
                .addInboundPermitted(new PermittedOptions()
                        .setAddress("config")
                        .setMatch(new JsonObject().put("action", "set")))
                .addOutboundPermitted(new PermittedOptions()
                        .setAddress("diameter.events"));

        router.mountSubRouter("/eventbus", sockJSHandler.bridge(bridgeOptions));

        vertx.eventBus().consumer("config", message -> {
            JsonObject json = (JsonObject) message.body();
            switch (json.getString("action")) {
                case "get":
                    retrieveOne("SELECT * FROM CONFIG WHERE ID = 1")
                            .map(entries -> entries.orElse(new JsonObject()))
                            .setHandler(jsonObjectAsyncResult -> {
                                logger.debug("###: config "+jsonObjectAsyncResult.result());
                                message.reply(jsonObjectAsyncResult.result());
                            });
                    break;
                case "set":
                    JsonObject config = json.getJsonObject("body");
                    JsonArray params = new JsonArray().add(config.getValue("REMOTE_PEER"));
                    saveOne("UPDATE CONFIG SET REMOTE_PEER=? WHERE ID=1", params)
                            .setHandler(voidAsyncResult -> {
                                logger.debug("###: config saved "+json);
                            });
                    break;
            }
        });

        vertx.eventBus().consumer("diameter.api", message -> {
            JsonObject json = (JsonObject) message.body();
            switch (json.getString("action")) {
                case "start": {
                    vertx.executeBlocking(promise ->
                            {
                                try {
//                                    diameterClient.sendCCRI(diameterClient.getSession());
                                } catch (Exception e) {
                                    logger.error(e, e);
                                    promise.fail(e);
                                }
                                promise.complete();
                            },
                            asyncResult -> {
                            });

                    break;
                }
                case "init": {
                    new FakeDiameterClient(vertx, this).sendCCRI();
                    break;
                }
                default: {
                    logger.error("unknown message: " + json);
                }
            }
        });

        router.route().handler(StaticHandler.create("./web"));

        server.requestHandler(router).listen(9090, "localhost");

        JsonObject config = new JsonObject()
                .put("url", "jdbc:h2:~/demu")
                .put("driver_class", "org.h2.Driver")
                .put("user", "sa")
                .put("password", "")
                .put("max_pool_size", 30);
        sqlClient = JDBCClient.createShared(vertx, config);

    }

    protected Future<SQLConnection> getConnection() {
        Promise<SQLConnection> promise = Promise.promise();
        sqlClient.getConnection(promise);
        return promise.future();
    }

    protected Future<Optional<JsonObject>> retrieveOne(String sql) {
        return getConnection()
                .compose(sqlConnection -> {
                    Promise<Optional<JsonObject>> promise = Promise.promise();
                    sqlConnection.query(sql, r -> {
                        if (r.succeeded()) {
                            List<JsonObject> resList = r.result().getRows();

                            if (resList.isEmpty()) {
                                promise.complete(Optional.empty());
                            } else {
                                promise.complete(Optional.of(resList.get(0)));
                            }
                        } else {
                            promise.fail(r.cause());
                        }

                        sqlConnection.close();
                    });
                    return promise.future();
                });
    }

    protected Future<Void> saveOne(String sql, JsonArray params) {
        return getConnection()
                .compose(sqlConnection -> {
                    Promise<Void> promise = Promise.promise();
                    sqlConnection.updateWithParams(sql, params, updateResultAsyncResult -> {
                        if (updateResultAsyncResult.succeeded()) {
                            promise.complete();
                        } else {
                            promise.fail(updateResultAsyncResult.cause());
                        }
                    });
                    return promise.future();
                });
    }


    private void handleAPI() {

    }

    @Override
    public void notifyRequest(JsonObject request) {
        vertx.eventBus().publish("diameter.events", request);
    }

    @Override
    public void notifyResponse(JsonObject response) {
        vertx.eventBus().publish("diameter.events", response);
    }
}
