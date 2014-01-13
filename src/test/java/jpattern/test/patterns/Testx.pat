package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Testx extends Test
{
    public Testx() {super("13");};

    public Pattern makePattern()
    {
	Pattern p = @span("abc")@;
	return p;
    }
}

