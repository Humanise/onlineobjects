package dk.in2isoft.onlineobjects.model;



public class Statement extends Entity implements TextHolding {

	public static String TYPE = Entity.TYPE+"/Statement";
	public static String NAMESPACE = Entity.NAMESPACE+"Statement/";
	
	private String text;
	
	public Statement() {
		super();
	}

	public String getType() {
		return TYPE;
	}

	/* (non-Javadoc)
	 * @see dk.in2isoft.onlineobjects.model.TextHolding#getText()
	 */
	@Override
	public String getText() {
		return text;
	}

	/* (non-Javadoc)
	 * @see dk.in2isoft.onlineobjects.model.TextHolding#setText(java.lang.String)
	 */
	@Override
	public void setText(String text) {
		this.text = text;
	}
}
