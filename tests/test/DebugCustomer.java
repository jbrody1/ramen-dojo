package test;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import problem.Customer;
import problem.Party;
import problem.Restaurant;

class DebugCustomer implements Customer
{
	private final String id;
	private final AtomicReference<Thread> eatThread = new AtomicReference<>();
	private final AtomicBoolean hasEaten = new AtomicBoolean();
	private final AtomicLong enqueuedAt = new AtomicLong();
	private final AtomicLong eatTime = new AtomicLong();
	private final AtomicLong waitLimit = new AtomicLong(Long.MAX_VALUE);
	private final AtomicReference<Party> party = new AtomicReference<>();

	DebugCustomer(String id)
	{
		this.id = id;
	}

	// non-public setters

	void setParty(Party party)
	{
		this.party.set(party);
		party.add(this);
	}

	void setWaitLimit(long waitLimit)
	{
		this.waitLimit.set(waitLimit);
	}

	void setEatTime(long eatTime)
	{
		this.eatTime.set(eatTime);
	}

	// public getters

	@Override
	public Party getParty()
	{
		return party.get();
	}

	@Override
	public long getWaitLimit()
	{
		return waitLimit.get();
	}

	// synchronization logic

	@Override
	public synchronized void eat() throws InterruptedException
	{
		try
		{
			// validation
			long start = System.nanoTime();
			if (hasEaten.get()) throw new IllegalStateException("customer " + id + " allowed to eat multiple times without lining up!");
			if (enqueuedAt.get() == 0) throw new IllegalStateException("customer " + id + " allowed to eat without lining up!");
			if ((start - enqueuedAt.get()) / 1000000 > waitLimit.get()) throw new IllegalStateException("customer " + id + " wait time exceeded!");

			// eat!
			debug("customer " + id + " starting to eat...");
			long eatTime = this.eatTime.get();
			eatThread.set(Thread.currentThread());
			while ((eatTime -= (System.nanoTime() - start) / 1000000) > 0) wait(eatTime);
			hasEaten.set(true);
			debug("customer " + id + " finished eating");
		}
		catch (InterruptedException e)
		{
			debug("customer " + id + " interrupted while eating!");
			throw e;
		}
		finally
		{
			// reset state
			enqueuedAt.set(0);
			eatThread.set(null);
			notifyAll();
		}
	}

	@Override
	public synchronized void reject()
	{
		debug("customer " + id + " rejected");
		Thread eatThread = this.eatThread.get();
		if (eatThread != null) eatThread.interrupt();
		enqueuedAt.set(0);
		hasEaten.set(false);
	}

	synchronized Future<?> lineUp(Restaurant restaurant)
	{
		hasEaten.set(false);

		long now = System.nanoTime();
		if (!this.enqueuedAt.compareAndSet(0, now)) throw new IllegalStateException("customer " + id + " already in line!");

		debug("customer " + id + " lining up...");
		restaurant.lineUp(this);
		return new SimpleFuture<Object>(() -> { return waitUntilFinished(); });
	}

	synchronized boolean hasEaten()
	{
		return hasEaten.get();
	}

	private synchronized boolean waitUntilFinished()
	{
		debug("customer " + id + " waiting to finish...");
		try { while (enqueuedAt.get() > 0 && !hasEaten.get()) wait(); }
		catch (InterruptedException e) { e.printStackTrace(); }
		debug("customer " + id + " finished: " + hasEaten.get());
		return hasEaten.get();
	}

	private void debug(String str)
	{
		System.out.println(str);
	}
}
