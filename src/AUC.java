

public class AUC {
	int expected;
	int returned;
	String commit;
	public AUC ( String c,int e,int r) {
		commit  =c;
		expected=e;
		returned=r;
	}
	
	public int getExpected() {
		return expected;
	}
	
	public int getReturned() {
		return returned;
	}

	public String getCommit() {
		return commit;
	}

}
