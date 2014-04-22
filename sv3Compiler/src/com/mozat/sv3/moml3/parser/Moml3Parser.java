package com.mozat.sv3.moml3.parser;

import java.util.Stack;

import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.RemarkNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;

import com.mozat.sv3.moml3.tags.Anchor;
import com.mozat.sv3.moml3.tags.Br;
import com.mozat.sv3.moml3.tags.Button;
import com.mozat.sv3.moml3.tags.Checkbox;
import com.mozat.sv3.moml3.tags.Div;
import com.mozat.sv3.moml3.tags.Hr;
import com.mozat.sv3.moml3.tags.IMoml3Tag;
import com.mozat.sv3.moml3.tags.Img;
import com.mozat.sv3.moml3.tags.Input;
import com.mozat.sv3.moml3.tags.Moscript;
import com.mozat.sv3.moml3.tags.Option;
import com.mozat.sv3.moml3.tags.Page;
import com.mozat.sv3.moml3.tags.Select;
import com.mozat.sv3.moml3.tags.Span;
import com.mozat.sv3.moml3.tags.Spring;
import com.mozat.sv3.moml3.tags.TagUtil;
import com.mozat.sv3.moml3.tags.Title;
import com.mozat.sv3.moml3.tokens.BaseToken;
import com.mozat.sv3.moml3.tokens.ContentToken;
import com.mozat.sv3.moml3.tokens.DummyToken;
import com.mozat.sv3.moml3.tokens.TagToken;
import com.mozat.sv3.moml3.tokens.TagToken.TagType;

public class Moml3Parser {

	Lexer tokenizer;
	String src;

	public Moml3Parser(String src) {
		this.src = src;
		tokenizer = new Lexer(src);
	}

	public ParseResult parseSafely() {
		return parseSafely(ErrorLevel.FatalError);
	}

	public ParseResult parseSafely(ErrorLevel level) {
		try {
			return parse(level);
		} catch (Moml3Exception e) {
			ParseResult result = new ParseResult(null, e);
			return result;
		}
	}

	public ParseResult parse(ErrorLevel level) throws Moml3Exception {
		Stack<IMoml3Tag> tagObjStack = new Stack<IMoml3Tag>();
		Stack<String> tagNameStack = new Stack<String>();

		ErrorHandler handler = new ErrorHandler(src, level);

		Page page = new Page();

		Div root = new Div("doc");
		// root.setAlign(Div.ALIGN_L); // default is inherit, need to set to
		// left
		// root.setFlow(Div.FLOW_LTR); // default is inherit, need to set to
		// left

		// don't set id here, because we don't want the users to be able to
		// 'accidentally' remove _root_ or perform
		// other destructive operations on it
		// root.setIdRestricted("_root_");

		root.setWidth("100%");
		tagObjStack.push(root);

		BaseToken t = nextToken(handler, false);

		// boolean inScript = false;
		Moscript currentScript = null;
		BaseToken last = null;
		while (t != null) {
			if (t instanceof TagToken) {
				TagToken tt = (TagToken) t;
				TagToken.TagType type = tt.getTagType();
				if (type == TagToken.TagType.OpenTag
						|| type == TagToken.TagType.SelfClosedOpenTag) {
					boolean isClosed = type == TagToken.TagType.SelfClosedOpenTag;
					IMoml3Tag tag = toTag(tt, page, handler);
					boolean tolerantPartial = TagUtil.tolerantPartial(tt
							.getName());
					if (tag != null) {
						if (tag instanceof Moscript) {
							if (isClosed) {
								page.addScript((Moscript) tag);
							} else {
								currentScript = (Moscript) tag;
								currentScript.setStart(tt.getEnd() + 1);
							}
						} else if (tag instanceof Page) {
							page.copyPageAttrib((Page) tag);
						} else {
							tagObjStack.peek().addChild(tag, t.getStart(),
									handler);
						}
						if (!tolerantPartial && !isClosed) {
							tagObjStack.push(tag);
						}
					}
					if (!tolerantPartial && !isClosed) {
						tagNameStack.push(tt.getName().toLowerCase());
					}
				} else if (type == TagToken.TagType.CloseTag) {
					boolean tolerantPartial = TagUtil.tolerantPartial(tt
							.getName());
					if (tolerantPartial) {
						// just ignore
					} else if (tagNameStack.size() > 0
							&& tagNameStack.peek().equals(
									tt.getName().toLowerCase())) {
						tagNameStack.pop();
						if (tagObjStack.size() > 0
								&& tagObjStack.peek().getTagName()
										.equals(tt.getName())) {
							IMoml3Tag tag = tagObjStack.pop();
							if (tag == currentScript) {
								currentScript.setEnd(tt.getStart());
								currentScript.setText(src.substring(
										currentScript.getStart(),
										currentScript.getEnd()), currentScript
										.getStart(), handler);
								page.addScript(currentScript);
								currentScript = null;
							}
							tag.closeTag();
						} else {
							handler.raise(ErrorLevel.Warning,
									"unsupported close tag: " + tt.getName(),
									t.getStart());
						}
					} else {
						handler.raise(ErrorLevel.FatalError,
								"unexpeced close tag: " + tt.getName(),
								t.getStart());
					}
				} else if (type == TagToken.TagType.DocTypeTag) { // DocType tag
					if (t.getBody().toLowerCase().contains("moml3")) {
						// ok
					} else {
						handler.raise(ErrorLevel.Warning,
								"doctype not marked as moml3", t.getStart());
					}
				} else {
					// ignore
				}
			} else if (t instanceof ContentToken) { // content
				if (t.hasBody()) {
					tagObjStack.peek().setText(t.getBody(), t.getStart(),
							handler);
				}
			} else {
				// ignore
			}
			last = t;
			t = nextToken(handler, currentScript != null);
		}
		if (tagObjStack.size() > 1) {
			handler.raise(ErrorLevel.FatalError, "close tag expected: "
					+ tagObjStack.peek().getTagName(), last.getEnd());
		}

		// note that root is genretaed by default
		// if root has no children, the doc is empty
		page.setRoot(root);

		return new ParseResult(page, handler.getErrors());
	}

	private BaseToken nextToken(ErrorHandler handler, boolean inScript)
			throws Moml3Exception {
		try {
			return convert(tokenizer.nextNode(), handler, inScript);
		} catch (ParserException e) {
			handler.raise(ErrorLevel.FatalError, e.getMessage(), 0);
		}
		return null;
	}

	public static IMoml3Tag toTag(TagToken token, Page page,
			ErrorHandler handler) throws Moml3Exception {
		TagToken.TagType type = token.getTagType();
		String name = token.getName().toLowerCase();

		if (type == TagType.OpenTag || type == TagType.SelfClosedOpenTag) {
			IMoml3Tag tag = null;
			if (Br.tag.equals(name)) {
				tag = new Br(name);
			} else if (Button.tag.equals(name)) {
				tag = new Button(name);
			} else if (Checkbox.tag.equals(name)) {
				tag = new Checkbox(name);
			} else if (Div.tag.equals(name)) {
				tag = new Div(name);
			} else if (Hr.tag.equals(name)) {
				tag = new Hr(name);
			} else if (Img.tag.equals(name)) {
				tag = new Img(name);
			} else if (Input.tag.equals(name)) {
				tag = new Input(name);
			} else if (Select.tag.equals(name)) {
				tag = new Select(name);
			} else if (Option.tag.equals(name)) {
				tag = new Option(name);
			} else if (Span.tag.equals(name)) {
				tag = new Span(name);
			} else if (Anchor.tag.equals(name)) {
				tag = new Anchor();
			} else if (Div.altTags.contains(name)) {
				tag = new Div(name);
			} else if (Span.altTags.contains(name)) {
				tag = new Span(name);
			} else if (Page.tag.equals(name)) {
				tag = new Page();
			} else if (Moscript.tag.equals(name)) {
				tag = new Moscript();
			} else if (Title.tag.equals(name)) {
				tag = new Title(page);
			} else if (Spring.tag.equals(name)) {
				tag = new Spring(name);
			} else {
				handler.raise(ErrorLevel.Warning, "unsupported tag " + name,
						token.getStart());
			}
			if (tag != null) {
				tag.setPosition(token.getStart());
				tag.fromAttributeMap(token.getAttributes(), token.getStart(),
						handler);
			}
			return tag;
		}
		return null;
	}

	public static BaseToken convert(Node t, ErrorHandler handler,
			boolean inScript) throws Moml3Exception {
		BaseToken bt = DummyToken.getInstance();

		if (t == null) {
			return null;
		} else if (t instanceof TextNode) {
			if (inScript) {
				bt = new DummyToken();
			} else {
				bt = new ContentToken();
			}
		} else if (t instanceof RemarkNode) {
			bt = new DummyToken();
			// if (inScript) {
			// // just ignore too
			// } else {
			// // just ignore
			// }
		} else {
			TagToken tag = new TagToken();
			if (inScript) {
				tag.setBody(t.getText().trim(), false, handler);
				if (tag.getTagType() == TagToken.TagType.CloseTag
						&& Moscript.tag.equals(tag.getName())) {
					bt = tag;
				} else { // otherwise treat it as a context tag
					// bt = new ContentToken();
					// bt.setBody(t.getText(), false, handler);
					bt = new DummyToken();
				}
			} else {
				bt = tag;
			}
		}
		if (bt != null) {
			bt.setStart(t.getStartPosition());
			bt.setEnd(t.getEndPosition());
			bt.setBody(t.getText().trim(), false, handler);
		}
		return bt;
	}
}
