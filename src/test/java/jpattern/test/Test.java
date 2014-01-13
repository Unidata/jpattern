package jpattern.test;

import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.Variable;
import jpattern.ExternalMap;

abstract public class Test implements PatternBuilder
{
    String ID = null;
    Pattern TestPattern = null;
    VarMap vars = null;
    ExternalMap externs = null;

    public Test(String id) {ID=id;}

    public void buildPattern(VarMap v, ExternalMap em)
    {
	vars = v;
	externs = em;
	System.out.println("Example "+ID);
        TestPattern =  makePattern();
	vars.put("TestPattern",TestPattern);
    }

    abstract public Pattern makePattern();
}

