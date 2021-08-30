/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * 021101301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.ejb3.timerservice;

import org.jboss.as.ee.component.Component;
import org.jboss.as.ejb3.logging.EjbLogger;
import org.jboss.as.ejb3.component.EJBComponentDescription;
import org.jboss.as.ejb3.component.allowedmethods.AllowedMethodsInformation;
import org.jboss.as.ejb3.component.allowedmethods.MethodType;
import org.jboss.as.ejb3.component.singleton.SingletonComponent;
import org.jboss.as.ejb3.context.CurrentInvocationContext;
import org.jboss.invocation.InterceptorContext;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

import javax.ejb.ScheduleExpression;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;

/**
 * Non-functional timer service that is bound when the timer service is disabled.
 */
public class NonFunctionalTimerService implements TimerFactory, Service<TimerFactory> {

    public static final NonFunctionalTimerService DISABLED = new NonFunctionalTimerService(EjbLogger.EJB3_TIMER_LOGGER.timerServiceIsNotActive(), null);

    private final String message;
    private final TimerManagerRegistry registry;

    public NonFunctionalTimerService(final String message, final TimerManagerRegistry registry) {
        this.message = message;
        this.registry = registry;
    }

    public static ServiceName serviceNameFor(final EJBComponentDescription ejbComponentDescription) {
        if (ejbComponentDescription == null || ejbComponentDescription.getServiceName() == null) {
            return null;
        }
        return ejbComponentDescription.getServiceName().append("ejb", "non-functional-timerservice");
    }

    @Override
    public Timer createTimer(Date initialExpiration, long intervalDuration, Serializable info, boolean persistent) {
        throw new IllegalStateException(message);
    }

    @Override
    public Timer createCalendarTimer(ScheduleExpression schedule, Serializable info, boolean persistent, Method timeoutMethod) {
        throw new IllegalStateException(message);
    }

    @Override
    public Timer getTimer(String id) {
        return null;
    }

    @Override
    public TimerManagerRegistry getRegistry() {
        return this.registry;
    }

    @Override
    public Collection<Timer> getTimers() {
        assertInvocationAllowed();
        return Collections.emptySet();
    }

    private static void assertInvocationAllowed() {
        AllowedMethodsInformation.checkAllowed(MethodType.TIMER_SERVICE_METHOD);
        final InterceptorContext currentInvocationContext = CurrentInvocationContext.get();
        if (currentInvocationContext == null) {
            return;
        }
        // If the method in current invocation context is null,
        // then it represents a lifecycle callback invocation
        Method invokedMethod = currentInvocationContext.getMethod();
        if (invokedMethod == null) {
            // it's a lifecycle callback
            Component component = currentInvocationContext.getPrivateData(Component.class);
            if (!(component instanceof SingletonComponent)) {
                throw EjbLogger.EJB3_TIMER_LOGGER.failToInvokeTimerServiceDoLifecycle();
            }
        }
    }

    @Override
    public void start(StartContext startContext) throws StartException {
        if (this.registry != null) {
            this.registry.registerTimerService(this);
        }
    }

    @Override
    public void stop(StopContext stopContext) {
        if (this.registry != null) {
            this.registry.unregisterTimerService(this);
        }
    }

    @Override
    public TimerFactory getValue() {
        return this;
    }
}
