package rafradek.TF2weapons.projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2Util;
import rafradek.TF2weapons.WeaponData.PropertyType;

public class EntityJar extends EntityProjectileBase {

	public EntityJar(World p_i1756_1_) {
		super(p_i1756_1_);
	}

	public EntityJar(World p_i1756_1_, EntityLivingBase p_i1756_2_, EnumHand hand) {
		super(p_i1756_1_, p_i1756_2_, hand);
	}

	@Override
	public void explode(double x, double y, double z, Entity direct, float power) {
		if (!this.world.isRemote) {
			int coatedCount=0;
			for (EntityLivingBase living : this.world.getEntitiesWithinAABB(EntityLivingBase.class,
					this.getEntityBoundingBox().grow(5, 5, 5)))
				if (living.canBeHitWithPotion() && living.getDistanceSqToEntity(this) < 25
						&& living != this.shootingEntity && !TF2Util.isOnSameTeam(this.shootingEntity, living)){
					living.addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation(
							ItemFromData.getData(this.usedWeapon).getString(PropertyType.EFFECT_TYPE)),
							300));
					if(TF2Util.isEnemy(this.shootingEntity,living))
						coatedCount++;
				}
				else
					living.extinguish();
			/*if(coatedCount>=5 && this.shootingEntity instanceof EntityPlayer)
				((EntityPlayer) this.shootingEntity).addStat(TF2Achievements.JARATE_MULTIPLE);*/
				
			this.playSound(TF2Sounds.JAR_EXPLODE, 1.5f, 1f);
			this.world.playEvent(2002, new BlockPos(this),
					Potion.getPotionFromResourceLocation(ItemFromData.getData(this.usedWeapon)
							.getString(PropertyType.EFFECT_TYPE)).getLiquidColor());
			this.setDead();
		}
	}

	@Override
	public void onHitGround(int x, int y, int z, RayTraceResult mop) {
		this.explode(mop.hitVec.x + mop.sideHit.getFrontOffsetX() * 0.05,
				mop.hitVec.y + mop.sideHit.getFrontOffsetY() * 0.05,
				mop.hitVec.z + mop.sideHit.getFrontOffsetZ() * 0.05, null, 1f);
	}

	@Override
	public void onHitMob(Entity entityHit, RayTraceResult mop) {
		this.explode(mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, mop.entityHit, 1f);
	}

	@Override
	public void onUpdate() {
		super.onUpdate();

	}

	@Override
	public void spawnParticles(double x, double y, double z) {
		if (this.isInWater())
			this.world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, x, y, z, this.motionX, this.motionY,
					this.motionZ);
	}

	@Override
	protected float getSpeed() {
		return 1.04f;
	}

}
