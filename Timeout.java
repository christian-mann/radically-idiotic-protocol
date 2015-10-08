
class Timeout implements Runnable {
	private Runnable r;
	private long millis;

	public Timeout(Runnable r, long millis) {
		this.r = r;
		this.millis = millis;
	}

	public void run() {
		try {
			Thread.sleep(millis);
			r.run();
		} catch (InterruptedException ex) {
			;
		}
	}
}