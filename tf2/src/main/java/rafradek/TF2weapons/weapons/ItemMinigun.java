package rafradek.TF2weapons.weapons;

import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.ItemFromData;
import rafradek.TF2weapons.TF2Attribute;
import rafradek.TF2weapons.TF2Util;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.WeaponData.PropertyType;

public class ItemMinigun extends ItemBulletWeapon {

	public static UUID slowdownUUID = UUID.fromString("12843092-A5D6-BBCD-3D4F-A3DD4D8C94C8");
	public static AttributeModifier slowdown = new AttributeModifier(slowdownUUID, "minigun slowdown", -0.5D, 2);

	public ItemMinigun() {
		super();
		this.addPropertyOverride(new ResourceLocation("spin"), new IItemPropertyGetter() {
			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
				if (entityIn != null && entityIn.hasCapability(TF2weapons.WEAPONS_CAP, null) && entityIn.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks > 0)
					return 1;
				return 0;
			}
		});
	}

	@Override
	public void onUpdate(ItemStack par1ItemStack, World par2World, Entity par3Entity, int par4, boolean par5) {
		super.onUpdate(par1ItemStack, par2World, par3Entity, par4, par5);
		WeaponsCapability cap = par3Entity.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (par5 && par1ItemStack.getTagCompound() != null)
			// System.out.println("EntityTicked" + cap.state+ par3Entity);
			if ((cap.state == 0 || cap.state == 4) && cap.chargeTicks > 0) {
			// System.out.println("Draining" + cap.chargeTicks);
				cap.killsSpinning=0;
				cap.chargeTicks -= 2;
				((EntityLivingBase) par3Entity).getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
			}
	}

	/*
	 * public void addInformation(ItemStack par1ItemStack, EntityPlayer
	 * par2EntityPlayer, List par2List, boolean par4) {
	 * super.addInformation(par1ItemStack, par2EntityPlayer, par2List, par4); if
	 * (par1ItemStack.hasTagCompound()) {
	 * par2List.add("minigun: "+Integer.toString(par1ItemStack.getTagCompound().
	 * getShort("minigunticks"))); } }
	 */
	@Override
	public float getWeaponDamage(ItemStack stack, EntityLivingBase living, Entity target) {
		return super.getWeaponDamage(stack, living, target)*(living != null?TF2Util.lerp(0.5f,1f,living.getCapability(TF2weapons.WEAPONS_CAP, null).minigunTicks/20f):1f);
	}
	
	@Override
	public float getWeaponSpreadBase(ItemStack stack, EntityLivingBase living) {
		return super.getWeaponSpreadBase(stack, living)*(living != null?TF2Util.lerp(1.5f,1f,living.getCapability(TF2weapons.WEAPONS_CAP, null).minigunTicks/20f):1f);
	}
	
	@Override
	public boolean startUse(ItemStack stack, EntityLivingBase living, World world, int oldState, int newState) {
		if (world.isRemote && oldState == 0
				&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 3))
			// ResourceLocation playSound=new
			// ResourceLocation(getData(stack).get("Wind Up
			// Sound").getString());
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.WIND_UP_SOUND), false, 3,
					stack);
		return false;
	}

	@Override
	public boolean endUse(ItemStack stack, EntityLivingBase living, World world, int action, int newState) {
		WeaponsCapability cap=living.getCapability(TF2weapons.WEAPONS_CAP, null);
		if (newState == 0)
			cap.minigunTicks = 0;
		if (world.isRemote && newState == 0 && cap.chargeTicks > 0 && (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 4))
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.WIND_DOWN_SOUND), false, 4,
					stack);
		return false;
	}

	@Override
	public boolean fireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (world.isRemote && this.canFire(world, living, stack)) {
			if (TF2Util.calculateCritPre(stack, living) != 2
					&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 0))
				ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.FIRE_LOOP_SOUND), true, 0,
						stack);
			else if (TF2Util.calculateCritPre(stack, living) == 2
					&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type != 1)) {
				ResourceLocation playSoundCrit = new ResourceLocation(
						ItemFromData.getData(stack).getString(PropertyType.FIRE_LOOP_SOUND) + ".crit");

				ClientProxy.playWeaponSound(living, SoundEvent.REGISTRY.getObject(playSoundCrit), true, 1, stack);
			}
		}
		// System.out.println("nie");
		this.spinMinigun(stack, living, world);
		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn,
			EnumHand hand) {
		return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
	}

	@Override
	public boolean altFireTick(ItemStack stack, EntityLivingBase living, World world) {
		if (world.isRemote && this.canFire(world, living, stack)
				&& (!ClientProxy.fireSounds.containsKey(living) || ClientProxy.fireSounds.get(living).type > 2))
			ClientProxy.playWeaponSound(living, ItemFromData.getSound(stack, PropertyType.SPIN_SOUND), true, 2, stack);
		/*
		 * System.out.println("start"); ResourceLocation playSound=new
		 * ResourceLocation(getData(stack).get("Spin Sound").getString());
		 * MinigunLoopSound sound=new MinigunLoopSound(playSound, living, false,
		 * getData(stack),false);
		 * Minecraft.getMinecraft().getSoundHandler().playSound(sound);
		 * MapList.spinSounds.put(sound, living); }
		 */
		if ((living.getCapability(TF2weapons.WEAPONS_CAP, null).state & 1) != 1)
			this.spinMinigun(stack, living, world);
		return false;
	}

	public void spinMinigun(ItemStack stack, final EntityLivingBase living, World world) {
		if (super.canFire(world, living, stack)) {
			WeaponsCapability cap = living.getCapability(TF2weapons.WEAPONS_CAP, null);
			
			float ammo=TF2Attribute.getModifier("Ammo Spinned", stack, 0, living);
			int spinuptime=(int) TF2Attribute.getModifier("Minigun Spinup", stack, 18, living);
			
			if(cap.chargeTicks >= spinuptime || (living instanceof EntityPlayer && ((EntityPlayer) living).isCreative())) {
				if(cap.minigunTicks<20)
					cap.minigunTicks+=1;
				if( ammo > 0 && !ItemAmmo.searchForAmmo(living, stack).isEmpty()) {
					if ((living.ticksExisted % (20/ammo)) == 0) {
						ItemAmmo.consumeAmmoGlobal(living, stack, 1);
					}
				}
				
				if(living.ticksExisted % 10 == 0) {
					
					float flamedmg=TF2Attribute.getModifier("Ring Fire", stack, 0, living);
					if(flamedmg > 0) {
						if(world.isRemote ) {
							for(int i=0;i<50;i++)
								ClientProxy.spawnFlameParticle(world, living, 0, true);
						}
						else {
							for(EntityLivingBase target:world.getEntitiesWithinAABB(EntityLivingBase.class, living.getEntityBoundingBox().grow(4, -0.5, 4).offset(0, -0.5, 0), new Predicate<EntityLivingBase>() {
		
								@Override
								public boolean apply(EntityLivingBase input) {
									// TODO Auto-generated method stub
									return input != living && TF2Util.canHit(living, input) && input.getDistanceSqToEntity(living)<16;
								}
								
							})){
								
								TF2Util.dealDamage(target, world, living, stack, 0, flamedmg, TF2Util.causeDirectDamage(stack, living, 0).setFireDamage());
								TF2Util.igniteAndAchievement(target, living, 7);
							}
						}
					}
					
				}
			}
			if (living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(slowdownUUID) == null)
				living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(slowdown);
			if (living.isSprinting()){
				living.motionX *= 0.6D;
                living.motionZ *= 0.6D;
                living.setSprinting(false);
			}
			if(world.isRemote)
				ClientProxy.removeSprint();
			
			if (cap.fire1Cool <= 0 && cap.chargeTicks < spinuptime)
				cap.chargeTicks += 1;
		}
	}

	@Override
	public boolean canFire(World world, EntityLivingBase living, ItemStack stack) {
		return super.canFire(world, living, stack)
				&& ((living.getCapability(TF2weapons.WEAPONS_CAP, null).chargeTicks >= (int)TF2Attribute
						.getModifier("Minigun Spinup", stack, 18, living))
						|| (living instanceof EntityPlayer && ((EntityPlayer) living).isCreative()));
	}

	@Override
	public void holster(WeaponsCapability cap, ItemStack stack, EntityLivingBase living, World world) {
		super.holster(cap, stack, living, world);
		living.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(slowdown);
	}
	public void onDealDamage(ItemStack stack, EntityLivingBase attacker, Entity target, DamageSource source, float amount) {
		super.onDealDamage(stack, attacker, target, source, amount);
		if(attacker instanceof EntityPlayer && !target.isEntityAlive() && target instanceof EntityLivingBase && TF2Util.isEnemy(attacker, (EntityLivingBase) target)){
			attacker.getCapability(TF2weapons.WEAPONS_CAP, null).killsSpinning++;
			/*if(attacker.getCapability(TF2weapons.WEAPONS_CAP, null).killsSpinning>=8)
				((EntityPlayer)attacker).addStat(TF2Achievements.REVOLUTION);*/
		}
	}
	
	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return super.showDurabilityBar(stack) || (TF2Attribute.getModifier("Knockback Rage", stack, 0, null) != 0 
				&& Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).getKnockbackRage() < 1f);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return TF2Attribute.getModifier("Knockback Rage", stack, 0, null)==1 ? (1D - Minecraft.getMinecraft().player.getCapability(TF2weapons.WEAPONS_CAP, null).getKnockbackRage())/1D 
				: super.getDurabilityForDisplay(stack);
	}
}
