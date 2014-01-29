package jpattern.compiler;

import jpattern.util.Debug;
import jpattern.Variable;

import static jpattern.util.QuotedString.*;

public class Parsetree
{
    Keyword code;
    Parsetree left;
    Parsetree right;
    Parsetree[] argv; // in the event that the tree has more than two children
    Object value;  // if code in {_PATTERN,_STRING,_INT,_VAR,_EXTERNAL}
    String text; // for storing compiled java code

    Parsetree(Keyword x)
	{code=x; left=null; right=null; argv = null; value = null; text=null;}
    Parsetree(Keyword x, Object v) {this(x); value = v;}
    Parsetree(Keyword x, int v) {this(x); value = new Integer(v);}
    Parsetree(Keyword x, char v) {this(x); value = new Character(v);}

    public String stringValue() {return (String)value;}
    public char charValue() {return ((Character)value).charValue();}
    public int intValue() {return ((Integer)value).intValue();}
    public Variable varValue() {return (Variable)value;}

    public String toString()
    {
	// handle value cases
	switch (code) {
	    case _STRING:
		return (DQUOTE+addEscapes(stringValue())+DQUOTE);
	    case _INT:
		return Integer.toString(intValue());
	    case _VAR:
		return varValue().Name;
	    default: break;
	}
	// handle operators	    
	String s = code.toString();
	if(left != null) {
	    s += (LPAREN + left.toString());
	    if(right != null) {s += ("," + right.toString());}	    
	    s += RPAREN;
	}
	return s;
    }
}
