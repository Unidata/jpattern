package jpattern;

import jpattern.util.Debug;
import static jpattern.PatternCode.*;
import static jpattern.PatternArg.*;
import static jpattern.util.JpatternConstants.*;

// Note: there is a lot of rubbish from the original Ada
// that is left in for documentation purposes

public class Matcher implements MatchResult
{
    // Capitalization is not consistent because
    // of the history from the Ada version.

    //////////////////////////////////////////////////
    // Constructor

    // Full-blown Matcher constructor
    public Matcher(Pattern parent, String subject,
		   VarMap vars, ExternalMap externs,
		   int stacksize)
    {
        if(!(parent instanceof PE))
	    throw new Error("Matcher: illegal pattern argument:"+parent);
	setPattern(parent);
	setSubject(subject);
	setVarMap(vars);		
	setExternalMap(externs);
	setStackSize(stacksize);
    }

    // Subset constructors
    public Matcher(Pattern parent, String subject,
		   VarMap vars, ExternalMap externs)
        {this(parent,subject,vars,externs,DEFAULT_STACK_SIZE);}

    public Matcher(Pattern parent, String subject, VarMap vars)
        {this(parent,subject,vars,null);}

    public Matcher(Pattern parent, String subject)
        {this(parent,subject,null,null);}

    public Matcher(Pattern parent)
        {this(parent,null,null,null);}

   //////////////////////////////////////////////////
   // Parameter setting

   //  This global variable can be set True to cause all subsequent pattern 
   //  matches to operate in anchored mode. In anchored mode, no attempt is 
   //  made to move the anchor point, so that if the match succeeds it must 
   //  succeed starting at the first character. Note that the effect of 
   //  anchored mode may be achieved in individual pattern matches by using 
   //  Fence or Pos(0) at the start of the pattern.
   public Matcher setAnchorMode(boolean b) {anchoredMode = b; return this;}

   //  Size used for internal pattern matching stack. Increase this size if 
   //  complex patterns cause Pattern_Stack_Overflow to be raised.
   public Matcher setStackSize(int n) throws Error
   {
	if(n <= 0)
	    throw new Error("Matcher.setStackSize: illegal size: "+n);
	S = null;
	stackSize = n;
	return this;
   }

    // reset the target subject
    public Matcher setSubject(String newsubject)
    {
	Subject = newsubject;
	return this;
    }

    // Alias
    public Matcher reset(String newsubject) {return setSubject(newsubject);}

   public Matcher setVarMap(VarMap map) {Vars = map; return this;}
   public Matcher setExternalMap(ExternalMap map) {Externs = map; return this;}

   public Matcher setPattern(Pattern p) {Pat = (PE)p; return this;}

   //////////////////////////////////////////////////
   //---------
   // Match --
   //---------

   // Full-blown match method
   public boolean match() throws Error
   {
      return XMatch();
   } //Match;


   // Full-blown match method
    public boolean match(String subject, VarMap vars, ExternalMap externs)
	throws Error
    {
	setSubject(subject);
	setVarMap(vars);		
	setExternalMap(externs);
	return XMatch();
    }

   //////////////////////////////////////////////////
   // Protected members
   //////////////////////////////////////////////////

   //////////////////////////////////////////////////
   //---------------------
   // Global variables
   //---------------------
 
    protected boolean anchoredMode = false; 

    protected int stackSize = DEFAULT_STACK_SIZE;

    protected VarMap Vars = null;
    protected ExternalMap Externs = null;

    //  The pattern matching backtrack stack for this instance of Match	
    protected Stack S = null;

    protected String Subject = null;

    protected int Cursor = 0;
// Revised version 1.3: be more explicit
//                      and to correct some cursor counting errors.
//
	//  If the value is non-negative, then this value is the index showing
	//  the current position of the match in the subject string.
	//  The Snobol4  cursor points between characters, so cursor == 0
	//  implies the cursor is before the first character and cursor == 1
	//  implies the cursor is between the first and second characters.
	//  However, Snobol4 assumes that strings are indexed starting at 1,
        //  not 0 as in Java.  This means that various counting corrections
	//  must occur to get this correct in Xmatch().
	//  With respect to Java strings, this means that the next
        //  character to be matched is at Subject(Cursor) and the ith character
	//  (e.g. pos(i)) is actually at Subject(i - 1) (pos(0) is still
	//  defined to be before the beginning of the string).

	//  if the value is negative, then this is a saved stack pointer,
	//  typically a base pointer of an inner or outer region. Cursor
	//  temporarily holds such a value when it is popped from the stack
	//  by Fail. In all cases, Cursor is reset to a proper non-negative
	//  cursor value before the match proceeds(e.G. By propagating the
	//  failure and popping a "real" cursor value from the stack.

    protected PE Pat = null;

    protected ResultState Result = new ResultState();

    //////////////////////////////////////////////////
   // Track state of the overall match
   //	Done => match is completed
   //	Stop => abort match
   //   Succeed => this pattern has matched successfully
   //   Fail => this pattern has failed to match
   //   Continue => match next pattern element (assumes Node has been
   //               correctly set).
   protected enum State { Done, Stop, Succeed, Fail, Continue; };

   //////////////////////////////////////////////////
   //----------
   // XMatch --
   //----------

    protected boolean XMatch() throws Error
    {
	PE Node = null;
	//  Pointer to current pattern node. Initialized from Pat_P, and)
	//  updated as the match proceeds through its constituent elements.

	int Length =  Subject.length();
	//  Length of string(= Subject'Last, since Subject'First is always 1)
	//  Watch out: subject indices for our purposes run from 1..length
	//  not 0..length-1

        boolean Assign_OnM = false;
	//  Set True if assign-on-match or write-on-match operations may be
	//  present in the history stack, which must then be scanned on a
	//  successful match.

        boolean Replace_OnM = false;
	//  Set True if replace-on-match operations may be
	//  present in the history stack, which must then be scanned on a
	//  successful match.

	PE PE_Unanchored = new PE(PC_Unanchored, Pat);
	//  Dummy pattern element used in the unanchored case.

	ExternalMatcher externalMatcher = null;
	// Used to pass match state to an external pattern

        Report report;

	ExternalMatcher extmatcher = null;
	// This will be set up at end of each stack pop

	//  Start of processing for XMatch

	// Create stack if it does not already exist
	if(S == null) S = new Stack(stackSize);

	// Now we can create report
        report = new Report(Subject,S);

	if(Pat == null) throw new Error("Uninitialized_Pattern");

	//  In anchored mode, the bottom entry on the stack is an abort entry

	if(anchoredMode) {
	    S.Stack[S.Stack_Init].Node   = CP_Cancel;
	    S.Stack[S.Stack_Init].Cursor = 0;
	} else {
	    //	In unanchored more, the bottom entry on the stack references
	    //	the special pattern element PE_Unanchored, whose Pthen field
	    //	points to the initial pattern element. The cursor value in this
	    //	entry is the number of anchor moves so far.
	    S.Stack[S.Stack_Init].Node = PE_Unanchored;
	    S.Stack[S.Stack_Init].Cursor = 0;
	}

	S.Stack_Ptr    = S.Stack_Init;
	S.Stack_Base   = S.Stack_Ptr;
	Cursor	       = 0;
	Node	       = Pat;

	// Original code uses gotos, so we simulate with a loop
	// and some flags to simulate an FSA.

	State state = State.Continue;
	
fsa:	for(;;) {

	//---------------------------------------
	// Main Pattern Matching State Control --
	//---------------------------------------

	//  This is a state machine which uses gotos to change state. The
	//  initial state is Match, to initiate the matching of the first
	//  element, so the goto Match above starts the match. In the
	//  following descriptions, we indicate the global values that
	//  are relevant for the state transition.

Match:

	//------------------------------------------------
	// Main Pattern Match Element Matching Routines --
	//------------------------------------------------

	//  Here is the case statement that processes the current node. The
	//  processing for each element does one of five things:

	//    goto Match	  to start a match
	//    goto Succeed	  to move to the successor
	//    goto Match_Succeed  if the entire match succeeds
	//    goto Match_Fail	  if the entire match fails
	//    goto Fail		  to signal failure of current match

	//  Processing is NOT allowed to fall through

Debug.Println("Match Node="+Node);
	PatternCode code = Node.Pcode;
step:	switch (code) {

	//  Cancel
	case PC_Cancel:
	{
	    report.print(Cursor,Node);
	    {state = State.Stop; break step;}
	}

	//  Alternation
	case PC_Alt:
	{
	    report.print(Cursor,Node.Alt.toString());
            S.Push(Node.Alt,Cursor);
            Node = Node.Pthen;
            {state = State.Continue; break step;}
        }

        //  Any(one character case)
        case PC_Any_CH:
        {
	    report.print(Cursor,Node);
Debug.level(1).println("any: cursor="+Cursor);
Debug.level(1).println("any: length="+Length);
            if(Cursor < Length && Subject.charAt(Cursor)==Node.Char) {
                Cursor = Cursor + 1;
                {state = State.Succeed; break step;}
            } else
                {state = State.Fail; break step;}
        }

        //  Any(character set case)
        case PC_Any_CS:
        {
	    report.print(Cursor,Node);
            String s = Node.Str;
            if(Cursor < Length
                && Is_In(Subject.charAt(Cursor), s)) {
                Cursor = Cursor + 1;
                {state = State.Succeed; break step;}
            } else
                {state = State.Fail; break step;}
        }

        case PC_Any_V:
        {
	    report.print(Cursor,Node);
	    if(Vars == null)
		throw new Error("No VarMap context Provided: "+Node.Var);
            String s = Vars.getString(Node.Var);
            if(Cursor < Length
                && Is_In(Subject.charAt(Cursor), s)) {
                Cursor = Cursor + 1;
                {state = State.Succeed; break step;}
            } else
                {state = State.Fail; break step;}
        }

        //  Arb(initial match)
        case PC_Arb_X:
        {
	    report.print(Cursor,Node);
	    S.Push(Node.Alt,Cursor);
	    Node = Node.Pthen;
	    {state = State.Continue; break step;}
	}

	//  Arb(extension)
	case PC_Arb_Y :
	{
	    report.print(Cursor,Node);
	    if(Cursor < Length) {
		Cursor = Cursor + 1;
		S.Push(Node,Cursor);
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  Arbno_S(simple Arbno initialize). This is the node that
	//  initiates the match of a simple Arbno structure.
	case PC_Arbno_S:
	{
	    report.print(Cursor,Node);
	    S.Push(Node.Alt,Cursor);
	    Node = Node.Pthen;
	    {state = State.Continue; break step;}
	}

	//  Arbno_X(Arbno initialize). This is the node that initiates
	//  the match of a complex Arbno structure.
	case PC_Arbno_X:
	{
	    report.print(Cursor,Node);
	    S.Push(Node.Alt,Cursor);
	    Node = Node.Pthen;
	    {state = State.Continue; break step;}
	}

	//  Arbno_Y(Arbno rematch). This is the node that is executed
	//  following successful matching of one instance of a complex
	//  Arbno pattern.
	case PC_Arbno_Y:
	{
	    report.print(Cursor,Node);
	    boolean Null_Match =(Cursor == S.Stack[S.Stack_Base - 1].Cursor);
	    S.Pop_Region();
	    //	if arbno extension matched null, then immediately fail
	    if(Null_Match) {
		report.print(Cursor,Node);
		{state = State.Fail; break step;}
	    }
	    //	Here we must do a stack check to make sure enough stack
	    //	is left. This check will happen once for each instance of
	    //	the Arbno pattern that is matched. The Nat field of a
	    //	PC_Arbno pattern contains the maximum stack entries needed
	    //	for the Arbno with one instance and the successor pattern
//	    if(S.Stack_Ptr + Node.Int >= S.Stack.length)
//		throw new Error("Pattern_Stack_Overflow");
	    {state = State.Succeed; break step;}
	}

	//  Assign. if this node is executed, it means the assign-on-match
	//  or write-on-match operation will not happen after all, so we
	//  propagate the failure, removing the PC_Assign node.
	case PC_Assign:
	{
	    report.print(Cursor,Node);
	    {state = State.Fail; break step;}
	}

	//  Assign immediate. This node performs the actual assignment.
	case PC_Assign_Imm:
	{
	    int first = S.Stack[S.Stack_Base - 1].Cursor;
	    int last = Cursor;
	    String val = Subject.substring(first,last);
	    Debug.Println("Assign_Imm: "+Node.Var+"="+val);
	    Vars.write(Node.Var,val);
	    report.print(Cursor,Node);
	    S.Pop_Region();
	    {state = State.Succeed; break step;}
	}

	//  Assign on match. This node sets up for the eventual assignment
	case PC_Assign_OnM:
	{
	    report.print(Cursor,Node);
	    S.Stack[S.Stack_Base - 1].Node = Node; // save this node
						   // in the special entry
						   // node just below the
						   // remove entry node.
	    S.Push(CP_Assign,Cursor);
	    S.Pop_Region();
	    Assign_OnM = true;
	    {state = State.Succeed; break step;}
	}

	//  Bal, Bal(S), Bal(Var)
	case PC_Bal:
	case PC_Bal_CS:
	case PC_Bal_V:
        {
	    // common variable
	    String s = "()";
	    report.print(Cursor,Node);
	    if(code == PC_Bal_CS) {
		s = Node.Str;
	    } else if(code == PC_Bal_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    if(s.length() == 0) s = DFALT_BAL_PARENS;
	    else if(s.length() == 1) s = (s+s);
	    char lpar = s.charAt(0);
	    char rpar = s.charAt(1);
	    if(Cursor >= Length || Subject.charAt(Cursor) == rpar)
		{state = State.Fail; break step;}
	    else if(Subject.charAt(Cursor) == lpar) {
		int Paren_Count = 1;
		for(;;) {
		    Cursor = Cursor + 1;
		    if(Cursor >= Length) {
			{state = State.Fail; break step;}
		    } else if(Subject.charAt(Cursor) == lpar) {
			Paren_Count = Paren_Count + 1;
		    } else if(Subject.charAt(Cursor) == rpar) {
			Paren_Count = Paren_Count - 1;
			if(Paren_Count == 0) break;
		    }
		}
	    }
	    Cursor = Cursor + 1;
	    S.Push(Node,Cursor);
	    {state = State.Succeed; break step;}
        }

	//  Break(one character case)
	case PC_Break_CH:
	{
	    report.print(Cursor,Node);
	    while(Cursor < Length) {
		if(Subject.charAt(Cursor) == Node.Char)
		    {state = State.Succeed; break step;}
		else
		    Cursor = Cursor + 1;
	    }
	    {state = State.Fail; break step;}
	}

	//  Break(S), Break(V)
	case PC_Break_CS:
	case PC_Break_V:
	{
	    report.print(Cursor,Node);
	    String s = Node.Str;
	    if(code == PC_Break_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    while(Cursor < Length) {
		if(Is_In(Subject.charAt(Cursor), s))
		    {state = State.Succeed; break step;}
		else
		    Cursor = Cursor + 1;
	    }
	    {state = State.Fail; break step;}
	}

	//  BreakX(one character case)
	case PC_BreakX_CH:
	{
	    report.print(Cursor,Node);
	    while(Cursor < Length) {
		if(Subject.charAt(Cursor) == Node.Char)
		    {state = State.Succeed; break step;}
		else
		    Cursor = Cursor + 1;
	    }
	    {state = State.Fail; break step;}
	}

	//  BreakX(S), BreakX(V)
	case PC_BreakX_CS:
	case PC_BreakX_V:
	{
	    report.print(Cursor,Node);
	    String s = Node.Str;
	    if(code == PC_BreakX_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    while(Cursor < Length) {
		if(Is_In(Subject.charAt(Cursor), s))
		    {state = State.Succeed; break step;}
		else
		    Cursor = Cursor + 1;
	    }
	    {state = State.Fail; break step;}
	}

	//  BreakX_X(BreakX extension). See section on "Compound Pattern
	//  Structures". This node is the alternative that is stacked to
	//  skip past the break character and extend the break.
	case PC_BreakX_X:
	{
	    report.print(Cursor,Node);
	    Cursor = Cursor + 1;
	    {state = State.Succeed; break step;}
	}

	//  Character(one character string)
	case PC_Char:
	{
	    report.print(Cursor,Node);
	    if(Cursor < Length
		&& Subject.charAt(Cursor) == Node.Char) {
		Cursor = Cursor + 1;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  End of Pattern
	case PC_EOP:
	{
	    if(S.Stack_Base == S.Stack_Init) {
		report.print(Cursor,"EOP");
		{state = State.Done; break step;}
	    } else {
		//  End of recursive inner match. See separate section on
		//  handing of recursive pattern matches for details.
		report.print(Cursor,"End of Recursive Match");
		Node = S.Stack[S.Stack_Base - 1].Node; // should be the PC_Rpat then node
Debug.Print("eop: "+S.prettyPrint());
		S.Pop_Region();
		{state = State.Continue; break step;}
	    }
	}

	// Initial external pattern invocation
	case PC_External_X:
        {
	    report.print(Cursor,Node);
	    String xpname = Node.Str;
	    if(Externs == null)
		throw new Error("External.initial: no ExternalMap context Provided: "+xpname);
	    ExternalPattern xp = Externs.get(xpname);
	    if(xp == null)
		throw new Error("External.initial: no External matching name: "+xpname);
	    Object[] argv = Node.Argv;
	    int argc = (argv == null?0:argv.length);
	    if(xp.getNargs() < argc)
		throw new Error("External.initial: too few parameters: "
				+xpname
				+"; need: "+xp.getNargs()+", saw: "+argc
				);
	    externalMatcher = xp.matcher(argv);
	    // Fill the relevant fields
	    externalMatcher.Subject = Subject;
	    externalMatcher.Vars    = Vars;
	    externalMatcher.Externs = Externs;
	    externalMatcher.Anchor  = Cursor;
	    externalMatcher.Cursor  = Cursor;
	    if(!externalMatcher.initial()) {
		// allow external pattern to clean up
		externalMatcher.fail();
		{state = State.Fail; break step;}
	    }
	    if(externalMatcher.Cursor > Length)
		throw new Error("External.initial: illegal cursor: "+externalMatcher.Cursor);
	    Cursor = externalMatcher.Cursor;
	    S.Push(Node.Alt,Cursor,externalMatcher);
	    {state = State.Succeed; break step;}
	}

	// Retry external pattern invocation
	case PC_External_Y:
	{
	    report.print(Cursor,Node);
	    String xpname = Node.Str;
	    ExternalPattern xp = Externs.get(xpname);
	    if(xp == null)
		throw new Error("External.retry: no External matching name: "+xpname);
	    // The externalMatcher will have been popped at Fail at the end
	    // of this loop
	    // Fill the relevant fields
	    externalMatcher.Cursor  = Cursor;
	    if(!externalMatcher.retry()) {
		// allow external pattern to clean up
		externalMatcher.fail();
		{state = State.Fail; break step;}
	    }
	    if(externalMatcher.Cursor > Length)
		throw new Error("External pattern illegal cursor: "+externalMatcher.Cursor);
	    Cursor = externalMatcher.Cursor;
	    S.Push(Node,Cursor,externalMatcher);
	    {state = State.Succeed; break step;}
	}

	//  Fail
	case PC_Fail:
	{
	    report.print(Cursor,Node);
	    {state = State.Fail; break step;}
	}

	//  Fence(built in pattern)
	case PC_Fence:
	{
	    report.print(Cursor,Node);
	    S.Push(CP_Cancel,Cursor);
	    {state = State.Succeed; break step;}
	}

	//  Fence Pattern node X. This is the node that gets control
	//  after a successful match of the fenced pattern.
	case PC_Fence_X:
	{
	    report.print(Cursor,Node);
	    S.Push();
	    StackEntry e = S.Top();
	    e.Cursor = ptrToCursor(S.Stack_Base);
	    e.Node = CP_Fence_Y;
//???	    S.Stack_Base = e.Cursor;
	    S.Stack_Base = cursorToPtr(S.Stack[S.Stack_Base].Cursor);
	    S.RegionDecr();
	    {state = State.Succeed; break step;}
	}

	//  Fence Pattern node Y. This is the node that gets control on
	//  a failure that occurs after the fenced pattern has matched.
	//  Note: the Cursor at this stage is actually the inner stack
	//  base value. We don't reset this, but we do use it to strip
	//  off all the entries made by the fenced pattern.
	case PC_Fence_Y:
	{
	    report.print(Cursor,Node);
// revision 1.3:
// Original Ada Appears to be wrong;
// we need to pop based on the Fence_Y cursor
// which will happen automatically, since Fence_Y cursor will become new cursor
// on fail    
// wrong: S.Stack_Ptr = Cursor - 2;
	    {state = State.Fail; break step;}
	}

	//  Len(Int), Len(Var)
	case PC_Len_N:
	case PC_Len_V:
	{
	    report.print(Cursor,Node);
	    int len = Node.Int;
	    if(code == PC_Len_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                len = Vars.getInt(Node.Var,0);
	    }
	    if(Cursor + len > Length)
		{state = State.Fail; break step;}
	    else {
		Cursor = Cursor + len;
		{state = State.Succeed; break step;}
	    }
	}
	//  NotAny(one character case)
	case PC_NotAny_CH:
	{
	    report.print(Cursor,Node);
	    if(Cursor < Length
		&& Subject.charAt(Cursor) != Node.Char) {
		Cursor = Cursor + 1;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  NotAny(String), NotAny(V)
	case PC_NotAny_CS:
	case PC_NotAny_V:
	{
	    report.print(Cursor,Node);
	    String s = Node.Str;
	    if(code == PC_NotAny_V) {
	        if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    if(Cursor < Length
		&& !Is_In(Subject.charAt(Cursor), s)) {
		Cursor = Cursor + 1;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  NSpan(one character case)
	case PC_NSpan_CH:
	{
	    report.print(Cursor,Node);
	    while(Cursor < Length
		  && Subject.charAt(Cursor) == Node.Char) Cursor++;
	    {state = State.Succeed; break step;}
	}

	//  NSpan(String), NSpan(Var)
	case PC_NSpan_CS:
	case PC_NSpan_V:
	{
	    report.print(Cursor,Node);
	    String s = Node.Str;
	    if(code == PC_NSpan_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    while(Cursor < Length
		    && Is_In(Subject.charAt(Cursor), s)) Cursor++;
	    {state = State.Succeed; break step;}
	}

	//  Pos(Int), Pos(V)
	case PC_Pos_N:
	case PC_Pos_V:
	{
	    report.print(Cursor,Node);
	    int pos = Node.Int;
	    if(code == PC_Pos_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                pos = Vars.getInt(Node.Var,0);
	    }
	    if(Cursor == pos)
		{state = State.Succeed; break step;}
	    else
		{state = State.Fail; break step;}
	}

	//  Region Enter. Initiate new pattern history stack region
	case PC_R_Enter:
	{
	    report.print(Cursor,Node);
	    S.Stack[S.Stack_Ptr + 1].Cursor = Cursor;
	    S.Push_Region();
	    {state = State.Succeed; break step;}
	}

	//  Region Remove node. This is the node stacked by an R_Enter.
	//  It removes the special format stack entry right underneath, and
	//  then restores the outer level stack base and signals failure.
	//  Note: the cursor value at this stage is actually the (negative)
	//  stack base value for the outer level.
	case PC_R_Remove:
	{
	    report.print(Cursor,Node);
	    S.Stack_Base = cursorToPtr(Cursor);
	    S.RegionDecr();
	    S.Pop();
	    {state = State.Fail; break step;}
	}

	//  Region restore node. This is the node stacked at the end of an
	//  inner level match. Its Pattern is to restore the inner level
	//  region, so that alternatives in this region can be sought.
	//  Note: the Cursor at this stage is actually the negative of the
	//  inner stack base value, which we use to restore the inner region.
	case PC_R_Restore:
	{
	    report.print(Cursor,Node);
	    S.RegionIncr();
	    S.Stack_Base = cursorToPtr(Cursor);
	    {state = State.Fail; break step;}
	}

	//  Replace. if this node is executed, it means the replace_on-match
	//  operation will not happen after all, so we
	//  propagate the failure, removing the PC_Replace node.
	//  Purpose of this node is to mark cursor for the replacement
	case PC_Replace:
	{
	    report.print(Cursor,Node);
	    {state = State.Fail; break step;}
	}

	//  Replace on match. This node sets up for the eventual replacement
	case PC_Replace_OnM:
	{
	    report.print(Cursor,Node);
	    S.Stack[S.Stack_Base - 1].Node = Node;
	    S.Push(CP_Replace,Cursor);
	    S.Pop_Region();
	    Replace_OnM = true;
	    {state = State.Succeed; break step;}
	}

	//  Rest
	case PC_Rest:
	{
	    report.print(Cursor,Node);
	    Cursor = Length;
	    {state = State.Succeed; break step;}
	}

	//  Initiate recursive and deferred match
	//  subsumes case PC_Pred_Func: 
	case PC_Rpat:
	{
	    report.print(Cursor,Node);
	    Object o = null;
	    if(Vars != null) o = Vars.read(Node.Var);
	    Debug.level(3).println("vars["+Node.Var+"]="+o);
	    if(o == null) {state = State.Fail; break step;}
	    if(o instanceof Boolean) {
		boolean b = ((Boolean)o).booleanValue();
		state = (b?State.Succeed:State.Fail);
		break step;
	    }
	    if(o instanceof Pattern) {
		S.Stack[S.Stack_Ptr + 1].Node = Node.Pthen;
		S.Push_Region();
		Node = (PE)o;
		{state = State.Continue; break step;}
	    }
	    // default is to convert to a string and match (see PC_String)
	    String s = o.toString();
	    int Len = s.length();
	    if(Cursor+Len <= Length
		&&  Subject.startsWith(s,Cursor)) {
		Cursor = Cursor + Len;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  RPos(Int), RPos(Var)
	case PC_RPos_N:
	case PC_RPos_V:
	{
	    report.print(Cursor,Node);
	    int pos = Node.Int;
	    if(code == PC_RPos_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                pos = Vars.getInt(Node.Var,0);
	    }
	    if((Length - Cursor) == pos)
		{state = State.Succeed; break step;}
	    else
		{state = State.Fail; break step;}
	}

	//  RTab(Int), RTab(Var)
	case PC_RTab_N:
	case PC_RTab_V:
	{
	    report.print(Cursor,Node);
	    int tabn = Node.Int;
	    if(code == PC_RTab_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                tabn= Vars.getInt(Node.Var,0);
	    }
	    if(Cursor <=(Length - tabn)) {
		Cursor = Length - tabn;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  Cursor assignment
	case PC_Setcur:
	{
	    report.print(Cursor,Node);
	    if(Vars != null) {
		Debug.Println("Setcur: "+Node.Var+"="+(Cursor));
	        Vars.write(Node.Var,new Integer(Cursor));
	    }
	    {state = State.Succeed; break step;}
	}

	//  Span(one character case)
	case PC_Span_CH:
	{
	    report.print(Cursor,Node);
	    int P = Cursor;
	    while(P < Length && Subject.charAt(P) == Node.Char) P++;
	    if(P != Cursor) {
		Cursor = P;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  Span(String), Span(Var)
	case PC_Span_CS: 
	case PC_Span_V: 
	{
	    String s = Node.Str;
	    if(code == PC_Span_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                s = Vars.getString(Node.Var);
	    }
	    int P = Cursor;
	    while(P < Length && Is_In(Subject.charAt(P), s)) P++;
	    if(P != Cursor) {
		Cursor = P;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  String
	case PC_String:
	{
	    report.print(Cursor,Node);
	    int Len = Node.Str.length();
	    if((Cursor + Len) <= Length
		&&  Subject.startsWith(Node.Str,Cursor)) {
		Cursor = Cursor + Len;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  Succeed
	case PC_Succeed:
	{
	    report.print(Cursor,Node);
	    S.Push(Node,Cursor);
	    {state = State.Succeed; break step;}
	}

	//  Tab(Int), Tab(Var)
	case PC_Tab_N:
	case PC_Tab_V:
	{
	    report.print(Cursor,Node);
	    int tabn = Node.Int;
	    if(code == PC_Tab_V) {
		if(Vars == null)
		    throw new Error("No VarMap context Provided: "+Node.Var);
                tabn= Vars.getInt(Node.Var,0);
	    }
	    if(tabn <= Length && Cursor <= tabn) {
		Cursor = tabn;
		{state = State.Succeed; break step;}
	    } else
		{state = State.Fail; break step;}
	}

	//  Unanchored movement
	case PC_Unanchored:
	{
	    report.print(Cursor,Node);
	    //	All done if we tried every position
	    if(Cursor >= Length)
		{state = State.Stop; break step;}
	    else {//  Otherwise extend the anchor point, and restack ourself
		Cursor = Cursor + 1;
		S.Push(Node,Cursor);
		{state = State.Succeed; break step;}
	    }
	}

	//  We are NOT allowed to fall though this case statement, since every
	//  match routine must end by executing a goto to the appropriate point
	//  in the finite state machine model.
	default:
	    throw new Error("Logic_Error: "+Node.Pcode);

	} // step: switch

	// After doing one op, check to see if we are done,
	// or if the match of the element failed

	switch(state) {
	    case Stop: //	 Come here if entire match fails
		Debug.level(1).println("Match Failed");
		Result.Subject = Subject;
		Result.Start   = 0;
		Result.Stop    = 0;
		return false;

	    case Done: //	 Come here if entire match succeeds
		// Cursor = current position in subject string
		Result.Subject = Subject;
		Result.Start   = S.Stack[S.Stack_Init].Cursor;
		Result.Stop    = Cursor;
		Debug.level(1).println("Match Succeeded");
		Debug.level(1).println("Range[%d..%d]=%s",
					Result.Start,Result.Stop-1,
					Subject.substring(Result.Start,
							  Result.Stop));
		//  Scan history stack for deferred assignments
		if(Assign_OnM && Vars != null) {
		    for(int e=S.Stack_Init;e<=S.Stack_Ptr;e++) {
			if(S.Stack[e].Node == CP_Assign) {
			    // Following assumes that the assign is followed
			    // by a PC_R_Replace node
			    int Inner_Base = cursorToPtr(S.Stack[e + 1].Cursor);
 			    int Special_Entry = Inner_Base - 1;
			    PE Node_OnM = S.Stack[Special_Entry].Node;
			    int Start = S.Stack[Special_Entry].Cursor;
			    int Stop = S.Stack[e].Cursor;
			    if(Node_OnM.Pcode == PC_Assign_OnM) {
				String val = Subject.substring(Start,Stop);
				Vars.write(Node_OnM.Var,val);
				Debug.level(3).println("Deferred Assign: %s=%s",S.Stack[e].Node,val);
			    } else
				throw new Error("Logic_Error");
			}
		    }
		}
		//  Scan history stack for deferred replacements
	        //  Do after assignments
		if(Replace_OnM && Vars != null) {
		    StringBuilder buf = new StringBuilder(Subject);
		    for(int e=S.Stack_Init;e<=S.Stack_Ptr;e++) {
			if(S.Stack[e].Node == CP_Replace) {
			    int Inner_Base = cursorToPtr(S.Stack[e + 1].Cursor);
			    int Special_Entry = Inner_Base - 1;
			    PE Node_OnM = S.Stack[Special_Entry].Node;
			    int Start = S.Stack[Special_Entry].Cursor;
			    int Stop = S.Stack[e].Cursor;
			    if(Node_OnM.Pcode == PC_Replace_OnM) {
				String val = Vars.getString(Node_OnM.Var);
				if(val == null) val = "";
				Debug.level(1).println("%s=%s",
					    buf.substring(Start,Stop),
					    val);
				Replace(buf,val,Start,Stop);
			    } else
				throw new Error("Logic_Error");
			}
		    }
		    Subject = buf.toString();
		    Result.Subject = Subject; // keep correct
		}
		return true;

	    case Fail:
		//  Come here if attempt to match current element fails
		//    Stack_Base    current stack base
		//    S.Stack_Ptr	    current stack pointer
		// If the stack is empty, then we fail the whole pattern
		StackEntry e = S.Top();
		Cursor = e.Cursor;
		Node   = e.Node;
		externalMatcher = e.Extern; // only used by PC_EXTERNAL_Y
		S.Pop();
		if(Cursor >= 0) {
		    Debug.level(3).println("Fail: cursor->"+Cursor);
		}
	 	report.print(Cursor,"Fail.");
		break;

	    case Succeed:
		//  Come here if attempt to match current element succeeds
		//    Cursor	    current position in subject string
		//    Node	    pointer to node successfully matched
		//    Stack_Base    current stack base
		//    S.Stack_Ptr	    current stack pointer
		Node = Node.Pthen;
	 	report.print(Cursor,"Succeed.");
	        break;
 
	    case Continue:
		 //  Come here to match the next pattern element
		 //    Cursor	    current position in subject string
		 //    Node	    pointer to node to be matched
		 //    Stack_Base    current stack base
		 //    S.Stack_Ptr	    current stack pointer
		break;
	} //switch; break;

	} // fsa: for(;;); break;

    } //XMatch;


    //////////////////////////////////////////////////
    // Implement approximations to the java.util.regex.Matcher interface
/*
    public boolean lookingAt()
    {
	setAnchorMode(true);

    }

    public boolean find()
    {
	setAnchorMode(false);
    }
*/


    //////////////////////////////////////////////////
    // Utilities

    public void Replace(StringBuilder s, String rep, int Start, int Stop)
	    {s.replace(Start,Stop,rep);}

    protected boolean Is_In(char C, String Str) {return Str.indexOf(C) >= 0;}

    static int cursorToPtr(int Cursor) {return (Cursor & PTRMASK);}
    static int ptrToCursor(int ptr) {return (ptr | PTRFLAG);}
    static boolean isPtr(int Cursor) {return ((Cursor & PTRFLAG)==PTRFLAG);}

    //////////////////////////////////////////////////
    // Utility classes

    protected final class ResultState
    {
	String Subject = null;
	int Start = 0;
	int Stop = 0;
    }

    protected final class Report
    {
	Stack S=null; String Subject = null;
        String prefix = null; String suffix=null; String range=null;

	public Report(String sub, Stack Stk) {S=Stk; Subject = sub;}
	public void print(int Cursor, PE Node)
	{
	    split(Cursor);
	    if(Debug.debugLevel < 3) {
		Debug.level(1).println("%d: \"%s^%s^%s\"; node=%s",
					S.Region_Level,
					prefix,range,suffix,

					Node.toString());
	    } else {
		Debug.level(1).println();
		Debug.level(1).println("[*/%d] \"%s^%s^%s\"; node=%s",
					S.Region_Level,
					prefix,range,suffix,
					Node.toString());
		Debug.level(3).print(S.prettyPrint());
	    }
	}
	void print(int Cursor, String s)
	{
	    split(Cursor);
	    Debug.level(1).println("%d: \"%s^%s^%s\" ; %s",
					S.Region_Level,
					prefix,range,suffix,
					s);
	}
	void split(int Cursor)
	{
	    int top = 0;
	    if(!S.empty()) {
		// find a real cursor
		for(int i=0;;i++) {
		    StackEntry nth = S.Nth(i);
		    if(nth == null) break;
		    if(!isPtr(nth.Cursor)) {
			top = nth.Cursor;
			break;
		    }
		}
	    }
	    top = Math.min(top,Subject.length());
	    int start = Math.max(top,0);
	    int stop = Math.max(Cursor,start);
	    boolean ptr = isPtr(Cursor);
	    prefix = (start == 0?"":Subject.substring(0,start));
	    range = (start == stop?"":Subject.substring(start,stop));
	    suffix = (stop >= Subject.length()?""
			      :Subject.substring(stop,Subject.length()));
Debug.level(1).println("cursor=%s%d top=%d start=%d stop=%d prefix=|%s| range=|%s| suffix=|%s| |subject|=%d subject=|%s|",
		(ptr?"&":""),(ptr?cursorToPtr(Cursor):Cursor),
		top,start,stop,prefix,range,suffix,Subject.length(),Subject);
	}
    };

    //////////////////////////////////////////////////
    // Interface MatchResult methods
    public String getSubject() {return Result.Subject; }
    public int getStart() {return Result.Start;}
    public int getStop() {return Result.Stop;}

    // Also provide a (non-interface) toString() function
    public String toString()
    {
	return String.format("MatchResult(%d,%d,|%s|)",
			     getStart(),
			     getStop(),
			     (getSubject()==null?"null":getSubject()));
    }

}
