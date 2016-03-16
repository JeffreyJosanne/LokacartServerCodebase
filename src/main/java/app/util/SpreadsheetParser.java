package app.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SpreadsheetParser {

	public static File generateProductSheet(List<Integer> productId, List<String> productName, List<String> productType,
			List<Float> unitRate, List<Float> quantity, String organization) {
		List<String> productTypes = new ArrayList<String>();
		String[] test = productType.toArray(new String[0]);
		for (int i = 0; i < test.length; i++) {
			// System.out.println(test[i]);
			if (productTypes.indexOf(test[i]) == -1) {
				productTypes.add(test[i]);
			}
		}
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Product Manifest");

		XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
		XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper
				.createExplicitListConstraint(productTypes.toArray(new String[0]));
		CellRangeAddressList addressList = new CellRangeAddressList(productId.size(), productId.size() + 100, 2, 2);
		XSSFDataValidation validation = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, addressList);
		validation.setShowErrorBox(true);
		sheet.addValidationData(validation);
		CellStyle headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		headerStyle.setFont(font);		
		CellStyle unlockedCellStyle = workbook.createCellStyle();
		unlockedCellStyle.setLocked(false);
		sheet.protectSheet("password");
		int rownum = 0;
		Row headerRow = sheet.createRow(rownum++);
		Cell cellId = headerRow.createCell(0);
		cellId.setCellValue("Product ID");
		cellId.setCellStyle(headerStyle);
		Cell cellName = headerRow.createCell(1);
		cellName.setCellValue("Product Name");
		cellName.setCellStyle(headerStyle);
		Cell cellType = headerRow.createCell(2);
		cellType.setCellValue("Product Type");
		cellType.setCellStyle(headerStyle);
		Cell cellRate = headerRow.createCell(3);
		cellRate.setCellValue("Unit Rate");
		cellRate.setCellStyle(headerStyle);
		Cell cellQuantity = headerRow.createCell(4);
		cellQuantity.setCellValue("Quantity");
		cellQuantity.setCellStyle(headerStyle);
		for (int i = 0; i < productId.size(); i++) {
			Row row = sheet.createRow(rownum++);
			int cellnum = 0;
			Cell cell1 = row.createCell(cellnum++);
			cell1.setCellValue(productId.get(i));
			
			Cell cell2 = row.createCell(cellnum++);
			cell2.setCellValue(productName.get(i));
			cell2.setCellStyle(unlockedCellStyle);

			Cell cell3 = row.createCell(cellnum++);
			cell3.setCellValue(productType.get(i));
			
			Cell cell4 = row.createCell(cellnum++);
			cell4.setCellValue(unitRate.get(i));
			cell4.setCellStyle(unlockedCellStyle);
			
			Cell cell5 = row.createCell(cellnum++);
			cell5.setCellValue(quantity.get(i));
		}
		sheet.autoSizeColumn(0);
		sheet.autoSizeColumn(1);
		sheet.autoSizeColumn(2);
		sheet.autoSizeColumn(3);
		sheet.autoSizeColumn(4);

		for (int i = productId.size(); i <= productId.size() + 100; i++) {
			Row row = sheet.createRow(i);
			Cell nameCell = row.createCell(1);
			nameCell.setCellStyle(unlockedCellStyle);
			Cell prodTypeCell = row.createCell(2);
			prodTypeCell.setCellStyle(unlockedCellStyle);
			Cell unitRateCell = row.createCell(3);
			unitRateCell.setCellStyle(unlockedCellStyle);
			Cell qtyCell = row.createCell(4);
			qtyCell.setCellStyle(unlockedCellStyle);
		}
		File file = null;
		try {
			FileOutputStream out = new FileOutputStream(new File(organization+"-products.xlsx"));
			workbook.write(out);
			out.close();
			workbook.close();
			file = new File(organization+"-products.xlsx");
			System.out.println("Excel written successfully..");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return file;
	}
	
	public static HashMap<Integer, String[]> parseProductSheet(String organization)  {
		FileInputStream file =null;
		HashMap <Integer, String[]> data = new HashMap();
		int count =0;
		try {
			file = new FileInputStream(new File(organization+"-product.xlsx"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		XSSFWorkbook workbook=null;
		try {
			workbook = new XSSFWorkbook (file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		XSSFSheet sheet = workbook.getSheetAt(0);
		Iterator<Row> rowIterator = sheet.iterator();
		
		
		
		
		/*Row row = sheet.getRow(2);
		Cell cellId = row.getCell(0);
		Cell cellName = row.getCell(1);
		Cell cellType = row.getCell(2);
		Cell cellRate = row.getCell(3);
		int id = 0;
		String name = null;
		String type = null;
		double rate = 0;
		try {
		id = (int) cellId.getNumericCellValue();
		name = cellName.getStringCellValue();
		type=cellType.getStringCellValue();
		rate = cellRate.getNumericCellValue();	
		}
		catch(Exception e) {
			System.out.println("handeld");
		}
		if(id == 0) {
			System.out.println("id is null");
		}
		if (name == null) {
			System.out.println("name is null");
		}
		else {
		System.out.println("Id: "+id);
		System.out.println("Name: "+name);
		System.out.println("Type: "+type);
		System.out.println("rate: "+rate);
		}*/
		
		
		Row header = rowIterator.next();

		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Cell cellId = row.getCell(0);
			Cell cellName = row.getCell(1);
			Cell cellType = row.getCell(2);
			Cell cellRate = row.getCell(3);
			Cell cellQty = row.getCell(4);
			int id = 0; 
			String name = null;
			String type = null;
			float rate = 0;
			float qty = 0;
			try {
				name = cellName.getStringCellValue();
				type = cellType.getStringCellValue();
				rate = (float)cellRate.getNumericCellValue();	
				qty = (float)cellQty.getNumericCellValue();
				id =(int) cellId.getNumericCellValue();
				
				String [] contents = new String [5];
				contents[0] = new String();
				contents[1] = new String();
				contents[2] = new String();
				contents[3] = new String();
				contents[4] = new String();

				
				contents[0] = String.valueOf(id);
				System.out.println("id: "+contents[0]);
				contents[1] = name;
				contents[2] = type;
				contents[3] = String.valueOf(rate);
				contents[4] = String.valueOf(qty);

				
				data.put(count++, contents);

			}
			catch (Exception e) {
				if((id == 0) && !(name.equals("")) && !(type.equals(""))) {
				String [] contents = new String [5];
				contents[0] = new String();
				contents[1] = new String();
				contents[2] = new String();
				contents[3] = new String();
				contents[4] = new String();

				System.out.println("in  exception");
				contents[0] = String.valueOf(id);
				contents[1] = name;
				contents[2] = type;
				contents[3] = String.valueOf(rate);
				contents[4] = String.valueOf(qty);

				data.put(count++, contents);
				}
				
			}
			
		}
		try {
			System.out.println("Count: "+count);
			workbook.close();
			file.close();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;

	}

}
