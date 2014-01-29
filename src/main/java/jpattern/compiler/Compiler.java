package jpattern.compiler;

import jpattern.Failure;
import jpattern.Pattern;
import jpattern.util.*;

import static jpattern.compiler.Keyword.*;
import static jpattern.util.JpatternConstants.*;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Stack;

public class Compiler
{
    Parser parser;
    HashMap<String, Keyword> Keywords = new HashMap<String, Keyword>();

    String patternPrefix = PATTERN_PREFIX;

    public Compiler()
    {
        parser = new Parser();
    }

    public void setQuote(char q) 
    {
        parser.setQuote(q);
    }

    public void setConflict(boolean tf)
    {
        patternPrefix = PATTERN_PREFIX;
        if(tf) patternPrefix += DFALT_CONFLICT_PREFIX;
    }

    // This is only external access to the compiler
    public Pattern compile(String expr) 
    {
        Parsetree tree = parser.parse(expr);
        Debug.println(2, "tree=" + tree.toString());
        Pattern p = compile(tree);
        return p;
    }

    String compileJ(String expr) 
    {
        return compileJ(expr, 0, DEFAULTINDENTINCR);
    }

    String compileJ(String expr, int indent) 
    {
        return compileJ(expr, indent, DEFAULTINDENTINCR);
    }

    String compileJ(String expr, int indent, int incr) 
    {
        Parsetree tree = parser.parse(expr);
        Debug.println(2,"tree=" + tree);
        compileJ(tree, false); // generate without indents initially
        return indent(tree.text, indent, incr);
    }

    Pattern compile(Parsetree n) 
    {
        Pattern p = null;
        String s = null;

        switch (n.code) {
        case ALT:
            p = Pattern.Alternate(compile(n.left), compile(n.right));
            break;
        case ANY:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.Any(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.Any(n.left.varValue());
                break;
            }
            break;
        case ARB:
            p = Pattern.Arb();
            break;
        case ARBNO:
            if(n.left.code == _STRING)
                p = Pattern.Arbno(n.left.stringValue());
            else
                p = Pattern.Arbno(compile(n.left));
            break;
        case ASSIGN:
            p = Pattern.Assign(compile(n.left), n.right.varValue());
            break;
        case BAL:
            if(n.left.code == null) p = Pattern.Bal();
            else switch (n.left.code) {
            case _STRING:
                p = Pattern.Bal(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.Bal(n.left.varValue());
                break;
            }
            break;
        case BREAK:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.Break(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.Break(n.left.varValue());
                break;
            }
            break;
        case BREAKX:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.BreakX(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.BreakX(n.left.varValue());
                break;
            }
            break;
        case CAT:
            p = Pattern.Concat(compile(n.left), compile(n.right));
            break;
        case ABORT:
        case CANCEL:
            p = Pattern.Cancel();
            break;
        case DEFER:
            p = Pattern.Defer(n.left.varValue());
            break;
        case FAIL:
            p = Pattern.Fail();
            break;
        case FENCE:
            if(n.left == null) p = Pattern.Fence();
            else {
                p = compile(n.left);
                p = Pattern.Fence(p);
            }
            break;
        case IASSIGN:
            p = Pattern.IAssign(compile(n.left), n.right.varValue());
            break;
        case LEN:
            switch (n.left.code) {
            case _INT:
                p = Pattern.Len(n.left.intValue());
                break;
            case _VAR:
                p = Pattern.Len(n.left.varValue());
                break;
            }
            break;
        case NOTANY:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.NotAny(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.NotAny(n.left.varValue());
                break;
            }
            break;
        case NSPAN:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.NSpan(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.NSpan(n.left.varValue());
                break;
            }
            break;
        case POS:
            switch (n.left.code) {
            case _INT:
                p = Pattern.Pos(n.left.intValue());
                break;
            case _VAR:
                p = Pattern.Pos(n.left.varValue());
                break;
            }
            break;
        case REPLACE:
            p = Pattern.Replace(compile(n.left), n.right.varValue());
            break;
        case REM:
        case REST:
            p = Pattern.Rest();
            break;
        case RPOS:
            switch (n.left.code) {
            case _INT:
                p = Pattern.RPos(n.left.intValue());
                break;
            case _VAR:
                p = Pattern.RPos(n.left.varValue());
                break;
            }
            break;
        case RTAB:
            switch (n.left.code) {
            case _INT:
                p = Pattern.RTab(n.left.intValue());
                break;
            case _VAR:
                p = Pattern.RTab(n.left.varValue());
                break;
            }
            break;
        case SETCUR:
            p = Pattern.Setcur(n.left.varValue());
            break;
        case SPAN:
            switch (n.left.code) {
            case _STRING:
                p = Pattern.Span(n.left.stringValue());
                break;
            case _VAR:
                p = Pattern.Span(n.left.varValue());
                break;
            }
            break;
        case SUCCEED:
            p = Pattern.Succeed();
            break;
        case TAB:
            switch (n.left.code) {
            case _INT:
                p = Pattern.Tab(n.left.intValue());
                break;
            case _VAR:
                p = Pattern.Tab(n.left.varValue());
                break;
            }
            break;

        case _INT:
            p = Pattern.StringPattern(Integer.toString(n.intValue()));
            break;
        case _STRING:
            s = n.stringValue();
            if(s.length() == 1)
                p = Pattern.CharPattern(s.charAt(0));
            else
                p = Pattern.StringPattern(s);
            break;
        case JAVA:
        case _JAVASTRING:
            throw new Failure("Compiler.compile: embedded java string `...` not allowed");
        case _VAR:
            p = Pattern.Defer(n.varValue());
            break;

        case _EXTERNAL:
            String name = n.stringValue();
            Object[] argv = new Object[n.argv.length];
            for(int i = 0;i < argv.length;i++) argv[i] = compile(n.argv[i]);
            p = Pattern.External(n.stringValue(), argv);
            break;

        default:
            throw new Failure("Compiler error: unexpected Node: " + n.code);
        }
        return p;
    }

    //////////////////////////////////////////////////
    // Compile a pattern into a long nested set of 
    // Pattern package calls.

    void compileJ(Parsetree n, boolean stringOK) 
    {
        switch (n.code) {
        // operators with zero arguments
        case ARB:
        case CANCEL:
        case DEFER:
        case FAIL:
        case REM:
        case REST:
        case SUCCEED:
            // operators with one integer argument
        case LEN:
        case POS:
        case RPOS:
        case RTAB:
        case TAB:
        case SETCUR:
            // operators with optional integer argument
            // operators with one string argument
        case ANY:
        case BREAK:
        case BREAKX:
        case NOTANY:
        case NSPAN:
        case SPAN:
            // operators with optional string argument
        case BAL:
            // operators with one pattern argument
        case ARBNO: {
            n.text = patternPrefix + testOperator(n.code) + LPAREN;
            if(n.left != null) {
                compileJ(n.left, true);
                n.text += n.left.text;
            }
            if(n.right != null) {
                compileJ(n.right, true);
                n.text += (", " + n.right.text);
            }
            n.text += RPAREN;
        }
        ;
        break;

        // Special case operators with optional pattern argument
        case FENCE:
            n.text = patternPrefix + testOperator(n.code) + LPAREN;
            // check for 0 arg case
            if(n.left != null) {// check 1 arg case
                compileJ(n.left, false);
                n.text += n.left.text;
            }
            n.text += RPAREN;
            break;

        // Special case two operand case
        case ALT:
        case CAT:
        case ASSIGN:
        case IASSIGN:
        case REPLACE: {
            n.text = patternPrefix + testOperator(n.code) + LPAREN;
            if(n.left != null) {
                compileJ(n.left, false);
                n.text += n.left.text;
            }
            if(n.right != null) {
                compileJ(n.right, false);
                n.text += ("," + n.right.text);
            }
            n.text += RPAREN;
        }
        ;
        break;

        // Special case for Java keyword
        case JAVA:
            n.text = (patternPrefix + "Java" + LPAREN + (n.stringValue()) + RPAREN);
            break;

        case _STRING: {
            String s = (DQUOTE // for java, always use DQUOTE
                + QuotedString.addEscapes(n.stringValue(), DQUOTE)
                + DQUOTE);
            if(stringOK)
                n.text = s;
            else {
                if(s.length() == 1)
                    n.text = (patternPrefix + "CharPattern" + LPAREN + SQUOTE
                        + QuotedString.addEscapedChar(s.charAt(0))
                        + SQUOTE + RPAREN);
                else
                    n.text = (patternPrefix + "StringPattern" + LPAREN + s + RPAREN);
            }
        }
        ;
        break;

        case _JAVASTRING: {
            n.text = (LPAREN + n.stringValue() + RPAREN);
        }
        ;
        break;

        case _INT:
            n.text = Integer.toString(n.intValue());
            break;

        case _VAR:
            n.text = ("Variable.create(" + DQUOTE
                + QuotedString.addEscapes(n.varValue().Name)
                + DQUOTE + ")");
            break;

        case _EXTERNAL:
            String s = (patternPrefix + "External" + LPAREN);
            s += (DQUOTE + QuotedString.addEscapes(n.stringValue()) + DQUOTE);
            for(Parsetree arg : n.argv) {
                compileJ(arg, true);
                s += ("," + arg.text);
            }
            s += RPAREN;
            n.text = s;
            break;

        default:
            throw new Failure("Compiler error: unexpected Node: " + n.code);
        }
//	System.err.println("generate: |"+n.text+"|");
    }

    static String indent = "                         ";

    static String getIndent(int n)
    {
        while(n > indent.length()) indent = (indent + indent);
        return indent.substring(0, n);
    }

    // Insert indentation into a nested Java Pattern expression.
    // Assume:
    // 1. All string constants are escaped
    // 2. There is no whitespace outside of a string constant

    // constants
    static String delims = "\",()";
    static String sdelims = "\"\\"; // for parsing inside string constants
    static String[] breakops = new String[]{
        "Concat", "Alternate"
    };

    String indent(String expr, int iindent, int incr)
    {
        String curindent = "";
        int depth = 0;
        Stack<Integer> istack = new Stack<Integer>();
        istack.push(0); // prime stack
        StringBuilder buf = new StringBuilder();
        StringTokenizer lex = new StringTokenizer(expr, delims, true);

        while(lex.hasMoreTokens()) {
            String tok = getToken(lex);
            if(tok.length() == 1 && delims.indexOf(tok.charAt(0)) >= 0) {
                switch (tok.charAt(0)) {
                case ',':
                    buf.append(tok);
                    // see if we are at the same depth as the stack top
                    if(depth == istack.peek()) {
                        // force newline and indent
                        buf.append("\n" + getIndent(iindent));
                        buf.append(curindent);
                    }
                    break;
                case '(':
                    buf.append(tok);
                    depth++;
                    break;
                case ')':
                    // check if we are closing the stack top depth
                    if(depth == istack.peek()) {
                        // if so, then add newline and indent
                        curindent = curindent.substring(incr);
                        buf.append("\n" + getIndent(iindent));
                        buf.append(curindent);
                        istack.pop();
                    }
                    buf.append(tok);
                    depth--;
                    break;
                case '"':
                    buf.append(tok);
                    passString(buf, lex);
                    break;
                }
            } else { // basically an identifier
                // pass the operator
                buf.append(tok);
                if(isBreakop(tok)) {
                    // pass the following LPAREN
                    tok = getToken(lex);
                    buf.append(tok);
                    // bump the depth
                    depth++;
                    // stack this depth
                    istack.push(depth);
                    // alter the current indent
                    curindent = getIndent(depth * incr);
                    // add a newline
                    buf.append("\n" + getIndent(iindent));
                    // add in the indent
                    buf.append(curindent);
                }
            }
        }
        return buf.toString();
    }

    private void passString(StringBuilder buf, StringTokenizer lex)
    {
        while(lex.hasMoreTokens()) {
            String tok = lex.nextToken(sdelims);
            buf.append(tok); // pass this token
            if(tok.equals("\"")) return;
            if(tok.equals("\\")) {
                // pass next token
                tok = lex.nextToken(sdelims);
                buf.append(tok);
            }
        }
    }

    private boolean isBreakop(String op)
    {
        for(String bop : breakops) {
            if(op.equals(patternPrefix + bop)) return true;
        }
        return false;
    }

    private String getToken(StringTokenizer lex)
    {
        String tok = lex.nextToken(delims);
        Debug.println(3,"token=" + tok);
        return tok;
    }
}
