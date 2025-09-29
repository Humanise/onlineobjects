package org.onlineobjects.modules.intelligence;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestAnthropic {

	@Test
	public void test() {
		Anthropic anthropic = new Anthropic();
		String json = "{\"type\":\"content_block_delta\",\"index\":0,\"delta\":{\"type\":\"text_delta\",\"text\":\" causes wetness rather than being inherently wet itself\"}    }";
		String result = anthropic.extract(json);
		assertEquals(" causes wetness rather than being inherently wet itself", result);
	}

}
