package net.mistersecret312.aperture_innovations.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class ThrownIntoFluidCriterion extends SimpleCriterionTrigger<ThrownIntoFluidCriterion.ThrownIntoFluidTriggerInstance>
{

	public void trigger(ServerPlayer player, ItemStack stack, BlockPos pos)
	{
		this.trigger(player, (trigger -> trigger.matches(stack, player.level(), pos)));
	}

	@Override
	public Codec<ThrownIntoFluidTriggerInstance> codec()
	{
		return ThrownIntoFluidTriggerInstance.CODEC;
	}

	public static record ThrownIntoFluidTriggerInstance(Optional<ContextAwarePredicate> player,
														Optional<ItemPredicate> item,
														Optional<FluidPredicate> fluid) implements SimpleCriterionTrigger.SimpleInstance
	{

		public static final Codec<ThrownIntoFluidTriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ThrownIntoFluidTriggerInstance::player),
				ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ThrownIntoFluidTriggerInstance::item),
				FluidPredicate.CODEC.optionalFieldOf("fluid").forGetter(ThrownIntoFluidTriggerInstance::fluid)
				).apply(instance, ThrownIntoFluidTriggerInstance::new));

		public boolean matches(ItemStack stack, Level level, BlockPos blockPos)
		{
			if(this.item.isPresent() && !this.item.get().test(stack))
				return false;

			if(this.fluid.isPresent() && !this.fluid.get().matches((ServerLevel) level, blockPos))
				return false;

			return true;
		}
	}
}
