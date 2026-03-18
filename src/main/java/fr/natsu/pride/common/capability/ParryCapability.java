package fr.natsu.pride.common.capability;

import net.minecraft.nbt.CompoundTag;

public class ParryCapability implements IParryCapability {

	public static final int PARRY_COOLDOWN_TICKS = 0;		// In case i want to add a cooldown
	private boolean parry = false;
	private int cd = 0;
	
	@Override
	public boolean isParrying() { return parry; }

	@Override
	public void setParrying(boolean parry) { this.parry = parry; }

	@Override
	public int getCooldown() { return this.cd; }

	@Override
	public void setCooldown(int ticks) { this.cd = ticks; }

	@Override
	public void tick() {
		if (this.cd > 0) this.cd--;
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean("parrying", parry);
		tag.putInt("cooldown", cd);
		return tag;
	}

	@Override
	public void deserialize(CompoundTag tag) {
		this.parry = tag.getBoolean("parrying");
		this.cd = tag.getInt("cooldown");
	}

}
