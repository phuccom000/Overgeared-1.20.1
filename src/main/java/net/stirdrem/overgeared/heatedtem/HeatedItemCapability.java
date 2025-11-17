package net.stirdrem.overgeared.heatedtem;

public class HeatedItemCapability {
    private boolean heated;
    private long heatedSince = -1;
    private long nextCheck = 0;

    public void setHeated(long gameTime) {
        this.heated = true;
        this.heatedSince = gameTime;
        this.nextCheck = gameTime + 40; // first check in 2 seconds
    }

    public boolean isHeated() {
        return heated;
    }

    public long getHeatedSince() {
        return heatedSince;
    }

    public long getNextCheck() {
        return nextCheck;
    }

    public void scheduleNextCheck(long now) {
        this.nextCheck = now + 40 + (long) (Math.random() * 40);
    }

    public void clear() {
        heated = false;
        heatedSince = -1;
        nextCheck = 0;
    }
}
