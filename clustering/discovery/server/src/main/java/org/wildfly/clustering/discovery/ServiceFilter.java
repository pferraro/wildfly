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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.function.Predicate;

import org.kohsuke.MetaInfServices;
import org.wildfly.clustering.marshalling.Externalizer;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceType;
import org.wildfly.discovery.ServiceURL;

/**
 * @author Paul Ferraro
 */
public class ServiceFilter implements Predicate<ServiceURL>, Serializable {
    private static final long serialVersionUID = 4138458491824268009L;

    private final ServiceType type;
    private final FilterSpec filter;

    public ServiceFilter(ServiceType type, FilterSpec filter) {
        this.type = type;
        this.filter = filter;
    }

    ServiceType getServiceType() {
        return this.type;
    }

    FilterSpec getFilterSpec() {
        return this.filter;
    }

    @Override
    public boolean test(ServiceURL service) {
        if (this.type.implies(service) && ((this.filter == null) || service.satisfies(this.filter))) {
            System.out.println(service + " does not imply " + this.type + " or does not satisfy " + this.filter);
        }
        return this.type.implies(service) && ((this.filter == null) || service.satisfies(this.filter));
    }

    @MetaInfServices(Externalizer.class)
    public static class ServiceFilterExternalizer implements Externalizer<ServiceFilter> {

        @Override
        public void writeObject(ObjectOutput output, ServiceFilter filter) throws IOException {
            ServiceTypeSerializer.INSTANCE.write(output, filter.getServiceType());
            FilterSpecSerializer.INSTANCE.write(output, filter.getFilterSpec());
        }

        @Override
        public ServiceFilter readObject(ObjectInput input) throws IOException, ClassNotFoundException {
            ServiceType type = ServiceTypeSerializer.INSTANCE.read(input);
            FilterSpec filter = FilterSpecSerializer.INSTANCE.read(input);
            return new ServiceFilter(type, filter);
        }

        @Override
        public Class<ServiceFilter> getTargetClass() {
            return ServiceFilter.class;
        }
    }
}
