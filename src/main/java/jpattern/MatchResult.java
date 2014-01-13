package jpattern;

//  Type used to record result of pattern match
public interface MatchResult
{
    // return subject (may differ from the original if replacement occurs)
    public String getSubject();

    //  Starting index position(0 origin) of matched section of
    //  subject string.
    public int getStart();

    //  Ending index position(0 origin) of matched section of
    //  subject string.
    public int getStop();
}
