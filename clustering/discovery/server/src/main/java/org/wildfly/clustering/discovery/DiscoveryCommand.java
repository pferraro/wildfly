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

package org.wildfly.clustering.discovery;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.dispatcher.Command;
import org.wildfly.clustering.marshalling.Externalizer;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryRequest;
import org.wildfly.discovery.spi.DiscoveryResult;

/**
 * Executes a discovery request on a {@link DiscoveryProvider}.
 * @author Paul Ferraro
 */
public class DiscoveryCommand implements Command<Iterable<ServiceURL>, DiscoveryProvider> {
    private static final long serialVersionUID = -2058174159716521380L;

    private final ServiceType serviceType;
    private final FilterSpec filterSpec;

    public DiscoveryCommand(ServiceType serviceType, FilterSpec filterSpec) {
        this.serviceType = serviceType;
        this.filterSpec = filterSpec;
    }

    ServiceType getServiceType() {
        return this.serviceType;
    }

    FilterSpec getFilterSpec() {
        return this.filterSpec;
    }

    @Override
    public Iterable<ServiceURL> execute(DiscoveryProvider provider) {
        CompletableFuture<Iterable<ServiceURL>> future = new CompletableFuture<>();
        FutureDiscoveryResult result = new FutureDiscoveryResult(future);
        DiscoveryRequest request = provider.discover(this.serviceType, this.filterSpec, result);
        try {
            return future.join();
        } catch (RuntimeException | Error e) {
            e.printStackTrace();
            throw e;
        } finally {
            request.cancel();
        }
    }

    private static class FutureDiscoveryResult implements DiscoveryResult {
        private final CompletableFuture<Iterable<ServiceURL>> future;
        private final Collection<ServiceURL> urls = new LinkedBlockingQueue<>();

        FutureDiscoveryResult(CompletableFuture<Iterable<ServiceURL>> future) {
            this.future = future;
        }

        @Override
        public void complete() {
            this.future.complete(this.urls);
        }

        @Override
        public void reportProblem(Throwable cause) {
            this.future.completeExceptionally(cause);
        }

        @Override
        public void addMatch(ServiceURL url) {
            if (!this.future.isDone()) {
                this.urls.add(url);
            }
        }
    }

    @MetaInfServices(Externalizer.class)
    public static class DiscoveryCommandExternalizer implements Externalizer<DiscoveryCommand> {

        @Override
        public void writeObject(ObjectOutput output, DiscoveryCommand command) throws IOException {
            ServiceTypeSerializer.INSTANCE.write(output, command.getServiceType());
            FilterSpecSerializer.INSTANCE.write(output, command.getFilterSpec());
        }

        @Override
        public DiscoveryCommand readObject(ObjectInput input) throws IOException {
            ServiceType type = ServiceTypeSerializer.INSTANCE.read(input);
            FilterSpec filter = FilterSpecSerializer.INSTANCE.read(input);
            return new DiscoveryCommand(type, filter);
        }

        @Override
        public Class<DiscoveryCommand> getTargetClass() {
            return DiscoveryCommand.class;
        }
    }
}
