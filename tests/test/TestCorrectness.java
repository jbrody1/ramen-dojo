package test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.Test;

import problem.Restaurant;
import problem.Table;

public class TestCorrectness extends AbstractTest
{
	private Restaurant restaurant = buildRestaurant(1);
	private DebugParty partyOf3 = buildParty(3);
	private DebugParty partyOf6 = buildParty(6);

	@Test
	public void testOrdering() throws Exception
	{
		Future<?> f1 = lineUp(restaurant, partyOf3);
		Future<?> f2 = lineUp(restaurant, partyOf6);
		long start = System.nanoTime();

		f1.get();
		long t1 = delta(start);

		f2.get();
		long t2 = delta(start);

		assertApprox(1000, t1);
		assertApprox(2000, t2);
	}
	
	@Test
	public void testStraggler() throws Exception
	{
		Set<DebugCustomer> others = partyOf3.getDebugCustomers();
		DebugCustomer straggler = others.iterator().next();
		others.remove(straggler);

		Future<?> f1 = lineUp(restaurant, others);
		Future<?> f2 = lineUp(restaurant, partyOf6);
		long start = System.nanoTime();

		// party of 6 should eat ahead of party of 3
		f2.get();
		long t1 = delta(start);

		// make sure party of 3 has not eaten
		assertFalse(straggler.hasEaten());
		assertFalse(others.iterator().next().hasEaten());
		
		// now the straggler joins
		lineUp(restaurant, straggler);

		f1.get();
		long t2 = delta(start);

		assertApprox(1000, t1);
		assertApprox(2000, t2);
	}

	@Test
	public void testTableSize() throws Exception
	{
		Restaurant restaurant = buildRestaurant(1);
		DebugParty partyOf7 = buildParty(7);
		Future<?> f1 = lineUp(restaurant, partyOf7);
		Future<?> f2 = lineUp(restaurant, partyOf3);
		long start = System.nanoTime();

		// party of 3 should eat ahead of party of 7
		f2.get();
		long t1 = delta(start);

		// make sure party of 7 has not eaten
		assertFalse(partyOf7.getDebugCustomers().iterator().next().hasEaten());
		assertTrue(partyOf3.getDebugCustomers().iterator().next().hasEaten());
		
		// now set a larger table
		Set<Table> tables = new HashSet<>(Arrays.asList(new Table(6), new Table(8)));
		restaurant.setTables(tables);
		f1 = lineUp(restaurant, partyOf7);
		f2 = lineUp(restaurant, partyOf3);

		f1.get();
		long t2 = delta(start);

		assertApprox(1000, t1);
		assertApprox(2000, t2);
		assertTrue(partyOf7.getDebugCustomers().iterator().next().hasEaten());
	}

	@Test
	public void testQueueJumping() throws Exception
	{
		Set<DebugCustomer> others = partyOf3.getDebugCustomers();
		DebugCustomer straggler = others.iterator().next();
		others.remove(straggler);

		Future<?> f1 = lineUp(restaurant, others);
		Future<?> f2 = lineUp(restaurant, partyOf6);
		Future<?> f3 = lineUp(restaurant, buildParty(5));
		long start = System.nanoTime();

		// now the straggler joins
		lineUp(restaurant, straggler);

		// party of 6 should eat ahead of party of 3
		f2.get();
		long t1 = delta(start);

		// make sure party of 3 has not eaten
		assertFalse(straggler.hasEaten());
		assertFalse(others.iterator().next().hasEaten());
		
		f1.get();
		long t2 = delta(start);

		f3.get();
		long t3 = delta(start);

		assertApprox(1000, t1);
		assertApprox(2000, t2);
		assertApprox(3000, t3);
	}

	@Test
	public void testRageQuitAndReturn() throws Exception
	{
		DebugParty partyOf4 = buildParty(3);
		buildCustomer(partyOf4, 1000, 1500);
		
		lineUp(restaurant, partyOf3);
		Future<?> f1 = lineUp(restaurant, partyOf6);
		Future<?> f2 = lineUp(restaurant, partyOf4);
		long start = System.nanoTime();

		f1.get();
		f2.get();
		long t1 = delta(start);

		// make sure the party of 4 has not eaten
		assertApprox(2000, t1);
		for (DebugCustomer customer : partyOf4.getDebugCustomers()) assertFalse(customer.hasEaten());

		// now re-enqueue the party of 4... this would throw an exception if they are still lined up
		Future<?> f3 = lineUp(restaurant, partyOf4);
		f3.get();
		long t3 = delta(start);
		
		assertApprox(3000, t3);
		for (DebugCustomer customer : partyOf4.getDebugCustomers()) assertTrue(customer.hasEaten());
	}

	@Test
	public void testLyingImpostor() throws Exception
	{
		DebugParty partyOf3 = buildParty(3);
		DebugCustomer impostor = buildCustomer(partyOf3, 1000, 5000);
		partyOf3.remove(impostor);

		Set<DebugCustomer> others = partyOf3.getDebugCustomers();
		DebugCustomer straggler = others.iterator().next();
		others.remove(straggler);

		Future<?> f1 = lineUp(restaurant, others);
		long start = System.nanoTime();

		// now the impostor joins
		Future<?> f2 = lineUp(restaurant, impostor);

		// impostor should be rejected immediately
		f2.get();
		long t1 = delta(start);
		assertApprox(0, t1);
		assertFalse(impostor.hasEaten());
		for (DebugCustomer customer : partyOf3.getDebugCustomers()) assertFalse(customer.hasEaten());

		// now the straggler joins
		lineUp(restaurant, straggler);

		// now the impostor tries to join again
		Future<?> f3 = lineUp(restaurant, impostor);

		f1.get();
		f3.get();
		long t2 = delta(start);
		assertApprox(1000, t2);
		assertFalse(impostor.hasEaten());
		for (DebugCustomer customer : partyOf3.getDebugCustomers()) assertTrue(customer.hasEaten());
	}
}
