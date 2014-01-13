package jpattern.test;

import jpattern.ExternalVariable;
import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

// Test the read and write of an ExternalVariable
// in deferred mode

public class Test5c extends Test
{
    Check_1 extvar;

    public Test5c() {super("5c");  extvar = new Check_1("before");};

    public Pattern makePattern()
    {
	vars.put("Check_1",extvar);
	Pattern p = @len(1)**Check_1@;
	return p; // top level pattern
    }
}

class Check_1 implements ExternalVariable
{
    Object current = null;

    public Check_1(Object o) {current=o;}

    public String toString() {return "Check_1("+current+")";}

    // Interface Extvar methods
    public Object get(VarMap vars) {
System.out.println("extvar.get: "+current);
return current;}
    public void put(VarMap vars, Object o) {
System.out.println("extvar.put: "+current+"->"+o);
current = o;
}
}

