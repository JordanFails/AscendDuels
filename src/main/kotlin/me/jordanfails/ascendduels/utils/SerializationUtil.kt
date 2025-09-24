package net.pvpwars.duels.util

import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

object SerializationUtil {
    fun itemStackToBase64(itemStack: ItemStack?): String {
        try {
            ByteArrayOutputStream().use { outputStream ->
                BukkitObjectOutputStream(outputStream).use { dataOutput ->
                    dataOutput.writeObject(itemStack)
                    return Base64Coder.encodeLines(outputStream.toByteArray())
                }
            }
        } catch (ex: Exception) {
            throw IllegalStateException("Unable to save item stack.", ex)
        }
    }

    fun itemStackFromBase64(string: String): ItemStack? {
        try {
            BukkitObjectInputStream(ByteArrayInputStream(Base64Coder.decodeLines(string))).use { dataInput ->
                return dataInput.readObject() as ItemStack?
            }
        } catch (ex: Exception) {
            throw IllegalStateException("Unable to read item stack.", ex)
        }
    }

    @JvmStatic
    fun serialize(item: ItemStack?): String? {
        try {
            ByteArrayOutputStream().use { io ->
                BukkitObjectOutputStream(io).use { os ->
                    os.writeObject(item)
                    os.flush()

                    val serializedObject: ByteArray = io.toByteArray()
                    return Base64.getEncoder().encodeToString(serializedObject)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    @JvmStatic
    fun deserialize(serialized: String?): ItemStack? {
        try {
            val decodedBytes: ByteArray = Base64.getDecoder().decode(serialized)

            ByteArrayInputStream(decodedBytes).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { objectInputStream ->
                    return objectInputStream.readObject() as ItemStack
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }

        return null
    }


    /**
     * Serializes an ArrayList to Base64 string
     * @param list The ArrayList to serialize
     * @return Base64 encoded string or null if serialization fails
     */
    @JvmStatic
    fun serializeArrayList(list: ArrayList<*>?): String? {
        if (list == null) return null
        
        try {
            ByteArrayOutputStream().use { io ->
                BukkitObjectOutputStream(io).use { os ->
                    os.writeObject(list)
                    os.flush()
                    
                    val serializedObject: ByteArray = io.toByteArray()
                    return Base64.getEncoder().encodeToString(serializedObject)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Deserializes an ArrayList from Base64 string
     * @param serialized The Base64 encoded string
     * @return Deserialized ArrayList or null if deserialization fails
     */
    @JvmStatic
    fun deserializeArrayList(serialized: String?): ArrayList<*>? {
        if (serialized == null || serialized.isEmpty()) return null
        
        try {
            val decodedBytes: ByteArray = Base64.getDecoder().decode(serialized)
            
            ByteArrayInputStream(decodedBytes).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { objectInputStream ->
                    return objectInputStream.readObject() as ArrayList<*>
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        
        return null
    }

    /**
     * Serializes a MutableList to Base64 string
     * @param list The MutableList to serialize
     * @return Base64 encoded string or null if serialization fails
     */
    @JvmStatic
    fun serializeMutableList(list: MutableList<*>?): String? {
        if (list == null) return null
        
        try {
            ByteArrayOutputStream().use { io ->
                BukkitObjectOutputStream(io).use { os ->
                    os.writeObject(list)
                    os.flush()
                    
                    val serializedObject: ByteArray = io.toByteArray()
                    return Base64.getEncoder().encodeToString(serializedObject)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Deserializes a MutableList from Base64 string
     * @param serialized The Base64 encoded string
     * @return Deserialized MutableList or null if deserialization fails
     */
    @JvmStatic
    fun deserializeMutableList(serialized: String?): MutableList<*>? {
        if (serialized == null || serialized.isEmpty()) return null
        
        try {
            val decodedBytes: ByteArray = Base64.getDecoder().decode(serialized)
            
            ByteArrayInputStream(decodedBytes).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { objectInputStream ->
                    return objectInputStream.readObject() as MutableList<*>
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        
        return null
    }

    /**
     * Generic method to serialize any Collection to Base64 string
     * @param collection The Collection to serialize
     * @return Base64 encoded string or null if serialization fails
     */
    @JvmStatic
    fun serializeCollection(collection: Collection<*>?): String? {
        if (collection == null) return null
        
        try {
            ByteArrayOutputStream().use { io ->
                BukkitObjectOutputStream(io).use { os ->
                    os.writeObject(collection)
                    os.flush()
                    
                    val serializedObject: ByteArray = io.toByteArray()
                    return Base64.getEncoder().encodeToString(serializedObject)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Generic method to deserialize any Collection from Base64 string
     * @param serialized The Base64 encoded string
     * @return Deserialized Collection or null if deserialization fails
     */
    @JvmStatic
    fun deserializeCollection(serialized: String?): Collection<*>? {
        if (serialized == null || serialized.isEmpty()) return null
        
        try {
            val decodedBytes: ByteArray = Base64.getDecoder().decode(serialized)
            
            ByteArrayInputStream(decodedBytes).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { objectInputStream ->
                    return objectInputStream.readObject() as Collection<*>
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        
        return null
    }
}