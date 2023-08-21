package io.github.e9ae9933.aicd;

public class Pair <A,B>
{
	public A first;
	public B second;

	public Pair(A first, B second)
	{
		this.first = first;
		this.second = second;
	}
	@Override
	public boolean equals(Object o)
	{
		if(!(o instanceof Pair))
			return false;
		return first.equals(((Pair<?, ?>) o).first)&&second.equals(((Pair<?, ?>) o).second);
	}

	@Override
	public String toString()
	{
		return "Pair{" +
				"first=" + first +
				", second=" + second +
				'}';
	}

	@Override
	public int hashCode()
	{
		return first.hashCode()^second.hashCode();
	}

}
