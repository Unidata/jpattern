package jpattern;

import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Arrays;

public class VarMap extends HashMap<String,Object>
{
    public VarMap() {super();}

    // Pure set/get
    public void put(Variable var, Object value) {super.put(var.Name,value);}

    public Object get(Variable var) {return super.get(var.Name);}

    // get/put that take ExternalVariables into account

    public Object read(Variable var) {return read(var.Name);}

    public Object read(String var)
    {
	Object o = this.get(var);
	if(o != null && (o instanceof ExternalVariable)) {
	    o = ((ExternalVariable)o).get(this);
	}
	return o;
    }

    // Typed gets

    public int getInt(Variable var) {return this.getInt(var,-1);}

    public int getInt(Variable var, int dfalt) {return getInt(var.Name,dfalt);}

    public int getInt(String var, int dfalt)
    {
	Object o = this.read(var);
	if(o == null) return dfalt;
	if(o instanceof Number) return ((Number)o).intValue();
	if(o instanceof Boolean) return ((Boolean)o).booleanValue()?1:0;
	if(o instanceof String) return Integer.parseInt((String)o);
	return dfalt;
    }

    public String getString(Variable var) {return this.getString(var,null);}

    public String getString(Variable var, String dfalt)
	{return getString(var.Name,dfalt);}

    public String getString(String var, String dfalt)
    {
	Object o = this.read(var);
	if(o == null) return dfalt;
	return String.valueOf(o);
    }

    public boolean getBoolean(Variable var)
        {return this.getBoolean(var,false);}

    public boolean getBoolean(Variable var, boolean dfalt)
	{return getBoolean(var.Name,dfalt);}

    public boolean getBoolean(String var, boolean dfalt)
    {
	Object o = this.read(var);
	if(o == null) return dfalt;
	if(o instanceof Boolean) return ((Boolean)o).booleanValue();
	if(o instanceof Number) return ((Number)o).intValue()==0?false:true;
	if(o instanceof String) return Boolean.parseBoolean((String)o);
	return dfalt;
    }

    // Pattern dfalt is always null
    public Pattern getPattern(Variable var) {return getPattern(var.Name);}

    public Pattern getPattern(String var)
    {
	Object o = this.read(var);
	if(o == null || !(o instanceof Pattern)) return null;
	return (Pattern)o;
    }

    //////////////////////////////////////////////////
    // Variable writes

    public void write(Variable var, Object val) {write(var.Name,val);}

    public void write(String var, Object value)
    {
	Object o = this.get(var);
	if(o == null) {
	    this.put(var,value);
	} else {
	    // handle collections and ExternalVariables
	    if(o instanceof ExternalVariable) {
		((ExternalVariable)o).put(this,value);
	    } else if(o instanceof Collection) {
		// add value to collection
		((Collection)o).add(value);
	    } else
	        this.put(var,value);
	}
    }

    //////////////////////////////////////////////////
    public String toString()
    {
        String s = "{";
	boolean first = true;
	String[] keys = sortKeys();
	for(String key: keys) {
	    s += (first?"":" ");
	    s += (key+"=");
	    Object o = super.get(key);
	    if(o instanceof ExternalVariable) {
		s += o.toString();
	    } else if(o instanceof Collection) {
	        s += "[";
		boolean one = true;
	        for(Object x : ((Collection)o)) {
		    s += (one?"":" ");
		    s += x.toString();
		    one = false;
		}
	        s += "]";
	    } else {
	        s += ("|"+o+"|");
	    }
	}
	s += "}";
        return s;
    }

    public String prettyPrint()
    {
        String s = "";
	String[] keys = sortKeys();
	for(String key: keys) {
	    s += (key+"=");
	    Object o = super.get(key);
	    if(o instanceof ExternalVariable) {
		s += o.toString();
	    } else if(o instanceof Collection) {
	        s += "[";
		boolean one = true;
	        for(Object x : ((Collection)o)) {
		    s += (one?"":" ");
		    s += x.toString();
		    one = false;
		}
	        s += "]";
	    } else {
	        s += ("|"+o+"|");
	    }
	    s += "\n";
	}
        return s;
    }

    public String[] sortKeys()
    {
	String[] keys = new String[size()];
	int index = 0;
	for(Iterator it=keySet().iterator();it.hasNext();) {
	    String key = (String)it.next();
	    keys[index++] = key;	    
	}		
	Arrays.sort(keys);
	return keys;	
    }


}
