package com.codahale.metrics;

/**
 * A metric which calculates the distribution of a value.
 *
 * @see <a href="http://www.johndcook.com/standard_deviation.html">Accurately
 *      computing running variance</a>
 */
public class Histogram implements Metric, Sampling, Counting {
	protected final Reservoir reservoir;

	/**
	 * Creates a new {@link Histogram} with the given reservoir.
	 *
	 * @param reservoir
	 *            the reservoir to create a histogram from
	 */
	public Histogram(Reservoir reservoir) {
		this.reservoir = reservoir;
	}

	/**
	 * Adds a recorded value.
	 *
	 * @param value
	 *            the length of the value
	 */
	public void update(int value) {
		update((long) value);
	}

	/**
	 * Adds a recorded value.
	 *
	 * @param value
	 *            the length of the value
	 */
	public void update(long value) {
		reservoir.update(value);
	}

	/**
	 * Returns the number of values recorded.
	 *
	 * @return the number of values recorded
	 */
	@Override
	public long getCount() {
		return 0;
	}

	@Override
	public Snapshot getSnapshot() {
		return reservoir.getSnapshot();
	}

}
