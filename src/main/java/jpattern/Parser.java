package jpattern.compiler;

import jpattern.Variable;
import jpattern.util.CharStream;
import jpattern.util.QuotedString;
import jpattern.util.Debug;
import static jpattern.util.JpatternConstants.*;

import java.util.HashMap;
import java.util.ArrayList;

class Parser
{
    char lexquote = DQUOTE;
    int ttype;
    String token;
    CharStream cs;   
    Lexer lex;

    public void setQuote(char q) throws Error
	{lexquote=q; if(lex!=null) lex.setQuote(q);}

    static boolean isAltFollow(int ttype)
	{return (ttype==Token.BAR)
		||(ttype==Token.EOF)
		||(ttype==Token.RPAREN)
	        ;
	}

    public Parsetree parse(String expr)
	throws Error
    {
        cs = new CharStream(expr);
	lex = new Lexer(cs);
	lex.setQuote(lexquote);
	// prime pump
	advance();
	Parsetree node = null;
	try {
	    node = parsePat();
	} catch (Exception e) {
	    node = null;
	    int cursor = cs.getCursor();
	    String s = (String)cs.getSequence();
	    String prefix = s.substring(0,cursor);
	    String suffix = s.substring(cursor,s.length());
Debug.level(2).println(e.toString()+"; "+prefix+"|"+suffix);
if(Debug.islevel(2)) e.printStackTrace();
	    throw new Error(prefix+"|"+suffix,e);
	}
	// Check to see that whole expr was consumed
	int cursor = cs.getCursor();
	if(cursor < expr.length()) {
	    throw new Error("Compiler.parse: expr incompletely consumed: |"
			     + expr.substring(cursor,expr.length())+"|");
	}	
	return node;
    }

    protected Parsetree parsePat() throws Error
    {
Debug.level(3).println("Pat");
	Parsetree n = parseAlt();
Debug.level(3).println("Pat: "+n);
	return n;
    }

    protected Parsetree parseAlt() throws Error
    {
Debug.level(3).println("Alt");
	Parsetree n = parseCat();
	if(n == null || ttype != Token.BAR) {
Debug.level(3).println("Alt0: "+n);
	    return n;
	}
Debug.level(3).println("Alt.l: "+n);
	Parsetree onode = new Parsetree(Keyword.ALT);
        onode.left = n;
	advance(); // skip BAR
	n = parseAlt();
	if(n == null)
	    throw new Error("Expected ALT: saw: "+token);
        onode.right = n;
Debug.level(3).println("Alt.f: "+onode);
	return onode;
    }

    protected Parsetree parseCat() throws Error
    {
Debug.level(3).println("Cat");
	Parsetree n = parseReplace();
	if(n == null) {
Debug.level(3).println("Cat0: "+n);
	    return n;
	}
Debug.level(3).println("Cat.l: "+n);
	// Check for explicit concatenation
	if(isAltFollow(ttype)) {// check for alt following
	    // looks like an alt follows, so quit
Debug.level(3).println("Cat1: "+n);
	    return n;
	} // else should be implicit concat or possibly EOF or RPAREN
	if(ttype == Token.AND) advance(); // consume the concat symbol
	Parsetree nr = parseCat();
	if(nr != null) {
	    Parsetree onode = new Parsetree(Keyword.CAT);
	    onode.left = n;
	    onode.right = nr;
	    n = onode;
	}
Debug.level(3).println("Cat.f: "+n);
	return n;
    }

    protected Parsetree parseReplace() throws Error
    {
Debug.level(3).println("Replace");
	Parsetree n = parseAssign();
	if(n == null) {
Debug.level(3).println("Replace: "+n);
	    return n;
	}
Debug.level(3).println("Replace.l: "+n);
	for(;;) {
	    Parsetree onode;
	    if(ttype == Token.EQUAL) {
	        onode = new Parsetree(Keyword.REPLACE);
	    } else
	        break;
	    onode.left = n;
	    advance(); // skip EQUAL
	    if(ttype == Token.PLUS) advance(); // skip +
	    if(ttype != Token.ID)
	        throw new Error("Expected VAR: saw: "+token);
	    onode.right = new Parsetree(Keyword._VAR,new Variable(token));
	    advance(); // skip ID
	    n = onode;
	}
Debug.level(3).println("Replace.f: "+n);
	return n;
    }

    protected Parsetree parseAssign() throws Error
    {
Debug.level(3).println("Assign");
	Parsetree n = parseElement();
	if(n == null) {
Debug.level(3).println("Assign: "+n);
	    return n;
        }
Debug.level(3).println("Assign.l: "+n);
	for(;;) {
	    Parsetree onode;
	    if(ttype == Token.DOT || ttype == Token.STARSTAR) {
		onode = new Parsetree(Keyword.ASSIGN);
	    } else if(ttype == Token.DOLLAR || ttype == Token.STAR) {
	        onode = new Parsetree(Keyword.IASSIGN);
	    } else
	        break;
	    onode.left = n;
	    advance(); // skip STAR or STARSTAR
	    if(ttype == Token.PLUS) advance(); // optional
	    if(ttype != Token.ID)
	        throw new Error("Expected VAR: saw: "+token);
	    onode.right = new Parsetree(Keyword._VAR,new Variable(token));
	    advance(); // skip ID
	    n = onode;
	}
Debug.level(3).println("Assign.f: "+n);
	return n;
    }

    protected Parsetree parseElement() throws Error
    {
Debug.level(3).println("Element");
	Parsetree node = null;
Debug.level(3).println("Element.T: ttype="+Token.tokenName(ttype));
	switch (ttype) {

	case Token.BAR:
	case Token.AND:
	    break; // let next level up handle this
	    
	case Token.LPAREN:
Debug.level(3).println("LPAREN.1: ttype="+Token.tokenName(ttype));
	    advance();
Debug.level(3).println("LPAREN.2: ttype="+Token.tokenName(ttype));
	    node = parsePat();
Debug.level(3).println("LPAREN.3: ttype="+Token.tokenName(ttype)+" node="+node);
	    if(ttype != Token.RPAREN)
		throw new Error("Expected RPAREN: saw: "+token);
	    advance();
	    break;	    

	case Token.PLUS:
	    node = new Parsetree(Keyword.DEFER);
	    advance();
	    if(ttype != Token.ID)
		throw new Error("Expected ID: saw: "+token);
	    node.left = parseVarg();
	    break;

	case Token.ID: 
	    node = parseOp(); 
	    break;

	case Token.EQUAL:
if(false) { // Handled above specially
	    node = new Parsetree(Keyword.REPLACE);
	    advance();
	    if(ttype != Token.ID)
		throw new Error("Expected ID: saw: "+token);
	    node.left = new Parsetree(Keyword._VAR,new Variable(token));
	    advance();
}
	    break;

	case Token.DOLLAR:
	case Token.STAR:
	    // Handled specially at beginning of loop
if(false) {
	    node = new Parsetree(Keyword.IASSIGN);
	    advance();
	    if(ttype != Token.ID)
		throw new Error("Expected ID: saw: "+token);
	    node.left = new Parsetree(Keyword._VAR,new Variable(token));
	    advance();
}
	    break;

	case Token.DOT:
	case Token.STARSTAR:
	    // Handled at beginning of loop specially
if(false) {
	    node = new Parsetree(Keyword.ASSIGN);
	    advance();
	    if(ttype != Token.ID)
		throw new Error("Expected ID: saw: "+token);
	    node.left = new Parsetree(Keyword._VAR,new Variable(token));
	    advance();
}
	    break;

	case Token.INT:
	    node = new Parsetree(Keyword._INT,token);
	    advance();
	    break;

	case Token.STRING:
	    node = new Parsetree(Keyword._STRING,token);
	    advance();
	    break;

	case Token.JAVASTRING:
	    // wrap in a java keyword when used as a pattern element
	    node = new Parsetree(Keyword.JAVA,token);
	    advance();
	    break;

	default:
	    break;
        }; // switch

Debug.level(3).println("Element.f: "+node);
        return node;
    }

    protected Parsetree parseSVarg() throws Error
    {
	Parsetree n = null;
	if((n = parseSarg()) != null) return n;
	if((n = parseVarg()) != null) return n;
	return null;
    }

    protected Parsetree parseNVarg() throws Error
    {
	Parsetree n = null;
	if((n = parseNarg()) != null) return n;
	if((n = parseVarg()) != null) return n;
	return null;
    }

    // Expect a String or a JavaString
    protected Parsetree parseSarg() throws Error
    {
	Parsetree n = null;
	if(ttype == Token.STRING) {
	    n = new Parsetree(Keyword._STRING,token);
	    advance();
	} else if(ttype == Token.JAVASTRING) {
	    n = new Parsetree(Keyword._JAVASTRING,token);
	    advance();
	} else return null;
	return n;
    }

    // Expect a number or a JavaString
    protected Parsetree parseNarg() throws Error
    {
	Parsetree n = null;
	if(ttype == Token.INT) {
	    n = new Parsetree(Keyword._INT,Integer.parseInt(token));
	    advance();
	} else if(ttype == Token.JAVASTRING) {
	    n = new Parsetree(Keyword._JAVASTRING,token);
	    advance();
	} else return null;
	return n;
    }

    protected Parsetree parseVarg() throws Error
    {
	if(ttype == Token.PLUS) { // this is optional but if there => Var
	    advance();
	    if(ttype != Token.ID)
		throw new Error("Expected ID: saw: "+token);
	} else if(ttype != Token.ID) return null;
	String name = token; // save
	advance0();
	// check for possible external function
	if(ttype == Token.LPAREN) {return parseExtern(name);}
	Parsetree node = new Parsetree(Keyword._VAR,new Variable(name));
	if(ttype == Token.WHITESPACE) advance();
	return node;
    }

    protected Parsetree parseExtern(String name) throws Error
    {
	// assume that token is at the LPAREN
	// Accumulate the arguments, arg 0 becomes the value
	// remaining args go into node.argv
	Parsetree node = new Parsetree(Keyword._EXTERNAL,name);
	ArrayList<Parsetree> argv = new ArrayList<Parsetree>();
	lex.pushBack(); // so paramStart will work
	paramStart();
	while(ttype != Token.RPAREN) {
	    switch(ttype) {
		case Token.INT:
		    argv.add(new Parsetree(Keyword._INT,token));
		    break;
		case Token.STRING:
		    argv.add(new Parsetree(Keyword._STRING,token));
		    break;
		case Token.JAVASTRING:
		    argv.add(new Parsetree(Keyword._JAVASTRING,token));
		    break;
		default:
		    throw new Error("External Pattern call: expected int,,string, or javacode: saw: "+token);
	    }
	    advance();
	}
	paramEnd();
	if(argv.size() > 0)
	    node.argv = (Parsetree[])argv.toArray(new Parsetree[argv.size()]);
	return node;
    }

    // Parse a simple pattern operator
    // or possible var or possible external pattern
    protected Parsetree parseOp() throws Error
    {
Debug.level(3).println("Op");
        Keyword kw = Keyword.testKeyword(token);
	Parsetree node = null;
	if(kw == Keyword.UNKNOWN) {
	    // save the name
	    String name = token;
	    // see if this is a possible external pattern function
	    advance0();
	    if(ttype == Token.LPAREN) {return parseExtern(name);}
	    // Treat simple ID (not a keyword)
	    // as if it had an implicit "+" in front of it
	    node = new Parsetree(Keyword.DEFER);
	    node.left = new Parsetree(Keyword._VAR,new Variable(name));
	    if(ttype == Token.WHITESPACE) advance();
	    return node;
	}

	// Special case, use advance0 to not skip white space
	// => expected lparen will be immediately after the
	// keyword

	node = new Parsetree(kw);

	switch (kw) {

	case ANY:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case ARB:
	    advance();
	    break;

	case ARBNO:
	    advance0();
	    if(ttype != Token.LPAREN)
	        throw new Error("Expected LPAREN; found:"+token);
	    // leave left paren as curren token
	    node.left = parseElement();
	    break;

	case BAL: // two cases here BAL vs BAL(String|Var)
	    advance0();
	    if(ttype == Token.LPAREN) { // BAL(String|Var) case
		advance();
		node.left = parseSVarg();
		if(node.left == null)	    		    	    
		    throw new Error("Expected parameter; found:"+token);
		paramEnd();
	    } else { // BAL case
		if(ttype == Token.WHITESPACE) advance();
	    }
	    break;

	case BREAK:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
	        throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case BREAKX:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case CANCEL:
	    advance(); 
	    break;

	case DEFER: // unary +
	    paramStart();
	    node.left = parseVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case FAIL:
	    advance(); 
	    break;

	case FENCE: // two cases here FENCE vs FENCE(P)
	    advance0();
	    if(ttype == Token.LPAREN) { // FENCE(P) case
		// leave left paren as curren token
		node.left = parseElement();
	    } else { // FENCE case
		if(ttype == Token.WHITESPACE) advance();
	    }
	    break;		

	case LEN:
	    paramStart();
	    node.left = parseNVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case NOTANY:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case NSPAN:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case POS:
	    paramStart();
	    node.left = parseNVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case REM:
	case REST:
	    advance();
	    break;

	case RPOS:
	    paramStart();
	    node.left = parseNVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case RTAB:
	    paramStart();
	    node.left = parseNVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case SETCUR:
	    paramStart();
	    node.left = parseVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case SPAN:
	    paramStart();
	    node.left = parseSVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case SUCCEED:
	    advance();
	    break;

	case TAB:
	    paramStart();
	    node.left = parseNVarg();
	    if(node.left == null)	    		    	    
		throw new Error("Expected parameter; found:"+token);
	    paramEnd();
	    break;

	case ALT:
	case ASSIGN:
	case IASSIGN:
	case CAT:
	default: throw new Error("Unexpected pattern: "+lex.token());
	    
	}
Debug.level(3).println("Op.f: "+node.toString());
	return node;
    }

    protected void paramStart() throws Error
    {
	advance0(); 
	if(ttype != Token.LPAREN)
	    throw new Error("Expected LPAREN; found:"+token);
	advance();
    }

    protected void paramEnd() throws Error
    {
	if(ttype != Token.RPAREN)
	    throw new Error("Expected RPAREN; found:"+token);
	advance();
    }

    protected void advance() throws Error
    {
	// skip ws and comments
	do {advance1();} while(ttype == Token.COMMENT);
    }

    protected void advance1() throws Error
    {
	// skip ws but not comments
	do {advance0();} while(ttype == Token.WHITESPACE);
    }

    protected void advance0() throws Error
    {
	// skip nothing
	ttype = lex.nextToken();
	token = lex.token();
    }

}

//////////////////////////////////////////////////
// Auxilliary Classes

//////////////////////////////////////////////////
// Because it includes all characters, we can't
// implement Token as an enum (can we?) so fall back to constants
abstract class Token
{
    // Single char tokens
    static final int EOL = ('\n');
    static final int BAR = ('|');
    static final int PLUS = ('+');
    static final int AND = ('&');
    static final int STAR = ('*');
    static final int MINUS = ('-');
    static final int SLASH = ('/');
    static final int EQUAL = ('=');
    static final int DOLLAR = ('$');
    static final int DOT = ('.');
    static final int LPAREN = QuotedString.LPAREN;
    static final int RPAREN = QuotedString.RPAREN;
    static final int BACKSLASH = QuotedString.ESCAPE;

    static final int NOTOKEN = (0);

    // Non single char tokens
    static final int EOF = (-1);
    static final int WHITESPACE = (-2);
    static final int COMMENT = (-3);
    static final int ID = (-4);
    static final int INT = (-5);
    static final int STRING = (-6);
    static final int JAVASTRING = (-7);
    static final int STARSTAR = (-8);

    static public String tokenName(int ttype)
    {
	String name = null;
	if(ttype <= 0) {
	    switch (ttype) {
		case NOTOKEN: name = "NOTOKEN"; break;
		case EOF: name = "EOF"; break;
		case WHITESPACE: name = "WHITESPACE"; break;
		case COMMENT: name = "COMMENT"; break;
		case ID: name = "ID"; break;
		case INT: name = "INT"; break;
		case STRING: name = "STRING"; break;
		case JAVASTRING: name = "JAVASTRING"; break;
		case STARSTAR: name = "STARSTAR"; break;
		default: name = "unknown"+LPAREN+ttype+RPAREN; break;
	    }
	} else if(ttype == EOL) {
	    name = "\\n";
	} else {
	    name = "'" + ((char)ttype) + "'";
	}
	return name;
    }
}

class Lexer
{
    static final char DQUOTE = QuotedString.DQUOTE;
    static final char BQUOTE = QuotedString.BQUOTE;

    char quoteChar = DQUOTE;
    StringBuilder token;
    boolean pushedback;
    int ttype;
    CharStream in;

    public Lexer(CharStream cs)
    {
	this.in = cs;
	this.token = new StringBuilder();
	this.ttype = Token.NOTOKEN;
	pushedback=false;
    }

    public String token() {return token.toString();}
    public int tokenType() {return ttype;}
    public void pushBack() {pushedback = true;}
    public void setQuote(char q) throws Error
    {
	if(q != QuotedString.SQUOTE && q != DQUOTE)
	    throw new Error("Compiler.setQuote: illegal argument ("+q+")");
	quoteChar=q;
    }

    public int nextToken() throws Error
    {
	int ttype = nextToken0();
	Debug.level(2).print("nextToken: "+Token.tokenName(ttype));
	if(ttype < 0 && ttype != Token.EOF) Debug.level(2).print(" token=|"+token+"|");
	Debug.level(2).println();
	return ttype;
    }

    protected int nextToken0() throws Error
    {
	if(pushedback) {
	    pushedback = false;
	    return ttype;
	}
	token.setLength(0);
	int ch = in.getch();
	// All whitespace is accumulated into a singl token
	if(isWhiteSpace(ch)) {
	    do {
	        token.append((char)ch); ch = in.getch();
	    } while(isWhiteSpace(ch));
	    in.pushback(ch);
	    return (ttype=Token.WHITESPACE);
	}
	if(isID(ch)) {
	    do {
	        token.append((char)ch); ch = in.getch();
	    } while(isID2(ch));
	    in.pushback(ch);
	    return (ttype = Token.ID);
	}
	if(ch == Token.MINUS) {
	    token.append((char)ch);
	    ch = in.getch();
	if(!isDigit(ch)) {
	    in.pushback(ch);
	    return (ttype = Token.MINUS);
	}
	// negative number
	do {token.append((char)ch); ch = in.getch();} while(isDigit(ch));
	    in.pushback(ch);
	    return (ttype = Token.INT);
	}
	if(isDigit(ch)) {
	    do {token.append((char)ch); ch = in.getch();} while(isDigit(ch));
	    in.pushback(ch);
	    return (ttype = Token.INT);
	}
	if(ch == Token.STAR) {
	    // check to see if this is "**" vs "*"
	    token.append((char)ch);
	    ch = in.getch();
	    if(ch != Token.STAR) {
		in.pushback(ch);
		ttype = Token.STAR;
	    } else {
		token.append((char)ch);
	        ttype = Token.STARSTAR;
	    }
	    return ttype;
	} // star
	if(ch == quoteChar) {
	    // leave off quotes
	    in.pushback(ch);
	    if(QuotedString.removeEscapes(in,token,quoteChar) == null)
	        throw new Error("Unterminated String");
	    return (ttype = Token.STRING);
	} // quoteChar
	if(ch == BQUOTE) {
	    // leave off back quotes
	    in.pushback(ch);
	    if(QuotedString.removeEscapes(in,token,BQUOTE) == null)
	        throw new Error("Unterminated backquoted string");
	    return Token.JAVASTRING;
	} // quoteChar
	if(ch == Token.SLASH) { //possible comment
	    token.append((char)ch);
	    ch = in.getch();
	    if(ch != Token.SLASH && ch != Token.STAR) {
	        in.pushback(ch);
	        return (ttype = Token.SLASH);
	    }
	    // we have a comment of some kind
	    token.append((char)ch);
	    if(ch == Token.SLASH) {
	        // sweep to end of line (or eof)
	        ch = in.getch();
	        while(ch != Token.EOL && ch != Token.EOF) {
		    token.append((char)ch);
		    ch = in.getch();
		}
		if(ch == Token.EOL) token.append((char)ch);
	    } else { // /*...*/ comment
	        for(;;) {
		    ch = in.getch();
		    if(ch == Token.EOF)
		        throw new Error("Unterminated comment: "+token);
		    if(ch != Token.STAR) {token.append((char)ch); continue;}
		    // possible end of comment
		    token.append((char)ch); // pass the star
		    ch = in.getch();
		    if(ch == Token.SLASH) { // done
		        token.append((char)ch);
		        break;
		    }
		    // reread this next char
		    in.pushback(ch);
		}
	    }
	    return (ttype = Token.COMMENT);
	}
	if(ch == Token.BACKSLASH) {
	    ch = QuotedString.getEscapedChar(in);
	    // fall thru
	}
	if(ch == Token.EOF) {
	    token.append("EOF");
	    return (ttype = ch);
	}
	// default case: just pass as a delimiter or whitespace
	token.append((char)ch);
	return (ttype = ch);
    }

    static public boolean isID(int ch)
    {
	return (
	  (ch >= 'a' && ch <= 'z')
	   || (ch >= 'A' && ch <= 'Z')
	   || (ch >= '\u00A0' && ch <= '\u00FF')
	   || (ch == '_')
	);
    }

    // ID2 characters can appear after the initial id char
    static public boolean isID2(int ch)
	{return (isID(ch) || isDigit(ch));}

    static public boolean isDigit(int ch)
	{return ((ch >= '0' && ch <= '9'));}

    static public boolean isOctalDigit(int ch)
	{return ((ch >= '0' && ch <= '7'));}

    static public boolean isHexDigit(int ch)
    {
	return (
	  (ch >= '0' && ch <= '9')
	   || (ch >= 'A' && ch <= 'F')
	   || (ch >= 'a' && ch <= 'f')
	);
    }

    static public boolean isWhiteSpace(int ch)
	{return ((ch >= '\u0000' && ch <= '\u0020'));}

    static public int charToHex(int ch)
    {
	if(ch >= 'A' && ch <= 'Z') return ((ch - 'A')+10);
	if(ch >= 'a' && ch <= 'z') return ((ch - 'a')+10);
	// assume digit
	return (ch - '0');
    }

    static public int charToOctal(int ch)
	{return (ch - '0');}

}; // Lexer
