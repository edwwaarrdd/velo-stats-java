package com.velostats.domain.weather.command;

import com.velostats.console.CommandArguments;
import com.velostats.console.ConsoleCommand;
import com.velostats.console.ConsoleOutput;
import com.velostats.domain.rides.repository.RideRepository;
import com.velostats.domain.weather.message.CheckRideWeather;
import com.velostats.queue.RedisQueue;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class CheckRideWeatherCommand implements ConsoleCommand {

    private final RideRepository rides;
    private final RedisQueue queue;
    private final ConsoleOutput output;

    public CheckRideWeatherCommand(RideRepository rides, RedisQueue queue, ConsoleOutput output) {
        this.rides = rides;
        this.queue = queue;
        this.output = output;
    }

    @Override
    public String name() {
        return "rides:check-weather";
    }

    @Override
    public String description() {
        return "Queue a weather lookup for every ride that has not had one. Pass --force to re-fetch every ride.";
    }

    @Override
    public int run(CommandArguments arguments) {
        boolean force = arguments.flag("force");
        List<Long> rideIds = force ? rides.allIds() : rides.idsAwaitingWeatherCheck();

        rideIds.forEach(rideId -> queue.dispatch(new CheckRideWeather(rideId, force)));

        output.success(String.format("Dispatched %d ride weather check task(s).", rideIds.size()));

        return 0;
    }
}
