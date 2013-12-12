import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LanguageSyntaxFileLoader {

	public static SyntaxSettings loadSyntaxFile(File f) {
		SyntaxSettings ret = new SyntaxSettings();
		
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(f);

			// optional, but recommended
			// read this -
			// http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();

			ret.setLanguageName(parseLanguageName(doc));
			ret.setKeywordString(parseKeywordString(doc));
			ret.setDataTypeString(parseDataTypeString(doc));
			ret.setLiteralValueString(parseLiteralValueString(doc));
			ret.setCommentMap(parseCommentMap(doc));
			
			System.out.println("Root element :"
					+ doc.getDocumentElement().getNodeName());

		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	private static String parseKeywordString(Document d) {
		String ret = "";
		
		NodeList nList = d.getElementsByTagName("Language_Keywords");
		
		if (nList.getLength() > 0) {
			ret = nList.item(0).getTextContent();
		}
		return ret;
	}
	
	private static String parseLanguageName(Document d) {
		String ret = "";
		
		NodeList nList = d.getElementsByTagName("Language_Name");
		
		if (nList.getLength() > 0) {
			ret = nList.item(0).getTextContent();
		}
		return ret;
	}
	
	private static String parseDataTypeString(Document d) {
		String ret = "";
		
		NodeList nList = d.getElementsByTagName("Data_Types");
		
		if (nList.getLength() > 0) {
			ret = nList.item(0).getTextContent();
		}
		return ret;
	}
	
	private static String parseLiteralValueString(Document d) {
		String ret = "";
		
		NodeList nList = d.getElementsByTagName("Literal_Value_Terms");
		
		if (nList.getLength() > 0) {
			ret = nList.item(0).getTextContent();
		}
		return ret;
	}
	
	private static ArrayList<Pair<String>> parseCommentMap(Document d) {
		ArrayList<Pair<String>> ret = new ArrayList<Pair<String>>();
		
		NodeList nList = d.getElementsByTagName("Comment");
		
		for (int i = 0; i < nList.getLength(); i++) {
			Element e = (Element) nList.item(i);
			
			String start = "";
			NodeList sList = e.getElementsByTagName("Start");
			if (sList.getLength() > 0) 
				start = sList.item(0).getTextContent();
			
			String end = "";
			NodeList eList = e.getElementsByTagName("End");
			if (eList.getLength() > 0) 
				end = eList.item(0).getTextContent();
			
			ret.add(new Pair<String>(start, end));
		}
		
		return ret;
	}
}
