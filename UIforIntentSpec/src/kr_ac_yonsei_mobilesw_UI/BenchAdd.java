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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JSplitPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;

import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class BenchAdd extends JFrame implements InterfaceWithExecution {
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
	
	private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
	private FileHandler fileHandler;

	private static String genCommand;
	
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
					BenchAdd frame = new BenchAdd(benchmarkUI, args);
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
	public BenchAdd(Benchmark mUI, String[] args) {
		//addFileHandler(logger);
		
		setTitle("Adb Command Generation");
		
		this.benchmarkUI = mUI;
		
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 1259, 750);
		setBounds(100, 100, 970, 750);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(12, 417, 926, 245);
		contentPane.add(scrollPane);
		
		txtAdbCommand = new JTextArea();
		txtAdbCommand.setFont(new Font("Courier New", Font.PLAIN, 16));
		scrollPane.setViewportView(txtAdbCommand);
		
		JButton btnOk = new JButton("Run");
		btnOk.setFont(new Font("Arial", Font.PLAIN, 12));
		btnOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				AddAdbCommand();
			}
		});
		btnOk.setBounds(728, 672, 99, 30);
		contentPane.add(btnOk);
		
		JButton btnCancel = new JButton("Go back");
		btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
		btnCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Close();
			}
		});
		btnCancel.setBounds(839, 672, 99, 30);
		contentPane.add(btnCancel);
		
//		try {
//			if (args.length >= 1) {
//				int typeofcomponent = Integer.parseInt(args[0]);
//				cboComponent.setSelectedIndex(typeofcomponent);
//			}
//		} catch(NumberFormatException e) {
//		}
		
		JButton btnMake = new JButton("Generate ADB Commands");
		btnMake.setFont(new Font("Arial", Font.PLAIN, 12));
		btnMake.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				String command = System.getProperty("user.dir") 
						+ "/../GenTestsfromIntentSpec/bin/" + genCommand + " AdbCommand "; 
				
				long count = Calendar.getInstance().getTimeInMillis();
				
				String intentSpec = txtIntentSpec.getText();
//				if(intentSpec.charAt(0) != '"')
//				{
//					intentSpec = "\"" + intentSpec;
//				}
//				if(intentSpec.charAt(intentSpec.length() - 1) != '"')
//				{
//					intentSpec = intentSpec + "\""; 
//				}
				
				String path = JavaCommand.buildIntentSpecParam("param"+count+".is", intentSpec);
				
				intentSpec = " -ftmp " + path;
				
				command = command + cboMakeMode.getSelectedIndex() + " " +
								    txtCount.getText() + " " +
								    intentSpec + " ";
				
				System.out.println("RUN: " + command);
				
				
//				try {
//					Runtime.getRuntime().exec(command);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
				
				ExecuteShellCommand.executeMakeTestArtifacts(BenchAdd.this, command);
			}
		});
		btnMake.setBounds(346, 31, 181, 30);
		contentPane.add(btnMake);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 107, 926, 264);
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
		
		txtCount = new JTextField("3");
		txtCount.setBounds(158, 33, 176, 28);
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
		lblIntentspec.setBounds(12, 77, 134, 20);
		contentPane.add(lblIntentspec);
		
		chkExtraValueReplace = new JCheckBox("ExtraValueReplace");
		chkExtraValueReplace.setFont(new Font("Arial", Font.PLAIN, 12));
		chkExtraValueReplace.setSelected(true);
		chkExtraValueReplace.setBounds(556, 676, 134, 23);
		contentPane.add(chkExtraValueReplace);
		
		JLabel lblAdbCommands = new JLabel("ADB Commands : ");
		lblAdbCommands.setFont(new Font("Arial", Font.PLAIN, 12));
		lblAdbCommands.setBounds(12, 388, 106, 15);
		contentPane.add(lblAdbCommands);
		
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

				int returnVal = fc.showOpenDialog(BenchAdd.this);
				
				File file;
				
		        if (returnVal != JFileChooser.APPROVE_OPTION) {
		        	return;
		        }
		        
		        file = fc.getSelectedFile();
		        
		        Config.putImportPath(fc.getCurrentDirectory().getAbsolutePath());				
				
				String command = JavaCommand.javaCmd() 
									+ "com.example.java.GenIntentSpecFromAPK " 
									+ "\"" + file.getAbsolutePath() + "\""; // ' ' in the file name
				
				System.out.println("RUN: " + command);
				
				btnImportFromApk.setText(labelBtnImporting);
				ExecuteShellCommand.executeImportIntentSpecCommand(BenchAdd.this, command);
			}
		});
		btnImportFromApk.setBounds(141, 71, 99, 30);
		contentPane.add(btnImportFromApk);
		
		JButton btnClear = new JButton("Clear");
		btnClear.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtIntentSpec.setText("");
			}
		});
		btnClear.setBounds(252, 71, 97, 30);
		contentPane.add(btnClear);
		
		JButton btnClear_1 = new JButton("Clear");
		btnClear_1.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClear_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtAdbCommand.setText("");
			}
		});
		btnClear_1.setBounds(130, 381, 97, 30);
		contentPane.add(btnClear_1);
		
		
	}
	
	public void appendTxt_testArtifacts(String str)
	{
		txtAdbCommand.append(str);
		txtAdbCommand.setCaretPosition(txtAdbCommand.getCaretPosition() + str.length());
	}
	
	public void done_testArtifacts(boolean fail) {
		System.out.println("done. " + fail);
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

		if (adbCommand.length > 0)
			Benchmark.main(adbCommand);
		
		//DefaultTableModel modelAdbCommand = this.benchmarkUI.getModelAdbCommand();
		
		//for(int i = 0; i < adbCommand.length; i++)
		//{
			//modelAdbCommand.addRow(makeRow(i, adbCommand[i]));			
		//}
		
		//Close();
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
	
	/*
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
    */
    
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
