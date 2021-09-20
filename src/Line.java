

public class Line {
	String label;
	String line_text;
	String commit_id;
	public Line(String id,String label,String text) {
		this.commit_id = id;
		this.line_text = text;
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getLine_text() {
		return line_text;
	}
	public void setLine_text(String line_text) {
		this.line_text = line_text;
	}
	public String getCommit_id() {
		return commit_id;
	}
	public void setCommit_id(String commit_id) {
		this.commit_id = commit_id;
	}
	

}
