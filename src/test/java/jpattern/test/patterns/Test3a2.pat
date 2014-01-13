package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test3a2 extends Test
{
    public Test3a2() {super("3a2");};


    Pattern Digs = null;
    Pattern UDigs = null;
    Pattern Hdig = null;
    Pattern UHdig = null;
    Pattern Bnum = null;

    public Pattern makePattern()
    {
	vars.put("DecDigits","0123456789");
	vars.put("HexDigits","0123456789abcdefABCDEF");
	Digs = @span(+DecDigits)@;
	UDigs = @+Digs arbno("_" +Digs)@;
	Hdig = @span(+HexDigits)@;
	UHdig = @+Hdig arbno("_" +Hdig)@;
	Bnum = @+UDigs "#" +UHdig "#"@;
	vars.put("Digs",Digs);
	vars.put("UDigs",UDigs);
	vars.put("Hdig",Hdig);
	vars.put("UHdig",UHdig);
	vars.put("Bnum",Bnum);
	return Bnum; // top level pattern
    }
}

