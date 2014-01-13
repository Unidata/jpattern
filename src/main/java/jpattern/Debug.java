package jpattern.util;

import java.io.PrintWriter;

public class Debug
{
    static PrintWriter out = new PrintWriter(System.err);
    static int depth = 0;
    static public int debugLevel = 0;

    static public void setDebug(boolean x) {Debug.debugLevel=(x?1:0);}
    static public void setDebugn(int x) {Debug.debugLevel=x;}
    static public void setWriter(PrintWriter p) {Debug.out = p;}
    static public void setDepth(int x) {Debug.depth = x;}

    // Unconditional printing
    static public void Println() {self.println();}
    static public void Println(String f, Object ... Args) {self.println(f,Args);}
    static public void Println(String s) {self.println(s);}
    static public void Print(String s) {self.print(s);}
    static public void Print(String f, Object ... Args) {self.print(f,Args);}

    //////////////////////////////////////////////////

    static Debug self = new Debug();
    static DebugNull selfnull = new DebugNull();

    static public Debug level(int l) {return islevel(l)?self:selfnull;}
    static public boolean islevel(int l)
    {
	return (debugLevel >= l);
    }
    //////////////////////////////////////////////////

    public Debug() {};

    public void println() {println("");}
    public void println(String f, Object ... args) {println(String.format(f,args));}
    public void println(String Str) {if(debugLevel > 0) {out.println(Str); out.flush();}}
    public void print(String f, Object ... args) {print(String.format(f,args));}
    public void print(String Str) {if(debugLevel > 0) {out.print(Str); out.flush();}}
}

class DebugNull extends Debug
{
    public void println() {}
    public void println(String f, Object ... args) {}
    public void println(String Str) {}
    public void print(String f, Object ... args) {}
    public void print(String Str) {}
}