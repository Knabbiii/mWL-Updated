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
public class SubCommandToggle implements Command {
    ServiceRegister serviceRegister;

    @Override
    public CommandEntity getEntity() {
        return CommandEntity.builder()
                .name(() -> "toggle")
                .regex(() -> "^(enable|disable)?$")
                .completeArgs(() -> new String[]{"enable|disable"})
                .permission(() -> "mwl.command.toggle")
                .handler(this::handle)
                .build();
    }

    @Override
    public boolean handle(CommandContext context) {
        CommandSender commandSender = context.getSender();
        String[] args = context.getArgs();
        String action = (args.length > 0 && !args[0].isEmpty()) ? args[0] : null;

        serviceRegister.getWhitelistActionService().switchWhitelist(commandSender, action);

        return true;
    }
}
