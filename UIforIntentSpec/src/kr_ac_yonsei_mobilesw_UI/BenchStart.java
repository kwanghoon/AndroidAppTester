package kr_ac_yonsei_mobilesw_UI;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.*;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellBorder.BorderSide;

//import jxl.*;
//import jxl.format.BoldStyle;
//import jxl.format.Border;
//import jxl.format.BorderLineStyle;
//import jxl.format.Colour;
//import jxl.format.Pattern;
//import jxl.format.UnderlineStyle;
//import jxl.write.Font;
//import jxl.write.Label;
//import jxl.write.WritableCellFormat;
//import jxl.write.WritableFont;
//import jxl.write.WritableSheet;
//import jxl.write.WritableWorkbook;
//import jxl.write.WriteException;

public class BenchStart {

	//WritableWorkbook workbook; // XSSFWorkbook
	SXSSFWorkbook workbook;
	//WritableSheet sheet;	// XSSFSheet
	SXSSFSheet sheet;

	boolean flagUseIntentAssertion = false; // IntentAssert 
	public boolean benchStartProcessingFlag = false;
	private String log_file_name;

	public void setLogFileName(String log_file_name) {
		this.log_file_name = log_file_name;
	}
	
	public Thread start(final Benchbase base) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy_MM_dd_HH_mm_ss", Locale.KOREA );
				Date currentTime = new Date( );
				String dTime = formatter.format ( currentTime );
				String fileName = MainByCommand.getPkgName() + "LOG_" + dTime + ".xlsx";
				System.out.println("Test start");			
				makeExcel();
				makeCellStyle();
				int Normal = 0;
				int Exit = 0;
				int ErrorExit = 0;
				int IntentSpecCatchAndNormal = 0;
				int IntentSpecCatchAndExit = 0;
				int IntentSpecCatchAndErrorExit = 0;
				int IntentSpecPassAndNormal = 0;
				int IntentSpecPassAndExit = 0;
				int IntentSpecPassAndErrorExit = 0;
				int CantAnalyze = 0;
				boolean rebooted = false;
				
				if(base.modelAdbCommand.getRowCount() < 1)
				{
					return;
				}

				for(int i = 0; i < base.modelAdbCommand.getRowCount(); i++)
				{
					if(benchStartProcessingFlag == true)
					{
						
						String adbCommand = base.modelAdbCommand.getValueAt(i, 1).toString();
						String packageName = adbCommand.substring(adbCommand.indexOf("-n ") + 3, adbCommand.indexOf('/', adbCommand.indexOf("-n ") + 3));

						ComponentMode mode;

						if(adbCommand.contains("shell am startservice"))
						{
							mode = ComponentMode.Service;
						}
						else if(adbCommand.contains("shell am start"))
						{
							mode = ComponentMode.Activity;
						}
						else if(adbCommand.contains("shell am broadcast"))
						{
							mode = ComponentMode.BroadcastReceiver;
						}
						else				//else if(adbCommand.contains("shell am startservice"))
						{
							mode = null;
						}

						base.exec("adb shell am force-stop " + packageName);
						try {
							Thread.currentThread().sleep(2000);
						} catch (InterruptedException e) {

						}		

						base.setLogcat(adbCommand, packageName);

						try {
							Thread.currentThread().sleep(3000);
						} catch (InterruptedException e) {

						}

						boolean normalCommand = false;
						String[] spLine = base.getAdbCommandLog();

						for(int k = 0; k < spLine.length; k++)
						{
							addRowinExcel(spLine[k]);
							System.out.println(spLine[k]);
							if(mode == ComponentMode.Activity)
							{
								if(spLine[k].contains("Starting: Intent"))
								{
									normalCommand = true;
								}
							}
							else if(mode == ComponentMode.BroadcastReceiver)
							{
								if(spLine[k].contains("Broadcasting: Intent"))
								{
									normalCommand = true;
								}
							}
							else if(mode == ComponentMode.Service)
							{
								if(spLine[k].contains("Starting service: Intent"))
								{
									normalCommand = true;
								}
							}
						}

						
						AnalyzeResult result;
						result = benchResult(base, packageName, mode, normalCommand);

						// TODO Auto-generated catch block

						base.modelAdbCommand.setValueAt(result, i, 2);
						base.modelAdbCommand.fireTableDataChanged();

						if(normalCommand == true)
						{
							if(result == AnalyzeResult.Normal)
							{
								Normal++;
							}
							else if(result == AnalyzeResult.Exit)
							{
								Exit++;
							}
							else if(result == AnalyzeResult.ErrorExit)
							{
								ErrorExit++;
							}
							else if(result == AnalyzeResult.IntentSpecCatchAndNormal)
							{
								IntentSpecCatchAndNormal++;
							}
							else if(result == AnalyzeResult.IntentSpecCatchAndExit)
							{
								IntentSpecCatchAndExit++;
							}
							else if(result == AnalyzeResult.IntentSpecCatchAndErrorExit)
							{
								IntentSpecCatchAndErrorExit++;
							}
							else if(result == AnalyzeResult.IntentSpecPassAndNormal)
							{
								IntentSpecPassAndNormal++;
							}
							else if(result == AnalyzeResult.IntentSpecPassAndExit)
							{
								IntentSpecPassAndExit++;
							}
							else if(result == AnalyzeResult.IntentSpecPassAndErrorExit)
							{
								IntentSpecPassAndErrorExit++;
							}
						}
						else
						{
							if(rebooted == false)
							{
								base.RebootDevice();								
								i--;
								rebooted = true;
								System.out.println("Reboot device");
								continue;
							}
							else
							{
								rebooted = false;
								CantAnalyze++;
							}
						}

						int resultCount = Normal + Exit + ErrorExit + 
								+ IntentSpecCatchAndNormal +  IntentSpecCatchAndExit + IntentSpecCatchAndErrorExit
								+ IntentSpecPassAndNormal + IntentSpecPassAndExit + IntentSpecPassAndErrorExit + CantAnalyze;

						base.showResult(Normal, Exit, ErrorExit, IntentSpecCatchAndNormal, IntentSpecCatchAndExit,
								IntentSpecCatchAndErrorExit, IntentSpecPassAndNormal, IntentSpecPassAndExit, IntentSpecPassAndErrorExit,
								resultCount, CantAnalyze, flagUseIntentAssertion);

						addRowinExcel("result : " + result.toString());
						addRowinExcel("------------------------------------------------------------");
						try {
							sheet.flushRows();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				String resultAll = "Pass\t: " + (Normal + Exit)
						// + "\nPass\t\t: " + Exit 
						+ "\nFail\t: " + ErrorExit
						+ (flagUseIntentAssertion ?

								"\nAssert/F=>Pass\t: " + IntentSpecCatchAndNormal
								+ "\nAssert/F=>Pass\t: " + IntentSpecCatchAndExit
								+ "\nAssert/F=>Fail\t: " + IntentSpecCatchAndErrorExit
								+ "\nAssert/T=>Pass\t: " + IntentSpecPassAndNormal 
								+ "\nAssert/T=>Pass\t: " + IntentSpecPassAndExit 
								+ "\nAssert/T=>Fail\t: " + IntentSpecPassAndErrorExit

								: "")

						+ "\nAnalysis Failure : " + CantAnalyze
						+ "\nResult Count\t\t: " + (Normal + Exit + ErrorExit 
								+ IntentSpecCatchAndNormal + IntentSpecCatchAndExit + IntentSpecCatchAndErrorExit 
								+ IntentSpecPassAndNormal + IntentSpecPassAndExit + IntentSpecPassAndErrorExit + CantAnalyze);

				String[] resultAllLine = resultAll.split("\n");

				for(int i = 0; i < resultAllLine.length; i++)
				{
					addRowinExcel(resultAllLine[i]);
				}

				try {
					workbook.write(new FileOutputStream(fileName));
					workbook.close();  // close()
					System.out.println("Test end");
					base.endBenchStart(fileName);
					//} catch (WriteException | IOException e) {  // IOException
				} catch(Throwable e) {
					e.printStackTrace();
				}
			}
		};

		worker.start();

		return worker;
	}

	CellStyle blackdefault;
	CellStyle bluedefault, greendefault, orangedefault, reddefault;
	CellStyle blackblue, blackgreen, blackred, blackorange, 
	blueblue, bluegreen, bluered, blueorange,
	greenblue, greengreen, greenred, greenorange,
	redblue, redgreen, redred, redorange,
	orangeblue, orangegreen, orangered, orangeorange;

	public void makeCellStyle()
	{
		Font bk = workbook.createFont();
		bk.setFontName("Courier New");
		bk.setColor(black);
		Font bl = workbook.createFont();
		bl.setFontName("Courier New");
		bl.setColor(blue);
		Font g = workbook.createFont();
		g.setFontName("Courier New");
		g.setColor(green);
		Font r = workbook.createFont();
		r.setFontName("Courier New");
		r.setColor(red);
		Font o = workbook.createFont();
		o.setFontName("Courier New");
		o.setColor(orange);

		blackdefault = workbook.createCellStyle();
		blackdefault.setFont(bk);
		bluedefault = workbook.createCellStyle();
		bluedefault.setFont(bl);
		greendefault = workbook.createCellStyle();
		greendefault.setFont(g);
		orangedefault = workbook.createCellStyle();
		orangedefault.setFont(o);
		reddefault = workbook.createCellStyle();
		reddefault.setFont(r);


		blackblue = workbook.createCellStyle();
		blackblue.setFont(bk);
		blackblue.setBorderBottom(CellStyle.BORDER_THICK);
		blackblue.setBottomBorderColor(blue);	
		blackgreen = workbook.createCellStyle();
		blackgreen.setFont(bk);
		blackgreen.setBorderBottom(CellStyle.BORDER_THICK);
		blackgreen.setBottomBorderColor(green);	
		blackred = workbook.createCellStyle();
		blackred.setFont(bk);
		blackred.setBorderBottom(CellStyle.BORDER_THICK);
		blackred.setBottomBorderColor(red);	
		blackorange = workbook.createCellStyle();
		blackorange.setFont(bk);
		blackorange.setBorderBottom(CellStyle.BORDER_THICK);
		blackorange.setBottomBorderColor(orange);

		blueblue = workbook.createCellStyle();
		blueblue.setFont(bl);
		blueblue.setBorderBottom(CellStyle.BORDER_THICK);
		blueblue.setBottomBorderColor(blue);	
		bluegreen = workbook.createCellStyle();
		bluegreen.setFont(bl);
		bluegreen.setBorderBottom(CellStyle.BORDER_THICK);
		bluegreen.setBottomBorderColor(green);	
		bluered = workbook.createCellStyle();
		bluered.setFont(bl);
		bluered.setBorderBottom(CellStyle.BORDER_THICK);
		bluered.setBottomBorderColor(red);	
		blueorange = workbook.createCellStyle();
		blueorange.setFont(bl);
		blueorange.setBorderBottom(CellStyle.BORDER_THICK);
		blueorange.setBottomBorderColor(orange);

		greenblue = workbook.createCellStyle();
		greenblue.setFont(g);
		greenblue.setBorderBottom(CellStyle.BORDER_THICK);
		greenblue.setBottomBorderColor(blue);	
		greengreen = workbook.createCellStyle();
		greengreen.setFont(g);
		greengreen.setBorderBottom(CellStyle.BORDER_THICK);
		greengreen.setBottomBorderColor(green);	
		greenred = workbook.createCellStyle();
		greenred.setFont(g);
		greenred.setBorderBottom(CellStyle.BORDER_THICK);
		greenred.setBottomBorderColor(red);	
		greenorange = workbook.createCellStyle();
		greenorange.setFont(g);
		greenorange.setBorderBottom(CellStyle.BORDER_THICK);
		greenorange.setBottomBorderColor(orange);

		redblue = workbook.createCellStyle();
		redblue.setFont(r);
		redblue.setBorderBottom(CellStyle.BORDER_THICK);
		redblue.setBottomBorderColor(blue);	
		redgreen = workbook.createCellStyle();
		redgreen.setFont(r);
		redgreen.setBorderBottom(CellStyle.BORDER_THICK);
		redgreen.setBottomBorderColor(green);	
		redred = workbook.createCellStyle();
		redred.setFont(r);
		redred.setBorderBottom(CellStyle.BORDER_THICK);
		redred.setBottomBorderColor(red);	
		redorange = workbook.createCellStyle();
		redorange.setFont(r);
		redorange.setBorderBottom(CellStyle.BORDER_THICK);
		redorange.setBottomBorderColor(orange);

		orangeblue = workbook.createCellStyle();
		orangeblue.setFont(o);
		orangeblue.setBorderBottom(CellStyle.BORDER_THICK);
		orangeblue.setBottomBorderColor(blue);	
		orangegreen = workbook.createCellStyle();
		orangegreen.setFont(o);
		orangegreen.setBorderBottom(CellStyle.BORDER_THICK);
		orangegreen.setBottomBorderColor(green);	
		orangered = workbook.createCellStyle();
		orangered.setFont(o);
		orangered.setBorderBottom(CellStyle.BORDER_THICK);
		orangered.setBottomBorderColor(red);	
		orangeorange = workbook.createCellStyle();
		orangeorange.setFont(o);
		orangeorange.setBorderBottom(CellStyle.BORDER_THICK);
		orangeorange.setBottomBorderColor(orange);
	}

	public void addRowinExcel(String str)
	{
		//WritableFont font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
		//WritableCellFormat analysisFormat = new WritableCellFormat(font);


		//		XSSFFont font = workbook.createFont();
		//		font.setFontName("Courier New");
		//		font.setColor(black);
		//		
		//		//XSSFCellStyle analysisFormat = new XSSFCellStyle(new StylesTable());
		//		XSSFCellStyle analysisFormat = workbook.createCellStyle();
		//		analysisFormat.setFont(font);


		//int nowRow = sheet.getRows();

		//		try {
		//			sheet.addCell(new Label(0, nowRow, str, analysisFormat));
		//		} catch (WriteException e) {
		//			e.printStackTrace();
		//		}

		int nowRow =sheet.getPhysicalNumberOfRows(); 
		SXSSFRow row = sheet.createRow(nowRow); // nowRow-th row

		SXSSFCell cell = row.createCell(0);				
		cell.setCellValue(str);
		cell.setCellStyle(blackdefault);
	}

	public void makeExcel()
	{
		//		try {
		//			SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy_MM_dd_HH_mm_ss", Locale.KOREA );
		//			Date currentTime = new Date( );
		//			String dTime = formatter.format ( currentTime );
		//			
		//		    String fileName = "LOG_" + dTime + ".xls";
		//workbook = Workbook.createWorkbook(new File(fileName)); // workbook = new XSSFWOrkbook();  �뙆�씪�씠由꾩� writer�븷 �븣 吏��젙
		workbook = new SXSSFWorkbook(-1);
		//sheet = workbook.createSheet("Benchmark Result", 0);  // sheet = workbook.createSheet("Benchmark Result");
		sheet = workbook.createSheet("Benchmark Result");

		//		    WritableFont font = new WritableFont(WritableFont.createFont("Courier New"));
		//		    WritableCellFormat format = new WritableCellFormat(font);;
		//		    
		//		    sheet.addCell(new Label(0, 0, "Level", format));
		//			sheet.addCell(new Label(1, 0, "Time", format));
		//			sheet.addCell(new Label(2, 0, "PID", format));
		//			sheet.addCell(new Label(3, 0, "TID", format));
		//			sheet.addCell(new Label(4, 0, "Application", format));
		//			sheet.addCell(new Label(5, 0, "Tag", format));
		//			sheet.addCell(new Label(6, 0, "Text", format));
		//			sheet.addCell(new Label(7, 0, "RawMessage", format));

		SXSSFRow row = sheet.createRow(0); // 0th row

		SXSSFCell cell;

		//XSSFCellStyle format = new XSSFCellStyle(new StylesTable());
		CellStyle format = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setFontName("Courier New");
		format.setFont(font);

		cell = row.createCell(0);
		cell.setCellValue("Level");
		cell.setCellStyle(format);

		cell = row.createCell(1);
		cell.setCellValue("Time");
		cell.setCellStyle(format);

		cell = row.createCell(2);
		cell.setCellValue("PID");
		cell.setCellStyle(format);

		cell = row.createCell(3);
		cell.setCellValue("TID");
		cell.setCellStyle(format);

		cell = row.createCell(4);
		cell.setCellValue("Application");
		cell.setCellStyle(format);

		cell = row.createCell(5);
		cell.setCellValue("Tag");
		cell.setCellStyle(format);

		cell = row.createCell(6);
		cell.setCellValue("Text");
		cell.setCellStyle(format);

		cell = row.createCell(7);
		cell.setCellValue("RawMessage");
		cell.setCellStyle(format);

		//			sheet.setColumnView(0, 3);
		//			sheet.setColumnView(1, 25);
		//			sheet.setColumnView(2, 6);
		//			sheet.setColumnView(3, 6);
		//			sheet.setColumnView(4, 20);
		//			sheet.setColumnView(5, 20);
		//			sheet.setColumnView(6, 150);
		//			sheet.setColumnView(7, 150);

		sheet.setColumnWidth(0, 3*256);
		sheet.setColumnWidth(1, 25*256);
		sheet.setColumnWidth(2, 6*256);
		sheet.setColumnWidth(3, 6*256);
		sheet.setColumnWidth(4, 20*256);
		sheet.setColumnWidth(5, 20*256);
		sheet.setColumnWidth(6, 150*256);
		sheet.setColumnWidth(7, 150*256);

		//} catch (IOException | WriteException e) {
		//			e.printStackTrace();
		//		}
	}

	final short black = (short)Color.black.getRGB();
	final short red = (short)Color.RED.getRGB();
	final short green = (short)Color.GREEN.getRGB();
	final short orange = (short)Color.ORANGE.getRGB();
	final short blue = (short)Color.blue.getRGB();

	final int DEFAULT = 0, BLUE = 1, GREEN = 2, ORANGE = 3, RED = 4;

	public AnalyzeResult benchResult(Benchbase base, String filter, ComponentMode mode, boolean normalCommand)
	{
		AnalyzeResult result = AnalyzeResult.CantAnalyze;
		int IntentSpecFlag = 0;		//0 : NonIntentSpec, 1 : IntentSpecCatch, 2 : IntentSpecPass
		int ProgramState = 0;		//0 : Normal, 1 : Exit, 2 : ErrorExit
		int fontstyle = 0; // 0 : default( = black), 1 : blue, 2 : green, 3:orange, 4: red
		int underbar = 0; // 0 : default( = nothing), 1 : blue, 2 : green, 3:orange, 4: red
		CellStyle analysisFormat = blackdefault;
		base.LogcatLock.lock();

		//		try {

		for(int i = 0; i < base.modelLogcatView.getRowCount(); i++)
		{
			//				WritableFont font;
			//				XSSFFont font;		
			String level = base.modelLogcatView.getValueAt(i, 0).toString();
			if(level.equals("V"))
			{
				//font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
				fontstyle = DEFAULT;
			}	
			else if(level.equals("D"))
			{
				//font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);              	
				fontstyle = BLUE;
			}
			else if(level.equals("I"))
			{
				//font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.GREEN);
				fontstyle = GREEN;
			}
			else if(level.equals("W"))
			{
				// font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.ORANGE);
				fontstyle = ORANGE;
			}
			else if(level.equals("E"))
			{
				//font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
				fontstyle = RED;
			}
			else
			{
				//font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);

				fontstyle = DEFAULT;
			}


			//WritableCellFormat analysisFormat = new WritableCellFormat(font);

			//XSSFCellStyle analysisFormat = new XSSFCellStyle(new StylesTable());
			String rawLog = base.modelLogcatView.getValueAt(i, 7).toString();

			if(mode == ComponentMode.Activity)
			{
				if(rawLog.toString().contains("Force finishing"))
				{
					ProgramState = 2;
					// analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.RED);
					underbar = RED;
				}
				else if(rawLog.toString().contains("Finishing"))
				{
					ProgramState = 1;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					underbar = ORANGE;
				}
				else if(rawLog.toString().contains("Force removing"))
				{
					ProgramState = 1;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_GREEN);
					underbar = GREEN;
				}
				else if(rawLog.toString().contains("Displayed"))
				{
					ProgramState = 0;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					underbar = BLUE;
				}
				else
				{
					underbar = DEFAULT;
				}

				if(rawLog.toString().contains("IntentSpec: Error Catch"))
				{
					underbar = ORANGE;
				}
				else if(rawLog.toString().contains("IntentSpec: Pass"))
				{
					underbar = BLUE;
				}

			}
			else if(mode == ComponentMode.BroadcastReceiver)
			{
				if(rawLog.toString().contains("has died"))
				{
					ProgramState = 2;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.RED);

					underbar = RED;
				}

				if(rawLog.toString().contains("IntentSpec: Error Catch"))
				{
					IntentSpecFlag = 1;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					underbar = ORANGE;
				}
				else if(rawLog.toString().contains("IntentSpec: Pass"))
				{
					IntentSpecFlag = 2;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					underbar = BLUE;
				}
			}
			else if(mode == ComponentMode.Service)
			{
				if(rawLog.toString().contains("Shutting down VM"))
				{
					underbar = RED;
				}
				else if(rawLog.toString().contains("VM exiting with result code"))
				{
					ProgramState = 1;
					//analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					underbar = ORANGE;
				}

				if(rawLog.toString().contains("IntentSpec: Error Catch"))
				{
					IntentSpecFlag = 1;
					// analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					underbar = ORANGE;
				}
				else if(rawLog.toString().contains("IntentSpec: Pass"))
				{
					IntentSpecFlag = 2;
					// analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					underbar = BLUE;
				}
			}


			int nowRow = sheet.getPhysicalNumberOfRows();
			nowRow = sheet.getPhysicalNumberOfRows();

			//				sheet.addCell(new Label(0, nowRow, ui.modelLogcatView.getValueAt(i, 0).toString(), analysisFormat));
			//				sheet.addCell(new Label(1, nowRow, ui.modelLogcatView.getValueAt(i, 1).toString(), analysisFormat));
			//				sheet.addCell(new Label(2, nowRow, ui.modelLogcatView.getValueAt(i, 2).toString(), analysisFormat));
			//				sheet.addCell(new Label(3, nowRow, ui.modelLogcatView.getValueAt(i, 3).toString(), analysisFormat));
			//				sheet.addCell(new Label(4, nowRow, ui.modelLogcatView.getValueAt(i, 4).toString(), analysisFormat));
			//				sheet.addCell(new Label(5, nowRow, ui.modelLogcatView.getValueAt(i, 5).toString(), analysisFormat));
			//				sheet.addCell(new Label(6, nowRow, ui.modelLogcatView.getValueAt(i, 6).toString(), analysisFormat));
			//				sheet.addCell(new Label(7, nowRow, ui.modelLogcatView.getValueAt(i, 7).toString(), analysisFormat));


			SXSSFRow row = sheet.createRow(nowRow); // nowRow-th row
			analysisFormat = ChooseCellStyle(fontstyle, underbar);
			for(int col=0; col<=7; col++) {
				SXSSFCell cell = row.createCell(col);				
				cell.setCellValue(base.modelLogcatView.getValueAt(i, col).toString());
				cell.setCellStyle(analysisFormat);
			}

		}

		if(normalCommand == true)
		{
			if(IntentSpecFlag == 0)
			{
				if(ProgramState == 0)
				{
					result = AnalyzeResult.Normal;
				}
				else if(ProgramState == 1)
				{
					result = AnalyzeResult.Exit;
				}
				else if(ProgramState == 2)
				{
					result = AnalyzeResult.ErrorExit;
				}
			}
			else if(IntentSpecFlag == 1)
			{
				if(ProgramState == 0)
				{
					result = AnalyzeResult.IntentSpecCatchAndNormal;
				}
				else if(ProgramState == 1)
				{
					result = AnalyzeResult.IntentSpecCatchAndExit;
				}
				else if(ProgramState == 2)
				{
					result = AnalyzeResult.IntentSpecCatchAndErrorExit;
				}
			}
			else if(IntentSpecFlag == 2)
			{
				if(ProgramState == 0)
				{
					result = AnalyzeResult.IntentSpecPassAndNormal;
				}
				else if(ProgramState == 1)
				{
					result = AnalyzeResult.IntentSpecPassAndExit;
				}
				else if(ProgramState == 2)
				{
					result = AnalyzeResult.IntentSpecPassAndErrorExit;
				}
			}
		}
		else
		{
			result = AnalyzeResult.CantAnalyze;
		}


		//		} catch (WriteException e) {
		//			e.printStackTrace();
		//		}

		base.LogcatLock.unlock();

		return result;
	}

	public CellStyle ChooseCellStyle(int fontstyle, int underbar){
		switch(fontstyle){
		case DEFAULT:{
			switch(underbar){
			case DEFAULT:
				return blackdefault;
			case BLUE:
				return blackblue;
			case GREEN:
				return blackgreen;
			case ORANGE:
				return blackorange;
			case RED:
				return blackred;
			default:
				return blackdefault;
			}
		}
		case BLUE:{
			switch(underbar){
			case DEFAULT:
				return bluedefault;
			case BLUE:
				return blueblue;
			case GREEN:
				return bluegreen;
			case ORANGE:
				return blueorange;
			case RED:
				return bluered;
			default:
				return bluedefault;
			}
		}
		case GREEN:{
			switch(underbar){
			case DEFAULT:
				return greendefault;
			case BLUE:
				return greenblue;
			case GREEN:
				return greengreen;
			case ORANGE:
				return greenorange;
			case RED:
				return greenred;
			default:
				return greendefault;
			}
		}
		case ORANGE:{
			switch(underbar){
			case DEFAULT:
				return orangedefault;
			case BLUE:
				return orangeblue;
			case GREEN:
				return orangegreen;
			case ORANGE:
				return orangeorange;
			case RED:
				return orangered;
			default:
				return orangedefault;
			}
		}
		case RED:{
			switch(underbar){
			case DEFAULT:
				return reddefault;
			case BLUE:
				return redblue;
			case GREEN:
				return redgreen;
			case ORANGE:
				return redorange;
			case RED:
				return redred;
			default:
				return reddefault;
			}
		}
		default:
			return blackdefault;
		}
	}
}
