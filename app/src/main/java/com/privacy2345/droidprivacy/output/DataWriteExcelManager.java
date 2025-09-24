package com.privacy2345.droidprivacy.output;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.privacy2345.droidprivacy.app.MyApplication;
import com.privacy2345.droidprivacy.model.ApiCallRecord;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 数据写入Excel。PS：通过poi库将数据写入到excel中，操作不能太过频繁，否则会导致卡顿
 *
 * @author : zhongjy@2345.com
 */
public class DataWriteExcelManager {

    public static DataWriteExcelManager getInstance() {
        return InstanceHolder.instance;
    }

    private static final class InstanceHolder {
        static final DataWriteExcelManager instance = new DataWriteExcelManager();
    }

    private static final String TAG = "DataWriteExcelManager";
    public static final String DIC_NAME = "DroidPrivacy";
    private static final String[] SHEET_TITLE = {"触发序号", "触发时间", "行为主体", "行为分类", "行为描述", "触发进程", "触发方法", "方法参数", "调用堆栈"};
    private final DateFormat dateFormat;
    private String fileDirPath;
    private final Map<String, String> filePathMap;
    private final Map<String, Integer> fileNameIndexMap;
    private final Map<String, Workbook> workbookMap;
    private final Map<String, Sheet> sheetMap;
    private int rowIndex;

    private DataWriteExcelManager() {
        fileNameIndexMap = new HashMap<>();
        filePathMap = new HashMap<>();
        workbookMap = new HashMap<>();
        sheetMap = new HashMap<>();
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
    }

    /**
     * 存储在/sdcard/Android/data/com.privacy.checker/files/DroidPrivacy
     */
    private String getFileDirPath() {
        if (!TextUtils.isEmpty(fileDirPath)) {
            return fileDirPath;
        }
        // 获取内部存储目录
        try {
            boolean isGranted = ActivityCompat.checkSelfPermission(MyApplication.getApplication(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            File filesDir = isGranted ? Environment.getExternalStorageDirectory() : MyApplication.getApplication().getExternalFilesDir(null);

            // 创建privacy文件夹
            File privacyDir = new File(filesDir, DIC_NAME);
            if (!privacyDir.exists()) {
                privacyDir.mkdir();
            }
            fileDirPath = privacyDir.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "创建文件夹出错");
        }
        Log.d(TAG, "文件夹路径：" + fileDirPath);
        return fileDirPath;
    }

    /**
     * 按照包名，以天维度创建文件
     */
    private String getFileName(String packageName, boolean isNeedCreateNew) {
        int index = 0;
        String fileNamePrefix = packageName + "_" + dateFormat.format(new Date());
        if (fileNameIndexMap.containsKey(fileNamePrefix)) {
            index = fileNameIndexMap.get(fileNamePrefix);
        }
        if (isNeedCreateNew) {
            index = getFileNameIndex(fileNamePrefix, index);
            rowIndex = 0;
            Iterator<Map.Entry<String, Workbook>> iterator1 = workbookMap.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, Workbook> entry = iterator1.next();
                if (entry.getKey().startsWith(fileNamePrefix)
                        && !TextUtils.equals(fileNamePrefix + "_" + index + ".xlsx", entry.getKey())) {
                    iterator1.remove();
                    Log.d(TAG, "excel，删除" + entry.getKey() + "的workbook");
                }
            }

            Iterator<Map.Entry<String, Sheet>> iterator2 = sheetMap.entrySet().iterator();
            while (iterator2.hasNext()) {
                Map.Entry<String, Sheet> entry = iterator2.next();
                if (entry.getKey().startsWith(fileNamePrefix)
                        && !TextUtils.equals(fileNamePrefix + "_" + index + ".xlsx", entry.getKey())) {
                    iterator2.remove();
                    Log.d(TAG, "excel，删除" + entry.getKey() + "的sheet");
                }
            }
        }
        fileNameIndexMap.put(fileNamePrefix, index);
        return fileNamePrefix + "_" + index + ".xlsx";
    }

    private int getFileNameIndex(String fileNamePrefix, int index) {
        File file = new File(getFileDirPath() + File.separator + fileNamePrefix + "_" + index + ".xlsx");
        if (file.exists()) {
            return getFileNameIndex(fileNamePrefix, index + 1);
        } else {
            return index;
        }
    }

    public void writeFile(String packageName, List<ApiCallRecord> apiCallRecordList) {
        if (TextUtils.isEmpty(packageName) || apiCallRecordList == null || apiCallRecordList.isEmpty()) {
            return;
        }
        String fileName = getFileName(packageName, rowIndex + apiCallRecordList.size() > 1500);
        String filePath;
        Workbook workbook = null;
        Sheet sheet = null;
        Log.d(TAG, "excel写入，文件名:" + fileName + ",写入数据数量:" + apiCallRecordList.size());
        if (filePathMap.containsKey(fileName)) {
            filePath = filePathMap.get(fileName);
            workbook = workbookMap.get(fileName);
            sheet = sheetMap.get(fileName);
            Log.d(TAG, "excel写入，使用已打开文件：" + fileName + "，起始行号:" + rowIndex);
        } else {
            filePath = getFileDirPath() + File.separator + fileName;
            if (TextUtils.isEmpty(filePath)) {
                return;
            }
            File file = new File(filePath);
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                    workbook = new XSSFWorkbook(fileInputStream);
                    sheet = workbook.getSheetAt(0);
                    rowIndex = sheet.getLastRowNum() + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Log.d(TAG, "excel写入，打开本地已存在文件：" + fileName + "，起始行号:" + rowIndex);
            } else {
                try {
                    file.createNewFile();
                    workbook = new XSSFWorkbook();
                    sheet = workbook.createSheet("Sheet0");

                    rowIndex = 0;

                    // 表头样式:上下左右居中、填充背景色、设置边框
                    CellStyle titleStyle = workbook.createCellStyle();
                    titleStyle.setAlignment(HorizontalAlignment.CENTER);
                    titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
                    titleStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
                    titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                    titleStyle.setBorderLeft(BorderStyle.THIN);
                    titleStyle.setBorderTop(BorderStyle.THIN);
                    titleStyle.setBorderRight(BorderStyle.THIN);
                    titleStyle.setBorderBottom(BorderStyle.THIN);
                    titleStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
                    titleStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
                    titleStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
                    titleStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());

                    // 创建并填充表头
                    Row row = sheet.createRow(0);
                    row.setHeightInPoints(28f);
                    for (int i = 0; i < SHEET_TITLE.length; i++) {
                        if (i == 0) {// 触发序号宽度设置
                            sheet.setColumnWidth(i, 10 * 256);
                        } else if (i == SHEET_TITLE.length - 3) {// 触发方法列宽度设置
                            sheet.setColumnWidth(i, 90 * 256);
                        } else if (i == SHEET_TITLE.length - 2) { // 方法参数列宽度设置
                            sheet.setColumnWidth(i, 100 * 256);
                        } else if (i == SHEET_TITLE.length - 1) { // 调用堆栈列宽度设置
                            sheet.setColumnWidth(i, 120 * 256);
                        } else {
                            sheet.setColumnWidth(i, 25 * 256);
                        }
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(titleStyle);
                        cell.setCellValue(SHEET_TITLE[i]);
                    }
                    // 冻结首行
                    sheet.createFreezePane(0, 1, 0, 1);
                    rowIndex++;
                    Log.d(TAG, "excel写入，创建文件：" + fileName + "，创建表头");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (workbook != null && sheet != null) {
                filePathMap.put(fileName, filePath);
                workbookMap.put(fileName, workbook);
                sheetMap.put(fileName, sheet);
            }
        }

        if (workbook == null || sheet == null) {
            Log.e(TAG, "excel写入，中止：workbook or sheet 为空");
            return;
        }

        Row row;
        final int startRow = rowIndex;

        // 上下居中、左右居中、自动换行
        CellStyle contentStyle1 = workbook.createCellStyle();
        contentStyle1.setAlignment(HorizontalAlignment.CENTER);
        contentStyle1.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle1.setWrapText(true);
        contentStyle1.setBorderLeft(BorderStyle.THIN);
        contentStyle1.setBorderTop(BorderStyle.THIN);
        contentStyle1.setBorderRight(BorderStyle.THIN);
        contentStyle1.setBorderBottom(BorderStyle.THIN);
        contentStyle1.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle1.setTopBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle1.setRightBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle1.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        // 上下居中、左右居左、自动换行
        CellStyle contentStyle2 = workbook.createCellStyle();
        contentStyle2.setAlignment(HorizontalAlignment.LEFT);
        contentStyle2.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle2.setWrapText(true);
        contentStyle2.setBorderLeft(BorderStyle.THIN);
        contentStyle2.setBorderTop(BorderStyle.THIN);
        contentStyle2.setBorderRight(BorderStyle.THIN);
        contentStyle2.setBorderBottom(BorderStyle.THIN);
        contentStyle2.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle2.setTopBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle2.setRightBorderColor(IndexedColors.BLACK.getIndex());
        contentStyle2.setBottomBorderColor(IndexedColors.BLACK.getIndex());

        for (int rowNum = 0; rowNum < apiCallRecordList.size(); rowNum++) {
            row = sheet.createRow(startRow + rowNum);
            // 自动行高
            row.setHeightInPoints((short) -1);

            ApiCallRecord bean = apiCallRecordList.get(rowNum);
            for (int j = 0; j < SHEET_TITLE.length; j++) {
                Cell cell = row.createCell(j);
                switch (j) {
                    case 0: // 触发序号
                        cell.setCellValue(bean.serialNumber);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 1:// 触发时间
                        cell.setCellValue(bean.timestamp);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 2: // 行为主体
                        cell.setCellValue(bean.invokerName);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 3: // 行为分类
                        cell.setCellValue(bean.invokerCategory);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 4: // 行为描述
                        cell.setCellValue(bean.invokerRule);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 5: // 触发进程
                        cell.setCellValue(bean.invokerProcess);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 6:// 触发方法
                        cell.setCellValue(bean.invokerMethod);
                        cell.setCellStyle(contentStyle1);
                        break;
                    case 7:// 方法参数
                        cell.setCellValue(bean.invokerMethodArgs);
                        cell.setCellStyle(contentStyle2);
                        break;
                    case 8:// 调用堆栈
                        cell.setCellValue(bean.invokerStack);
                        cell.setCellStyle(contentStyle2);
                        break;
                }
            }
            rowIndex++;
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(filePath)) {
            workbook.write(fileOutputStream);
            Log.d(TAG, "excel写入，数据写入成功");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "excel写入，数据写入出错");
        }
    }

    public void clear() {
        if (filePathMap != null) {
            filePathMap.clear();
        }
        if (workbookMap != null) {
            workbookMap.clear();
        }
        if (sheetMap != null) {
            sheetMap.clear();
        }
    }

}
