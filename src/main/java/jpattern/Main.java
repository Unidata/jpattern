package jpattern.compiler;

import jpattern.compiler.Compiler;
import jpattern.Matcher;
import jpattern.MatchResult;
import jpattern.Pattern;
import jpattern.VarMap;
import jpattern.util.Debug;
import jpattern.util.Factory;
import jpattern.util.Parameters;
import jpattern.util.ParseArgs;
import jpattern.util.QuotedString;
import jpattern.Variable;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;	
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.FileReader;
import java.io.FileWriter;

/*
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
*/

public class Main
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

   // Misc constants
    static char DQUOTE = QuotedString.DQUOTE;

    // Define defalt cmdline arguments
    static String[] defaultFormals = new String[] {
	"envfile=",
	"env*",
	"v?",
	"f=",
	"o=",
	"err=",
	"debug?",
	"debugn#",
	"pat=",
	"subject=",
	"var*",
	"tag=",
	"xmltag=",
	"squote?",
	"dquote?",
	"conflict?"
    };

    //////////////////////////////////////////////////

    static public void main(String[] argv) throws Exception
    {
	Main main = new Main();
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
	ParseArgs.parse(argv,(HashMap<String,Object>)cmdparms,getFormals());// case 2
	// Use cmdparms to find any env or envfile parameters

	// load any  env args first
	ArrayList envargs = (ArrayList)cmdparms.get("env");
	if(envargs != null) {
	    for(int i=0;i<envargs.size();i++)
		envparms.load1((String)envargs.get(i),false);
	}
	// load the env file, if any
	String envf = cmdparms.getString("envfile");
	if(envf != null) {envparms.load(envf,false);}

	// Now merge all the parameter sets
	parms = new Parameters();
	parms.putAll(mainparms);
	parms.putAll(envparms);
	parms.putAll(cmdparms);

	// Dump the arguments if -v
	if(parms.getBoolean("v")) {
	    System.err.println("parms="+parms.toString());
	}

	// Initialize any static class values for this and other classes
	// (must be a better way to do this)
	int lev = parms.getInt("debugn",0);
	Debug.setDebugn(lev);
	if(parms.getBoolean("debug")) Debug.setDebug(true);

	Main.reader = new InputStreamReader(System.in);
	Main.writer = new OutputStreamWriter(System.out);
	Main.errwriter = new OutputStreamWriter(System.err);

	String infile = parms.getEnv("stdin");
	if(infile == null) infile = parms.getEnv("f");
	if(infile != null) {
	    Main.reader=new FileReader(infile);
	}

	String outfile = parms.getEnv("stdout");
	if(outfile == null) outfile = parms.getEnv("o");
	if(outfile != null) {
	    Main.writer=new FileWriter(outfile);
	}

	String errfile = parms.getEnv("stderr");
	if(errfile == null) errfile = parms.getEnv("err");
	if(errfile != null) {
	    Main.errwriter=new FileWriter(errfile);
	}

	Main.stdin = new BufferedReader(Main.reader);
	Main.stdout = new PrintWriter(Main.writer,true);
	Main.stderr = new PrintWriter(Main.errwriter,true);

    }

    //////////////////////////////////////////////////
    // Subclass overrides

    public void start()
    {
	try {
	// Instantiate the compiler
	Compiler compiler = new Compiler();
	if(parms.getBoolean("squote")) compiler.setQuote(QuotedString.SQUOTE);
	if(parms.getBoolean("dquote")) compiler.setQuote(QuotedString.DQUOTE);
	// decide what to do
	String jtag = parms.getString("tag");
	String jxml = parms.getString("xmltag");
        String spat = parms.getString("pat");
	String subject = parms.getString("subject");
	String jopen = null;
	String jclose = null;
	if(jtag != null)  {
	    jopen = jtag;
	    jclose = jtag;
	} else if(jxml != null) {
	    if(!jxml.matches("[<]\\w+[>]"))
		throw new Exception("malformed -xmltag: "+jxml);
	    jopen = jxml;
	    jclose = "</"+jxml.substring(1);
	}
	if(spat != null)
	    processString(spat,subject,compiler);
	else if(jopen != null)
	    processTemplate(jopen,jclose,compiler);
	else
	    throw new Exception("must specify: -tag | -xmltag | -pat");

	} catch (Exception e) {
	    System.err.println("Compile failed: "+ e);
	    if(Debug.islevel(1)) e.printStackTrace(System.err);
	    System.err.flush();
	    System.exit(1);
	}
    }

    void processJava(String spat, Compiler compiler) throws Exception
    {    
	String javacode = compiler.compileJ(spat);
	// dump to stdout
	System.out.print(javacode);
    }

    void processString(String spat,String subject,Compiler compiler)
	throws Exception
    {    
	Pattern p = compiler.compile(spat);
	if(subject != null) {
	    Matcher matcher = p.matcher(subject);
	    matcher.match();
	}
    }

    void match(String subject, Pattern p) throws Exception
    {
	Debug.level(1).println("pattern="+p.graphToString());
	// load up any variables
	VarMap vars = loadVars();
	stdout.println("vars before: "+vars);
	Matcher matcher = p.matcher(subject);
	matcher.setVarMap(vars);
	boolean ok = matcher.match();
	MatchResult result = (MatchResult)matcher;
	String s = result.getSubject();
	if(ok) {
	    stdout.print("succeed: ");
	    String prefix = s.substring(0,result.getStart());
	    String middle = s.substring(result.getStart(),result.getStop());
	    String suffix = s.substring(result.getStop(),s.length());
	    stdout.println(prefix+"^"+middle+"^"+suffix);
	} else {
	    stdout.println("fail.");
	}
	stdout.println("vars after: "+vars.prettyPrint());
    }

    // process stdin as a template
    void processTemplate(String opentag, String closetag, Compiler compiler)
	throws Exception
    {
	// validate tag
        if(opentag.length() == 0 || opentag.indexOf("\n") >= 0)
	    throw new Exception("invalid open tag string: "+opentag);
        if(closetag.length() == 0 || closetag.indexOf("\n") >= 0)
	    throw new Exception("invalid close tag string: "+closetag);
	for(;;) {
	    String line = getLine();
	    if(line == null) break;
	    int len = line.length();
	    if(line.startsWith("//")
		|| line.startsWith("/*")) {
		stdout.print(line+"\n");
		continue;
	    }
	    int index = line.indexOf(opentag);
	    if(index < 0) { // just pass thru
		stdout.print(line+"\n");
	    } else {
		int predex = index;
		int start = index+opentag.length();
		int stop = line.indexOf(closetag,start);
		if(stop == -1) stop = len;
		int sufdex = Math.min(len,stop+closetag.length());
		if(line.indexOf(opentag,sufdex) >= 0)
		// complain if multiple patterns per line.
		    throw new Exception("Multiple tags per line: |"+line+"|");
		String spat = line.substring(start,stop);
Debug.level(1).println("pattern="+spat);
	        String javacode = compiler.compileJ(spat,index);
		if(predex > 0) stdout.print(line.substring(0,predex));
	        stdout.print(javacode);
		if(sufdex < len)
		    stdout.print(line.substring(sufdex,len)+"\n");
	    }
	}
	stdout.flush();
    }

    // Handle escaped eols and comments (but rather badly)

    String getLine() throws Exception
    {
	String line0 = getLine1(); // single line with no back slashes
	if(line0 == null || line0.length() == 0) return line0;
	// Check for possible comment
	if(!line0.startsWith("/*")) return line0;
	String line = "";
	int commentdepth = 0;
	// accumulate the /*...*/ comment (with possible nesting)
	commentdepth++;
	for(;;) {
	    line += ("\n"+line0);
	    line0 = getLine1();
	    if(line0 == null) {
		// open ended comment, but oh well
	        stderr.println("/*...*/ comment not closed");
		break;
	    } else if(line0.equals("*/")) {
		commentdepth--;
		if(commentdepth == 0) {
		    line += ("\n"+line0);
		    break;
		}
	    } else if(line0.equals("/*")) {
		commentdepth++;
	    }
	}
	// strip leading eol
	return line.substring(1,line.length());
    }


    String getLine1() throws Exception
    {
	String line0 = stdin.readLine();
	if(line0 == null || line0.length() == 0) return line0;
	// accumulate lines if they end in backslash;
	String line = null;

if(false) {
	// Note: kill everything after the backslash
	StringBuilder buf = new StringBuilder();
	while(line0 != null) {
	    // do shortcut check for DQUOTE before back slash
	    if(line0.indexOf('"') >= line0.indexOf('\\')) {
	        // no strings to get in the way; look for a back slash
	        int i = line0.indexOf('\\');
	        if(i >=  0) { // line has backslash

		    line0 = line0.substring(0,i);
		} else {// line has no back slash
		    break;		    		    
		}
	    } else {
		// ok, line has string constants; we have to do detailed parse
		// locate the trailing backslash (if any)
	        // taking quoted strings into account.
	        buf.setLength(0);
	        if(!locateBslash(buf,line0)) break;  // upto '\\' or end
		line0 = buf.toString();
	    }
	    // accumulate lines into one big line
	    line = (line==null?line0:(line+"\n"+line0));
	    line0 = stdin.readLine();
	}
} else {
	while(line0 != null) {
	    // check for trailing back slash
	    if(!line0.matches("^.*\\\\s*$")) break;
	    // right trim and remove trailing back slash
	    line0 = line0.substring(0,line0.lastIndexOf('\\'));
	    // accumulate lines into one big line
	    line = (line==null?line0:(line+"\n"+line0));
	    line0 = stdin.readLine();
	}
}
	if(line0 != null) {
	    // append the last line
	    line = (line==null?line0:(line+"\n"+line0));
	}
	return line;
    }

    private boolean locateBslash(StringBuilder buf, String line)
    {
	StringTokenizer lex = new StringTokenizer(line,"\"\\",true);
	boolean hasbslash = false;
	while(lex.hasMoreTokens()) {
	    String tok = getToken(lex);
	    if(tok.equals("\"")) {
		buf.append(tok);
		for(;;) {
		    buf.append(tok = getToken(lex));
		    if(tok.equals("\"")) break;
		    if(tok.equals("\\")) buf.append(tok = getToken(lex));
		}
	    } else if(tok.equals("\\")) {hasbslash = true; break;}
	      else {buf.append(tok);}
	}
	return hasbslash;
    }

    private String getToken(StringTokenizer lex)
    {
	String tok=lex.nextToken("\"\\");
//	System.err.println("token="+tok);
	return tok;
    }

    VarMap loadVars() throws Exception
    {
	VarMap vars = new VarMap();
	ArrayList varargs = (ArrayList)parms.get("var");
	if(varargs != null) {
	    for(int i=0;i<varargs.size();i++) {
		// parse each assignment
		String pair = (String)varargs.get(i);
		if(pair.charAt(pair.length()-1) == '*') {
		    // make the var be a multiple assignment
		    String nm = pair.substring(0,pair.length()-1);
		    ArrayList val = new ArrayList();
		    vars.put(new Variable(nm),val);
		} else {
		    int eqdex = pair.indexOf("=");
		    if(eqdex < 0) {
		        stderr.println("bad -var: "+pair);
		        continue;
		    }
		    String nm = pair.substring(0,eqdex);
		    String val = pair.substring(eqdex+1,pair.length());
		    vars.put(new Variable(nm),val);
		}
	    }
	}
	return vars;
    }

    public void cleanup() throws Exception {};

    public String[] getFormals()
    {
	return Main.defaultFormals;
    }

    public void setDefaults(Parameters parms)
    {
	ArrayList<String> list = new ArrayList<String>();
	list.add("");
	list.add("snobol");
	list.add("jbol.util");
	list.add("jbol.compiler");
	parms.put("packageprefixes",list);
    }

    //////////////////////////////////////////////////

    static void usage(String msg) {usage(msg,null);}
    static void usage(Exception e) {usage(e.toString(),e);}

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
	String[] tmp = new String[fs1.length+fs2.length];
	int last = 0;
	if(fs1 != null) {
	    System.arraycopy(fs1,0,tmp,0,fs1.length);
	    last = fs1.length;
	}
	if(fs2 != null) {
	    for(String s: fs2) {
	        if(contains(fs1,s)) continue;
	        tmp[last++] = s;
	    }
	}
	String[] result = new String[last];
	System.arraycopy(tmp,0,result,0,last);
	return result;
    }

    static public String[] minus(String[] fs1, String[] fs2)
    {
	if(fs1 == null) return null;
	if(fs2 == null) return fs1;
	String[] tmp = new String[fs1.length];
	int last = 0;
	for(String s: fs1) {
	        if(!contains(fs2,s)) continue;
	        tmp[last++] = s;
	}
	String[] result = new String[last];
	System.arraycopy(tmp,0,result,0,last);
	return result;
    }

}