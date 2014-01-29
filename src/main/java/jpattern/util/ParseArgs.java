package jpattern.util;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class ParseArgs
{
    static public HashMap<String,Object> parse(String[] argv,
				   HashMap<String,Object> props,
				   String[] formals)
	throws Exception
    {
if(false) {for(String s:formals) System.err.println("formal="+s);}
	int argc = argv.length;
	int i;
parse:	for(i=0;i < argc;i++) {
	    String arg = canonical(argv[i]);
	    if(!arg.startsWith("-")) {
		// file all other args under the prop "--"
		ArrayList a = (ArrayList)props.get("--");
		if(a == null) {a = new ArrayList(); props.put("--",a);}
		a.add(arg);
		continue;
	    }	
	    if(arg.equals("-")) {
		// stop all further argument examination
		ArrayList a = (ArrayList)props.get("--");
		if(a == null) {a = new ArrayList<Object>(); props.put("--",a);}
		for(int j=i+1;j<argc;j++) a.add(argv[j]);
		break parse;
	    }
	    arg = arg.substring(1);
	    String formal = match(formals,arg);
	    if(formal == null)
		throw new Exception("-"+arg+": unknown option"); // invalid
	    // switch on tag char
	    char tag = (formal.charAt(formal.length()-1));
	    if(tag != '?' && i == argv.length)
	        throw new Exception("-"+arg+"requires an argument");
	    ArrayList<String> a;
	    switch (tag) {
		case '?': // simple flag
		    props.put(arg,Boolean.TRUE);
		    break;
		case '#': // single valued integer
		    i++;
		    if(i >= argc)
			throw new Exception("-"+arg+": requires an argument");
		    String n = argv[i].trim();
		    props.put(arg,new Integer(Integer.parseInt(n)));
		    break;
		case '=': // single valued
		    i++;
		    if(i >= argc)
			throw new Exception("-"+arg+": requires an argument");
		    props.put(arg,argv[i]);
		    break;
		case '*': // list valued		
		    a = (ArrayList)props.get(arg);
		    if(a == null) {a = new ArrayList(); props.put(arg,a);}
		    if(i >= argc)
			throw new Exception("-"+arg+": requires an argument");
		    collectWords(a,argv[++i]);
		    break;
		case '+': // list valued integers
		    a = (ArrayList)props.get(arg);
		    if(a == null) {a = new ArrayList(); props.put(arg,a);}
		    if(i >= argc)
			throw new Exception("-"+arg+": requires an argument");
		    collectInts(a,argv[++i]);
		    break;
	    }
	}
	return props;
      }

    static String match(String[] sa, String s)
    {
	if(sa == null) return null;
	for(int i=0;i<sa.length;i++) {
	    String m = sa[i].substring(0,sa[i].length()-1);
	    if(s.equals(m)) return sa[i];
	}
	return null;
    }

    static void collectWords(ArrayList a, String words)
    {
	if(words == null || a == null) return;
	StringTokenizer t = new StringTokenizer(words);
	while(t.hasMoreTokens()) {
	    a.add(t.nextToken()); // duplicates ok
	}
    }

    static void collectInts(ArrayList a, String words)
	throws Exception
    {
	if(words == null || a == null) return;
	StringTokenizer t = new StringTokenizer(words);
	while(t.hasMoreTokens()) {
	    String number = t.nextToken();
	    a.add(new Integer(Integer.parseInt(number.trim())));
	}
    }

    static public String canonical(String arg)
    {
	if (arg.startsWith("++")) arg = "-"+arg.substring(2);
	if (arg.startsWith("--")) arg = "-"+arg.substring(2);
	if (arg.startsWith("+")) arg = "-"+arg.substring(1);
	return arg;
    }


    static public String[] sconcat(String[] sa1, String[] sa2)
    {
	if(sa1 == null) return sa2;
	if(sa2 == null) return sa1;
	String[] cat = new String[sa1.length+sa2.length];
	System.arraycopy(sa1,0,cat,0,sa1.length);
	System.arraycopy(sa2,0,cat,sa1.length,sa2.length);
	return cat;
    }

}


