package dk.in2isoft.in2igui.jsf;

import java.io.IOException;

import jakarta.faces.component.FacesComponent;
import jakarta.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;

import dk.in2isoft.commons.jsf.AbstractComponent;
import dk.in2isoft.commons.jsf.ClassBuilder;
import dk.in2isoft.commons.jsf.Components;
import dk.in2isoft.commons.jsf.Dependencies;
import dk.in2isoft.commons.jsf.ScriptWriter;
import dk.in2isoft.commons.jsf.StyleBuilder;
import dk.in2isoft.commons.jsf.TagWriter;
import dk.in2isoft.commons.lang.Strings;

@FacesComponent(value = ButtonComponent.TYPE)
@Dependencies(js = { "/hui/js/hui_animation.js", "/hui/js/Button.js" }, css = { "/hui/css/button.css" }, requires = { HUIComponent.class }, uses = { SymbolComponent.class })
public class ButtonComponent extends AbstractComponent {

	public static final String TYPE = "hui.button";

	private String text;
	private String name;
	private boolean highlighted;
	private boolean large;
	private boolean small;
	private boolean mini;
	private boolean tiny;
	private boolean disabled;
	private String click;
	private boolean submit;
	private String styleClass;
	private String variant;
	private Integer left;
	private Integer right;
	private String href;
	private String symbol;

	public ButtonComponent() {
		super(TYPE);
	}

	@Override
	public void restoreState(Object[] state) {
		text = (String) state[0];
		name = (String) state[1];
		highlighted = (Boolean) state[2];
		small = (Boolean) state[3];
		click = (String) state[4];
		styleClass = (String) state[5];
		submit = (Boolean) state[6];
		variant = (String) state[7];
		left = (Integer) state[8];
		right = (Integer) state[9];
		mini = (Boolean) state[10];
		tiny = (Boolean) state[11];
		disabled = (Boolean) state[12];
		large = (Boolean) state[13];
		symbol = (String) state[14];
	}

	@Override
	public Object[] saveState() {
		return new Object[] { text, name, highlighted, small, click, styleClass, submit, variant, left, right, mini, tiny, disabled, large, symbol };
	}

	@Override
	public void encodeBegin(FacesContext context, TagWriter out) throws IOException {
		encodeMarkup(context, out);
		ConfirmComponent confirm = Components.getChild(this, ConfirmComponent.class);
		String name = getName(context);
		String click = getClick(context);
		if (confirm==null && Strings.isBlank(name) && Strings.isBlank(click) && !submit) {
			return;
		}
		String id = getClientId();
		ScriptWriter js = out.getScriptWriter();
		js.startScript();
		js.startNewObject(id, "hui.ui.Button").property("element", id);
		js.comma().property("submit", submit);
		if (name != null) {
			js.comma().property("name", name);
		}
		if (styleClass != null) {
			js.comma().property("class", styleClass);
		}
		if (confirm != null) {
			String confirmation = Strings.asNonBlank(confirm.getText(context), "Are you sure?");
			String okText = Strings.asNonBlank(confirm.getOkText(context), "OK");
			String canceltext = Strings.asNonBlank(confirm.getCancelText(context), "Cancel");
			js.write(",confirm:{text:'").writeScriptString(confirmation).write("',okText:'").writeScriptString(okText).write("',cancelText:'").writeScriptString(canceltext)
					.write("'}");
		}
		if (StringUtils.isNotBlank(click)) {
			js.comma().write("listener : {$click:function(widget) {").write(click).write("}}");
		}
		js.endNewObject();
		js.endScript();
	}

	public void encodeMarkup(FacesContext context, TagWriter writer) throws IOException {
		String id = getClientId();
		String cls = buildClass();
		String href = getExpression("href", this.href, context);
		if (Strings.isNotBlank(href)) {
			writer.startA(cls).withHref(href);
		} else {
			writer.startVoidA(cls);
		}
		writer.withId(id);
		if (left != null || right != null) {
			StyleBuilder css = new StyleBuilder();
			css.withMarginLeft(left).withMarginRight(right);
			writer.withStyle(css);
		}
		writer.withTestName(testName);
		if (symbol != null) {
			writer.startSpan("hui_symbol");
			writer.startElement("svg").withAttribute("xmlns", "http://www.w3.org/2000/svg").withAttribute("viewBox", "0 0 32 32");
			writer.startElement("use").withAttribute("xlink:href", "/hui/symbols/all.svg#icon-" + symbol).endElement("use");
			writer.endElement("svg");
			writer.endSpan();
		}
		String text = getText(context);
		writer.write(text);
		writer.endA();
	}

	private String buildClass() {
		ClassBuilder cls = new ClassBuilder("hui_button").add(styleClass);
		if ("light".equals(variant)) {
			cls.add("hui-light");
		}
		if (small) {
			cls.add("hui-small");
		} else if (mini) {
			cls.add("hui-mini");
		} else if (tiny) {
			cls.add("hui-tiny");
		} else if (large) {
			cls.add("hui-large");
		}
		if (highlighted) {
			cls.add("hui-highlighted");
		}
		if (disabled) {
			cls.add("hui-disabled");
		}
		return cls.toString();
	}

	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public boolean isHighlighted() {
		return highlighted;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public String getText(FacesContext context) {
		return Components.getBindingAsString(this, "text", text, context);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getName(FacesContext context) {
		return Components.getBindingAsString(this, "name", name, context);
	}

	public void setSmall(boolean small) {
		this.small = small;
	}

	public boolean isSmall() {
		return small;
	}

	public void setClick(String click) {
		this.click = click;
	}

	public String getClick() {
		return click;
	}

	public String getClick(FacesContext context) {
		return getExpression("click", click, context);
	}

	public void setSubmit(boolean submit) {
		this.submit = submit;
	}

	public boolean isSubmit() {
		return submit;
	}

	public void setStyleClass(String styleClass) {
		this.styleClass = styleClass;
	}

	public String getStyleClass() {
		return styleClass;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getVariant() {
		return variant;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

	public boolean isMini() {
		return mini;
	}

	public void setMini(boolean mini) {
		this.mini = mini;
	}

	public boolean isTiny() {
		return tiny;
	}

	public void setTiny(boolean tiny) {
		this.tiny = tiny;
	}

	public boolean isLarge() {
		return large;
	}

	public void setLarge(boolean large) {
		this.large = large;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getSymbol() {
		return symbol;
	}
}
