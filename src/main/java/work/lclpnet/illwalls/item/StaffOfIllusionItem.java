package work.lclpnet.illwalls.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class StaffOfIllusionItem extends Item {

    public StaffOfIllusionItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
