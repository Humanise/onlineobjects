package dk.in2isoft.onlineobjects.modules.organic;

import java.util.List;

import com.google.common.collect.Lists;

public class Environment implements HeartBeating {
	
	private List<HeartBeating> beating;
	
	private List<Cell> cells;
	
	public Environment() {
		beating = Lists.newArrayList();
		cells = Lists.newArrayList();
	}

	public void beat() {
		for (HeartBeating thing : beating) {
			thing.beat();
		}
	}
	
	public void addCell(Cell cell) {
		cells.add(cell);
		beating.add(cell);
	}
}
