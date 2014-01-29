package jpattern.test;

import jpattern.compiler.Compiler;

import jpattern.util.*;
import jpattern.*;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

/*
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
*/

public class Driver
{
    // Following are static because they are referenced
    // in other classes
    static Parameters parms;

    static InputStreamReader reader;
    static OutputStreamWriter writer;
    static OutputStreamWriter errwriter;
    static BufferedReader stdin;
    static PrintWriter stdout;
    static PrintWriter stderr;

    static VarMap vars = new VarMap();
    static ExternalMap externs = new ExternalMap();

    // Misc constants
    static char DQUOTE = QuotedString.DQUOTE;

    static final String DFALT_PACKAGE = "jpattern.test";
    static final String DFALT_PATTERN = "TestPattern";

    // Define defalt cmdline arguments
    static String[] defaultFormals = new String[]{
        "envfile=",
        "env*",
        "f=",
        "o=",
        "err=",
        "debug?",
        "debugn#",
        "test=",
        "subject=",
        "pattern=",
        "package=",
        "anchor?"
    };

    String testpattern = null; // default primary pattern

    //////////////////////////////////////////////////

    static public void main(String[] argv) throws Exception
    {
        Driver main = new Driver();
        main.initialize(argv);
        main.start();
        main.cleanup();
        System.exit(0);
    }

    //////////////////////////////////////////////////

    // should only be called from subclass main
    void initialize(String[] argv) throws Exception
    {
        // Load in order, with later overriding earlier
        // 1. this class defaults
        // 2. env file
        // 3. command-line args

        Parameters mainparms = new Parameters(); // case 1
        Parameters envparms = new Parameters(); // case 2
        Parameters cmdparms = new Parameters(); // case 3

        setDefaults(mainparms); // case 1: main class defaults
        ParseArgs.parse(argv, (HashMap) cmdparms, getFormals());// case 2
        // Use cmdparms to find any env or envfile parameters

        // load any  env args first
        ArrayList envargs = (ArrayList) cmdparms.get("env");
        if(envargs != null) {
            for(int i = 0;i < envargs.size();i++)
                envparms.load1((String) envargs.get(i), false);
        }
        // load the env file, if any
        String envf = cmdparms.getString("envfile");
        if(envf != null) {
            envparms.load(envf, false);
        }

        // Now merge all the parameter sets
        parms = new Parameters();
        parms.putAll(mainparms);
        parms.putAll(envparms);
        parms.putAll(cmdparms);

        // Dump the arguments if -v
        if(parms.getBoolean("v")) {
            System.err.println("parms=" + parms.toString());
        }

        // Initialize any static class values for this and other classes
        // (must be a better way to do this)
        int lev = parms.getInt("debugn", 0);
        Debug.setDebugn(lev);
        if(parms.getBoolean("debug")) Debug.setDebug(true);

        Driver.reader = new InputStreamReader(System.in);
        Driver.writer = new OutputStreamWriter(System.out);
        Driver.errwriter = new OutputStreamWriter(System.err);

        String infile = parms.getEnv("stdin");
        if(infile == null) infile = parms.getEnv("f");
        if(infile != null) {
            Driver.reader = new FileReader(infile);
        }

        String outfile = parms.getEnv("stdout");
        if(outfile == null) outfile = parms.getEnv("o");
        if(outfile != null) {
            Driver.writer = new FileWriter(outfile);
        }

        String errfile = parms.getEnv("stderr");
        if(errfile == null) errfile = parms.getEnv("err");
        if(errfile != null) {
            Driver.errwriter = new FileWriter(errfile);
        }

        Driver.stdin = new BufferedReader(Driver.reader);
        Driver.stdout = new PrintWriter(Driver.writer, true);
        Driver.stderr = new PrintWriter(Driver.errwriter, true);

    }

    //////////////////////////////////////////////////
    // Subclass overrides

    public void start() throws Exception
    {
        boolean anchor = parms.getBoolean("anchor");
        testpattern = parms.getString("pattern", DFALT_PATTERN);
        String subject = parms.getString("subject");
        String testclass = parms.getString("test");
        String prefix = parms.getString("package", DFALT_PACKAGE);
        if(subject == null)
            subject = "";
        if(testclass == null)
            throw new Exception("-test must be specified");
        String className = prefix + "." + testclass;
        processClass(className, subject, anchor);

    }

    void processClass(String clname, String subject, boolean anchor)
        throws Exception
    {
        Test pb = (Test) Factory.newInstance(clname);
	anchor = pb.anchorMode();	
        vars.clear();
        externs.clear();
        pb.buildPattern(vars, externs);
        stdout.println("Testclass: " + clname);
        stdout.println("Defined patterns:");
        printVars(vars, false);
        if(externs.size() > 0) {
            stdout.println("Testclass: " + clname + "; Extern Set:");
            for(String key : externs.keySet())
                stdout.println(key + "=" + externs.get(key).toString());
        }
        Object p = vars.get(testpattern);
        if(!(p instanceof Pattern))
            throw new Exception(testpattern + " not a pattern name in class " + clname);
        stdout.println("match: subject="
            + DQUOTE + QuotedString.addEscapes(subject) + DQUOTE
            + " pattern=" + testpattern);
        match((Pattern) p, subject, anchor);
    }

    void match(Pattern p, String subject, boolean anchor)
        throws Exception
    {
        Matcher matcher = p.matcher();
        if(anchor) matcher.setAnchorMode(true);
        matcher.setIO(stdin,stdout,stderr);
        MatchResult result = (MatchResult) matcher; // alter ego
        boolean ok = matcher.match(subject, vars, externs);
        String s = result.getSubject();
        if(ok) {
            stdout.println("matchresult=" + result);
            stdout.print("succeed: ");
            stdout.print("|");
            String prefix = s.substring(0, result.getStart());
            String middle = s.substring(result.getStart(), result.getStop());
            String suffix = s.substring(result.getStop(), s.length());
            stdout.print(prefix + "^" + middle + "^" + suffix);
            stdout.println("|");
        } else {
            stdout.println("fail.");
        }
        stdout.println("(non-pattern) vars after:");
        stdout.println("----------");
        printVars(vars, true);
        stdout.println("----------");
    }

    public void cleanup() throws Exception
    {
    }

    ;

    public String[] getFormals()
    {
        return Driver.defaultFormals;
    }

    public void setDefaults(Parameters parms)
    {
        ArrayList<String> list = new ArrayList<String>();
        list.add("");
        list.add("snobol");
        list.add("jpattern.util");
        list.add("jpattern.compiler");
        parms.put("packageprefixes", list);
    }

    //////////////////////////////////////////////////

    static void usage(String msg)
    {
        usage(msg, null);
    }

    static void usage(Exception e)
    {
        usage(e.toString(), e);
    }

    static void usage(String msg, Exception e)
    {
        System.err.println(msg);
        if(e != null)
            e.printStackTrace(System.err);
        System.exit(1);
    }

    //////////////////////////////////////////////////
    // Misc. Common routines

    static public boolean contains(int[] indices, int i)
    {
        if(indices == null) return false;
        for(int si : indices) if(si == i) return true;
        return false;
    }

    static public boolean contains(String[] ss, String s)
    {
        if(ss == null) return false;
        for(String si : ss) if(s.equals(si)) return true;
        return false;
    }

    static public String[] union(String[] fs1, String[] fs2)
    {
        String[] tmp = new String[fs1.length + fs2.length];
        int last = 0;
        if(fs1 != null) {
            System.arraycopy(fs1, 0, tmp, 0, fs1.length);
            last = fs1.length;
        }
        if(fs2 != null) {
            for(String s : fs2) {
                if(contains(fs1, s)) continue;
                tmp[last++] = s;
            }
        }
        String[] result = new String[last];
        System.arraycopy(tmp, 0, result, 0, last);
        return result;
    }

    static public String[] minus(String[] fs1, String[] fs2)
    {
        if(fs1 == null) return null;
        if(fs2 == null) return fs1;
        String[] tmp = new String[fs1.length];
        int last = 0;
        for(String s : fs1) {
            if(!contains(fs2, s)) continue;
            tmp[last++] = s;
        }
        String[] result = new String[last];
        System.arraycopy(tmp, 0, result, 0, last);
        return result;
    }

    void printVars(VarMap vars, boolean varsonly)
    {
        Object val = null;
        // Always print testpattern first (it exists)
        if(!varsonly) {
            val = vars.get(testpattern);
            if(val != null) stdout.println(printVar(testpattern, val));
        }
        String[] keys = vars.sortKeys();
        for(String key : keys) {
            if(testpattern.equals(key)) continue;
            val = vars.get(key);
            if(val instanceof SystemObject) continue;
            if(varsonly && (val instanceof Pattern)) continue;
            if(!varsonly && !(val instanceof Pattern)) continue;
	    String s = printVar(key,val);
            stdout.println(s);
            stdout.flush();
        }
    }

    String printVar(String key, Object val)
    {
        String s = "";
        s += (key + "=");
        if(val instanceof Pattern) {
            s += ((Pattern) val).graphToString();
        } else if(val instanceof String) {
            s += (DQUOTE + QuotedString.addEscapes((String) val) + DQUOTE);
        } else if(val instanceof ExternalVariable) {
            s += val.toString();
        } else if(val instanceof Collection) {
	    Collection col = (Collection) val;
	    if(col.size() == 0)
		new Exception().printStackTrace();
            s += "[";
            boolean one = true;
            for(Object x : col) {
                s += (one ? "" : " ");
                s += ("|" + x.toString() + "|");
                one = false;
            }
            s += "]";
        } else {
            s += ("|" + val + "|");
        }
        return s;
    }
}
