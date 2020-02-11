package org.schors.demu.client;

import io.vertx.core.Vertx;
import org.schors.demu.client.diameter.GUIConfiguration;

import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        Dao.get();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new TheApp());
    }

    public static void transfer(InputStream in, OutputStream out, int buffer) throws IOException {
        byte[] read = new byte[buffer];
        while (0 < (buffer = in.read(read)))
            out.write(read, 0, buffer);
    }
}
