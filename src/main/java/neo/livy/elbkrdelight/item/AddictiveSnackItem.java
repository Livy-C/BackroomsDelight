package neo.livy.elbkrdelight.item;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A hand-held snack that leaves nothing behind when eaten, unlike the bowl meals.
 *
 * <p>Built on plain {@link Item} so that {@code craftRemainder} stays unset: the whole thing is
 * eaten, which is the point of a fried carpet.
 */
public class AddictiveSnackItem extends Item {
	/** How long the withdrawal lasts after eating, in ticks. */
	private final int withdrawalDurationTicks;

	/** Highest withdrawal amplifier this item can build up to. */
	private final int withdrawalMaxAmplifier;

	public AddictiveSnackItem(Properties properties, int withdrawalDurationTicks, int withdrawalMaxAmplifier) {
		super(properties);
		this.withdrawalDurationTicks = withdrawalDurationTicks;
		this.withdrawalMaxAmplifier = withdrawalMaxAmplifier;
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		// super applies the FoodProperties effects, which only LivingEntity.eat reaches.
		ItemStack result = super.finishUsingItem(stack, level, entity);

		if (!level.isClientSide) {
			Withdrawal.apply(entity, withdrawalDurationTicks, withdrawalMaxAmplifier);
		}

		return result;
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
		super.appendHoverText(stack, level, tooltip, flag);
		FoodEffectTooltip.append(stack, tooltip);
	}
}
