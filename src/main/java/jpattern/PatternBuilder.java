package jpattern;

import static jpattern.PatternCode.*;
import static jpattern.util.JpatternConstants.EOP;

import java.util.HashSet;
import java.util.HashMap;

abstract public class PatternBuilder
{

    //////////////////////////////////////////////////
    // Public Static Members for Pattern construction
    //////////////////////////////////////////////////

    //----------
    // Abort/Cancel
    //----------

    static public Pattern Cancel() 
    {
        return new PE(PC_Cancel, EOP);
    } //Cancel;

    static public Pattern Abort() 
    {
        return Cancel();
    }

    //--------
    // "or" --
    //--------

    static public Pattern Alternate(Pattern L, Pattern R) 
    {
        //  if the left pattern is null, then we just add the alternation
        //  node with an index one greater than the right hand pattern.

        if(L == EOP) {
            return new PE(PC_Alt, EOP, R);
        }

        //  if the left pattern is non-null, then build a reference vector
        //  for its elements, and adjust their index values to acccomodate
        //  the right hand elements. Then add the alternation node.

        return new PE(PC_Alt, (PE) L, (PE) R);
    } //Alternate;

    //-------
    // Any --
    //-------

    static public Pattern Any(String Str) 
    {
        return (Str.length() == 1 ? new PE(PC_Any_CH, EOP, Str.charAt(0))
            : new PE(PC_Any_CS, EOP, Str));
    } //Any;

    static public Pattern Any(Variable Var) 
    {
        return new PE(PC_Any_V, EOP, Var);
    } //Any;

    //-------
    // Arb --
    //-------

    //	  +---+
    //	  | X |---->
    //	  +---+
    //	    .
    //	    .
    //	  +---+
    //	  | Y |---->
    //	  +---+

    //	The PC_Arb_X element is numbered 2, and the PC_Arb_Y element is 1.

    static public Pattern Arb() 
    {
        PE Y = new PE(PC_Arb_Y, EOP);
        return new PE(PC_Arb_X, EOP, Y);
    } //Arb;

    //---------
    // Arbno --
    //---------

    static public Pattern Arbno(String P) 
    {
        if(P == null || P.length() == 0)
            return EOP;
        return Arbno_Simple(String_To_PE(P));
    }

    //  This is the complex case, either the pattern makes stack entries
    //  or it is possible for the pattern to match the null string(more
    //  accurately, we don't know that this is not the case).

    //      +--------------------------+
    //      |			  ^
    //      V			  |
    //    +---+			  |
    //    | X |---->			  |
    //    +---+			  |
    //      .			  |
    //      .			  |
    //    +---+     +---+	 +---+	  |
    //    | E |---->| P |---->| Y |--->+
    //    +---+     +---+	 +---+

    //  The node numbering of the constituent pattern P is not affected.
    //  Where N is the number of nodes in P, the Y node is numbered N + 1,
    //  the E node is N + 2, and the X node is N + 3.

    static public Pattern Arbno(Pattern P) 
    {
        PE Pat = Copy((PE) P);
        if(isSimpleArbno(Pat.Pcode)) {
            return Arbno_Simple(Pat);
        }
        PE E = new PE(PC_R_Enter, EOP);
        PE X = new PE(PC_Arbno_X, EOP, E);
        PE Y = new PE(PC_Arbno_Y, X);
        PE EPY = Bracket(E, Pat, Y);
        X.Alt = EPY;
        return X;
    } //Arbno;

    //--------
    // "**" --
    //--------

    //	Assign on match

    //	  +---+	    +---+     +---+
    //	  | E |---->| P |---->| A |---->
    //	  +---+	    +---+     +---+

    //	The node numbering of the constituent pattern P is not affected.
    //	Where N is the number of nodes in P, the A node is numbered N + 1,
    //	and the E node is N + 2.

    static public Pattern Assign(Pattern P, Variable var) 
    {
        PE pat = Copy((PE) P);
        PE E = new PE(PC_R_Enter, EOP);
        PE A = new PE(PC_Assign_OnM, EOP, var);
        return Bracket(E, pat, A);
    } //"*";

    //-------
    // Bal --
    //-------

    static public Pattern Bal() 
    {
        return new PE(PC_Bal, EOP);
    } //Bal;

    static public Pattern Bal(String parens) 
    {
        if(parens.length() != 2)
            throw new Failure("Bal(S): illegal argument: " + parens);
        return new PE(PC_Bal_CS, EOP, parens);
    } //Break;

    static public Pattern Bal(Variable Var) 
    {
        return new PE(PC_Bal_V, EOP, Var);
    } //Break;

    //---------
    // Break --
    //---------

    static public Pattern Break(String Str) 
    {
        return (Str.length() == 1 ? new PE(PC_Break_CH, EOP, Str.charAt(0))
            : new PE(PC_Break_CS, EOP, Str));
    } //Break;

    static public Pattern Break(Variable Var) 
    {
        return new PE(PC_Break_V, EOP, Var);
    } //Break;

    //----------
    // BreakX --
    //----------

    static public Pattern BreakX(String Str) 
    {
        PE P, X;
        if(Str.length() == 1) {
            char c = Str.charAt(0);
            X = new PE(PC_BreakX_CH_X, EOP, c);
            P = new PE(PC_BreakX_CH, EOP, c);
        } else {
            X = new PE(PC_BreakX_S_X, EOP, Str);
            P = new PE(PC_BreakX_S, EOP, Str);
        }
        P.Alt = X;
        X.Alt = X;
        return P;
    } //BreakX;

    //	  +---+	    +---+
    //	  | B |---->| A |---->
    //	  +---+	    +---+
    //	    ^	      .
    //	    |	      .
    //	    |	    +---+
    //	    +<------| X |
    //		    +---+

    //	The B node is numbered 3, the alternative node is 1, and the X
    //	node is 2.

    static public Pattern BreakX(Variable Var) 
    {
        PE X = new PE(PC_BreakX_V_X, EOP, Var);
        PE P = new PE(PC_BreakX_V, EOP, Var);
        P.Alt = X;
        X.Alt = X;
        return P;
    }

    //-------
    // "&" --
    //-------

    static public Pattern Concat(Pattern L, Pattern R) 
    {
        PE pr = Copy((PE) R);
        PE pl = Copy((PE) L);
        PE p = Set_Successor(pl, pr);
        return p;
    } //"&";

    //-------
    // "+" --
    //-------

    static public Pattern Defer(Variable Var) 
    {
        return new PE(PC_Rpat, EOP, Var);
    } //"+";

    //--------
    // Fail --
    //--------

    static public Pattern Fail() 
    {
        return new PE(PC_Fail, EOP);
    } //Fail;

    //---------
    // Fence --
    //---------

    //	Simple case

    static public Pattern Fence() 
    {
        return new PE(PC_Fence, EOP);
    } //Fence;

    //	Function Case

    //	  +---+	    +---+     +---+
    //	  | E |---->| P |---->| X |---->
    //	  +---+	    +---+     +---+

    //	The node numbering of the constituent pattern P is not affected.
    //	Where N is the number of nodes in P, the X node is numbered N + 1,
    //	and the E node is N + 2.

    static public Pattern Fence(Pattern P) 
    {
        PE Pat = Copy((PE) P);
        PE E = new PE(PC_R_Enter, EOP);
        PE X = new PE(PC_Fence_X, EOP);
        return Bracket(E, Pat, X);
    } //Fence;

    //-------
    // "*" --
    //-------

    //	Assign immediate

    //	  +---+	    +---+     +---+
    //	  | E |---->| P |---->| A |---->
    //	  +---+	    +---+     +---+

    //	The node numbering of the constituent pattern P is not affected.
    //	Where N is the number of nodes in P, the A node is numbered N + 1,
    //	and the E node is N + 2.

    static public Pattern IAssign(Pattern P, Variable var) 
    {
        PE pat = Copy((PE) P);
        PE E = new PE(PC_R_Enter, EOP);
        PE A = new PE(PC_Assign_Imm, EOP, var);
        PE b = Bracket(E, pat, A);
        return b;
    } //"*";

    //-------
    // Java --
    //-------

    static public Pattern Java(Object o) 
    {
        // This is applied to back quoted strings that
        // appear in the position of pattern arguments.
        // It ensures that the result is a Pattern instance
        if(o == null) o = "";
        if(o instanceof Pattern) return (Pattern) o;
        return StringPattern(o.toString());
    } //Java;

    //-------
    // Len --
    //-------

    static public Pattern Len(int Count) 
    {
        //  Note, the following is not just an optimization, it is needed
        //  to ensure that Arbno(Len(0)) does not generate an infinite
        //  matching loop(since PC_Len_Nat isSimpleArbno).

        if(Count == 0) return EOP;
        return new PE(PC_Len_N, EOP, Count);
    } //Len;

    static public Pattern Len(Variable Var) 
    {
        return new PE(PC_Len_V, EOP, Var);
    } //Len;

    //----------
    // NotAny --
    //----------

    static public Pattern NotAny(String Str) 
    {
        return (Str.length() == 1 ? new PE(PC_NotAny_CH, EOP, Str.charAt(0))
            : new PE(PC_NotAny_CS, EOP, Str));
    } //NotAny;

    static public Pattern NotAny(Variable Var) 
    {
        return new PE(PC_NotAny_V, EOP, Var);
    } //NotAny;

    //---------
    // NSpan --
    //---------

    static public Pattern NSpan(String Str) 
    {
        return (Str.length() == 1 ? new PE(PC_NSpan_CH, EOP, Str.charAt(0))
            : new PE(PC_NSpan_CS, EOP, Str));
    } //NSpan;

    static public Pattern NSpan(Variable Var) 
    {
        return new PE(PC_NSpan_V, EOP, Var);
    } //NSpan;

    //-------
    // Pos --
    //-------

    static public Pattern Pos(int Count) 
    {
        return new PE(PC_Pos_N, EOP, Count);
    } //Pos;

    static public Pattern Pos(Variable Var) 
    {
        return new PE(PC_Pos_V, EOP, Var);
    } //Pos;

    //--------
    // "="
    //--------

    //	Replace on match

    //	  +---+	    +---+     +---+
    //	  | E |---->| P |---->| R |---->
    //	  +---+	    +---+     +---+

    //	The node numbering of the constituent pattern P is not affected.
    //	Where N is the number of nodes in P, the A node is numbered N + 1,
    //	and the E node is N + 2.

    static public Pattern Replace(Pattern P, Variable var) 
    {
        PE pat = Copy((PE) P);
        PE E = new PE(PC_R_Enter, EOP);
        PE R = new PE(PC_Replace_OnM, EOP, var);
        return Bracket(E, pat, R);
    } //"*";

    //--------
    // Rem/Rest --
    //--------

    static public Pattern Rest() 
    {
        return new PE(PC_Rest, EOP);
    } //Rest;

    static public Pattern Rem() 
    {
        return Rest();
    }
    //--------
    // RPos --
    //--------

    static public Pattern RPos(int Count) 
    {
        return new PE(PC_RPos_N, EOP, Count);
    } //RPos;

    static public Pattern RPos(Variable Var) 
    {
        return new PE(PC_RPos_V, EOP, Var);
    } //RPos;

    //--------
    // RTab --
    //--------

    static public Pattern RTab(int Count) 
    {
        return new PE(PC_RTab_N, EOP, Count);
    } //RTab;

    static public Pattern RTab(Variable Var) 
    {
        return new PE(PC_RTab_V, EOP, Var);
    } //RTab;

    //----------
    // Setcur --
    //----------

    static public Pattern Setcur(Variable var) 
    {
        return new PE(PC_Setcur, EOP, var);
    } //Setcur;

    //--------
    // Span --
    //--------

    static public Pattern Span(String Str) 
    {
        return (Str.length() == 1 ? new PE(PC_Span_CH, EOP, Str.charAt(0))
            : new PE(PC_Span_CS, EOP, Str));
    } //Span;

    static public Pattern Span(Variable Var) 
    {
        return new PE(PC_Span_V, EOP, Var);
    } //Span;

    //-----------
    // Succeed --
    //-----------

    static public Pattern Succeed() 
    {
        return new PE(PC_Succeed, EOP);
    } //Succeed;

    //-------
    // Tab --
    //-------

    static public Pattern Tab(int Count) 
    {
        return new PE(PC_Tab_N, EOP, Count);
    } //Tab;

    static public Pattern Tab(Variable Var) 
    {
        return new PE(PC_Tab_V, EOP, Var);
    } //Tab;


    //////////////////////////////////////////////////
    //-----------
    // S_To_PE --
    //-----------

    static public Pattern StringPattern(String Str)
    {
        return String_To_PE(Str);
    }

    //-----------
    // C_To_PE --
    //-----------

    static public Pattern CharPattern(char C) 
    {
        return Char_To_PE(C);
    } //C_To_PE;

    //////////////////////////////////////////////////
    // Protected members
    //////////////////////////////////////////////////

    //////////////////////////////////////////////////
    // Protected static builders

    //----------------
    // Arbno_Simple --
    //----------------

    //	    +-------------+
    //	    |	     ^
    //	    V	     |
    //	  +---+	     |
    //	  | S |---->	     |
    //	  +---+	     |
    //	    .	     |
    //	    .	     |
    //	  +---+	     |
    //	  | P |---------->+
    //	  +---+


    //	Note that we know that P cannot be EOP, because a null pattern
    //	does not meet the requirements for simple Arbno.

    static PE Arbno_Simple(PE P) 
    {
        PE S = new PE(PC_Arbno_S, EOP, P);
        Set_Successor(P, S);
        return S;
    } //Arbno_Simple;

    //-----------
    // Bracket
    //-----------
    // Effect is to bracket P with E and A ~= E & P & A
    // E is assumed to be a single element PE (not then or alt)
    // E, A, and P are assumed new or copied

    static PE Bracket(PE E, PE P, PE A) 
    {
        if(P == EOP) {
            E.Pthen = A;
        } else {
            E.Pthen = P;
            Set_Successor(P, A);
        }
        return E;
    } //Bracket;

    //-----------
    // C_To_PE --
    //-----------

    static PE Char_To_PE(char C) 
    {
        return new PE(PC_Char, EOP, C);
    } //C_To_PE;


    //-----------
    // Copy
    //-----------

    // Produce a deep copy duplicate of PE P and its subpatterns
    // Assume cycles are not possible, but DAG is ok => need to keep
    // track of common subgraphs

    static PE Copy(PE P)
    {
        return Copy(P, new HashMap<PE, PE>());
    }

    static PE Copy(PE P, HashMap<PE, PE> visited)
    {
        if(P == null || P == EOP) return P;
        PE copy = visited.get(P);
        if(copy != null) return copy;
        copy = new PE(P); // do the initial shallow copy
        visited.put(P, copy);
        copy.Pthen = Copy(copy.Pthen, visited);
        copy.Alt = Copy(copy.Alt, visited);
        if(copy.Pat != null)
            copy.Pat = Copy((PE) copy.Pat, visited);
        return copy;
    } //Copy;

    //-----------
    // S_To_PE --
    //-----------

    static PE String_To_PE(String Str)
    {
        int Len = Str.length();
        if(Len == 0) return EOP;
        if(Len == 1) return new PE(PC_Char, EOP, Str.charAt(0));
        return new PE(PC_String, EOP, Str);
    }

    //------------
    // External --
    //------------

    //  +------------+   
    //  | External_X |-------------->
    //	+------------+    |
    //        ||          |
    //        \/          |
    //  +------------+    |
    //  | External_Y |----
    //  +------------+

    static public Pattern External(String name, Object... argv) 
    {
        if(name == null)
            throw new Failure("External: null external pattern name");
        PE alt = new PE(PC_External_Y, EOP);
        alt.Str = name;
        PE pe = new PE(PC_External_X, EOP, alt, argv);
        pe.Str = name; // we will do the lookup at execution time
        return pe;
    } //External

    //-----------------
    // Set_Successor --
    //-----------------

    //  Cause all EOP's in L to point to R
    //  Assumed: L and R have been copied or are new,
    //  and are not null;
    //  either might contain cycles (See Arbno_Simple)

    static PE Set_Successor(PE L, PE R) 
    {
        if(L == null || R == null)
            throw new Failure("Set_Successor: null arg: L+" + L + " R=" + R);
        HashSet<PE> visited = new HashSet<PE>();
        // Mark R as visited and EOP as visited
        visited.add(R);
        visited.add(EOP);
        PE x = Set_Successor(L, R, visited);
        return x;
    }

    static PE Set_Successor(PE L, PE R, HashSet<PE> visited) 
    {
        if(R == EOP) return L;
        if(L == EOP) return R;
        if(visited.contains(L))
            return L; // avoid cycles
        visited.add(L);
        // recurse to find EOP's
        if(L.Pthen != null) {
            if(L.Pthen == EOP)
                L.Pthen = R; // short circuit
            else {
//Debug.println("L.Pthen="+L.Pthen.Pcode);
                L.Pthen = Set_Successor(L.Pthen, R, visited);
            }
        }
        if(L.Alt != null) {
            if(L.Alt == EOP)
                L.Alt = R;
            else {
//Debug.println("L.Alt="+L.Alt.Pcode);
                L.Alt = Set_Successor(L.Alt, R, visited);
            }
        }
        return L;
    } //Set_Successor;


}
