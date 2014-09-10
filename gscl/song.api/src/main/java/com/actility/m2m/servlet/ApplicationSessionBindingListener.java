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
 * id $Id: ApplicationSessionBindingListener.java 6081 2013-10-15 13:33:50Z mlouiset $
 * author $Author: mlouiset $
 * version $Revision: 6081 $
 * lastrevision $Date: 2013-10-15 15:33:50 +0200 (Tue, 15 Oct 2013) $
 * modifiedby $LastChangedBy: mlouiset $
 * lastmodified $LastChangedDate: 2013-10-15 15:33:50 +0200 (Tue, 15 Oct 2013) $
 */

package com.actility.m2m.servlet;

import java.util.EventListener;

/**
 * Causes an object to be notified when it is bound to or unbound from an ApplicationSession. The object is notified by an
 * ApplicationSessionBindingEvent object. This may be as a result of a servlet programmer explicitly unbinding an attribute from
 * an application session, due to an application session being invalidated, or due to an application session timing out.
 * 
 * @see ApplicationSession ApplicationSessionBindingEvent
 */
public interface ApplicationSessionBindingListener extends EventListener {

    /**
     * Notifies the object that it is being bound to an application session and identifies the application session.
     * 
     * @param event The binding event
     */
    void valueBound(ApplicationSessionBindingEvent event);

    /**
     * Notifies the object that it is being unbound from an application session and identifies the application session.
     * 
     * @param event The binding event
     */
    void valueUnbound(ApplicationSessionBindingEvent event);
}