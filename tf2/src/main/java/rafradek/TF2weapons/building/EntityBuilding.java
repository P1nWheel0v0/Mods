package rafradek.TF2weapons.building;

import java.util.UUID;

import com.google.common.base.Optional;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import rafradek.TF2weapons.ClientProxy;
import rafradek.TF2weapons.TF2ConfigVars;
import rafradek.TF2weapons.TF2EventsCommon;
import rafradek.TF2weapons.TF2Sounds;
import rafradek.TF2weapons.TF2Util;
import rafradek.TF2weapons.TF2weapons;
import rafradek.TF2weapons.characters.EntityEngineer;
import rafradek.TF2weapons.characters.IEntityTF2;
import rafradek.TF2weapons.weapons.ItemSapper;

public class EntityBuilding extends EntityCreature implements IEntityOwnable, IEntityTF2 {

	private static final DataParameter<Byte> VIS_TEAM = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> LEVEL = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.BYTE);
	private static final DataParameter<Byte> SOUND_STATE = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.BYTE);
	private static final DataParameter<Integer> PROGRESS = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.VARINT);
	private static final DataParameter<Integer> CONSTRUCTING = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.VARINT);
	private static final DataParameter<Byte> SAPPED = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.BYTE);
	protected static final DataParameter<Optional<UUID>> OWNER_UUID = EntityDataManager.createKey(EntityBuilding.class,
			DataSerializers.OPTIONAL_UNIQUE_ID);
	
	public EntityLivingBase owner;
	public BuildingSound buildingSound;
	public int wrenchBonusTime;
	public float wrenchBonusMult;
	public ItemStack sapper=ItemStack.EMPTY;
	public EntityLivingBase sapperOwner;
	public boolean playerOwner;
	public boolean redeploy;
	public String ownerName;

	public int ticksNoOwner;
	private boolean engMade;
	
	public EntityBuilding(World worldIn) {
		super(worldIn);
		this.applyTasks();
		this.setHealth(0.1f);
		// this.notifyDataManagerChange(LEVEL);
	}

	public EntityBuilding(World worldIn, EntityLivingBase owner) {
		this(worldIn);
		this.setOwner(owner);
	}

	public void applyTasks() {

	}

	@Override
	public boolean canBePushed() {
		return false;
	}

	@Override
	public void applyEntityCollision(Entity entityIn) {
		if (entityIn.getEntityBoundingBox().intersects(this.getCollisionBoundingBox()))
			super.applyEntityCollision(entityIn);
	}

	@Override
	public void notifyDataManagerChange(DataParameter<?> key) {
		this.adjustSize();
		
		// System.out.println("Watcher update: "+data);
		if (!this.world.isRemote && key == CONSTRUCTING) {
			this.setSoundState(this.dataManager.get(CONSTRUCTING) >= this.getConstructionTime()? 0 : 25);
		}
		if (this.world.isRemote && key == SOUND_STATE) {
			SoundEvent sound = this.getSoundNameForState(this.getSoundState());
			if (sound != null) {
				// System.out.println("Playing Sound: "+sound);
				if (this.buildingSound != null)
					this.buildingSound.stopPlaying();
				this.buildingSound = new BuildingSound(this, sound, this.getSoundState());
				ClientProxy.playBuildingSound(buildingSound);
			}
			else{
				if(this.buildingSound != null)
					this.buildingSound.stopPlaying();
			}
		}
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) {
		if (!this.world.isRemote && player == this.getOwner() && hand == EnumHand.MAIN_HAND) {
			this.grab();
			return true;
		}
		return false;
	}

	public void grab() {
		if(!this.isDisabled()) {
			ItemStack stack = new ItemStack(TF2weapons.itemBuildingBox, 1,
					(this instanceof EntitySentry ? 18 : (this instanceof EntityDispenser ? 20 : 22)) + this.getEntTeam());
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setTag("SavedEntity", new NBTTagCompound());
			this.writeEntityToNBT(stack.getTagCompound().getCompoundTag("SavedEntity"));
			this.entityDropItem(stack, 0);
			// System.out.println("Saved:
			// "+stack.getTagCompound().getCompoundTag("SavedEntity"));
			this.setDead();
		}
	}

	public void adjustSize() {

	}

	public boolean isPotionApplicable(PotionEffect potioneffectIn)
    {
		return potioneffectIn.getPotion() == TF2weapons.stun;
    }
	public SoundEvent getSoundNameForState(int state) {
		return state == 50 ? TF2Sounds.MOB_SAPPER_IDLE : null;
	}

	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16D*TF2ConfigVars.damageMultiplier);
		this.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1D);
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0D);
	}

	@Override
	public boolean canBreatheUnderwater() {
		return true;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		if (source == DamageSource.ON_FIRE)
			return false;
		return super.attackEntityFrom(source, amount);
	}

	@Override
	public void setFire(int time) {
		super.setFire(0);
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		this.dataManager.register(VIS_TEAM, (byte) this.rand.nextInt(2));
		this.dataManager.register(OWNER_UUID, Optional.<UUID>absent());
		this.dataManager.register(LEVEL, (byte) 1);
		this.dataManager.register(SOUND_STATE, (byte) 25);
		this.dataManager.register(PROGRESS, 0);
		this.dataManager.register(SAPPED, (byte) 0);
		this.dataManager.register(CONSTRUCTING, 0);
		this.adjustSize();
	}

	public int getSoundState() {
		return this.dataManager.get(SOUND_STATE);
	}

	public void setSoundState(int state) {
		this.dataManager.set(SOUND_STATE, (byte) state);
	}

	@Override
	public UUID getOwnerId() {
		// TODO Auto-generated method stub
		return this.dataManager.get(OWNER_UUID).orNull();
	}

	@Override
	public EntityLivingBase getOwner() {
		// TODO Auto-generated method stub
		if (this.owner != null && !(this.owner instanceof EntityPlayer && this.owner.isDead))
			return this.owner;
		else if (this.getOwnerId() != null)
			return this.owner = this.world.getPlayerEntityByUUID(this.getOwnerId());
		// System.out.println("owner: "+this.getOwnerId());
		return null;
	}

	public void setOwner(EntityLivingBase owner) {
		// TODO Auto-generated method stub
		this.owner = owner;
		if (owner instanceof EntityPlayer){
			this.ownerName = owner.getName();
			this.dataManager.set(OWNER_UUID, Optional.of(owner.getUniqueID()));
			this.enablePersistence();
		}
		else if(owner != null)
			this.engMade = true;
	}

	@Override
	public void onUpdate() {
		long nanoTimeStart=System.nanoTime();
		this.motionX = 0;
		this.motionZ = 0;

		if (!this.world.isRemote && this.engMade && this.getOwnerId() == null && (this.owner == null || this.owner.isDead) && this.ticksNoOwner++ >= 120)
			this.setHealth(0);
		else
			this.ticksNoOwner = 0;
		if (this.motionY > 0)
			this.motionY = 0;
		if (!this.world.isRemote && this.isSapped())
			TF2Util.dealDamage(this, this.world, this.sapperOwner, this.sapper, 0,
					this.sapper.isEmpty() ? 0.14f
							: ((ItemSapper) this.sapper.getItem()).getWeaponDamage(sapper, this.sapperOwner, this),
					TF2Util.causeDirectDamage(this.sapper, this.sapperOwner, 0));
		super.onUpdate();
		if(this.isConstructing())
			this.updateConstruction();
		this.wrenchBonusTime--;
		if(!this.world.isRemote) {
			TF2EventsCommon.tickTimeOther[TF2weapons.server.getTickCounter()%20]+=System.nanoTime()-nanoTimeStart;
		}
	}

	public void setSapped(EntityLivingBase owner, ItemStack sapper) {
		this.sapperOwner = owner;
		this.sapper = sapper;
		this.dataManager.set(SAPPED, (byte) 2);
		this.setSoundState(50);
	}
	
	public boolean isAIDisabled()
    {
        return super.isAIDisabled() || this.isDisabled();
    }
	
	public boolean isSapped() {
		return this.dataManager.get(SAPPED) > 0;
	}

	public boolean isDisabled() {
		return this.isConstructing() || this.isSapped() || this.getActivePotionEffect(TF2weapons.stun) != null;
	}

	public void removeSapper() {
		dataManager.set(SAPPED, (byte) (dataManager.get(SAPPED) - 1));
		if (!isSapped()) {
			this.setSoundState(0);
			this.playSound(TF2Sounds.MOB_SAPPER_DEATH, 1.5f, 1f);
			this.dropItem(Items.IRON_INGOT, 1);
		}
	}

	@Override
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		return ((entityIn!=null && !TF2Util.isOnSameTeam(entityIn, this)) || entityIn==this.getOwner()) &&this.isEntityAlive() ? entityIn.getEntityBoundingBox() : null;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox() {
		if (!this.isEntityAlive())
			return null;
		/*
		 * else if(this.height>this.getCollHeight()){ AxisAlignedBB
		 * colBox=this.getEntityBoundingBox();
		 * colBox=colBox.grow((this.getCollWidth()-this.width)/2,
		 * (this.getCollHeight()-this.height)/2,
		 * (this.getCollWidth()-this.width)/2); colBox=colBox.offset(0,
		 * this.getEntityBoundingBox().minY-colBox.minY, 0); return colBox; }
		 */
		return this.getEntityBoundingBox();
	}

	@Override
	public Team getTeam() {
		if(this.getOwner() != null) {
			return this.getOwner().getTeam();
		}
		else if(this.getOwnerId() != null){
			return this.world.getScoreboard().getPlayersTeam(this.ownerName);
		}
		return this.getEntTeam() == 0 ? this.world.getScoreboard().getTeam("RED")
						: this.world.getScoreboard().getTeam("BLU");
	}

	public int getProgress() {
		if (this.isConstructing())
			return (int) (((float)this.dataManager.get(CONSTRUCTING)/this.getConstructionTime())*200);
		else
			return this.dataManager.get(PROGRESS);
	}
	
	public void setProgress(int progress) {
		this.dataManager.set(PROGRESS, progress);
	}

	public int getLevel() {
		return this.dataManager.get(LEVEL);
	}

	public void setLevel(int level) {
		this.dataManager.set(LEVEL, (byte) level);
	}

	public void upgrade() {
		this.setLevel(this.getLevel() + 1);
		this.setProgress(0);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
				.setBaseValue(this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() * 1.2);
		this.setHealth(this.getMaxHealth());
		this.adjustSize();
	}

	public int getEntTeam() {
		return this.dataManager.get(VIS_TEAM);
	}

	public void setEntTeam(int team) {
		this.dataManager.set(VIS_TEAM, (byte) team);
	}

	public boolean isConstructing() {
		return this.dataManager.get(CONSTRUCTING)<this.getConstructionTime();
	}

	public void setConstructing(boolean constr) {

		this.dataManager.set(CONSTRUCTING, constr?0:this.getConstructionTime());
	}
	
	public void updateConstruction() {
		if(!this.redeploy)
			this.heal((this.getConstructionRate()*this.getMaxHealth())/this.getConstructionTime());
		this.dataManager.set(CONSTRUCTING, this.dataManager.get(CONSTRUCTING)+this.getConstructionRate());
		if(this.redeploy && this.dataManager.get(CONSTRUCTING)>=this.getConstructionTime())
			this.redeploy=false;
	}
	/*@Override
	public boolean writeToNBTOptional(NBTTagCompound tagCompund) {
		return this.getOwnerId() != null ? super.writeToNBTOptional(tagCompund) : false;
	}*/

	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setByte("Team", (byte) this.getEntTeam());
		par1NBTTagCompound.setByte("Level", (byte) this.getLevel());
		par1NBTTagCompound.setShort("Progress", (byte) this.getProgress());
		par1NBTTagCompound.setShort("Sapper", this.dataManager.get(SAPPED));
		par1NBTTagCompound.setShort("Construction", this.dataManager.get(CONSTRUCTING).shortValue());
		par1NBTTagCompound.setByte("WrenchBonus", (byte) this.wrenchBonusTime);
		par1NBTTagCompound.setBoolean("Redeploy", this.redeploy);
		par1NBTTagCompound.setBoolean("EngMade", this.engMade);
		par1NBTTagCompound.setByte("TicksOwnerless", (byte) this.ticksNoOwner);
		if (this.getOwnerId() != null) {
			par1NBTTagCompound.setUniqueId("Owner", this.getOwnerId());
			par1NBTTagCompound.setString("OwnerName", this.ownerName);
		}
		if (this.isDisabled())
        {
			par1NBTTagCompound.setBoolean("NoAI", false);
        }
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);

		this.setEntTeam(tag.getByte("Team"));
		this.setLevel(tag.getByte("Level"));
		this.setProgress(tag.getByte("Progress"));
		this.dataManager.set(CONSTRUCTING, (int)tag.getShort("Construction"));
		this.wrenchBonusTime=tag.getByte("WrenchBonus");
		this.redeploy=tag.getBoolean("Redeploy");
		this.ticksNoOwner=tag.getByte("Ownerless");
		this.engMade=tag.getBoolean("EngMade");
		if (tag.getByte("Sapper") != 0)
			this.setSapped(this, ItemStack.EMPTY);
		
		if (tag.hasUniqueId("Owner")) {
			UUID ownerID = tag.getUniqueId("Owner");
			this.dataManager.set(OWNER_UUID, Optional.of(ownerID));
			this.ownerName = tag.getString("OwnerName");
			this.getOwner();
			this.enablePersistence();
		}
	}

	public float getCollHeight() {
		return 1f;
	}

	public float getCollWidth() {
		return 0.95f;
	}

	public boolean canUseWrench() {
		return this.getMaxHealth() > this.getHealth() || this.getLevel() < 3;
	}
	public boolean canBeHitWithPotion()
    {
        return false;
    }
	protected float updateDistance(float p_110146_1_, float p_110146_2_)
    {
		this.renderYawOffset=this.rotationYaw;
		return p_110146_2_;
    }
	@Override
	protected void dropFewItems(boolean p_70628_1_, int p_70628_2_) {
		EntityLivingBase attacker=this.getAttackingEntity();
		if (TF2Util.isOnSameTeam(attacker, this) && this.getOwnerId() == null)
			return;
		for (int i = 0; i < this.getIronDrop(); i++)
			this.dropItem(Items.IRON_INGOT, 1);
	}

	public int getIronDrop() {
		return 1 + this.getLevel();
	}
	
	@Override
	protected boolean canDespawn() {
		return this.getOwnerId() == null;
	}
	
	@SideOnly(Side.CLIENT)
	public void renderGUI(BufferBuilder renderer, Tessellator tessellator, EntityPlayer player, int width, int height, GuiIngame gui) {
		
	}
	
	public int getGuiHeight() {
		return 48;
	}
	
	public int getConstructionTime() {
		return 21000;
	}
	
	public int getConstructionRate() {
		int i=50;
		if(this.wrenchBonusTime>0)
			i+=75 * this.wrenchBonusMult;
		if(this.redeploy)
			i+=100;
		if(this.getOwner() != null && this.getOwner() instanceof EntityEngineer)
			i+=125;
		//System.out.println("Constr: "+i);
		return i;
	}
}
