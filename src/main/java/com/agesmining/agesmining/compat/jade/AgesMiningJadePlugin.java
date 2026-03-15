package com.agesmining.agesmining.compat.jade;

import net.minecraft.world.level.block.Block;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class AgesMiningJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(AgesMiningJadeSupportProvider.INSTANCE, Block.class);
    }
}
