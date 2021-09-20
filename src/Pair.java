

public class Pair {
	int expected;
	int predicted;
	
	public Pair(int e, int p) {
		expected=e;
		predicted=p;
	}
	public int getExpected() {
		return expected;
	}
	public void setExpected(int expected) {
		this.expected = expected;
	}

	public int getPredicted() {
		return predicted;
	}
	public void setPredicted(int predicted) {
		this.predicted = predicted;
	}
}
