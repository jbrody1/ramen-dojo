package test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;

import glint.RamenDojo;
import problem.Party;
import problem.Restaurant;
import problem.Table;

abstract class AbstractTest
{
	private static final int[] tableSizes = new int[] { 6, 4, 2 };
	private static final float tolerance = 0.05f;

	protected Restaurant buildRestaurant(int numTables)
	{
		Restaurant restaurant;
		//restaurant = new MyRestaurant();
		restaurant = new RamenDojo();
		restaurant.setTables(buildTables(numTables));
		return restaurant;
	}

	protected Set<Table> buildTables(int numTables)
	{
		Set<Table> tables = new HashSet<>();
		while (numTables > 0)
		{
			for (int size : tableSizes)
			{
				tables.add(new Table(size));
				if (--numTables <= 0) break;
			}
		}
		return tables;
	}

	protected DebugCustomer buildCustomer(Party party, long eatTime, long waitLimit)
	{
		DebugCustomer customer = new DebugCustomer(generateId());
		if (party != null) customer.setParty(party);
		customer.setEatTime(eatTime);
		customer.setWaitLimit(waitLimit);
		return customer;
	}

	protected DebugParty buildParty(int size)
	{
		DebugParty party = new DebugParty();
		for (int i=0; i<size; i++) buildCustomer(party, 1000, 5000);
		return party;
	}

	protected Future<?> lineUp(Restaurant restaurant, DebugParty party)
	{
		return lineUp(restaurant, party.getDebugCustomers());
	}

	protected Future<?> lineUp(Restaurant restaurant, DebugCustomer customer)
	{
		return lineUp(restaurant, Arrays.asList(customer));
	}

	protected Future<?> lineUp(Restaurant restaurant, Collection<DebugCustomer> customers)
	{
		List<Future<?>> futures = new ArrayList<>();
		for (DebugCustomer customer : customers)
		{
			futures.add(customer.lineUp(restaurant));
		}
		return new SimpleFuture<Object>(() -> { for (Future<?> future : futures) future.get(); return null; });
	}

	protected long delta(long start)
	{
		return (System.nanoTime() - start) / 1000000;
	}

	protected void assertApprox(long expected, long actual)
	{
		long delta = Math.max((long) (expected * tolerance), 20);
		if (actual > expected + delta || actual < expected - delta) Assert.fail("expected " + expected + ", got " + actual);
	}

	private final AtomicInteger id = new AtomicInteger();

	private String generateId()
	{
		return lpad(Integer.toString(id.incrementAndGet()), 3, '0');
	}

	private String lpad(String str, int len, char padding)
	{
		len -= str.length();
		StringBuffer buf = new StringBuffer();
		while (buf.length() < len) buf.append(0);
		return buf.append(str).toString();
	}
}
