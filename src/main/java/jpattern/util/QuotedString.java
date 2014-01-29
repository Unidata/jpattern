package jpattern.util;

public class QuotedString
{
    static public final int FAIL = -2;
    static public final int EOF = -1;
    static public final char EOL = '\n';

    static public final char DQUOTE = '"';
    static public final char SQUOTE = '\'';
    static public final char BQUOTE = '`';
    static public final char ESCAPE = '\\';

    static public final char LPAREN = '(';
    static public final char RPAREN = ')';
    static public final char LBRACK = '[';
    static public final char RBRACK = ']';
    static public final char LBRACE = '{';
    static public final char RBRACE = '}';

    static public char hextable[] = new char[]{
	'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'
	};
    static public char octtable[] = new char[]{
	'0','1','2','3','4','5','6','7'
	};

    static public String addEscapes(String s)
	{return addEscapes(s,new StringBuilder()).toString();}

    static public String addEscapes(String s, char quot)
	{return addEscapes(s,new StringBuilder(),quot).toString();}

    static public StringBuilder addEscapes(String s, StringBuilder buf)
	{return addEscapes(s,buf,(char)DQUOTE);}

    static public StringBuilder addEscapes(String s,
					   StringBuilder buf,
					   char quot)
	{return addEscapes(s,buf,quot,null);}

    // Convert a string to one with included escapes.
    // Do not add enclosing quote characters.
    static public StringBuilder addEscapes(String s,
					     StringBuilder es,
					     char quote,
					     String special)
    {
	if(s == null) return es;
	for(int i=0;i<s.length();i++) {
	    int ch = s.charAt(i);
	    boolean isSpecial=(special!=null && special.indexOf((char)ch)>=0);
	    addEscapedChar(ch,es,quote,isSpecial);
	}
	return es;
    }

    static public String addEscapedChar(int ch)
	{return addEscapedChar(ch,new StringBuilder(),SQUOTE).toString();}

    static public StringBuilder addEscapedChar(int ch,StringBuilder es)
	{return addEscapedChar(ch,es,SQUOTE);}

    static public StringBuilder addEscapedChar(int ch, StringBuilder es,
					       char quote)
	{return addEscapedChar(ch,es,quote,false);}

    static public StringBuilder addEscapedChar(int ch,
				   StringBuilder es,
				   char quote,
				   boolean isSpecial)
    {
	if(ch < 0x20 || ch >= 0x80
	   || ch == quote || ch == ESCAPE || isSpecial) {
	    es.append((char)ESCAPE);
	    // these can get by with simple escape format
	    if(ch == quote || ch == ESCAPE || isSpecial) {
		es.append((char)ch);
	    } else {
		switch (ch) {
		case '\n': es.append('n'); break;
		case '\r': es.append('r'); break;
		case '\t': es.append('t'); break;
		default: // use u format
		    es.append('u');
		    for(int j=3;j>=0;j--) {es.append(hexToChar(ch >>> (4*j)));}
		}
	    }
	} else
	    es.append((char)ch);
	return es;
    }

    static public StringBuilder removeEscapes(CharStream gc)
    { return removeEscapes(gc,new StringBuilder()); }

    static public StringBuilder removeEscapes(CharStream gc, StringBuilder buf)
    { return removeEscapes(gc,buf,(char)DQUOTE); }

    // Convert a string with included escapes to a normal string.
    // Assume initial quote HAS NOT been read;
    // returned string does not include enclosing quotes
    static public StringBuilder removeEscapes(CharStream gc,
					 StringBuilder buf,
					 char quote)
    {
	int ch = gc.getch();
	if(ch != quote) gc.pushback(ch);
	for(;;) {
	    ch = gc.getch();
	    if(ch == EOF || ch == EOL) return null;
	    if(ch == quote) break;
	    if(ch == ESCAPE) ch = getEscapedChar(gc);
	    buf.append((char)ch);
	}
	return buf;	
    }

    static public int getEscapedChar(CharStream gc)
    {
	int newch = 0;
	int ch = gc.getch(); // char after back slash
	switch (ch) {
	    case 'n': newch = '\n'; break;
	    case 'r': newch = '\r'; break;
	    case 't': newch = '\t'; break;
	    case 'u':
		for(int i=0;i<4;i++) {
		    ch = gc.getch();
		    if(!isHexDigit(ch)) return FAIL;
		    newch = (newch<<4) | charToHex(ch);
		}
		break;
	    default:
		if(isOctalDigit(ch)) {
		    newch = (newch<<3)|(charToOctal(ch));
		    for(int i=0;i<2;i++) {
			ch = gc.getch();
		        if(!isOctalDigit(ch)) return FAIL;
		        newch = (newch<<3)|(charToOctal(ch));
		    }
		} else newch = ch; // just pass it
		break;
	}
	return newch;
    }
    
    static public boolean isOctalDigit(int ch)
    {
	return ((ch >= '0' && ch <= '7'));
    }

    static public boolean isHexDigit(int ch)
    {
	return (
	  (ch >= '0' && ch <= '9')
       || (ch >= 'A' && ch <= 'F')
       || (ch >= 'a' && ch <= 'f')
	);
    }

    static public int charToHex(int ch)
    {
        if(ch >= '0' && ch <= '9')
	    return (ch - '0');
        else if(ch >= 'A' && ch <= 'F')
	    return (ch - 'A') + 0xa;
        else if(ch >= 'a' && ch <= 'f')
	    return (ch - 'a')+0xa;
	else return 0;
    }

    static public int charToOctal(int ch)
    {
	return (ch - '0');
    }

    static public char octalToChar(int oc)
    {
	return (char)octtable[oc & 0x7];
    }

    static public char hexToChar(int hx)
    {
	return (char)hextable[hx & 0xf];
    }

}
