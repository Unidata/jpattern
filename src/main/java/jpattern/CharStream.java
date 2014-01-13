package jpattern.util;

public class CharStream
{
    protected class Push{int ch; Push next;}
    protected Push pushed;
    protected CharSequence seq;
    protected int index;

    public CharStream() {reset();}
    public CharStream(CharSequence seq) {reset(seq);}

    public void pushback(int ch)
    {
	Push p = new Push();
	p.ch = ch;
	p.next = pushed;
	pushed = p;	
    }

    protected boolean pushed() {return pushed != null;}
    protected int unpush()
    {
	int ch = 0;
	if(pushed != null) {
	    ch = pushed.ch;
	    pushed = pushed.next;
	}
	return ch;
    }

    public int getch() {return pushed()?unpush():nextch();}

    protected  void reset() {pushed = null;}

    protected int nextch()
    {
	if(index == seq.length()) return -1; // EOF
	return seq.charAt(index++);
    }

    public CharStream reset(CharSequence seq)
    {
	reset();
	this.seq = seq;
	index = 0;
	return this;
    }

    // Allow for extraction of the string and the current cursor
    public CharSequence getSequence() {return seq;}
    public int getCursor() {return index;}

}; //CharStream
