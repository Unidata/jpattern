package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test6 extends Test
{
    public Test6() {super("6");};



    public Pattern makePattern()
    {
	Pattern p = @fence(arb & "b") & "c"@;
	return p;
    }
}

