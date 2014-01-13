package jpattern;

// Used to hold an arbitrary value in the map
// passed to the Match() function. 

public interface ExternalVariable
{
    public Object get(VarMap vars);
    public void put(VarMap vars, Object value);
}
