package com.minecolonies.blocks;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.Colony;
import com.minecolonies.colony.ColonyManager;
import com.minecolonies.creativetab.ModCreativeTabs;
import com.minecolonies.inventory.InventoryField;
import com.minecolonies.lib.Constants;
import com.minecolonies.tileentities.ScarecrowTileEntity;
import com.minecolonies.util.EntityUtils;
import com.minecolonies.util.LanguageHandler;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * The class handling the fieldBlocks, placement and activation.
 */
public class BlockHutField extends BlockContainer
{
    /**
     * Hardness of the block.
     */
    private static final float HARDNESS = 10F;

    /**
     * Resistance of the block.
     */
    private static final float RESISTANCE = 10F;

    /**
     * The position it faces.
     */
    private static final PropertyDirection FACING = PropertyDirection.create("FACING", EnumFacing.Plane.HORIZONTAL);

    /**
     * Start of the collision box at y.
     */
    private static final double BOTTOM_COLLISION = 0.0;

    /**
     * Start of the collision box at x and z.
     */
    private static final double START_COLLISION = 0.1;

    /**
     * End of the collision box.
     */
    private static final double END_COLLISION = 0.9;

    /**
     * Height of the collision box.
     */
    private static final double HEIGHT_COLLISION = 2.5;

    /**
     * Registry name for this block.
     */
    private static final String REGISTRY_NAME = "blockHutField";

    /**
     * Constructor called on block placement.
     */
    BlockHutField()
    {
        super(Material.wood);
        initBlock();
    }

    /**
     * Method called by constructor.
     * Sets basic details of the block.
     */
    private void initBlock()
    {
        setRegistryName(REGISTRY_NAME);
        setUnlocalizedName(Constants.MOD_ID.toLowerCase() + "." + "blockHutField");
        setCreativeTab(ModCreativeTabs.MINECOLONIES);
        //Blast resistance for creepers etc. makes them explosion proof.
        setResistance(RESISTANCE);
        //Hardness of 10 takes a long time to mine to not loose progress.
        setHardness(HARDNESS);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        GameRegistry.registerBlock(this);
        setBlockBounds((float) START_COLLISION, (float) BOTTOM_COLLISION, (float) START_COLLISION, (float) END_COLLISION, (float) HEIGHT_COLLISION, (float) END_COLLISION);
    }

    @Override
    public int getRenderType()
    {
        return -1;
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        EnumFacing facing = EnumFacing.getFront(meta);
        if (facing.getAxis() == EnumFacing.Axis.Y)
        {
            facing = EnumFacing.NORTH;
        }
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public boolean isFullCube()
    {
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer()
    {
        return EnumWorldBlockLayer.SOLID;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        //If the world is server, open the inventory of the field.
        if (!worldIn.isRemote)
        {
            final Colony colony = ColonyManager.getColony(worldIn, pos);
            if (colony != null)
            {
                playerIn.openGui(MineColonies.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
                return true;
            }
        }
        return false;
    }

    // =======================================================================
    // ======================= Rendering & IBlockState =======================
    // =======================================================================
    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
        final EnumFacing enumFacing = (placer == null) ? EnumFacing.NORTH : EnumFacing.fromAngle(placer.rotationYaw);
        return this.getDefaultState().withProperty(FACING, enumFacing);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
        //Only work on server side.
        if (worldIn.isRemote)
        {
            return;
        }

        if (placer instanceof EntityPlayer)
        {
            final Colony colony = ColonyManager.getColony(worldIn, pos);

            if (colony != null)
            {
                final InventoryField inventoryField = new InventoryField(LanguageHandler.getString("com.minecolonies.gui.inventory.scarecrow"), true);

                ((ScarecrowTileEntity) worldIn.getTileEntity(pos)).setInventoryField(inventoryField);
                colony.addNewField(inventoryField, ((EntityPlayer) placer).inventory, pos, worldIn);
            }
        }
    }

    @Override
    protected BlockState createBlockState()
    {
        return new BlockState(this, FACING);
    }

    @Override
    public boolean hasTileEntity(final IBlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new ScarecrowTileEntity();
    }
    // =======================================================================
    // ===================== END of Rendering & Meta-Data ====================
    // =======================================================================
}
