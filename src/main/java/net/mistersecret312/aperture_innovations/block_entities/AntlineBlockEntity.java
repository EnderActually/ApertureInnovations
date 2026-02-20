package net.mistersecret312.aperture_innovations.block_entities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.mistersecret312.aperture_innovations.blocks.AntlineBlock;
import net.mistersecret312.aperture_innovations.blocks.enums.ConnectionState;
import net.mistersecret312.aperture_innovations.data.AntlineData;
import net.mistersecret312.aperture_innovations.data.antline.Antline;
import net.mistersecret312.aperture_innovations.init.BlockEntityInit;
import net.mistersecret312.aperture_innovations.init.TagInit;
import net.mistersecret312.aperture_innovations.network.ClientboundAntlineUpdatePacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class AntlineBlockEntity extends BlockEntity
{
	public int color = -1;
	public int activeColor = -1;

	public ConnectionState north = ConnectionState.NONE;
	public ConnectionState south = ConnectionState.NONE;
	public ConnectionState west = ConnectionState.NONE;
	public ConnectionState east = ConnectionState.NONE;
	public ConnectionState up = ConnectionState.NONE;
	public ConnectionState down = ConnectionState.NONE;

	public boolean active;
	public boolean outputting;

	public int networkId = 0;
	private Antline antline = null;

	public int signal = 0;

	public AntlineBlockEntity(BlockPos pos, BlockState blockState)
	{
		super(BlockEntityInit.ANTLINE.get(), pos, blockState);
	}

	@Override
	public void setChanged()
	{
		if(getLevel() != null && !getLevel().isClientSide())
		{
			PacketDistributor.sendToPlayersTrackingChunk((ServerLevel) getLevel(), new ChunkPos(getBlockPos()),
					new ClientboundAntlineUpdatePacket(getBlockPos(), active));
		}
		super.setChanged();
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries)
	{
		return this.saveWithoutMetadata(registries);
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		tag.putInt("network_id", this.networkId);

		tag.putInt("color", this.color);
		tag.putInt("active_color", this.activeColor);

		tag.putBoolean("active", this.active);

		tag.putString("north", this.north.name);
		tag.putString("south", this.south.name);
		tag.putString("west", this.west.name);
		tag.putString("east", this.east.name);
		tag.putString("up", this.up.name);
		tag.putString("down", this.down.name);

		super.saveAdditional(tag, registries);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
	{
		super.loadAdditional(tag, registries);

		this.networkId = tag.getInt("network_id");

		this.color = tag.getInt("color");
		this.activeColor = tag.getInt("active_color");

		this.active = tag.getBoolean("active");

		this.north = ConnectionState.fromString(tag.getString("north"));
		this.south = ConnectionState.fromString(tag.getString("south"));
		this.west = ConnectionState.fromString(tag.getString("west"));
		this.east = ConnectionState.fromString(tag.getString("east"));
		this.up = ConnectionState.fromString(tag.getString("up"));
		this.down = ConnectionState.fromString(tag.getString("down"));
	}

	public boolean isActive()
	{
		return active;
	}

	public Direction getNormal()
	{
		return getBlockState().getValue(AntlineBlock.NORMAL);
	}

	public ConnectionState getState(Direction direction)
	{
		return switch(direction)
		{
			case UP -> up;
			case DOWN -> down;
			case NORTH -> north;
			case SOUTH -> south;
			case EAST -> east;
			case WEST -> west;
		};
	}

	public List<Direction> getConnectedSides()
	{
		List<Direction> connections = new ArrayList<>();
		for(Direction direction : Direction.values())
		{
			ConnectionState state = getState(direction);
			if(!state.equals(ConnectionState.NONE)) connections.add(direction);
		}

		return connections;
	}

	public boolean hasConnectionType(ConnectionState filter)
	{
		for(Direction direction : Direction.values())
		{
			ConnectionState state = getState(direction);
			if(state.equals(filter)) return true;
		}

		return false;
	}

	public void setState(Direction direction, ConnectionState state)
	{
		switch(direction)
		{
			case SOUTH -> this.south = state;
			case NORTH -> this.north = state;
			case EAST -> this.east = state;
			case WEST -> this.west = state;
			case UP -> this.up = state;
			case DOWN -> this.down = state;
		}

		setChanged();
	}

	public int getNetworkID()
	{
		return networkId;
	}

	public void setNetworkID(int networkId)
	{
		this.networkId = networkId;
		this.antline = null;

		setChanged();
	}

	public Antline getAntline()
	{
		if(antline != null) return antline;

		if(networkId == 0) return null;

		antline = AntlineData.get(getLevel()).getLine(networkId);
		return antline;
	}

	public void updateConnections()
	{
		Level level = this.getLevel();
		Direction normal = this.getNormal();

		if(level == null)
			return;

		for(Direction direction : Direction.values())
		{
			if(direction.getAxis().equals(normal.getAxis()))
				continue;

			ConnectionState state;
			BlockPos relativePos = getBlockPos().relative(direction);

			state = validateBlock(level, relativePos, direction);
			if(state.equals(ConnectionState.NONE))
			{
				state = validateBlock(level, relativePos.relative(normal.getOpposite()), direction);
				if(state.equals(ConnectionState.NONE))
				{
					state = validateBlock(level, relativePos.relative(normal), direction);

					if(!state.equals(ConnectionState.NONE) &&
							   level.getBlockEntity(relativePos.relative(normal)) instanceof AntlineBlockEntity antlineBlockEntity)
					{
						if(antlineBlockEntity.getNormal().equals(this.getNormal()))
						{
							this.setState(direction, ConnectionState.SIDE_UP);
							continue;
						}
						else continue;
					}

					if(state.equals(ConnectionState.NONE))
					{
						if(level.getBlockEntity(getBlockPos().relative(normal)) instanceof AntlineBlockEntity antlineBlockEntity)
						{
							if(antlineBlockEntity.getNormal().getOpposite().equals(direction))
							{
								state = validateBlock(level, getBlockPos().relative(normal), direction);
								if(!state.equals(ConnectionState.NONE))
									state = ConnectionState.UP;
							}
						}
					}
				}
			}

			this.setState(direction, state);
		}
	}

	public ConnectionState validateBlock(Level level, BlockPos relativePos, Direction direction)
	{
		BlockState blockState = level.getBlockState(relativePos);
		BlockEntity blockEntity = level.getBlockEntity(relativePos);

		if(blockEntity instanceof AntlineBlockEntity antline)
		{
			if(antline.color != this.color || antline.activeColor != this.activeColor)
				return ConnectionState.NONE;

			return ConnectionState.SIDE;
		}

		if(blockState.is(TagInit.Blocks.CONNECTS_TO_ANTLINE))
			return ConnectionState.LINK;

		if(blockState.canRedstoneConnectTo(level, relativePos, direction.getOpposite()))
			return ConnectionState.LINK;

		return ConnectionState.NONE;
	}

	public void trimConnections()
	{
		Level level = getLevel();
		BlockPos pos = getBlockPos();
		Direction normal = getNormal();

		if(level == null)
			return;

		if(true)
			return;

		for(Direction direction : Direction.values())
		{
			boolean valid = false;
			ConnectionState state = getState(direction);
			if(state.equals(ConnectionState.NONE))
				continue;

			if(state.equals(ConnectionState.SIDE) || state.equals(ConnectionState.LINK))
			{
				BlockPos relativePos = pos.relative(direction);
				BlockState blockState = level.getBlockState(relativePos);

				if(blockState.is(TagInit.Blocks.CONNECTS_TO_ANTLINE))
					valid = true;

				if(blockState.canRedstoneConnectTo(level, relativePos, direction.getOpposite()))
					valid = true;

				if(level.getBlockEntity(relativePos) instanceof AntlineBlockEntity antline)
				{
					if(antline.color == this.color && antline.activeColor == this.activeColor)
						valid = true;
				}
			}

			if(state.equals(ConnectionState.UP))
			{
				BlockPos relativePos = pos.relative(direction).relative(normal);
				if(direction.getAxis() == normal.getAxis())
					relativePos = pos.relative(normal);

				BlockState blockState = level.getBlockState(relativePos);

				if(blockState.is(TagInit.Blocks.CONNECTS_TO_ANTLINE))
					valid = true;

				if(blockState.canRedstoneConnectTo(level, relativePos, direction.getOpposite()))
					valid = true;

				if(level.getBlockEntity(relativePos) instanceof AntlineBlockEntity antline)
				{
					if(antline.color == this.color && antline.activeColor == this.activeColor)
						valid = true;
				}
			}

			if(state.equals(ConnectionState.DOWN))
			{
				BlockPos relativePos = pos.relative(direction).relative(normal.getOpposite());

				BlockState blockState = level.getBlockState(relativePos);

				if(blockState.is(TagInit.Blocks.CONNECTS_TO_ANTLINE))
					valid = true;

				if(blockState.canRedstoneConnectTo(level, relativePos, direction.getOpposite()))
					valid = true;

				if(level.getBlockEntity(relativePos) instanceof AntlineBlockEntity antline)
				{
					if(antline.color == this.color && antline.activeColor == this.activeColor)
						valid = true;
				}
			}

			if(!valid)
				setState(direction, ConnectionState.NONE);
		}
	}
}
