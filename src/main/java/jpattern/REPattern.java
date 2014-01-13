import jpattern.*;
import jpattern.compiler.*;
import jpattern.util.*;
import jpattern.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;


public class REPattern implements ExternalPattern
{
    public REPattern() {}

    public String toString() {return "REPattern()";}

    //////////////////////////////////////////////////
    // ExternalPattern Interface
    public String getName() {return "RE";}
    public int getNargs() {return 1;}

    public ExternalMatcher matcher(Object[] argv) throws java.lang.Error
    {
	java.util.regex.Pattern jpat = null;
	// assert |argv| >= nargs
	// obtain the java.util.regex Pattern instance
	if(argv[0] instanceof String) {
	    String s = (String)argv[0];
	    if(s.length() == 0)
		throw new java.lang.Error("REPattern: zero length string argument: "+argv[0]);
	    try {
		jpat = java.util.regex.Pattern.compile(s);
	    } catch(java.util.regex.PatternSyntaxException pse) {
		throw new java.lang.Error("REPattern: illegal regular expression: "+s,pse);
	    }
	} else if(argv[0] instanceof java.util.regex.Pattern) {
	    jpat = (java.util.regex.Pattern)argv[0];
	} else
	    throw new java.lang.Error("REPattern: illegal argument: "+argv[0]);
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

	public boolean initial() throws java.lang.Error
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
    
	public boolean retry() throws java.lang.Error
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
