package jpattern.util;

import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Parameters extends HashMap<String,Object>
{
        static String timefmt = "yyyy-MM-dd HH:mm:ss.SSS";
        static SimpleDateFormat format = new SimpleDateFormat(timefmt);

	public Parameters() {super();}

	// Typed gets: string is default

	public String getEnv(String name)
	{
	    Object o = get(name);
	    return (o == null?null:o.toString());
	}

	public String getString(String name) {return getEnv(name);}

	public String getString(String name,String dfalt)
        {
	    String s = getString(name);
	    if(s == null) s = dfalt;
	    return s;
	}


	public int getInt(String name) {return getInt(name,Integer.MAX_VALUE);}

	public int getInt(String name, int dfalt)
	{
	    Object o = get(name);
	    if(o == null) return dfalt;
	    int n = dfalt;
	    if(o instanceof Number) n = ((Number)o).intValue();
	    try {
		n = Integer.parseInt(o.toString());
	    } catch (NumberFormatException nfe) {
		n = dfalt;
	    }
	    return n;
	}

	public boolean getBoolean(String name) {return getBoolean(name,false);}

	public boolean getBoolean(String name, boolean dfalt)
	{
	    Object o = get(name);
	    if(o == null) return dfalt;
	    boolean b = dfalt;
	    if(o instanceof Boolean) b = ((Boolean)o).booleanValue();
	    String s = o.toString();
	    if(s.equalsIgnoreCase("true")) b = true;
	    else if(s.equalsIgnoreCase("false")) b = false;
	    else b = dfalt;
	    return b;
	}

	public Date getDate(String name) {return getDate(name,null);}

	public Date getDate(String name, Date dfalt)
	{
	    Object o = get(name);
	    if(o == null) return dfalt;
	    Date d = forceDate(o);
	    if(d == null) d = dfalt;
	    return d;
	}

	public Object putEnv(String name, Object val) {return put(name,val);}

        public String toString()
	{
	    String s= null;
	    for(Iterator it=super.keySet().iterator();it.hasNext();) {
		String key = (String)it.next();
		if(s == null) s = ""; else s = (s+" ");
		s += (key+"=");
	        Object o = super.get(key);
		if(o instanceof ArrayList) {
		    ArrayList a = (ArrayList)o;
		    s += ("[");		
		    for(int i=0;i<a.size();i++) s += (i==0?"":" ")+a.get(i);
		    s+= ("]");
		} else if(o instanceof Integer) {
		    s += (o);
		} else
		    s += ("|"+o+"|");
	    }
	    return s;
	}

	public void load(String fname, boolean override) throws Error
	{
	    if(fname == null) return;
	    BufferedReader in = null;
	    try {
	        in = new BufferedReader(new FileReader(fname));
	    } catch(FileNotFoundException fnfe) {
	        throw new Error("File cannot be read: "+fname);
	    }
	    String line;
	    for(;;) {		
		try {
		    line=in.readLine();
		} catch (IOException ioe) {
		    throw new Error(ioe.toString());
		}
		if(line == null) break;
		load1(line,override);
	    }
	}

	// load a single name=value entry
	public void load1(String entry, boolean override) throws Error
	{
	    // there are 3 cases:
	    // 1. x=y	action: add pair(x,y)
	    // 2. x=	action: remove x
	    // 3. x	action add pair(x,Boolean.TRUE)

	    String name = null;
	    Object value = null;
	    int sep = entry.indexOf("=");
	    if(sep < 0) { // case 3
		name = entry;
		value = Boolean.TRUE;		
	    } else { // case 1 or 2
		name = entry.substring(0,sep);
		value = entry.substring(sep+1,entry.length());
		// canonicalize value
		if(value != null && value.toString().length() == 0)
		    value = null; //case 2
		// convert to an integer if possible
		if(value != null) {
		    try {
		        value = new Integer(Integer.parseInt(value.toString().trim()));
		    } catch (NumberFormatException nfe) {}
		}
	    }
	    if(override) {
		if(value == null) {
		    remove(name);
		} else {
		    putEnv(name,value);
		}
	    } else if(getEnv(name) == null) {
		if(value != null) {
		    putEnv(name,value);
		}
	    }
	}

        static Date forceDate(Object o)
        {
	    if(o == null) return null;
	    if(o instanceof Date) return (Date)o;
	    String d = o.toString();
	    try {
		if(d.length() == 0 || d.equals("null")) return null;
	        return (d==null?null:format.parse(d));
	    } catch(Exception e) {
		return null;
	    }
	}

}

