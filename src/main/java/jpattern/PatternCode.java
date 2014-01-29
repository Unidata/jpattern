package jpattern;

import java.util.EnumSet;

public enum PatternCode
{
      PC_Unknown,
      PC_Alt,
      PC_Any_CH,
      PC_Any_CS,
      PC_Any_V,
      PC_Arb_X,
      PC_Arb_Y,
      PC_Arbno_S,
      PC_Arbno_X,
      PC_Arbno_Y,
      PC_Assign,
      PC_Assign_Imm,
      PC_Assign_OnM,
      PC_Bal,
      PC_Bal_CS,
      PC_Bal_V,
      PC_BreakX_CH,
      PC_BreakX_S,
      PC_BreakX_V,
      PC_BreakX_CH_X,
      PC_BreakX_S_X,
      PC_BreakX_V_X,
      PC_Break_CH,
      PC_Break_CS,
      PC_Break_V,
      PC_Cancel,
      PC_Cat,
      PC_Char,
      PC_EOP,
      PC_External_X,
      PC_External_Y,
      PC_Fail,
      PC_Fence,
      PC_Fence_X,
      PC_Fence_Y,
      PC_Len_N,
      PC_Len_V,
      PC_NSpan_CH,
      PC_NSpan_CS,
      PC_NSpan_V,
      PC_NotAny_CH,
      PC_NotAny_CS,
      PC_NotAny_V,
      PC_Pos_N,
      PC_Pos_V,
      PC_Replace,
      PC_Replace_OnM,
      PC_RPos_N,
      PC_RPos_V,
      PC_RTab_N,
      PC_RTab_V,
      PC_R_Enter,
      PC_R_Remove,
      PC_R_Restore,
      PC_Rest,
      PC_Rpat,
      PC_Setcur,
      PC_Span_CH,
      PC_Span_CS,
      PC_Span_V,
      PC_String,
      PC_Succeed,
      PC_Tab_N,
      PC_Tab_V,
      PC_Unanchored;

      static public final PatternCode END = PC_Unanchored;
      static public final int size = (1+END.ordinal());

    //  The following set is used to determine if a pattern used as an
    //  argument for Arbno is eligible for treatment using the simple Arbno
    //  structure(i.e. it is a pattern that is guaranteed to match at least
    //  one character on success, and not to make any entries on the stack).

    static EnumSet<PatternCode> PC_Simple_Arbno = EnumSet.of(
        PC_Any_CS,   PC_Any_CH,  PC_Any_V,     PC_Char,
        PC_Len_N,    PC_Len_V,   PC_NotAny_CS, PC_NotAny_CH,
        PC_NotAny_V, PC_Span_CS, PC_Span_CH,   PC_Span_V,
        PC_String);

    static boolean isSimpleArbno(PatternCode pc)
        {return PC_Simple_Arbno.contains(pc);}

    static String name(PatternCode pc)
    {
	String s = pc.toString();
	switch (pc) {
	    case PC_Cancel: s = "Abort"; break;
	    case PC_Rest: s = "Rem"; break;
	    default: // remove leading "PC_"
	        s.substring(2); break;
	}
	return s;
    }
}

