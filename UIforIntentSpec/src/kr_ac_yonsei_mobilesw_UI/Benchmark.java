package kr_ac_yonsei_mobilesw_UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.DebugGraphics;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import kr_ac_yonsei_mobilesw_shell.ExecuteShellCommand;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.table.TableModel;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JSplitPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Benchmark extends JFrame {

	private static final long serialVersionUID = -8114454317556683079L;
	
	private static final Logger logger = Logger.getLogger(Benchmark.class.getName());
    private FileHandler fileHandler;
	
	private boolean isBusy = false;
	private char logLevel = 'V';
	private char nowLogLevel = 'V';
	public final ReentrantLock LogcatLock = new ReentrantLock();
	public int logcatReadStartIndex = 0;
	public String logcatFilter = "";
	public String NowTxtFilter = "";
	private int logcatMaximumCount = 5000;
	private boolean scrollbarLockBottom = false;
	
	private JPanel contentPane;
	private JButton btnStart;
	JTextField txtAdbCommand;
	JTextArea txtAdbCommandLog;
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneLogcat;
	private JTable tblLogcat;
	private DefaultTableModel modelLogcat;
	DefaultTableModel modelLogcatView;
	private DefaultTableModel modelLogcatFilter;
	DefaultTableModel modelAdbCommand;
	JTextField txtFilter;
	private JTextField txtAdbPath;
	private JComboBox cboDeviceID;
	private JTable tblAdbCommand;
	JTextArea txtBenchResult;
	private JButton btnExecwithfilter;
	public boolean benchStartProcessingFlag = false;
	public boolean benchStartProcessingDoneFlag = false;
	public boolean showLogCatProcessingFlag = false;  
	public JButton btnBenchStart = null;
	
	private JFileChooser fc = new JFileChooser();
	
	HashMap<String, String> mapPidToApplicationName = new HashMap<String, String>();
	
	private static String adbPathString; 
	
	static {
		adbPathString = Config.getAdbPath();
		
		if (adbPathString == null) {
			String osName = System.getProperty("os.name");
			String osNameMatch = osName.toLowerCase();
			if(osNameMatch.contains("linux")) {
				adbPathString = "/home/";
			} else if(osNameMatch.contains("windows")) {
				adbPathString = "C:/users/";
			} else if(osNameMatch.contains("mac os") || osNameMatch.contains("macos") || osNameMatch.contains("darwin")) {
				adbPathString = "/Users/";
			} else {
				adbPathString = "C:/users/"; // Windows OS by default
			}
		}
	}

	/**
	 * Launch the application.
	 * 
	 * 
	 * args : [ -EXIT_ON_CLOSE ]  : An option to turn on EXIT_ON_CLOSE
	 *        adb shell ...
	 *        adb shell ...
	 *        ...
	 */
	
	public static void main(String[] args) {

		try {
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    } catch(Exception e) {
	        System.out.println("Error setting native LAF: " + e);
	    }

		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Benchmark frame = new Benchmark(args);
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
	public Benchmark(String[] args) {
		//addFileHandler(logger);
		
		if (args.length >= 1 && "-EXIT_ON_CLOSE".equals(args[0])) {
			String[] new_args = new String[args.length-1];
			for (int i=0; i<args.length-1; i++) {
				new_args[i] = args[i+1];
			}
			
			args = new_args;
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		
		setTitle("ADB Command");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1259, 750);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JButton btnViewlogcat = new JButton("ViewLogcat");
		btnViewlogcat.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(showLogCatProcessingFlag == false)
				{
					String command = "adb -s 5888e6a4 logcat -v threadtime *:V";		//Don't have command
					
					if(cboDeviceID.getSelectedItem() == null)
					{
						command = "adb logcat -v threadtime *:V";
					}
					else
					{
						String deviceID = cboDeviceID.getSelectedItem().toString().substring(cboDeviceID.getSelectedItem().toString().indexOf(":") + 1, cboDeviceID.getSelectedItem().toString().length());
						command = "adb -s " + deviceID + " logcat -v threadtime *:V";
						
						logger.info("btnViewlogcat => DevicesID : " + deviceID + ", command : " + command);
					}
					
					if(txtAdbPath.getText().trim().equals(""))
					{
						command = adbPathString + command;
					}
					else
					{
						command = txtAdbPath.getText().trim() + command;
					}
					
					ExecuteShellCommand.showLogcat(Benchmark.this, command);
					
					showLogCatProcessingFlag = true;
					btnViewlogcat.setText("Showing...");
				}
			}
		});
		btnViewlogcat.setBounds(1018, 155, 99, 30);
		contentPane.add(btnViewlogcat);
		
		btnStart = new JButton("exec");
		btnStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				exec();
			}
		});
		btnStart.setBounds(1073, 110, 79, 30);
		contentPane.add(btnStart);
		
		txtAdbCommand = new JTextField();
		txtAdbCommand.setBounds(241, 112, 831, 28);
		contentPane.add(txtAdbCommand);
		txtAdbCommand.setColumns(10);
		
		scrollPane = new JScrollPane();
		scrollPane.setBounds(241, 10, 990, 92);
		contentPane.add(scrollPane);
		
		txtAdbCommandLog = new JTextArea();
		txtAdbCommandLog.setFont(UIManager.getFont("TextField.font"));
		scrollPane.setViewportView(txtAdbCommandLog);
		
		scrollPaneLogcat = new JScrollPane();
		scrollPaneLogcat.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPaneLogcat.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneLogcat.setBounds(241, 217, 990, 485);
		
		scrollPaneLogcat.getViewport().setBackground(Color.white);
		
		contentPane.add(scrollPaneLogcat);
		
		modelLogcat = new DefaultTableModel();
		modelLogcat.addColumn("Level");
		modelLogcat.addColumn("Time");
		modelLogcat.addColumn("PID");
		modelLogcat.addColumn("TID");
		modelLogcat.addColumn("Application");
		modelLogcat.addColumn("Tag");
		modelLogcat.addColumn("Text");
		modelLogcat.addColumn("RawMessage");
		
		modelLogcatView = new DefaultTableModel();
		modelLogcatView.addColumn("Level");
		modelLogcatView.addColumn("Time");
		modelLogcatView.addColumn("PID");
		modelLogcatView.addColumn("TID");
		modelLogcatView.addColumn("Application");
		modelLogcatView.addColumn("Tag");
		modelLogcatView.addColumn("Text");
		modelLogcatView.addColumn("RawMessage");
		
		tblLogcat = new JTable(modelLogcatView){
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                JComponent jc = (JComponent) comp;
                
                if (getModel().getValueAt(row, 0) != null) 
                {
	                if(getModel().getValueAt(row, 0).toString().equals("V"))
	                {
	                	comp.setForeground(Color.black);
	                }	
	                else if(getModel().getValueAt(row, 0).toString().equals("D"))
	                {
	                	comp.setForeground(new Color(0, 0, 127));
	                }
	                else if(getModel().getValueAt(row, 0).toString().equals("I"))
	                {
	                	comp.setForeground(new Color(0, 127, 0));
	                }
	                else if(getModel().getValueAt(row, 0).toString().equals("W"))
	                {
	                	comp.setForeground(new Color(255, 127, 0));
	                }
	                else if(getModel().getValueAt(row, 0).toString().equals("E"))
	                {
	                	comp.setForeground(new Color(255, 0, 0));
	                }
	                else
	                {
	                	comp.setForeground(Color.black);
	                }
                }
                return comp;
            }
        };
		
		tblLogcat.setShowHorizontalLines(false);
		tblLogcat.setShowVerticalLines(false);
		scrollPaneLogcat.setViewportView(tblLogcat);
		
		tblLogcat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		tblLogcat.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblLogcat.getColumnModel().getColumn(1).setPreferredWidth(150);
		tblLogcat.getColumnModel().getColumn(2).setPreferredWidth(50);
		tblLogcat.getColumnModel().getColumn(3).setPreferredWidth(50);
		tblLogcat.getColumnModel().getColumn(4).setPreferredWidth(150);
		tblLogcat.getColumnModel().getColumn(5).setPreferredWidth(150);
		tblLogcat.getColumnModel().getColumn(6).setPreferredWidth(1000);
		tblLogcat.getColumnModel().getColumn(7).setPreferredWidth(1000);
		
		tblLogcat.setRowHeight(23);
		
		tblLogcat.setFont(new Font("Courier New", Font.PLAIN, 12));
		tblLogcat.setSelectionBackground(new Color(222, 237, 255));
		
		
		modelLogcatFilter = new DefaultTableModel();
		modelLogcatFilter.addColumn("Filter Name");
		modelLogcatFilter.addColumn("by Log Tag");
		modelLogcatFilter.addColumn("by Log Message");
		modelLogcatFilter.addColumn("by PID");
		modelLogcatFilter.addColumn("by Application Type");
		modelLogcatFilter.addColumn("by Log Level");
		
		txtFilter = new JTextField();
		txtFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				
				filterEvent();
				
			}
		});
		
		txtFilter.setBounds(241, 157, 660, 28);
		contentPane.add(txtFilter);
		
		JButton btnClear = new JButton("Clear");
		btnClear.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				LogcatClear();
			}
		});
		btnClear.setBounds(1120, 155, 62, 30);
		contentPane.add(btnClear);
		
		JButton btnLogcatScrollDown = new JButton("\u25BD");
		btnLogcatScrollDown.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				
				LogcatLock.lock();
				
				if(scrollbarLockBottom)
				{
					scrollbarLockBottom = false;
					btnLogcatScrollDown.setText("\u25BD");
				}
				else
				{
					scrollbarLockBottom = true;
					btnLogcatScrollDown.setText("\u25BC");
				}
				
				showLogcat();
				
				LogcatLock.unlock();
			}
		});
		btnLogcatScrollDown.setBounds(1184, 155, 47, 30);
		contentPane.add(btnLogcatScrollDown);
		
		JComboBox cboLogLevel = new JComboBox();
		cboLogLevel.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent event) {
				if (event.getStateChange() == ItemEvent.SELECTED) {
					
					LogcatLock.lock();
					
					String itemName = event.getItem().toString();
					
					if (itemName.equals("verbose")) {
						nowLogLevel = 'V';
					} else if (itemName.equals("debug")) {
						nowLogLevel = 'D';
					} else if (itemName.equals("info")) {
						nowLogLevel = 'I';
					} else if (itemName.equals("warn")) {
						nowLogLevel = 'W';
					} else if (itemName.equals("error")) {
						nowLogLevel = 'E';
					}
					
					showLogcat();
					
					LogcatLock.unlock();
				}
			}
		});
		
		cboLogLevel.setModel(new DefaultComboBoxModel(new String[] {"verbose", "debug", "info", "warn", "error"}));
		cboLogLevel.setBounds(913, 155, 99, 30);
		contentPane.add(cboLogLevel);
		
		txtAdbPath = new JTextField();
		txtAdbPath.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				
				String adbPath = txtAdbPath.getText();
				
				txtAdbPath.setText(txtAdbPath.getText().replaceAll("\\\\", "/"));
			}
		});
		txtAdbPath.setText(adbPathString);
		txtAdbPath.setBounds(12, 35, 214, 28);
		contentPane.add(txtAdbPath);
		txtAdbPath.setColumns(10);
		
		JLabel lblAdbPath = new JLabel("adb path : ");
		lblAdbPath.setBounds(12, 10, 67, 15);
		contentPane.add(lblAdbPath);
		
		cboDeviceID = new JComboBox();
		cboDeviceID.setBounds(12, 115, 214, 30);
		contentPane.add(cboDeviceID);
		
		JLabel lblDeviceId = new JLabel("Device ID : ");
		lblDeviceId.setBounds(12, 79, 67, 15);
		contentPane.add(lblDeviceId);
		
		JButton btnReadDevice = new JButton("ReadDevice");
		btnReadDevice.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {

				String command = "adb devices -l";
				
				if(getisBusy() == true)										//Already processing
				{
					JOptionPane.showMessageDialog (null, "Now Processing...");
					return;
				}
				
				setisBusy(true);

				cboDeviceID.removeAllItems();

				if(txtAdbPath.getText().trim().equals(""))
				{
					command = adbPathString + command;
				}
				else
				{
					command = txtAdbPath.getText().trim() + command;
				}
				
				ExecuteShellCommand.readDevice(Benchmark.this, command);

				setisBusy(false);
				
			}
		});
		btnReadDevice.setBounds(130, 73, 99, 30);
		contentPane.add(btnReadDevice);
		
		JScrollPane scrollPaneBench = new JScrollPane();
		scrollPaneBench.setBounds(12, 217, 217, 244);
		scrollPaneBench.getViewport().setBackground(Color.white);
		contentPane.add(scrollPaneBench);
		
		modelAdbCommand = new DefaultTableModel();
		modelAdbCommand.addColumn("Seq");
		modelAdbCommand.addColumn("Command");
		modelAdbCommand.addColumn("Result");
		
		for(int i=0; i<args.length; i++) {
			modelAdbCommand.addRow(new Object[] { String.valueOf(i+1), args[i], ""});
		}
		
		tblAdbCommand = new JTable(modelAdbCommand){
            private static final long serialVersionUID = 1L;

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component comp = super.prepareRenderer(renderer, row, column);
                JComponent jc = (JComponent) comp;
                
                String result = getModel().getValueAt(row, 2).toString();
                
                if(result.equals("Normal"))
                {
                	comp.setForeground(new Color(0, 0, 127));
                }
                else if(result.equals("Exit"))
                {
                	comp.setForeground(new Color(0, 127, 0));
                }
                else if(result.equals("ErrorExit"))
                {
                	comp.setForeground(new Color(255, 0, 0));
                }
                else if(result.equals("IntentSpecCatch"))
                {
                	comp.setForeground(new Color(255, 127, 0));
                }	
                else if(result.equals("IntentSpecPassAndNormal"))
                {
                	comp.setForeground(new Color(0, 0, 127));
                }	
                else if(result.equals("IntentSpecPassAndExit"))
                {
                	comp.setForeground(new Color(0, 127, 0));
                }	
                else if(result.equals("IntentSpecPassAndErrorExit"))
                {
                	comp.setForeground(new Color(255, 0, 0));
                }	
                else
                {
                	comp.setForeground(Color.black);
                }
                
                return comp;
            }
        };
		tblAdbCommand.setShowVerticalLines(false);
		tblAdbCommand.setShowHorizontalLines(false);
		tblAdbCommand.setSelectionBackground(new Color(222, 237, 255));
		tblAdbCommand.setRowHeight(23);
		tblAdbCommand.setFont(new Font("Courier New", Font.PLAIN, 12));
		tblAdbCommand.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPaneBench.setViewportView(tblAdbCommand);
		
		tblAdbCommand.getColumnModel().getColumn(0).setPreferredWidth(50);
		tblAdbCommand.getColumnModel().getColumn(1).setPreferredWidth(2000);
		tblAdbCommand.getColumnModel().getColumn(2).setPreferredWidth(150);
		
		btnBenchStart = new JButton("Run");
		btnBenchStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnBenchStart.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				if(benchStartProcessingFlag == false)
				{
					if(benchStartProcessingDoneFlag == false)
					{
						btnBenchStart.setText("Stop");
						benchStartProcessingFlag = true;
						benchStartProcessingDoneFlag = true;
						benchStart();
					}
				}
				else
				{
					if(benchStartProcessingDoneFlag == true)
					{
						btnBenchStart.setText("Stop...");
						benchStartProcessingFlag = false;
					}
				}
					
				
			}
		});
		btnBenchStart.setBounds(12, 155, 67, 30);
		contentPane.add(btnBenchStart);
		
		JButton btnAdbCommandClr = new JButton("Clear");
		btnAdbCommandClr.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				modelAdbCommand.setRowCount(0);
			}
		});
		btnAdbCommandClr.setBounds(161, 155, 67, 30);
		contentPane.add(btnAdbCommandClr);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(12, 484, 217, 218);
		contentPane.add(scrollPane_1);
		
		txtBenchResult = new JTextArea();
		scrollPane_1.setViewportView(txtBenchResult);
		txtBenchResult.setFont(UIManager.getFont("TextField.font"));
		
		btnExecwithfilter = new JButton("exFilter");
		btnExecwithfilter.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent arg0) {
				execWithFilter();
			}
		});
		btnExecwithfilter.setBounds(1152, 110, 79, 30);
		contentPane.add(btnExecwithfilter);
		
		// fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		// fc.setAcceptAllFileFilterUsed(false);
		
		JButton btnFind = new JButton("Find ADB");
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String adbpath = txtAdbPath.getText();
				if (adbpath != null || "".equals(adbpath)) {
					fc.setCurrentDirectory(new File(adbpath));
				}
				
				int returnVal = fc.showOpenDialog(Benchmark.this);
				
				File file;
				
		        if (returnVal != JFileChooser.APPROVE_OPTION) {
		        	return;
		        }
		        
		        file = fc.getSelectedFile();
			
		        String fullname = file.getAbsolutePath();
		        String filename = file.getName();
		        String adbPath = fullname.substring(0, fullname.length()-filename.length());
		        txtAdbPath.setText(adbPath);
		        
		        Config.putAdbPath(adbPath);
			}
		});
		btnFind.setBounds(130, 9, 97, 23);
		contentPane.add(btnFind);
		
		JLabel lblNewLabel = new JLabel("Pass / Fail");
		lblNewLabel.setFont(new Font("Arial", Font.PLAIN, 12));
		lblNewLabel.setBounds(12, 461, 203, 23);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("ADB Commands");
		lblNewLabel_1.setFont(new Font("Arial", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(12, 190, 217, 23);
		contentPane.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Android Logs");
		lblNewLabel_2.setFont(new Font("Arial", Font.PLAIN, 12));
		lblNewLabel_2.setBounds(241, 190, 122, 20);
		contentPane.add(lblNewLabel_2);
	}

	public int getLocationX()
	{
		return getLocation().x;
	}
	
	public int getLocationY()
	{
		return getLocation().y;
	}
	
	public void appendTxt_adbCommandLog(String str)
	{
		txtAdbCommandLog.append(str);
		txtAdbCommandLog.setCaretPosition(txtAdbCommandLog.getCaretPosition() + str.length());
	}
	
	public void appendTxt_logcat(String str)
	{
		if(str.equals("--------- beginning of /dev/log/system") || str.equals("--------- beginning of /dev/log/main"))
		{
			return;
		}
		
		Object[] newRow = parseLog(str);
		
		LogcatLock.lock();
		
		if(newRow != null)
		{
			modelLogcat.addRow(newRow);
			
			if(modelLogcat.getRowCount() > logcatMaximumCount)
			{
				modelLogcat.removeRow(0);
				logcatReadStartIndex--;
			}
		}
		
		//logger.info("modelLogcat => modelLogcat.getRowCount() : " + modelLogcat.getRowCount() + ", logcatMaximumCount : " + logcatMaximumCount + ", logcatReadStartIndex : " + logcatReadStartIndex);
		
		LogcatLock.unlock();
	}
	
	public Object[] parseLog(String str)
	{
		Object[] row = null;
		
		
		//mapPidToApplicationName
		try
		{
			if(str.indexOf("Start proc ") != -1)
			{
				int idx = str.indexOf("Start proc ");
				
				if(str.indexOf("pid=") != -1)
				{
					String pid = str.substring(str.indexOf("pid=") + 4, str.indexOf(" ", str.indexOf("pid=") + 4));
					String applicationName = str.substring(str.indexOf("Start proc ") + 11, str.indexOf(" ", str.indexOf("Start proc ") + 11));
					
					String orgApplicationName = mapPidToApplicationName.get(pid);
					if(orgApplicationName == null)
					{
						mapPidToApplicationName.put(pid, applicationName);						
					}
					else
					{
						mapPidToApplicationName.remove(pid);
						mapPidToApplicationName.put(pid, applicationName);	
					}
				}
			}
			
			if(str.length() > 33)
			{
				String level = str.substring(31, 32);
				String time = str.substring(0, 18);
				String pid = str.substring(19, 24).trim();
				String tid = str.substring(25, 30).trim();
				int endOfTag = str.indexOf(':', 33);
				String tag = str.substring(33, endOfTag);
				String text = str.substring(endOfTag + 2, str.length());
				
				String ApplicationName = mapPidToApplicationName.get(pid);
				if(ApplicationName != null)
				{
					str = str + " id:" + ApplicationName + " ";
				}
				int startOfId = str.indexOf(" id:");
				String application = "";
				if(startOfId != -1)
				{
					int endOfId = str.indexOf(' ', startOfId + 5);
					if(endOfId == -1)
					{
						endOfId = str.length();
					}
					//logger.info("btnViewlogcat => parseLog - endOfId : " + endOfId);
					application = str.substring(startOfId + 4, endOfId).trim();
				}
				
				row = new Object[]{level, time, pid, tid, application, tag, text, str};
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return row;
	}
	
	public void setisBusy(boolean bool)
	{
		isBusy = bool;
	}
	
	public boolean getisBusy()
	{
		return isBusy;
	}
	
	public void setLogLevel(char ch)
	{
		logLevel = ch;
	}
	
	public char getLogLevel()
	{
		return logLevel;
	}
	
	public void showLogcat()
	{
		LogcatLock.lock();
		
		if(NowTxtFilter.equals("") == false || nowLogLevel != 'V')
		{
			if(logcatFilter.equals(NowTxtFilter) == false || logLevel != nowLogLevel)
			{
				logLevel = nowLogLevel;
				logcatFilter = NowTxtFilter;
				modelLogcatView.setRowCount(0);
				
				for(int i = 0; i < modelLogcat.getRowCount(); i++)
				{
					char level = modelLogcat.getValueAt(i, 0).toString().charAt(0);
					
					if(modelLogcat.getValueAt(i, 7).toString().contains(logcatFilter))
					{
						if(logLevel == 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'D'
								&& level != 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'I'
								&& level != 'V'
								&& level != 'D')
						{
							addRow(i);
						}
						else if(logLevel == 'W'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I')
						{
							addRow(i);
						}
						else if(logLevel == 'E'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I'
								&& level != 'W')
						{
							addRow(i);
						}
						
					}
				}
			}
			else
			{
				for(int i = logcatReadStartIndex; i < modelLogcat.getRowCount(); i++)
				{
					
					char level = modelLogcat.getValueAt(i, 0).toString().charAt(0);
					
					if(modelLogcat.getValueAt(i, 7).toString().contains(logcatFilter))
					{
						if(logLevel == 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'D'
								&& level != 'V')
						{
							addRow(i);
						}
						else if(logLevel == 'I'
								&& level != 'V'
								&& level != 'D')
						{
							addRow(i);
						}
						else if(logLevel == 'W'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I')
						{
							addRow(i);
						}
						else if(logLevel == 'E'
								&& level != 'V'
								&& level != 'D'
								&& level != 'I'
								&& level != 'W')
						{
							addRow(i);
						}
					}
				}
			}
		}
		else
		{
			if(logcatFilter.equals(NowTxtFilter) == false || logLevel != nowLogLevel)
			{
				logLevel = nowLogLevel;
				logcatFilter = NowTxtFilter;
				modelLogcatView.setRowCount(0);
				
				for(int i = 0; i < modelLogcat.getRowCount(); i++)
				{
					addRow(i);
				}
			}
			else
			{
				for(int i = logcatReadStartIndex; i < modelLogcat.getRowCount(); i++)
				{
					addRow(i);
				}
			}
		}
		
		logcatReadStartIndex = modelLogcat.getRowCount();
		
		if(modelLogcatView.getRowCount() > logcatMaximumCount)
		{
			modelLogcatView.removeRow(0);
		}
		
		if(scrollbarLockBottom == true)
		{
			JScrollBar vertical = scrollPaneLogcat.getVerticalScrollBar();
			scrollPaneLogcat.getViewport().setViewPosition(new Point(0, vertical.getMaximum()));
		}
		
		//logger.info("modelLogcatView => modelLogcatView.getRowCount() : " + modelLogcatView.getRowCount() + ", logcatMaximumCount : " + logcatMaximumCount + ", logcatReadStartIndex : " + logcatReadStartIndex);
		
		LogcatLock.unlock();
	}
	
	private void addRow(int row)
	{
		Object[] newRow = new Object[]{modelLogcat.getValueAt(row, 0), modelLogcat.getValueAt(row, 1), modelLogcat.getValueAt(row, 2), modelLogcat.getValueAt(row, 3),
				modelLogcat.getValueAt(row, 4), modelLogcat.getValueAt(row, 5), modelLogcat.getValueAt(row, 6), modelLogcat.getValueAt(row, 7)};
		
		modelLogcatView.addRow(newRow);
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
    
    public void showDeviceList(String deviceText)
    {
    	if(deviceText.contains("List of devices attached")){
    		return;
    	}
    	
    	String DevicesID = deviceText.substring(0, deviceText.indexOf(" "));
    	String model = deviceText.substring(deviceText.indexOf("model:") + 6, deviceText.indexOf(" ", deviceText.indexOf("model:") + 5)); 
    	
    	//logger.info("showDeviceList => DevicesID : " + DevicesID + ", model : "  + model + ", raw : " + deviceText);
    	
    	cboDeviceID.addItem(model + ":" + DevicesID);
    }
    
    public DefaultTableModel getModelAdbCommand()
    {
    	return modelAdbCommand;
    }
    
    public void LogcatClear()
    {
    	LogcatLock.lock();
    	
    	modelLogcat.setRowCount(0);
    	modelLogcatView.setRowCount(0);
    	logcatReadStartIndex = 0;
    	
    	LogcatLock.unlock();
    }
    
    public void benchmarkRunButtonFix()
    {
    	benchStartProcessingDoneFlag = false;

    	btnBenchStart.setText("Run");
    }
    
    public void filterEvent()
    {
    	LogcatLock.lock();
		
		NowTxtFilter = txtFilter.getText().trim();
		showLogcat();
		
		LogcatLock.unlock();
    }
    
    public void benchStart()
    {
    	if(modelAdbCommand.getRowCount() < 1)
    	{
    		return;
    	}
    	
    	BenchStart benchStart = new BenchStart();
    	benchStart.start(Benchmark.this);
		
    }
    
    public void execWithFilter()
    {
    	String Command = txtAdbCommand.getText().trim();
    	
    	if(Command.equals("") == true)
		{
			return;
		}
    	
    	String packageName = Command.substring(Command.indexOf("-n ") + 3, Command.indexOf('/', Command.indexOf("-n ") + 3));
		
		txtFilter.setText(packageName);
		
		LogcatClear();
		filterEvent();
		
		exec();
    }
    
    public void exec()
    {
		String command = txtAdbCommand.getText().trim();		//Don't have command
		if(command.equals("") == true)
		{
			return;
		}
		
		if(getisBusy() == true)										//Already processing
		{
			JOptionPane.showMessageDialog (null, "Now Processing...");
			return;
		}
		
		setisBusy(true);

		command = adjustAdbCommand(command);

		//adb shell am start -n com.enterpriseandroid.androidSecurity/.MainActivity -a android.intent.action.ERROR
		ExecuteShellCommand.executeCommand(Benchmark.this, command);
		
		setisBusy(false);
    }
    
    public void exec(String command)
    {
		if(command.equals("") == true)
		{
			return;
		}
		
		if(getisBusy() == true)										//Already processing
		{
			JOptionPane.showMessageDialog (null, "Now Processing...");
			return;
		}
		
		setisBusy(true);

		command = adjustAdbCommand(command);

		ExecuteShellCommand.executeCommand(Benchmark.this, command);
		
		setisBusy(false);
    }
    
    public String adjustAdbCommand(String command)
    {
		if(command.substring(0, 3).equals("adb"))
		{
			
			if(cboDeviceID.getSelectedItem() == null)
			{
				//none
			}
			else
			{
				String deviceID = cboDeviceID.getSelectedItem().toString().substring(cboDeviceID.getSelectedItem().toString().indexOf(":") + 1, cboDeviceID.getSelectedItem().toString().length());
				command = command.replace("adb shell am", "adb -s " + deviceID + " shell am");
				
				logger.info("btnStart => DevicesID : " + deviceID + ", command : " + command);
			}
			
			if(txtAdbPath.getText().trim().equals(""))
			{
				command = adbPathString + command;
			}
			else
			{
				command = txtAdbPath.getText().trim() + command;
			}
		}
		
		return command;
    }
}
