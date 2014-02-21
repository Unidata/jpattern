package jpattern.test;

import jpattern.ExternalVariable;
import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

// Test the use of ExternalVariable
// to do a more general computation

public class Test5b extends Test
{
    GTS gts;

    public Test5b() {super("5b");  gts = new GTS();};

    // Use the jpattern.ExternalVariable mechanism to
    // access a Java variable at pattern time and
    // match time while changing its value in between.

    public Pattern makePattern()
    {
	vars.put("GTS",gts);
	vars.put("Digit","0123456789");
	Pattern Digs = @span(+Digit)@;
	// Note that the comments are inside the backslash
	Pattern Find = @""*Max fence           // initialize Max to null \
        	        & breakx(+Digit)       // scan looking for digits \
	                & ((span(+Digit)*Cur   // assign next string to Cur \
	                    & +GTS             // check size(Cur) > Size(Max) \
	                    & setcur(+Loc))    // if so, save end position\
	                   * Max)              // and assign to Max. \
	                & fail@;               // seek all alternatives
	return Find; // top level pattern
    }
}

class GTS implements ExternalVariable
{
    public GTS() {};
    public String toString() {return "GTS()";}
    // ExternalVariable interface
    public Object get(VarMap vars) {
        String Cur = vars.getString("Cur","");
        String Max = vars.getString("Max","");
        return Boolean.valueOf(Cur.length() > Max.length());
    }
    public void put(VarMap vars, Object val) {} // not used
}
