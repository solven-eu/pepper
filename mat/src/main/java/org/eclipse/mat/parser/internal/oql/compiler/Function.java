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
 *    Andrew Johnson - eval method
 *******************************************************************************/
package org.eclipse.mat.parser.internal.oql.compiler;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.parser.internal.Messages;
import org.eclipse.mat.snapshot.model.IObject;
import org.eclipse.mat.snapshot.registry.ClassSpecificNameResolverRegistry;
import org.eclipse.mat.util.IProgressListener.OperationCanceledException;
import org.eclipse.mat.util.MessageUtil;

abstract class Function extends Expression {
	Expression argument;

	public Function(Expression argument) {
		this.argument = argument;
	}

	@Override
	public boolean isContextDependent(EvaluationContext ctx) {
		return this.argument.isContextDependent(ctx);
	}

	public abstract String getSymbol();

	@Override
	public String toString() {
		return getSymbol() + "(" + argument + ")";//$NON-NLS-1$//$NON-NLS-2$
	}

	static class ToHex extends Function {

		public ToHex(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (!(s instanceof Number))
				throw new SnapshotException(MessageUtil.format(Messages.Function_Error_NeedsNumberAsInput,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));

			return "0x" + Long.toHexString(((Number) s).longValue());//$NON-NLS-1$
		}

		@Override
		public String getSymbol() {
			return "toHex";//$NON-NLS-1$
		}
	}

	static class ToString extends Function {

		public ToString(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (s instanceof IObject) {
				String name = ClassSpecificNameResolverRegistry.resolve((IObject) s);
				return name != null ? name : "";//$NON-NLS-1$
			} else {
				return String.valueOf(s);
			}
		}

		@Override
		public String getSymbol() {
			return "toString";//$NON-NLS-1$
		}

	}

	static class Outbounds extends Function {

		public Outbounds(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (s instanceof IObject) {
				return ctx.getSnapshot().getOutboundReferentIds(((IObject) s).getObjectId());
			} else if (s instanceof Integer) {
				return ctx.getSnapshot().getOutboundReferentIds(((Integer) s).intValue());
			} else {
				throw new SnapshotException(MessageUtil.format(Messages.Function_ErrorNoFunction,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));
			}
		}

		@Override
		public String getSymbol() {
			return "outbounds";//$NON-NLS-1$
		}

	}

	static class Inbounds extends Function {

		public Inbounds(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (s instanceof IObject) {
				return ctx.getSnapshot().getInboundRefererIds(((IObject) s).getObjectId());
			} else if (s instanceof Integer) {
				return ctx.getSnapshot().getInboundRefererIds(((Integer) s).intValue());
			} else {
				throw new SnapshotException(MessageUtil.format(Messages.Function_ErrorNoFunction,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));
			}
		}

		@Override
		public String getSymbol() {
			return "inbounds";//$NON-NLS-1$
		}

	}

	static class Dominators extends Function {

		public Dominators(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (s instanceof IObject) {
				return ctx.getSnapshot().getImmediateDominatedIds(((IObject) s).getObjectId());
			} else if (s instanceof Integer) {
				return ctx.getSnapshot().getImmediateDominatedIds(((Integer) s).intValue());
			} else {
				throw new SnapshotException(MessageUtil.format(Messages.Function_ErrorNoFunction,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));
			}
		}

		@Override
		public String getSymbol() {
			return "dominators";//$NON-NLS-1$
		}

	}

	static class ClassOf extends Function {

		public ClassOf(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			if (s instanceof IObject) {
				return ((IObject) s).getClazz();
			} else if (s instanceof Integer) {
				return ctx.getSnapshot().getClassOf(((Integer) s).intValue());
			} else {
				throw new SnapshotException(MessageUtil.format(Messages.Function_ErrorNoFunction,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));
			}
		}

		@Override
		public String getSymbol() {
			return "classof";//$NON-NLS-1$
		}

	}

	static class DominatorOf extends Function {

		public DominatorOf(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);

			int dominatorId = -1;

			if (s instanceof IObject) {
				dominatorId = ctx.getSnapshot().getImmediateDominatorId(((IObject) s).getObjectId());
			} else if (s instanceof Integer) {
				dominatorId = ctx.getSnapshot().getImmediateDominatorId(((Integer) s).intValue());
			} else {
				throw new SnapshotException(MessageUtil.format(Messages.Function_ErrorNoFunction,
						argument,
						s,
						s != null ? s.getClass().getName() : Messages.Function_unknown,
						getSymbol()));
			}

			return dominatorId >= 0 ? ctx.getSnapshot().getObject(dominatorId) : null;
		}

		@Override
		public String getSymbol() {
			return "dominatorof";//$NON-NLS-1$
		}

	}

	/**
	 * Can be used to allow array and method access to expressions or select results.
	 */
	static class Eval extends Function {
		public Eval(Expression argument) {
			super(argument);
		}

		@Override
		public Object compute(EvaluationContext ctx) throws SnapshotException, OperationCanceledException {
			Object s = this.argument.compute(ctx);
			return s;
		}

		@Override
		public String getSymbol() {
			return "eval";//$NON-NLS-1$
		}
	}
}
