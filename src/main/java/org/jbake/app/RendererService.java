package org.jbake.app;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class RendererService implements Runnable {
	
	private final Renderer				renderer;
	private final Map<String, Object>	content;
	private final AtomicInteger			errorCount;
	private final AtomicInteger			renderedCount;

	public RendererService(Renderer renderer, 
			Map<String, Object> content, 
			AtomicInteger errorCount,
			AtomicInteger renderedCount) {
		this.renderer = renderer;
		this.content = content;
		this.errorCount = errorCount;
		this.renderedCount = renderedCount;
	}

	@Override
	public void run() {
		try {
			renderer.render(content);
			renderedCount.incrementAndGet();
		} catch (Exception e) {
			errorCount.incrementAndGet();
		}
	}
	
}