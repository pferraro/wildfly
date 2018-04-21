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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;

import org.wildfly.clustering.dispatcher.CommandDispatcher;
import org.wildfly.clustering.dispatcher.CommandDispatcherException;
import org.wildfly.clustering.group.Node;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;
import org.wildfly.discovery.spi.DiscoveryProvider;
import org.wildfly.discovery.spi.DiscoveryRequest;
import org.wildfly.discovery.spi.DiscoveryResult;

/**
 * @author Paul Ferraro
 */
public class CommandDispatcherDiscoveryProvider implements DiscoveryProvider {

    private final CommandDispatcher<DiscoveryProvider> dispatcher;

    public CommandDispatcherDiscoveryProvider(CommandDispatcher<DiscoveryProvider> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public DiscoveryRequest discover(ServiceType serviceType, FilterSpec filterSpec, DiscoveryResult result) {
        try {
            Map<Node, CompletionStage<Iterable<ServiceURL>>> responses = this.dispatcher.executeOnGroup(new DiscoveryCommand(serviceType, filterSpec));
            BiConsumer<Iterable<ServiceURL>, Throwable> handler = new DiscoveryCompletionHandler(result);
            List<CompletionStage<?>> stages = new ArrayList<>(responses.size());
            for (CompletionStage<Iterable<ServiceURL>> response : responses.values()) {
                stages.add(response.whenComplete(handler));
            }
            CompletableFuture.allOf(stages.toArray(new CompletableFuture<?>[responses.size()])).whenComplete(new DiscoveryResultCompletionHandler(result));
            return new DistributedDiscoveryRequest(responses.values());
        } catch (CommandDispatcherException e) {
            result.reportProblem(e);
            return DiscoveryRequest.NULL;
        }
    }

    private static class DiscoveryCompletionHandler implements BiConsumer<Iterable<ServiceURL>, Throwable> {
        private final DiscoveryResult result;

        DiscoveryCompletionHandler(DiscoveryResult result) {
            this.result = result;
        }

        @Override
        public void accept(Iterable<ServiceURL> urls, Throwable exception) {
            if (exception == null) {
                for (ServiceURL url : urls) {
                    this.result.addMatch(url);
                }
            } else if (!(exception instanceof CancellationException)) {
                this.result.reportProblem(exception);
            }
        }
    }

    private static class DiscoveryResultCompletionHandler implements BiConsumer<Void, Throwable> {
        private final DiscoveryResult result;

        DiscoveryResultCompletionHandler(DiscoveryResult result) {
            this.result = result;
        }

        @Override
        public void accept(Void result, Throwable exception) {
            this.result.complete();
        }
    }

    private static class DistributedDiscoveryRequest implements DiscoveryRequest {
        private final Collection<CompletionStage<Iterable<ServiceURL>>> responses;

        DistributedDiscoveryRequest(Collection<CompletionStage<Iterable<ServiceURL>>> responses) {
            this.responses = responses;
        }

        @Override
        public void cancel() {
            for (CompletionStage<?> response : this.responses) {
                response.toCompletableFuture().cancel(true);
            }
        }
    }
}
