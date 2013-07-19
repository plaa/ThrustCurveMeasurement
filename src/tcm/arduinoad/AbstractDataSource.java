package tcm.arduinoad;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import net.sf.openrocket.util.Pair;

public class AbstractDataSource {
	
	private final List<Pair<DataListener, Boolean>> listeners = 
		Collections.synchronizedList(new ArrayList<Pair<DataListener, Boolean>>());

	private final int bufferTime;
	private List<DataVO> dataBuffer = null;
	private long lastFiring = 0;
	
	
	protected AbstractDataSource() {
		this(0);
	}
	
	protected AbstractDataSource(int bufferTime) {
		this.bufferTime = bufferTime;
	}
	
	
	
	public void addDataListener(DataListener listener) {
		this.addDataListener(listener, false);
	}
	
	public void addDataListener(DataListener listener, boolean edt) {
		listeners.add(new Pair<DataListener, Boolean>(listener,edt));
	}
	
	public void removeDataListener(DataListener listener) {
		synchronized (listeners) {
			Iterator<Pair<DataListener, Boolean>> iterator = listeners.iterator();
			while (iterator.hasNext()) {
				Pair<DataListener, Boolean> pair = iterator.next();
				if (listener == pair.getU())
					iterator.remove();
			}
		}
	}
	
	
	public void resetDataBuffer() {
		this.dataBuffer = null;
		this.lastFiring = 0;
	}
	
	@SuppressWarnings("unchecked")
	private void checkForFire() {
		if (System.currentTimeMillis() < lastFiring + bufferTime)
			return;
		
		lastFiring = System.currentTimeMillis();
		
		if (dataBuffer == null || dataBuffer.size() == 0)
			return;
		
		final List<DataVO> data;
		if (dataBuffer != null)
			data = dataBuffer;
		else
			data = new LinkedList<DataVO>();
		
		dataBuffer = new LinkedList<DataVO>();
		
//		System.out.println(this + ": Firing "+data.size()+" blocks of data");
		
		
		Pair<DataListener,Boolean>[] array = listeners.toArray(new Pair[0]);
		for (Pair<DataListener,Boolean> pair: array) {
			final boolean edt = pair.getV();
			final DataListener l = pair.getU();
			
			if (!edt || SwingUtilities.isEventDispatchThread()) {
				
				l.processData(data);
				
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						l.processData(data);
					}
				});
			}
		}

	}
	
	
	protected void fireData(List<DataVO> data) {
//		System.out.println(this + ": Buffering "+data.size()+" blocks of data");
		if (dataBuffer == null) {
			dataBuffer = new LinkedList<DataVO>();
		}
		
		dataBuffer.addAll(data);
		checkForFire();
	}
	
	protected void fireData(final DataVO data) {
//		System.out.println(this + ": Buffering one block of data");
		if (dataBuffer == null) {
			dataBuffer = new LinkedList<DataVO>();
		}
		
		dataBuffer.add(data);
		checkForFire();
	}

}
