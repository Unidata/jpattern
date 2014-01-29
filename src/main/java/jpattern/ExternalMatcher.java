package jpattern;

//  Type used to record current state of a  match
//  Currently only used when calling Functions.

abstract public class ExternalMatcher
{
    public ExternalMatcher() {}

    public String toString()
    {
	return String.format("ExternalMatcher(%d,%d,|%s|)",
			     Anchor,
			     Cursor,
			     (Subject==null?"null":Subject));
    }
    // Subclasses should use only the following publicmethods
    // and public fields

    public boolean Cancel = false;
    public String Subject = null;
    public VarMap Vars = null;
    public ExternalMap Externs = null;
    //  Current starting index position(0 origin) of match
    //  with respect to the subject
    public int Anchor = 0;
    //  Current cursor position index position(0 origin)
    public int Cursor = 0;

    // This controls the match intial/retry.
    public boolean retry() 
    {return false;}
    public void fail(){}

    // Subclass defined
    abstract public boolean initial();
}
