package dolphin.android.apps.dsttranslate

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

abstract class PoHelper {
    companion object {
        private const val CHS_PO = "chinese_s.po"
        private const val CHT_PO = "chinese_t.po"
        const val DST_PO = "dst_cht.po"
    }

    protected val replaceList = ArrayList<String>()
    protected var replace3dot: String = ""
    private val replace6dot: String
        get() = "$replace3dot$replace3dot"
    protected var replaceLeftBracket: String = ""
    protected var replaceRightBracket: String = ""

    private val sourceMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from official simplified chinese po file
     */
    fun chs(key: String): WordEntry? = sourceMap[key]

    private val revisedMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from official traditional chinese po file
     */
    fun cht(key: String): WordEntry? = revisedMap[key]

    private val originMap = HashMap<String, WordEntry>()

    /**
     * @param key entry key
     * @return word entry from original map
     */
    fun dst(key: String): WordEntry? = originMap[key]

    /**
     * @return all word entry in original map
     */
    fun dstValues(): List<WordEntry> = originMap.map { entry -> entry.value }

    private val wordList = ArrayList<WordEntry>()

    /**
     * @return full word list entry
     */
    fun allValues(): List<WordEntry> = wordList

    /**
     * A debug log output implementation.
     *
     * @param message log message to standard output
     */
    protected abstract fun log(message: String)

    /**
     * Prepare the helper instance. Usually call this when init.
     */
    abstract fun prepare()

    protected fun loadFromReader(
        reader: BufferedReader,
        @Suppress("SameParameterValue") line2Enabled: Boolean = true
    ): ArrayList<WordEntry> {
        val list = ArrayList<WordEntry>()
        try {
            // do reading, usually loop until end of file reading
            var line: String? = ""//reader.readLine()
            while (line != null) {
                val line1 = reader.readLine()
                if (!line1.startsWith("#")) {
                    log("bypass $line1")
                    continue //bypass some invalid header
                }
                val line2 = if (line2Enabled) reader.readLine() else ""
                val line3 = reader.readLine()
                val line4 = reader.readLine()
                val entry = WordEntry.from(line1, line2, line3, line4)
                if (entry != null) {
                    list.add(entry)
                } else {
                    log("invalid input: $line1")
                }
                line = reader.readLine()
            }
        } catch (e: Exception) {
            log("Exception: ${e.message}")
        } finally {
            try {
                reader.close()
            } catch (e: Exception) {
                log("close: ${e.message}")
            }
        }
        return list
    }

    private fun writeEntryToFile(
        dst: File = getOutputFile(),
        list: ArrayList<WordEntry> = wordList
    ): Boolean {
        if (list.isEmpty()) return false // no list, don't write
        val writer: BufferedWriter?
        try { // http://stackoverflow.com/a/1053474
            writer = BufferedWriter(FileWriter(dst))
            var content = "\"Language: zh-tw\"\n\"POT Version: 2.0\"\n"
            writer.write(content, 0, content.length)
            list.forEach { entry ->
                content = "\n"
                content += "#. ${entry.key}\n"
                content += "msgctxt ${entry.text}\n"
                content += "msgid ${entry.id}\n"
                content += "msgstr ${entry.str}\n"
                writer.write(content, 0, content.length)
            }
            writer.close()
            // writer = null
        } catch (e: Exception) {
            // e.printStackTrace()
            log("writeStringToFile: ${e.message}")
            return false
        }
        log("write to ${dst.absolutePath} with ${dst.length()} done")
        return true
    }

    /**
     * Implementation of loading asset file to memory
     *
     * @param name asset name
     * @param line2Enabled true if line 2 is required
     * @return word entry list
     */
    abstract fun loadAssetFile(name: String, line2Enabled: Boolean = true): ArrayList<WordEntry>

//    fun runTranslation(postAction: ((timeCost: Long) -> Unit)? = null) {
//        val cost = runBlocking { runTranslationProcess() }
//        postAction?.let { action -> context.runOnUiThread { action(cost) } }
//    }

    private val processStatus = MutableStateFlow("")

    /**
     * Process status
     */
    val status: StateFlow<String> = processStatus

    /**
     * Loading status. True means the app is processing data.
     */
    val loading = MutableStateFlow(false)

    /**
     * Load chs and cht translation file to app.
     *
     * @return total process time
     */
    suspend fun runTranslationProcess(): Long = withContext(Dispatchers.IO) {
        log("run translation")
        loading.emit(true)
        val start = System.currentTimeMillis()

        processStatus.emit("load $CHS_PO")
        val s = loadAssetFile(CHS_PO)
        sourceMap.clear()
        s.forEach { entry ->
            // entry.str = sc2tc(entry.str).trim() // translate to traditional
            sourceMap[entry.key] = entry
        }
        val stop1 = System.currentTimeMillis()
        log("original SC size: ${s.size} (${stop1 - start} ms)")

        processStatus.emit("load $CHT_PO")
        val t = loadAssetFile(CHT_PO)
        revisedMap.clear()
        t.forEach { entry ->
            revisedMap[entry.key] = entry
        }
        val stop2 = System.currentTimeMillis()
        log("original TC size: ${t.size} (${stop2 - start} ms)")

        processStatus.emit("load $DST_PO")
        originMap.clear()
        loadAssetFile(DST_PO).filter { entry ->
            entry.id != "\"\"" && entry.str != "\"\""
        }.forEach { entry ->
            originMap[entry.key] = entry
        }
        val stop3 = System.currentTimeMillis()
        log("previous data size: ${originMap.size} (${stop3 - stop1} ms)")

        processStatus.emit("prepare word list")
        wordList.clear()
        s.forEachIndexed { index, entry ->
            var newly = false
            var str = ""
            if (originMap.containsKey(entry.key)) {
                val str1 = originMap[entry.key]?.str ?: ""
                if (str1.isNotEmpty()) str = str1.trim()
            } else {
                processStatus.emit("${entry.key} (${index + 1}/${s.size})")
            }
            if (str.isEmpty()) { // not in the translated po
                newly = true
                str = sc2tc(entry.str).trim()
            }
            str = getReplacement(str)
            wordList.add(WordEntry(entry.key, entry.text, entry.id, str, newly))
        }
        val stop4 = System.currentTimeMillis()
        log("new list size: ${wordList.size} (${stop4 - stop3} ms)")

        writeEntryToFile(getCachedFile(), wordList) // runTranslationProcess
        val cost = System.currentTimeMillis() - start
        log("write data done. $cost ms")
        processStatus.emit("")
        loading.emit(false) // complete
        return@withContext cost
    }

    /**
     * Write all word entries to a file
     *
     * @param dst destination file
     * @param list word entry list
     * @return true if file written is success
     */
    suspend fun writeTranslationFile(
        dst: File = getOutputFile(),
        list: ArrayList<WordEntry> = wordList
    ): Boolean = withContext(Dispatchers.IO) {
        loading.emit(true)
        val start = System.currentTimeMillis()
        val result = writeEntryToFile(dst, list) // writeTranslationFile
        val cost = System.currentTimeMillis() - start
        log("write data done. $cost ms")
        loading.emit(false) // complete
        return@withContext result
    }

    /**
     * @return actual output file
     */
    abstract fun getOutputFile(): File

    /**
     * @return cache file
     */
    abstract fun getCachedFile(): File

    /**
     * Convert simplified chinese to traditional chinese
     *
     * @param str simplified chinese text
     * @return traditional chinese text
     */
    abstract fun sc2tc(str: String): String

    private fun getReplacement(src: String): String {
        var str = src.replace("...", replace3dot)
        str = if (str != "\"$replace6dot\"") str.replace(replace6dot, replace3dot) else str
        replaceList.forEach {
            val pair = it.split("|")
            str = str.replace(pair[0], pair[1])
        }
        if (str.contains("\\\"")) {
            var i = 0
            val str1 = str.replace("\\\"", "%@%").replace("%@%".toRegex()) {
                if (i++ % 2 == 0) replaceLeftBracket else replaceRightBracket
            }
            if (i % 2 == 0) {
                str = str1 // only replace the paired string
            }
        }
        return str
    }

    /**
     * Build a list with changed entries
     *
     * @return word list with change items
     */
    fun buildChangeList(): List<WordEntry> = wordList.filter { entry ->
        val dst = dst(entry.key)
        val chs = chs(entry.key)
        (entry.newly || // new entry
                dst?.id != entry.id || // english text changed
                dst.str != entry.str || // translation changed
                entry.changed > 0 || // entry itself changed by editor
                chs?.id != dst.id) // source english text changed
                && entry.str.length > 2 && !entry.string().startsWith("only_used_by")
    }

    /**
     * Update text of specific word entry
     *
     * @param key entry key
     * @param value entry text
     */
    fun update(key: String, value: String) {
        wordList.find { entry -> entry.key == key }?.apply {
            str = value
            // println("set new $key to $str")
            changed = System.currentTimeMillis() // set new change time
        }
    }
}
