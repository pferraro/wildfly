/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
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

package org.wildfly.clustering.discovery;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import org.wildfly.clustering.provider.ServiceProviderRegistry;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceRegistration;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryRequest;
import org.wildfly.discovery.spi.DiscoveryResult;
import org.wildfly.discovery.spi.RegistryProvider;

/**
 * {@link DiscoveryProvider} and {@link RegistryProvider} based on a {@link ServiceProviderRegistry}.
 * @author Paul Ferraro
 */
public class ServiceProviderRegistryDiscoveryProvider implements DiscoveryProvider, RegistryProvider {

    private final ServiceProviderRegistry<ServiceURL> registry;
    private final Executor executor;

    public ServiceProviderRegistryDiscoveryProvider(ServiceProviderRegistry<ServiceURL> registry, Executor executor) {
        this.registry = registry;
        this.executor = executor;
    }

    @Override
    public DiscoveryRequest discover(ServiceType type, FilterSpec filterSpec, DiscoveryResult result) {
        ServiceProviderRegistry<ServiceURL> registry = this.registry;
        DiscoveryResultCompletionHandler handler = new DiscoveryResultCompletionHandler(result);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                // Leverage Infinispan distributed streams
                try (Stream<ServiceURL> services = registry.getServices().stream().onClose(handler)) {
                    // Filter and terminal operation are invoked remotely and must be marshallable
                    for (ServiceURL url : services.filter(new ServiceFilter(type, filterSpec)).toArray(ServiceURLArrayFactory.INSTANCE)) {
                        result.addMatch(url);
                    }
                } catch (RuntimeException e) {
                    result.reportProblem(e);
                }
            }
        };
        return new FutureDiscoveryRequest(CompletableFuture.runAsync(task, this.executor));
    }

    @Override
    public ServiceRegistration registerService(ServiceURL service) {
        return new DistributableRegistration(this.registry.register(service));
    }

    private static class DiscoveryResultCompletionHandler implements Runnable {
        private final DiscoveryResult result;

        DiscoveryResultCompletionHandler(DiscoveryResult result) {
            this.result = result;
        }

        @Override
        public void run() {
            this.result.complete();
        }
    }

    private static class FutureDiscoveryRequest implements DiscoveryRequest {
        private final Future<?> future;

        FutureDiscoveryRequest(Future<?> future) {
            this.future = future;
        }

        @Override
        public void cancel() {
            this.future.cancel(true);
        }
    }
}
