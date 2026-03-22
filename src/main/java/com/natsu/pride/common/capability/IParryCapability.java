package com.natsu.pride.common.capability;

import net.minecraft.nbt.CompoundTag;

public interface IParryCapability {

	boolean isParrying();
	void setParrying(boolean parry);
	int getCooldown();
	void setCooldown(int ticks);
	void tick();
	
	CompoundTag serializeNBT();
	void deserialize(CompoundTag tag);
}
