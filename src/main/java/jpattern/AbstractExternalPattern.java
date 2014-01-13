package jpattern;

// Define an abstract instance class for ExternalPattern to
// simplify its use.

abstract public class AbstractExternalPattern
			implements ExternalPattern
{
    String _id = null;
    int _nargs = 0;
    public AbstractExternalPattern(String id, int nargs)
	{this._id = id; this._nargs = nargs;}
    public AbstractExternalPattern(String id) {this(id,0);}

    // Useful, but not part of ExternalPattern
    public String toString() {return "AbstractExternalPattern("+_id+")";}

    // Shared Interface ExternalPattern methods
    public String getName() {return _id;}
    public int nargs() {return _nargs;};
}
