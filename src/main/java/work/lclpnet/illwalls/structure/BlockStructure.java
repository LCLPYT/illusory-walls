package work.lclpnet.illwalls.structure;

import work.lclpnet.illwalls.schem.SchematicAdapter;
import work.lclpnet.illwalls.schem.SchematicRegion;

public interface BlockStructure extends BlockStorage, SchematicRegion, SchematicAdapter {

    BlockStructure EMPTY = new EmptyStructure();
}
