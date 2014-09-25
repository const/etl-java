/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2013 Constantine A Plotnikov
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.sf.etl.parsers.event.grammar.impl.flattened;

import net.sf.etl.parsers.event.unstable.model.grammar.Associativity;
import net.sf.etl.parsers.event.unstable.model.grammar.BlankSyntaxStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Let;
import net.sf.etl.parsers.event.unstable.model.grammar.ObjectOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperandOp;
import net.sf.etl.parsers.event.unstable.model.grammar.OperatorDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.RefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxStatement;
import net.sf.etl.parsers.literals.LiteralUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A view for operator definitions.
 *
 * @author const
 */
public final class OpDefinitionView extends ObjectDefinitionView {
    /**
     * The map from context to operator information.
     */
    private HashMap<ContextView, OpInfo> opInfo = new HashMap<ContextView, OpInfo>();

    /**
     * The constructor.
     *
     * @param context    the context of view
     * @param definition the wrapped definition
     */
    public OpDefinitionView(final ContextView context, final OperatorDefinition definition) {
        super(context, definition);
    }

    /**
     * The constructor.
     *
     * @param context    the context of view
     * @param definition the referenced definition
     */
    public OpDefinitionView(final ContextView context, final OpDefinitionView definition) {
        super(context, definition);
    }

    /**
     * @return the precedence of the operator
     */
    public Integer precedence() {
        final Integer rc = operator().getPrecedence() == null ? null
                : LiteralUtils.parseInt(operator().getPrecedence().text());
        return rc == null ? 0 : rc;
    }

    /**
     * @return the definition from grammar.
     */
    public OperatorDefinition operator() {
        return (OperatorDefinition) super.definition();
    }

    /**
     * @return the operator associativity
     */
    public Associativity associativity() {
        final OperatorDefinition od = operator();
        return od.getAssociativity() == null ? null : Associativity.valueOf(od.getAssociativity().text().toUpperCase());
    }

    /**
     * @param context the grammar context
     * @return the right argument property or null if it is not defined
     */
    public PropertyInfo getRightArgProperty(final ContextView context) {
        OpInfo op = parseDefinition(context);
        return op.rightProperty;
    }

    /**
     * @param context the grammar context
     * @return the left argument property or null if it is not defined
     */
    public PropertyInfo getLeftArgProperty(final ContextView context) {
        OpInfo op = parseDefinition(context);
        return op.leftProperty;
    }

    /**
     * Find top object and properties in operator definition.
     *
     * @param context the grammar context
     * @return the operator information for the specified context
     */
    private OpInfo parseDefinition(final ContextView context) {
        OpInfo rc = opInfo.get(context);
        if (rc != null) {
            return rc;
        }
        rc = new OpInfo();
        opInfo.put(context, rc);
        // NOTE POST 0.2: make definition parsing more flexible. Remove
        // artificial restrictions on refs.
        OperatorDefinition od = operator();
        if (isComposite()) {
            // ensure that text is not defined, and precedence is defined unless
            // operator is of F kind
            if (!od.getText().isEmpty()) {
                error(this, od, "grammar.OperatorDefinition.composite.operatorTextPresent");
            }
        } else {
            // ensure that text, and precedence are defined
            if (od.getText().isEmpty()) {
                error(this, od, "grammar.OperatorDefinition.simple.operatorTextMissed");
            }
        }
        rc.rootObject = topObject(context);
        final List<SyntaxStatement> syntax = rc.rootObject.getSyntax().getSyntax();
        parseSyntaxContent(new HashSet<DefinitionView>(), context, this, rc, syntax);
        return rc;
    }

    /**
     * Parse content of the root object, visiting corresponding definitions.
     *
     * @param visited visited definitions
     * @param context the current context
     * @param current the current definition
     * @param rc      the object info to update
     * @param syntax  the syntax statements to parse
     */
    private void parseSyntaxContent(final Set<DefinitionView> visited,
                                    final ContextView context, final DefinitionView current, final OpInfo rc,
                                    final List<SyntaxStatement> syntax) {
        for (final Object element : syntax) {
            if (element instanceof BlankSyntaxStatement) {
                // do nothing
                continue;
            }
            if (element instanceof Let) {
                final Let let = (Let) element;
                if (let.getExpression() instanceof OperandOp) {
                    final boolean isList = "+=".equals(let.getOperator().text());
                    final OperandOp operand = (OperandOp) let.getExpression();
                    final Associativity associativity = associativity();
                    if ("left".equals(operand.getPosition())) {
                        // check if operator has left argument
                        switch (associativity) {
                            case XF:
                            case YF:
                            case XFY:
                            case XFX:
                            case YFX:
                            case YFY:
                                if (rc.leftProperty == null) {
                                    rc.leftProperty = new PropertyInfo(let, let.getName().text(), isList);
                                } else {
                                    // Only one left operand is
                                    // allowed.
                                    error(this, operand, "grammar.Operand.duplicateLeftOperand");
                                }
                                break;
                            default:
                                error(this, operand, "grammar.Operand.leftNotAllowed", associativity.name());
                        }
                    } else if ("right".equals(operand.getPosition())) {
                        switch (associativity) {
                            case FX:
                            case FY:
                            case XFY:
                            case XFX:
                            case YFX:
                            case YFY:
                                if (rc.rightProperty == null) {
                                    rc.rightProperty = new PropertyInfo(let, let.getName().text(), isList);
                                } else {
                                    // Only one right operand is
                                    // allowed.
                                    error(this, operand, "grammar.Operand.duplicateRightOperand");
                                }
                                break;
                            default:
                                error(this, operand, "grammar.Operand.rightNotAllowed", associativity.name());
                        }
                    }
                } else {
                    processCompositeStatement(rc, let);
                }
            } else if (element instanceof ExpressionStatement) {
                ExpressionStatement es = (ExpressionStatement) element;
                if (es.getSyntax() instanceof RefOp) {
                    RefOp r = (RefOp) es.getSyntax();
                    DefView d = context.def(r.getName().text());
                    if (d == null) {
                        if (context.choice(r.getName().text()) != null) {
                            processCompositeStatement(rc, es);
                        } else {
                            error(current, r, "grammar.Ref.danglingRef", r.getName().text());
                        }
                    } else {
                        if (!enterDefContext(visited, d)) {
                            return;
                        }
                        parseSyntaxContent(visited, context, d, rc, d.statements());
                        leaveDefContext(visited, d);
                    }
                } else {
                    processCompositeStatement(rc, es);
                }
            } else {
                SyntaxStatement stmt = (SyntaxStatement) element;
                processCompositeStatement(rc, stmt);
            }
        }
    }

    /**
     * Process composite statement.
     *
     * @param rc   object information object to handle
     * @param stmt the statement to process
     */
    private void processCompositeStatement(final OpInfo rc, final SyntaxStatement stmt) {
        if (isComposite()) {
            rc.operatorStatements.add(stmt);
        } else {
            error(this, rc.rootObject, "grammar.SimpleOp.compositeSyntax");
        }
    }

    /**
     * @return true if current operation is composite
     */
    public boolean isComposite() {
        return operator().getCompositeModifier() != null;
    }

    /**
     * @param context the grammar context
     * @return operator statements
     */
    public List<SyntaxStatement> operatorStatements(final ContextView context) {
        OpInfo op = parseDefinition(context);
        return op.operatorStatements;
    }

    /**
     * @param context the grammar context
     * @return root object of the definition
     */
    public ObjectOp rootObject(final ContextView context) {
        OpInfo op = parseDefinition(context);
        return op.rootObject;
    }

    /**
     * Property information.
     */
    public static final class PropertyInfo {
        // NOTE POST 0.2: consider adding isList property to to PropertyName
        // class and removing this class.

        /**
         * true if it is a list property.
         */
        private final boolean isList;
        /**
         * The element.
         */
        private final Element element;
        /**
         * name of the property.
         */
        private final String name;

        /**
         * The constructor.
         *
         * @param element the element that defines the property
         * @param name    the property name
         * @param isList  if true, this is a list property
         */
        public PropertyInfo(final Element element, final String name, final boolean isList) {
            this.element = element;
            this.name = name;
            this.isList = isList;
        }

        /**
         * @return true if property is a list property
         */
        public boolean isList() {
            return isList;
        }

        /**
         * @return name of property
         */
        public String getName() {
            return name;
        }

        /**
         * @return the element that defines the property
         */
        public Element element() {
            return element;
        }
    }

    /**
     * A context specific operator information.
     */
    private class OpInfo {
        /**
         * List of operator statements.
         */
        private ArrayList<SyntaxStatement> operatorStatements = new ArrayList<SyntaxStatement>();
        /**
         * root object for operator.
         */
        private ObjectOp rootObject;
        /**
         * left property or null.
         */
        private PropertyInfo leftProperty;
        /**
         * right property or null.
         */
        private PropertyInfo rightProperty;
    }
}
