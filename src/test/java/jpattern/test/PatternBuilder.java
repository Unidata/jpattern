package jpattern.test;

import jpattern.VarMap;
import jpattern.ExternalMap;

public interface PatternBuilder
{
    public void buildPattern(VarMap vars, ExternalMap externs);
}
