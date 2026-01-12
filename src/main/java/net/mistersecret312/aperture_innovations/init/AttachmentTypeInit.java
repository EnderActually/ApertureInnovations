package net.mistersecret312.aperture_innovations.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.mistersecret312.aperture_innovations.ApertureInnovations;
import net.mistersecret312.aperture_innovations.capabilities.ApertureCapability;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class AttachmentTypeInit
{
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(
			NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ApertureInnovations.MODID);

	public static final DeferredHolder<AttachmentType<?>, AttachmentType<ApertureCapability>> APERTURE =
			ATTACHMENT_TYPES.register("aperture",
					() -> AttachmentType.builder(ApertureCapability::new)
										.serialize(new IAttachmentSerializer<CompoundTag, ApertureCapability>() {
											@Override
											public ApertureCapability read(IAttachmentHolder holder, CompoundTag tag,
																		   HolderLookup.Provider provider)
											{
												ApertureCapability capability = new ApertureCapability();
												capability.deserializeNBT(provider, tag);
												return capability;
											}

											@Override
											public @Nullable CompoundTag write(ApertureCapability attachment,
																	   HolderLookup.Provider provider)
											{
												return attachment.serializeNBT(provider);
											}
										})
										.copyOnDeath()
										.build());

	public static void register(IEventBus eventBus)
	{
		ATTACHMENT_TYPES.register(eventBus);
	}
}
