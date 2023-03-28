package work.lclpnet.illwalls.schem.v2;

import work.lclpnet.illwalls.schem.SchematicAPI;
import work.lclpnet.illwalls.schem.SchematicReader;
import work.lclpnet.illwalls.schem.SchematicWriter;
import work.lclpnet.illwalls.structure.BlockStateGetter;
import work.lclpnet.illwalls.structure.DirectBlockStateGetter;

/**
 * A trimmed implementation of the <a href="https://github.com/SpongePowered/Schematic-Specification/blob/8e6be2d980d3bd794bc29df5fdca5921129fac5d/versions/schematic-2.md">Sponge schematic format, version 2</a>.
 * For comparison, check the implementation of <a href="https://github.com/EngineHub/WorldEdit/tree/df3f7b2ae66de0cf34215c012e7abe4fc61210fc/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/sponge">WorldEdit Core</a>.
 * This implementation is inspired by worldedit-core.
 */
public class SpongeSchematicV2 implements SchematicAPI {

    public static final int FORMAT_VERSION = 2;
    static final String
            SCHEMATIC = "Schematic",
            VERSION = "Version",
            DATA_VERSION = "DataVersion",
            WIDTH = "Width",
            HEIGHT = "Height",
            LENGTH = "Length",
            OFFSET = "Offset",
            PALETTE_MAX = "PaletteMax",
            PALETTE = "Palette",
            BLOCK_DATA = "BlockData",
            BLOCK_ENTITIES = "BlockEntities",
            BLOCK_ENTITY_ID = "Id",
            BLOCK_ENTITY_POS = "Pos";
    private static final Object mutex = new Object();
    private static volatile SpongeSchematicV2 instance = null;
    private final BlockStateGetter blockStateGetter = new DirectBlockStateGetter();

    private SpongeSchematicV2() {
    }

    public static SpongeSchematicV2 getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    instance = new SpongeSchematicV2();
                }
            }
        }
        return instance;
    }

    @Override
    public SchematicWriter writer() {
        return new Writer();
    }

    @Override
    public SchematicReader reader() {
        return new Reader(blockStateGetter);
    }
}
