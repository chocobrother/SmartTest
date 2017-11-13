package kr.ac.sch.se.algorithm;

import java.util.LinkedList;
import java.util.Queue;

public class MovingAverage_Filter {
	private Queue<Double> window;
	private int period;
	private double sum;

	public MovingAverage_Filter(int period) {		
		this.window = new LinkedList<Double>();
		this.period = period;
	}
	
	public void newNum(double num) {
		sum += num;
		window.add(num);
		if (window.size() > period) {
			sum -= window.remove();
		}
	}
	
	public double getAvg() {
		if (window.isEmpty())
			return 0; // technically the average is undefined
		return sum / window.size();
	}
}
