package itz.lirdev.tools;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class SchedulerUtil {

    private static final boolean FOLIA;

    private static MethodHandle globalRegionScheduler;
    private static MethodHandle asyncScheduler;
    private static MethodHandle playerScheduler;

    private static MethodHandle globalRunAtFixedRate;
    private static MethodHandle globalRunDelayed;
    private static MethodHandle asyncRunDelayed;
    private static MethodHandle asyncRunNow;
    private static MethodHandle playerRunDelayed;
    private static MethodHandle playerRun;
    private static MethodHandle scheduledTaskCancel;
    private static MethodHandle isOwnedByCurrentRegion;

    private static Class<?> scheduledTaskClass;

    static {
        boolean folia = false;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");

            MethodHandles.Lookup lookup = MethodHandles.publicLookup();

            Class<?> bukkit = Bukkit.class;
            Class<?> playerCls = Player.class;

            Class<?> globalSched = Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            Class<?> asyncSched = Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler");
            Class<?> entitySched = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            Class<?> schedTask = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            scheduledTaskClass = schedTask;

            globalRegionScheduler = lookup.unreflect(bukkit.getMethod("getGlobalRegionScheduler"))
                    .asType(MethodType.methodType(Object.class));
            asyncScheduler = lookup.unreflect(bukkit.getMethod("getAsyncScheduler"))
                    .asType(MethodType.methodType(Object.class));
            playerScheduler = lookup.unreflect(playerCls.getMethod("getScheduler"))
                    .asType(MethodType.methodType(Object.class, Player.class));

            globalRunAtFixedRate = lookup.unreflect(globalSched.getMethod("runAtFixedRate",
                    org.bukkit.plugin.Plugin.class, Consumer.class, long.class, long.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class, long.class, long.class));

            globalRunDelayed = lookup.unreflect(globalSched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class, Consumer.class, long.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class, long.class));

            asyncRunDelayed = lookup.unreflect(asyncSched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class, Consumer.class, long.class, TimeUnit.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class, long.class, TimeUnit.class));

            asyncRunNow = lookup.unreflect(asyncSched.getMethod("runNow",
                    org.bukkit.plugin.Plugin.class, Consumer.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class));

            playerRunDelayed = lookup.unreflect(entitySched.getMethod("runDelayed",
                    org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class, long.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class, long.class));

            playerRun = lookup.unreflect(entitySched.getMethod("run",
                    org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class))
                    .asType(MethodType.methodType(Object.class,
                            Object.class, org.bukkit.plugin.Plugin.class, Consumer.class, Runnable.class));

            scheduledTaskCancel = lookup.unreflect(schedTask.getMethod("cancel"))
                    .asType(MethodType.methodType(Object.class, Object.class));

            try {
                isOwnedByCurrentRegion = lookup.unreflect(
                        bukkit.getMethod("isOwnedByCurrentRegion", org.bukkit.entity.Entity.class))
                        .asType(MethodType.methodType(boolean.class, org.bukkit.entity.Entity.class));
            } catch (NoSuchMethodException | IllegalAccessException ignored) {
            }

            folia = true;
        } catch (Throwable ignored) {
        }

        FOLIA = folia;
    }

    private SchedulerUtil() {
    }

    public static boolean isFolia() {
        return FOLIA;
    }

    public static Object runTaskTimer(JavaPlugin plugin, Runnable task, long delay, long period) {
        if (FOLIA) {
            try {
                Object scheduler = globalRegionScheduler.invoke();
                Consumer<Object> consumer = t -> task.run();
                return globalRunAtFixedRate.invoke(scheduler, plugin, consumer,
                        delay <= 0 ? 1 : delay, period);
            } catch (Throwable e) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    public static Object runTaskLater(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = globalRegionScheduler.invoke();
                Consumer<Object> consumer = t -> task.run();
                return globalRunDelayed.invoke(scheduler, plugin, consumer,
                        delayTicks <= 0 ? 1 : delayTicks);
            } catch (Throwable e) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static Object runTaskLaterForPlayer(JavaPlugin plugin, Player player, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = playerScheduler.invoke(player);
                Consumer<Object> consumer = t -> task.run();
                return playerRunDelayed.invoke(scheduler, plugin, consumer, (Runnable) null,
                        delayTicks <= 0 ? 1 : delayTicks);
            } catch (Throwable e) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    public static Object runAsync(JavaPlugin plugin, Runnable task, long delayTicks) {
        if (FOLIA) {
            try {
                Object scheduler = asyncScheduler.invoke();
                Consumer<Object> consumer = t -> task.run();
                return asyncRunDelayed.invoke(scheduler, plugin, consumer,
                        delayTicks * 50L, TimeUnit.MILLISECONDS);
            } catch (Throwable e) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public static Object runAsync(JavaPlugin plugin, Runnable task) {
        if (FOLIA) {
            try {
                Object scheduler = asyncScheduler.invoke();
                Consumer<Object> consumer = t -> task.run();
                return asyncRunNow.invoke(scheduler, plugin, consumer);
            } catch (Throwable e) {
                return null;
            }
        }
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }

    public static void runForPlayer(JavaPlugin plugin, Player player, Runnable task) {
        if (FOLIA) {
            try {
                if (isOwnedByCurrentRegion != null
                        && (boolean) isOwnedByCurrentRegion.invoke(player)) {
                    task.run();
                    return;
                }
                Object scheduler = playerScheduler.invoke(player);
                Consumer<Object> consumer = t -> task.run();
                playerRun.invoke(scheduler, plugin, consumer, (Runnable) null);
                return;
            } catch (Throwable ignored) {
            }
        }
        task.run();
    }

    public static void cancelTask(Object task) {
        if (task == null) {
            return;
        }
        if (FOLIA && scheduledTaskClass != null && scheduledTaskClass.isInstance(task)) {
            try {
                scheduledTaskCancel.invoke(task);
            } catch (Throwable ignored) {
            }
        } else if (task instanceof BukkitTask) {
            ((BukkitTask) task).cancel();
        }
    }
}