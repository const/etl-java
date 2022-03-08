/*
 * Reference ETL Parser for Java
 * Copyright (c) 2000-2022 Konstantin Plotnikov
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
package net.sf.etl.parsers.event.grammar.impl.flattened; // NOPMD

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.unstable.model.grammar.BlankSyntaxStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Element;
import net.sf.etl.parsers.event.unstable.model.grammar.ExpressionStatement;
import net.sf.etl.parsers.event.unstable.model.grammar.Let;
import net.sf.etl.parsers.event.unstable.model.grammar.ObjectOp;
import net.sf.etl.parsers.event.unstable.model.grammar.RefOp;
import net.sf.etl.parsers.event.unstable.model.grammar.Sequence;
import net.sf.etl.parsers.event.unstable.model.grammar.Syntax;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxDefinition;
import net.sf.etl.parsers.event.unstable.model.grammar.SyntaxStatement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A view of definition that must have a root object. This object might
 * available directly or through definition. This class provides utility methods
 * that are used to extract this object.
 *
 * @author const
 */
public abstract class ObjectDefinitionView extends DefinitionView {
    /**
     * The syntax field in syntax definition.
     */
    private static final String DEFINITION_SYNTAX_FIELD = "syntax";

    /**
     * The syntax field in sequence.
     */
    private static final String SEQUENCE_SYNTAX_FIELD = "syntax";

    /**
     * The name field in syntax definition.
     */
    private static final String NAME_FIELD = "name";

    /**
     * Top object of the definition, key is context and value is object. This
     * structure is used because due to possible def redefinition actual top
     * object might change from context to context.
     */
    private final Map<ContextView, Syntax> topObjects = new HashMap<ContextView, Syntax>(); // NOPMD
    /**
     * a definition that actually holds a top object. See comment to
     * {@link #topObject} for explanation why it is a hash map.
     */
    private final Map<ContextView, DefinitionView> topObjectDefinitions = // NOPMD
            new HashMap<ContextView, DefinitionView>();

    /**
     * The constructor.
     *
     * @param context    the defining context
     * @param definition the definition
     */
    public ObjectDefinitionView(final ContextView context, final SyntaxDefinition definition) {
        super(context, definition);
    }

    /**
     * A constructor used to implement grammar includes.
     *
     * @param context    a including context
     * @param definition a definition view
     */
    public ObjectDefinitionView(final ContextView context, final DefinitionView definition) {
        super(context, definition);
    }

    /**
     * This method gets top object for definition. Object definitions must have
     * exactly one top object. However this object might be created either
     * directly in the object definition or indirectly through def/ref
     * construct.
     *
     * @param context a context use to resolve definitions
     * @return a top object of the statement.
     */
    public final ObjectOp topObject(final ContextView context) {
        ObjectOp rc = (ObjectOp) topObjects.get(context);
        if (rc == null) {
            extractTopObject(context);
            rc = (ObjectOp) topObjects.get(context);
        }
        return rc;
    }

    /**
     * Get name of top object.
     *
     * @param context the context to get name
     * @return the top object
     */
    public final ObjectName topObjectName(final ContextView context) {
        final ObjectOp topObject = topObject(context);
        if (topObject == null) {
            return null;
        }
        final net.sf.etl.parsers.event.unstable.model.grammar.ObjectName name = topObject.getName();
        return topObjectDefinition(context).convertName(name);

    }

    /**
     * Get the definition that actually defines top object.
     *
     * @param context the context use to resolve definitions
     * @return the definition that contains the top object of the statement. It
     * might be either statement or def referenced in the statement.
     */
    public final DefinitionView topObjectDefinition(final ContextView context) {
        DefinitionView rc = topObjectDefinitions.get(context);
        if (rc == null) {
            extractTopObject(context);
            rc = topObjectDefinitions.get(context);
        }
        return rc;
    }

    /**
     * This method extracts and finds top object and saves it and its definition
     * to variables. The method ignored blank statements. The method tried
     * default namespace and if not found, it reports as error if it encounters
     * anything beyond object or blank statement at that level. However the
     * check is not deep right now.
     *
     * @param context a context use to resolve definitions
     */
    private void extractTopObject(final ContextView context) {
        extractTopObject(new HashSet<DefinitionView>(), this, context);
    }

    /**
     * Extract top object from view. If object cannot be extracted, follow def
     * chain further.
     *
     * @param visited the set that is used to detect loops in the definitions
     * @param view    the view to extract.
     * @param context the context use to resolve definitions
     */
    private void extractTopObject(final Set<DefinitionView> visited, final DefinitionView view,
                                  final ContextView context) {
        if (!enterDefContext(visited, view)) {
            return;
        }
        for (final SyntaxStatement stmt : view.definition().getSyntax()) {
            if (stmt instanceof BlankSyntaxStatement) {
                // Ignore the thing. Blank statements do not contain
                // anything significant.
                continue;
            }
            if (stmt instanceof Let) {
                final ObjectOp op = makeDefaultObject(context, view, stmt);
                if (op == null) {
                    error(view, stmt, "grammar.ObjectDefinition.misplacedLet",
                            definition().getName(), view.includingContext().name(),
                            view.includingContext().grammar().getSystemId());
                }
                break;
            } else if (stmt instanceof ExpressionStatement) {
                final ExpressionStatement exprStmt = (ExpressionStatement) stmt;
                final Syntax s = exprStmt.getSyntax();
                if (s instanceof ObjectOp) {
                    if (topObjects.containsKey(context)) {
                        // Additional top object has been found.
                        // This is an error because only one top object
                        // is allowed for object definitions.
                        error(view, stmt,
                                "grammar.ObjectDefinition.duplicateTopObject",
                                definition().getName(), view.includingContext().name(),
                                view.includingContext().grammar().getSystemId());
                    } else {
                        topObjects.put(context, s); // object expression
                        topObjectDefinitions.put(context, view); // actual definition
                    }
                } else if (s instanceof RefOp) {
                    final RefOp r = (RefOp) s;
                    final DefView d = context.def(r.getName().text());
                    if (d == null) {
                        if (context.choice(r.getName().text()) != null) {
                            makeDefaultObject(context, view, s);
                            break;
                        } else {
                            error(view, r, "grammar.Ref.danglingRef", r.getName().text());
                        }
                    } else {
                        extractTopObject(visited, d, context);
                    }
                } else {
                    makeDefaultObject(context, view, s);
                    break;
                }
            }
        }
        leaveDefContext(visited, view);
        if (visited.isEmpty() && topObjects.get(context) == null) {
            makeDefaultObject(context, view, view.definition());
        }
    }

    /**
     * Leave definition context.
     *
     * @param visited the set of visited definitions
     * @param view    the definition view that was visited
     */
    protected final void leaveDefContext(final Set<DefinitionView> visited, final DefinitionView view) {
        visited.remove(view);
    }

    /**
     * Enter definition context.
     *
     * @param visited the set of visited definitions
     * @param view    the definition view that is about to be visited
     * @return true if visiting unvisited definition.
     */
    protected final boolean enterDefContext(final Set<DefinitionView> visited,
                                            final DefinitionView view) {
        if (visited.contains(view)) {
            error(view, view.definition(), "grammar.Def.cyclicDefinition",
                    view.includingContext().name(),
                    view.includingContext().grammar().getSystemId());
            return false;
        }
        visited.add(view);
        return true;
    }

    /**
     * Make a default object expression in the case when the top level element
     * is not an object.
     *
     * @param context the object context
     * @param view    the definition that contains element
     * @param element the element that forces object creation
     * @return the object create expression or null if the grammar does not have
     * a default namespace
     */
    private ObjectOp makeDefaultObject(final ContextView context, final DefinitionView view, final Element element) {
        final GrammarView grammar = definingContext().grammar();
        final Token defaultNamespacePrefix = grammar.defaultNamespacePrefix();
        final SyntaxDefinition definition = definition();
        if (defaultNamespacePrefix == null) {
            error(view, element, "grammar.ObjectDefinition.noDefaultGrammar",
                    definition.getName(), view.definingContext().name(),
                    view.definingContext().grammar().getSystemId());
            return null;
        }
        final ObjectOp o = new ObjectOp();
        o.setOwnerObject(definition);
        o.setOwnerFeature(DEFINITION_SYNTAX_FIELD);
        o.setLocation(definition.getLocation());
        final net.sf.etl.parsers.event.unstable.model.grammar.ObjectName name =
                new net.sf.etl.parsers.event.unstable.model.grammar.ObjectName();
        name.setOwnerObject(o);
        name.setOwnerFeature(NAME_FIELD);
        name.setLocation(o.getLocation());
        name.setPrefix(defaultNamespacePrefix);
        name.setName(definition.getName());
        o.setName(name);
        final Sequence s = new Sequence();
        s.getSyntax().addAll(definition.getSyntax());
        s.setLocation(o.getLocation());
        s.setOwnerObject(o);
        s.setOwnerFeature(SEQUENCE_SYNTAX_FIELD);
        o.setSyntax(s);
        topObjects.put(context, o); // object expression
        topObjectDefinitions.put(context, view); // actual
        // definition
        return o;
    }
}
