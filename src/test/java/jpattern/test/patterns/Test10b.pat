package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test10b extends Test
{
    public Test10b() {super("10b");};

    public Pattern makePattern()
    {
	Pattern p = @(len(1) $ x) & (len(1) = x)@;
	return p;
    }
}

