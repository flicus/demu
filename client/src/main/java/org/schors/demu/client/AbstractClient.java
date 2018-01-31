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

package org.schors.demu.client;

import org.jdiameter.api.*;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.auth.events.ReAuthRequest;
import org.jdiameter.api.cca.ClientCCASession;
import org.jdiameter.api.cca.ClientCCASessionListener;
import org.jdiameter.api.cca.ServerCCASession;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.common.api.app.cca.IClientCCASessionContext;
import org.jdiameter.common.impl.app.cca.CCASessionFactoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public abstract class AbstractClient implements EventListener<Request, Answer>, NetworkReqListener, StateChangeListener<AppSession>, ClientCCASessionListener, IClientCCASessionContext {

    protected static final Logger logger = LoggerFactory.getLogger("CCAClient");

    protected static final int CC_REQUEST_TYPE_INITIAL = 1;
    protected static final int CC_REQUEST_TYPE_INTERIM = 2;
    protected static final int CC_REQUEST_TYPE_TERMINATE = 3;
    protected static final int CC_REQUEST_TYPE_EVENT = 4;

    protected StackCreator stack;
    protected ISessionFactory sessionFactory;
    protected ApplicationId applicationId;
    protected ClientCCASession clientCCASession;
    protected int ccRequestNumber = 0;

    public void init(InputStream configStream, String clientID, ApplicationId appId) throws Exception {
        this.applicationId = appId;
        stack = new StackCreator();
        stack.init(configStream, this, this, clientID, true, appId); // lets always pass
        this.sessionFactory = (ISessionFactory) this.stack.getSessionFactory();

        try {
            CCASessionFactoryImpl creditControlSessionFactory = new CCASessionFactoryImpl(this.sessionFactory);
            sessionFactory.registerAppFacory(ServerCCASession.class, creditControlSessionFactory);
            sessionFactory.registerAppFacory(ClientCCASession.class, creditControlSessionFactory);

            creditControlSessionFactory.setStateListener(this);
            creditControlSessionFactory.setClientSessionListener(this);
            creditControlSessionFactory.setClientContextListener(this);
            this.clientCCASession = this.sessionFactory.getNewAppSession(this.sessionFactory.getSessionId("xxTESTxx"), applicationId, ClientCCASession.class,
                    (Object) null);
        } finally {
            try {
                configStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start() throws IllegalDiameterStateException, InternalException {
        stack.start();
    }

    public void start(Mode mode, long timeOut, TimeUnit timeUnit) throws IllegalDiameterStateException, InternalException {
        stack.start(mode, timeOut, timeUnit);
    }

    public void stop(long timeOut, TimeUnit timeUnit, int disconnectCause) throws IllegalDiameterStateException, InternalException {
        stack.stop(timeOut, timeUnit, disconnectCause);
    }

    public void stop(int disconnectCause) {
        stack.stop(disconnectCause);
    }

    public String getSessionId() {
        return this.clientCCASession.getSessionId();
    }

    public void fetchSession(String sessionId) throws InternalException {
        this.clientCCASession = stack.getSession(sessionId, ClientCCASession.class);
    }

    public ClientCCASession getSession() {
        return this.clientCCASession;
    }

    @Override
    public void receivedSuccessMessage(Request request, Answer answer) {
        Utils.printMessage(answer);
    }

    @Override
    public void timeoutExpired(Request request) {

    }

    @Override
    public Answer processRequest(Request request) {
        return null;
    }

    @Override
    public void stateChanged(Enum oldState, Enum newState) {

    }

    @Override
    public void stateChanged(AppSession source, Enum oldState, Enum newState) {

    }

    @Override
    public void doReAuthRequest(ClientCCASession session, ReAuthRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {

    }

    @Override
    public int getDefaultDDFHValue() {
        return 1;
    }

    @Override
    public void grantAccessOnDeliverFailure(ClientCCASession clientCCASessionImpl, Message request) {

    }

    @Override
    public void denyAccessOnDeliverFailure(ClientCCASession clientCCASessionImpl, Message request) {

    }

    @Override
    public void grantAccessOnTxExpire(ClientCCASession clientCCASessionImpl) {

    }

    @Override
    public void denyAccessOnTxExpire(ClientCCASession clientCCASessionImpl) {

    }

    @Override
    public void grantAccessOnFailureMessage(ClientCCASession clientCCASessionImpl) {

    }

    @Override
    public void denyAccessOnFailureMessage(ClientCCASession clientCCASessionImpl) {

    }

    @Override
    public void indicateServiceError(ClientCCASession clientCCASessionImpl) {

    }

    @Override
    public long getDefaultTxTimerValue() {
        return 10;
    }

    @Override
    public void txTimerExpired(ClientCCASession session) {

    }

    @Override
    public int getDefaultCCFHValue() {
        return 1;
    }
}
