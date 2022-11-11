/*******************************************************************************
 * Copyright (c) 2008, 2014 SAP AG and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    SAP AG - initial API and implementation
 *    Andrew Johnson - array access
 *******************************************************************************/
package org.eclipse.mat.parser.internal.oql;

import java.util.List;

import org.eclipse.mat.parser.internal.oql.compiler.Expression;
import org.eclipse.mat.parser.internal.oql.compiler.Query;

public interface ICompiler {
	Object and(Object arguments[]);

	Object or(Object arguments[]);

	Object equal(Object left, Object right);

	Object notEqual(Object left, Object right);

	Object lessThan(Object left, Object right);

	Object lessThanOrEqual(Object left, Object right);

	Object greaterThan(Object left, Object right);

	Object greaterThanOrEqual(Object left, Object right);

	Object like(Object ex, String regex);

	Object notLike(Object ex, String regex);

	Object instanceOf(Object left, String className);

	Object in(Object left, Object right);

	Object notIn(Object left, Object right);

	Object literal(Object object);

	Object nullLiteral();

	Object path(List<Object> attributes);

	Object method(String name, List<Expression> parameters, boolean isFirstInPath);

	Object subQuery(Query q);

	Object plus(Object left, Object right);

	Object minus(Object left, Object right);

	Object multiply(Object left, Object right);

	Object divide(Object left, Object right);

	Object array(Object index);

	Object array(Object index, Object index2);
}
