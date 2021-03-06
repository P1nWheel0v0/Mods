package rafradek.TF2weapons;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockOre;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.tileentity.BannerPattern;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.IFuelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.Mod.Metadata;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import rafradek.TF2weapons.WeaponData.PropertyType;
import rafradek.TF2weapons.boss.BlockProp;
import rafradek.TF2weapons.boss.EntityHHH;
import rafradek.TF2weapons.boss.EntityMerasmus;
import rafradek.TF2weapons.boss.EntityMonoculus;
import rafradek.TF2weapons.building.EntityDispenser;
import rafradek.TF2weapons.building.EntitySentry;
import rafradek.TF2weapons.building.EntityTeleporter;
import rafradek.TF2weapons.building.EntityTeleporter.TeleporterData;
import rafradek.TF2weapons.building.ItemBuildingBox;
import rafradek.TF2weapons.characters.ContainerMercenary;
import rafradek.TF2weapons.characters.EntityDemoman;
import rafradek.TF2weapons.characters.EntityEngineer;
import rafradek.TF2weapons.characters.EntityHeavy;
import rafradek.TF2weapons.characters.EntityMedic;
import rafradek.TF2weapons.characters.EntityPyro;
import rafradek.TF2weapons.characters.EntitySaxtonHale;
import rafradek.TF2weapons.characters.EntityScout;
import rafradek.TF2weapons.characters.EntitySniper;
import rafradek.TF2weapons.characters.EntitySoldier;
import rafradek.TF2weapons.characters.EntitySpy;
import rafradek.TF2weapons.characters.EntityStatue;
import rafradek.TF2weapons.characters.EntityTF2Character;
import rafradek.TF2weapons.characters.GuiMercenary;
import rafradek.TF2weapons.characters.ItemMonsterPlacerPlus;
import rafradek.TF2weapons.crafting.BlockAmmoFurnace;
import rafradek.TF2weapons.crafting.BlockCabinet;
import rafradek.TF2weapons.crafting.ContainerAmmoFurnace;
import rafradek.TF2weapons.crafting.ContainerTF2Workbench;
import rafradek.TF2weapons.crafting.GuiAmmoFurnace;
import rafradek.TF2weapons.crafting.GuiTF2Crafting;
import rafradek.TF2weapons.crafting.ItemTF2;
import rafradek.TF2weapons.crafting.OpenCrateRecipe;
import rafradek.TF2weapons.crafting.TileEntityAmmoFurnace;
import rafradek.TF2weapons.decoration.ContainerWearables;
import rafradek.TF2weapons.decoration.EntityTarget;
import rafradek.TF2weapons.decoration.GuiWearables;
import rafradek.TF2weapons.decoration.InventoryWearables;
import rafradek.TF2weapons.decoration.ItemStatue;
import rafradek.TF2weapons.loot.EntityBuildingFunction;
import rafradek.TF2weapons.loot.EntityOfClassFunction;
import rafradek.TF2weapons.loot.KilledByTeam;
import rafradek.TF2weapons.loot.RandomWeaponFunction;
import rafradek.TF2weapons.message.TF2ActionHandler;
import rafradek.TF2weapons.message.TF2AttackSyncHandler;
import rafradek.TF2weapons.message.TF2BulletHandler;
import rafradek.TF2weapons.message.TF2CapabilityHandler;
import rafradek.TF2weapons.message.TF2ContractHandler;
import rafradek.TF2weapons.message.TF2DisguiseHandler;
import rafradek.TF2weapons.message.TF2GuiConfigHandler;
import rafradek.TF2weapons.message.TF2InitClientHandler;
import rafradek.TF2weapons.message.TF2InitHandler;
import rafradek.TF2weapons.message.TF2Message;
import rafradek.TF2weapons.message.TF2NetworkWrapper;
import rafradek.TF2weapons.message.TF2ProjectileHandler;
import rafradek.TF2weapons.message.TF2PropertyHandler;
import rafradek.TF2weapons.message.TF2ShowGuiHandler;
import rafradek.TF2weapons.message.TF2UseHandler;
import rafradek.TF2weapons.message.TF2VelocityAddHandler;
import rafradek.TF2weapons.message.TF2WeaponDataHandler;
import rafradek.TF2weapons.message.TF2WeaponDropHandler;
import rafradek.TF2weapons.message.TF2WearableChangeHandler;
import rafradek.TF2weapons.message.udp.TF2UdpClient;
import rafradek.TF2weapons.message.udp.TF2UdpServer;
import rafradek.TF2weapons.projectiles.EntityBall;
import rafradek.TF2weapons.projectiles.EntityCleaver;
import rafradek.TF2weapons.projectiles.EntityFlame;
import rafradek.TF2weapons.projectiles.EntityFlare;
import rafradek.TF2weapons.projectiles.EntityGrenade;
import rafradek.TF2weapons.projectiles.EntityJar;
import rafradek.TF2weapons.projectiles.EntityProjectileSimple;
import rafradek.TF2weapons.projectiles.EntityRocket;
import rafradek.TF2weapons.projectiles.EntityStickybomb;
import rafradek.TF2weapons.projectiles.EntityStickProjectile;
import rafradek.TF2weapons.upgrade.BlockUpgradeStation;
import rafradek.TF2weapons.upgrade.ContainerUpgrades;
import rafradek.TF2weapons.upgrade.GuiUpgradeStation;
import rafradek.TF2weapons.upgrade.MannCoBuilding;
import rafradek.TF2weapons.upgrade.TileEntityUpgrades;
import rafradek.TF2weapons.weapons.InventoryAmmoBelt;
import rafradek.TF2weapons.weapons.ItemAmmo;
import rafradek.TF2weapons.weapons.ItemAmmoBelt;
import rafradek.TF2weapons.weapons.ItemAmmoPackage;
import rafradek.TF2weapons.weapons.ItemDisguiseKit;
import rafradek.TF2weapons.weapons.ItemFireAmmo;
import rafradek.TF2weapons.weapons.ItemHorn;
import rafradek.TF2weapons.weapons.WeaponsCapability;

@Mod(modid = "rafradek_tf2_weapons", name = "TF2 Stuff", version = "1.2.3", guiFactory = "rafradek.TF2weapons.TF2GuiFactory", dependencies = "after:dynamiclights", updateJSON="https://rafradek.github.io/tf2stuffmod.json")
public class TF2weapons {

	public static final String MOD_ID = "rafradek_tf2_weapons";
	@Metadata(MOD_ID)
	public static ModMetadata metadata;

	@CapabilityInject(WeaponsCapability.class)
	public static final Capability<WeaponsCapability> WEAPONS_CAP = null;

	@CapabilityInject(InventoryWearables.class)
	public static final Capability<InventoryWearables> INVENTORY_CAP = null;

	@CapabilityInject(InventoryAmmoBelt.class)
	public static final Capability<InventoryAmmoBelt> INVENTORY_BELT_CAP = null;
	
	@CapabilityInject(TF2EventsCommon.TF2WorldStorage.class)
	public static final Capability<TF2EventsCommon.TF2WorldStorage> WORLD_CAP = null;

	@CapabilityInject(WeaponData.WeaponDataCapability.class)
	public static final Capability<WeaponData.WeaponDataCapability> WEAPONS_DATA_CAP = null;
	
	@CapabilityInject(TF2PlayerCapability.class)
	public static final Capability<TF2PlayerCapability> PLAYER_CAP = null;
	
	public static TF2UdpServer udpServer;

	public int[] itemid = new int[9];
	public static Configuration conf;

	public static CreativeTabs tabutilitytf2;
	public static CreativeTabs tabweapontf2;
	public static CreativeTabs tabsurvivaltf2;
	// public static final ArmorMaterial OPARMOR =
	// EnumHelper.addArmorMaterial("OPARMOR", "", 1000, new int[] {24,0,0,0},
	// 100);
	public static TF2NetworkWrapper network;
	public static Item itemPlacer;
	public static Item mobHeldItem;

	static int weaponVersion;

	@Instance(value = MOD_ID)
	public static TF2weapons instance;

	public File weaponDir;

	public static Block blockCabinet;
	public static Block blockCopperOre;
	public static Block blockProp;
	public static Block blockLeadOre;
	public static Block blockAustraliumOre;
	public static Block blockAustralium;
	public static Block blockUpgradeStation;
	public static Block blockAmmoFurnace;
	public static Block blockOverheadDoor;

	public static boolean generateCopper;
	public static boolean generateLead;
	public static boolean generateAustralium;

	public static Potion bonk;
	public static Potion stun;
	public static Potion crit;
	public static Potion buffbanner;
	public static Potion backup;
	public static Potion conch;
	public static Potion markDeath;
	public static Potion jarate;
	public static Potion madmilk;
	public static Potion critBoost;
	public static Potion charging;
	public static Potion uber;
	public static Potion it;
	public static Potion bombmrs;
	public static Potion bleeding;

	public static Item itemDisguiseKit;
	public static Item itemBuildingBox;
	public static Item itemSandvich;
	public static Item itemChocolate;
	public static Item itemAmmo;
	public static Item itemAmmoFire;
	public static Item itemAmmoPackage;
	public static Item itemAmmoMedigun;
	public static Item itemAmmoBelt;
	public static Item itemScoutBoots;
	public static Item itemMantreads;
	public static Item itemTF2;
	public static Item itemHorn;
	public static Item itemStatue;
	
	public static ResourceLocation lootTF2Character;
	public static ResourceLocation lootScout;
	public static ResourceLocation lootSpy;
	public static ResourceLocation lootHeavy;
	public static ResourceLocation lootEngineer;
	public static ResourceLocation lootMedic;
	public static ResourceLocation lootPyro;
	public static ResourceLocation lootSoldier;
	public static ResourceLocation lootDemoman;
	public static ResourceLocation lootSniper;
	public static ResourceLocation lootHale;

	public static byte[] itemDataCompressed;
	public static GZIPOutputStream out;
	
	public static StatBase cratesOpened;
	public static MinecraftServer server;
	public static EntityLivingBase dummyEnt;
	
	public static BannerPattern redPattern;
	public static BannerPattern bluPattern;
	public static BannerPattern fastSpawn;
	public static ArrayList<ResourceLocation> animals;
	
	@SidedProxy(clientSide = "rafradek.TF2weapons.ClientProxy", serverSide = "rafradek.TF2weapons.CommonProxy")
	public static CommonProxy proxy;
	
	public static int getCurrentWeaponVersion() {
		return 28;
	}

	@Mod.EventHandler
	public void preinit(FMLPreInitializationEvent event) {

		this.weaponDir = new File(event.getModConfigurationDirectory(), "TF2WeaponsLists");
		if (!this.weaponDir.exists())
			this.weaponDir.mkdirs();
		metadata.autogenerated = false;

		conf = new Configuration(event.getSuggestedConfigurationFile());
		TF2ConfigVars.conf = conf;
		boolean shouldCopy = false;
		if (!conf.hasKey("internal", "Weapon Config Version"))
			shouldCopy = true;
		TF2ConfigVars.createConfig();
		File outputFile = new File(this.weaponDir, "Weapons.json");
		File outputFile2 = new File(this.weaponDir, "Cosmetics.json");
		File outputFile3 = new File(this.weaponDir, "Crates.json");
		File file = event.getSourceFile();
		// System.out.println("LOLOLOLOLOLOL "+file.getAbsolutePath());
		// System.out.println("LOLOLOLOLOLOL2
		// "+event.getModConfigurationDirectory());
		// System.out.println("Istnieje? "+outputFile.exists());
		if (weaponVersion < getCurrentWeaponVersion() || !file.isFile())
			shouldCopy = true;
		if (!outputFile.exists() || shouldCopy ) {
			conf.get("internal", "Weapon Config Version", getCurrentWeaponVersion()).set(getCurrentWeaponVersion());
			conf.save();

			if (file.isFile())
				try {
					ZipFile zip = new ZipFile(file);
					ZipEntry entry = zip.getEntry("Weapons.json");
					ZipEntry entryHats = zip.getEntry("Cosmetics.json");
					ZipEntry entryCrates = zip.getEntry("Crates.json");
					if (entry != null) {

						InputStream zin = zip.getInputStream(entry);
						byte[] bytes = new byte[(int) entry.getSize()];
						zin.read(bytes);
						FileOutputStream str = new FileOutputStream(outputFile);
						str.write(bytes);
						str.flush();
						str.close();
						zin.close();

					}
					if (entryHats != null) {

						InputStream zin = zip.getInputStream(entryHats);
						byte[] bytes = new byte[(int) entryHats.getSize()];
						zin.read(bytes);
						FileOutputStream str = new FileOutputStream(outputFile2);
						str.write(bytes);
						str.flush();
						str.close();
						zin.close();

					}
					if (entryCrates != null) {

						InputStream zin = zip.getInputStream(entryCrates);
						byte[] bytes = new byte[(int) entryCrates.getSize()];
						zin.read(bytes);
						FileOutputStream str = new FileOutputStream(outputFile3);
						str.write(bytes);
						str.flush();
						str.close();
						zin.close();

					}
					zip.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else
				try {
					File inputFile = new File(file, "Weapons.json");
					File inputFileHats = new File(file, "Cosmetics.json");
					File inputFileCrates = new File(file, "Crates.json");
					FileInputStream istr = new FileInputStream(inputFile);

					byte[] bytes = new byte[(int) inputFile.length()];
					istr.read(bytes);
					FileOutputStream str = new FileOutputStream(outputFile);
					str.write(bytes);
					str.flush();
					str.close();
					istr.close();

					istr = new FileInputStream(inputFileHats);

					bytes = new byte[(int) inputFileHats.length()];
					istr.read(bytes);
					str = new FileOutputStream(outputFile2);
					str.write(bytes);
					str.flush();
					str.close();
					istr.close();

					istr = new FileInputStream(inputFileCrates);

					bytes = new byte[(int) inputFileCrates.length()];
					istr.read(bytes);
					str = new FileOutputStream(outputFile3);
					str.write(bytes);
					str.flush();
					str.close();
					istr.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		bluPattern=EnumHelper.addEnum(BannerPattern.class, "BLU_PATTERN", new Class<?>[]{String.class,String.class}, "blu_base","bb");
		redPattern=EnumHelper.addEnum(BannerPattern.class, "RED_PATTERN", new Class<?>[]{String.class,String.class}, "red_base","rb");
		fastSpawn=EnumHelper.addEnum(BannerPattern.class, "FAST_SPAWN", new Class<?>[]{String.class,String.class}, "fast_spawn","fs");
		
		MapList.initMaps();
		TF2Attribute.initAttributes();
		
		/*File refFile=new File(event.getModConfigurationDirectory(), "TF2References");
		if(!refFile.exists())
			refFile.mkdirs();
		try {
			BufferedWriter attributes=new BufferedWriter(new FileWriter(new File(refFile,"attributes.txt")));
			attributes.write("ID - Name - Effect - State - Type - Default\n");
			for(int i=0;i<TF2Attribute.attributes.length;i++){
				TF2Attribute attr=TF2Attribute.attributes[i];
				if(attr != null){
					attributes.write(attr.id+" - "+attr.name+" - "+attr.effect+" - "+attr.state.toString()+" - "+attr.typeOfValue.toString()+" - "+attr.defaultValue+"\n");
				}
			}
			attributes.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		tabweapontf2 = new CreativeTabs("tf2weapons") {
			@Override
			public ItemStack getTabIconItem() {
				return ItemFromData.getNewStack("minigun");
			}
			
		};
		tabutilitytf2 = new CreativeTabs("tf2util") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(itemDisguiseKit);
			}
			
			@SideOnly(Side.CLIENT)
		    public void displayAllRelevantItems(NonNullList<ItemStack> list) {
				super.displayAllRelevantItems(list);
				for(int i=0;i<2;i++){
					for(int j=0;j<2;j++){
						ItemStack banner=new ItemStack(Items.BANNER,1,(i==0?EnumDyeColor.RED:EnumDyeColor.BLUE).getDyeDamage());
						NBTTagList patterns=new NBTTagList();
						banner.getOrCreateSubCompound("BlockEntityTag").setTag("Patterns", patterns);
						NBTTagCompound pattern=new NBTTagCompound();
						if(i==0){
							pattern.setString("Pattern", "rb");
							pattern.setInteger("Color", 15);
						}
						else{
							pattern.setString("Pattern", "bb");
							pattern.setInteger("Color", 15);
						}
						
						patterns.appendTag(pattern);
						if(j==1){
							NBTTagCompound patternfast=new NBTTagCompound();
							patternfast.setString("Pattern", "fs");
							patternfast.setInteger("Color", 15);
							patterns.appendTag(patternfast);
						}
						
						list.add(banner);
					}
				}
		    }
		};
		tabsurvivaltf2 = new CreativeTabs("tf2misc") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Item.getItemFromBlock(blockCabinet));
			}
		};
		// EntityRegistry.registerModEntity(EntityBullet.class, "bullet", 1,
		// this, 256, 100, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"heavy"),EntityHeavy.class, "heavy", 2, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"scout"),EntityScout.class, "scout", 3, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sniper"),EntitySniper.class, "sniper", 4, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"soldier"),EntitySoldier.class, "soldier", 5, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"pyro"),EntityPyro.class, "pyro", 6, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"demoman"),EntityDemoman.class, "demoman", 7, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"medic"),EntityMedic.class, "medic", 8, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"spy"),EntitySpy.class, "spy", 9, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"engineer"),EntityEngineer.class, "engineer", 10, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"rocket"),EntityRocket.class, "rocket", 11, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"flame"),EntityFlame.class, "flame", 12, this, 0, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"grenade"),EntityGrenade.class, "grenade", 13, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sticky"),EntityStickybomb.class, "sticky", 14, this, 64, 5, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"simple"),EntityProjectileSimple.class, "simple", 26, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"syringe"),EntityStickProjectile.class, "syringe", 15, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"flare"),EntityFlare.class, "flare", 20, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"jar"),EntityJar.class, "jar", 21, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"ball"),EntityBall.class, "ball", 22, this, 64, 10, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"sentry"),EntitySentry.class, "sentry", 16, this, 80, 2, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"dispenser"),EntityDispenser.class, "dispenser", 17, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"teleporter"),EntityTeleporter.class, "teleporter", 18, this, 80, 40, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"hale"),EntitySaxtonHale.class, "hale", 19, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"monoculus"),EntityMonoculus.class, "monoculus", 23, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"hhh"),EntityHHH.class, "hhh", 24, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"merasmus"),EntityMerasmus.class, "merasmus", 25, this, 80, 3, true);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"cleaver"),EntityCleaver.class, "cleaver", 27, this, 64, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"target"),EntityTarget.class, "target", 28, this, 80, 20, false);
		EntityRegistry.registerModEntity(new ResourceLocation(MOD_ID,"statue"),EntityStatue.class, "statue", 29, this, 80, 20, false);
		// GameRegistry.registerItem(new ItemArmor(TF2weapons.OPARMOR, 3,
		// 0).setUnlocalizedName("oparmor").setTextureName("diamond_helmet").setCreativeTab(tabtf2),"oparmor");
		ForgeRegistries.ITEMS.register(itemPlacer = new ItemMonsterPlacerPlus().setUnlocalizedName("monsterPlacer").setRegistryName(TF2weapons.MOD_ID + ":placer"));
		ForgeRegistries.ITEMS.register(itemDisguiseKit = new ItemDisguiseKit().setUnlocalizedName("disguiseKit").setRegistryName(TF2weapons.MOD_ID + ":disguise_kit"));
		ForgeRegistries.ITEMS.register(itemBuildingBox = new ItemBuildingBox().setUnlocalizedName("buildingBox").setRegistryName(TF2weapons.MOD_ID + ":building_box"));
		ForgeRegistries.ITEMS.register(itemSandvich = new ItemFood(14, 1, false).setPotionEffect(new PotionEffect(MobEffects.REGENERATION, 120, 2), 1f).setUnlocalizedName("sandvich")
				.setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID + ":sandvich"));
		ForgeRegistries.ITEMS.register(itemChocolate = new ItemFood(7, 0.6F, false).setPotionEffect(new PotionEffect(MobEffects.ABSORPTION, 600, 0), 1f).setUnlocalizedName("chocolate")
				.setCreativeTab(tabutilitytf2).setRegistryName(TF2weapons.MOD_ID + ":chocolate"));
		ForgeRegistries.ITEMS.register(itemHorn = new ItemHorn().setUnlocalizedName("horn").setRegistryName(TF2weapons.MOD_ID + ":horn"));
		ForgeRegistries.ITEMS.register(itemAmmo = new ItemAmmo().setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo"));
		ForgeRegistries.ITEMS.register(itemAmmoPackage = new ItemAmmoPackage().setUnlocalizedName("tf2ammobox").setRegistryName(TF2weapons.MOD_ID + ":ammo_box"));
		ForgeRegistries.ITEMS.register(itemAmmoFire = new ItemFireAmmo(10, 350).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_fire"));
		ForgeRegistries.ITEMS.register(itemAmmoMedigun = new ItemFireAmmo(12, 1400).setUnlocalizedName("tf2ammo").setRegistryName(TF2weapons.MOD_ID + ":ammo_medigun"));
		ForgeRegistries.ITEMS.register(itemAmmoBelt = new ItemAmmoBelt().setUnlocalizedName("ammoBelt").setRegistryName(TF2weapons.MOD_ID + ":ammo_belt").setCreativeTab(tabsurvivaltf2));
		ForgeRegistries.ITEMS.register(itemScoutBoots = new ItemArmorTF2(ArmorMaterial.LEATHER, 0, EntityEquipmentSlot.FEET,"Allows double jumping",0f)
				.setUnlocalizedName("scoutBoots").setRegistryName(TF2weapons.MOD_ID + ":scout_shoes").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemMantreads = new ItemArmorTF2(ArmorMaterial.IRON, 0, EntityEquipmentSlot.FEET,"Deals 1.8x falling damage to the player you land on",0.75f)
				.setUnlocalizedName("mantreads").setRegistryName(TF2weapons.MOD_ID + ":mantreads").setCreativeTab(tabutilitytf2));
		ForgeRegistries.ITEMS.register(itemStatue = new ItemStatue().setRegistryName(TF2weapons.MOD_ID +":statue"));
		// GameRegistry.register(itemCopperIngot=new
		// Item().setUnlocalizedName("ingotCopper").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotCopper"));
		// GameRegistry.register(itemLeadIngot=new
		// Item().setUnlocalizedName("ingotLead").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotLead"));
		// GameRegistry.register(itemAustraliumIngot=new
		// Item().setUnlocalizedName("ingotAustralium").setCreativeTab(tabtf2).setRegistryName(TF2weapons.MOD_ID+":ingotAustralium"));
		ForgeRegistries.ITEMS.register(itemTF2 = new ItemTF2().setRegistryName(TF2weapons.MOD_ID + ":itemTF2"));
		
		Iterator<String> iterator = MapList.weaponClasses.keySet().iterator();
		while (iterator.hasNext()) {
			String name = iterator.next();
			ForgeRegistries.ITEMS.register(MapList.weaponClasses.get(name).setRegistryName(new ResourceLocation(MOD_ID, "" + name.toLowerCase())));
		}

		GameRegistry.registerTileEntity(TileEntityUpgrades.class, "UpgradeStation");
		GameRegistry.registerTileEntity(TileEntityAmmoFurnace.class, "AmmoFurnace");

		registerBlock(blockCabinet = new BlockCabinet().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("cabinet"), TF2weapons.MOD_ID + ":tf2workbench");
		registerBlock(blockAmmoFurnace = new BlockAmmoFurnace().setHardness(5.0F).setResistance(10.0F).setUnlocalizedName("ammoFurnace"), TF2weapons.MOD_ID + ":ammo_furnace");
		registerBlock(blockUpgradeStation = new BlockUpgradeStation().setBlockUnbreakable().setResistance(10.0F).setUnlocalizedName("upgradeStation"),
				TF2weapons.MOD_ID + ":upgrade_station");
		registerBlock(blockCopperOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreCopper"),
				TF2weapons.MOD_ID + ":copper_ore");
		registerBlock(blockLeadOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(3.0F).setResistance(5.0F).setUnlocalizedName("oreLead"),
				TF2weapons.MOD_ID + ":lead_ore");
		registerBlock(blockAustraliumOre = new BlockOre().setCreativeTab(tabsurvivaltf2).setHardness(6.0F).setResistance(10.0F).setUnlocalizedName("oreAustralium"),
				TF2weapons.MOD_ID + ":australium_ore");
		registerBlock(blockAustralium = new Block(Material.IRON, MapColor.GOLD).setCreativeTab(tabsurvivaltf2).setHardness(9.0F).setResistance(20.0F)
				.setUnlocalizedName("blockAustralium"), TF2weapons.MOD_ID + ":australium_block");
		registerBlock(blockOverheadDoor = new BlockOverheadDoor().setCreativeTab(tabsurvivaltf2).setHardness(9.0F).setResistance(20.0F)
				.setUnlocalizedName("blockOverhead"), TF2weapons.MOD_ID + ":overhead_door");
		ForgeRegistries.BLOCKS.register(blockProp= new BlockProp(Material.WOOD, MapColor.GOLD).setHardness(1.0F).setResistance(2.0F)
				.setUnlocalizedName("blockProp").setRegistryName(TF2weapons.MOD_ID + ":prop_block"));

		OreDictionary.registerOre("oreCopper", blockCopperOre);
		OreDictionary.registerOre("oreLead", blockLeadOre);
		OreDictionary.registerOre("oreAustralium", blockAustraliumOre);
		OreDictionary.registerOre("blockAustralium", blockAustralium);
		OreDictionary.registerOre("ingotCopper", new ItemStack(itemTF2, 1, 0));
		OreDictionary.registerOre("ingotLead", new ItemStack(itemTF2, 1, 1));
		OreDictionary.registerOre("ingotAustralium", new ItemStack(itemTF2, 1, 2));
		OreDictionary.registerOre("nuggetAustralium", new ItemStack(itemTF2, 1, 9));

		blockCopperOre.setHarvestLevel("pickaxe", 1);
		blockLeadOre.setHarvestLevel("pickaxe", 1);
		blockAustraliumOre.setHarvestLevel("pickaxe", 2);

		ItemAmmo.STACK_FILL = new ItemStack(itemAmmo);
		CapabilityManager.INSTANCE.register(TF2PlayerCapability.class, new NullStorage<TF2PlayerCapability>(), new Callable<TF2PlayerCapability>() {

			@Override
			public TF2PlayerCapability call() throws Exception {
				// TODO Auto-generated method stub
				return new TF2PlayerCapability(null);
			}

		});
		CapabilityManager.INSTANCE.register(WeaponsCapability.class, new NullStorage<WeaponsCapability>(), new Callable<WeaponsCapability>() {

			@Override
			public WeaponsCapability call() throws Exception {
				// TODO Auto-generated method stub
				return new WeaponsCapability(null);
			}

		});
		CapabilityManager.INSTANCE.register(InventoryWearables.class, new NullStorage<InventoryWearables>(), new Callable<InventoryWearables>() {

			@Override
			public InventoryWearables call() throws Exception {
				// TODO Auto-generated method stub
				return new InventoryWearables(null);
			}

		});
		CapabilityManager.INSTANCE.register(WeaponData.WeaponDataCapability.class, new NullStorage<WeaponData.WeaponDataCapability>(),
				new Callable<WeaponData.WeaponDataCapability>() {

					@Override
					public WeaponData.WeaponDataCapability call() throws Exception {
						// TODO Auto-generated method stub
						return new WeaponData.WeaponDataCapability();
					}

				});
		CapabilityManager.INSTANCE.register(TF2EventsCommon.TF2WorldStorage.class, new NullStorage<TF2EventsCommon.TF2WorldStorage>(),
				new Callable<TF2EventsCommon.TF2WorldStorage>() {

					@Override
					public TF2EventsCommon.TF2WorldStorage call() throws Exception {
						// TODO Auto-generated method stub
						return new TF2EventsCommon.TF2WorldStorage();
					}

				});
		ForgeRegistries.POTIONS.register(bonk = new PotionTF2Item(false, 0x696969, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/bonk.png")).setPotionName("effect.bonk")
				.setRegistryName(TF2weapons.MOD_ID + ":bonkEff"));
		ForgeRegistries.POTIONS.register(stun = new PotionTF2(true, 0, 3, 1).setPotionName("effect.stun").setRegistryName(TF2weapons.MOD_ID + ":stunEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354F0D8BA", -0.5D, 2));
		ForgeRegistries.POTIONS.register(crit = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/critacola.png")).setPotionName("effect.crit")
				.setRegistryName(TF2weapons.MOD_ID + ":critEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B354E56B59", 0.25D, 2));
		ForgeRegistries.POTIONS.register(buffbanner = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/buffbanner.png")).setPotionName("effect.banner")
				.setRegistryName(TF2weapons.MOD_ID + ":bannerEff"));
		ForgeRegistries.POTIONS.register(backup = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/backup.png")).setPotionName("effect.backup")
				.setRegistryName(TF2weapons.MOD_ID + ":backupEff"));
		ForgeRegistries.POTIONS.register(conch = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/conch.png")).setPotionName("effect.conch")
				.setRegistryName(TF2weapons.MOD_ID + ":conchEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E2", 0.25D, 2));
		ForgeRegistries.POTIONS.register(markDeath = new PotionTF2(true, 0, 1, 2).setPotionName("effect.markDeath").setRegistryName(TF2weapons.MOD_ID + ":markDeathEff"));
		ForgeRegistries.POTIONS.register(critBoost = new PotionTF2(false, 0, 4, 0).setPotionName("effect.critBoost").setRegistryName(TF2weapons.MOD_ID + ":critBoostEff"));
		ForgeRegistries.POTIONS.register(jarate = new PotionTF2Item(true, 0xFFD500, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/jarate.png")).setPotionName("effect.jarate")
				.setRegistryName(TF2weapons.MOD_ID + ":jarateEff"));
		ForgeRegistries.POTIONS.register(madmilk = new PotionTF2Item(true, 0xF1F1F1, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/madmilk.png")).setPotionName("effect.madmilk")
				.setRegistryName(TF2weapons.MOD_ID + ":madmilkEff"));
		ForgeRegistries.POTIONS.register(charging = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/charging_targe.png")).setPotionName("effect.charging")
				.setRegistryName(TF2weapons.MOD_ID + ":chargingEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE5E-7CE8-4030-940E-14B35B5565E6", 2D, 2));
		ForgeRegistries.POTIONS.register(uber = new PotionTF2Item(false, 0, new ResourceLocation(TF2weapons.MOD_ID, "textures/items/medigun_im.png")).setPotionName("effect.uber")
				.setRegistryName(TF2weapons.MOD_ID + ":uberEff"));
		ForgeRegistries.POTIONS.register(it = new PotionTF2(true, 0xFFFFFFFF, 1, 2).setPotionName("effect.it").setRegistryName(TF2weapons.MOD_ID + ":itEff"));
		ForgeRegistries.POTIONS.register(bombmrs = new PotionTF2(false, 0xFFFFFFFF, 1, 2).setPotionName("effect.bombmrs").setRegistryName(TF2weapons.MOD_ID + ":bombEff")
				.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "7107DE6F-7CE8-4030-940E-14B354F0D8BA", 1.25D, 2));
		ForgeRegistries.POTIONS.register(bleeding = new PotionTF2(true, 0, 4, 0).setPotionName("effect.bleeding").setRegistryName(TF2weapons.MOD_ID + ":bleedingEff"));
		// conf.save();
		WeaponData.PropertyType.init();
		if(!TF2ConfigVars.disableGeneration){
			MapGenStructureIO.registerStructureComponent(MannCoBuilding.class, "ViMC");
			VillagerRegistry.instance().registerVillageCreationHandler(new MannCoBuilding.CreationHandler());
		}
		loadWeapons();
			//System.out.println(MapList.nameToData.get("rocketlauncher"));
		cratesOpened = (new StatBasic("stat.cratesOpened", new TextComponentTranslation("stat.cratesOpened", new Object[0]))).registerStat();

		proxy.preInit();
		MinecraftForge.EVENT_BUS.register(new TF2EventsCommon());
		MinecraftForge.ORE_GEN_BUS.register(new TF2EventsCommon());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		/*if (event.getSide() == Side.CLIENT) {
			AchievementPage.registerAchievementPage(new TF2Achievements());
		}*/
		GameRegistry.addSmelting(new ItemStack(blockCopperOre), new ItemStack(itemTF2, 1, 0), 0.5f);
		GameRegistry.addSmelting(new ItemStack(blockLeadOre), new ItemStack(itemTF2, 1, 1), 0.55f);
		GameRegistry.addSmelting(new ItemStack(blockAustraliumOre), new ItemStack(itemTF2, 1, 2), 2f);
		GameRegistry.addSmelting(new ItemStack(itemTF2, 1, 3), new ItemStack(Items.IRON_INGOT, 2), 0.35f);
		GameRegistry.registerFuelHandler(new IFuelHandler() {

			@Override
			public int getBurnTime(ItemStack fuel) {
				// TODO Auto-generated method stub
				return fuel.getItem() instanceof ItemCrate ? 300 : 0;
			}

		});
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(blockCabinet), "SCS", "SIS", 'S', new ItemStack(itemTF2, 1, 3), 'C', "workbench", 'I', "blockIron")
				.setRegistryName(TF2weapons.MOD_ID,"recipe1"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(itemTF2, 1, 2), "AAA", "AAA", "AAA", 'A', new ItemStack(itemTF2, 1, 6))
				.setRegistryName(TF2weapons.MOD_ID,"recipe2"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null,new ItemStack(itemTF2, 9, 6), "ingotAustralium").setRegistryName(TF2weapons.MOD_ID,"recipe3"));
		ForgeRegistries.RECIPES.register(new ShapedOreRecipe(null, new ItemStack(blockAustralium), "AAA", "AAA", "AAA", 'A', new ItemStack(itemTF2, 1, 2))
				.setRegistryName(TF2weapons.MOD_ID,"recipe4"));
		ForgeRegistries.RECIPES.register(new ShapelessOreRecipe(null,new ItemStack(itemTF2, 9, 2), "blockAustralium").setRegistryName(TF2weapons.MOD_ID,"recipe5"));
		ForgeRegistries.RECIPES.register(new OpenCrateRecipe().setRegistryName(TF2weapons.MOD_ID,"opencrate"));

		LootFunctionManager.registerFunction(new EntityBuildingFunction.Serializer());
		LootFunctionManager.registerFunction(new EntityOfClassFunction.Serializer());
		LootFunctionManager.registerFunction(new RandomWeaponFunction.Serializer());
		LootConditionManager.registerCondition(new KilledByTeam.Serializer());
		
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/simple_dungeon"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/nether_bridge"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/stronghold_corridor"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/end_city_treasure"));
		LootTableList.register(new ResourceLocation(MOD_ID, "chests/abandoned_mineshaft"));
		
		lootTF2Character = LootTableList.register(new ResourceLocation(MOD_ID, "entities/tf2character"));
		lootScout = LootTableList.register(new ResourceLocation(MOD_ID, "entities/scout"));
		lootHeavy = LootTableList.register(new ResourceLocation(MOD_ID, "entities/heavy"));
		lootSniper = LootTableList.register(new ResourceLocation(MOD_ID, "entities/sniper"));
		lootSpy = LootTableList.register(new ResourceLocation(MOD_ID, "entities/spy"));
		lootDemoman = LootTableList.register(new ResourceLocation(MOD_ID, "entities/demoman"));
		lootEngineer = LootTableList.register(new ResourceLocation(MOD_ID, "entities/engineer"));
		lootSoldier = LootTableList.register(new ResourceLocation(MOD_ID, "entities/soldier"));
		lootMedic = LootTableList.register(new ResourceLocation(MOD_ID, "entities/medic"));
		lootPyro = LootTableList.register(new ResourceLocation(MOD_ID, "entities/pyro"));
		lootHale = LootTableList.register(new ResourceLocation(MOD_ID, "entities/hale"));

		

		// new
		// Item(2498).setUnlocalizedName("FakeItem").setTextureName(TF2weapons.MOD_ID+":saw").setCreativeTab(CreativeTabs.tabBlock);
		BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemPlacer, new BehaviorDefaultDispenseItem() {
			@Override
			public ItemStack dispenseStack(IBlockSource p_82487_1_, ItemStack p_82487_2_) {
				EnumFacing enumfacing = p_82487_1_.getBlockState().getValue(BlockDispenser.FACING);
				double d0 = p_82487_1_.getX() + enumfacing.getFrontOffsetX();
				double d1 = p_82487_1_.getY() + 0.2F;
				double d2 = p_82487_1_.getZ() + enumfacing.getFrontOffsetZ();
				Entity entity = ItemMonsterPlacerPlus.spawnCreature(p_82487_1_.getWorld(), p_82487_2_.getItemDamage(), d0, d1, d2,
						p_82487_2_.getTagCompound() != null && p_82487_2_.getTagCompound().hasKey("SavedEntity") ? p_82487_2_.getTagCompound().getCompoundTag("SavedEntity")
								: null);

				if (entity instanceof EntityLivingBase && p_82487_2_.hasDisplayName())
					((EntityLiving) entity).setCustomNameTag(p_82487_2_.getDisplayName());

				p_82487_2_.splitStack(1);
				return p_82487_2_;
			}
		});

		network = new TF2NetworkWrapper(MOD_ID);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Side.SERVER, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Side.SERVER, false);
		network.registerMessage(TF2BulletHandler.class, TF2Message.BulletMessage.class, 3, Side.SERVER, true);
		network.registerMessage(TF2ProjectileHandler.class, TF2Message.PredictionMessage.class, 4, Side.SERVER, true);
		network.registerMessage(TF2GuiConfigHandler.class, TF2Message.GuiConfigMessage.class, 5, Side.SERVER, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 7, Side.SERVER, false);
		network.registerMessage(TF2ShowGuiHandler.class, TF2Message.ShowGuiMessage.class, 9, Side.SERVER, false);
		network.registerMessage(TF2DisguiseHandler.class, TF2Message.DisguiseMessage.class, 11, Side.SERVER, false);
		network.registerMessage(TF2ActionHandler.class, TF2Message.ActionMessage.class, 0, Side.CLIENT, true);
		network.registerMessage(TF2UseHandler.class, TF2Message.UseMessage.class, 1, Side.CLIENT, true);
		network.registerMessage(TF2PropertyHandler.class, TF2Message.PropertyMessage.class, 2, Side.CLIENT, false);
		network.registerMessage(TF2CapabilityHandler.class, TF2Message.CapabilityMessage.class, 6, Side.CLIENT, false);
		network.registerMessage(TF2WeaponDataHandler.class, TF2Message.WeaponDataMessage.class, 8, Side.CLIENT, false);
		network.registerMessage(TF2WearableChangeHandler.class, TF2Message.WearableChangeMessage.class, 10, Side.CLIENT, false);
		network.registerMessage(TF2WeaponDropHandler.class, TF2Message.WeaponDroppedMessage.class, 12, Side.CLIENT, true);
		network.registerMessage(TF2ContractHandler.class, TF2Message.ContractMessage.class, 13, Side.CLIENT, false);
		network.registerMessage(TF2VelocityAddHandler.class, TF2Message.VelocityAddMessage.class, 14, Side.CLIENT, true);
		network.registerMessage(TF2AttackSyncHandler.class, TF2Message.AttackSyncMessage.class, 15, Side.SERVER, true);
		network.registerMessage(TF2InitHandler.class, TF2Message.InitMessage.class, 16, Side.CLIENT, false);
		network.registerMessage(TF2InitClientHandler.class, TF2Message.InitClientMessage.class, 17, Side.SERVER, false);
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new IGuiHandler() {

			@Override
			public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0){
					ContainerWearables container=new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), false, player);
					container.addListener(new TF2EventsCommon.TF2ContainerListener((EntityPlayerMP) player));
					return container;
				}
				else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new ContainerTF2Workbench(player, player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */ world, pos);
				else if (ID == 2 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityUpgrades){
					if(((TileEntityUpgrades) world.getTileEntity(pos)).attributes.size()==0){
						((TileEntityUpgrades) world.getTileEntity(pos)).generateUpgrades(player.getRNG());
					}
					if(player instanceof EntityPlayerMP)
						((EntityPlayerMP)player).connection.sendPacket(world.getTileEntity(pos).getUpdatePacket());
					return new ContainerUpgrades(player, player.inventory, (TileEntityUpgrades) world.getTileEntity(pos), world, pos);
				}
				else if (ID == 3 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new ContainerAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getTileEntity(pos));
				else if (ID == 4 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityTF2Character)
					return new ContainerMercenary(player, (EntityTF2Character) world.getEntityByID(x), world);
				return null;
			}

			@Override
			public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
				// TODO Auto-generated method stub
				BlockPos pos = new BlockPos(x, y, z);
				if (ID == 0)
					return new GuiWearables(new ContainerWearables(player.inventory, player.getCapability(INVENTORY_CAP, null), true, player));
				else if (ID == 1 && world.getBlockState(pos).getBlock() instanceof BlockCabinet)
					return new GuiTF2Crafting(player.inventory,
							/* (TileEntityCabinet) world.getTileEntity(pos), */world, pos);
				else if (ID == 2 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityUpgrades){
					return new GuiUpgradeStation(player.inventory, (TileEntityUpgrades) world.getTileEntity(pos), world, pos);
				}
				else if (ID == 3 && world.getTileEntity(pos) != null && world.getTileEntity(pos) instanceof TileEntityAmmoFurnace)
					return new GuiAmmoFurnace(player.inventory, (TileEntityAmmoFurnace) world.getTileEntity(pos));
				else if (ID == 4 && world.getEntityByID(x) != null && world.getEntityByID(x) instanceof EntityTF2Character)
					return new GuiMercenary(player.inventory,(EntityTF2Character) world.getEntityByID(x),world);
				return null;
			}

		});
		// TickRegistry.registerTickHandler(new CommonTickHandler(),
		// Side.SERVER);

		// FMLCommonHandler.instance().bus().register(new
		// TF2EventBusListener());
		proxy.registerRenderInformation();

	}

	@Mod.EventHandler
	public void postinit(FMLPostInitializationEvent event) {

		updateMobSpawning();
		updateOreGenStatus();
		
		animals = new ArrayList<>();
		for(ResourceLocation entry:ForgeRegistries.ENTITIES.getKeys()) {
			if(EntityAnimal.class.isAssignableFrom(EntityList.getClass(entry))) {
				animals.add(entry);
			}
		}
	}

	public static void updateOreGenStatus() {

		//System.out.println("Generowane: " + OreDictionary.getOres("oreCopper").size());
		String copper =conf.get("world gen", "Generate copper", "Default").setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String lead =conf.get("world gen", "Generate lead", "Default").setValidValues(new String[] { "Always", "Default", "Never" }).getString();
		String australium =conf.get("world gen", "Generate australium", "Always").setValidValues(new String[] { "Always", "Never" }).getString();
		generateCopper = copper.equals("Always");
		generateLead = lead.equals("Always");
		generateAustralium = australium.equals("Always");
		
		if (copper.equals("Default") && OreDictionary.getOres("oreCopper").size() == 1)
			generateCopper = true;
		if (lead.equals("Default") && OreDictionary.getOres("oreLead").size() == 1)
			generateLead = true;
	}

	public static void registerBlock(Block block, String name) {
		ForgeRegistries.BLOCKS.register(block.setRegistryName(name));
		ItemBlock item = new ItemBlock(block);
		item.setRegistryName(block.getRegistryName());
		ForgeRegistries.ITEMS.register(item);
		proxy.registerItemBlock(item);
	}

	public static void loadWeapons() {

		MapList.nameToData.clear();
		MapList.buildInAttributes.clear();
		try {
			ByteArrayOutputStream output=new ByteArrayOutputStream();
			DataOutputStream stream=new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(output)));
		
			loadConfig(new File(instance.weaponDir, "Weapons.json"),stream);
			File[] files = instance.weaponDir.listFiles(new FilenameFilter() {
	
				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".json") && !arg1.equalsIgnoreCase("Weapons.json");
				}
	
			});
			for (File file : files)
				loadConfig(file,stream);
			stream.flush();
			stream.close();
			itemDataCompressed=output.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void loadConfig(File file,DataOutput output) {
		/*
		 * Configuration weaponsFile= new Configuration(file);
		 * weaponsFile.load();
		 */
		ArrayList<WeaponData> list = WeaponData.parseFile(file);
		for (WeaponData data : list) {
			String weaponEntry = data.getName();
			// Class<?> weaponClass =
			// MapList.weaponClasses.get(weaponData.get("Class").getString());
			try {
				// System.out.println("attach "+weaponEntry);
				if (PropertyType.BASED_ON.hasKey(data) && MapList.nameToData.containsKey(PropertyType.BASED_ON.getString(data)))
					data = attach(MapList.nameToData.get(PropertyType.BASED_ON.getString(data)), data);
				loadWeapon(weaponEntry, data);
				
				output.writeUTF(data.getName());
				output.writeByte(data.properties.size());
				for (PropertyType type : data.properties.keySet()){
					
					type.serialize(output, data);
				}
				output.writeByte(Math.max(data.attributes.size(), data.crateContent.size()));
				for (Entry<TF2Attribute, Float> attr : data.attributes.entrySet()) {
					output.writeByte(attr.getKey().id);
					output.writeFloat(attr.getValue());
				}
				for (Entry<String, Integer> entry : data.crateContent.entrySet()) {
					output.writeUTF(entry.getKey());
					output.writeFloat(entry.getValue());
				}

			} catch (Exception var4) {
				var4.printStackTrace();
			}
		}
	}
	public static void loadWeapon(String name, WeaponData weapon) {
		
		
		/*
		 * else{ weaponList[Integer.parseInt(weaponEntry)] =(ItemUsable)
		 * weaponClass.getConstructor(new Class[] {ConfigCategory.class,
		 * ConfigCategory.class}).newInstance(new Object[] {weaponData, null});
		 * }
		 */
		// GameRegistry.registerItem(weaponList[Integer.parseInt(weaponEntry)],
		// "weapon"+Integer.parseInt(weaponEntry));
		MapList.nameToData.put(name, weapon);
		// System.out.println("Weapon read: "+name);
		/*
		 * for(Entry<PropertyType, WeaponData.Property>
		 * entry:weapon.properties.entrySet()){
		 * System.out.println("Property: "+entry.getKey().name+" Value: "+entry.
		 * getValue().intValue+" "+entry.getValue().stringValue); }
		 */
		// LanguageRegistry.instance().addStringLocalization(weaponData.get("Name").getString()+".name",
		// weaponData.get("Name").getString());
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Type", name);
		NBTTagCompound tag2 = new NBTTagCompound();
		if (!weapon.attributes.isEmpty())
			for (Entry<TF2Attribute, Float> entry : weapon.attributes.entrySet())
				tag2.setFloat(String.valueOf(entry.getKey().id), entry.getValue());
		MapList.buildInAttributes.put(name, tag2);
	}

	@SuppressWarnings("unchecked")
	public static WeaponData attach(WeaponData base, WeaponData additional) {
		for (PropertyType prop : base.properties.keySet())
			if (!additional.properties.containsKey(prop))
				additional.properties.put(prop, base.properties.get(prop));
		MapList.buildInAttributes.put(additional.getName(), MapList.buildInAttributes.get(base.getName()));
		// System.out.println("merged: "+additional.getName()+" "+key);

		// new ConfigCategory(null, additional);
		return additional;
	}

	@Mod.EventHandler
	public void serverPreInit(FMLServerAboutToStartEvent event) {
		// System.out.println("Starting server");
		
		if (!event.getServer().isDedicatedServer())
			for (WeaponData weapon : MapList.nameToData.values())
				ClientProxy.RegisterWeaponData(weapon);
		
		/*if(event.getSide()==Side.SERVER)
			AchievementPage.registerAchievementPage(new TF2Achievements());*/
	}

	@Mod.EventHandler
	public void serverInit(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandGiveWeapon());
		event.registerServerCommand(new CommandResetWeapons());
		event.registerServerCommand(new CommandResetStat());
		event.registerServerCommand(new CommandGenerateReferences());
		if(event.getServer().isSinglePlayer())
			TF2UdpClient.addressToUse = "127.0.0.1";
		try {
			server = event.getServer();
			File input = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory, server.getFolderName() + "/teleports.dat");
			NBTTagCompound tagRoot = CompressedStreamTools.readCompressed(new FileInputStream(input));
			NBTTagCompound tag = tagRoot.getCompoundTag("Teleporters");
			for (String keys : tag.getKeySet()) {
				TeleporterData[] blockArray = new TeleporterData[EntityTeleporter.TP_PER_PLAYER];
				EntityTeleporter.teleporters.put(UUID.fromString(keys), blockArray);
				NBTTagCompound exitTag = tag.getCompoundTag(keys);
				for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++)
					if (exitTag.hasKey(Integer.toString(i))) {
						int[] array = exitTag.getIntArray(Integer.toString(i));
						blockArray[i] = new TeleporterData(new BlockPos(array[0], array[1], array[2]), array[3], array[4]);
					}
			}
			EntityTeleporter.tpCount = tagRoot.getInteger("TPCount");

		} catch (IOException e) {
			System.err.println("Reading teleporter data skipped");
		}
		
		if(TF2ConfigVars.enableUdp) {
			try {
				udpServer = new TF2UdpServer(event.getServer().isSinglePlayer()? 12454 : event.getServer().getServerPort() );
				udpServer.start();
				TF2weapons.network.useUdp=true;
				//new TF2UdpClient(SocketUtils.socketAddress("127.0.0.1", 12454));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Mod.EventHandler
	public void serverStop(FMLServerStoppingEvent event) {
		//MapList.nameToData.clear();
		//MapList.buildInAttributes.clear();

		File output = new File(((AnvilSaveConverter) server.getActiveAnvilConverter()).savesDirectory, server.getFolderName() + "/teleports.dat");
		NBTTagCompound tagRoot = new NBTTagCompound();
		NBTTagCompound tag = new NBTTagCompound();
		tagRoot.setTag("Teleporters", tag);

		for (Entry<UUID, TeleporterData[]> entry : EntityTeleporter.teleporters.entrySet()) {
			NBTTagCompound exitTag = new NBTTagCompound();
			for (int i = 0; i < EntityTeleporter.TP_PER_PLAYER; i++) {
				TeleporterData blockPos = entry.getValue()[i];
				if (blockPos != null)
					exitTag.setIntArray(Integer.toString(i), new int[] { blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.id, blockPos.dimension});
			}
			tag.setTag(entry.getKey().toString(), exitTag);
		}
		tagRoot.setInteger("TPCount", EntityTeleporter.tpCount);

		try {
			CompressedStreamTools.writeCompressed(tagRoot, new FileOutputStream(output));
		} catch (IOException e) {
			e.printStackTrace();
		}

		EntityTeleporter.teleporters.clear();
		EntityTeleporter.tpCount = 0;
		if(udpServer != null) {
			udpServer.stopServer();
			udpServer = null;
		}
	}

	/*
	 * public static void openWearableGUI(EntityPlayerMP player){ InventoryBasic
	 * inventory=new InventoryBasic("Wearables", false, 3); NBTTagList
	 * nbttaglist = player.getEntityData().getTagList("Wearables", 10); for (int
	 * i = 0; i < nbttaglist.tagCount(); ++i) { NBTTagCompound nbttagcompound =
	 * nbttaglist.getCompoundTagAt(i); int j = nbttagcompound.getByte("Slot") &
	 * 255; inventory.setInventorySlotContents(j,
	 * ItemStack.loadItemStackFromNBT(nbttagcompound)); } if
	 * (player.openContainer != player.inventoryContainer) {
	 * player.closeScreen(); }
	 * 
	 * player.getNextWindowId(); player.connection.sendPacket(new
	 * SPacketOpenWindow(player.currentWindowId, "rafradek_wearables",
	 * inventory.getDisplayName(), inventory.getSizeInventory(),
	 * player.getEntityId())); player.openContainer = new
	 * ContainerHorseInventory(player.inventory, inventory, horse, this);
	 * player.openContainer.windowId = player.currentWindowId;
	 * player.openContainer.addListener(this); }
	 */

	public static void updateMobSpawning() {
		ArrayList<Biome> biomesList = new ArrayList<Biome>();
		for (Biome biome : GameRegistry.findRegistry(Biome.class)){
			if(biome!=Biomes.HELL && biome!=Biomes.SKY)
			biomesList.add(biome);
		}
		if (!TF2ConfigVars.disableSpawn) {
			Biome[] biomes = biomesList.toArray(new Biome[biomesList.size()]);
			for(Entry<Class<? extends EntityLiving>, Integer> entry:TF2ConfigVars.spawnRate.entrySet()) {
				EntityRegistry.addSpawn(entry.getKey(), entry.getValue().intValue(), 1, 3, EnumCreatureType.MONSTER, biomes);
			}
		}
	}
	public static class NullStorage<T> implements IStorage<T> {

		@Override
		public NBTBase writeNBT(Capability<T> capability, T instance, EnumFacing side) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void readNBT(Capability<T> capability, T instance, EnumFacing side, NBTBase nbt) {
			// TODO Auto-generated method stub

		}

	}
}
