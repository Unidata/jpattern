package jpattern.test;

import junit.framework.TestCase;


public class UnitTestCommon extends TestCase
{
    //////////////////////////////////////////////////
    // Constants
    static final String DEFAULTTREEROOT = "jpattern";
    static final String[] DEFAULTSUBDIRS
            = new String[]{"src"};

    //////////////////////////////////////////////////
    // Static variables

    // Define a tree pattern to recognize the root.
    static String patternroot = DEFAULTTREEROOT; // dir to locate
    static String[] patternsubdirs = DEFAULTSUBDIRS; // with these immediate subdirectories
    static final String root;

  static {
    // Compute the root path
    root = locateOpulsRoot();
  }

    //////////////////////////////////////////////////
    // static methods

    public String getRoot()
    {
        return root;
    }

    static void setTreePattern(String root, String[] subdirs)
    {
        patternroot = root;
        patternsubdirs = subdirs;
    }

    // Walk around the directory structure to locate
    // the path to a given directory.

    static String locateJpatternRoot()
    {
        // Walk up the user.dir path looking for a node that has
        // the name of the ROOTNAME and
        // all the directories in SUBROOTS.

        String path = System.getProperty("user.dir");

        // clean up the path
        path = path.replace('\\', '/'); // only use forward slash
        assert (path != null);
        if(path.endsWith("/")) path = path.substring(0, path.length() - 1);

        while(path != null) {
            // See if this is the tree root
            int index = path.lastIndexOf("/");
            if(index < 0)
                return null; // not found => we are root
            String lastdir = path.substring(index + 1, path.length());
            if(patternroot.equals(lastdir)) {// We have a candidate
                // See if all subdirs are immediate subdirectories
                boolean allfound = true;
                for(String dirname : patternsubdirs) {
                    // look for dirname in current directory
                    String s = path + "/" + dirname;
                    File tmp = new File(s);
                    if(!tmp.exists() || !tmp.isDirectory()) {
                        allfound = false;
                        break;
                    }
                }
                if(allfound)
                    return path; // presumably the root
            }
      path = path.substring(0, index); // move up the tree
    }
        return null;
    }

    static public void
    clearDir(File dir, boolean clearsubdirs)
    {
        // wipe out the dir contents
        if(!dir.exists()) return;
        for(File f : dir.listFiles()) {
            if(f.isDirectory()) {
                if(clearsubdirs) {
                    clearDir(f, true); // clear subdirs
          f.delete();
        }
            } else
              f.delete();
        }
    }

    //////////////////////////////////////////////////
    // Instance databuffer

    String title = "Testing";
    String name = "testcommon";

    public UnitTestCommon()
    {
        this("UnitTest");
    }

    public UnitTestCommon(String name)
    {
        super(name);
        this.name = name;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getTitle()
    {
        return this.title;
    }

  // Copy result into the a specified dir
  public void
  writefile(String path, String content)
    throws IOException
  {
    FileWriter out = new FileWriter(path);
    out.write(content);
    out.close();
  }

  // Copy result into the a specified dir
  static public void
  writefile(String path, byte[] content)
    throws IOException
  {
    FileOutputStream out = new FileOutputStream(path);
    out.write(content);
    out.close();
  }

  static public String
  readfile(String filename)
    throws IOException
  {
    StringBuilder buf = new StringBuilder();
    FileReader file = new FileReader(filename);
    BufferedReader rdr = new BufferedReader(file);
    String line;
    while((line = rdr.readLine()) != null) {
      if(line.startsWith("#")) continue;
      buf.append(line + "\n");
    }
    return buf.toString();
  }

  static public byte[]
  readbinaryfile(String filename)
    throws IOException
  {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    FileInputStream file = new FileInputStream(filename);
    return DapUtil.readbinaryfile(file);
  }

  public void
  visual(String header, String captured)
  {
    if(!captured.endsWith("\n"))
      captured = captured + "\n";
    // Dump the output for visual comparison
    System.out.println("Testing " + getName() + ": "+header+":");
    System.out.println("---------------");
    System.out.print(captured);
    System.out.println("---------------");
  }

  public boolean
  compare(String baselinecontent, String testresult)
    throws Exception
  {
    StringReader baserdr = new StringReader(baselinecontent);
    StringReader resultrdr = new StringReader(testresult);
    // Diff the two files
    Diff diff = new Diff("Testing " + getTitle());
    boolean pass = !diff.doDiff(baserdr, resultrdr);
    baserdr.close();
    resultrdr.close();
    return pass;
  }
}

