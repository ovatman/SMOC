package tr.edu.itu.cloudcorelab.geoclient.shell;

public class Analyzer {

    private double circularBuffer[];
	private double avg;
	private int circularIndex;
	private int count;
	private boolean isFirstCall;

	/**
	 * Calculates moving average from the most recent samples.
	 * <p>
	 * Constructor creates circular buffer for storing recent samples.
	 * @param len - buffer size
	 */
	public Analyzer(int len) {
		circularBuffer = new double[len];
		count = 0;
		isFirstCall = true;
		circularIndex = 0;
		avg = 0;
	}

	/** Get the current moving average. */
	public double getValue() {
		return avg;
	}

	/** Calculate moving average and store last value in circular buffer. */
	public void pushValue(double x) {
		if (isFirstCall) {
			primeBuffer(x);
			isFirstCall = false;
		}
		count++;
		double lastValue = circularBuffer[circularIndex];
		avg = avg + (x - lastValue) / circularBuffer.length;
		circularBuffer[circularIndex] = x;
		circularIndex = nextIndex(circularIndex);
	}

	public long getCount() {
		return count;
	}

	private void primeBuffer(double val) {
		for (int i = 0; i < circularBuffer.length; ++i) {
			circularBuffer[i] = val;
		}
		avg = val;
	}

	private int nextIndex(int curIndex) {
		if (curIndex + 1 >= circularBuffer.length) {
			return 0;
		}
		return curIndex + 1;
	}

}
