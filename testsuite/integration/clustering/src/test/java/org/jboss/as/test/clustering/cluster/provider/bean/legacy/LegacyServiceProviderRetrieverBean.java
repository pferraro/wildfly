/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.as.test.clustering.cluster.provider.bean.legacy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.ejb.EJB;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;

import org.jboss.as.test.clustering.cluster.provider.bean.ServiceProviderRetriever;
import org.wildfly.clustering.group.Node;
import org.wildfly.clustering.provider.ServiceProviderRegistration;

@Stateless
@Remote(ServiceProviderRetriever.class)
public class LegacyServiceProviderRetrieverBean implements ServiceProviderRetriever {

    @EJB
    private ServiceProviderRegistration<String> registration;

    @Override
    public Collection<String> getProviders() {
        Set<Node> nodes = this.registration.getProviders();
        List<String> result = new ArrayList<>(nodes.size());
        for (Node node: nodes) {
            result.add(node.getName());
        }
        return result;
    }
}
