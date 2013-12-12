import java.util.ArrayList;


public class SyntaxSettings {

	private String languageName;
	private String kwString;
	private String dtString;
	private String lvString;
	private ArrayList<Pair<String>> commentStrings;
	
	public SyntaxSettings() {
		languageName = "";
		kwString = "";
		dtString = "";
		lvString = "";
		commentStrings = new ArrayList<Pair<String>>();
	}
	
	public void setLanguageName(String s) {
		languageName = s;
	}
	
	public void setKeywordString(String s) {
		kwString = s;
	}
	
	public void setDataTypeString(String s) {
		dtString = s;
	}
	
	public void setLiteralValueString(String s) {
		lvString = s;
	}
	
	public void setCommentMap(ArrayList<Pair<String>> comment) {
		commentStrings = comment;
	}
	
	public String getLanguageName() {
		return languageName;
	}
	
	public String getKeywordString() {
		return kwString;
	}
	
	public String getDataTypeString() {
		return dtString;
	}
	
	public String getLiteralValueString() {
		return lvString;
	}
	
	public ArrayList<Pair<String>> getCommentMap() {
		return commentStrings;
	}
}