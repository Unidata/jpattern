package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;

import java.util.ArrayList;

public class Test4 extends Test
{
    public Test4() {super("4");};


    Pattern Capture = null;
    Pattern Element = null;
    Pattern Balanced_String = null;

    public Pattern makePattern()
    {
	Element = @notany("[]{}") \
			   | ("[" Balanced_String "]") \
			   | ("{" Balanced_String  "}")@;
	Balanced_String = @Element arbno(Element)@;
	Capture = @Balanced_String$Output fail@;
	vars.put("Element",Element);
	vars.put("Balanced_String",Balanced_String);
	vars.put("Capture",Capture);
	vars.put("Output",new ArrayList()); // capture everything
	return Capture; // top level pattern
    }

//Match("xy[ab{cd}]", Balanced_String*Current_Output Fail")
//x xy xy[ab{cd}] y y[ab{cd}] [ab{cd}] a ab ab{cd} b b{cd} {cd} c cd d
}

