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

package org.jboss.as.test.clustering.cluster.discovery;

import static org.jboss.as.test.clustering.cluster.discovery.service.DistributedDiscoveryServiceActivator.*;

import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.as.server.security.ServerPermission;
import org.jboss.as.test.clustering.CLIServerSetupTask;
import org.jboss.as.test.clustering.cluster.AbstractClusteringTestCase;
import org.jboss.as.test.clustering.cluster.discovery.service.DiscoveryServiceActivator;
import org.jboss.as.test.clustering.cluster.discovery.service.DistributedDiscoveryServiceActivator;
import org.jboss.as.test.clustering.cluster.discovery.servlet.DiscoveryServlet;
import org.jboss.as.test.http.util.TestHttpClientUtils;
import org.jboss.as.test.shared.integration.ejb.security.PermissionUtils;
import org.jboss.msc.service.ServiceActivator;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Paul Ferraro
 */
@RunWith(Arquillian.class)
@ServerSetup(DistributedDiscoveryProviderTestCase.ServerSetupTask.class)
public class DistributedDiscoveryProviderTestCase extends AbstractClusteringTestCase {

    public static class ServerSetupTask extends CLIServerSetupTask {
        public ServerSetupTask() {
            this.builder.node(TWO_NODES)
                    .setup("/subsystem=discovery/local-provider=%s:add()", REGISTRY_PROVIDER_NAME)
                    .setup("/subsystem=discovery/distributed-provider=%s:add(provider=%s)", DISCOVERY_PROVIDER_NAME, REGISTRY_PROVIDER_NAME)
                    .teardown("/subsystem=discovery/distributed-provider=%s:remove()", DISCOVERY_PROVIDER_NAME)
                    .teardown("/subsystem=discovery/local-provider=%s:remove()", REGISTRY_PROVIDER_NAME)
                    ;
        }
    }

    @Deployment(name = DEPLOYMENT_1, managed = false, testable = false)
    @TargetsContainer(NODE_1)
    public static Archive<?> deployment1() {
        return getDeployment();
    }

    @Deployment(name = DEPLOYMENT_2, managed = false, testable = false)
    @TargetsContainer(NODE_2)
    public static Archive<?> deployment2() {
        return getDeployment();
    }

    private static Archive<?> getDeployment() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, DistributedDiscoveryProviderTestCase.class.getSimpleName() + ".war");
        archive.addClasses(DiscoveryServlet.class, DistributedDiscoveryServiceActivator.class, DiscoveryServiceActivator.class);
        archive.setManifest(new StringAsset("Manifest-Version: 1.0\nDependencies: org.jboss.as.server, org.jboss.as.controller, org.jboss.as.clustering.common, org.wildfly.clustering.service, org.wildfly.client.config, org.wildfly.common, org.wildfly.discovery, org.wildfly.clustering.discovery.spi\n"));
        archive.addAsManifestResource(PermissionUtils.createPermissionsXmlAsset(new ServerPermission("getCurrentServiceContainer")), "permissions.xml");
        archive.addAsServiceProvider(ServiceActivator.class, DistributedDiscoveryServiceActivator.class);
        return archive;
    }

    @Test
    public void test(
            @ArquillianResource(DiscoveryServlet.class) @OperateOnDeployment(DEPLOYMENT_1) URL baseURL1,
            @ArquillianResource(DiscoveryServlet.class) @OperateOnDeployment(DEPLOYMENT_2) URL baseURL2) throws Exception {

        URI registerURI1 = DiscoveryServlet.createURI(baseURL1, REGISTRY_PROVIDER_NAME, baseURL1);
        URI registerURI2 = DiscoveryServlet.createURI(baseURL2, REGISTRY_PROVIDER_NAME, baseURL2);

        URI discoverURI1 = DiscoveryServlet.createURI(baseURL1, DISCOVERY_PROVIDER_NAME);
        URI discoverURI2 = DiscoveryServlet.createURI(baseURL2, DISCOVERY_PROVIDER_NAME);

        try (CloseableHttpClient client = TestHttpClientUtils.promiscuousCookieHttpClient()) {
            for (URI uri : new URI[] { registerURI1, registerURI2 }) {
                HttpResponse response = client.execute(new HttpGet(uri));
                Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());
            }

            Set<URI> expected = new TreeSet<>();
            expected.add(baseURL1.toURI());
            expected.add(baseURL2.toURI());

            for (URI uri : new URI[] { discoverURI1, discoverURI2 }) {
                HttpResponse response = client.execute(new HttpGet(uri));
                Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatusLine().getStatusCode());

                Header[] headers = response.getHeaders(DiscoveryServlet.SERVICE_URI);
                Assert.assertEquals(2, headers.length);
                Set<URI> discovered = Stream.of(headers).map(Header::getValue).map(URI::create).collect(Collectors.toSet());
                Assert.assertEquals(expected, discovered);
            }
        }
    }
}
