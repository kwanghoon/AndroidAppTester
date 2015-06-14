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

/*
 *  TODO: 
 *  1. GenItentSpecFromAPK에서 service와 receiver에 대한 인텐트 스펙 생성이 잘되는지 확인 필요 
 *  2. ADB 명령어를 만들어 처음 테스트할 때 항상 중간에 멈추는 문제 발생
 *  3. ADB 명령어로 실행하는 중에 멈추는 기능 추가
 *  4. ADB 명령어를 실행하는 중에 멈추기 전까지 다른 버튼이 동작하지 않도록
 */
public class Main extends JFrame implements InterfaceWithExecution {

	private JPanel contentPane;
	private static Benchmark benchmarkUI;
	private JTextArea txtIntentSpec;
	
	private Random rand = new Random(System.currentTimeMillis());
	
	private JButton btnImportFromApk;
	private final static String labelBtnImport = "Import";
	private final static String labelBtnImporting = "Importing ...";
	
	private JFileChooser fc = new JFileChooser();
	
	private JFileChooser fcOutput = new JFileChooser();
	
	private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
	private FileHandler fileHandler;

	private static String genCommand;
	
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
					Main frame = new Main(benchmarkUI);
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
	public Main(Benchmark mUI) {
		addFileHandler(logger);
		
		setTitle("Android Testing Framework for The Vulnerability of Android Components");
		
		this.benchmarkUI = mUI;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 1259, 750);
		setBounds(100, 100, 851, 622);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnMakeAndroidTestCode = new JButton("Android Test Code");
		btnMakeAndroidTestCode.setFont(new Font("Arial", Font.PLAIN, 12));
		btnMakeAndroidTestCode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnMakeAndroidTestCode.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				String command = "java -cp " 
							+ System.getProperty("user.dir") + "/bin;" 
							+ System.getProperty("user.dir") + "/lib/* "
							+ " kr_ac_yonsei_mobilesw_UI.GenAndroidTestCodeUI "; 
				
				
				String intentSepc = txtIntentSpec.getText();
				if (intentSepc == null || "".equals(intentSepc)) {
					return;
				}
				
				if(intentSepc.charAt(0) != '"')
				{
					intentSepc = "\"" + intentSepc;
				}
				if(intentSepc.charAt(intentSepc.length() - 1) != '"')
				{
					intentSepc = intentSepc + "\""; 
				}
				
				command = command + intentSepc + " " ;
				
				System.out.println("RUN: " + command);
				
				// ExecuteShellCommand.executeMakeTestArtifacts(Main.this, command);
				GenAndroidTestCodeUI.main(new String[] { 
						// cboComponent.getSelectedIndex() + "", 
						txtIntentSpec.getText()
						}
				);
			}
		});
		btnMakeAndroidTestCode.setBounds(678, 544, 145, 30);
		contentPane.add(btnMakeAndroidTestCode);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 46, 811, 488);
		scrollPane_1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane_1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane_1);
		
		txtIntentSpec = new JTextArea();
		txtIntentSpec.setFont(new Font("Monospaced", Font.PLAIN, 16));
		//txtIntentSpec.setBounds(12, 98, 1219, 97);
		//contentPane.add(txtIntentSpec);
		scrollPane_1.setViewportView(txtIntentSpec);
		
		JLabel lblIntentspec = new JLabel("Write your Intent specification:");
		lblIntentspec.setFont(new Font("Arial", Font.BOLD, 16));
		lblIntentspec.setBounds(12, 10, 272, 20);
		contentPane.add(lblIntentspec);
		
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

				int returnVal = fc.showOpenDialog(Main.this);
				
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
				ExecuteShellCommand.executeImportIntentSpecCommand(Main.this, command);
				//com.example.java.GenIntentSpecFromAPK.main(new String[] { 
				//		compTypeOption, 
				//		file.getAbsolutePath()
				//		} );
			}
		});
		btnImportFromApk.setBounds(622, 5, 110, 30);
		contentPane.add(btnImportFromApk);
		
		JButton btnClear = new JButton("Clear");
		btnClear.setFont(new Font("Arial", Font.PLAIN, 12));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtIntentSpec.setText("");
			}
		});
		btnClear.setBounds(744, 5, 79, 30);
		contentPane.add(btnClear);
		
		JButton btnMakeADBCmd = new JButton("ADB Command");
		btnMakeADBCmd.setFont(new Font("Arial", Font.PLAIN, 12));
		btnMakeADBCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = "java -cp " 
						+ System.getProperty("user.dir") + "/bin;" 
						+ System.getProperty("user.dir") + "/lib/* "
						+ " kr_ac_yonsei_mobilesw_UI.BenchAdd "; 
			
			
				String intentSepc = txtIntentSpec.getText();
				if (intentSepc == null || "".equals(intentSepc)) {
					return;
				}
				
				if(intentSepc.charAt(0) != '"')
				{
					intentSepc = "\"" + intentSepc;
				}
				if(intentSepc.charAt(intentSepc.length() - 1) != '"')
				{
					intentSepc = intentSepc + "\""; 
				}
				
				command = command + intentSepc + " " ;
				
				System.out.println("RUN: " + command);
				
				//ExecuteShellCommand.executeMakeTestArtifacts(Main.this, command);
				BenchAdd.main(new String[] { 
						// cboComponent.getSelectedIndex() + "", 
						txtIntentSpec.getText()
						}
				);
			}
		});
		btnMakeADBCmd.setBounds(537, 544, 129, 30);
		contentPane.add(btnMakeADBCmd);
		
		JLabel lblThenChooseYour = new JLabel("Then choose the type of your testing artifacts :");
		lblThenChooseYour.setFont(new Font("Arial", Font.BOLD, 16));
		lblThenChooseYour.setBounds(161, 543, 372, 30);
		contentPane.add(lblThenChooseYour);
		
		fcOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fcOutput.setAcceptAllFileFilterUsed(false);
		importpath = Config.getImportPath();
		if (importpath != null) {
			fcOutput.setCurrentDirectory(new File(importpath));
		}
		
		pkgName = "";
		
		clzName = "";
		
		testNo = "";
		
		testFileName = "";
	}
	
	public void setTestFileName(String pkg, String clz, String testno) {
	}
	
	public String getTestFileName(String pkg, String clz, String testno) {
		String _pkg = pkg==null || pkg.equals("") ? "${PACKAGE NAME}" : pkg.replaceAll("\\.", "/");
		String _clz = clz==null || clz.equals("") ? "${CLASS NAME}" : clz;
		String _testno = testno==null || testno.equals("") ? "${TEST NUMBER}" : testno;
		return _pkg + "/" + _clz + "Test_" + _testno + ".java";
	}
	
	public void appendTxt_testArtifacts(String str)
	{
		System.out.println(str);
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
