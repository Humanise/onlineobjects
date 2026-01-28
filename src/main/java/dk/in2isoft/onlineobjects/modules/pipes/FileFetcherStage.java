package dk.in2isoft.onlineobjects.modules.pipes;

public class FileFetcherStage extends PipelineStageAdapter {

	private String url;

	public FileFetcherStage(String url) {
		this.url = url;
	}

	@Override
	public void run() {
		/*
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod();
		File temp = null;
		OutputStream outputStream = null;
		try {
			context.info(this, "Starting");
			method.setURI(new URI(url,true));
			client.executeMethod(method);
			method.getResponseBodyAsStream();
			temp = File.createTempFile(getClass().getName(), "txt");
			outputStream = new FileOutputStream(temp);
			IOUtils.copy(method.getResponseBodyAsStream(), outputStream);
			context.info(this, "Finished");
			context.forvardFile(temp);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (temp!=null) {
				temp.delete();
				context.info(this, "Cleaning");
			}
		}*/

	}
}
