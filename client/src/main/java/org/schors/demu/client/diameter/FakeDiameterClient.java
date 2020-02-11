package org.schors.demu.client.diameter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

public class FakeDiameterClient {

    private EventNotifier eventNotifier;
    private Vertx vertx;

    public FakeDiameterClient(Vertx vertx, EventNotifier eventNotifier) {
        this.vertx = vertx;
        this.eventNotifier = eventNotifier;
    }

    public void sendCCRI() {
        JsonDiameter ccri = new JsonDiameter().req(true).reqType(1);
        JsonDiameter ccai = new JsonDiameter().req(false).reqType(1).res(2001);
        JsonDiameter ccru = new JsonDiameter().req(true).reqType(2);
        JsonDiameter ccau = new JsonDiameter().req(false).reqType(2).res(2001);
        JsonDiameter ccrt = new JsonDiameter().req(true).reqType(3);
        JsonDiameter ccat = new JsonDiameter().req(false).reqType(3).res(2001);


        Future.future(promise -> {
            eventNotifier.notifyRequest(ccri.get());
            promise.complete();
        }).compose(o -> Future.future(promise -> {
            vertx.setTimer(1000, aLong -> {
                eventNotifier.notifyRequest(ccai.get());
                promise.complete();
            });
        })).compose(o -> Future.future(promise -> {
            eventNotifier.notifyRequest(ccru.get());
            promise.complete();
        })).compose(o -> Future.future(promise -> {
            vertx.setTimer(1000, aLong -> {
                eventNotifier.notifyRequest(ccau.get());
                promise.complete();
            });
        })).compose(o -> Future.future(promise -> {
            eventNotifier.notifyRequest(ccrt.get());
            promise.complete();
        })).compose(o -> Future.future(promise -> {
            vertx.setTimer(1000, aLong -> {
                eventNotifier.notifyRequest(ccat.get());
                promise.complete();
            });
        }));
    }
}

