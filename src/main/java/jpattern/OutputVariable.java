package jpattern;

import java.io.PrintWriter;

public class OutputVariable implements ExternalVariable, SystemObject
{
    PrintWriter output = null;

    public OutputVariable(PrintWriter pw)
    {
        this.output = pw;
    }

    // ExternalVariable Interface  methods
    public Object get(VarMap vars)
    {
        return this;
    }

    public void put(VarMap vars, Object o)
    {
        if(o == null) o = "";
        output.println(o.toString());
        output.flush();
    }
}

