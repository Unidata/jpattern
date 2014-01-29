package jpattern;

public class Failure extends RuntimeException
{
    public Failure() {super();}
    public Failure(String s, Exception e) {super(s,e);}
    public Failure(String s) {super(s);}
    public Failure(String fmt, Object... args)
	{super(String.format(fmt,args));}
}
