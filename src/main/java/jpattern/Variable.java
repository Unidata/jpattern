package jpattern;

// Use to define a variable name

public class Variable
{
    public String Name;
    public Variable(String name) {this.Name = name;}
    public String toString() {return "+"+Name;};

    // Kludge to keep whitespace out of compiled Java expressions
    static public Variable create(String name) {return new Variable(name);}
}
