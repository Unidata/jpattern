package jpattern;

//-----------------------------
// The Pattern History Stack --
//-----------------------------

//  The pattern history stack is used for controlling backtracking when
//  a match fails. The idea is to stack entries that give a cursor value
//  to be restored, and a node to be reestablished as the current node to
//  attempt an appropriate rematch operation. The processing for a pattern
//  element that has rematch alternatives pushes an appropriate entry or
//  entry on to the stack, and the proceeds. if(a match fails at any point,
//  the top element of the stack is popped off, resetting the cursor and
//  the match continues by accessing the node stored with this entry.

import static jpattern.util.JpatternConstants.CP_Fence_Y;

class StackEntry
{
    //  Saved cursor value that is restored when this entry is popped
    //  from the stack if(a match attempt fails. Occasionally, this
    //  field is used to store a history stack pointer instead of a
    //  cursor. Such cases are noted in the documentation and the value
    //  stored is negative since stack pointer values are always negative.
    int Cursor = 0;

    //  This pattern element reference is reestablished as the current
    //  Node to be matched(which will attempt an appropriate rematch).
    PE Node = null;

    // Provide a field to allow external patterns to save state
    // between initial match and retry.
    ExternalMatcher Extern = null;

    public String toString()
    {
	// do some special casing
	String s = "";
	if(Matcher.isPtr(Cursor))
	    s += "&"+Matcher.cursorToPtr(Cursor);
	else
	    s += Cursor;
	s+= ("::"+Node);
//	if(Node == CP_Fence_Y) s += "{OldBase="+Cursor+"}";
	return s;
    }

}


