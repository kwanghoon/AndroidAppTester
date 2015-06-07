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
import java.util.Arrays;
import java.util.Random;
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
	private JComboBox cboComponent;
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
	private JTextField textClass;
	private JTextField textTestNo;
	
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
					GenAndroidTestCodeUI frame = new GenAndroidTestCodeUI(benchmarkUI);
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
	public GenAndroidTestCodeUI(Benchmark mUI) {
		addFileHandler(logger);
		
		setTitle("Generating Android Test Code");
		
		this.benchmarkUI = mUI;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1259, 750);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 449, 1219, 221);
		contentPane.add(scrollPane);
		
		txtAdbCommand = new JTextArea();
		txtAdbCommand.setFont(new Font("Courier New", Font.PLAIN, 12));
		scrollPane.setViewportView(txtAdbCommand);
		
		JButton btnOk = new JButton("SAVE");
		btnOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String pkg = textPackage.getText();
				String clz = textClass.getText();
				String testno = textTestNo.getText();
				
				if (pkg==null || pkg.equals("") 
						|| clz==null || clz.equals("") 
						|| testno==null || testno.equals(""))
					return;
				
				String outdir = textOutputDir.getText() + "/";
				String pkgdir = pkg.replaceAll("\\.", "/") + "/";
				String dirtosave = outdir + pkgdir;
				
				File f_dirtosave = new File(dirtosave);
				if (f_dirtosave.exists() == false) {
					f_dirtosave.mkdirs();
				}
				else {
					File f_filetosave = new File(outdir + getTestFileName(pkg, clz,testno));
					if (f_filetosave.exists()) {
						Object[] options = {"Yes", "No" };
						int n = JOptionPane.showOptionDialog(GenAndroidTestCodeUI.this,
						    "Are you sure to overwrite the existing file?",
						    "Question",
						    JOptionPane.YES_NO_CANCEL_OPTION,
						    JOptionPane.QUESTION_MESSAGE,
						    null,
						    options,
						    options[1]);
						
						if( n==1 ) return;
					}
					
					
				}
				
				File f_filetosave = new File(outdir + getTestFileName(pkg, clz,testno));
				BufferedWriter bw = null;
				try {
					bw = new BufferedWriter(new FileWriter(f_filetosave));
					bw.write(txtAdbCommand.getText());
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				} 
				
				
			}
		});
		
		btnOk.setBounds(1021, 672, 99, 30);
		contentPane.add(btnOk);
		
		JButton btnCancel = new JButton("Cancel");
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Close();
			}
		});
		btnCancel.setBounds(1132, 672, 99, 30);
		contentPane.add(btnCancel);
		
		cboComponent = new JComboBox();
		cboComponent.setModel(new DefaultComboBoxModel(new String[] {"Activity", "Broadcast Receiver", "Service"}));
		cboComponent.setBounds(158, 31, 129, 30);
		contentPane.add(cboComponent);
		
		JButton btnMake = new JButton("Generate Android Test Code");
		btnMake.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnMake.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				String pkg = textPackage.getText();
				String clz = textClass.getText();
				String testno = textTestNo.getText();
				
				if (pkg==null || pkg.equals("") 
						|| clz==null || clz.equals("") 
						|| testno==null || testno.equals(""))
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
								    cboComponent.getSelectedIndex() + " " +
								    txtCount.getText() + " " +
								    intentSepc + " " +
								    pkg + " " +
									clz + " " +
									testno;
				
				System.out.println("RUN: " + command);
				
				ExecuteShellCommand.executeMakeTestArtifacts(GenAndroidTestCodeUI.this, command);
			}
		});
		btnMake.setBounds(487, 31, 209, 30);
		contentPane.add(btnMake);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 162, 1219, 209);
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane_1);
		
		txtIntentSpec = new JTextArea();
		//txtIntentSpec.setBounds(12, 98, 1219, 97);
		//contentPane.add(txtIntentSpec);
		scrollPane_1.setViewportView(txtIntentSpec);
		
		cboMakeMode = new JComboBox();
		cboMakeMode.setModel(new DefaultComboBoxModel(new String[] {"Compatible", "Shape-Compatible", "Random"}));
		cboMakeMode.setBounds(12, 31, 134, 30);
		contentPane.add(cboMakeMode);
		
		txtCount = new JTextField();
		txtCount.setBounds(299, 33, 176, 28);
		contentPane.add(txtCount);
		txtCount.setColumns(10);
		
		JLabel lblMakemode = new JLabel("Mode : ");
		lblMakemode.setBounds(12, 10, 75, 20);
		contentPane.add(lblMakemode);
		
		JLabel lblComponent = new JLabel("Component : ");
		lblComponent.setBounds(159, 10, 89, 20);
		contentPane.add(lblComponent);
		
		JLabel lblCount = new JLabel("Count :");
		lblCount.setBounds(299, 10, 75, 20);
		contentPane.add(lblCount);
		
		JLabel lblIntentspec = new JLabel("Intent Specification :");
		lblIntentspec.setBounds(12, 132, 134, 20);
		contentPane.add(lblIntentspec);
		
		chkExtraValueReplace = new JCheckBox("ExtraValueReplace");
		chkExtraValueReplace.setSelected(true);
		chkExtraValueReplace.setBounds(879, 676, 134, 23);
		contentPane.add(chkExtraValueReplace);
		
		fc.addChoosableFileFilter(new APKOrAndroidManifestFilter());
		fc.setAcceptAllFileFilterUsed(false);
		
		btnImportFromApk = new JButton("Import");
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
				
				// Activity, Service, Broadcast Receiver 선택된 컴포넌트 타입의 Intent Spec을 가져오기
				String compTypeOption = "-all";
				int cbo = cboComponent.getSelectedIndex();
				switch (cbo) {
				case 0: compTypeOption = "-activity";
						break;
				case 1: return; // Not support for Broadcast Receiver type
				case 2: compTypeOption = "-service";
						break;
				}
				
				
				String command = "java -cp " + System.getProperty("user.dir") 
									+ "/../GenIntentSpecfromAPK/bin/GenIntentSpecfromAPK.jar com.example.java.GenIntentSpecFromAPK " 
									+ compTypeOption + " " 
									+ "\"" + file.getAbsolutePath() + "\""; // ' ' in the file name
				
				System.out.println("RUN: " + command);
				
				btnImportFromApk.setText(labelBtnImporting);
				ExecuteShellCommand.executeImportIntentSpecCommand(GenAndroidTestCodeUI.this, command);
			}
		});
		btnImportFromApk.setBounds(149, 127, 99, 30);
		contentPane.add(btnImportFromApk);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtIntentSpec.setText("");
			}
		});
		btnClear.setBounds(253, 127, 97, 30);
		contentPane.add(btnClear);
		
		textOutputDir = new JTextField();
		textOutputDir.setBounds(12, 410, 1219, 30);
		contentPane.add(textOutputDir);
		textOutputDir.setColumns(10);
		
		fcOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcOutput.setAcceptAllFileFilterUsed(false);
		
		JButton btnOutputDirectory = new JButton("Output Directory :");
		btnOutputDirectory.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int returnVal = fcOutput.showOpenDialog(GenAndroidTestCodeUI.this);
				
				File file;
				
		        if (returnVal != JFileChooser.APPROVE_OPTION) {
		        	return;
		        }
		        
		        file = fcOutput.getSelectedFile();
			
		        textOutputDir.setText(file.getAbsolutePath());
			}
		});
		btnOutputDirectory.setBounds(12, 381, 134, 26);
		contentPane.add(btnOutputDirectory);
		
		lblTestCodeFileName = new JLabel("*.java");
		lblTestCodeFileName.setBounds(158, 387, 890, 15);
		contentPane.add(lblTestCodeFileName);

		
		JLabel lblPackage = new JLabel("Package :");
		lblPackage.setBounds(12, 71, 57, 15);
		contentPane.add(lblPackage);
		
		pkgName = "";
		
		JLabel lblClass = new JLabel("Class :");
		lblClass.setBounds(378, 71, 57, 15);
		contentPane.add(lblClass);
		
		clzName = "";
		
		JLabel lblTestNo = new JLabel("Test No. :");
		lblTestNo.setBounds(600, 71, 57, 15);
		contentPane.add(lblTestNo);
		
		testNo = "";
		
		textPackage = new JTextField();
		textPackage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				char ch = arg0.getKeyChar();
				String ch_s = ch == '\b' ? "" : ch + "" ; 
				setTestFileName(textPackage.getText() + ch_s
						, textClass.getText()
						, textTestNo.getText());
			}
		});
		textPackage.setBounds(12, 92, 362, 30);
		contentPane.add(textPackage);
		textPackage.setColumns(10);
		
		textClass = new JTextField();
		textClass.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				char ch = arg0.getKeyChar();
				String ch_s = ch == '\b' ? "" : ch + "" ; 
				setTestFileName(textPackage.getText()
						, textClass.getText() + ch_s
						, textTestNo.getText());
			}
		});
		textClass.setBounds(378, 91, 219, 31);
		contentPane.add(textClass);
		textClass.setColumns(10);
		
		textTestNo = new JTextField();
		textTestNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent arg0) {
				char ch = arg0.getKeyChar();
				String ch_s = ch == '\b' ? "" : ch + "" ; 
				setTestFileName(textPackage.getText()
						, textClass.getText()
						, textTestNo.getText()  + ch_s);
			}
		});
		textTestNo.setBounds(600, 91, 96, 31);
		contentPane.add(textTestNo);
		textTestNo.setColumns(10);
		
		JButton btnClearAndroidTestCode = new JButton("Clear");
		btnClearAndroidTestCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtAdbCommand.setText("");
			}
		});
		btnClearAndroidTestCode.setBounds(1132, 381, 97, 23);
		contentPane.add(btnClearAndroidTestCode);
		
		testFileName = "";
		
		
		setTestFileName(textPackage.getText()
				, textClass.getText()
				, textTestNo.getText());
	}
	
	public void setTestFileName(String pkg, String clz, String testno) {		
		lblTestCodeFileName.setText(getTestFileName(pkg, clz, testno));
	}
	
	public String getTestFileName(String pkg, String clz, String testno) {
		String _pkg = pkg==null || pkg.equals("") ? "${PACKAGE NAME}" : pkg.replaceAll("\\.", "/");
		String _clz = clz==null || clz.equals("") ? "${CLASS NAME}" : clz;
		String _testno = testno==null || testno.equals("") ? "${TEST NUMBER}" : testno;
		return _pkg + "/" + _clz + "Test_" + _testno + ".java";
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
