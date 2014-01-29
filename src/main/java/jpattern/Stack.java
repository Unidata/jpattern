package jpattern;

import jpattern.util.Debug;

import static jpattern.util.JpatternConstants.CP_R_Restore;
import static jpattern.util.JpatternConstants.CP_R_Remove;
import static jpattern.util.JpatternConstants.PTRMASK;

class Stack
{
    //  Keeps track of recursive region level. This is used only for
    //   debugging, it is the number of saved history stack base values.
    int Region_Level;

    int Stack_Size;

    public Stack(int size)
    {
        this.Stack_Size = size;
        Stack = new StackEntry[size];
        // fill in the stack with legitimate entries
        for(int i = 0;i < Stack.length;i++) Stack[i] = new StackEntry();
        Stack_Init = 0;
        Stack_Base = Stack_Init;
        Stack_Ptr = Stack_Init;
        Region_Level = 0;
        Debug.setDepth(Region_Level);
    }

    //  The pattern matching failure stack
    StackEntry[] Stack;

    //  Current stack pointer. This points to the top element of the stack
    //  that is currently in use. At the outer level this is the special
    //  entry placed on the stack according to the anchor mode.
    int Stack_Ptr;

    //  This is the initial value of the Stack_Ptr and Stack_Base. The
    //  initial(Stack'First) element of the stack is not used so that
    //  when we pop the last element off, Stack_Ptr is still in range.
    int Stack_Init;

    //  This value is the stack base value, i.E. The stack pointer for the
    //  first history stack entry in the current stack region. See separate
    //  section on handling of recursive pattern matches.
    int Stack_Base;


    //--------------
    // Pop_Region --
    //--------------

    //	Used at the end of processing of an inner region. if the inner
    //	region left no stack entries, then all trace of it is removed.
    //	Otherwise a PC_Restore_Region entry is pushed to ensure proper
    //	handling of alternatives in the inner region.

    void Pop_Region()
        
    {
        RegionDecr();

        // if nothing was pushed in the inner region, we can just get
        // rid of it entirely, leaving no traces that it was ever there
        if(Stack_Ptr == Stack_Base) {
            Stack_Ptr = Stack_Base - 2;
            Stack_Base = (Stack[Stack_Ptr + 2].Cursor & PTRMASK);
        } else {
            // if stuff was pushed in the inner region, then we have to
            // push a PC_R_Restore node so that we properly handle possible
            // rematches within the region.
            if(Stack_Ptr == Stack_Size) throw new Failure("Stack Overflow");
            Stack_Ptr = Stack_Ptr + 1;
            Stack[Stack_Ptr].Cursor = Matcher.ptrToCursor(Stack_Base);
            Stack[Stack_Ptr].Node = CP_R_Restore;
            Stack_Base = Matcher.cursorToPtr(Stack[Stack_Base].Cursor);
        }
    }

    //--------
    // Push --
    //--------

    // Make entry in pattern matching stack with current cursor value
    void Push(PE Node, int Cursor)
        
    {
        if(Stack_Ptr == Stack_Size) throw new Failure("Stack Overflow");
        Stack_Ptr = Stack_Ptr + 1;
        Stack[Stack_Ptr].Cursor = Cursor;
        Stack[Stack_Ptr].Node = Node;
    }

    //--------
    // Push --
    //--------

    // Make entry in pattern matching stack with current cursor value
    // and some external pattern state
    void Push(PE Node, int Cursor, ExternalMatcher matcher)
        
    {
        if(Stack_Ptr == Stack_Size) throw new Failure("Stack Overflow");
        Stack_Ptr = Stack_Ptr + 1;
        Stack[Stack_Ptr].Cursor = Cursor;
        Stack[Stack_Ptr].Node = Node;
        Stack[Stack_Ptr].Extern = matcher;
    }

    //---------------
    // Push_Region --
    //---------------

    // This void makes a new region on the history stack. The
    // caller first establishes the special entry on the stack, but
    // does not push the stack pointer. Then this call stacks a
    // PC_Remove_Region node, on top of this entry, using the cursor
    // field of the PC_Remove_Region entry to save the outer level
    // stack base value, and resets the stack base to point to this
    // PC_Remove_Region node.

    void Push_Region()
        
    {
        RegionIncr();
        if(Stack_Ptr >= Stack_Size - 1) throw new Failure("Stack Overflow");
        Stack_Ptr += 2;
        Stack[Stack_Ptr].Cursor = Matcher.ptrToCursor(Stack_Base);
        Stack[Stack_Ptr].Node = CP_R_Remove;
        Stack_Base = Stack_Ptr;
    }

    StackEntry Top()
    {
        return (Stack_Ptr < Stack_Init ? null : Stack[Stack_Ptr]);
    }

    StackEntry Nth(int n)
    {
        return (n > Stack_Ptr ? null : Stack[Stack_Ptr - n]);
    }

    boolean empty()
    {
        return Stack_Ptr <= Stack_Init;
    }

    void Pop()
    {
        Stack_Ptr--;
    }

    void Push()
    {
        if(Stack_Ptr == Stack_Size - 1)
            throw new Failure("Stack Overflow");
        Stack_Ptr++;
    }

    void RegionIncr()
    {
        Region_Level++;
        Debug.setDepth(Region_Level);
    }

    void RegionDecr()
    {
        Region_Level--;
        Debug.setDepth(Region_Level);
    }

    String prettyPrint()
    {
        String s = "";
        for(int sp = Stack_Ptr;sp >= 0;sp--) {
            String code = " ";
            if(sp == Stack_Base) code = "*";
            else if(sp == Stack_Init) code = "#";
            s += String.format("[%d]%s %s\n",
                sp,
                code,
                Stack[sp].toString());
        }
        return s;
    }

}
