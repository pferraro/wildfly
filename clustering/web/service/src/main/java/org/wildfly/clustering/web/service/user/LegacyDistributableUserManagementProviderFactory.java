/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.wildfly.clustering.web.service.user;

/**
 * @author Paul Ferraro
 */
public interface LegacyDistributableUserManagementProviderFactory {
    DistributableUserManagementProvider createUserManagementProvider();
}
