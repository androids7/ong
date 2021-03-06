/*
 * Copyright   Actility, SA. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 *
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 *
 * Please contact Actility, SA.,  4, rue Ampere 22300 LANNION FRANCE
 * or visit www.actility.com if you need additional
 * information or have any questions.
 *
 * id $Id: M2MContextImpl.java 8761 2014-05-21 15:31:37Z JReich $
 * author $Author: JReich $
 * version $Revision: 8761 $
 * lastrevision $Date: 2014-05-21 17:31:37 +0200 (Wed, 21 May 2014) $
 * modifiedby $LastChangedBy: JReich $
 * lastmodified $LastChangedDate: 2014-05-21 17:31:37 +0200 (Wed, 21 May 2014) $
 */

package com.actility.m2m.m2m.impl;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.actility.m2m.m2m.M2MConstants;
import com.actility.m2m.m2m.M2MContext;
import com.actility.m2m.m2m.M2MEventHandler;
import com.actility.m2m.m2m.M2MException;
import com.actility.m2m.m2m.M2MProxyHandler;
import com.actility.m2m.m2m.M2MSession;
import com.actility.m2m.m2m.M2MUtils;
import com.actility.m2m.m2m.Request;
import com.actility.m2m.m2m.StatusCode;
import com.actility.m2m.m2m.log.BundleLogger;
import com.actility.m2m.servlet.ApplicationSession;
import com.actility.m2m.servlet.ApplicationSessionEvent;
import com.actility.m2m.servlet.ApplicationSessionListener;
import com.actility.m2m.servlet.ServletTimer;
import com.actility.m2m.servlet.TimerListener;
import com.actility.m2m.servlet.TimerService;
import com.actility.m2m.servlet.song.LongPollURIs;
import com.actility.m2m.servlet.song.SongDynamicRouter;
import com.actility.m2m.servlet.song.SongFactory;
import com.actility.m2m.servlet.song.SongServlet;
import com.actility.m2m.servlet.song.SongServletMessage;
import com.actility.m2m.servlet.song.SongServletRequest;
import com.actility.m2m.servlet.song.SongServletResponse;
import com.actility.m2m.servlet.song.SongURI;
import com.actility.m2m.util.log.OSGiLogger;
import com.actility.m2m.xo.XoException;
import com.actility.m2m.xo.XoObject;
import com.actility.m2m.xo.XoService;

public final class M2MContextImpl extends SongServlet implements M2MContext, TimerListener, ApplicationSessionListener {
    private static final Logger LOG = OSGiLogger.getLogger(M2MContextImpl.class, BundleLogger.getStaticLogger());

    public static final String M2M_PREFIX = "m2m:";
    public static final String M2M_TIMER_PREFIX = M2M_PREFIX + "timer/";
    public static final String AT_REMOTE_RESOURCE = M2M_PREFIX + "RemoteResource";
    public static final String AT_INDICATION = M2M_PREFIX + "Indication";
    public static final String AT_REQUEST = M2M_PREFIX + "Request";
    private static final String AT_M2M_SESSION = M2M_PREFIX + "M2MSession";

    private ApplicationSession appSession;
    private final XoService xoService;
    private final M2MUtils m2mUtils;
    private final M2MProxyHandler proxyHandler;
    private final M2MEventHandler eventHandler;
    private final URI sclUri;
    private SongURI songSclUri;
    private SongFactory songFactory;
    private SongDynamicRouter songDynamicRouter;
    private TimerService timerService;
    private volatile int[] backtrackableErrorCodes;

    public M2MContextImpl(URI sclUri, M2MProxyHandler proxyHandler, M2MEventHandler eventHandler, XoService xoService,
            M2MUtils m2mUtils) {
        this.sclUri = sclUri;
        this.proxyHandler = proxyHandler;
        this.eventHandler = eventHandler;
        this.xoService = xoService;
        this.m2mUtils = m2mUtils;
        this.backtrackableErrorCodes = new int[0];
    }

    private void sendUnsuccessResponse(IndicationImpl indication, StatusCode statusCode, String message) throws IOException,
            ServletException {
        if (!indication.isCommitted()) {
            String mediaType = null;
            StatusCode realStatusCode = statusCode;
            String realMessage = message;
            try {
                mediaType = m2mUtils.getAcceptedXoMediaType(indication.getHeader(SongServletMessage.HD_ACCEPT));
            } catch (M2MException e) {
                LOG.error(e.getMessage(), e);
                realStatusCode = e.getStatusCode();
                realMessage = e.getMessage();
            }
            ResponseImpl response = (ResponseImpl) indication.createUnsuccessResponse(realStatusCode);

            XoObject errorInfo = null;
            try {
                if (statusCode == realStatusCode) {
                    errorInfo = xoService.newXmlXoObject(M2MConstants.TAG_M2M_ERROR_INFO);
                    errorInfo.setNameSpace(M2MConstants.PREFIX_M2M);
                    errorInfo.setStringAttribute(M2MConstants.TAG_M2M_STATUS_CODE, realStatusCode.name());
                    errorInfo.setStringAttribute(M2MConstants.TAG_M2M_ADDITIONAL_INFO, realMessage);
                    if (M2MConstants.MT_APPLICATION_EXI.equals(mediaType)) {
                        response.getSongResponse().setContent(errorInfo.saveExi(), M2MConstants.MT_APPLICATION_EXI);
                    } else {
                        response.getSongResponse().setContent(errorInfo.saveXml(), M2MConstants.CT_APPLICATION_XML_UTF8);
                    }
                }

                response.send();
            } catch (XoException e) {
                throw new ServletException(message, e);
            } finally {
                if (errorInfo != null) {
                    errorInfo.free(true);
                }
            }
        } else {
            throw new ServletException(message);
        }
    }

    public void init() {
        ServletContext servletContext = getServletContext();
        this.songFactory = (SongFactory) servletContext.getAttribute(SONG_FACTORY);
        this.songDynamicRouter = (SongDynamicRouter) servletContext.getAttribute(SONG_DYNAMIC_ROUTER);
        this.timerService = (TimerService) servletContext.getAttribute(TIMER_SERVICE);
        appSession = songFactory.createApplicationSession();
        appSession.setExpires(0);
        appSession.setInvalidateWhenReady(false);
        if (sclUri != null) {
            songSclUri = songFactory.createURI(sclUri);
        }
    }

    public void destroy() {
        if (appSession.isValid()) {
            appSession.invalidate();
        }
    }

    public M2MEventHandler getM2MHandler() {
        return eventHandler;
    }

    public String getApplicationName() {
        return getServletContext().getServletContextName();
    }

    public String getApplicationPath() {
        return getServletContext().getContextPath();
    }

    public int[] getBacktrackableErrorCodes() {
        return backtrackableErrorCodes;
    }

    public void setBacktrackableErrorCodes(int[] backtrackableErrors) {
        this.backtrackableErrorCodes = backtrackableErrors;
    }

    public Object getAttribute(String name) {
        if (name.startsWith(M2MContextImpl.M2M_PREFIX)) {
            return null;
        }
        return getServletContext().getAttribute(name);
    }

    public void setAttribute(String name, Object attribute) {
        if (name.startsWith(M2MContextImpl.M2M_PREFIX)) {
            throw new IllegalArgumentException(
                    "m2m: prefix is reserved for M2M Layer internal attributes. Cannot set an attribute with this prefix: "
                            + name);
        }
        getServletContext().setAttribute(name, attribute);
    }

    public void removeAttribute(String name) {
        if (name.startsWith(M2MContextImpl.M2M_PREFIX)) {
            throw new IllegalArgumentException(
                    "m2m: prefix is reserved for M2M Layer internal attributes. Cannot remove an attribute with this prefix: "
                            + name);
        }
        getServletContext().removeAttribute(name);
    }

    public void cancelTimer(String timerId) {
        ServletTimer timer = appSession.getTimer(timerId);
        if (timer != null) {
            timer.cancel();
            appSession.removeAttribute(timerId);
        }
    }

    public URI createLocalUri(URI reference, String path) throws M2MException {
        try {
            return songFactory.createLocalURIFrom(reference, path).toURI();
        } catch (ServletException e) {
            throw new M2MException("Cannot compute the local URI from " + reference.toString(),
                    StatusCode.STATUS_INTERNAL_SERVER_ERROR, e);
        }
    }

    public boolean canBeServer(URI reference) throws M2MException {
        try {
            return songDynamicRouter.canBeServerFrom(songFactory.createURI(reference));
        } catch (ServletException e) {
            throw new M2MException("Cannot determine if application can be server from " + reference.toString(),
                    StatusCode.STATUS_INTERNAL_SERVER_ERROR, e);
        }
    }

    public URI[] createServerLongPoll(URI remoteTarget) throws M2MException {
        try {
            LongPollURIs uris = songDynamicRouter.createServerLongPoll(songFactory.createURI(remoteTarget));
            return new URI[] { uris.getContactURI().toURI(), uris.getLongPollURI().toURI() };
        } catch (ServletException e) {
            throw new M2MException("Cannot create a server long poll connection", StatusCode.STATUS_INTERNAL_SERVER_ERROR, e);
        }
    }

    public void createServerLongPoll(URI contactUri, URI longPollUri) throws M2MException {
        try {
            songDynamicRouter.createServerLongPoll(songFactory.createURI(contactUri), songFactory.createURI(longPollUri));
        } catch (ServletException e) {
            throw new M2MException("Cannot create a server long poll connection", StatusCode.STATUS_INTERNAL_SERVER_ERROR, e);
        }
    }

    public void deleteServerLongPoll(URI contactUri, URI longPollUri) {
        songDynamicRouter.deleteServerLongPoll(songFactory.createURI(contactUri), songFactory.createURI(longPollUri));
    }

    public void createClientLongPoll(URI contactUri, URI longPollUri) throws M2MException {
        try {
            songDynamicRouter.createClientLongPoll(songFactory.createURI(contactUri), songFactory.createURI(longPollUri));
        } catch (ServletException e) {
            throw new M2MException("Cannot create a client long poll connection", StatusCode.STATUS_INTERNAL_SERVER_ERROR, e);
        }
    }

    public void deleteClientLongPoll(URI contactUri, URI longPollUri) {
        songDynamicRouter.deleteClientLongPoll(songFactory.createURI(contactUri), songFactory.createURI(longPollUri));
    }

    public String startTimer(long timeout, Serializable info) {
        ServletTimer timer = timerService.createTimer(appSession, timeout, false, info);
        return timer.getId();
    }

    public M2MSession createSession(int timeout) {
        ApplicationSession appSession = songFactory.createApplicationSession();
        M2MSession m2mSession = new M2MSessionImpl(timerService, appSession, timeout);
        appSession.setAttribute(AT_M2M_SESSION, m2mSession);
        return m2mSession;
    }

    public void doCreate(SongServletRequest request) throws IOException, ServletException {
        IndicationImpl indication = new IndicationImpl(songFactory, xoService, m2mUtils, request);
        StatusCode statusCode = null;
        String message = null;
        try {
            if (request.isProxy()) {
                proxyHandler.doProxyCreateIndication(indication);
            } else {
                eventHandler.doCreateIndication(indication);
            }
        } catch (M2MException e) {
            statusCode = e.getStatusCode();
            message = e.getMessage();
        }
        if (statusCode != null) {
            sendUnsuccessResponse(indication, statusCode, message);
        }
    }

    public void doRetrieve(SongServletRequest request) throws IOException, ServletException {
        IndicationImpl indication = new IndicationImpl(songFactory, xoService, m2mUtils, request);
        StatusCode statusCode = null;
        String message = null;
        try {
            if (request.isProxy()) {
                proxyHandler.doProxyRetrieveIndication(indication);
            } else {
                eventHandler.doRetrieveIndication(indication);
            }
        } catch (M2MException e) {
            statusCode = e.getStatusCode();
            message = e.getMessage();
        }
        if (statusCode != null) {
            sendUnsuccessResponse(indication, statusCode, message);
        }
    }

    public void doUpdate(SongServletRequest request) throws IOException, ServletException {
        IndicationImpl indication = new IndicationImpl(songFactory, xoService, m2mUtils, request);
        StatusCode statusCode = null;
        String message = null;
        try {
            if (request.isProxy()) {
                proxyHandler.doProxyUpdateIndication(indication);
            } else {
                eventHandler.doUpdateIndication(indication);
            }
        } catch (M2MException e) {
            statusCode = e.getStatusCode();
            message = e.getMessage();
        }
        if (statusCode != null) {
            sendUnsuccessResponse(indication, statusCode, message);
        }
    }

    public void doDelete(SongServletRequest request) throws IOException, ServletException {
        IndicationImpl indication = new IndicationImpl(songFactory, xoService, m2mUtils, request);
        StatusCode statusCode = null;
        String message = null;
        try {
            if (request.isProxy()) {
                proxyHandler.doProxyDeleteIndication(indication);
            } else {
                eventHandler.doDeleteIndication(indication);
            }
        } catch (M2MException e) {
            statusCode = e.getStatusCode();
            message = e.getMessage();
        }
        if (statusCode != null) {
            sendUnsuccessResponse(indication, statusCode, message);
        }
    }

    public void doSuccessResponse(SongServletResponse response) throws IOException {
        ConfirmImpl confirm = new ConfirmImpl(xoService, response);
        try {
            eventHandler.doSuccessConfirm(confirm);
        } catch (M2MException e) {
            LOG.error("ResourceException while calling doSuccessResponseConfirm for request on "
                    + response.getRequest().getTargetID().absoluteURI(), e);
        }
    }

    public void doErrorResponse(SongServletResponse response) throws IOException {
        ConfirmImpl confirm = new ConfirmImpl(xoService, response);
        try {
            eventHandler.doUnsuccessConfirm(confirm);
        } catch (M2MException e) {
            LOG.error("ResourceException while calling doSuccessResponseConfirm for request on "
                    + response.getRequest().getTargetID().absoluteURI(), e);
        }
    }

    public void timeout(ServletTimer timer) {
        M2MSession session = (M2MSession) timer.getApplicationSession().getAttribute(M2M_TIMER_PREFIX + timer.getId());
        try {
            eventHandler.timeout(timer.getId(), session, timer.getInfo());
        } catch (IOException e) {
            LOG.error("IOException while calling timeout", e);
        } catch (M2MException e) {
            LOG.error("ResoruceException while calling timeout", e);
        }
    }

    public void sessionCreated(ApplicationSessionEvent ev) {
        // Ignore
    }

    public void sessionDestroyed(ApplicationSessionEvent ev) {
        // Ignore
    }

    public void sessionExpired(ApplicationSessionEvent ev) {
        M2MSession session = (M2MSession) ev.getApplicationSession().getAttribute(AT_M2M_SESSION);
        if (session != null) {
            try {
                eventHandler.sessionExpired(session);
            } catch (IOException e) {
                LOG.error("IOException while calling sessionExpired", e);
            } catch (M2MException e) {
                LOG.error("ResourceException while calling sessionExpired", e);
            }
        }
    }

    public void sessionReadyToInvalidate(ApplicationSessionEvent ev) {
        // Ignore
    }

    public Request createRequest(String method, URI requestingEntity, URI targetID) {
        SongServletRequest request = songFactory.createRequest(method, requestingEntity, targetID);
        if (songSclUri != null) {
            request.getProxy().setProxyTo(songSclUri);
        }
        return new RequestImpl(songFactory, request);
    }

    public M2MUtils getM2MUtils() {
        return m2mUtils;
    }
}
