package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;
import jpattern.ExternalPattern;
import jpattern.ExternalMatcher;
import jpattern.Failure;

import java.util.regex.PatternSyntaxException;

// Let us define a new external pattern called "rematch(<regular expression>)".
// This function attempts to repeatedly match the regular expression (RE) against the
// subject at the current cursor.  Thus, it is roughly similar to the
// altered regular expression RE+, which of course would be a legal
// RE for this pattern and negates the need for the implicit plus.
// Rather the point is to demonstrate how to do backtracking.
// The arguments are either a String representing a regular expression
// (as defined by java.util.regex) or a java.util.regex.Pattern instance.
// (passed, obviously as a Java variable using back quotes).

class REPattern implements ExternalPattern
{
    public REPattern() {}

    public String toString() {return "REPattern()";}

    //////////////////////////////////////////////////
    // ExternalPattern Interface
    public String getName() {return "RE";}
    public int getNargs() {return 1;}

    public ExternalMatcher matcher(Object[] argv) throws Failure
    {
	java.util.regex.Pattern jpat = null;
	// assert |argv| >= nargs
	// obtain the java.util.regex Pattern instance
	if(argv[0] instanceof String) {
	    String s = (String)argv[0];
	    if(s.length() == 0)
		throw new Failure("REPattern: zero length string argument: "+argv[0]);
	    try {
		jpat = java.util.regex.Pattern.compile(s);
	    } catch(java.util.regex.PatternSyntaxException pse) {
		throw new Failure("REPattern: illegal regular expression: "+s,pse);
	    }
	} else if(argv[0] instanceof java.util.regex.Pattern) {
	    jpat = (java.util.regex.Pattern)argv[0];
	} else
	    throw new Failure("REPattern: illegal argument: "+argv[0]);
        ExternalMatcher rem = new REMatcher(jpat);
	System.out.println("RE Matcher="+rem);
	return rem;
    }

    //////////////////////////////////////////////////
    final class REMatcher extends ExternalMatcher
    {
	// The only relevant state is the regular expression
	java.util.regex.Pattern regexp = null;

	public REMatcher(java.util.regex.Pattern jpat) {regexp = jpat;}

        public String toString() {return "REMatcher("+regexp+")";}

	////////////////////////////////////////////////////////////
	// Override relevant methods from parent class ExternalMatcher

	public boolean initial() throws Failure
	{
	    // Try the initial match. We want the maximum match
	    // starting at the current cursor location.

	    // test report 
	    System.out.println("initial: subject="+super.Subject);
	    System.out.println("initial: anchor="+super.Anchor);
	    System.out.println("initial: cursor="+super.Cursor);

	    // Get the substring to match (with some test reporting)
	    int curse = super.Cursor;
	    String target = super.Subject.substring(curse);
	    System.out.println("initial: target="+target); // test report
	    java.util.regex.Matcher m = regexp.matcher(target);
	    boolean sf = m.lookingAt(); // anchored RE match
	    System.out.println("initial: sf="+sf); // test report
	    if(!sf) return false; // failed		    
	    // set the new cursor
	    System.out.println("initial: start="+m.start()+"; end="+m.end());
	    super.Cursor = (curse+m.end());
	    return true;
	}
    
	public boolean retry() throws Failure
	{
	    // test report
	    System.out.println("retry: subject="+Subject);
	    System.out.println("retry: anchor="+Anchor);
	    System.out.println("retry: cursor="+Cursor);

	    // Get the substring to match
	    int curse = super.Cursor;
	    String target = Subject.substring(curse);
	    System.out.println("retry: target="+target);
	    java.util.regex.Matcher m = regexp.matcher(target);
	    boolean sf = m.lookingAt(); // anchored RE match
	    if(!sf) return false; // failed		    
	    System.out.println("retry: sf="+sf);
	    // set the new cursor
	    System.out.println("retry: start="+m.start()+"; end="+m.end());
	    super.Cursor = (curse+m.end());
	    return true;
	}
    }
}
    
public class Test12 extends Test
{
    public Test12() {super("12");};

    public Pattern makePattern()
    {
	Pattern p = @RE("[a][b]") & "c"@;
	externs.add(new REPattern());
	return p;
    }
}
