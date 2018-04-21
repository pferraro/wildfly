/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat, Inc., and individual contributors
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

import org.junit.Assert;
import org.junit.Test;
import org.wildfly.clustering.discovery.ServiceFilter.ServiceFilterExternalizer;
import org.wildfly.clustering.marshalling.ExternalizerTester;
import org.wildfly.discovery.FilterSpec;
import org.wildfly.discovery.ServiceType;

/**
 * @author Paul Ferraro
 */
public class ServiceFilterExternalizerTestCase {
    @Test
    public void test() throws ClassNotFoundException, IOException {
        ExternalizerTester<ServiceFilter> tester = new ExternalizerTester<>(new ServiceFilterExternalizer(), ServiceFilterExternalizerTestCase::assertEquals);
        tester.test(new ServiceFilter(ServiceType.of("type", "auth"), null));
        tester.test(new ServiceFilter(ServiceType.of("abstract-type", "abstract-auth", "concrete-type", "concrete-auth"), FilterSpec.equal("attr", "value")));
    }

    static void assertEquals(ServiceFilter filter1, ServiceFilter filter2) {
        Assert.assertEquals(filter1.getServiceType(), filter2.getServiceType());
        Assert.assertEquals((filter1.getFilterSpec() != null) ? filter1.getFilterSpec().toString() : null, (filter2.getFilterSpec() != null) ? filter2.getFilterSpec().toString() : null);
    }
}
