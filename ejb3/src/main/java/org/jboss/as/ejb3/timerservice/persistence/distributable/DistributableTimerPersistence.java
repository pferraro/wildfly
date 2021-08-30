/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.ejb3.timerservice.persistence.distributable;

import java.io.Closeable;
import java.util.List;
import java.util.function.Supplier;

import javax.transaction.TransactionManager;

import org.jboss.as.ejb3.timerservice.Timer;
import org.jboss.as.ejb3.timerservice.TimerService;
import org.jboss.as.ejb3.timerservice.persistence.TimerPersistence;

/**
 * @author Paul Ferraro
 */
public class DistributableTimerPersistence implements TimerPersistence {

    private final Supplier<String> idFactory = null;

    @Override
    public void addTimer(Timer descriptor) {
    }

    @Override
    public void persistTimer(Timer descriptor) {
    }

    @Override
    public boolean shouldRun(Timer descriptor, TransactionManager txManager) {
        return false;
    }

    @Override
    public void timerUndeployed(String timedObjectId) {
    }

    @Override
    public List<Timer> loadActiveTimers(String timedObjectId, TimerService timerService) {
        return null;
    }

    @Override
    public Closeable registerChangeListener(String timedObjectId, TimerChangeListener listener) {
        return null;
    }
}
