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

package org.jboss.as.ejb3.timerservice;

import static org.jboss.as.ejb3.logging.EjbLogger.EJB3_TIMER_LOGGER;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;

import org.jboss.as.ejb3.component.EJBComponent;
import org.jboss.as.ejb3.component.allowedmethods.AllowedMethodsInformation;
import org.jboss.as.ejb3.component.allowedmethods.MethodType;
import org.jboss.as.ejb3.component.singleton.SingletonComponent;
import org.jboss.as.ejb3.context.CurrentInvocationContext;
import org.jboss.invocation.InterceptorContext;

/**
 * EJB Specification implementation of {@link javax.ejb.TimerService}.
 * @author Paul Ferraro
 */
public class EJBTimerService implements javax.ejb.TimerService {

    private final TimerFactory factory;
    private final boolean singleton;

    public EJBTimerService(EJBComponent component) {
        this.factory = component.getTimerService();
        this.singleton = component instanceof SingletonComponent;
    }

    @Override
    public javax.ejb.Timer createCalendarTimer(ScheduleExpression schedule) throws EJBException {
        return this.createCalendarTimer(schedule, null);
    }

    @Override
    public javax.ejb.Timer createCalendarTimer(ScheduleExpression schedule, TimerConfig timerConfig) throws EJBException {
        assertTimerServiceState();
        Serializable info = timerConfig == null ? null : timerConfig.getInfo();
        boolean persistent = timerConfig == null || timerConfig.isPersistent();
        return new EJBTimer(this.factory.createCalendarTimer(schedule, info, persistent, null));
    }

    @Override
    public javax.ejb.Timer createIntervalTimer(long initialDuration, long intervalDuration, TimerConfig timerConfig) throws EJBException {
        if (initialDuration < 0) {
            throw EJB3_TIMER_LOGGER.invalidInitialExpiration("intervalDuration");
        }
        return this.createIntervalTimer(new Date(System.currentTimeMillis() + initialDuration), intervalDuration, timerConfig);
    }

    @Override
    public javax.ejb.Timer createIntervalTimer(Date initialExpiration, long intervalDuration, TimerConfig timerConfig) throws EJBException {
        assertTimerServiceState();
        if (initialExpiration == null) {
            throw EJB3_TIMER_LOGGER.initialExpirationIsNullCreatingTimer();
        }
        if (initialExpiration.getTime() < 0) {
            throw EJB3_TIMER_LOGGER.invalidInitialExpiration("initialExpiration.getTime()");
        }
        if (intervalDuration < 0) {
            throw EJB3_TIMER_LOGGER.invalidInitialExpiration("intervalDuration");
        }
        return new EJBTimer(this.factory.createTimer(initialExpiration, intervalDuration, timerConfig.getInfo(), timerConfig.isPersistent()));
    }

    @Override
    public javax.ejb.Timer createSingleActionTimer(long duration, TimerConfig timerConfig) throws EJBException {
        if (duration < 0) {
            throw EJB3_TIMER_LOGGER.invalidDurationActionTimer();
        }
        return this.createSingleActionTimer(new Date(System.currentTimeMillis() + duration), timerConfig);
    }

    @Override
    public javax.ejb.Timer createSingleActionTimer(Date expiration, TimerConfig timerConfig) throws EJBException {
        assertTimerServiceState();
        if (expiration == null) {
            throw EJB3_TIMER_LOGGER.expirationIsNull();
        }
        if (expiration.getTime() < 0) {
            throw EJB3_TIMER_LOGGER.invalidExpirationActionTimer();
        }
        return new EJBTimer(this.factory.createTimer(expiration, 0, timerConfig.getInfo(), timerConfig.isPersistent()));
    }

    @Override
    public javax.ejb.Timer createTimer(long duration, Serializable info) throws EJBException {
        if (duration < 0) {
            throw EJB3_TIMER_LOGGER.invalidDurationTimer();
        }
        return this.createTimer(new Date(System.currentTimeMillis() + duration), info);
    }

    @Override
    public javax.ejb.Timer createTimer(Date expiration, Serializable info) throws EJBException {
        return this.createTimer(expiration, 0, info);
    }

    @Override
    public javax.ejb.Timer createTimer(long initialDuration, long intervalDuration, Serializable info) throws EJBException {
        if (initialDuration < 0) {
            throw EJB3_TIMER_LOGGER.invalidDurationTimer();
        }
        return this.createTimer(new Date(System.currentTimeMillis() + initialDuration), intervalDuration, info);
    }

    @Override
    public javax.ejb.Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info) throws EJBException {
        assertTimerServiceState();
        if (initialExpiration == null) {
            throw EJB3_TIMER_LOGGER.initialExpirationDateIsNull();
        }
        if (initialExpiration.getTime() < 0) {
            throw EJB3_TIMER_LOGGER.invalidExpirationTimer();
        }
        if (intervalDuration < 0) {
            throw EJB3_TIMER_LOGGER.invalidIntervalDurationTimer();
        }
        return new EJBTimer(this.factory.createTimer(initialExpiration, intervalDuration, info, true));
    }

    @Override
    public Collection<javax.ejb.Timer> getTimers() throws EJBException {
        assertTimerServiceState();
        return map(this.factory.getTimers());
    }

    @Override
    public Collection<javax.ejb.Timer> getAllTimers() throws EJBException {
        assertTimerServiceState();
        TimerManagerRegistry registry = this.factory.getRegistry();
        return map((registry != null) ? registry.getAllActiveTimers() : this.factory.getTimers());
    }

    private static Collection<javax.ejb.Timer> map(Collection<Timer> timers) {
        return timers.stream().map(timer -> new EJBTimer(timer)).collect(Collectors.toList());
    }

    private void assertTimerServiceState() {
        AllowedMethodsInformation.checkAllowed(MethodType.TIMER_SERVICE_METHOD);
        if (isLifecycleCallbackInvocation() && !this.singleton) {
            throw EJB3_TIMER_LOGGER.failToInvokeTimerServiceDoLifecycle();
        }
    }

    /**
     * Returns true if the {@link CurrentInvocationContext} represents a lifecycle
     * callback invocation. Else returns false.
     * <p>
     * This method internally relies on {@link CurrentInvocationContext#get()} to obtain
     * the current invocation context.
     * <ul>
     * <li>If the context is available then it looks for the method that was invoked.
     * The absence of a method indicates a lifecycle callback.</li>
     * <li>If the context is <i>not</i> available, then this method returns false (i.e.
     * it doesn't consider the current invocation as a lifecycle callback). This is
     * for convenience, to allow the invocation of {@link javax.ejb.TimerService} methods
     * in the absence of {@link CurrentInvocationContext}</li>
     * </ul>
     * <p/>
     * </p>
     *
     * @return
     */
    private static boolean isLifecycleCallbackInvocation() {
        final InterceptorContext currentInvocationContext = CurrentInvocationContext.get();
        if (currentInvocationContext == null) {
            return false;
        }
        // If the method in current invocation context is null,
        // then it represents a lifecycle callback invocation
        Method invokedMethod = currentInvocationContext.getMethod();
        if (invokedMethod == null) {
            // it's a lifecycle callback
            return true;
        }
        // not a lifecycle callback
        return false;
    }
}
