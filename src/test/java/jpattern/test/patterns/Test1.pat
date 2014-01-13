package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test1 extends Test
{
    public Test1() {super("1");}

    public Pattern makePattern()
    {
	// Use the ability to embed java code to include Digs
	Pattern Digs = @span("0123456789")@;
	Pattern Lnum = @pos(0) (`Digs` ".") span(" ")@;
	vars.put("Digs",Digs);
	vars.put("Lnum",Lnum);
	return Lnum;
    }
}

