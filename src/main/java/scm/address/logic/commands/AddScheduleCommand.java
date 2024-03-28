package scm.address.logic.commands;

import scm.address.model.Model;
import scm.address.model.schedule.Schedule;

/**
 * A command to add a schedule to the address book.
 * <p>
 * This command allows users to add a schedule with a title, description,
 * start datetime, and end datetime to the address book.
 */
public class AddScheduleCommand extends Command {

    public static final String COMMAND_WORD = "add_schedule";
    private final Schedule schedule;

    public AddScheduleCommand(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public CommandResult execute(Model model) {
        model.addSchedule(schedule);
        return new CommandResult("Added schedule: " + schedule);
    }

    public boolean scheduleExists() {
        return schedule != null;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof AddScheduleCommand)) {
            return false;
        }
        AddScheduleCommand test = (AddScheduleCommand) object;
        return this.schedule.equals(test.schedule);
    }
}
