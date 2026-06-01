package itz.lirdev.tools;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerUtil {

    private static final boolean FOLIA;

    private static Method globalRegionScheduler;
    private static Method asyncScheduler;
    private static Method playerScheduler;

    private static Method globalRunAtFixedRate;
    private static Method globalRunDelayed;
    private static Method asyncRunDelayed;
    private static Method asyncRunNow;
    private static Method playerRunDelayed;
    private static Method scheduledTaskCancel;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

            Class<?> bukkit = Bukkit.class;
            Class<?> playerCls = org.bukkit.entity.Player.class;

            Class<?> globalSched = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> asyncSched = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> entitySched = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> schedTask = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");

            globalRegionScheduler = bukkit.getMethod("getGlobalRegionScheduler");
            asyncScheduler = bukkit.getMethod("getAsyncScheduler");
            playerScheduler = playerCls.getMethod("getScheduler");

            globalRunAtFixedRate = globalSched.getMethod("runAtFixedRate",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class, long.class, long.class);
            globalRunDelayed = globalSched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class, long.class);

            asyncRunDelayed = asyncSched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class, long.class, TimeUnit.class);
            asyncRunNow = asyncSched.getMethod("runNow",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class);

            playerRunDelayed = entitySched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class,
                    java.util.function.Consumer.class, Runnable.class, long.class);

            scheduledTaskCancel = schedTask.getMethod("cancel");

            folia = true;
        } catch (Throwable ignored) {
        }

        FOLIA = folia;
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static Object runTaskTimer(JavaPlugin plugin, Runnable task, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = globalRegionScheduler.invoke(null);
                return globalRunAtFixedRate.invoke(scheduler, plugin,
                        (java.util.function.Consumer<?>) t -> task.run(),
                        delay == 0 ? 1 : delay, period);
            } catch (Exception ignored) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public static void cancelTask(Object task) {
        if (task == null) {
            return;
        }
        if (FOLIA) {
            try {
                scheduledTaskCancel.invoke(task);
            } catch (Exception ignored) {
            }
        } else {
            ((BukkitTask) task).cancel();
        }
    }

    public static void runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = globalRegionScheduler.invoke(null);
                globalRunDelayed.invoke(scheduler, plugin,
                        (java.util.function.Consumer<?>) t -> task.run(),
                        delayTicks == 0 ? 1 : delayTicks);
            } catch (Exception ignored) {
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runTaskLaterForPlayer(JavaPlugin plugin, Player player, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = playerScheduler.invoke(player);
                playerRunDelayed.invoke(scheduler, plugin,
                        (java.util.function.Consumer<?>) t -> task.run(),
                        (Runnable) null,
                        delayTicks == 0 ? 1 : delayTicks);
            } catch (Exception ignored) {
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    public static void runAsync(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = asyncScheduler.invoke(null);
                asyncRunDelayed.invoke(scheduler, plugin,
                        (java.util.function.Consumer<?>) t -> task.run(),
                        delayTicks * 50L, TimeUnit.MILLISECONDS);
            } catch (Exception ignored) {
            }
        } else {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        }
    }

    public static void runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            try {
                Object scheduler = asyncScheduler.invoke(null);
                asyncRunNow.invoke(scheduler, plugin,
                        (java.util.function.Consumer<?>) t -> task.run());
            } catch (Exception ignored) {
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}
