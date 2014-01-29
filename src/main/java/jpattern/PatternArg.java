package jpattern;

import java.util.EnumSet;

// Tags every PatternCode with a type
// indicating its argument types or
// other properties (like arbno_simple)

public enum PatternArg
{
    PA_String,
    PA_Int,
    PA_Char,
    PA_Var,
    PA_External, // externally defined pattern
    PA_None;

    static public final PatternArg END = PA_None;
    static public final int size = (1+END.ordinal());

    // Following must be consistent
    // with the PatternCode Enumeration (PatternCode.java)
    static private PatternArg[] ArgType = new PatternArg[] {
	PA_None,	// PC_Unknown
	PA_None,	// PC_Alt
	PA_Char,	// PC_Any_CH
	PA_String,	// PC_Any_CS
	PA_Var,		// PC_Any_V
	PA_None,	// PC_Arb_X
	PA_Int,		// PC_Arb_Y
	PA_None,	// PC_Arbno_S
	PA_None,	// PC_Arbno_X
	PA_None,	// PC_Arbno_Y
	PA_None,	// PC_Assign
	PA_Var,		// PC_Assign_Imm
	PA_Var,		// PC_Assign_OnM
	PA_None,	// PC_Bal
	PA_String,	// PC_Bal_CS
	PA_Var,		// PC_Bal_V
	PA_Char,	// PC_BreakX_CH
	PA_String,	// PC_BreakX_CS
	PA_Var,		// PC_BreakX_V
	PA_Char,	// PC_BreakX_CH_X
	PA_String,	// PC_BreakX_S_X
	PA_Var,		// PC_BreakX_V_X
	PA_Char,	// PC_Break_CH
	PA_String,	// PC_Break_CS
	PA_Var,		// PC_Break_V
	PA_None,	// PC_Cancel, PC_Abort
	PA_None,	// PC_Cat
	PA_Char,	// PC_Char
	PA_None,	// PC_EOP
	PA_External,    // PC_External_X
	PA_External,    // PC_External_Y
	PA_None,	// PC_Fail
	PA_None,	// PC_Fence
	PA_None,	// PC_Fence_X
	PA_None,	// PC_Fence_Y
	PA_Int,		// PC_Len_N
	PA_Var,		// PC_Len_V
	PA_Char,	// PC_NSpan_CH
	PA_String,	// PC_NSpan_CS
	PA_Var,		// PC_NSpan_V
	PA_Char,	// PC_NotAny_CH
	PA_String,	// PC_NotAny_CS
	PA_Var,		// PC_NotAny_V
	PA_Int,		// PC_Pos_N
	PA_Var,		// PC_Pos_V
	PA_None,	// PC_Replace,
        PA_Var,		// PC_Replace_OnM,
	PA_Int,		// PC_RPos_N
	PA_Var,		// PC_RPos_V
	PA_Int,		// PC_RTab_N
	PA_Var,		// PC_RTab_V
	PA_None,	// PC_R_Enter
	PA_None,	// PC_R_Remove
	PA_None,	// PC_R_Restore
	PA_None,	// PC_Rest, PC_Rem
	PA_Var,		// PC_Rpat
	PA_Var,		// PC_Setcur
	PA_Char,	// PC_Span_CH
	PA_String,	// PC_Span_CS
	PA_Var,		// PC_Span_V
	PA_String,	// PC_String
	PA_None,	// PC_Succeed
	PA_Int,		// PC_Tab_N
	PA_Var,		// PC_Tab_V
	PA_None,	// PC_Unanchored
    };
    static PatternArg argType(PatternCode pc) {return ArgType[pc.ordinal()];}
    static {
	if(ArgType.length != PatternCode.size)
	    System.err.println("|PatternArg.ArgType| != |PatternCode.size|");
    }

}
