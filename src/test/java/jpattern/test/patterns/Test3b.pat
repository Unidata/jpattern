package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

// Test3b builds Test3a1
public class Test3b extends Test3a1
{
    public Test3b() {super("3b");};
    public Test3b(String id) {super(id);};

    Pattern Bchar = null;

    public Pattern makePattern()
    {
	super.makePattern(); // make 3a1 patterns
	Bchar = @any("#:")@;
	vars.put("Bchar",Bchar);
	//Override the definition of Bnum in 3a1
	Bnum = @+UDigs +Bchar +UHdig +Bchar@;
	vars.put("Bnum",Bnum);
	return Bnum; // top level pattern
    }
}

