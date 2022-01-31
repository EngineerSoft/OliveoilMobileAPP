package com.mascir.oliveoilsensor.utils

import android.content.Context
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.*
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


class UtilsMethod(private val context: Context) {

    companion object {
        var nameOfFile = ""
        var spectroId = ""
    }

    fun transformMessage(message: String) : Pair<Int, List<Pair<Float, Float>>> {

            val stringPairs: MutableList<String> = message.trim().split("//").toMutableList()
            val testResult = stringPairs.removeAt(0)

            if (stringPairs[stringPairs.lastIndex] == "") stringPairs.removeAt(stringPairs.lastIndex)

            val pairs: List<Pair<Float, Float>> = stringPairs.map {
                val pair = it.split("-")
                return@map Pair(pair[0].toFloat(), pair[1].toFloat())
            }

            return Pair(testResult.toInt(), pairs)
    }

    fun saveReadingValue(result: Pair<Int, List<Pair<Float, Float>>>) : String {


        val dateTimeFormate = SimpleDateFormat("ddMMyyyy-HHmmss")
        val dateTime = dateTimeFormate.format(Date())
        spectroId = result.first.toString()

        nameOfFile = "${dateTime}.csv"
        val dir = File("${context.getExternalFilesDir(null)}/sensorDirectory")
        //Toast.makeText(context, "Begin saving...", Toast.LENGTH_LONG).show()

        if (!dir.exists()) dir.mkdir()
        val file = File(dir, nameOfFile)
        SharedPrefManager.getInstance(context).saveFile(nameOfFile)
        val os = file.outputStream()
        try {
            val builder = StringBuilder()
            //builder.append(result.first.toString() + "\n")
            builder.append("X,Y\r\n")
            result.second.forEach {
                builder.append("${it.first}, ${it.second}\r\n")
            }
            os.write(builder.toString().toByteArray())
//            Toast.makeText(context, file.name+" saved in internal storage", Toast.LENGTH_LONG).show()
            return file.name;

        } catch (ex: IOException) {
            ex.printStackTrace()
        } finally {
            os.close()
        }
        return "";
    }

    fun saveReadingValueXlsx(result: Pair<Int, List<Pair<Float, Float>>>) : String {

        val dateTimeFormate = SimpleDateFormat("ddMMyyyy-HHmmss")
        val dateTime = dateTimeFormate.format(Date())
        //spectroId = result.first.toString()

        nameOfFile = "${dateTime}.xlsx"
        val dir = File("${context.getExternalFilesDir(null)}/sensorDirectory")
        //Toast.makeText(context, "Begin saving...", Toast.LENGTH_LONG).show()

        if (!dir.exists()) dir.mkdir()
        val file = File(dir, nameOfFile)
        SharedPrefManager.getInstance(context).saveFile(nameOfFile)

        //creating an instance of XSSFWorkbook class
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Data Spectrum")

        //creating the 0th row using the createRow() method
        val rowhead: XSSFRow = sheet.createRow(0)
        rowhead.createCell(0).setCellValue("X")
        rowhead.createCell(1).setCellValue("Y")
        //creating cell by using the createCell() method and setting the values to the cell by using the setCellValue() method
        for(i in result.second.indices){
            val row: XSSFRow = sheet.createRow(i + 1)
            row.createCell(0).setCellValue("${result.second[i].first}")
            row.createCell(1).setCellValue("${result.second[i].second}")
        }

        // open an OutputStream to save written data into XLSX file
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream("$dir/$nameOfFile")
            workbook.write(os)
            println("Writing on XLSX file Finished...")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.name
    }

    fun readXlsxFile(filePath: String) : ArrayList<String> {
        val Tarray = ArrayList<String>()
        val file = File(filePath)
        val fis = FileInputStream(file)

        // Finds the workbook instance for XLSX file
        val myWorkBook = XSSFWorkbook(fis)

        // Return first sheet from the XLSX workbook
        val mySheet: XSSFSheet = myWorkBook.getSheetAt(0)

        // Get iterator to all the rows in current sheet
        val rowIterator: Iterator<Row> = mySheet.iterator()

        if (file.exists()) {
            // Traversing over each row of XLSX file
            while (rowIterator.hasNext()) {
                val row: Row = rowIterator.next()
                if (row.rowNum.equals(0)) {
                    continue
                }
                // For each row, iterate through each columns
                val cellIterator: Iterator<Cell> = row.cellIterator()
                while (cellIterator.hasNext()) {
                    val cell: Cell = cellIterator.next()
                    if(cell.columnIndex.equals(0)) continue
                    else Tarray.add(cell.stringCellValue)

                }
            }
        }
        return Tarray
    }

    fun createResultFile_1(resm1: Double?, resm2: Double?, resm3: Double?, resm4: Double?, spectroId: String): String {


        val dir = File("${context.getExternalFilesDir(null)}/resultDirectory")
        //Toast.makeText(context, "Begin saving...", Toast.LENGTH_LONG).show()

        if (!dir.exists()) dir.mkdir()

        SharedPrefManager.getInstance(context).saveFile(nameOfFile)
        val databaseDateTimeFormate = SimpleDateFormat("ddMMyyyy-HHmmss")
        val dateTime = databaseDateTimeFormate.format(Date())
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val date_time = simpleDateFormat.format(Date())

        nameOfFile = "${dateTime}.xlsx"
        val file = File(dir, nameOfFile)
        //creating an instance of XSSFWorkbook class
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Resultats")

        //creating the 0th row using the createRow() method
        val rowhead: XSSFRow = sheet.createRow(0)
        rowhead.createCell(0).setCellValue("ID Analyseur")
        rowhead.createCell(1).setCellValue("ResultatHOM5")
        //rowhead.createCell(2).setCellValue("ResultatHOM6")
        rowhead.createCell(2).setCellValue("ResultatHOM7")
        rowhead.createCell(3).setCellValue("ResultatHOM_Mixing11")
        rowhead.createCell(4).setCellValue("ResultatHOM_Minxig1")
        rowhead.createCell(5).setCellValue("Date de creation")

        val row1: XSSFRow = sheet.createRow(1)
        row1.createCell(0).setCellValue(spectroId)
        row1.createCell(1).setCellValue(decimalFormat(resm1!!))
        row1.createCell(2).setCellValue(decimalFormat(resm2!!))
        row1.createCell(3).setCellValue(decimalFormat(resm3!!))
        row1.createCell(4).setCellValue(decimalFormat(resm4!!))
        row1.createCell(5).setCellValue(date_time)

        // open an OutputStream to save written data into XLSX file
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream("$dir/$nameOfFile")
            workbook.write(os)
            println("Writing on XLSX file Finished...")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.name
    }

    fun createResultFile(resm1: Double?, resm2: Double?, resm3: Double?, spectroId: String): String {


        val dir = File("${context.getExternalFilesDir(null)}/resultDirectory")
        //Toast.makeText(context, "Begin saving...", Toast.LENGTH_LONG).show()

        if (!dir.exists()) dir.mkdir()

        SharedPrefManager.getInstance(context).saveFile(nameOfFile)
        val databaseDateTimeFormate = SimpleDateFormat("ddMMyyyy-HHmmss")
        val dateTime = databaseDateTimeFormate.format(Date())
        val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        val date_time = simpleDateFormat.format(Date())

        nameOfFile = "${dateTime}.xlsx"
        val file = File(dir, nameOfFile)
        //creating an instance of XSSFWorkbook class
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Resultats")

        //creating the 0th row using the createRow() method
        val rowhead: XSSFRow = sheet.createRow(0)
        rowhead.createCell(0).setCellValue("ID Analyseur")
        rowhead.createCell(1).setCellValue("ResultatHOM5")
        rowhead.createCell(2).setCellValue("ResultatHOM6")
        rowhead.createCell(3).setCellValue("ResultatHOM7")
        rowhead.createCell(4).setCellValue("Date de creation")

        val row1: XSSFRow = sheet.createRow(1)
        row1.createCell(0).setCellValue(spectroId)
        row1.createCell(1).setCellValue(decimalFormat(resm1!!))
        row1.createCell(2).setCellValue(decimalFormat(resm2!!))
        row1.createCell(3).setCellValue(decimalFormat(resm3!!))
        row1.createCell(4).setCellValue(date_time)

        // open an OutputStream to save written data into XLSX file
        var os: FileOutputStream? = null
        try {
            os = FileOutputStream("$dir/$nameOfFile")
            workbook.write(os)
            println("Writing on XLSX file Finished...")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.name
    }

    private fun decimalFormat(res: Double): String? {
        return DecimalFormat("##.##").format(res)
    }

}