package jpattern;

import java.io.BufferedReader;
import java.io.IOException;

public class InputVariable implements ExternalVariable, SystemObject
{
    static final String EOL = "\n"; // use unix line terminator

    BufferedReader input = null;

    public InputVariable(BufferedReader br)
    {
        this.input = br;
    }

    // ExternalVariable Interface  methods
    public Object get(VarMap vars)
    {
        String line = null;
        try {
            line = input.readLine();
        } catch(IOException ioe) {};
        if(line == null) // eof
            return "";
        return line + EOL;
    }

    public void put(VarMap vars, Object o)
    {
        // ignore
    }
}


