package com.github.manolo8.darkbot.config.actions.conditions;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.actions.Condition;
import com.github.manolo8.darkbot.config.actions.Parser;
import com.github.manolo8.darkbot.config.actions.SyntaxException;
import com.github.manolo8.darkbot.config.actions.ValueData;
import com.github.manolo8.darkbot.config.actions.parser.ParseUtil;
import eu.darkbot.api.managers.QuestAPI;
import org.jetbrains.annotations.NotNull;

@ValueData(name = "has-quest", description = "True if it has quests", example = "has-quest()")
public class HasQuestCondition implements Condition, Parser {
    private QuestAPI questApi;

    @Override
    public @NotNull Result get(Main main) {
        if (questApi == null) {
            this.questApi = main.pluginAPI.getAPI(QuestAPI.class);
            if (questApi == null) return Result.ABSTAIN;
        }

        return Result.fromBoolean(this.questApi.getDisplayedQuest() != null);
    }

    @Override
    public String toString() {
        return "has-quest()";
    }

    @Override
    public String parse(String str) throws SyntaxException {
        String[] params = str.split("\\)", 2);
        return ParseUtil.separate(params, getClass(), ")");
    }
}
