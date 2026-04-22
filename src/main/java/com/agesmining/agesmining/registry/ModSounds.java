package com.agesmining.agesmining.registry;

import com.agesmining.agesmining.AgesMining;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
        DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AgesMining.MOD_ID);

    public static final RegistryObject<SoundEvent> CAVE_IN =
        SOUNDS.register("cave_in", () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, "cave_in")));

    public static final RegistryObject<SoundEvent> CAVE_WARNING =
        SOUNDS.register("cave_warning", () -> SoundEvent.createVariableRangeEvent(
            ResourceLocation.fromNamespaceAndPath(AgesMining.MOD_ID, "cave_warning")));

    private ModSounds() {}
}
