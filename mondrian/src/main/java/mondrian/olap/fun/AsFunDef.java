/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package mondrian.olap.fun;

import mondrian.calc.*;
import mondrian.calc.impl.AbstractIterCalc;
import mondrian.mdx.NamedSetExpr;
import mondrian.mdx.ResolvedFunCall;
import mondrian.olap.*;

import java.util.List;


/**
 * Definition of the <code>AS</code> MDX operator.
 *
 * <p>Using <code>AS</code>, you can define an alias for an MDX expression
 * anywhere it appears in a query, and use that alias as you would a calculated
 * yet.
 *
 * @author jhyde
 * @since Oct 7, 2009
 */
class AsFunDef extends FunDefBase {
    public static final Resolver RESOLVER = new ResolverImpl();
    private final Query.ScopedNamedSet scopedNamedSet;

    /**
     * Creates an AsFunDef.
     *
     * @param scopedNamedSet Named set definition
     */
    private AsFunDef(Query.ScopedNamedSet scopedNamedSet) {
        super(
            "AS",
            "<Expression> AS <Name>",
            "Assigns an alias to an expression",
            "ixxn");
        this.scopedNamedSet = scopedNamedSet;
    }

    public Calc compileCall(ResolvedFunCall call, ExpCompiler compiler) {
        // Argument 0, the definition of the set, has been resolved since the
        // scoped named set was created. Implicit conversions, like converting
        // a member to a set, have been performed. Use the new expression.
        scopedNamedSet.setExp(call.getArg(0));

        return new AbstractIterCalc(call, new Calc[0]) {
            public TupleIterable evaluateIterable(
                Evaluator evaluator)
            {
                final Evaluator.NamedSetEvaluator namedSetEvaluator =
                    evaluator.getNamedSetEvaluator(scopedNamedSet, false);
                return namedSetEvaluator.evaluateTupleIterable(evaluator);
            }
        };
    }

    private static class ResolverImpl extends ResolverBase {
        public ResolverImpl() {
            super("AS", null, null, Syntax.Infix);
        }

        public FunDef resolve(
            Exp[] args,
            Validator validator,
            List<Conversion> conversions)
        {
            final Exp exp = args[0];
            if (!validator.canConvert(
                    0, args[0], Category.Set, conversions))
            {
                return null;
            }

            // By the time resolve is called, the id argument has already been
            // resolved... to a named set, namely itself. That's not pretty.
            // We'd rather it stayed as an id, and we'd rather that a named set
            // was not visible in the scope that defines it. But we can work
            // with this.
            final String name =
                ((NamedSetExpr) args[1]).getNamedSet().getName();

            final Query.ScopedNamedSet scopedNamedSet =
                (Query.ScopedNamedSet) ((NamedSetExpr) args[1]).getNamedSet();
//                validator.getQuery().createScopedNamedSet(
//                    name, (QueryPart) exp, exp);
            return new AsFunDef(scopedNamedSet);
        }
    }
}

// End AsFunDef.java
