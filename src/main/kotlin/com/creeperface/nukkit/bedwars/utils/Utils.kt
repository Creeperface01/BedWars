package com.creeperface.nukkit.bedwars.utils

import cn.nukkit.block.Block
import cn.nukkit.blockentity.BlockEntity
import cn.nukkit.utils.DyeColor
import cn.nukkit.utils.TextFormat

private val RGB_CONVERTER = arrayOf(
        1908001,
        11546150,
        6192150,
        8606770,
        3949738,
        8991416,
        1481884,
        10329495,
        4673362,
        15961002,
        8439583,
        16701501,
        3847130,
        13061821,
        16351261,
        16383998
)

val DyeColor.rgb: Int
    get() = RGB_CONVERTER[this.ordinal]

operator fun TextFormat.plus(any: Any) = this.toString() + any

val Block.blockEntity: BlockEntity
    get() = this.level.getBlockEntity(this)