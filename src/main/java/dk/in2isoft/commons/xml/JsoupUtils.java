package dk.in2isoft.commons.xml;

import javax.xml.parsers.DocumentBuilderFactory;

import org.jsoup.nodes.Attribute;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * Helper class to transform a {@link org.jsoup.nodes.Document} to a {@link org.w3c.dom.Document org.w3c.dom.Document},
 * for integration with toolsets that use the W3C DOM.
 */
public class JsoupUtils {
    protected DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    
    public nu.xom.Document toXOM(org.jsoup.nodes.Document in) {
    	org.jsoup.nodes.Element inRoot = in.child(0);
		String tagName = inRoot.tagName();
		nu.xom.Element root = new nu.xom.Element(tagName, "http://www.w3.org/1999/xhtml");

        NodeTraversor.traverse(new XOMBuilder(root), in.child(0));
    			
    	return new nu.xom.Document(root);
    }

    /**
     * Implements the conversion by walking the input.
     */
    protected static class XOMBuilder implements NodeVisitor {

        private nu.xom.Element dest;

        public XOMBuilder(nu.xom.Element root) {
            this.dest = root;
        }

        public void head(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element sourceEl = (org.jsoup.nodes.Element) source;

                String tagName = sanitizeTagName(sourceEl.tagName());
                if (tagName.contains(":")) {
                	tagName = tagName.split(":")[1];
                }
                nu.xom.Element el = new nu.xom.Element(tagName, "http://www.w3.org/1999/xhtml");
                copyAttributes(sourceEl, el);
                dest.appendChild(el);
                dest = el; // descend
            } else if (source instanceof org.jsoup.nodes.TextNode) {
                org.jsoup.nodes.TextNode sourceText = (org.jsoup.nodes.TextNode) source;
                nu.xom.Text text = new nu.xom.Text(sanitize(sourceText.getWholeText()));
                dest.appendChild(text);
            } else if (source instanceof org.jsoup.nodes.Comment) {
            	// TODO: Comment data cannot contain carriage returns. (\r)
            	// http://www.tfcbooks.com/tesla/1926-01-30.htm
                //org.jsoup.nodes.Comment sourceComment = (org.jsoup.nodes.Comment) source;
                //nu.xom.Comment comment = new nu.xom.Comment(sourceComment.getData());
                //dest.appendChild(comment);
            } else if (source instanceof org.jsoup.nodes.DataNode) {
                org.jsoup.nodes.DataNode sourceData = (org.jsoup.nodes.DataNode) source;
                nu.xom.Text node = new nu.xom.Text(sanitize(sourceData.getWholeData()));
                dest.appendChild(node);
            } else {
                // unhandled
            }
        }

        private String sanitizeTagName(String tagName) {
			return tagName.replaceAll("[^a-zA-Z0-9\\-:]","");
		}

		private String sanitize(String str) {
			
            char[] data = str.toCharArray();
            for (int i = 0, len = data.length; i < len; i++) {
                int ch = data[i];
                if (!isValidChar(ch)) {
                	data[i] = ' ';
                }
            }
			return new String(data);
		}

		private boolean isValidChar(int code) {
			return code == 0x9 ||
			        code == 0xA ||
			        code == 0xD ||
			        code >= 0x20;
		}


		public void tail(org.jsoup.nodes.Node source, int depth) {
            if (source instanceof org.jsoup.nodes.Element && dest.getParent() instanceof nu.xom.Element) {
                dest = (nu.xom.Element) dest.getParent(); // undescend. cromulent.
            }
        }

        private void copyAttributes(org.jsoup.nodes.Node source, nu.xom.Element el) {
            for (Attribute attribute : source.attributes()) {
                // valid xml attribute names are: ^[a-zA-Z_:][-a-zA-Z0-9_:.]
                String key = attribute.getKey().replaceAll("[^-a-zA-Z0-9_:.]", "");
                if (key.matches("[a-zA-Z_][-a-zA-Z0-9_.]*") && !key.equals("xmlns") && !key.equals("xml")) {
					String value  = attribute.getValue();
					el.addAttribute(new nu.xom.Attribute(key, value == null ? "" : sanitize(value)));
				}
            }
        }

    }
}