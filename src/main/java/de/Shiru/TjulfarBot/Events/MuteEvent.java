package de.Shiru.TjulfarBot.Events;

import de.Shiru.TjulfarBot.BotMain;
import de.Shiru.TjulfarBot.Commands.Mute;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.GenericEvent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public class MuteEvent implements GenericEvent {
    private Member member;
    private Member entity;
    private Mute.MutedMember mute;
    private int time;
    private TimeUnit timeUnit;
    private String reason;

    public MuteEvent(@Nullable Member member, Member entity, Mute.MutedMember mutedMember, int time, TimeUnit timeUnit, String reason) {
        this.member = member;
        this.entity = entity;
        this.mute = mutedMember;
        this.time = time;
        this.timeUnit = timeUnit;
        this.reason = reason;
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

    public Mute.MutedMember getMute() {
        return mute;
    }

    public Guild getGuild() {
        return member.getGuild();
    }

    public int getTime() {
        return time;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public String getReason() {
        return reason;
    }

}
