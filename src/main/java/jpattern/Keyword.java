package jpattern.compiler;

import jpattern.util.CharStream;

import java.util.HashMap;
import java.util.EnumSet;

enum Keyword
{
    ABORT, ALT, ANY, ARB, ARBNO, ASSIGN, BAL,
    BREAK, BREAKX, CAT, CANCEL, DEFER, FAIL,
    FENCE, IASSIGN, LEN, NOTANY,
    NSPAN, POS, REM, REPLACE, REST, RPOS,
    RTAB, SETCUR, SPAN, SUCCEED, TAB,
    JAVA, // special keyword for wrapping backquotes
    // Following are markers for simple constants and variables
    // for use in class Node
    _STRING, _INT, _VAR,
    // Following is to mark java expression inclusions
    _JAVASTRING,
    // mark external patterns
    _EXTERNAL,    
    // If all else fails
    UNKNOWN;

    static public final Keyword END = UNKNOWN;
    static public final int size = (1+END.ordinal());

    static private String[] names = new String[] {
	"abort", "|", "any", "arb", "arbno", "$", "bal",
	"break", "breakx", "&", "cancel", "+", "fail",
	"fence", ".", "len", "notany",
	"nspan", "pos", "rem", "=", "rest", "rpos",
	"rtab", "setcur", "span", "succeed", "tab",
	"java",
	"_STRING", "_INT", "_VAR", "_JAVASTRING",
        "_EXTERNAL",
	"",
    };

    static private String[] opnames= new String[] {
	"Abort", "Alternate", "Any", "Arb", "Arbno", "Assign", "Bal",
	"Break", "BreakX", "Concat", "Abort", "Defer", "Fail",
	"Fence", "IAssign", "Len", "NotAny",
	"NSpan", "Pos", "Rem", "Replace", "Rem", "RPos",
	"RTab", "Setcur", "Span", "Succeed", "Tab",
	"Java",
	null, null, null, null,
	null,
        null
    };

    static HashMap<String,Keyword> Keywords;
    static Keyword testKeyword(String s)
        {return(Keywords.containsKey(s)?Keywords.get(s.toLowerCase()):UNKNOWN);}
    static HashMap<Keyword,String> JavaOps;
    static String testOperator(Keyword kw)
        {return (JavaOps.containsKey(kw)?JavaOps.get(kw):null);}

    static
    {
	// Establish keyword toname map
	Keywords = new HashMap<String,Keyword>();
	for(Keyword k : Keyword.values())
	    Keywords.put(names[k.ordinal()],k);

	// Establish the Keyword to Java operator map
	JavaOps = new HashMap<Keyword,String>();
	for(Keyword k : Keyword.values()) {
	    if(opnames[k.ordinal()] != null) {
		JavaOps.put(k,opnames[k.ordinal()]);
	    }
	}
    }

    // Define the set of simple valued nodes
    static EnumSet<Keyword> Simplevalued
	= EnumSet.of(_STRING, _INT, _VAR);

}; // enum Keyword
