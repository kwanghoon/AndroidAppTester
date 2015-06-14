package kr_ac_yonsei_mobilesw_UI;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jxl.*;
import jxl.format.BoldStyle;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.Pattern;
import jxl.format.UnderlineStyle;
import jxl.write.Font;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class BenchStart {

	WritableWorkbook workbook;
	WritableSheet sheet;	
	
	boolean flagUseIntentAssertion = false; // IntentAssert 사용 여부
	
	public void start(Benchmark ui) 
	{		
		Thread worker = new Thread()
		{
			public void run()
			{
				makeExcel();
				
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
				
				
				if(ui.modelAdbCommand.getRowCount() < 1)
				{
					return;
				}
				
				for(int i = 0; i < ui.modelAdbCommand.getRowCount(); i++)
				{
					String adbCommand = ui.modelAdbCommand.getValueAt(i, 1).toString();
					String packageName = adbCommand.substring(adbCommand.indexOf("-n ") + 3, adbCommand.indexOf('/', adbCommand.indexOf("-n ") + 3));
					
					ComponentMode mode;
					
					if(adbCommand.contains("shell am start"))
					{
						mode = ComponentMode.Activity;
					}
					else if(adbCommand.contains("shell am broadcast"))
					{
						mode = ComponentMode.BroadcastReceiver;
					}
					else				//else if(adbCommand.contains("shell am startservice"))
					{
						mode = ComponentMode.Service;
					}
					
					ui.exec("adb shell am force-stop " + packageName);
					try {
						Thread.currentThread().sleep(2000);
					} catch (InterruptedException e) {
						
					}
					
					ui.txtAdbCommand.setText(adbCommand);
					ui.txtFilter.setText(packageName);
					ui.txtAdbCommandLog.setText("");
					
					ui.LogcatClear();
					ui.filterEvent();
					ui.exec();
					try {
						Thread.currentThread().sleep(3000);
					} catch (InterruptedException e) {
						
					}
					
					boolean normalCommand = false;
					String[] spLine = ui.txtAdbCommandLog.getText().split("\n");
					for(int k = 0; k < spLine.length; k++)
					{
						addRowinExcel(spLine[k]);
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
					
					AnalyzeResult result = benchResult(ui, packageName, mode, normalCommand);
					ui.modelAdbCommand.setValueAt(result, i, 2);
					ui.modelAdbCommand.fireTableDataChanged();
					
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
						CantAnalyze++;
					}
				
					int resultCount = Normal + Exit + ErrorExit + 
							+ IntentSpecCatchAndNormal +  IntentSpecCatchAndExit + IntentSpecCatchAndErrorExit
							+ IntentSpecPassAndNormal + IntentSpecPassAndExit + IntentSpecPassAndErrorExit + CantAnalyze;
					
					ui.txtBenchResult.setText("Pass\t: " + (Normal+Exit)
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
							
							+ "\nProgress\t: " + (int)(((double)resultCount / ui.modelAdbCommand.getRowCount()) * 100) + "% (" + (resultCount + "/" + ui.modelAdbCommand.getRowCount() + ")")
							+ "\nAnalysis Failure\t: " + CantAnalyze );
					
					addRowinExcel("result : " + result.toString());
					addRowinExcel("------------------------------------------------------------");
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
						
						+ "\nAnalysis Failure\t: " + CantAnalyze
						+ "\nResult Count\t\t: " + (Normal + Exit + ErrorExit 
								+ IntentSpecCatchAndNormal + IntentSpecCatchAndExit + IntentSpecCatchAndErrorExit 
								+ IntentSpecPassAndNormal + IntentSpecPassAndExit + IntentSpecPassAndErrorExit + CantAnalyze);
				
				String[] resultAllLine = resultAll.split("\n");
				
				for(int i = 0; i < resultAllLine.length; i++)
				{
					addRowinExcel(resultAllLine[i]);
				}
				
				try {
					workbook.write();
					workbook.close();
				} catch (WriteException | IOException e) {
					e.printStackTrace();
				}
			}
		};
		
		worker.start();
	}
	
	public void addRowinExcel(String str)
	{
		WritableFont font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
		WritableCellFormat analysisFormat = new WritableCellFormat(font);
		
		int nowRow = sheet.getRows();
		
		try {
			sheet.addCell(new Label(0, nowRow, str, analysisFormat));
		} catch (WriteException e) {
			e.printStackTrace();
		}
	}
	
	public void makeExcel()
	{
		try {
			SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy_MM_dd_HH_mm_ss", Locale.KOREA );
			Date currentTime = new Date( );
			String dTime = formatter.format ( currentTime );
			
		    String fileName = "LOG_" + dTime + ".xls";
		    workbook = Workbook.createWorkbook(new File(fileName));
		    sheet = workbook.createSheet("Benchmark Result", 0);
		    
		    WritableFont font = new WritableFont(WritableFont.createFont("Courier New"));
		    WritableCellFormat format = new WritableCellFormat(font);;
		    
		    sheet.addCell(new Label(0, 0, "Level", format));
			sheet.addCell(new Label(1, 0, "Time", format));
			sheet.addCell(new Label(2, 0, "PID", format));
			sheet.addCell(new Label(3, 0, "TID", format));
			sheet.addCell(new Label(4, 0, "Application", format));
			sheet.addCell(new Label(5, 0, "Tag", format));
			sheet.addCell(new Label(6, 0, "Text", format));
			sheet.addCell(new Label(7, 0, "RawMessage", format));
			
			sheet.setColumnView(0, 3);
			sheet.setColumnView(1, 25);
			sheet.setColumnView(2, 6);
			sheet.setColumnView(3, 6);
			sheet.setColumnView(4, 20);
			sheet.setColumnView(5, 20);
			sheet.setColumnView(6, 150);
			sheet.setColumnView(7, 150);
		    
		} catch (IOException | WriteException e) {
			e.printStackTrace();
		}
	}
	
    public AnalyzeResult benchResult(Benchmark ui, String filter, ComponentMode mode, boolean normalCommand)
    {
    	AnalyzeResult result = AnalyzeResult.CantAnalyze;
    	int IntentSpecFlag = 0;		//0 : NonIntentSpec, 1 : IntentSpecCatch, 2 : IntentSpecPass
    	int ProgramState = 0;		//0 : Normal, 1 : Exit, 2 : ErrorExit
    	
    	ui.LogcatLock.lock();
		
		try {
			
			for(int i = 0; i < ui.modelLogcatView.getRowCount(); i++)
			{
				WritableFont font;
				
				String level = ui.modelLogcatView.getValueAt(i, 0).toString();
                if(level.equals("V"))
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                }	
                else if(level.equals("D"))
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLUE);
                }
                else if(level.equals("I"))
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.GREEN);
                }
                else if(level.equals("W"))
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.ORANGE);
                }
                else if(level.equals("E"))
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.RED);
                }
                else
                {
                	font = new WritableFont(WritableFont.createFont("Courier New"), WritableFont.DEFAULT_POINT_SIZE, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK);
                }
				
				
		    	WritableCellFormat analysisFormat = new WritableCellFormat(font);
				
				String rawLog = ui.modelLogcatView.getValueAt(i, 7).toString();
				
				if(mode == ComponentMode.Activity)
				{
					if(rawLog.toString().contains("Force finishing"))
					{
						ProgramState = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.RED);
					}
					else if(rawLog.toString().contains("Finishing"))
					{
						ProgramState = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					}
					else if(rawLog.toString().contains("Force removing"))
					{
						ProgramState = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_GREEN);
					}
					else if(rawLog.toString().contains("Displayed"))
					{
						ProgramState = 0;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					}
					else
					{

					}
					
					if(rawLog.toString().contains("IntentSpec: Error Catch"))
					{
						IntentSpecFlag = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					}
					else if(rawLog.toString().contains("IntentSpec: Pass"))
					{
						IntentSpecFlag = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					}
					
				}
				else if(mode == ComponentMode.BroadcastReceiver)
				{
					if(rawLog.toString().contains("has died"))
					{
						ProgramState = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.RED);
					}
					
					if(rawLog.toString().contains("IntentSpec: Error Catch"))
					{
						IntentSpecFlag = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					}
					else if(rawLog.toString().contains("IntentSpec: Pass"))
					{
						IntentSpecFlag = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					}
				}
				else if(mode == ComponentMode.Service)
				{
					if(rawLog.toString().contains("Shutting down VM"))
					{
						ProgramState = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.RED);
					}
					else if(rawLog.toString().contains("VM exiting with result code"))
					{
						ProgramState = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					}
					
					if(rawLog.toString().contains("IntentSpec: Error Catch"))
					{
						IntentSpecFlag = 1;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.ORANGE);
					}
					else if(rawLog.toString().contains("IntentSpec: Pass"))
					{
						IntentSpecFlag = 2;
						analysisFormat.setBorder(Border.BOTTOM, BorderLineStyle.THICK, Colour.LIGHT_BLUE);
					}
				}
				
				
				int nowRow = sheet.getRows();
				
				sheet.addCell(new Label(0, nowRow, ui.modelLogcatView.getValueAt(i, 0).toString(), analysisFormat));
				sheet.addCell(new Label(1, nowRow, ui.modelLogcatView.getValueAt(i, 1).toString(), analysisFormat));
				sheet.addCell(new Label(2, nowRow, ui.modelLogcatView.getValueAt(i, 2).toString(), analysisFormat));
				sheet.addCell(new Label(3, nowRow, ui.modelLogcatView.getValueAt(i, 3).toString(), analysisFormat));
				sheet.addCell(new Label(4, nowRow, ui.modelLogcatView.getValueAt(i, 4).toString(), analysisFormat));
				sheet.addCell(new Label(5, nowRow, ui.modelLogcatView.getValueAt(i, 5).toString(), analysisFormat));
				sheet.addCell(new Label(6, nowRow, ui.modelLogcatView.getValueAt(i, 6).toString(), analysisFormat));
				sheet.addCell(new Label(7, nowRow, ui.modelLogcatView.getValueAt(i, 7).toString(), analysisFormat));
				
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

		
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
		ui.LogcatLock.unlock();
		
		return result;
    }
}
