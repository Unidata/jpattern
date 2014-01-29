package jpattern.util;

import jpattern.Failure;

import java.lang.reflect.Method;

public class Factory
{
    static String[] packagelist = new String[]{""};

    static public void initialize(String[] pl) {packagelist = pl;}

    static public Class getClass(String cname) throws Failure
	{return getClass(cname,null);}

    static public Class getClass(String clname, String defalt) throws Failure
    {
	if(clname == null) clname = defalt;
	if(clname == null)
	    throw new Failure("Factory: null class specified");
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
	    throw new Failure("Factory.getClass: class not found: "+clname);
	return cl;
    }

    static public Object newInstance(String cname) throws Failure
    {
	Class cl = getClass(cname);
	return newInstance(cl);
    }

    static public Object newInstance(Class cl) throws Failure
    {
	try {
	    return cl.newInstance();
	} catch (InstantiationException ie) {
	    throw new Failure(ie.toString());
	} catch (IllegalAccessException iae) {
	    throw new Failure(iae.toString());
	}
    }

    static Object getProperty(String cname, String pname) throws Failure
	{return Factory.getProperty(Factory.getClass(cname),pname);}

    static Object getProperty(Class cl, String pname) throws Failure
    {
	try {
	    Method m = cl.getMethod(pname,(Class[])null);
	    return m.invoke(null);
	} catch (Exception e) {
	    throw new Failure(e.toString());
	}
    }

/*
    // classInitialize method may or may not have a single argument
    static void classInitialize(Class cl) throws Failure
    {
	try {
	    Method m
		= cl.getMethod(Constants.DEFAULTCLASSINIT,
				(Class[])null);
	    m.invoke(null);
	    return;
	} catch (Exception e) {
	    throw new Failure(e.toString());
	}
    }

    static void classInitialize(Class cl, Object arg) throws Failure
    {
	try {
	    Method m
		= cl.getMethod(Constants.DEFAULTCLASSINIT,Object.class);
	    m.invoke(null,arg);
	    return;
	} catch (Exception e) {
	    throw new Failure(e.toString());
	}
    }
*/

}
