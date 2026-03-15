package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AgesMining.MOD_ID);

    public static final RegistryObject<SoundEvent> CAVE_IN =
        SOUNDS.register("cave_in", () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(AgesMining.MOD_ID, "cave_in")));

    public static final RegistryObject<SoundEvent> CAVE_WARNING =
        SOUNDS.register("cave_warning", () -> SoundEvent.createVariableRangeEvent(
            new ResourceLocation(AgesMining.MOD_ID, "cave_warning")));

    private ModSounds() {}
}
