package dk.in2isoft.onlineobjects.test;

import org.apache.log4j.Logger;

import dk.in2isoft.commons.util.GraphUtil;
import junit.framework.TestCase;

public class TestGraph extends TestCase {

	private static Logger log = Logger.getLogger(TestGraph.class);
	
	public void testBasic() throws Exception {
		String dot = "digraph finite_state_machine {\n"+
			"graph [normalize=true, outputorder=edgesfirst, overlap=false, pack=false, packmode=\"node\", sep=\"1\", splines=true, size=\"6,6\"]"+
			   	"node [shape = doublecircle]; LR_0 LR_3 LR_4 LR_8;"+
			   	"node [shape = circle];"+
			   	"LR_0 -> LR_2 [ label = \"SS(B)\" ];"+
			   	"LR_0 -> LR_1 [ label = \"SS(S)\" ];"+
			   	"LR_1 -> LR_3 [ label = \"S($end)\" ];"+
			   	"LR_2 -> LR_6 [ label = \"SS(b)\" ];"+
			   	"LR_2 -> LR_5 [ label = \"SS(a)\" ];"+
			   	"LR_2 -> LR_4 [ label = \"S(A)\" ];"+
			   	"LR_5 -> LR_7 [ label = \"S(b)\" ];"+
			   	"LR_5 -> LR_5 [ label = \"S(a)\" ];"+
			   	"LR_6 -> LR_6 [ label = \"S(b)\" ];"+
			   	"LR_6 -> LR_5 [ label = \"S(a)\" ];"+
			   	"LR_7 -> LR_8 [ label = \"S(b)\" ];"+
			   	"LR_7 -> LR_5 [ label = \"S(a)\" ];"+
			   	"LR_8 -> LR_6 [ label = \"S(b)\" ];"+
			   	"LR_8 -> LR_5 [ label = \"S(a)\" ];"+
			   "}";
		String svg = GraphUtil.dotToSvg(dot);
		log.debug(svg);
	}
}