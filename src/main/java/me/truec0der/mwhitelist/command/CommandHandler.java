package me.truec0der.mwhitelist.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.truec0der.mwhitelist.command.subcommand.*;
import me.truec0der.mwhitelist.config.ConfigRegister;
import me.truec0der.mwhitelist.config.configs.LangConfig;
import me.truec0der.mwhitelist.service.ServiceRegister;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommandHandler implements CommandExecutor, TabCompleter {
    CommandController commandController;
    ConfigRegister configRegister;
    ServiceRegister serviceRegister;

    public void init() {
        commandController.command(new SubCommandHelp(serviceRegister).getEntity());
        commandController.command(new SubCommandInfo(serviceRegister).getEntity());
        commandController.command(new SubCommandToggle(serviceRegister).getEntity());
        commandController.command(new SubCommandAdd(serviceRegister).getEntity());
        commandController.command(new SubCommandAddTemp(serviceRegister).getEntity());
        commandController.command(new SubCommandSetTemp(serviceRegister).getEntity());
        commandController.command(new SubCommandExtendTemp(serviceRegister).getEntity());
        commandController.command(new SubCommandRemove(serviceRegister).getEntity());
        commandController.command(new SubCommandList(serviceRegister).getEntity());
        commandController.command(new SubCommandCheck(serviceRegister).getEntity());
        commandController.command(new SubCommandReload(serviceRegister).getEntity());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LangConfig langConfig = configRegister.getLangConfig();

        String[] slicedArgs = args.length > 0 ? Arrays.copyOfRange(args, 1, args.length) : new String[0];

        CommandContext commandContext = new CommandContext(sender, slicedArgs);
        Optional<CommandEntity> emptyCommand = commandController.findEmptyCommands().stream().findFirst();

        if (args.length == 0) {
            return handleEmptyCommand(emptyCommand.orElse(null), commandContext);
        }

        List<CommandEntity> foundCommands = commandController.findCommands(args[0]);

        if (foundCommands.isEmpty()) {
            sender.sendMessage(langConfig.getNeedCorrectArgs());
            return true;
        }

        String joinedArgs = StringUtils.join(slicedArgs, " ");

        Optional<CommandEntity> matchingCommand = foundCommands.stream()
                .filter(entity -> Pattern.matches(entity.getRegex().get(), joinedArgs))
                .findFirst();

        if (matchingCommand.isEmpty()) {
            sender.sendMessage(langConfig.getNeedCorrectArgs());
            return true;
        }

        if (!matchingCommand.get().hasPermission(sender)) {
            sender.sendMessage(langConfig.getNotPerms());
            return true;
        }

        matchingCommand.get().execute(commandContext);
        return true;
    }

    private boolean handleEmptyCommand(CommandEntity commandEntity, CommandContext context) {
        LangConfig langConfig = configRegister.getLangConfig();

        CommandSender sender = context.getSender();

        if (commandEntity == null) {
            sender.sendMessage(langConfig.getNeedCorrectArgs());
            return true;
        }

        if (!sender.hasPermission(commandEntity.getPermission().get())) {
            sender.sendMessage(langConfig.getNotPerms());
            return true;
        }

        return commandEntity.execute(context);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) return commandController.findNames(sender);

        String commandName = args[0];
        List<CommandEntity> foundCommands = commandController.findCommands(commandName);

        return foundCommands.stream()
                .filter(entity -> sender.hasPermission(entity.getPermission().get()) && !entity.getName().get().isEmpty())
                .flatMap(entity -> getCompletionList(args, entity).stream())
                .collect(Collectors.toList());
    }

    private List<String> getCompletionList(String[] args, CommandEntity commandEntity) {
        int argIndex = args.length - 2;

        if (argIndex >= 0 && argIndex < commandEntity.getCompleteArgs().get().length) {
            return Arrays.asList(
                    commandEntity
                            .getCompleteArgs().get()[argIndex]
                            .split("\\|")
            );
        }

        return Collections.emptyList();
    }
}
