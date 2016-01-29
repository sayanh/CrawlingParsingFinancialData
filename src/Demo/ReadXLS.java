package Demo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadXLS {
	/**
	 * Read an excel file and spit out what we find.
	 *
	 * @param args
	 *            Expect one argument that is the file to read.
	 * @throws IOException
	 *             When there is an error processing the file.
	 */
	public static void main(String[] args) throws IOException {
		try {
			// create a new file input stream with the input file specified
			// at the command line
			File f = new File(
					"D:/work/TUM/study/IDP/FinancialandMarkets/Sayan_PanelData_5k.xlsx");
			FileInputStream fin = new FileInputStream(f);
			// create a new org.apache.poi.poifs.filesystem.Filesystem
			// POIFSFileSystem poifs = new POIFSFileSystem(f);
			System.out.println("Excel sheet is getting processed.....");
			XSSFWorkbook workbook = new XSSFWorkbook(fin);
			System.out.println("Get active sheet="
					+ workbook.getActiveSheetIndex());

			XSSFSheet mySheet = workbook.getSheetAt(0);
			// Get iterator to all the rows in current sheet
			Iterator<Row> rowIterator = mySheet.iterator();
			int counter = 0;
			String tempString = "";
			// Traversing over each row of XLSX file
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				tempString = tempString + counter + "|";
				
				// For each row, iterate through each columns
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_STRING:
						tempString = tempString + cell.getStringCellValue()
								+ "|";
						break;
					case Cell.CELL_TYPE_NUMERIC:
						tempString = tempString + cell.getNumericCellValue()
								+ "|";
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						tempString = tempString + cell.getBooleanCellValue()
								+ "|";
						break;
					default:
					}

				}
				counter++;
				tempString = tempString + "\n";
			}

			Utility.writeToFile(
					"D:/work/TUM/study/IDP/FinancialandMarkets/Sayan_PanelData.txt",
					tempString);
			// System.out.println(tempString);

			System.out.println("...done.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
