package net.mistersecret312.aperture_innovations.advancements;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;
import java.util.Optional;

public class PortalTravelCriterion extends SimpleCriterionTrigger<PortalTravelCriterion.PortalTravelTriggerInstance>
{

	public void trigger(ServerPlayer player, ResourceLocation initialDimension, ResourceLocation destinationDimension,
						double teleportDistance, double verticalFlight, double horizontalJump, boolean lunacy)
	{
		this.trigger(player, (trigger -> trigger.matches(initialDimension, destinationDimension,
				teleportDistance, horizontalJump, verticalFlight, lunacy)));
	}

	@Override
	public Codec<PortalTravelCriterion.PortalTravelTriggerInstance> codec()
	{
		return PortalTravelTriggerInstance.CODEC;
	}

	public static record PortalTravelTriggerInstance(Optional<ContextAwarePredicate> player,
			                                 Optional<ResourceLocation> initialDimension, Optional<ResourceLocation> destinationDimension,
											 Optional<Double> teleportDistance, Optional<Double> horizontalJump,
											 Optional<Double> verticalFlight, Optional<Boolean> lunacy) implements SimpleCriterionTrigger.SimpleInstance
	{
		public static final Codec<PortalTravelTriggerInstance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
				EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(PortalTravelTriggerInstance::player),
				ResourceLocation.CODEC.optionalFieldOf("from").forGetter(PortalTravelTriggerInstance::initialDimension),
				ResourceLocation.CODEC.optionalFieldOf("to").forGetter(PortalTravelTriggerInstance::destinationDimension),
				Codec.DOUBLE.optionalFieldOf("distance").forGetter(PortalTravelTriggerInstance::teleportDistance),
				Codec.DOUBLE.optionalFieldOf("jump").forGetter(PortalTravelTriggerInstance::horizontalJump),
				Codec.DOUBLE.optionalFieldOf("flight").forGetter(PortalTravelTriggerInstance::verticalFlight),
				Codec.BOOL.optionalFieldOf("lunacy").forGetter(PortalTravelTriggerInstance::lunacy)
		).apply(instance, PortalTravelTriggerInstance::new));

		public boolean matches(ResourceLocation initialDimension, ResourceLocation destinationDimension,
							   double teleportDistance, double horizontalJump, double verticalFlight, boolean lunacy)
		{
			if(this.initialDimension.isPresent())
			{
				if(!Objects.equals(this.initialDimension.get(), initialDimension))
					return false;
			}

			if(this.destinationDimension.isPresent())
			{
				if(!Objects.equals(this.destinationDimension.get(), destinationDimension))
					return false;
			}

			if(this.teleportDistance.isPresent())
			{
				if(this.teleportDistance.get() > teleportDistance)
					return false;
			}

			if(this.horizontalJump.isPresent())
			{
				if(this.horizontalJump.get() > horizontalJump)
					return false;
			}

			if(this.verticalFlight.isPresent())
			{
				if(this.verticalFlight.get() > verticalFlight)
					return false;
			}

			if(this.lunacy.isPresent())
				if(this.lunacy.get() != lunacy)
					return false;

			return true;
		}
	}
}
