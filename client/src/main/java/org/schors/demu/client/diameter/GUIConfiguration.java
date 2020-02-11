package org.schors.demu.client.diameter;

import org.jdiameter.api.Configuration;
import org.jdiameter.server.impl.helpers.Parameters;
import org.jdiameter.server.impl.helpers.XMLConfiguration;


import java.io.InputStream;

public class GUIConfiguration extends XMLConfiguration {

    private Configuration peer;

    public GUIConfiguration(InputStream in) throws Exception {
        super(in);
    }

    @Override
    public Configuration[] getChildren(int what) {
        Configuration[] res;
        if (Parameters.PeerTable.ordinal() == what && peer != null) {
            res = new Configuration[]{peer};
        } else {
            res = super.getChildren(what);
        }
        return res;
    }

    public void setRemotePeer(String uri) {
        peer = getInstance()
                .add(Parameters.PeerRating, 1)
                .add(Parameters.PeerName, uri);
    }
}
