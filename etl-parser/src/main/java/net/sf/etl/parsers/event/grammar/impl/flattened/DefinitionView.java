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

import net.sf.etl.parsers.DefinitionInfo;
import net.sf.etl.parsers.ObjectName;
import net.sf.etl.parsers.SourceLocation;
import net.sf.etl.parsers.event.unstable.model.grammar.*;

import java.util.List;

/**
 * This is the definition view.
 *
 * @author const
 */

public abstract class DefinitionView extends ContextMemberView {
    /**
     * The wrapped definition
     */
    private final SyntaxDefinition definition;
    /**
     * The original definition
     */
    private final DefinitionView originalDefinition;
    /**
     * The The definition info
     */
    private final DefinitionInfo definitionInfo;

    /**
     * The constructor
     *
     * @param context    the context that defines this definition
     * @param definition the wrapped definition
     */
    protected DefinitionView(ContextView context, SyntaxDefinition definition) {
        super(context, context);
        this.definition = definition;
        this.originalDefinition = this;
        this.definitionInfo = new DefinitionInfo(
                context.getDefinitionContext(),
                definition.name.text(),
                new SourceLocation(definition.start, definition.end, context.grammar().getSystemId()));
    }

    /**
     * The constructor
     *
     * @param context    the context that includes this definition
     * @param definition the wrapped definition
     */
    protected DefinitionView(ContextView context, DefinitionView definition) {
        super(definition.definingContext(), context);
        this.definition = definition.definition();
        this.originalDefinition = definition.originalDefinition;
        this.definitionInfo = new DefinitionInfo(
                context.getDefinitionContext(),
                originalDefinition.definitionInfo().getName(),
                originalDefinition.definitionInfo().getLocation());
    }


    /**
     * @return the definition info
     */
    public DefinitionInfo definitionInfo() {
        return definitionInfo;
    }

    /**
     * @return the name of definition
     */
    public String name() {
        return definition().name.text();
    }

    /**
     * Get view of definition
     *
     * @param context the defining context
     * @param def     the definition
     * @return the view for definition
     */
    public static DefinitionView get(ContextView context, SyntaxDefinition def) {
        if (def instanceof Def) {
            return new DefView(context, (Def) def);
        } else if (def instanceof DocumentationSyntax) {
            return new DocumentationView(context, (DocumentationSyntax) def);
        } else if (def instanceof Statement) {
            return new StatementView(context, (Statement) def);
        } else if (def instanceof OperatorDefinition) {
            return new OpDefinitionView(context, (OperatorDefinition) def);
        } else if (def instanceof Attributes) {
            return new AttributesView(context, (Attributes) def);
        } else {
            throw new RuntimeException("Unknown definition: " + def.getClass().getCanonicalName());
        }
    }

    /**
     * Get view of definition
     *
     * @param context the defining context
     * @param def     the definition
     * @return the view for definition
     */
    public static DefinitionView get(ContextView context, DefinitionView def) {
        if (def instanceof DefView) {
            return new DefView(context, def);
        } else if (def instanceof DocumentationView) {
            return new DocumentationView(context, def);
        } else if (def instanceof StatementView) {
            return new StatementView(context, def);
        } else if (def instanceof OpDefinitionView) {
            return new OpDefinitionView(context, (OpDefinitionView) def);
        } else if (def instanceof AttributesView) {
            return new AttributesView(context, (AttributesView) def);
        } else {
            throw new RuntimeException("Unknown definition view: " + def.getClass().getName());
        }
    }

    /**
     * @return the original definition
     */
    public DefinitionView originalDefinition() {
        return originalDefinition;
    }

    /**
     * @return the definition from grammar.
     */
    public SyntaxDefinition definition() {
        return definition;
    }

    /**
     * Resolve prefix to the object in the context of definition's grammar
     *
     * @param prefix the prefix to resolve
     * @return namespace for prefix in the context of the definition
     */
    public String resolvePrefix(String prefix) {
        return originalDefinition.definingContext().grammar().namespace(prefix);
    }

    /**
     * @return statements inside documentation syntax
     */
    public List<SyntaxStatement> statements() {
        final SyntaxDefinition s = definition();
        return s.syntax;
    }

    /**
     * Report error
     *
     * @param view     the definition view that contains the problem
     * @param e        the element about which error is reported
     * @param errorId  the error id
     * @param errorArg the error arguments
     */
    protected void error(DefinitionView view, Element e, String errorId, Object... errorArg) {
        view.definingContext().error(e, errorId, errorArg);
    }

    /**
     * Convert object name from prefix form to fully qualified form. Conversion
     * happens in context of grammar that actually contains the definition.
     *
     * @param name a object name in the syntax
     * @return a name to convert
     */
    public ObjectName convertName(net.sf.etl.parsers.event.unstable.model.grammar.ObjectName name) {
        final DefinitionView originalDefinition = originalDefinition();
        final ContextView definingContext = originalDefinition.definingContext();
        final GrammarView grammar = definingContext.grammar();
        final String prefix = name.prefix.text();
        final String uri = grammar.namespace(prefix);
        if (uri == null) {
            error(originalDefinition, name, "grammar.ObjectName.undefinedPrefix", prefix);
        }
        // NOTE POST 0.2: try to use cache
        return new ObjectName(uri, name.name.text());
    }


    /**
     * @return get location associated with the definition
     */
    public SourceLocation sourceLocation() {
        return originalDefinition().definition().sourceLocation();
    }
}
