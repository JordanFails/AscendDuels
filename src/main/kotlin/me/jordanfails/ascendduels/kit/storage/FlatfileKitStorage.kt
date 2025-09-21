package me.jordanfails.ascendduels.kit.storage

import me.jordanfails.ascendduels.AscendDuels
import net.pvpwars.core.util.LogUtil
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.kit.KitService
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.function.Consumer

class FlatfileKitStorage(private val kitService: KitService) : KitStorage {
    private val file: File = File(AscendDuels.instance.dataFolder, "kits.json")

    public override fun loadKits() {
        try {
            if (file.exists()) {
                val lines = FileUtils.readLines(file)
                lines.forEach(Consumer { line: String? ->
                    val kit = Kit()
                    kit.deserialize(line)
                    kitService.add(kit)
                })
            } else file.createNewFile()
            //            try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
//                String line;
//                while((line = reader.readLine()) != null) {
//                    Kit kit = new Kit(line);
//                    kitService.add(kit);
//                }
//            }
        } catch (ex: Exception) {
            LogUtil.error("There was an error loading the kits: {0}", ex.message)
            ex.printStackTrace()
        }
    }

    public override fun saveKits() {
        try {
            PrintWriter(FileWriter(file)).use { writer ->
                kitService.all().stream().filter(Kit::isComplete)
                    .forEach { kit: Kit? -> writer.println(kit!!.serializeToString()) }
                writer.flush()
            }
        } catch (ex: Exception) {
            LogUtil.error("There was an error saving the kits: {0}", ex.message)
            ex.printStackTrace()
        }
    }
}