package dk.in2isoft.onlineobjects.apps.developer.views;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Lists;

import dk.in2isoft.onlineobjects.apps.community.jsf.AbstractManagedBean;
import dk.in2isoft.onlineobjects.ui.jsf.model.Option;

public class ComponentTestView extends AbstractManagedBean implements InitializingBean {
	

	public void afterPropertiesSet() throws Exception {
	}

	public Date getDate() {
		return new Date();
	}
	
	public List<Option> getOptions() {
		List<Option> options = Lists.newArrayList();
		for (int i = 0; i < 10; i++) {
			options.add(new Option(i, "Option " + i));
		}
		return options;
	}
}
