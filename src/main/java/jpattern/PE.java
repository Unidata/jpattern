package jpattern;

import jpattern.util.QuotedString;
import jpattern.util.Debug;
import jpattern.Variable;

import static jpattern.PatternCode.*;
import static jpattern.util.JpatternConstants.EOP;

import java.util.HashMap;
import java.util.HashSet;

public class PE extends Pattern
{
    static final char LPAREN = QuotedString.LPAREN;
    static final char RPAREN = QuotedString.RPAREN;
    static final char LBRACE = QuotedString.LBRACE;
    static final char RBRACE = QuotedString.RBRACE;

    PatternCode Pcode = PC_Unknown;

    PE Pthen = null; //  Successor element, to be matched after this one
    PE Alt = null; // Optional alternative; technically a parameter

    PE getAlt() {return Alt;}
    void setAlt(PE alt) {Alt = alt;}

    // All parameters (except Alt) are stored as separate fields
    PE Pat = null;
    String Str = null;
    int Int = 0;
    char Char = 0;
    Variable Var = null;
    Object[] Argv = null;

    public PE(PatternCode Pcode)
	{this.Pcode = Pcode;}

    public PE(PatternCode Pcode, PE Pthen)
	{this(Pcode); this.Pthen = Pthen;}

    public PE(PatternCode Pcode, PE Pthen, PE alt)
	{this(Pcode,Pthen); Alt = alt;}

    public PE(PatternCode Pcode, PE Pthen, Pattern pp)
	{this(Pcode,Pthen); Pat = (PE)pp;}

    public PE(PatternCode Pcode, PE Pthen, Variable var)
	{this(Pcode,Pthen); Var = var;}

    public PE(PatternCode Pcode, PE Pthen, int nat)
	{this(Pcode,Pthen); Int = nat;}

    public PE(PatternCode Pcode, PE Pthen, String  str)
	{this(Pcode,Pthen); Str = str;}

    public PE(PatternCode Pcode, PE Pthen, char cc)
	{this(Pcode,Pthen); Char = cc;}

    // This is currently only used by Pattern.External()
    public PE(PatternCode Pcode, PE Pthen, PE Palt, Object[] argv)
	{this(Pcode,Pthen,Palt); Argv = argv;}

    public PE(PE p)
    {
	this(p.Pcode,p.Pthen);
	this.Alt = p.Alt;
	this.Pat = p.Pat;
	this.Str = p.Str;
	this.Int = p.Int;
	this.Char = p.Char;
	this.Var = p.Var;
	this.Argv = p.Argv;
    }

    //////////////////////////////////////////////////
    // Print out just this PE

    public String toString()
    {
	String s = "";
	s += LPAREN;
	s += (getPcode() + LBRACE);
	switch (PatternArg.argType(Pcode)) {
	    case PA_String:
	    {
		s += (QuotedString.DQUOTE);
		s += (QuotedString.addEscapes((Str == null?"<missing>":Str)));
		s += (QuotedString.DQUOTE);
	    }; break;

	    case PA_Int:
		s += (LPAREN);
		s += (Int);
		s += (RPAREN);
		break;

	    case PA_Char:
		s += (QuotedString.SQUOTE);
		s += QuotedString.addEscapedChar(Char);
		s += (QuotedString.SQUOTE);
		break;

	    case PA_Var:
	    {
		s += ("+");
		s += (Var == null?"<missing>":Var.Name);
	    }; break;

	    case PA_External:
	    {
		s += Str;
		s += LPAREN;
		if(Argv != null) {
		    for(int i=0;i<Argv.length;i++) {
			if(i > 0) s += ",";
			if(s == null) s += "null";
			else s += Argv[i].toString();
		    }
		}
		s += RPAREN;
	    }; break;

	    case PA_None:
		break;
	}
	s += RBRACE;
	if(Pthen != null && Pthen != EOP) s += ", Then ...";
	if(Alt != null && Alt != EOP) s += ", Else ...";
	s+= RPAREN;
        return s;
    }

    //////////////////////////////////////////////////
    // Print out a whole graph beginning at this PE

    // Graph may have cycles and common subgraphs
    // To print the most concise string representing a PE,
    // We need to track common subnodes and make sure
    // they are printed only at the highest level
    // using Pthen chains.

    public String graphToString()
    {
	HashMap<PE,Integer> shared = new HashMap<PE,Integer>();
	HashSet<PE> visited = new HashSet<PE>();
	shared.put(EOP,-1); // always shared
	visited.add(EOP); // always visited
	computeChains(0,shared,visited);
	visited.clear();
	return graphToString(0,shared,visited);
    }

    void computeChains(int depth,
		       HashMap<PE,Integer> shared,
		       HashSet<PE> visited)
    {
	if(shared.get(this) != null) return;
	shared.put(this,depth);
	if(visited.contains(this)) return;
	visited.add(this);
	if(Pthen == null)
	    throw new Error("null Pthen: "+this.Pcode);
	if(this.Pcode == PC_EOP) return;
	Pthen.computeChains(depth,shared,visited);
	if(Alt != null) Alt.computeChains(depth+1,shared,visited);
	if(Pat != null) Pat.computeChains(depth+1,shared,visited);
    }

    String getPcode()
    {
	String s = "0000000000000000" + Integer.toHexString(hashCode());
	int len = s.length();
	s = s.substring(len-4,len);
//	return PatternCode.name(Pcode)+"["+s+"]";
	return PatternCode.name(Pcode);
    }

    String graphToString(int depth,
		    HashMap<PE,Integer> shared,
		    HashSet<PE> visited)
    {
	// Suppress PC_EOP
	if(Pcode == PC_EOP) return "";
	StringBuilder tos = new StringBuilder();
	if(Alt != null) tos.append(LPAREN);
	tos.append(getPcode());
	switch (PatternArg.argType(Pcode)) {
	    case PA_String:
	    {
		tos.append(LPAREN);
		String s = (Str == null?"<missing>":Str);
		tos.append(QuotedString.DQUOTE);
		tos.append(QuotedString.addEscapes(s));
		tos.append(QuotedString.DQUOTE);
		tos.append(RPAREN);
	    }; break;

	    case PA_Int:
		tos.append(LPAREN);
		tos.append(""+Int);
		tos.append(RPAREN);
		break;

	    case PA_Char:
		tos.append(LPAREN);
		tos.append(QuotedString.SQUOTE);
		QuotedString.addEscapedChar(Char,tos);
		tos.append(QuotedString.SQUOTE);
		tos.append(RPAREN);
		break;

	    case PA_Var:
	    {
		tos.append(LPAREN);
		String s = (Var == null?"<missing>":Var.Name);
		tos.append("+");
		tos.append(s);
		tos.append(RPAREN);
	    }; break;

	    case PA_External:
	    {
		tos.append(LPAREN);
		String s = Str;
		s += LPAREN;
		if(Argv != null) {
		    for(int i=0;i<Argv.length;i++) {
			if(i > 0) s += ",";
			if(s == null) s += "null";
			else s += Argv[i].toString();
		    }
		}
		s += RPAREN;
		tos.append(s);
		tos.append(RPAREN);
	    }; break;

	    case PA_None:
		break;
	}
	visited.add(this);
	if(Alt != null) {
	    tos.append(" |= ");
	    tos.append(LPAREN);
	    if(visited.contains(Alt))
		tos.append(Alt.getPcode()+"|...");
	    else
	        tos.append(Alt == null?"<missing>"
				      :Alt.graphToString(depth+1,shared,visited));
	    tos.append(RPAREN);
	    tos.append(RPAREN);
	}
	if(Pthen != null) {
	    if(visited.contains(Pthen)) {
		tos.append(" & ");
		tos.append(Pthen.getPcode()+"...");
	    } else {
		Integer level = shared.get(Pthen);
		if(level == depth)
		{
		    tos.append(" & ");
		    tos.append(Pthen.graphToString(depth,shared,visited));
		}
	    }
	}
        return tos.toString();
    }

    //////////////////////////////////////////////////
}

