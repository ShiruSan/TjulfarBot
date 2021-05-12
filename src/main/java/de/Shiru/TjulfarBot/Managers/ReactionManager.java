package de.Shiru.TjulfarBot.Managers;

import de.Shiru.TjulfarBot.BotMain;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReactionManager {
    private static List<ReactionRole> reactionRoles = new ArrayList<>();
    private static ReactionManager instance;
    private int sqId;

    private Connection getConnection() throws SQLException {
        return BotMain.getInstance().getManager().dataSource.getConnection();
    }

    public static ReactionManager get() {
        if(instance == null) instance = new ReactionManager();
        return instance;
    }

    public void init() {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("CREATE TABLE If NOT EXISTS `Reactions` (" +
                    "`id` integer not null," +
                    "`messageid` bigint not null," +
                    "`channel` bigint not null," +
                    "`role` bigint not null," +
                    "`reaction` varchar(10) not null," +
                    "Primary Key(id));");
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = getConnection().prepareStatement("select * from `Reactions`");
            ResultSet set = preparedStatement.executeQuery();
            while(set.next()) {
                ReactionRole reactionRole = new ReactionRole(set.getInt(1), set.getLong(2), set.getLong(3), set.getLong(4), set.getString(5));
                reactionRoles.add(reactionRole);
                sqId = reactionRole.sqID;
            }
            set.close();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean containsReactionRole(long message, String reaction) {
        for(ReactionRole reactionRole : reactionRoles) {
            if(reactionRole.message.getIdLong() == message && reactionRole.reaction.equals(reaction)) return true;
        }
        return false;
    }

    public void addReactionRole(ReactionRole reactionRole) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("insert into `Reactions` values (?, ?, ?, ?, ?)");
            preparedStatement.setInt(1, reactionRole.sqID);
            preparedStatement.setLong(2, reactionRole.message.getIdLong());
            preparedStatement.setLong(3, reactionRole.channel.getIdLong());
            preparedStatement.setLong(4, reactionRole.role.getIdLong());
            preparedStatement.setString(5, reactionRole.reaction);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            reactionRoles.add(reactionRole);
            sqId = reactionRole.sqID;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeReactionRole(ReactionRole reactionRole) {
        try {
            PreparedStatement preparedStatement = getConnection().prepareStatement("delete from `Reactions` where id = ?");
            preparedStatement.setInt(1, reactionRole.sqID);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            reactionRoles.remove(reactionRole);
            sqId = reactionRole.sqID - 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ReactionRole createReactionRole(Message message, Role role, String reaction) {
        return new ReactionRole(sqId + 1, message, message.getChannel().getIdLong(), role.getIdLong(), reaction);
    }

    public ReactionRole getReactionRole(long message, String reaction) {
        for(ReactionRole reactionRole : reactionRoles) {
            if(reactionRole.message.getIdLong() == message && reactionRole.reaction.equals(reaction)) return reactionRole;
        }
        return null;
    }

    public List<ReactionRole> getReactionRolesWithRole(Role role) {
        List<ReactionRole> reactionRoles2 = new ArrayList<>();
        for(ReactionRole reactionRole : reactionRoles) {
            if(reactionRole.role.getIdLong() == role.getIdLong()) reactionRoles2.add(reactionRole);
        }
        return reactionRoles2;
    }

    public class ReactionRole {
        private int sqID;
        private Message message;
        private TextChannel channel;
        private Role role;
        private String reaction;

        public ReactionRole(int sqID, Message message, long channel, long role, String reaction) {
            this.sqID = sqID;
            this.channel = BotMain.getInstance().getJda().getTextChannelById(channel);
            this.message = message;
            this.role = BotMain.getInstance().getJda().getRoleById(role);
            this.reaction = reaction;
        }

        public ReactionRole(int sqID, long message, long channel, long role, String reaction) {
            this.sqID = sqID;
            this.channel = BotMain.getInstance().getJda().getTextChannelById(channel);
            this.message = this.channel.retrieveMessageById(message).complete();
            this.role = BotMain.getInstance().getJda().getRoleById(role);
            this.reaction = reaction;
        }

        public int getSqID() {
            return sqID;
        }

        public Message getMessage() {
            return message;
        }

        public TextChannel getChannel() {
            return channel;
        }

        public Role getRole() {
            return role;
        }

        public String getReaction() {
            return reaction;
        }
    }

}
