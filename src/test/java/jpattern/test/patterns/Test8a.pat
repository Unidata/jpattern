package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test8a extends Test
{
    public Test8a() {super("8a");};



    public Pattern makePattern()
    {
	Pattern p = @"a" . result@;
	return p;
    }
}

