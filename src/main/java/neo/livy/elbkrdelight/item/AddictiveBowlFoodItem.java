package neo.livy.elbkrdelight.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * A bowl food that leaves an empty bowl behind and is addictive, matching Endless Backrooms' own
 * royal rations and moth jelly.
 *
 * <p>Extends plain {@link Item} rather than Farmer's Delight's {@code ConsumableItem}: the bowl
 * comes back through {@code craftRemainder(Items.BOWL)}, which vanilla already honours in
 * {@code LivingEntity.eat} and therefore in {@link Item#finishUsingItem}, so this avoids depending
 * on Farmer's Delight's config for tooltip behaviour.
 */
public class AddictiveBowlFoodItem extends Item {
	/** How long the withdrawal lasts after eating, in ticks. */
	private final int withdrawalDurationTicks;

	/** Highest withdrawal amplifier this item can build up to. */
	private final int withdrawalMaxAmplifier;

	public AddictiveBowlFoodItem(Properties properties, int withdrawalDurationTicks, int withdrawalMaxAmplifier) {
		super(properties);
		this.withdrawalDurationTicks = withdrawalDurationTicks;
		this.withdrawalMaxAmplifier = withdrawalMaxAmplifier;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		// Applies the FoodProperties effects and returns the empty bowl through the recipe
		// remainder. Must run first: the food effects are applied by LivingEntity.eat, which only
		// the superclass call reaches.
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide) {
			applyWithdrawal(entity);
		}

		return result;
	}

	/**
	 * Raises the withdrawal amplifier by one step, capped, and refreshes its duration.
	 *
	 * <p>Resolved by resource location rather than by referencing {@code ModEffects.WITHDRAWAL},
	 * because Endless Backrooms ships no sources and compiling against its internal classes would be
	 * fragile.
	 *
	 * <p>1.20.1's {@code addEffect} returns a boolean and gives no way to read back what stuck, so
	 * candidates are offered from the highest amplifier down and the first one that is rejected ends
	 * the search; the accepted one is already in place by then. Vanilla only merges an incoming
	 * instance when it is strictly stronger than the active one, which is why offering the highest
	 * candidate first reproduces the ingredients' exact upgrade behaviour: a weaker candidate cannot
	 * downgrade an existing, stronger withdrawal.
	 */
	private void applyWithdrawal(LivingEntity entity) {
		MobEffect withdrawal = BuiltInRegistries.MOB_EFFECT.get(new ResourceLocation("endless_backrooms", "withdrawal"));

		if (withdrawal == null) {
			throw new IllegalStateException(
					"Missing mob effect endless_backrooms:withdrawal - Endless Backrooms must be installed.");
		}

		MobEffectInstance current = entity.getEffect(withdrawal);
		int nextAmplifier = current == null
				? 0
				: Math.min(current.getAmplifier() + 1, withdrawalMaxAmplifier);

		for (int amplifier = nextAmplifier; amplifier >= 0; amplifier--) {
			if (!entity.addEffect(new MobEffectInstance(withdrawal, withdrawalDurationTicks, amplifier))) {
				return;
			}
		}
	}
}
