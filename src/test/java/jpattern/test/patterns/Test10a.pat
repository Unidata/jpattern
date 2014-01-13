package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test10a extends Test
{
    public Test10a() {super("10a");};

    public Pattern makePattern()
    {
	// use embedded Java code
	int LEN = 1;
	Pattern p = @(len(1) $ x) & (len(`LEN`) = +x)@;
	return p;
    }
}

