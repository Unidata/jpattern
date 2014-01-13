package jpattern;

import jpattern.util.Debug;
import jpattern.util.QuotedString;
import jpattern.Variable;

import static jpattern.PatternCode.*;
import static jpattern.util.JpatternConstants.EOP;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;

abstract public class Pattern extends PatternBuilder
{
    //////////////////////////////////////////////////
    // Public member Implementation
    //////////////////////////////////////////////////

    public Matcher matcher()
	{return new Matcher(this,null,null,null);}

    public Matcher matcher(String subject)
	{return new Matcher(this,subject,null,null);}

    public Matcher matcher(String subject, VarMap vars)
	{return new Matcher(this,subject,vars,null);}

    public Matcher matcher(String subject, VarMap vars, ExternalMap externs)
	{return new Matcher(this,subject,vars,externs);}

    // Print out just this Pattern node
    abstract public String toString();

    // Assume cycles not possible
    abstract public String graphToString();

}
