/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package org.jboss.as.ejb3.component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jboss.as.ejb3.timerservice.Timer;
import org.jboss.as.ejb3.timerservice.TimerManager;
import org.jboss.as.ejb3.timerservice.TimerManagerRegistry;

/**
 * A registry to which individual {@link javax.ejb.TimerService timer services} can register to (and un-register from). The main purpose
 * of this registry is to provide an implementation of {@link #getAllActiveTimers()} which returns all
 * {@link javax.ejb.TimerService#getTimers() active timers} after querying each of the {@link javax.ejb.TimerService timer services} registered
 * with this {@link TimerServiceRegistry registry}.
 * <p/>
 * Typical use of this registry is to maintain one instance of this registry, per deployment unit (also known as EJB module) and register the timer
 * services of all EJB components that belong to that deployment unit. Effectively, such an instance can then be used to fetch all active timers
 * that are applicable to that deployment unit (a.k.a EJB module).
 *
 * @author Jaikiran Pai
 */
public class TimerServiceRegistry implements TimerManagerRegistry {

    private final Set<TimerManager> timerServices = Collections.synchronizedSet(new HashSet<>());

    @Override
    public void registerTimerService(final TimerManager timerService) {
        if (timerService == null) {
            return;
        }
        timerServices.add(timerService);
    }

    @Override
    public boolean unregisterTimerService(final TimerManager timerService) {
        if (timerService == null) {
            return false;
        }
        return timerServices.remove(timerService);
    }

    @Override
    public Collection<Timer> getAllActiveTimers() {
        final Collection<Timer> activeTimers = new HashSet<>();
        synchronized (timerServices) {
            for (final TimerManager timerService : timerServices) {
                activeTimers.addAll(timerService.getTimers());
            }
        }
        return activeTimers;
    }
}
