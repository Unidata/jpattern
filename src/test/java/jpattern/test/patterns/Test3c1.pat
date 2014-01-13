package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

// 3c1 builds on 3b (which builds on 3a1)
public class Test3c1 extends Test3b
{
    public Test3c1() {super("3c1");};
    public Test3c1(String id) {super(id);};

    Pattern Bnum = null;

    public Pattern makePattern()
    {
	super.makePattern();
	// Override Bnum from 3b
	Bnum = @+UDigs +Bchar$Temp +UHdig +Temp@;
	vars.put("Bnum",Bnum); // override
	return Bnum; // top level pattern
    }
}

