package com.lahsuak.apps.tasks.data.repository

import android.net.Uri
import android.util.Log
import com.lahsuak.apps.tasks.TaskApp
import com.lahsuak.apps.tasks.data.db.TaskDatabase
import com.lahsuak.apps.tasks.data.model.Task
import com.lahsuak.apps.tasks.util.AppConstants.BACKUP
import com.lahsuak.apps.tasks.util.AppConstants.RESTORE
import com.lahsuak.apps.tasks.util.CsvUtil
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupRepository(
    private val database: TaskDatabase,
    private val mutex: Mutex,
    private val scope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
) {
    private companion object {
        const val ID = "ID"
        const val TITLE = "Title"
        const val COMPLETED = "Completed"
        const val IMP = "Imp"
        const val REMINDER = "Reminder"
        const val PROGRESS = "Progress"
        const val SUBTASKS = "SubTasks"
        const val COLOR = "Color"
        const val START_DATE = "StartDate"
        const val END_DATE = "EndDate"
        const val TASK_DIR = "tasks_dir"
        const val CSV_NAME = "tasks.csv"
    }

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
                    val csvWriter = CsvUtil.Writer(FileWriter(File(backupDir, CSV_NAME)))
                    // write the headers to the csv file
                    csvWriter.writeNext(
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
                    database.dao.getAllTaskByName("", false).first().forEach { task ->
                        // write the image to the backup directory if it exists
                        val subList = if (task.subTaskList != null) {
                            task.subTaskList!!.replace("\n", context.packageName)
                        } else
                            ""
                        csvWriter.writeNext(
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
                    csvWriter.close()
                    // create a zip file containing the csv and the images
                    ZipOutputStream(
                        BufferedOutputStream(
                            context.contentResolver.openOutputStream(uri)
                        )
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
                                file.outputStream().use { output ->
                                    zip.copyTo(output)
                                }
                                entry = zip.nextEntry
                            }
                        }
                    }
                    // open the notes csv file
                    val csvReader = CsvUtil.Reader(FileReader(File(restoreDir, CSV_NAME)))
                    // read all the lines and discard the headers
                    val rows = csvReader.rows().drop(1)
                    // close the csv reader
                    csvReader.close()
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
                            columns[6].replace(context.packageName, "\n")
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
                    // delete the restore directory
                    restoreDir.deleteRecursively()
                } catch (e: Exception) {
                    Log.e(BackupRepository::class.simpleName, "Import", e)
                }
            }
        }
    }
}