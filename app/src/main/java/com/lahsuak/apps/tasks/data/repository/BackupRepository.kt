package com.lahsuak.apps.tasks.data.repository

import android.net.Uri
import android.util.Log
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.model.SubTask
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.util.AppConstants.BACKUP
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.COLOR
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.COMPLETED
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.CSV_SUBTASK_FILE_NAME
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.CSV_TASK_FILE_NAME
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.DATE_TIME
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.END_DATE
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.ID
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.IMP
import com.lahsuak.apps.tasks.util.AppConstants.DEFAULT_LINE_END
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.PROGRESS
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.REMINDER
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.SID
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.START_DATE
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.SUBTASKS
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.TASK_DIR
import com.lahsuak.apps.tasks.util.AppConstants.BackUpRepo.TITLE
import com.lahsuak.apps.tasks.util.AppConstants.RESTORE
import com.lahsuak.apps.tasks.util.CsvUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class BackupRepository(
    private val database: TaskDatabase,
    private val mutex: Mutex,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) {

    val context = TaskApp.appContext
    suspend fun export(uri: Uri) {
        withContext(dispatcher + scope.coroutineContext) {
            mutex.withLock {
                try {
                    // create a backup folder in the cache dir
                    val backupDir = File(context.externalCacheDir, BACKUP)
                    // if the backup directory already exists, delete it
                    if (backupDir.exists()) backupDir.deleteRecursively()
                    // create a new backup directory
                    backupDir.mkdir()
                    // creates a csv writer for writing the notes to a csv file in the backup directory
                    val csvTaskWriter =
                        CsvUtil.Writer(FileWriter(File(backupDir, CSV_TASK_FILE_NAME)))
                    val csvSubTaskWriter =
                        CsvUtil.Writer(FileWriter(File(backupDir, CSV_SUBTASK_FILE_NAME)))
                    // write the headers to the csv file
                    csvTaskWriter.writeNext(
                        arrayOf(
                            ID,
                            TITLE,
                            COMPLETED,
                            IMP,
                            REMINDER,
                            PROGRESS,
                            SUBTASKS,
                            COLOR,
                            START_DATE,
                            END_DATE
                        )
                    )
                    // write the notes to the csv file
                    database.dao.getAllTaskByName("").first().forEach { task ->
                        // write the image to the backup directory if it exists
                        val subList = if (task.subTaskList != null) {
                            task.subTaskList!!.replace(DEFAULT_LINE_END, context.packageName)
                        } else
                            ""
                        csvTaskWriter.writeNext(
                            arrayOf(
                                task.id.toString(),
                                task.title,
                                task.isDone.toString(),
                                task.isImp.toString(),
                                task.reminder?.toString() ?: "",
                                task.progress.toString(),
                                subList,
                                task.color.toString(),
                                task.startDate.toString(),
                                task.endDate?.toString() ?: ""
                            )
                        )
                    }
                    // close the csv writer
                    csvTaskWriter.close()
                    csvSubTaskWriter.writeNext(
                        arrayOf(
                            ID,
                            SID,
                            TITLE,
                            COMPLETED,
                            IMP,
                            REMINDER,
                            DATE_TIME
                        )
                    )
                    database.dao.getAllTaskByName("").first().forEach { task ->
                        database.dao.getAllSubTaskByName(task.id, "").first()
                            .forEach { subTask ->
                                if (task.subTaskList != null) {
                                    csvSubTaskWriter.writeNext(
                                        arrayOf(
                                            subTask.id.toString(),
                                            subTask.sId.toString(),
                                            subTask.subTitle,
                                            subTask.isDone.toString(),
                                            subTask.isImportant.toString(),
                                            subTask.reminder?.toString() ?: "",
                                            subTask.dateTime.toString(),
                                        )
                                    )
                                }
                            }
                    }
                    csvSubTaskWriter.close()

                    // create a zip file containing the csv and the images
                    ZipOutputStream(
                        BufferedOutputStream(context.contentResolver.openOutputStream(uri))
                    ).use { zip ->
                        backupDir.listFiles()?.forEach { file ->
                            zip.putNextEntry(ZipEntry(file.name))
                            file.inputStream().copyTo(zip)
                            zip.closeEntry()
                        }
                    }
                    // delete the backup directory
                    backupDir.deleteRecursively()
                } catch (e: Exception) {
                    Log.e(BackupRepository::class.simpleName, "Export", e)
                }
            }
        }
    }

    suspend fun import(uri: Uri) {
        withContext(dispatcher + scope.coroutineContext) {
            mutex.withLock {
                try {
                    val restoreDir = File(context.externalCacheDir, RESTORE)
                    // delete the restore directory if it already exists
                    if (restoreDir.exists()) restoreDir.deleteRecursively()
                    // create a new restore directory
                    restoreDir.mkdir()
                    // extract the zip file to the restore directory
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        ZipInputStream(BufferedInputStream(stream)).use { zip ->
                            var entry = zip.nextEntry
                            while (entry != null) {
                                val file = File(restoreDir, entry.name)
//                                val canonicalPath = file.canonicalPath
//                                if (!canonicalPath.startsWith(restoreDir.name)) {
//                                    throw Exception(
//                                        String.format(
//                                            "Found Zip Path Traversal Vulnerability with %s",
//                                            canonicalPath
//                                        )
//                                    )
//                                }
                                file.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                entry = zip.nextEntry
                            }
                        }
                    }
                    // open the notes csv file
                    val csvSubTaskReader =
                        CsvUtil.Reader(FileReader(File(restoreDir, CSV_SUBTASK_FILE_NAME)))
                    val csvTaskReader =
                        CsvUtil.Reader(FileReader(File(restoreDir, CSV_TASK_FILE_NAME)))
                    // read all the lines and discard the headers
                    val rowsSubTask = csvSubTaskReader.rows().drop(1)
                    val rows = csvTaskReader.rows().drop(1)
                    // close the csv reader
                    csvSubTaskReader.close()
                    csvTaskReader.close()
                    // clear the database to remove all the existing notes
                    database.dao.deleteAllTask()
                    // delete all images from the cache directory
                    File(context.externalCacheDir, TASK_DIR).deleteRecursively()
                    // import the notes from the csv file into the database
                    rows.forEach { columns ->
                        val id = columns[0].toInt()
                        val title = columns[1]
                        val isDone = columns[2].toBoolean()
                        val isImp = columns[3].toBoolean()
                        val reminder = if (columns[4] != "") {
                            columns[4].toLong()
                        } else {
                            null
                        }
                        val progress = columns[5].toFloat()
                        val subTaskList = if (columns[6] != "") {
                            columns[6].replace(context.packageName, DEFAULT_LINE_END)
                        } else null
                        val color = columns[7].toInt()
                        val startDate = columns[8].toLong()
                        val endDate = if (columns[9] != "") columns[9].toLong() else null
                        val task = Task(
                            id,
                            title,
                            isDone,
                            isImp,
                            reminder,
                            progress,
                            subTaskList,
                            color,
                            startDate,
                            endDate
                        )
                        database.dao.insert(task)
                    }
                    rows.forEach { columns ->
                        val id = columns[0].toInt()
                        val subTaskList = columns[6]
                        if (subTaskList.isEmpty().not()) {
                            database.dao.deleteAllSubTask(id)
                        }
                    }
                    rowsSubTask.forEach { colm ->
                        val taskId = colm[0].toInt()
                        val sId = colm[1].toInt()
                        val subTitle = colm[2]
                        val isSubTaskDone = colm[3].toBoolean()
                        val isImportant = colm[4].toBoolean()
                        val subTaskReminder = if (colm[5] != "") {
                            colm[5].toLong()
                        } else {
                            null
                        }
                        val datetime = colm[6].toLong()
                        val subTask = SubTask(
                            id = taskId,
                            subTitle = subTitle,
                            isDone = isSubTaskDone,
                            isImportant = isImportant,
                            sId = sId,
                            dateTime = datetime,
                            reminder = subTaskReminder
                        )
                        database.dao.insertSubTask(subTask)
                    }

                    // delete the restore directory
                    restoreDir.deleteRecursively()
                } catch (e: Exception) {
                    Log.e(BackupRepository::class.simpleName, "Import", e)
                }
            }
        }
    }
}