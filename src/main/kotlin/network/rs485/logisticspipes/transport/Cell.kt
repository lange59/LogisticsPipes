/*
 * Copyright (c) 2019  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2019  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.logisticspipes.transport

import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Identifier
import network.rs485.logisticspipes.init.Registries
import network.rs485.logisticspipes.util.SerializableKey
import network.rs485.logisticspipes.util.TypedMutableMap
import network.rs485.logisticspipes.util.TypedMutableMapAccess
import java.util.*

/**
 * A container flowing through a pipe.
 */
class Cell<out T : CellContent>(
        val content: T,
        val id: UUID = UUID.randomUUID(),
        val data: TypedMutableMap = TypedMutableMap()
) : TypedMutableMapAccess {

    fun getSpeedFactor() = 1.0f

    fun patch(tag: CompoundTag) {
        val newId = tag.getUuid("id")
        if (newId != id) error("This data is for another cell (expected ID $id, got ID $newId")

        val type = Identifier(tag.getString("type"))
        if (content.getType() != Registries.CellContentType[type])
            error("Can't change cell type (expected type ${Registries.CellContentType.getId(content.getType())}, got type $type)")

        content.fromTag(tag.getCompound("content"))
    }

    override operator fun <T : Any> get(key: SerializableKey<T>) = data[key]

    override operator fun <T : Any> set(key: SerializableKey<T>, t: T?) {
        data[key] = t
    }

    fun toTag(tag: CompoundTag = CompoundTag()): CompoundTag {
        tag.putUuid("id", id)
        val typeId = Registries.CellContentType.getId(content.getType())
                ?: error("Unregistered pipe content type ${content.getType()}!")
        tag.putString("type", typeId.toString())
        tag.put("content", content.toTag())
        tag.put("data", data.toTag())
        return tag
    }

    companion object {
        fun fromTag(tag: CompoundTag): Cell<*>? {
            val id = tag.getUuid("id")
            val typeId = tag.getString("type")
            val type = Registries.CellContentType[Identifier(typeId)] ?: return null
            val content = type.create()
            content.fromTag(tag.getCompound("content"))
            val extraData = TypedMutableMap.fromTag(tag.getCompound("data"))
            return Cell(content, id, extraData)
        }
    }

}