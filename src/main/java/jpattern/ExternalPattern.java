package jpattern;

// Allow user defined pattern operators
// to extend the pattern matcher.

public interface ExternalPattern
{
    public String getName();
    public int getNargs();
    public ExternalMatcher matcher(Object[] argv) throws Error;
}

