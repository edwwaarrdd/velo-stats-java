package com.velostats.domain.rides.command;

import com.velostats.console.CommandArguments;
import com.velostats.console.ConsoleCommand;
import com.velostats.console.ConsoleOutput;
import com.velostats.domain.rides.message.CheckRideDistance;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.queue.RedisQueue;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CheckRideDistancesCommand implements ConsoleCommand {

    private final RideRepository rides;
    private final RedisQueue queue;
    private final ConsoleOutput output;

    public CheckRideDistancesCommand(RideRepository rides, RedisQueue queue, ConsoleOutput output) {
        this.rides = rides;
        this.queue = queue;
        this.output = output;
    }

    @Override
    public String name() {
        return "rides:check-distances";
    }

    @Override
    public String description() {
        return "Queue a cycling distance lookup for every ride that has not had one.";
    }

    @Override
    public int run(CommandArguments arguments) {
        List<Long> rideIds = rides.idsAwaitingDistanceCheck();

        rideIds.forEach(rideId -> queue.dispatch(new CheckRideDistance(rideId)));

        output.success(String.format("Dispatched %d ride distance check task(s).", rideIds.size()));

        return 0;
    }
}
