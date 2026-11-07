package com.createredline.tier;

/**
 * Mixed into Create's BeltBlockEntity. A belt's tier comes from the shafts it was built between:
 * every segment stores the tier of the shaft it replaced (if it is a pulley, so removal gives that exact shaft
 * back) and the chain tier = the lowest pulley tier, which sets the belt's speed cap.
 */
public interface ITieredBelt {
    KineticTier redline$getBeltTier();

    void redline$setBeltTier(KineticTier tier);

    KineticTier redline$getPulleyTier();

    void redline$setPulleyTier(KineticTier tier);
}
