package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

public class Test3a3 extends Test
{
    public Test3a3() {super("3a3");};

    Pattern Digs = null;
    Pattern UDigs = null;
    Pattern Hdig = null;
    Pattern UHdig = null;
    Pattern Bnum = null;

    public Pattern makePattern()
    {
	// This is the same as Test3a1, but using embedded java code
	String DecDigits = "0123456789";
	String HexDigits = "0123456789abcdefABCDEF";
	Digs = @span(`DecDigits`)@;
	UDigs = @`Digs` arbno("_" `Digs`)@;
	Hdig = @span(`HexDigits`)@;
	UHdig = @`Hdig` arbno("_" `Hdig`)@;
	Bnum = @`UDigs` "#" `UHdig` "#"@;
	vars.put("Bnum",Bnum);
	return Bnum; // top level pattern
    }
}

