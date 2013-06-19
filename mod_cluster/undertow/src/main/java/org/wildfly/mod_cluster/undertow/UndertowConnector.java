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

package org.wildfly.mod_cluster.undertow;

import java.net.InetAddress;

import org.jboss.modcluster.container.Connector;
import org.wildfly.extension.undertow.AbstractListenerService;
import org.wildfly.extension.undertow.AjpListenerService;
import org.wildfly.extension.undertow.HttpListenerService;

/**
 * Adapts {@link AbstractListenerService} to a {@link Connector}.
 *
 * @author Radoslav Husar
 * @since 8.0
 */
public class UndertowConnector implements Connector {

    private AbstractListenerService<?> listener;

    public UndertowConnector(AbstractListenerService<?> listener) {
        this.listener = listener;
    }

    @Override
    public boolean isReverse() {
        return true;
    }

    @Override
    public Type getType() {
        if (this.listener instanceof AjpListenerService) {
            return Type.AJP;
        } else if (this.listener instanceof HttpListenerService) {
            if (this.listener.isSecure()) {
                return Type.HTTPS;
            } else {
                return Type.HTTP;
            }
        }
        return null;
    }

    @Override
    public InetAddress getAddress() {
        return this.listener.getBinding().getValue().getAddress();
    }

    @Override
    public void setAddress(InetAddress address) {
        // Do nothing
    }

    @Override
    public int getPort() {
        return this.listener.getBinding().getValue().getPort();
    }

    @Override
    public boolean isAvailable() {
        return !this.listener.getWorker().getValue().isShutdown();
    }

    @Override
    public int getMaxThreads() {
        return this.listener.getWorker().getValue().getIoThreadCount();
    }

    @Override
    public int getBusyThreads() {
        // TODO -- ongoing discussion with David
        return 0;
    }

    @Override
    public long getBytesSent() {
        // TODO -- ongoing discussion with David
        return 0;
    }

    @Override
    public long getBytesReceived() {
        // TODO -- ongoing discussion with David
        return 0;
    }

    @Override
    public long getRequestCount() {
        // TODO -- ongoing discussion with David
        return 0;
    }

    @Override
    public String toString() {
        return this.listener.getName();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UndertowConnector)) return false;

        UndertowConnector connector = (UndertowConnector) object;
        return this.listener.getName().equals(connector.listener.getName());
    }

    @Override
    public int hashCode() {
        return this.listener.getName().hashCode();
    }
}
