package jpattern.util;

import java.lang.reflect.Method;

public class Factory
{
    static String[] packagelist = new String[]{""};

    static public void initialize(String[] pl) {packagelist = pl;}

    static public Class getClass(String cname) throws Error
	{return getClass(cname,null);}

    static public Class getClass(String clname, String defalt) throws Error
    {
	if(clname == null) clname = defalt;
	if(clname == null)
	    throw new Error("Factory: null class specified");
	Class cl = null;
	for(String prefix : packagelist) {
	    try {
	        if(prefix.length()==0) prefix = null;
		String fullname = (prefix==null?clname:(prefix+"."+clname));
	        cl = Class.forName(fullname);
		break; // only reached if class found
	    } catch (ClassNotFoundException cnfe) {cl=null;}
	}
	if(cl == null)
	    throw new Error("Factory.getClass: class not found: "+clname);
	return cl;
    }

    static public Object newInstance(String cname) throws Error
    {
	Class cl = getClass(cname);
	return newInstance(cl);
    }

    static public Object newInstance(Class cl) throws Error
    {
	try {
	    return cl.newInstance();
	} catch (InstantiationException ie) {
	    throw new Error(ie.toString());
	} catch (IllegalAccessException iae) {
	    throw new Error(iae.toString());
	}
    }

    static Object getProperty(String cname, String pname) throws Error
	{return Factory.getProperty(Factory.getClass(cname),pname);}

    static Object getProperty(Class cl, String pname) throws Error
    {
	try {
	    Method m = cl.getMethod(pname,(Class[])null);
	    return m.invoke(null);
	} catch (Exception e) {
	    throw new Error(e.toString());
	}
    }

/*
    // classInitialize method may or may not have a single argument
    static void classInitialize(Class cl) throws Error
    {
	try {
	    Method m
		= cl.getMethod(Constants.DEFAULTCLASSINIT,
				(Class[])null);
	    m.invoke(null);
	    return;
	} catch (Error e) {
	    throw new Error(e.toString());
	}
    }

    static void classInitialize(Class cl, Object arg) throws Error
    {
	try {
	    Method m
		= cl.getMethod(Constants.DEFAULTCLASSINIT,Object.class);
	    m.invoke(null,arg);
	    return;
	} catch (Error e) {
	    throw new Error(e.toString());
	}
    }
*/

}