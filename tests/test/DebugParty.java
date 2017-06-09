package test;

import java.util.HashSet;
import java.util.Set;

import problem.Customer;
import problem.Party;

public class DebugParty extends Party
{
	synchronized void remove(Customer customer)
	{
		customers.remove(customer);
	}

	synchronized Set<DebugCustomer> getDebugCustomers()
	{
		Set<DebugCustomer> customers = new HashSet<>();
		for (Customer customer : getCustomers()) customers.add((DebugCustomer) customer);
		return customers;
	}
}
