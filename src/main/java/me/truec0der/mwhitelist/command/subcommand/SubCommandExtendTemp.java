package me.truec0der.mwhitelist.command.subcommand;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.command.Command;
import me.truec0der.mwhitelist.command.CommandContext;
import me.truec0der.mwhitelist.command.CommandEntity;
import me.truec0der.mwhitelist.service.ServiceRegister;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SubCommandExtendTemp implements Command {
    ServiceRegister serviceRegister;

    @Override
    public CommandEntity getEntity() {
        return CommandEntity.builder()
                .name(() -> "extendtemp")
                .regex(() -> "^(\\S+)\\s+(.+)$")
                .completeArgs(() -> new String[0])
                .permission(() -> "mwl.command.extendtemp")
                .handler(this::handle)
                .build();
    }

    @Override
    public boolean handle(CommandContext context) {
        CommandSender commandSender = context.getSender();
        String nickname = context.getArgs()[0];

        serviceRegister.getWhitelistActionService().extendPlayerTemp(
                commandSender,
                nickname,
                context.getArgs()
        );

        return true;
    }
}
