
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Path;

public class Commit {
    String commitHash;
    boolean buggy;
    
    public Commit(String commit, boolean bug) {
        this.commitHash=commit;       
        this.buggy=bug;
        //System.out.println(this.commitHash+" "+this.buggy);

    }                        
    public String getCommitHash() {
        return commitHash;
    }
    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }
    
    public boolean isBuggy() {
        return buggy;
    }
    public void setBuggy(boolean buggy) {
        this.buggy = buggy;
    }

   
}


