package com.natsu.pride.common.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

public class ParryCapabilityProvider implements ICapabilitySerializable<CompoundTag>{

	private final LazyOptional<IParryCapability> instance = LazyOptional.of(ParryCapability::new);
	public static final Capability<IParryCapability> PARRY_CAP = CapabilityManager.get(new CapabilityToken<>() {});
	
	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		return PARRY_CAP.orEmpty(cap, instance);
	}

	@Override
	public CompoundTag serializeNBT() {
		return instance.orElseThrow(NullPointerException::new).serializeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		instance.orElseThrow(NullPointerException::new).deserialize(nbt);
	}

	public void invalidate() {
		instance.invalidate();
	}
	
}
