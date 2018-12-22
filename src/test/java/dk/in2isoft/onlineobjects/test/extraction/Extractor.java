package dk.in2isoft.onlineobjects.test.extraction;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dk.in2isoft.onlineobjects.modules.information.ContentExtractor;
import dk.in2isoft.onlineobjects.test.extraction.TestExtractionComparison.Info;
import dk.in2isoft.onlineobjects.test.extraction.TestExtractionComparison.Lines;

class Extractor {
	private ContentExtractor extractor;
	String name;
	private int count;
	private double total;
	private double min = Double.MAX_VALUE;
	private double max = Double.MIN_VALUE;
	private long time;
	
	private int missing;
	private int extra;
	private List<ReportTest> tests = new ArrayList<>();

	public Extractor(String name, ContentExtractor extractor) {
		super();
		this.name = name;
		this.extractor = extractor;
	}
	
	public String getStatus() {
		DecimalFormat df = new DecimalFormat("#,###,##0.00");
		return name+" | min : "+df.format(min*100)+"%, max : "+df.format(max*100)+"%, average : "+df.format(total/count*100)+"%, missing: " + this.missing + ", extra: " + this.extra + ", time: "+df.format(((double)this.time)/1000000.0)+"ms";
	}

	public String getName() {
		return name;
	}
	
	public void addTime(long time) {
		this.time += time;
	}
	
	public ContentExtractor getExtractor() {
		return extractor;
	}

	public void addComparison(double comparison) {
		min = Math.min(min, comparison);
		max = Math.max(max, comparison);
		count++;
		total+=comparison;
	}
	
	public void addResult(String name, Info info, Lines lines, double comparison) {
		ReportTest test = new ReportTest();
		test.name = name;
		test.url = info.url;
		test.lines = lines;
		test.comparison = comparison;
		tests.add(test);
	}
	
	public List<ReportTest> getTests() {
		return tests;
	}
	
	public List<ReportTest> getTestsByComparison() {
		return tests.stream().sorted((a,b) -> {
			if (a.comparison - b.comparison == 0) return 0; 
			return a.comparison - b.comparison > 0 ? 1 : -1;
		}).collect(Collectors.toList());
	}
	
	public void lines(Lines lines) {
		this.missing += lines.missing.size();
		this.extra += lines.extra.size();
	}
	
	public int getMissing() {
		return missing;
	}
	
	public int getExtra() {
		return extra;
	}
	
	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	public double getAverage() {
		return total/count;
	}
}