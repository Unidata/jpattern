package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test2b extends Test
{
    public Test2b() {super("2b");};


    Pattern B = null;
    Pattern N = null;
    Pattern T = null;

    public Pattern makePattern()
    {
	B = @nspan(" ")@;
	N = @span("0123456789")@;
	T = @nspan(" ") +N*Num1 span(" ,") +N*Num2@;
	vars.put("B",B);
	vars.put("N",N);
	vars.put("T",T);
	return T; // top level pattern
    }
}

