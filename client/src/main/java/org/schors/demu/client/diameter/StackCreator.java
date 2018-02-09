/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 schors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.schors.demu.client.diameter;

import org.apache.log4j.Logger;
import org.jdiameter.api.*;
import org.jdiameter.api.validation.ValidatorLevel;
import org.jdiameter.common.impl.validation.DictionaryImpl;
import org.jdiameter.server.impl.StackImpl;
import org.jdiameter.server.impl.helpers.XMLConfiguration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Set;

/**
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 * @author <a href="mailto:baranowb@gmail.com"> Bartosz Baranowski </a>
 */
public class StackCreator extends StackImpl {

    private static Logger logger = Logger.getLogger(StackCreator.class);

    public StackCreator() {
        super();
    }

    public StackCreator(InputStream streamConfig, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener, String dooer,
                        Boolean isServer, ApplicationId... appIds) throws Exception {
        init(isServer ? new XMLConfiguration(streamConfig) : new org.jdiameter.client.impl.helpers.XMLConfiguration(streamConfig), networkReqListener,
                eventListener, dooer, isServer, appIds);
    }

    public StackCreator(String stringConfig, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener, String dooer, Boolean isServer,
                        ApplicationId... appIds) throws Exception {
        init(isServer ? new XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes())) : new org.jdiameter.client.impl.helpers.XMLConfiguration(
                new ByteArrayInputStream(stringConfig.getBytes())), networkReqListener, eventListener, dooer, isServer, appIds);
    }

    public void init(String stringConfig, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener, String dooer, Boolean isServer,
                     ApplicationId... appIds) throws Exception {
        this.init(isServer ? new XMLConfiguration(new ByteArrayInputStream(stringConfig.getBytes())) :
                new org.jdiameter.client.impl.helpers.XMLConfiguration(new ByteArrayInputStream(
                        stringConfig.getBytes())), networkReqListener, eventListener, dooer, isServer, appIds);
    }

    public void init(InputStream streamConfig, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener, String dooer,
                     Boolean isServer, ApplicationId... appIds) throws Exception {
        this.init(isServer ? new XMLConfiguration(streamConfig) :
                new org.jdiameter.client.impl.helpers.XMLConfiguration(streamConfig), networkReqListener, eventListener, dooer, isServer, appIds);
    }

    public void init(Configuration config, NetworkReqListener networkReqListener, EventListener<Request, Answer> eventListener, String identifier,
                     Boolean isServer, ApplicationId... appIds) throws Exception {
        // local one
        try {
            this.init(config);

            // Let it stabilize...
            Thread.sleep(500);

            // Let's do it right and enable all validation levels!
            DictionaryImpl.INSTANCE.setEnabled(true);
            DictionaryImpl.INSTANCE.setReceiveLevel(ValidatorLevel.ALL);
            DictionaryImpl.INSTANCE.setSendLevel(ValidatorLevel.ALL);

            Network network = unwrap(Network.class);

            if (appIds != null) {

                for (ApplicationId appId : appIds) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Diameter " + identifier + " :: Adding Listener for [" + appId + "].");
                    }
                    network.addNetworkReqListener(networkReqListener, appId);
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Diameter " + identifier + " :: Supporting " + appIds.length + " applications.");
                }
            } else {
                Set<ApplicationId> stackAppIds = getMetaData().getLocalPeer().getCommonApplications();

                for (ApplicationId appId : stackAppIds) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Diameter " + identifier + " :: Adding Listener for [" + appId + "].");
                    }
                    network.addNetworkReqListener(networkReqListener, appId);
                }

                if (logger.isInfoEnabled()) {
                    logger.info("Diameter " + identifier + " :: Supporting " + stackAppIds.size() + " applications.");
                }
            }
        } catch (Exception e) {
            logger.error("Failure creating stack '" + identifier + "'", e);
        }

    }

}
