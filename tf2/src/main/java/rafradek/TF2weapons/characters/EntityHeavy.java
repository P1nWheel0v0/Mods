package rafradek.TF2weapons.characters;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2weapons;

public class EntityHeavy extends EntityTF2Character {

	public EntityHeavy(World par1World) {
		super(par1World);
		if (this.attack != null) {
			this.moveAttack.setDodge(true, true);
			this.attack.dodgeSpeed = 1.25f;
		}
		this.rotation = 10;
		//this.ammoLeft = 133;
		this.experienceValue = 15;
		this.setSize(0.6F, 1.99F);
		// this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND,
		// ItemUsable.getNewStack("Minigun"));

	}

	protected void addWeapons() {
		super.addWeapons();
		float chance = this.rand.nextFloat();
		if (chance < 0.2f)
			this.refill.setStackInSlot(0, new ItemStack(TF2weapons.itemSandvich));
		else if(chance < 0.3f)
			this.refill.setStackInSlot(0, new ItemStack(TF2weapons.itemChocolate));
	}
	@Override
	protected ResourceLocation getLootTable() {
		return TF2weapons.lootHeavy;
	}

	public float[] getDropChance() {
		return new float[] { 0.045f, 0.12f, 0.11f };
	}
	
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(40.0D);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30.0D);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.5D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.265D);
		this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(6.0D);
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();
		if (this.getAmmo() > 0 && this.getAttackTarget() != null
				&& this.getDistanceSqToEntity(this.getAttackTarget()) <= 350
				&& (this.getCapability(TF2weapons.WEAPONS_CAP, null).state & 2) == 0)
			this.getCapability(TF2weapons.WEAPONS_CAP, null).state += 2;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return TF2Sounds.MOB_HEAVY_SAY;
	}

	/**
	 * Returns the sound this mob makes when it is hurt.
	 */
	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return TF2Sounds.MOB_HEAVY_HURT;
	}

	/**
	 * Returns the sound this mob makes on death.
	 */
	@Override
	protected SoundEvent getDeathSound() {
		return TF2Sounds.MOB_HEAVY_DEATH;
	}

	/**
	 * Get this Entity's EnumCreatureAttribute
	 */
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		if (this.rand.nextFloat() < 0.15f + p_70628_2_ * 0.075f)
			this.entityDropItem(ItemFromData.getNewStack("shotgun"), 0);
		if (this.rand.nextFloat() < 0.05f + p_70628_2_ * 0.025f)
			this.entityDropItem(ItemFromData.getNewStack("minigun"), 0);
	}

	@Override
	public float getAttributeModifier(String attribute) {
		if (shouldScaleAttributes()){
			if (attribute.equals("Minigun Spinup"))
				return this.getDiff() == 1 ? 2f : (this.getDiff() == 3 ? 1.2f : 1.55f);
			if (attribute.equals("Damage"))
				return 0.9f;
		}
		return super.getAttributeModifier(attribute);
	}
}
