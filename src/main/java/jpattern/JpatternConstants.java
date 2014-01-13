package jpattern.util;

import jpattern.PE;
import static jpattern.util.QuotedString.*;
import static jpattern.PatternCode.*;

public class JpatternConstants
{
    //////////////////////////////////////////////////
    // Matcher Constants
    //////////////////////////////////////////////////
        
    // flag to separate out cursor = stack ptr
    public static final int PTRFLAG = 0x80000000;
    public static final int PTRMASK = (~PTRFLAG);

    public static final String DFALT_BAL_PARENS = "()";

   //---------------------
   // Global Constants
   //---------------------

   public static int DEFAULT_STACK_SIZE = 200;

    //  The following pattern elements are referenced only from the pattern
    //  history stack. In each case the processing for the pattern element
    //  results in pattern match abort, or futher failure, so there is no
    //  need for a successor and no need for a node number

    public static PE CP_Assign;
    public static PE CP_Replace;
    public static PE CP_Cancel;
    public static PE CP_Fence_Y;
    public static PE CP_R_Remove;
    public static PE CP_R_Restore;
    public static PE CP_R_NULL;

    //////////////////////////////////////////////////
    // Parser and Compiler Constants
    //////////////////////////////////////////////////
        
    public static final char DQUOTE = QuotedString.DQUOTE;
    public static final char SQUOTE = QuotedString.SQUOTE;
    public static final char BQUOTE = QuotedString.BQUOTE;
    public static final char LPAREN = QuotedString.LPAREN;
    public static final char RPAREN = QuotedString.RPAREN;

    public static final int DEFAULTINDENTINCR = 4;
    public static String DFALT_CONFLICT_PREFIX = "jpattern.";
    public static String PATTERN_PREFIX = "Pattern.";

    //////////////////////////////////////////////////
    // Pattern.java constants
    //	This is the end of pattern element, and is thus the representation of
    //	a null pattern. It has a zero index element since it is never placed
    //	inside a pattern. Furthermore it does not need a successor, since it
    //	marks the end of the pattern, so that no more successors are needed.
    //  Note: this is the only case where Pthen == null
    public static final PE EOP;

    //////////////////////////////////////////////////
    //Static initialization
    static {
	EOP = new PE(PC_EOP, null);
	CP_Assign = new PE(PC_Assign, EOP);
	CP_Replace = new PE(PC_Replace, EOP);
	CP_Cancel = new PE(PC_Cancel, EOP);
	CP_Fence_Y = new PE(PC_Fence_Y, EOP);
	CP_R_Remove = new PE(PC_R_Remove, EOP);
	CP_R_Restore = new PE(PC_R_Restore, EOP);
	CP_R_NULL = new PE(PC_Unknown, EOP);
    }


}
