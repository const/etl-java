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

import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.Token;
import net.sf.etl.parsers.event.unstable.model.grammar.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
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
     * Top object of the definition, key is context and value is object. This
     * structure is used because due to possible def redefinition actual top
     * object might change from context to context.
     */
    private final HashMap<ContextView, Syntax> topObjects = new HashMap<ContextView, Syntax>();
    /**
     * a definition that actually holds a top object. See comment to
     * {@link #topObject} for explanation why it is a hash map.
     */
    private final HashMap<ContextView, DefinitionView> topObjectDefinitions = new HashMap<ContextView, DefinitionView>();
    /**
     * The syntax field in syntax definition.
     */
    private static final Field DEFINITION_SYNTAX_FIELD;

    static {
        try {
            DEFINITION_SYNTAX_FIELD = SyntaxDefinition.class.getField("syntax");
        } catch (Exception e) {
            throw new Error("Unexpected failure for getting field syntax", e);
        }
    }

    /**
     * The syntax field in sequence.
     */
    private static final Field SEQUENCE_SYNTAX_FIELD;

    static {
        try {
            SEQUENCE_SYNTAX_FIELD = Sequence.class.getField("syntax");
        } catch (Exception e) {
            throw new Error("Unexpected failure for getting field syntax", e);
        }
    }

    /**
     * The name field in syntax definition.
     */
    private static final Field NAME_FIELD;

    static {
        try {
            NAME_FIELD = SyntaxDefinition.class.getField("syntax");
        } catch (Exception e) {
            throw new Error("Unexpected failure for getting field syntax", e);
        }
    }

    /**
     * The constructor
     *
     * @param context    the defining context
     * @param definition the definition
     */
    public ObjectDefinitionView(ContextView context, SyntaxDefinition definition) {
        super(context, definition);
    }

    /**
     * A constructor used to implement grammar includes
     *
     * @param context    a including context
     * @param definition a definition view
     */
    public ObjectDefinitionView(ContextView context, DefinitionView definition) {
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
    public ObjectOp topObject(ContextView context) {
        ObjectOp rc = (ObjectOp) topObjects.get(context);
        if (rc == null) {
            extractTopObject(context);
            rc = (ObjectOp) topObjects.get(context);
        }
        return rc;
    }

    /**
     * Get name of top object
     *
     * @param context the context to get name
     * @return the top object
     */
    public ObjectName topObjectName(ContextView context) {
        ObjectOp topObject = topObject(context);
        if (topObject == null) {
            return null;
        }
        net.sf.etl.parsers.event.unstable.model.grammar.ObjectName name = topObject.name;
        return topObjectDefinition(context).convertName(name);

    }

    /**
     * Get the definition that actually defines top object
     *
     * @param context the context use to resolve definitions
     * @return the definition that contains the top object of the statement. It
     *         might be either statement or def referenced in the statement.
     */
    public DefinitionView topObjectDefinition(ContextView context) {
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
    private void extractTopObject(ContextView context) {
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
    private void extractTopObject(Set<DefinitionView> visited, DefinitionView view, ContextView context) {
        if (!enterDefContext(visited, view)) {
            return;
        }
        for (final SyntaxStatement stmt : view.definition().syntax) {
            if (stmt instanceof BlankSyntaxStatement) {
                // Ignore the thing. Blank statements do not contain
                // anything significant.
            } else if (stmt instanceof Let) {
                ObjectOp op = makeDefaultObject(context, view, stmt);
                if (op == null) {
                    error(view, stmt, "grammar.ObjectDefinition.misplacedLet",
                            definition().name, view.includingContext().name(),
                            view.includingContext().grammar().getSystemId());
                }
                break;
            } else if (stmt instanceof ExpressionStatement) {
                final ExpressionStatement exprStmt = (ExpressionStatement) stmt;
                final Syntax s = exprStmt.syntax;
                if (s instanceof ObjectOp) {
                    if (topObjects.containsKey(context)) {
                        // Additional top object has been found.
                        // This is an error because only one top object
                        // is allowed for object definitions.
                        error(view, stmt,
                                "grammar.ObjectDefinition.duplicateTopObject",
                                definition().name, view.includingContext().name(),
                                view.includingContext().grammar().getSystemId());
                    } else {
                        topObjects.put(context, s); // object expression
                        topObjectDefinitions.put(context, view); // actual definition
                    }
                } else if (s instanceof RefOp) {
                    final RefOp r = (RefOp) s;
                    final DefView d = context.def(r.name.text());
                    if (d == null) {
                        error(view, r, "grammar.Ref.danglingRef", r.name.text());
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
        if (visited.size() == 0 && topObjects.get(context) == null) {
            makeDefaultObject(context, view, view.definition());
        }
    }

    /**
     * Leave definition context
     *
     * @param visited the set of visited definitions
     * @param view    the definition view that was visited
     */
    protected void leaveDefContext(Set<DefinitionView> visited, DefinitionView view) {
        visited.remove(view);
    }

    /**
     * Enter definition context
     *
     * @param visited the set of visited definitions
     * @param view    the definition view that is about to be visited
     * @return true if visiting unvisited definition.
     */
    protected boolean enterDefContext(Set<DefinitionView> visited,
                                      DefinitionView view) {
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
     *         a default namespace
     */
    private ObjectOp makeDefaultObject(ContextView context, DefinitionView view, Element element) {
        GrammarView grammar = definingContext().grammar();
        Token defaultNamespacePrefix = grammar.defaultNamespacePrefix();
        SyntaxDefinition definition = definition();
        if (defaultNamespacePrefix == null) {
            error(view, element, "grammar.ObjectDefinition.noDefaultGrammar",
                    definition.name, view.definingContext().name(),
                    view.definingContext().grammar().getSystemId());
            return null;
        }
        ObjectOp o = new ObjectOp();
        o.ownerObject = definition;
        o.ownerFeature = DEFINITION_SYNTAX_FIELD;
        o.start = definition.start;
        o.end = definition.end;
        net.sf.etl.parsers.event.unstable.model.grammar.ObjectName name = new net.sf.etl.parsers.event.unstable.model.grammar.ObjectName();
        name.ownerObject = o;
        name.ownerFeature = NAME_FIELD;
        name.start = o.start;
        name.end = o.end;
        name.prefix = defaultNamespacePrefix;
        name.name = definition.name;
        o.name = name;
        Sequence s = new Sequence();
        s.syntax.addAll(definition.syntax);
        s.start = o.start;
        s.end = o.end;
        s.ownerObject = o;
        s.ownerFeature = SEQUENCE_SYNTAX_FIELD;
        o.syntax = s;
        topObjects.put(context, o); // object expression
        topObjectDefinitions.put(context, view); // actual
        // definition
        return o;
    }
}
