package io.github.bananapuncher714.crafters.implementation.v1_13_R2;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.server.v1_13_R2.ContainerWorkbench;
import net.minecraft.server.v1_13_R2.EntityHuman;
import net.minecraft.server.v1_13_R2.EntityPlayer;
import net.minecraft.server.v1_13_R2.IInventory;
import net.minecraft.server.v1_13_R2.IRecipe;
import net.minecraft.server.v1_13_R2.InventoryCraftResult;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.NonNullList;
import net.minecraft.server.v1_13_R2.PacketPlayOutSetSlot;
import net.minecraft.server.v1_13_R2.PlayerInventory;
import net.minecraft.server.v1_13_R2.Slot;
import net.minecraft.server.v1_13_R2.SlotResult;
import net.minecraft.server.v1_13_R2.World;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_13_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;

public class CustomContainerWorkbench extends ContainerWorkbench {
	public InventoryCraftResult resultInventory;
	public CustomInventoryCrafting craftInventory;
	World world;
	HumanEntity viewer;
	private List< Slot > theseSlots;
	
	public CustomContainerWorkbench( HumanEntity player, Location blockLocation, CustomInventoryCrafting crafting, InventoryCraftResult result ) {
		// It's time to set up the annoying ContainerWorkbench
		super ( ( ( CraftHumanEntity ) player ).getHandle().inventory, ( ( CraftWorld ) blockLocation.getWorld() ).getHandle(), null );
		
		try {
			Field slots = this.getClass().getField( "slots" );
			theseSlots = ( List< Slot > ) slots.get( this );
			
		} catch ( Exception exception ) {
			try {
				Field c = this.getClass().getField( "c" );
				theseSlots = ( List< Slot > ) c.get( this );
			} catch ( Exception anotherException ) {
				anotherException.printStackTrace();
			}
		}
		theseSlots.clear();
		
		try {
			Field slots = this.getClass().getField( "items" );
			( ( NonNullList< ItemStack > ) slots.get( this ) ).clear();
		} catch ( Exception exception ) {
			try {
				Field c = this.getClass().getField( "b" );
				( ( NonNullList< ItemStack > ) c.get( this ) ).clear();
			} catch ( Exception anotherException ) {
				anotherException.printStackTrace();
			}
		}
		super.craftInventory = crafting;
		super.resultInventory = result;
		
		viewer = player;
		resultInventory = result;
		craftInventory = crafting;
		world = ( ( CraftWorld ) blockLocation.getWorld() ).getHandle();

		a( new SlotResult( ( ( CraftHumanEntity ) player ).getHandle(), craftInventory, resultInventory, 0, 124, 35));
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				a( new Slot( craftInventory, j + i * 3, 30 + j * 18, 17 + i * 18 ) );
			}
		}
		for ( int i = 0; i < 3; i++ ) {
			for (int j = 0; j < 9; j++) {
				a( new Slot( ( ( CraftHumanEntity ) player ).getHandle().inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 ) );
			}
		}
		for ( int i = 0; i < 9; i++ ) {
			a( new Slot( ( ( CraftHumanEntity ) player ).getHandle().inventory, i, 8 + i * 18, 142 ) );
		}
		a( craftInventory );
	}
	
	// Overrides 1.12
	public ItemStack b( EntityHuman entityhuman, int i ) {
		return shiftClick( entityhuman, i );
	}
	
	// Overrides 1.12.2
	public ItemStack shiftClick(EntityHuman entityhuman, int i) {
		ItemStack itemstack = ItemStack.a;
		Slot slot = ( Slot ) theseSlots.get(i);
		if ((slot != null) && (slot.hasItem())) {
			ItemStack itemstack1 = slot.getItem();

			itemstack = itemstack1.cloneItemStack();
			if (i == 0) {
				itemstack1.getItem().b(itemstack1, world, entityhuman);
				if (!a(itemstack1, 10, 46, true)) {
					return ItemStack.a;
				}
				slot.a(itemstack1, itemstack);
			} else if ((i >= 10) && (i < 37)) {
				if (!a(itemstack1, 37, 46, false)) {
					return ItemStack.a;
				}
			} else if ((i >= 37) && (i < 46)) {
				if (!a(itemstack1, 10, 37, false)) {
					return ItemStack.a;
				}
			} else if (!a(itemstack1, 10, 46, false)) {
				return ItemStack.a;
			}
			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.a);
			} else {
				slot.f();
			}
			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.a;
			}
			ItemStack itemstack2 = slot.a(entityhuman, itemstack1);
			if (i == 0) {
				entityhuman.drop(itemstack2, false);
			}
		}
		return itemstack;
	}
	
	public void setInventoryCrafting( CustomInventoryCrafting crafting ) {
		craftInventory = crafting;
	}

	@Override
	public void a( IInventory inventory ) {
		setCraftResult();
	}
	
	@Override
	public boolean a( ItemStack itemstack, Slot slot ) {
		return isNotResultSlot( slot );
	}
	
	public void setCraftResult() {
		if ( !world.isClientSide ) {
			EntityPlayer entityplayer = ( EntityPlayer ) ((CraftHumanEntity)viewer).getHandle();
			ItemStack itemstack = ItemStack.a;
			IRecipe irecipe = world.getMinecraftServer().getCraftingManager().b( craftInventory, world );
			if ((resultInventory.a(world, entityplayer, irecipe)) && (irecipe != null)) {
				resultInventory.a( irecipe );
				itemstack = irecipe.craftItem(resultInventory);
		      }
			itemstack = CraftEventFactory.callPreCraftEvent(craftInventory, resultInventory, itemstack, getBukkitView(), false);

			resultInventory.setItem(0, itemstack);
			entityplayer.playerConnection.sendPacket(new PacketPlayOutSetSlot(this.windowId, 0, itemstack));
			for ( Object listener : listeners ) {
				EntityPlayer player = ( EntityPlayer ) listener;
				player.playerConnection.sendPacket( new PacketPlayOutSetSlot( player.activeContainer.windowId, 0, itemstack ) );
			}
		}
	}
	
	public boolean isNotResultSlot( Slot slot ) {
		return slot.inventory != resultInventory;
	}
	
	/**
	 * This might be for when the inventory closes?
	 */
	@Override
	public void b( EntityHuman entity ) {
		PlayerInventory playerinventory = entity.inventory;
		if ( !playerinventory.getCarried().isEmpty() ) {
			entity.drop( playerinventory.getCarried(), false );
			playerinventory.setCarried( ItemStack.a );
		}
		// Make sure the craft inventory stops watching this container
		craftInventory.removeContainer( this );
	}
	
	// Overrides 1.12
	public boolean a( EntityHuman entity ) {
		return canUse( entity );
	}

	// Overrides 1.12.2
	public boolean canUse( EntityHuman entity ) {
		if ( !checkReachable ) {
			return true;
		}
		return craftInventory.getLocation().getBlock().getType() == Material.WORKBENCH;
	}

	@Override
	public CraftInventoryView getBukkitView() {
		return new CraftInventoryView( viewer, new CraftInventoryCrafting( craftInventory, resultInventory ), this );
	}

	protected HumanEntity getViewer() {
		return viewer;
	}
}
