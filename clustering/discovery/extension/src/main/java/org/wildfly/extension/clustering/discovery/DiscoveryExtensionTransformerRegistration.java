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

package org.wildfly.extension.clustering.discovery;

import static org.jboss.as.controller.transform.description.TransformationDescription.Tools.register;

import java.util.EnumSet;

import org.jboss.as.controller.ModelVersion;
import org.jboss.as.controller.transform.ExtensionTransformerRegistration;
import org.jboss.as.controller.transform.SubsystemTransformerRegistration;
import org.kohsuke.MetaInfServices;

/**
 * @author Paul Ferraro
 */
@MetaInfServices(ExtensionTransformerRegistration.class)
public class DiscoveryExtensionTransformerRegistration implements ExtensionTransformerRegistration {

    @Override
    public String getSubsystemName() {
        return DiscoveryExtension.SUBSYSTEM_NAME;
    }

    @Override
    public void registerTransformers(SubsystemTransformerRegistration registration) {
        for (DiscoveryModel model : EnumSet.complementOf(EnumSet.of(DiscoveryModel.CURRENT))) {
            ModelVersion version = model.getVersion();
            register(DiscoveryResourceDefinition.buildTransformers(version), registration, version);
        }
    }
}
