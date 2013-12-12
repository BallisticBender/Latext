import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;

public class Latext extends JFrame implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8507865331275787162L;

	private File PLUGIN_DIR;

	private DefaultStyledDocument doc = new DefaultStyledDocument();
	private DefaultStyledDocument backDoc = new DefaultStyledDocument();

	private JMenuBar mBar;

	private boolean running;

	private long lastEdit;

	private JPanel container;
	private JTextPane textPane;
	private JScrollPane scroll;

	private SyntaxSettings settings;

	private String[] plugins;

	private File editingFile;

	private long lastSave;

	public Latext() {

		init();
		construct();

		running = true;
		Thread t = new Thread(this);
		t.start();
	}

	private void init() {

		loadPlugins();

		lastEdit = System.currentTimeMillis();
		lastSave = lastEdit;

		doc.addDocumentListener(new DocumentListener() {

			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				lastEdit = System.currentTimeMillis();
				System.out.println("EDIT");
			}

			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				lastEdit = System.currentTimeMillis();
				System.out.println("EDIT");
			}

		});

		editingFile = null;
	}

	private void loadPlugins() {
		
		File app = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		File topDir = app.getParentFile();
		PLUGIN_DIR = new File(topDir, "plugin");
		
		if (PLUGIN_DIR != null) {
			if (!PLUGIN_DIR.exists()) {
				PLUGIN_DIR.mkdir();
			}
		}
		
		
		if (PLUGIN_DIR.isDirectory()) {
			FilenameFilter filter = new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String arg1) {
					if (arg1.endsWith(".def")) {
						return true;
					}
					return false;
				}

			};
			plugins = PLUGIN_DIR.list(filter);
		}
		if (plugins == null)
			plugins = new String[0];
		// settings = LanguageSyntaxFileLoader.loadSyntaxFile(new
		// File("/home/ted/Desktop/JavaSyntax.def"));
		settings = new SyntaxSettings();
	}

	private void construct() {

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(640, 400);
		setLocationRelativeTo(null);

		container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));

		constructMenu();

		// container.add(mBar);

		textPane = new JTextPane(doc);

		scroll = new JScrollPane(textPane);
		container.add(scroll);

		this.setJMenuBar(mBar);
		this.add(container);
		setVisible(true);
	}

	private void constructMenu() {
		mBar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu plgn = new JMenu("Plugins");

		JMenuItem newFile = new JMenuItem("New");
		JMenuItem openFile = new JMenuItem("Open...");
		JMenuItem saveFile = new JMenuItem("Save");
		JMenuItem saveAsFile = new JMenuItem("Save As...");

		newFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				newFile();

			}

		});

		openFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				openFile();

			}
		});

		saveFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveFile();

			}
		});

		saveAsFile.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveAsFile();
			}
		});

		file.add(newFile);
		file.add(openFile);
		file.add(saveFile);
		file.add(saveAsFile);

		for (int i = 0; i < plugins.length; i++) {
			JMenuItem p = new JMenuItem(plugins[i]);
			p.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					JMenuItem m = (JMenuItem) e.getSource();
					switchPlugin(m.getText());
				}
			});
			plgn.add(p);
		}

		mBar.add(file);
		mBar.add(plgn);

		mBar.setVisible(true);
	}

	protected void switchPlugin(String name) {
		if (name == null) {
			return;
		}
		settings = LanguageSyntaxFileLoader.loadSyntaxFile(new File(PLUGIN_DIR, name));
	}

	protected void saveAsFile() {
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(container);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			editingFile = chooser.getSelectedFile();
			saveFile();
		}
	}

	protected void saveFile() {
		if (editingFile == null) {
			saveAsFile();
		} else { // file is instantiated and it exists
			saveFile(editingFile);
		}
	}

	protected void saveFile(File f) {

		if (f != null) {
			if (!f.exists()) {
				try {
					f.createNewFile();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(container,
							"Unable to create File!", "Error",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			if (f.canWrite()) {
				BufferedOutputStream str;
				try {
					str = new BufferedOutputStream(new FileOutputStream(f));

					String text = textPane.getText();
					byte[] toFile = text.getBytes();

					try {
						str.write(toFile);
						str.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(container,
						"Unable to write to that file!", "Error",
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}
		}
		lastSave = System.currentTimeMillis();
	}

	protected void openFile() {
		if (lastSave < lastEdit) {
			String filename = "New Document";
			if (editingFile != null)
				filename = editingFile.getName();

			int result = JOptionPane.showConfirmDialog(container,
					"Save changes to " + filename + " before closing?",
					"Warning", JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				saveFile();
			} else if (result == JOptionPane.NO_OPTION) {
				lastSave = System.currentTimeMillis();
			} else {
				return;
			}
		}
		JFileChooser chooser = new JFileChooser();
		int returnVal = chooser.showOpenDialog(container);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			openFile(chooser.getSelectedFile());
		}

	}

	private void openFile(File selectedFile) {
		if (selectedFile != null) {
			if (!selectedFile.exists()) {
				try {
					selectedFile.createNewFile();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(container,
							"Unable to create that file!", "Error",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
			}
			if (selectedFile.canRead()) {
				FileInputStream str;
				try {
					str = new FileInputStream(selectedFile);
				} catch (FileNotFoundException e) {
					JOptionPane.showMessageDialog(container,
							"File Disappeared!! WHAT DID YOU DO?!", "Error",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				long length = selectedFile.length();
				long bytesRead = 0;
				int readSize = 1024;

				byte[] read = new byte[readSize];
				String fileContent = "";

				while (bytesRead < length) {
					try {
						bytesRead += str.read(read);
					} catch (IOException e) {
						e.printStackTrace();
					}

					String s = new String(read);
					fileContent += s;
				}
				try {
					str.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				textPane.setText(fileContent);
				editingFile = selectedFile;
				lastSave = System.currentTimeMillis();
			}
		} else {
			JOptionPane.showMessageDialog(container,
					"Unable to read that file!", "Error",
					JOptionPane.INFORMATION_MESSAGE);
		}

	}

	protected void newFile() {
		if (lastSave < lastEdit) {
			String filename = "New Document";
			if (editingFile != null)
				filename = editingFile.getName();

			int result = JOptionPane.showConfirmDialog(this, "Save changes to "
					+ filename + " before closing?", "Warning",
					JOptionPane.YES_NO_CANCEL_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				saveFile();
			} else if (result == JOptionPane.NO_OPTION) {
				lastSave = System.currentTimeMillis();
			} else {
				return;
			}
		}
		editingFile = null;
		textPane.setText("");
	}

	public void run() {
		final AttributeSet kwAttr = getKeywordAttributes();
		final AttributeSet dtAttr = getDatatypeAttributes();
		final AttributeSet lvAttr = getLiteralValueAttributes();
		final AttributeSet attrBlack = getNormalAttributes();

		String text;
		long lastFormat = -1;

		while (running) {
			try {
				Thread.sleep(100);
				if (lastFormat != lastEdit
						&& (System.currentTimeMillis() - lastEdit) > 500) {
					lastFormat = lastEdit;
				} else {
					continue;
				}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				continue;
			}

			backDoc = new DefaultStyledDocument();

			try {
				text = doc.getText(0, doc.getLength());

				backDoc.insertString(0, text, attrBlack);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			int before = 0;
			
			int after = text.length();
			int wordL = before;
			int wordR = before;

			ArrayList<Pair<Integer>> qZ = new ArrayList<Pair<Integer>>();

			String commentAndQuoteStart = "\"|\'";

			for (int i = 0; i < settings.getCommentMap().size(); i++) {
				Pair<String> comment = settings.getCommentMap().get(i);

				commentAndQuoteStart += "|" + regexEscape(comment.element1);
			}
			
			// Scan for quotes and commas
			//System.out.println(commentAndQuoteStart);
			Pattern startP = Pattern.compile(commentAndQuoteStart); // start
																	// quote/comment
																	// pattern
			Matcher m = startP.matcher(text);

			while (m.find()) {
				int start = m.start();
				int end = m.end();

				String s = text.substring(start, end);

				if (s.equals("\"")) {
					Pattern endP = Pattern.compile("\"");
					m.usePattern(endP);
					if (m.find()) {
						end = m.end();
					} else {
						end = text.length();
					}

					highlightQuote(start, end, backDoc);
					qZ.add(new Pair(start, end));
				} else if (s.equals("\'")) {
					Pattern endP = Pattern.compile("\'");
					m.usePattern(endP);
					if (m.find()) {
						end = m.end();
					} else {
						end = text.length();
					}

					highlightQuote(start, end, backDoc);
					qZ.add(new Pair(start, end));
				} else {
					for (int i = 0; i < settings.getCommentMap().size(); i++) {
						Pair<String> comment = settings.getCommentMap().get(i);
						if (s.equals(comment.element1)) {
							Pattern endP = Pattern
									.compile(regexEscape(comment.element2));
							m.usePattern(endP);

							if (m.find()) {
								end = m.end();
							} else {
								end = text.length();
							}

							highlightComment(start, end, backDoc);
							qZ.add(new Pair(start, end));
							break;
						}
					}
				}

				if (m.hitEnd())
					break;
				m.usePattern(startP);
			}

			while (wordR <= after) {
				for (int i = 0; i < qZ.size(); i++) {
					Pair<Integer> p = qZ.get(i);

					if (wordR >= p.element1 && wordR <= p.element2) {
						wordR = p.element2;
						wordL = wordR;
						break;
					}
				}

				if (wordR > after)
					break;
				if (wordR == after
						|| String.valueOf(text.charAt(wordR)).matches("\\W")) {
					if (text.substring(wordL, wordR).matches(
							"(\\W)*" + "(" + settings.getKeywordString() + ")"))
						backDoc.setCharacterAttributes(wordL, wordR - wordL,
								kwAttr, false);
					else if (text.substring(wordL, wordR).matches(
							"(\\W)*(" + settings.getDataTypeString() + ")"))
						backDoc.setCharacterAttributes(wordL, wordR - wordL,
								dtAttr, false);
					else if (text.substring(wordL, wordR).matches(
							"(\\W)*(" + settings.getLiteralValueString() + ")"))
						backDoc.setCharacterAttributes(wordL, wordR - wordL,
								lvAttr, false);
					else
						backDoc.setCharacterAttributes(wordL, wordR - wordL,
								attrBlack, false);

					wordL = wordR;
				}
				wordR++;
			}
			int caretP = textPane.getCaretPosition();
			int hScrollP = scroll.getHorizontalScrollBarPolicy();
			int vScrollP = scroll.getVerticalScrollBarPolicy();

			backDoc.addDocumentListener(new DocumentListener() {

				@Override
				public void changedUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
				}

				@Override
				public void insertUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					lastEdit = System.currentTimeMillis();
					// System.out.println("EDIT");
				}

				@Override
				public void removeUpdate(DocumentEvent arg0) {
					// TODO Auto-generated method stub
					lastEdit = System.currentTimeMillis();
					// System.out.println("EDIT");
				}

			});

			doc = backDoc;
			textPane.setDocument(doc);

			textPane.setCaretPosition(caretP);

			scroll.setHorizontalScrollBarPolicy(hScrollP);
			scroll.setVerticalScrollBarPolicy(vScrollP);
		}
	}

	private void highlightComment(int startP, int endP, DefaultStyledDocument d) {
		AttributeSet a = getCommentAttributes();
		d.setCharacterAttributes(startP, endP, a, false);
	}

	private void highlightQuote(int startP, int endP, DefaultStyledDocument d) {
		AttributeSet a = getQuoteAttributes();
		d.setCharacterAttributes(startP, endP, a, false);
	}

	private AttributeSet getQuoteAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, Color.BLUE);
		ret = cont.addAttribute(ret, StyleConstants.Bold, false);

		return ret;
	}

	private AttributeSet getCommentAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, Color.GREEN);
		ret = cont.addAttribute(ret, StyleConstants.Bold, false);

		return ret;
	}

	private AttributeSet getNormalAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, Color.BLACK);
		ret = cont.addAttribute(ret, StyleConstants.Bold, false);

		return ret;
	}

	private AttributeSet getLiteralValueAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, new Color(127, 0, 85));
		ret = cont.addAttribute(ret, StyleConstants.Bold, true);

		return ret;
	}

	private AttributeSet getDatatypeAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, new Color(127, 0, 85));
		ret = cont.addAttribute(ret, StyleConstants.Bold, true);

		return ret;
	}

	private AttributeSet getKeywordAttributes() {
		StyleContext cont = StyleContext.getDefaultStyleContext();
		AttributeSet ret = cont.addAttribute(cont.getEmptySet(),
				StyleConstants.Foreground, new Color(127, 0, 85));
		ret = cont.addAttribute(ret, StyleConstants.Bold, true);

		return ret;
	}

	private String regexEscape(String s) {
		String ret = s;

		String fixExp = "\\*";

		Pattern p = Pattern.compile(fixExp);
		Matcher m = p.matcher(s);

		while (m.find()) {
			String start = s.substring(0, m.start());

			String end = s.substring(m.end() - 1);

			ret = start + "\\" + end;
		}

		return ret;
	}

	public static void main(String args[]) {
		new Latext();
	}
}