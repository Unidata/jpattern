package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test8b extends Test
{
    public Test8b() {super("8b");};

    public Pattern makePattern()
    {
	Pattern p = @"a" $ result@;
	return p;
    }
}

