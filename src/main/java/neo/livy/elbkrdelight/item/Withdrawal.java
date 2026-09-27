package neo.livy.elbkrdelight.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * Applies the withdrawal effect that everything this mod makes out of Endless Backrooms
 * ingredients shares, mirroring how that mod's own royal rations and moth jelly work.
 *
 * <p>Resolved by resource location rather than by referencing {@code ModEffects.WITHDRAWAL},
 * because Endless Backrooms ships no sources and compiling against its internal classes would be
 * fragile.
 */
final class Withdrawal {
	private static final ResourceLocation ID = new ResourceLocation("endless_backrooms", "withdrawal");

	private Withdrawal() {
	}

	/**
	 * Raises the entity's withdrawal amplifier by one step, capped, and refreshes its duration.
	 *
	 * <p>1.20.1's {@code addEffect} returns a boolean and gives no way to read back what stuck, so
	 * candidates are offered from the highest amplifier down and the first one that is rejected ends
	 * the search; the accepted one is already in place by then. Vanilla only merges an incoming
	 * instance when it is strictly stronger than the active one, which is why offering the highest
	 * candidate first reproduces the ingredients' exact upgrade behaviour: a weaker candidate cannot
	 * downgrade an existing, stronger withdrawal.
	 *
	 * @param entity       the entity that just ate
	 * @param durationTicks how long the withdrawal lasts
	 * @param maxAmplifier  highest amplifier it is allowed to build up to
	 */
	static void apply(LivingEntity entity, int durationTicks, int maxAmplifier) {
		MobEffect withdrawal = BuiltInRegistries.MOB_EFFECT.get(ID);

		if (withdrawal == null) {
			throw new IllegalStateException(
					"Missing mob effect endless_backrooms:withdrawal - Endless Backrooms must be installed.");
		}

		MobEffectInstance current = entity.getEffect(withdrawal);
		int nextAmplifier = current == null ? 0 : Math.min(current.getAmplifier() + 1, maxAmplifier);

		for (int amplifier = nextAmplifier; amplifier >= 0; amplifier--) {
			if (!entity.addEffect(new MobEffectInstance(withdrawal, durationTicks, amplifier))) {
				return;
			}
		}
	}
}
