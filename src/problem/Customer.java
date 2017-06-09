package problem;

public interface Customer
{
    public Party getParty();
    public long getWaitLimit();
    public void eat() throws InterruptedException;
    public void reject();
}