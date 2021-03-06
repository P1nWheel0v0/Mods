package rafradek.TF2weapons.projectiles;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2Util;
import rafradek.TF2weapons.TF2weapons;

public class EntityStickybomb extends EntityProjectileBase {

	
	public EntityStickybomb(World p_i1756_1_) {
		super(p_i1756_1_);
		this.setSize(0.3f, 0.3f);
	}

	public EntityStickybomb(World p_i1756_1_, EntityLivingBase p_i1756_2_, EnumHand hand) {
		super(p_i1756_1_, p_i1756_2_, hand);
		this.setSize(0.3f, 0.3f);
		this.setType((int) TF2Attribute.getModifier("Weapon Mode", this.usedWeapon, 0, p_i1756_2_));
	}

	@Override
	public float getPitchAddition() {
		return 3;
	}

	
	
	@Override
	protected void entityInit() {
		super.entityInit();
	}
	
	public int getArmTime() {
		return Math.round(TF2Attribute.getModifier("Arm Time", this.usedWeapon, 0.8f, this.shootingEntity)*20);
	}
	public boolean canBeCollidedWith()
    {
        return this.isSticked();
    }
	
	@Override
	public void onHitGround(int x, int y, int z, RayTraceResult mop) {

	}

	@Override
	public void onHitMob(Entity entityHit, RayTraceResult mop) {

	}

	public double maxMotion() {
		return Math.max(this.motionX, Math.max(this.motionY, this.motionZ));
	}

	@Override
	public void spawnParticles(double x, double y, double z) {

	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.shootingEntity == null || !this.shootingEntity.isEntityAlive())
			this.setDead();
	}

	@Override
	public void setDead() {
		super.setDead();
		if (!this.world.isRemote)
			this.shootingEntity.getCapability(TF2weapons.WEAPONS_CAP, null).activeBomb.remove(this);
	}

	@Override
	protected float getSpeed() {
		return 0.7667625f;
	}

	@Override
	public double getGravity() {
		return 0.0381f;
	}

	@Override
	public boolean isSticky() {
		return true;
	}

	@Override
	public boolean useCollisionBox() {
		return true;
	}
	
	@Override
	public int getMaxTime() {
		return 72000;
	}

	@Override
	public void onHitBlockX() {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
	}

	@Override
	public void onHitBlockY(Block block) {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
	}

	@Override
	public void onHitBlockZ() {
		this.motionX = 0;
		this.motionY = 0;
		this.motionZ = 0;
	}
	
	public boolean isGlowing()
    {
        return super.isGlowing() || (this.getType() == 1 && this.world.isRemote && this.shootingEntity==ClientProxy.getLocalPlayer() && this.ticksExisted >= this.getArmTime()&& TF2Util.lookingAt(this.shootingEntity, 30, this.posX, this.posY, this.posZ));
    }
}
