package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test13 extends Test
{
    public Test13() {super("13");};

    public Pattern makePattern()
    {
	Pattern p = @span("abc")@;
	return p;
    }
}

