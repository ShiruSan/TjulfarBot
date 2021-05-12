package de.Shiru.TjulfarBot.Events;

import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import org.jetbrains.annotations.NotNull;

public class UnmuteEvent implements GenericEvent  {
    private Member member;
    private Member entity;

    public UnmuteEvent(Member member, Member entity) {
        this.member = member;
        this.entity = entity;
    }

    @NotNull
    @Override
    public JDA getJDA() {
        return BotMain.getInstance().getJda();
    }

    @Override
    public long getResponseNumber() {
        return 0;
    }

    public Member getMember() {
        return member;
    }

    public Member getEntity() {
        return entity;
    }

    public Guild getGuild() {
        return member.getGuild();
    }

}