package jpattern.util;

import java.io.PrintWriter;

public class Debug
{
    static PrintWriter out = new PrintWriter(System.err);
    static int depth = 0;
    static public int level = 0;

    static public void setDebug(boolean x)
    {
        setDebugn(x ? 1 : 0);
    }

    static public void setDebugn(int x)
    {
        Debug.level = x;
    }

    static public void setWriter(PrintWriter p)
    {
        Debug.out = p;
    }

    static public void setDepth(int x)
    {
        Debug.depth = x;
    }

    // Unconditional printing
    static public void Println()
    {
        out.println();
	out.flush();
    }

    static public void Println(String f, Object... Args)
    {
        out.println(String.format(f, Args));
	out.flush();
    }

    static public void Println(String s)
    {
        out.println(s);
	out.flush();
    }

    static public void Print(String s)
    {
        out.print(s);
	out.flush();
    }

    static public void Print(String f, Object... Args)
    {
        out.print(String.format(f, Args));
	out.flush();
    }

    //////////////////////////////////////////////////

    static public void println(int l)
    {
        if(level >= l) {
            out.println("");
            out.flush();
        }
    }

    static public void println(int l, String f, Object... args)
    {
        if(level >= l) {
	    out.println(String.format(f, args));
	    out.flush();
	}
    }

    static public void println(int l, String Str)
    {
        if(level >= l) {
            out.println(Str);
            out.flush();
        }
    }
}
