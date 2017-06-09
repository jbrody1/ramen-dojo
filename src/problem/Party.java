package problem;

import java.util.HashSet;
import java.util.Set;

public class Party
{
	protected final Set<Customer> customers = new HashSet<>();

	public synchronized void add(Customer customer)
	{
		customers.add(customer);
	}

	public synchronized Set<Customer> getCustomers()
	{
		return new HashSet<>(customers);
	}
}
