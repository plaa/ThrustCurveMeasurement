package tcm.configuration;

public abstract class CloneableConfiguration implements Configuration, Cloneable {
	
	@Override
	public Configuration copy() {
		try {
			return (Configuration) clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
	
}
