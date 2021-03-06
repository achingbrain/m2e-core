/*******************************************************************************
 * Copyright (c) 2008-2010 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      Sonatype, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.m2e.core.internal.embedder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import org.eclipse.m2e.core.internal.IMavenConstants;


/**
 * A custom Guice module that picks the components contributed by extensions.
 */
class ExtensionModule extends AbstractModule implements IMavenComponentContributor.IMavenComponentBinder {
  private static final Logger log = LoggerFactory.getLogger(ExtensionModule.class);

  public <T> void bind(Class<T> role, Class<? extends T> impl, String hint) {
    if(hint == null || hint.length() <= 0 || "default".equals(hint)) { //$NON-NLS-1$
      bind(role).to(impl);
    } else {
      bind(role).annotatedWith(Names.named(hint)).to(impl);
    }
  }

  protected void configure() {
    IExtensionRegistry r = Platform.getExtensionRegistry();
    for(IConfigurationElement c : r.getConfigurationElementsFor(IMavenConstants.MAVEN_COMPONENT_CONTRIBUTORS_XPT)) {
      if("configurator".equals(c.getName())) { //$NON-NLS-1$
        try {
          IMavenComponentContributor contributor = (IMavenComponentContributor) c.createExecutableExtension("class"); //$NON-NLS-1$
          contributor.contribute(this);
        } catch(CoreException ex) {
          log.error(ex.getMessage(), ex);
        }
      }
    }
  }
}
