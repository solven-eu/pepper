/**
 * The MIT License
 * Copyright (c) 2008-2012 Benoit Lacelle - SOLVEN
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.eclipse.mat.snapshot.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.inspections.CommonNameResolver;
import org.eclipse.mat.internal.Messages;
import org.eclipse.mat.snapshot.extension.IClassSpecificNameResolver;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.util.MessageUtil;

/**
 * Registry for name resolvers which resolve the names for objects of specific classes (found in an snapshot), e.g.
 * String (where the char[] is evaluated) or a specific class loader (where the appropriate field holding its name and
 * thereby deployment unit is evaluated).
 */
public final class ClassSpecificNameResolverRegistry {
	// //////////////////////////////////////////////////////////////
	// Singleton
	// //////////////////////////////////////////////////////////////

	/** inner class because RegistryReader API not public */
	private static class RegistryImpl {
		// For registerResolver()
		private Map<String, IClassSpecificNameResolver> resolvers;

		public RegistryImpl() {
			// For registerResolver()
			resolvers = new HashMap<String, IClassSpecificNameResolver>();
		}

		private String doResolve(IObject object) {
			try {
				IClass clazz = object.getClazz();
				while (clazz != null) {
					// For registerResolver()
					IClassSpecificNameResolver resolver = resolvers.get(clazz.getName());
					if (resolver != null) {
						return resolver.resolve(object);
					}

					resolver = lookup(clazz.getName());
					if (resolver != null) {
						return resolver.resolve(object);
					}
					clazz = clazz.getSuperClass();
				}
				return null;
			} catch (RuntimeException e) {
				Logger.getLogger(ClassSpecificNameResolverRegistry.class.getName())
						.log(Level.SEVERE,
								MessageUtil.format(Messages.ClassSpecificNameResolverRegistry_ErrorMsg_DuringResolving,
										object.getTechnicalName()),
								e);
				return null;
			} catch (SnapshotException e) {
				Logger.getLogger(ClassSpecificNameResolverRegistry.class.getName())
						.log(Level.SEVERE,
								MessageUtil.format(Messages.ClassSpecificNameResolverRegistry_ErrorMsg_DuringResolving,
										object.getTechnicalName()),
								e);
				return null;
			}
		}

		private IClassSpecificNameResolver lookup(String name) {
			// TODO We have deactivate the plugins mechanism
			if (String.class.getName().equals(name)) {
				return new CommonNameResolver.StringResolver();
			} else {
				throw new UnsupportedOperationException("Apex MAT: TODO");
			}
		}

	}

	private static ClassSpecificNameResolverRegistry instance = new ClassSpecificNameResolverRegistry();

	public static ClassSpecificNameResolverRegistry instance() {
		return instance;
	}

	// //////////////////////////////////////////////////////////////
	// registry methods
	// //////////////////////////////////////////////////////////////

	private RegistryImpl registry;

	private ClassSpecificNameResolverRegistry() {
		registry = new RegistryImpl();
	}

	/**
	 * Register class specific name resolver.
	 *
	 * @param className
	 *            class name for which the class specific name resolver should be used
	 * @param resolver
	 *            class specific name resolver
	 * @deprecated Use default extension mechanism: just implement interface and register location via UI
	 */
	@Deprecated
	public static void registerResolver(String className, IClassSpecificNameResolver resolver) {
		instance().registry.resolvers.put(className, resolver);
	}

	/**
	 * Resolve name of the given snapshot object or return null if it can't be resolved.
	 *
	 * @param object
	 *            snapshot object for which the name should be resolved
	 * @return name of the given snapshot object or null if it can't be resolved
	 */
	public static String resolve(IObject object) {
		if (object == null)
			throw new NullPointerException(Messages.ClassSpecificNameResolverRegistry_Error_MissingObject);

		return instance().registry.doResolve(object);
	}

}