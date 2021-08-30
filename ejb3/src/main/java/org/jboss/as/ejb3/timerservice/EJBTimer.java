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

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBException;
import javax.ejb.NoMoreTimeoutsException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.ScheduleExpression;
import javax.ejb.TimerHandle;

import org.jboss.as.ejb3.component.allowedmethods.AllowedMethodsInformation;
import org.jboss.as.ejb3.component.allowedmethods.MethodType;
import org.jboss.as.ejb3.logging.EjbLogger;
import org.jboss.as.ejb3.timerservice.schedule.CalendarBasedTimeout;

/**
 * EJB Specification implementation of {@link javax.ejb.Timer}.
 * @author Paul Ferraro
 */
public class EJBTimer implements javax.ejb.Timer {

    private final Timer timer;

    public EJBTimer(Timer timer) {
        this.timer = timer;
    }

    @Override
    public void cancel() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        this.timer.cancel();
    }

    @Override
    public long getTimeRemaining() throws IllegalStateException, NoMoreTimeoutsException, NoSuchObjectLocalException, EJBException {
        // first check the validity of the timer state
        assertTimerState(this.timer.getState());
        return this.timer.getTimeRemaining();
    }

    @Override
    public Date getNextTimeout() throws IllegalStateException, NoMoreTimeoutsException, NoSuchObjectLocalException, EJBException {
        // first check the validity of the timer state
        assertTimerState(this.timer.getState());
        Date nextTimeout = this.timer.getNextExpiration();
        if (nextTimeout == null) {
            throw EjbLogger.EJB3_TIMER_LOGGER.noMoreTimeoutForTimer(this.timer);
        }
        return nextTimeout;
    }

    @Override
    public Serializable getInfo() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        // make sure this call is allowed
        assertTimerState(this.timer.getState());
        return this.timer.getState();
    }

    @Override
    public TimerHandle getHandle() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        // make sure it's in correct state
        assertTimerState(this.timer.getState());
        // for non-persistent timers throws an exception (mandated by EJB3 spec)
        if (!this.timer.isTimerPersistent()) {
            throw EjbLogger.EJB3_TIMER_LOGGER.invalidTimerHandlersForPersistentTimers("EJB3.1 Spec 18.2.6");
        }
        return this.timer.getTimerHandle();
    }

    @Override
    public ScheduleExpression getSchedule() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        // make sure the call is allowed in the current timer state
        assertTimerState(this.timer.getState());
        CalendarBasedTimeout timeout = this.timer.getCalendarTimeout();
        if (timeout == null) {
            throw EjbLogger.EJB3_TIMER_LOGGER.invalidTimerNotCalendarBaseTimer(this.timer);
        }
        return timeout.getScheduleExpression();
    }

    @Override
    public boolean isCalendarTimer() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        // first check whether this timer has expired or cancelled
        assertTimerState(this.timer.getState());
        return this.timer.getCalendarTimeout() != null;
    }

    @Override
    public boolean isPersistent() throws IllegalStateException, NoSuchObjectLocalException, EJBException {
        // make sure the call is allowed in the current timer state
        assertTimerState(this.timer.getState());
        return this.timer.isTimerPersistent();
    }

    /**
     * Asserts that the timer is <i>not</i> in any of the following states:
     * <ul>
     * <li>{@link TimerState#CANCELED}</li>
     * <li>{@link TimerState#EXPIRED}</li>
     * </ul>
     *
     * @throws javax.ejb.NoSuchObjectLocalException
     *          if the txtimer was canceled or has expired
     */
    static void assertTimerState(TimerState state) {
        switch (state) {
            case EXPIRED: {
                throw EjbLogger.EJB3_TIMER_LOGGER.timerHasExpired();
            }
            case CANCELED: {
                throw EjbLogger.EJB3_TIMER_LOGGER.timerWasCanceled();
            }
            default: {
                AllowedMethodsInformation.checkAllowed(MethodType.TIMER_SERVICE_METHOD);
            }
        }
    }
}
