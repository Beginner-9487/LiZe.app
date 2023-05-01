package com.example.lize_app.utils;

import android.content.Context;
import android.os.Environment;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class My_Excel_File {

    private HSSFWorkbook workbook = null;
    private ArrayList<HSSFCellStyle> headerCellStyle = new ArrayList<>();

    // Global Variables
    private ArrayList<ArrayList<Row>> mRows = new ArrayList<>();
    private ArrayList<ArrayList<ArrayList<Cell>>> mCells = new ArrayList<>();
    private ArrayList<Sheet> mSheets = new ArrayList<>();

    private ArrayList<CellStyle> cellStyle = new ArrayList<>();

    private String file_path;

    public My_Excel_File() {}

    /**
     * Method: Generate Excel Workbook
     *
     * @param FilePath - Pass the desired filePame for the output excel Workbook
     */
    public void createExcelWorkbook(String FilePath) {
        file_path = FilePath;
        workbook = new HSSFWorkbook();
    }

    /**
     * Method: Create new Sheet into Excel Workbook
     *
     * @param sheetName - Pass the desired sheetName for the new excel sheet
     */
    public void create_new_sheet(String sheetName) {
        mSheets.add(workbook.createSheet(sheetName));
        mRows.add(new ArrayList());
        mCells.add(new ArrayList());
    }

    /**
     * Method: Write Data into Excel Workbook
     *
     * @param (sheet_name, row, column) - Write data into this position
     * @param data - Contains the actual data to be displayed in excel
     */
    public void write_file(String sheet_name, int row, int column, String data) {
        for (Sheet s:mSheets) {
            int sheet_index = 0;
            if(s.getSheetName().equals(sheet_name)) {
                write_cell(sheet_index, row, column, data);
            }
            sheet_index++;
        }
    }
    /**
     * Method: Write Data into Excel Workbook
     *
     * @param (sheet_index, row, column) - Write data into this position
     * @param data - Contains the actual data to be displayed in excel
     */
    public void write_file(int sheet_index, int row, int column, String data) {
        if(sheet_index<mSheets.size()) { write_cell(sheet_index, row, column, data); }
    }
    private void write_cell(int sheet_index, int row, int column, String data) {
        while (mRows.get(sheet_index).size() <= row) {
            mCells.get(sheet_index).add(new ArrayList());
            mRows.get(sheet_index).add(mSheets.get(sheet_index).createRow(mRows.get(sheet_index).size()));
        }
        while (mCells.get(sheet_index).get(row).size() <= column) {
            mCells.get(sheet_index).get(row).add(mRows.get(sheet_index).get(row).createCell(mCells.get(sheet_index).get(row).size()));
        }
        mCells.get(sheet_index).get(row).get(column).setCellValue(data);
    }

    /**
     * Method: Read Data from exited Excel File
     *
     * @param context  - Pass the application context
     * @param FilePath - Pass the desired filePame for the input excel Workbook
     */
    public void readExcelFromStorage(Context context, String FilePath) {
        File file = new File(FilePath);
        FileInputStream fileInputStream = null;

        try {

            fileInputStream = new FileInputStream(file);
            Log.i("Reading from Excel" + file);

            // Create instance having reference to .xls file
            workbook = new HSSFWorkbook(fileInputStream);

            // Fetch sheet at position 'i' from the workbook
            mSheets.clear();
            mRows.clear();
            mCells.clear();

            for (int i=0; i<workbook.getNumberOfSheets(); i++) {
                mSheets.add(workbook.getSheetAt(i));
                mRows.add(new ArrayList());
                mCells.add(new ArrayList());
                for (Row r:workbook.getSheetAt(i)) {
                    mRows.get(i).add(r);
                    mCells.get(i).add(new ArrayList());
                    for (Cell c:r) {
                        mCells.get(i).get(r.getRowNum()).add(c);
                    }
                }
            }

        } catch (IOException e) {
            Log.e("Error Reading Exception: ", e);
        } catch (Exception e) {
            Log.e("Failed to read file due to Exception: ", e);
        } finally {
            try {
                if (null != fileInputStream) {
                    fileInputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    /**
     * Export Data into Excel Workbook
     *
     * @return boolean - returns state whether workbook is written into storage or not
     */
    public boolean exportDataIntoWorkbook() {
        boolean isWorkbookWrittenIntoStorage;

        // Check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.e("Storage not available or read only");
            return false;
        }

        isWorkbookWrittenIntoStorage = storeExcelInStorage(file_path);

        return isWorkbookWrittenIntoStorage;
    }

    /**
     * Checks if Storage is READ-ONLY
     *
     * @return boolean
     */
    private static boolean isExternalStorageReadOnly() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED_READ_ONLY.equals(externalStorageState);
    }

    /**
     * Checks if Storage is Available
     *
     * @return boolean
     */
    private static boolean isExternalStorageAvailable() {
        String externalStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(externalStorageState);
    }

    /**
     * Store Excel Workbook in external storage
     *
     * @param fileName - name of workbook which will be stored in device
     * @return boolean - returns state whether workbook is written into storage or not
     */
    private boolean storeExcelInStorage(String fileName) {
        boolean isSuccess;
        File file = new File(fileName);
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(file);
            workbook.write(fileOutputStream);
            Log.i("Writing file" + file);
            isSuccess = true;
        } catch (IOException e) {
            Log.e("Error writing Exception: ", e);
            isSuccess = false;
        } catch (Exception e) {
            Log.e("Failed to save file due to Exception: ", e);
            isSuccess = false;
        } finally {
            try {
                if (null != fileOutputStream) {
                    fileOutputStream.close();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return isSuccess;
    }


}
