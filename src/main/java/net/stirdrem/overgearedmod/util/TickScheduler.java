package net.stirdrem.overgearedmod.util;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.LinkedList;
import java.util.Queue;

public class TickScheduler {
    // An entry that holds an action and delay counter
    private static class ScheduledTask {
        int remainingTicks;
        Runnable action;

        ScheduledTask(int delayTicks, Runnable action) {
            this.remainingTicks = delayTicks;
            this.action = action;
        }
    }

    private static final Queue<ScheduledTask> taskQueue = new LinkedList<>();

    // Call this from anywhere to schedule a delayed action
    public static void schedule(int delayTicks, Runnable action) {
        if (action != null && delayTicks >= 0) {
            taskQueue.add(new ScheduledTask(delayTicks, action));
        }
    }

    // Call this to clear all pending tasks (optional utility)
    public static void clear() {
        taskQueue.clear();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        int size = taskQueue.size();
        for (int i = 0; i < size; i++) {
            ScheduledTask task = taskQueue.poll();
            if (task == null) continue;

            task.remainingTicks--;

            if (task.remainingTicks <= 0) {
                try {
                    task.action.run(); // Run the task
                } catch (Exception e) {
                    e.printStackTrace(); // Debug if needed
                }
            } else {
                taskQueue.add(task); // Reschedule for later
            }
        }
    }


}
