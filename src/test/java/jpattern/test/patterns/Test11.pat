package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test11 extends Test
{
    public Test11() {super("11");};

    public Pattern makePattern()
    {
	Pattern p = @fence bal("[]") fail@;
	return p;
    }
}

