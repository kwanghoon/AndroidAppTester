package kr_ac_yonsei_mobilesw_Grouping;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Vector;

import org.apache.poi.xssf.streaming.*;
import org.apache.poi.xssf.usermodel.*;

import kr_ac_yonsei_mobilesw_UI.GenAndroidTestCodeCommand;


public class GroupingResult {

	public static ArrayList<Integer> Represent = new ArrayList<Integer>();
	public static ArrayList<Integer> ErrorIndex = new ArrayList<Integer>();
	public static String randomisfileName = "";
	// ���� �׷�ȭ�� ������ �� ���
	public static void main(String[] args) {
		String excelFileName = args[0];
		String pkgName = args[1];
		Date startTime = new Date();

		List list = readFile(excelFileName);

		Date endTime = new Date();
		long readTime = endTime.getTime() - startTime.getTime();	
		randomisfileName = String.valueOf(readTime) + ".randomis";
		writeFile(excelFileName, list, readTime);
	} 

	// �׷�ȭ ���� �Լ�(UI ���α׷����� ������ �� ���)
	public void start(final String fileName){

		// ������...
		Thread worker = new Thread()
		{
			public void run(){

				Date startTime = new Date();

				List list = readFile(fileName);

				Date endTime = new Date();
				long readTime = endTime.getTime() - startTime.getTime();


				writeFile(fileName, list, readTime);

			}
		};
		worker.start();

	}

	// ���������� �д� �Լ�
	public static List readFile(String fileName){
		System.out.println("Start Grouping\n");

		// 1 : ���� ���� �� �ʱ�ȭ
		List list = null;	// ����Ʈ
		int rowIndex = 0;	// �� �ε���
		int startRow = 0;	// ������
		int count = -1;			// �׷� ��ǥ ����

		// 2-1 : ���� �Է�
		FileInputStream file = null;
		try {
			file = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// 2-2 : ���� ���� �Է�
		XSSFWorkbook workbook = null;
		try {
			workbook = new XSSFWorkbook(file);
		} catch (IOException e) {
			e.printStackTrace();
		}		

		// 2-3 : ���� ���ϳ� (ù��°) ��Ʈ ��������
		XSSFSheet sheet = workbook.getSheetAt(0);


		// 2-4 : �м�
		// 2-4-1 : �������� ��ü/���� Ž��
		int rows = sheet.getPhysicalNumberOfRows();
		for (rowIndex=0; rowIndex<rows; rowIndex++){
			XSSFRow row = sheet.getRow(rowIndex);
			if (row != null) {
				XSSFCell cell = row.getCell(0);

				// 2-4-2 : ���ڿ� ����
				String value = "";
				if (cell == null) {
					continue;
				} 
				else {
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_FORMULA:
						value = cell.getCellFormula();
						break;
					case XSSFCell.CELL_TYPE_NUMERIC:
						value = cell.getNumericCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_STRING:
						value = cell.getStringCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_BLANK:
						value = cell.getBooleanCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_ERROR:
						value = cell.getErrorCellValue() + "";
						break;
					}
				}

				// 2-4-3 : (i,0)�� "Level"(ó��) �Ǵ� "------------------------------------------------------------"(������)�϶�(adb ��ɾ��� �����϶�) ������ �ӽ� ����
				if (value.equals("Level") || value.equals("------------------------------------------------------------")) {
					startRow = rowIndex;
					count++;
				}

				// 2-4-4 : ��� Ž�� ���� �� (i,0)�� "result : ErrorExit"�϶�(Failure �϶�) ��� ���� �� ������ ����(������, ����)
				// 2-4-5 : ����Ʈ�� ����
				else if (value.equals("result : ErrorExit")) {        
					if (list == null) {
						list = new List();
						list.add(startRow,rowIndex-startRow);
					}
					else {
						list.add(startRow,rowIndex-startRow);

						// 2-4-6 : ����Ʈ�� ���� ��� (Failure)�� ���� ��� 1:1 ���絵 �� ���� �� ��� ����
						String newCase = saveString(workbook, startRow, rowIndex-startRow);;
						String oldCase = "";
						String lcs = "";
						int lcsLength;
						double bigSimilarity = 0;
						double smallSimilarity = 0;

						for (int j=0; j<list.getListLength()-1; j++) {								
							oldCase = saveString(workbook, list.getStartRow(j), list.getRowLength(j));		
							lcs = compareString(newCase.length(), oldCase.length(), newCase, oldCase);
							lcsLength = lcs.length();

							bigSimilarity = 0;
							smallSimilarity = 0;
							if (newCase.length()<=oldCase.length()){
								bigSimilarity = (double)lcsLength / (double)oldCase.length() * 100;
								smallSimilarity = (double)lcsLength / (double)newCase.length() * 100;
							}
							else{
								bigSimilarity = (double)lcsLength / (double)newCase.length() * 100;
								smallSimilarity = (double)lcsLength / (double)oldCase.length() * 100;
							}

							list.saveData(list.getListLength()-1, newCase.length(), oldCase.length(), lcs, lcsLength, bigSimilarity, smallSimilarity);
						}
					}
					ErrorIndex.add(new Integer(count));
				}
			}
		}
		return list;
	}

	// ���ڿ� ���� �Լ�
	private static String saveString(XSSFWorkbook workbook, int startRow, int failLength) {

		XSSFSheet sheet = workbook.getSheetAt(0);
		StringBuilder loadString = new StringBuilder();

		// Result�� ���� ���ڿ��� ���� : �� 0,4,5,6
		int rowIndex = startRow+3;
		for(int i=0; i<failLength-3; i++){
			XSSFRow row = sheet.getRow(rowIndex);
			if (row != null) {
				XSSFCell cell = row.getCell(0);	// 0

				String value = "";
				if (cell != null) {		
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_FORMULA:
						value = cell.getCellFormula();
						break;
					case XSSFCell.CELL_TYPE_NUMERIC:
						value = cell.getNumericCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_STRING:
						value = cell.getStringCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_BLANK:
						value = cell.getBooleanCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_ERROR:
						value = cell.getErrorCellValue() + "";
						break;
					}
				}

				if(value.equals("W") || value.equals("E")){
					loadString.append(value);

					for(int j=4; j<7; j++){	// 4,5,6
						XSSFCell cell2 = row.getCell(j);

						String value2 = "";
						if (cell2 != null) {		
							switch (cell2.getCellType()) {
							case XSSFCell.CELL_TYPE_FORMULA:
								value2 = cell2.getCellFormula();
								break;
							case XSSFCell.CELL_TYPE_NUMERIC:
								value2 = cell2.getNumericCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_STRING:
								value2 = cell2.getStringCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_BLANK:
								value2 = cell2.getBooleanCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_ERROR:
								value2 = cell2.getErrorCellValue() + "";
								break;
							}
						}
						loadString.append(value2);
					}
				}	
			}
			rowIndex++;
		}	
		return loadString.toString();	
	}

	// LCS ���� �Լ� : LCS Algorithm
	private static String compareString(int m, int n, String A, String B){

		// ===================================================================== //
		// 1. If problem is trivial, solve it :                                  //
		// ===================================================================== //
		
		
		String C = "";
		char[] c = new char[1];

		if(n==0 || m==0){}
		else if(m==1){
			for(int j=0; j<n; j++){
				if(A.charAt(0)==B.charAt(j)){
					c[0] = A.charAt(0);
					C = new String(c);
					break;
				}				
			}
		}


		// ===================================================================== //
		// 2. Otherwise, split problem :                                         //
		// ===================================================================== //

		else{
			int i = m/2;

			// ===================================================================== //
			// 3. Evaluate L(i,j) and L*(i,j) [j = 0...n] :                          //
			// ===================================================================== //

			int[] L1 = new int[n+1];
			int[] L2 = new int[n+1];

			compare(i,n,A.substring(0,i),B.substring(0,n),L1);
			compare(m-i,n,reverse(A.substring(i,m)),reverse(B.substring(0,n)),L2);


			// ===================================================================== //
			// 4. Find j such that L(i,j) + L*(i,j) = L(m,n) using theorem :         //
			// ===================================================================== //

			int max = 0;
			int k = 0;
			for(int j=0; j<=n; j++){
				if(max<L1[j]+L2[n-j]){
					max = L1[j] + L2[n-j];
					k = j;
				}
			}


			// ===================================================================== //
			// 5. Solve simpler problems :                                           //
			// ===================================================================== //

			String C1 = "";
			String C2 = "";

			C1 = compareString(i,k,A.substring(0, i),B.substring(0, k));
			C2 = compareString(m-i,n-k,A.substring(i, m),B.substring(k, n));


			// ===================================================================== //
			// 6. Give output :                                                      //
			// ===================================================================== //

			C = C1 + C2;

		}

		return C;

	}

	// LCS ���� ���� �Լ� : LCS Algorithm
	private static void compare(int m, int n, String A, String B, int[] LL){

		int [][] K = new int [2][n+1];

		for(int j=0; j<=n; j++){
			K[1][j] = 0;
		}

		for(int i=1; i<=m; i++){
			for(int j=0; j<=n; j++){
				K[0][j] = K[1][j];
			}
			for(int j=1; j<=n; j++){		
				if(A.charAt(i-1)==B.charAt(j-1)){
					K[1][j] = K[0][j-1] + 1;
				}
				else{
					if(K[1][j-1]>=K[0][j])
						K[1][j] = K[1][j-1];
					else
						K[1][j] = K[0][j];
				}
			}
		}

		for(int j=0; j<=n; j++){
			LL[j]=K[1][j];
		}

	}

	// ���ڿ� ������ �Լ�
	public static String reverse(String S){

		StringBuffer a = new StringBuffer();

		for(int i=S.length()-1; i>=0; i--){
			a.append(S.charAt(i));
		}

		return a.toString();
	}

	// ��� �� �α� ���� ��� �Լ�
	private static void writeFile(String fileName, List list, Long readTime) {

		Date startTime = new Date();
		//
		Vector<Vector<Integer>> group = new Vector<Vector<Integer>>();
		Vector<Integer> v1 = new Vector<Integer>();
		int flag;

		if(list!=null){
			for(int i=0; i<list.getListLength(); i++){
				flag = 0;
				Vector<Double> v2 = list.getBigSimilarity(i);

				for(int j=0; j<v2.size(); j++){
					if((Double)v2.get(j)>=99){ // *** ���絵 99 ��� (= 99% ������ ���)
						flag = 1;
						// i : ���, j : �񱳴��, (i>j)

						int insertNode=-1;
						for(int k=0; k<group.size(); k++){
							Vector<Integer> btmp = group.get(k);
							for(int z=0; z<btmp.size(); z++){
								// ��
								if(btmp.get(z)==j){
									insertNode=k;
									break;
								}
							}
							if(insertNode>=0)
								break;
						}
						group.get(insertNode).add(i);

						//
						break;
					}
				}

				if(flag==0){
					v1.addElement(i);
					Vector<Integer> tmp = new Vector<Integer>();
					tmp.addElement(i);
					group.addElement(tmp);
				}
			}
		}
		//


		FileInputStream file = null;
		XSSFWorkbook workbook = null;		
		SXSSFWorkbook workbook2 = new SXSSFWorkbook();
		SXSSFSheet sheet2 = workbook2.createSheet("Result");
		SXSSFRow row2 = null;
		SXSSFCell cell2 = null;

		int l = 0;
		int k = 0; // ���ͳ� ��� �ε���

		// ���� �Է�
		try {
			file = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// ���� ���� �Է�	
		try {
			workbook = new XSSFWorkbook(file);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// ���� ���ϳ� (ù��°)��Ʈ ��������
		XSSFSheet sheet = workbook.getSheetAt(0);


		// �׽�Ʈ ��� ��� //
		row2 = sheet2.createRow(l);
		cell2 = row2.createCell(0);
		cell2.setCellValue("< Test Result >");
		l++;

		int rows = sheet.getPhysicalNumberOfRows();
		for (int rowIndex=rows-4; rowIndex<rows; rowIndex++){
			XSSFRow row = sheet.getRow(rowIndex);
			if (row != null) {
				XSSFCell cell = row.getCell(0);

				// 2-4-2 : ���ڿ� ����
				String value = "";
				if (cell == null) {
					continue;
				} 
				else {
					switch (cell.getCellType()) {
					case XSSFCell.CELL_TYPE_FORMULA:
						value = cell.getCellFormula();
						break;
					case XSSFCell.CELL_TYPE_NUMERIC:
						value = cell.getNumericCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_STRING:
						value = cell.getStringCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_BLANK:
						value = cell.getBooleanCellValue() + "";
						break;
					case XSSFCell.CELL_TYPE_ERROR:
						value = cell.getErrorCellValue() + "";
						break;
					}
				}


				row2 = sheet2.createRow(l);
				cell2 = row2.createCell(0);
				cell2.setCellValue(value);
				l++;	
			}
		}
		l++;

		if(list!=null){
			// Fail Log ��� //
			for(int i=0; i<list.getListLength(); i++){		// i : ����Ʈ�� ��� �ε���
				//if(k>=v1.size()){
				//	break;
				//}
				//else if(i==(int)v1.get(k)){ 		// ��� ��� ����
				row2 = sheet2.createRow(l);
				cell2 = row2.createCell(0);
				cell2.setCellValue("Fail Log");
				cell2 = row2.createCell(1);
				cell2.setCellValue(k);
				l++;

				int startPoint = list.getStartRow(i) + 1;
				for(int j =0; j<list.getRowLength(i)-3; j++){
					XSSFRow row = sheet.getRow(startPoint);
					if (row != null) {
						XSSFCell cell = row.getCell(0);	// 0

						String value = "";
						if (cell != null) {		
							switch (cell.getCellType()) {
							case XSSFCell.CELL_TYPE_FORMULA:
								value = cell.getCellFormula();
								break;
							case XSSFCell.CELL_TYPE_NUMERIC:
								value = cell.getNumericCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_STRING:
								value = cell.getStringCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_BLANK:
								value = cell.getBooleanCellValue() + "";
								break;
							case XSSFCell.CELL_TYPE_ERROR:
								value = cell.getErrorCellValue() + "";
								break;
							}
						}

						row2 = sheet2.createRow(l);
						cell2 =row2.createCell(0);
						cell2.setCellValue(value);
						l++;

						for(int z=4; z<7; z++){	// 4,5,6
							XSSFCell cell3 = row.getCell(z);

							String value3 = "";
							if (cell3 != null) {		
								switch (cell3.getCellType()) {
								case XSSFCell.CELL_TYPE_FORMULA:
									value3 = cell3.getCellFormula();
									break;
								case XSSFCell.CELL_TYPE_NUMERIC:
									value3 = cell3.getNumericCellValue() + "";
									break;
								case XSSFCell.CELL_TYPE_STRING:
									value3 = cell3.getStringCellValue() + "";
									break;
								case XSSFCell.CELL_TYPE_BLANK:
									value3 = cell3.getBooleanCellValue() + "";
									break;
								case XSSFCell.CELL_TYPE_ERROR:
									value3 = cell3.getErrorCellValue() + "";
									break;
								}
							}
							if (value3.equalsIgnoreCase("false")) // ��ĭ�϶�
								value3 = "";

							cell2 =row2.createCell(z-3);
							cell2.setCellValue(value3);	
						}	
					}
					startPoint++;
				}

				row2 = sheet2.createRow(l);
				cell2 =row2.createCell(0);
				cell2.setCellValue("-");
				l++;

				k++;	
				//}
			}


			Vector<Integer> groups = new Vector<Integer>();

			if(group.size()!=0){
				row2 = sheet2.createRow(l);l++;
				cell2 = row2.createCell(0);
				cell2.setCellValue("< Group Info >");

				for(int i=0; i<group.size(); i++){	
					row2 = sheet2.createRow(l);l++;
					groups = group.get(i);
					cell2 = row2.createCell(0);
					cell2.setCellValue("GNum");
					cell2 = row2.createCell(1);
					cell2.setCellValue(i);
					cell2 = row2.createCell(2);
					cell2.setCellValue(":");
					Represent.add(ErrorIndex.get(groups.get(0)));
					for(int j=0; j<groups.size(); j++){
						cell2 = row2.createCell(j+3);
						cell2.setCellValue(groups.get(j));
					}
				}
			}
		}

		Date endTime = new Date();
		long writeTime = endTime.getTime() - startTime.getTime();	

		row2 = sheet2.createRow(l);l++;
		cell2 = row2.createCell(0);
		cell2.setCellValue("Grouping Time");
		cell2 = row2.createCell(1);
		cell2.setCellValue(readTime+writeTime);

		// fileName���� xlsx����
		//		int i=0;
		//		while(true){
		//			if(fileName.charAt(i)=='.')
		//				break;
		//			i++;
		//		}
		//		fileName=fileName.substring(0, i);

		FileOutputStream fileoutputstream = null;
		try {
			int index =fileName.lastIndexOf('/');
			String path = fileName.substring(0, index+1);	// ���
			String sname = fileName.substring(index+1);		// �̸�
			int index2 = sname.lastIndexOf('.');
			String name = sname.substring(0,index2);
			fileoutputstream = new FileOutputStream(path+"G"+name+"_"+readTime+writeTime+".xlsx");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			workbook2.write(fileoutputstream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileoutputstream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			workbook2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("End Grouping\n");
		System.out.println("Represent : " + Represent);
	}
	
	public static ArrayList<Integer> getRepresent()
	{
		return Represent;
	}
	
	public static String getRandomisfileName()
	{
		return randomisfileName;
	}
}
