/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extension.clustering.discovery;

import java.util.EnumSet;

import org.jboss.as.clustering.subsystem.AdditionalInitialization;
import org.jboss.as.clustering.subsystem.ClusteringSubsystemTest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.wildfly.clustering.spi.ClusteringCacheRequirement;
import org.wildfly.clustering.spi.ClusteringDefaultCacheRequirement;
import org.wildfly.clustering.spi.ClusteringDefaultRequirement;
import org.wildfly.clustering.spi.ClusteringRequirement;

/**
 * Unit test for discovery subsystem
 * @author Paul Ferraro
 */
@RunWith(value = Parameterized.class)
public class DiscoverySubsystemTestCase extends ClusteringSubsystemTest<DiscoverySchema> {

    @Parameters
    public static Iterable<DiscoverySchema> parameters() {
        return EnumSet.allOf(DiscoverySchema.class);
    }

    public DiscoverySubsystemTestCase(DiscoverySchema schema) {
        super(DiscoveryExtension.SUBSYSTEM_NAME, new DiscoveryExtension(), schema, DiscoverySchema.CURRENT, "wildfly-discovery-%d_%d.xml", "schema/wildfly-discovery_%d_%d.xsd");
    }

    @Override
    protected org.jboss.as.subsystem.test.AdditionalInitialization createAdditionalInitialization() {
        return new AdditionalInitialization()
                .require(ClusteringDefaultCacheRequirement.SERVICE_PROVIDER_REGISTRY, "foo")
                .require(ClusteringCacheRequirement.SERVICE_PROVIDER_REGISTRY, "foo", "bar")
                .require(ClusteringDefaultRequirement.COMMAND_DISPATCHER_FACTORY)
                .require(ClusteringRequirement.COMMAND_DISPATCHER_FACTORY, "foo")
                ;
    }
}