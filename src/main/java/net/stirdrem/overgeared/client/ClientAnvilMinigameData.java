package net.stirdrem.overgeared.client;

public class ClientAnvilMinigameData {
    private static int progress;

    public static void set(int progress) {
        ClientAnvilMinigameData.progress = progress;
    }

    public static int getProgress() {
        return progress;
    }
}
