package io.github.tasostilsi.bookstore.data;

import io.qameta.allure.Allure;
import net.datafaker.Faker;

import java.util.Random;

/**
 * One faker for the JVM, seeded once. The seed is printed at startup and attached to every payload,
 * so a run that fails on generated data can be replayed with {@code -Ddata.seed=<seed>}.
 */
public final class RandomData {

    private static final long SEED = Long.getLong("data.seed", new Random().nextLong());
    private static final Faker FAKER = new Faker(new Random(SEED));

    static {
        System.out.println("[data] faker seed " + SEED + " (rerun with -Ddata.seed=" + SEED + ")");
    }

    private RandomData() {
    }

    public static Faker faker() {
        return FAKER;
    }

    public static void attachSeed() {
        Allure.attachment("faker seed", "text/plain", Long.toString(SEED));
    }
}
