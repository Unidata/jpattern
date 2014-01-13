package jpattern.util;

public class Error extends Exception
{
    public Error() {super();}
    public Error(String s, Exception e) {super(s,e);}
    public Error(String s) {super(s);}
    public Error(String fmt, Object... args)
	{super(String.format(fmt,args));}
}