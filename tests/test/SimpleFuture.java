package test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimpleFuture<T> extends FutureTask<T>
{
	public SimpleFuture(Callable<T> callable)
	{
		super(callable);
	}
	
	public SimpleFuture(Runnable runnable, T value)
	{
		super(runnable, value);
	}

	@Override
	public T get() throws InterruptedException, ExecutionException
	{
		run();
		return super.get();
	}

	@Override
	public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
	{
		run();
		return super.get(timeout, unit);
	}
}
