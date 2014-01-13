import jpattern.*;
import jpattern.compiler.*;
import jpattern.util.*;
import jpattern.*;
import java.io.*;
import java.util.*;

public class Template
{

static public void main(String[] argv) throws Exception
{
    String regex = "[0-9]*";
    String patstring = "RE(\"" + regex + "\")";
    String subject = "1234567";

    ExternalMap externs = new ExternalMap();
    externs.add(new REPattern());

    VarMap vars = new VarMap();

jpattern.util.Debug.setDebugn(2);

    // Instantiate the compiler and compile the desired pattern.
    jpattern.compiler.Compiler compiler = new jpattern.compiler.Compiler();
    Pattern p =
//             Pattern.External("RE",regex);
               compiler.compile(patstring);
    Matcher m = p.matcher(subject, vars, externs);

    // Now perform the match and report the result
    boolean ok = m.match();
    System.out.println(ok?"succeed: "+((MatchResult)m).getSubject():"fail.");
}
}
