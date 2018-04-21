/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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

package org.wildfly.extension.clustering.discovery;

import java.security.PrivilegedAction;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import org.jboss.as.clustering.controller.ResourceServiceHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.threads.JBossThreadFactory;
import org.wildfly.clustering.discovery.ServiceProviderRegistryDiscoveryProvider;
import org.wildfly.clustering.service.Builder;
import org.wildfly.clustering.service.concurrent.CachedThreadPoolExecutorServiceBuilder;
import org.wildfly.clustering.service.concurrent.ClassLoaderThreadFactory;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 * Service handler for a distributed registry provider resource.
 * @author Paul Ferraro
 */
public class DistributedRegistryProviderServiceHandler implements ResourceServiceHandler, PrivilegedAction<ThreadFactory> {

    @Override
    public ThreadFactory run() {
        return new ClassLoaderThreadFactory(new JBossThreadFactory(new ThreadGroup(ServiceProviderRegistryDiscoveryProvider.class.getSimpleName()), Boolean.FALSE, null, "%G - %t", null, null), ServiceProviderRegistryDiscoveryProvider.class.getClassLoader());
    }

    @Override
    public void installServices(OperationContext context, ModelNode model) throws OperationFailedException {
        ServiceTarget target = context.getServiceTarget();

        DistributedRegistryProviderBuilder builder = new DistributedRegistryProviderBuilder(context.getCurrentAddress()).configure(context, model);
        builder.build(target).install();

        Builder<Executor> executorBuilder = new CachedThreadPoolExecutorServiceBuilder(builder.getExecutorServiceName(), WildFlySecurityManager.doUnchecked(this));
        executorBuilder.build(target).install();
    }

    @Override
    public void removeServices(OperationContext context, ModelNode model) throws OperationFailedException {
        DistributedRegistryProviderBuilder builder = new DistributedRegistryProviderBuilder(context.getCurrentAddress());
        context.removeService(builder.getServiceName());
        context.removeService(builder.getExecutorServiceName());
    }
}
