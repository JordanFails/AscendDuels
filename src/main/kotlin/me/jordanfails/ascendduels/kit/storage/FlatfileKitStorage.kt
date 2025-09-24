package me.jordanfails.ascendduels.kit.storage

import me.jordanfails.ascendduels.AscendDuels
import net.pvpwars.core.util.LogUtil
import me.jordanfails.ascendduels.kit.Kit
import me.jordanfails.ascendduels.kit.KitService
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.util.function.Consumer

class FlatfileKitStorage(private val kitService: KitService) : KitStorage {
    private val file: File = File(AscendDuels.instance.dataFolder, "kits.json")

    public override fun loadKits() {
        try {
            if (file.exists()) {
                val content = file.readText(StandardCharsets.UTF_8)
                if (content.trim().isNotEmpty()) {
                    // Try to parse as a JSON array first
                    try {
                        val gson = com.google.gson.Gson()
                        val trimmedContent = content.trim()
                        
                        // Check if it's already an array
                        if (trimmedContent.startsWith("[")) {
                            val jsonArray = gson.fromJson(trimmedContent, com.google.gson.JsonArray::class.java)
                            
                            for (element in jsonArray) {
                                try {
                                    val kit = Kit()
                                    kit.deserialize(element.asJsonObject)
                                    kitService.add(kit)
                                } catch (ex: Exception) {
                                    LogUtil.error("Failed to load kit from JSON element: {0}", ex.message)
                                }
                            }
                        } else {
                            // Single JSON object - wrap it in an array
                            val jsonObject = gson.fromJson(trimmedContent, com.google.gson.JsonObject::class.java)
                            val kit = Kit()
                            kit.deserialize(jsonObject)
                            kitService.add(kit)
                        }
                    } catch (ex: Exception) {
                        // If array parsing fails, try line-by-line parsing (legacy format)
                        LogUtil.info("Failed to parse as JSON array, trying line-by-line parsing...")
                        val lines = FileUtils.readLines(file)
                        var currentJson = StringBuilder()
                        var braceCount = 0
                        
                        for (line in lines) {
                            val trimmedLine = line.trim()
                            if (trimmedLine.isEmpty()) continue
                            
                            currentJson.append(line).append("\n")
                            
                            // Count braces to determine when we have a complete JSON object
                            for (char in line) {
                                when (char) {
                                    '{' -> braceCount++
                                    '}' -> braceCount--
                                }
                            }
                            
                            // If we have a complete JSON object, try to parse it
                            if (braceCount == 0 && currentJson.isNotEmpty()) {
                                try {
                                    val kit = Kit()
                                    kit.deserialize(currentJson.toString().trim())
                                    kitService.add(kit)
                                } catch (ex: Exception) {
                                    LogUtil.error("Failed to load kit from JSON block: {0}", ex.message)
                                }
                                currentJson = StringBuilder()
                            }
                        }
                    }
                }
            } else {
                file.createNewFile()
            }
        } catch (ex: Exception) {
            LogUtil.error("There was an error loading the kits: {0}", ex.message)
            ex.printStackTrace()
        }
    }

    public override fun saveKits() {
        try {
            val gson = com.google.gson.GsonBuilder()
                .setPrettyPrinting()
                .create()
            
            val jsonArray = com.google.gson.JsonArray()
            for (kit in kitService.all()) {
                if (kit.isComplete) {
                    jsonArray.add(kit.serialize())
                }
            }
            
            file.writeText(gson.toJson(jsonArray), StandardCharsets.UTF_8)
        } catch (ex: Exception) {
            LogUtil.error("There was an error saving the kits: {0}", ex.message)
            ex.printStackTrace()
        }
    }
}