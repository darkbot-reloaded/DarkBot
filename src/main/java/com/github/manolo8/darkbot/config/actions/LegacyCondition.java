package com.github.manolo8.darkbot.config.actions;

/**
 * This class exists PURELY to avoid an import mess.
 * Condition implementations need to keep implementing darkbot's Condition (interface)
 * for backwards-compat with older plugins, but if they implement darkbot's Condition directly,
 * Condition.Result is darkbot's condition instead of the API's. To solve this, they will now
 * implement LegacyCondition so that ConditionResult is the api's condition.
 * <br>
 * This will be all removed in a future release, when legacy plugins stop using legacy conditions and have
 * fully migrated to the API's condition type.
 */
@SuppressWarnings("deprecation")
public interface LegacyCondition extends Condition {
}
