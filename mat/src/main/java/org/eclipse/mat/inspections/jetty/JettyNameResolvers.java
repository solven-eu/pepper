/*******************************************************************************
 * Copyright (c) 2008, 2009 SAP AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *******************************************************************************/
package org.eclipse.mat.inspections.jetty;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.internal.Messages;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.extension.Subject;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.MessageUtil;

public class JettyNameResolvers {

	@Subject("org.mortbay.jetty.webapp.WebAppClassLoader")
	public static class WebAppClassLoaderResolver implements IClassSpecificNameResolver {
		public String resolve(IObject object) throws SnapshotException {
			IObject name = (IObject) object.resolveValue("_name"); //$NON-NLS-1$
			if (name != null)
				return name.getClassSpecificName();

			IObject contextPath = (IObject) object.resolveValue("_context._contextPath"); //$NON-NLS-1$
			return contextPath != null ? contextPath.getClassSpecificName() : null;
		}
	}

	@Subject("org.apache.jasper.servlet.JasperLoader")
	public static class JasperLoaderResolver implements IClassSpecificNameResolver {
		public String resolve(IObject object) throws SnapshotException {
			IObject parent = (IObject) object.resolveValue("parent"); //$NON-NLS-1$
			return parent != null
					? MessageUtil.format(Messages.JettyNameResolvers_JSPofWebApp, parent.getClassSpecificName())
					: null;
		}
	}
}
