package kr_ac_yonsei_mobilesw_UI;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import java.awt.Font;

import javax.swing.JButton;
import javax.swing.ScrollPaneConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSplitPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;

import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class GenAndroidTestCodeUI extends JFrame implements InterfaceWithExecution {

	private JPanel contentPane;
	private static Benchmark benchmarkUI;
	private JTextArea txtAdbCommand;
	private JTextArea txtIntentSpec;
	private JTextField txtCount;
	private JComboBox cboMakeMode;
	private JCheckBox chkExtraValueReplace;
	
	private Random rand = new Random(System.currentTimeMillis());
	
	private JButton btnImportFromApk;
	private final static String labelBtnImport = "Import";
	private final static String labelBtnImporting = "Importing ...";
	
	private JFileChooser fc = new JFileChooser();
	
	private JFileChooser fcOutput = new JFileChooser();
	
	private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
	private FileHandler fileHandler;

	private static String genCommand;
	private JTextField textOutputDir;
	private JTextField textPackage;
	
	private JLabel lblTestCodeFileName;
	
	private String pkgName;
	private String clzName;
	private String testNo;
	private String testFileName;
	
	static {
		String osName = System.getProperty("os.name");
		String osNameMatch = osName.toLowerCase();
		if(osNameMatch.contains("linux")) {
			genCommand = "gen_linux";
		} else if(osNameMatch.contains("windows")) {
			genCommand = "gen.exe";
		} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
			genCommand = "gen_mac";
		}else {
			genCommand = "gen.exe"; // Windows OS by default
		}
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GenAndroidTestCodeUI frame = new GenAndroidTestCodeUI(benchmarkUI, args);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public GenAndroidTestCodeUI(Benchmark mUI, String[] args) {
		addFileHandler(logger);
		
		setTitle("Android Test Code");
		
		this.benchmarkUI = mUI;
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 1259, 750);
		setBounds(100, 100, 1111, 754);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(512, 162, 571, 525);
		contentPane.add(scrollPane);
		
		txtAdbCommand = new JTextArea();
		txtAdbCommand.setFont(new Font("Courier New", Font.PLAIN, 16));
		scrollPane.setViewportView(txtAdbCommand);
		
		JButton btnOk = new JButton("SAVE");
		btnOk.setFont(new Font("Arial", Font.PLAIN, 12));
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String pkg = textPackage.getText();
				
				if (pkg==null || pkg.equals(""))
					return;
				
				String outdir = textOutputDir.getText() + "/";
				
				saveAndroidTestCode(pkg, outdir, txtAdbCommand.getText(), GenAndroidTestCodeUI.this, false); 
				
				
			}
		});
		
		btnOk.setBounds(764, 127, 99, 30);
		contentPane.add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Close();
			}
		});
		btnCancel.setBounds(984, 127, 99, 30);
		contentPane.add(btnCancel);
		
		JButton btnMake = new JButton("Generate");
		btnMake.setFont(new Font("Arial", Font.PLAIN, 12));
		btnMake.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnMake.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				String pkg = textPackage.getText();
				
				if (pkg==null || pkg.equals(""))
					return;
				
				String command = System.getProperty("user.dir") 
						+ "/../GenTestsfromIntentSpec/bin/" + genCommand + " AndroidTestCode "; 
				
				
				String intentSepc = txtIntentSpec.getText();
				if(intentSepc.charAt(0) != '"')
				{
					intentSepc = "\"" + intentSepc;
				}
				if(intentSepc.charAt(intentSepc.length() - 1) != '"')
				{
					intentSepc = intentSepc + "\""; 
				}
				
				command = command + cboMakeMode.getSelectedIndex() + " " +
								    txtCount.getText() + " " +
								    intentSepc + " " +
								    pkg;
				
				System.out.println("RUN: " + command);
				
				ExecuteShellCommand.executeMakeTestArtifacts(GenAndroidTestCodeUI.this, command);
			}
		});
		btnMake.setBounds(654, 127, 98, 30);
		contentPane.add(btnMake);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 106, 482, 581);
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane_1);
		
		txtIntentSpec = new JTextArea();
		txtIntentSpec.setFont(new Font("Monospaced", Font.PLAIN, 16));
		//txtIntentSpec.setBounds(12, 98, 1219, 97);
		//contentPane.add(txtIntentSpec);
		scrollPane_1.setViewportView(txtIntentSpec);
		
		if (args.length >= 1) {
			txtIntentSpec.setText("");
			
			Scanner scan = new Scanner(new StringReader(args[0]));
			
			while (scan.hasNextLine()) {
				txtIntentSpec.append(scan.nextLine() + "\n");
			}
		}
		
		cboMakeMode = new JComboBox();
		cboMakeMode.setFont(new Font("Arial", Font.PLAIN, 12));
		cboMakeMode.setModel(new DefaultComboBoxModel(new String[] {"Compatible", "Shape-Compatible", "Random"}));
		cboMakeMode.setBounds(12, 31, 134, 30);
		contentPane.add(cboMakeMode);
		
		txtCount = new JTextField();
		txtCount.setFont(new Font("Arial", Font.PLAIN, 12));
		txtCount.setBounds(158, 33, 336, 28);
		contentPane.add(txtCount);
		txtCount.setColumns(10);
		
		JLabel lblMakemode = new JLabel("Mode : ");
		lblMakemode.setFont(new Font("Arial", Font.PLAIN, 12));
		lblMakemode.setBounds(12, 10, 75, 20);
		contentPane.add(lblMakemode);
		
		JLabel lblCount = new JLabel("Count :");
		lblCount.setFont(new Font("Arial", Font.PLAIN, 12));
		lblCount.setBounds(158, 10, 75, 20);
		contentPane.add(lblCount);
		
		JLabel lblIntentspec = new JLabel("Intent Specification :");
		lblIntentspec.setFont(new Font("Arial", Font.PLAIN, 12));
		lblIntentspec.setBounds(12, 76, 134, 20);
		contentPane.add(lblIntentspec);
		
		chkExtraValueReplace = new JCheckBox("ExtraValueReplace");
		chkExtraValueReplace.setFont(new Font("Arial", Font.PLAIN, 12));
		chkExtraValueReplace.setSelected(true);
		chkExtraValueReplace.setBounds(360, 693, 134, 23);
		contentPane.add(chkExtraValueReplace);
		
		fc.addChoosableFileFilter(new APKOrAndroidManifestFilter());
		fc.setAcceptAllFileFilterUsed(false);
		String importpath = Config.getImportPath();
		if (importpath != null) {
			fc.setCurrentDirectory(new File(importpath));
		}
		
		
		btnImportFromApk = new JButton("Import");
		btnImportFromApk.setFont(new Font("Arial", Font.PLAIN, 12));
		btnImportFromApk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				//fc = new JFileChooser();
				// fc.addChoosableFileFilter(new APKFilter());
				// fc.setAcceptAllFileFilterUsed(false);

				int returnVal = fc.showOpenDialog(GenAndroidTestCodeUI.this);
				
				File file;
				
		        if (returnVal != JFileChooser.APPROVE_OPTION) {
		        	return;
		        }
		        
		        file = fc.getSelectedFile();
		        
		        Config.putImportPath(fc.getCurrentDirectory().getAbsolutePath());
				
				String command = "java -cp \"" 
									+ System.getProperty("user.dir") + "/bin;" 
									+ System.getProperty("user.dir") + "/lib/*\" " 
									+ "com.example.java.GenIntentSpecFromAPK " 
									+ "\"" + file.getAbsolutePath() + "\""; // ' ' in the file name
				
				System.out.println("RUN: " + command);
				
				btnImportFromApk.setText(labelBtnImporting);
				ExecuteShellCommand.executeImportIntentSpecCommand(GenAndroidTestCodeUI.this, command);
			}
		});
		btnImportFromApk.setBounds(285, 71, 99, 30);
		contentPane.add(btnImportFromApk);
		
		JButton btnClear = new JButton("Clear");
		btnClear.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtIntentSpec.setText("");
			}
		});
		btnClear.setBounds(395, 71, 97, 30);
		contentPane.add(btnClear);
		
		textOutputDir = new JTextField();
		textOutputDir.setFont(new Font("Arial", Font.PLAIN, 12));
		textOutputDir.setBounds(667, 32, 416, 30);
		contentPane.add(textOutputDir);
		textOutputDir.setColumns(10);
		
		fcOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcOutput.setAcceptAllFileFilterUsed(false);
		importpath = Config.getImportPath();
		if (importpath != null) {
			fcOutput.setCurrentDirectory(new File(importpath));
		}
		
		JButton btnOutputDirectory = new JButton("Test Code Path :");
		btnOutputDirectory.setFont(new Font("Arial", Font.PLAIN, 12));
		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcOutput.showOpenDialog(GenAndroidTestCodeUI.this);
				
				File file;
				
		        if (returnVal != JFileChooser.APPROVE_OPTION) {
		        	return;
		        }
		        
		        file = fcOutput.getSelectedFile();
		        
		        Config.putImportPath(fcOutput.getCurrentDirectory().getAbsolutePath());
			
		        textOutputDir.setText(file.getAbsolutePath());
			}
		});
		btnOutputDirectory.setBounds(503, 31, 152, 30);
		contentPane.add(btnOutputDirectory);
		
		lblTestCodeFileName = new JLabel("${TEST CODE PATH}/${TEST CODE PACKAGE}/${TEST CODE CLASS}.java");
		lblTestCodeFileName.setFont(new Font("Arial", Font.PLAIN, 12));
		lblTestCodeFileName.setBounds(512, 106, 571, 21);
		contentPane.add(lblTestCodeFileName);

		
		JLabel lblPackage = new JLabel("Test Code Package :");
		lblPackage.setFont(new Font("Arial", Font.PLAIN, 12));
		lblPackage.setBounds(512, 79, 143, 15);
		contentPane.add(lblPackage);
		
		pkgName = "";
		
		clzName = "";
		
		testNo = "";
		
		textPackage = new JTextField();
		textPackage.setFont(new Font("Arial", Font.PLAIN, 12));
		textPackage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				char ch = arg0.getKeyChar();
				String ch_s = ch == '\b' ? "" : ch + "" ; 
				setTestFileName(textPackage.getText() + ch_s, "", "");
			}
		});
		textPackage.setBounds(667, 72, 416, 30);
		contentPane.add(textPackage);
		textPackage.setColumns(10);
		
		JButton btnClearAndroidTestCode = new JButton("Clear");
		btnClearAndroidTestCode.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClearAndroidTestCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtAdbCommand.setText("");
			}
		});
		btnClearAndroidTestCode.setBounds(875, 128, 97, 28);
		contentPane.add(btnClearAndroidTestCode);
		
		JLabel lblAndroidTestCode = new JLabel("Android Test Code:");
		lblAndroidTestCode.setFont(new Font("Arial", Font.PLAIN, 12));
		lblAndroidTestCode.setBounds(512, 135, 134, 15);
		contentPane.add(lblAndroidTestCode);
		
		testFileName = "";
	}
	
	public void setTestFileName(String pkg, String clz, String testno) {		
		lblTestCodeFileName.setText(getTestFileName(pkg, clz, testno));
	}
	
	public String getTestFileName(String pkg, String clz, String testno) {
		String _pkg = pkg==null || pkg.equals("") ? "${TEST PACKAGE PATH}" : pkg.replaceAll("\\.", "/");

		return "${TEST CODE PATH}/" + _pkg + "/${TEST CODE CLASS}" + ".java";
	}
	
	public void appendTxt_testArtifacts(String str)
	{
		txtAdbCommand.append(str);
		txtAdbCommand.setCaretPosition(txtAdbCommand.getCaretPosition() + str.length());
	}
	
	public void done_testArtifacts() {
	}	
	
	public void appendTxt_intentSpec(String str)
	{
		txtIntentSpec.append(str);
		txtIntentSpec.setCaretPosition(txtIntentSpec.getCaretPosition() + str.length());
	}
	
	public void done_intentSpec() {
		btnImportFromApk.setText(labelBtnImport);
	}
	
	public void Close()
	{
		super.dispose();
	}
	
	public void AddAdbCommand()
	{
		String[] adbCommand = parseStr(txtAdbCommand.getText().trim());

		DefaultTableModel modelAdbCommand = this.benchmarkUI.getModelAdbCommand();
		
		for(int i = 0; i < adbCommand.length; i++)
		{
			modelAdbCommand.addRow(makeRow(i, adbCommand[i]));			
		}
		
		Close();
	}
	
	public String[] parseStr(String str)
	{
		String[] spLine = str.split("\n");
		
		for(int i = 0; i < spLine.length; i++)
		{
			logger.info("BenchAdd => parseStr i : " + i);
			
			String org = "";
			String[] spToken = spLine[i].split(" ");
			
			for(int k = 0; k < spToken.length; k++)
			{
				if(spToken[k].equals("--es") || spToken[k].equals("-e"))
				{
					//String
				}
				else if(spToken[k].equals("--ez"))
				{
					if(spToken[k + 2].equalsIgnoreCase("true") == false && spToken[k + 2].equalsIgnoreCase("false") == false)
					{
						spToken[k + 2] = String.valueOf(rand.nextBoolean());						
					}
				}
				else if(spToken[k].equals("--ei"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = String.valueOf(rand.nextInt());
					}
					else
					{
						try{
							Integer.parseInt(spToken[k + 2]);
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = String.valueOf(rand.nextInt());
						}
					}
				}
				else if(spToken[k].equals("--el"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = String.valueOf(rand.nextLong());
					}
					else
					{
						try{
							Long.parseLong(spToken[k + 2]);
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = String.valueOf(rand.nextLong());
						}
					}
				}
				else if(spToken[k].equals("--ef"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = String.valueOf(rand.nextFloat());
					}
					else
					{
						try{
							Float.parseFloat(spToken[k + 2]);
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = String.valueOf(rand.nextFloat());
						}
					}
				}
				else if(spToken[k].equals("--eu"))
				{
					//String
				}
				else if(spToken[k].equals("--ecn"))
				{
					//String
				}
				else if(spToken[k].equals("--eia"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = randomIntArray();
					}
					else
					{
						try{
							Integer.parseInt(spToken[k + 2].replace(",", ""));
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = randomIntArray();
						}
					}
				}
				else if(spToken[k].equals("--ela"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = randomLongArray();
					}
					else
					{
						try{
							Long.parseLong(spToken[k + 2].replace(",", ""));
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = randomLongArray();
						}
					}
				}
				else if(spToken[k].equals("--efa"))
				{
					if(chkExtraValueReplace.isSelected() == true)
					{
						spToken[k + 2] = randomfloatArray();
					}
					else
					{
						try{
							Float.parseFloat(spToken[k + 2].replace(",", ""));
						}
						catch(NumberFormatException e)
						{
							spToken[k + 2] = randomfloatArray();
						}
					}
				}
			}
			
			for(int k = 0; k < spToken.length; k++)
			{
				if(spToken[k].equals("-a") || spToken[k].equals("-d") || spToken[k].equals("-t") || spToken[k].equals("-c") ||
						spToken[k].equals("-n") || spToken[k].equals("-f") || spToken[k].equals("-esn"))
				{
					if((spToken[k].length() + spToken[k + 1].length() + 2 + org.length()) > 1024)		//<shell_command> limit 1024byte
					{
						break;
					}
				}
				else if(spToken[k].equals("--es") || spToken[k].equals("-e") || spToken[k].equals("--ez") || spToken[k].equals("--ei") ||
						spToken[k].equals("--el") || spToken[k].equals("--ef") || spToken[k].equals("--eu") || spToken[k].equals("--ecn") ||
						spToken[k].equals("--eia") || spToken[k].equals("--ela") || spToken[k].equals("--efa"))
				{
					if((spToken[k].length() + spToken[k + 1].length() + spToken[k + 2].length() + 3 + org.length()) > 1024)		//<shell_command> limit 1024byte
					{
						break;
					}
				}
				
				org += spToken[k] + " ";
			}
			
			spLine[i] = org;
		}
		
		return spLine;
	}
	
	public String randomIntArray()
	{
		String intArray = "";
		
		int count = rand.nextInt((15 - 1) + 1) + 1;
		
		intArray = String.valueOf(rand.nextInt());
		for(int i = 1; i < count; i++)
		{
			intArray += "," + rand.nextInt();
		}
		
		return intArray;
	}
	
	public String randomLongArray()
	{
		String longArray = "";
		
		int count = rand.nextInt((15 - 1) + 1) + 1;
		
		longArray = String.valueOf(rand.nextLong());
		for(int i = 1; i < count; i++)
		{
			longArray += "," + rand.nextLong();
		}
		
		return longArray;
	}
	
	public String randomfloatArray()
	{
		String floatArray = "";
		
		int count = rand.nextInt((15 - 1) + 1) + 1;
		
		floatArray = String.valueOf(rand.nextFloat());
		for(int i = 1; i < count; i++)
		{
			floatArray += "," + rand.nextFloat();
		}
		
		return floatArray;
	}
	
	public Object[] makeRow(int seq, String str)
	{
		String dummy = "";
		
		Object[] row = new Object[]{String.valueOf(seq), str, dummy};
		
		return row;
	}
	
    private void addFileHandler(Logger logger) {
        try {
            fileHandler = new FileHandler(Benchmark.class.getName() + ".log");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        logger.addHandler(fileHandler);
    }
    
    public static void saveAndroidTestCode(String pkg, String outdir, String testcode, JFrame jframe, boolean overwrite) {
    	
    	// Create the directory
    	String pkgdir = pkg.replaceAll("\\.", "/") + "/";
    	outdir = outdir + "/";
		String dirtosave = outdir + pkgdir;
		
		File f_dirtosave = new File(dirtosave);
		if (f_dirtosave.exists() == false) {
			f_dirtosave.mkdirs();
		}
		
		// Write Java files
		BufferedWriter bw = null;
		try {
			Scanner scan = new Scanner(new StringReader(testcode));
			String pkgpathClzDotJava = null;
			
			// Find '#'
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				
				if (line.charAt(0) == '#') {
					pkgpathClzDotJava = line.substring(1);
					break;
				}
			}
			
			while (scan.hasNextLine()) {				

				if (pkgpathClzDotJava == null) break; // End of text
				
				// Save the file
				File f_filetosave = new File(outdir + pkgpathClzDotJava);
				pkgpathClzDotJava = null;
				
				int n = 0; // Overwrite if there is any existing file.
				
				if (f_filetosave.exists() && overwrite==false) {
					Object[] options = {"Yes", "No", "Yes To All", "Cancel" };
					n = JOptionPane.showOptionDialog(jframe,
					    "Are you sure to overwrite the existing file?" 
							+ "\n"
					    	+ f_filetosave.getAbsolutePath(),
					    "Question",
					    JOptionPane.YES_NO_CANCEL_OPTION,
					    JOptionPane.QUESTION_MESSAGE,
					    null,
					    options,
					    options[1]);
					
					if (n == 2) {
						overwrite = true;
					}
					
					if (n == 3) {
						break;
					}
				}
				
				if (n == 0 || n == 2) {
					bw = new BufferedWriter(new FileWriter(f_filetosave));
					
					while (scan.hasNextLine()) {
							String line = scan.nextLine();
							
							if (line.charAt(0) == '#') {
								pkgpathClzDotJava = line.substring(1);
								break;
							}
							else {
								bw.write(line + "\n");
							}
					}
					
					bw.close();
				}
			}
			
			scan.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private class APKOrAndroidManifestFilter extends FileFilter {
    	 
        //Accept all directories and all apk files.
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
     
            String extension = getExtension(f);
            if (extension != null && extension.equals(apk)) {
            	return true;
            }
            else {
            	return "AndroidManifest.xml".equals(f.getName());
            }
        }
     
        //The description of this filter
        public String getDescription() {
            return "APK or AndroidManifest.xml";
        }
        
        public final static String apk = "apk";
        
        /*
         * Get the extension of a file.
         */
        public String getExtension(File f) {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
     
            if (i > 0 &&  i < s.length() - 1) {
                ext = s.substring(i+1).toLowerCase();
            }
            return ext;
        }
    }
}
