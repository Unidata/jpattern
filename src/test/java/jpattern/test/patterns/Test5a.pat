package jpattern.test;

import jpattern.ExternalVariable;
import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

// Test the read and write of an ExternalVariable

public class Test5a extends Test
{
    ExternalVar extvar;

    public Test5a() {super("5a");  extvar = new ExternalVar("before");};

    public Pattern makePattern()
    {
	vars.put("ExternalVar",extvar);
	Pattern p = @(+ExternalVar & span(" ")) $ +ExternalVar & "after" = +ExternalVar@;
	return p; // top level pattern
    }
}

class ExternalVar implements ExternalVariable
{
    Object current = null;

    public ExternalVar(Object o) {current=o;}

    public String toString() {return "ExternalVar("+current+")";}

    // Interface Extvar methods
    public Object get(VarMap vars) {
System.out.println("extvar.get: "+current);
return current;}
    public void put(VarMap vars, Object o) {
System.out.println("extvar.put: "+current+"->"+o);
current = o;
}
}

