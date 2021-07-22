package it.nexera.ris.common.helpers.create.xls;

import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public final class XlsxHelper {

    public static Sheet readSheet(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = StreamingReader.builder()
                .rowCacheSize(100)
                .open(fis);
        fis.close();
        return workbook.getSheetAt(0);
    }

    public static int findColumnIndexByName(Row row, String columnName) {
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            String cellValue = cell.getStringCellValue();
            if (cellValue.equals(columnName)) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

}
