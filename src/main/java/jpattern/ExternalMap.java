package jpattern;

import jpattern.Variable;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

public class ExternalMap extends HashMap<String,ExternalPattern>
{
    public ExternalMap() {super();}

    public void add(ExternalPattern ep)
    {
	super.put(ep.getName(),ep);
    }

    public String toString()
    {
        String s = "{";
	boolean first = true;
	for(Iterator it=keySet().iterator();it.hasNext();first=false) {
	    String key = (String)it.next();
	    s += (first?"":" ");
	    s += (key+"=");
	    ExternalPattern o = super.get(key);
	    s += o.toString();
	}
	s += "}";
        return s;
    }

    public String prettyPrint() {return this.toString();}
}
