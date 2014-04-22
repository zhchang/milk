package com.mozat.sv3.smartview3.layout;

import java.util.Hashtable;
import java.util.Vector;

import com.mozat.sv3.smartview3.utils.IFontUtil;

public class TextBreaker {

	static final byte TOKEN_LANG_INVALID = 0, TOKEN_LANG_ENGLISH = 1,
			TOKEN_LANG_EASTASIA = 2, TOKEN_LANG_ARABIC = 3,
			TOKEN_LANG_OTHER = 4;
	static final int MAX_TOKEN_LENGTH = 1024;
	static final int MIN_REASONABLE_BOUND_WIDTH = 40;
	static char EastAsiaBreakable[] = { '\u3001', '\u3002', '\uff0c', '\uff1a',
			'\uff1b', '\u300f', '\u201d', '\u2019', '\u300b', '\u300d', };
	static char[] kMOBreakableEnglishChars = { '-', '\0' };
	String content_;
	Vector segments_;
	Object font_;
	int firstLineWidth_;
	int clipWidth_;
	int maxLines_;
	int contentLength_;
	int currentIndex_;
	int lineIndex;
	IFontUtil fu;
	Hashtable emoticonTable;
	Token nextToken;

	static boolean isSpaceOrTab(char c) {
		return c == ' ' || c == '\t';
	}

	static boolean isNewLine(char c) {
		return c == '\r' || c == '\n';
	}

	static boolean MOIsCharEnglishAndBreakableAfter(char c) {
		for (int i = 0; i < kMOBreakableEnglishChars.length; ++i) {
			if (c == kMOBreakableEnglishChars[i]) {
				return true;
			}
		}
		return false;
	}

	static boolean MOIsCharEastAsiaAndBreakableAfter(char c, char nextChar) {
		boolean notBreakable = false;
		for (int i = 0; i < EastAsiaBreakable.length; ++i) {
			if (nextChar == EastAsiaBreakable[i]) {
				notBreakable = true;
				break;
			}
		}
		return !notBreakable;
	}

	static boolean MOIsCharEastAsian(char c) {
		return (c >= '\u2e80' && c <= '\u9fff')
				|| (c >= '\uf900' && c <= '\ufaff')
				|| (c >= '\uff00' && c <= '\ufffe');
	}

	static boolean MOIsCharOtherLanguageBreakableAfter(char c) {
		return MOIsCharEastAsian(c) && MOIsCharEastAsiaAndBreakableAfter(c, c);
	}

	static boolean MOIsCharBreakableAfter(char c) {
		// !!!Only spaces, hyphens, Chinese, and Japanese are breakable.
		return MOIsCharEnglishAndBreakableAfter(c)
				|| MOIsCharOtherLanguageBreakableAfter(c);
	}

	static boolean MOIsCharArabicOrHebrew(char c) {
		return (c >= '\u0590' && c <= '\u06ff');
	}

	/*
	 * ! what is considered a TOKEN? A token should be one of the following: a
	 * smiley strings that matches any of the smiley strings a text token, which
	 * is a sequence of characters that fullfil the following readable
	 * characters seperated by white space (OR EOF) on both side character of
	 * the same language
	 */
	private Token nextToken() {
		if (nextToken != null) {
			Token t = nextToken;
			nextToken = null;
			return t;
		}
		Token token = new Token();
		int cursor = currentIndex_;

		byte tokenLanguage = TOKEN_LANG_INVALID;

		// boolean isFirstChar;
		char nextChar = 0;
		while (cursor < contentLength_
				&& cursor - currentIndex_ < MAX_TOKEN_LENGTH) // play safe, we
																// don't allow a
																// token to grow
																// to a
																// indefinate
																// size
		{
			// isFirstChar = (cursor == currentIndex_);

			char curChar;
			if (nextChar != 0) {
				curChar = nextChar;
				nextChar = 0;
			} else {
				curChar = content_.charAt(cursor);
			}
			++cursor; // once read, increment cursor

			if (isNewLine(curChar)) {
				token.set(currentIndex_, cursor - currentIndex_, true);
				break;
			} else if (isSpaceOrTab(curChar)) {
				token.set(currentIndex_, cursor - currentIndex_, false);
				break;
			} else {
				boolean isEmoticon = false;
				Character charObj = new Character(curChar);
				if (emoticonTable != null && emoticonTable.containsKey(charObj)) {
					Vector emoticons = (Vector) emoticonTable.get(charObj);
					int emoCount = emoticons.size();
					for (int i = 0; i < emoCount; ++i) {
						IEmoticon emo = (IEmoticon) emoticons.elementAt(i);
						int tailLen = emo.matchTail(content_, cursor);
						if (tailLen >= 0) {
							isEmoticon = true;
							int emoCursor = cursor - 1;
							cursor += tailLen;
							if (emoCursor - currentIndex_ == 0) {
								token.set(currentIndex_,
										cursor - currentIndex_, false, emo);
							} else {
								token.set(currentIndex_, emoCursor
										- currentIndex_, false);
								nextToken = new Token();
								nextToken.set(emoCursor, cursor - emoCursor,
										false, emo);
							}
							break;
						}
					}
				}
				if (isEmoticon) {
					break;
				} else {
					byte charLanguage = TOKEN_LANG_INVALID;
					boolean canBreakAfterChar = false;

					if (MOIsCharArabicOrHebrew(curChar)) {
						charLanguage = TOKEN_LANG_ARABIC;
						canBreakAfterChar = false;
					} else if (MOIsCharEastAsian(curChar)) {
						charLanguage = TOKEN_LANG_EASTASIA;
						// breakability depends on the next char
						if (cursor < contentLength_) {
							nextChar = content_.charAt(cursor);
						}
						canBreakAfterChar = MOIsCharEastAsiaAndBreakableAfter(
								curChar, nextChar);
					} else {
						// if not smiley then go on
						charLanguage = TOKEN_LANG_ENGLISH;
						canBreakAfterChar = MOIsCharEnglishAndBreakableAfter(curChar);
					}

					if (tokenLanguage == TOKEN_LANG_INVALID) {
						tokenLanguage = charLanguage;
					}

					if (tokenLanguage != charLanguage) {
						// the current character is NOT included as part of the
						// token
						--cursor; // we need to put back the character and leave
									// it for the next token
						token.set(currentIndex_, cursor - currentIndex_, false);
						break;
					}

					if (canBreakAfterChar) {
						// the breakable character is included as the last char
						// of the token
						token.set(currentIndex_, cursor - currentIndex_, false);
						break;
					}
				}
			}
		}

		if (cursor > currentIndex_ && token.length == 0) // last segment before
															// EOF
		{
			token.set(currentIndex_, cursor - currentIndex_, false);
		}

		currentIndex_ = cursor;
		return token;
	}

	private void addSegment(TextSegment piece) {
		if (piece instanceof EmoticonSegment) {
			segments_.addElement(piece);
		} else {
			int size = segments_.size();
			if (size > 0) {
				TextSegment last = (TextSegment) segments_.elementAt(size - 1);
				if (last instanceof EmoticonSegment || last.line != piece.line) {
					segments_.addElement(piece);
				} else {
					// merge them
					last.mergeSegment(piece);
				}
			} else {
				segments_.addElement(piece);
			}
		}
	}

	Token partOfToken(Token token, int tokenWidth, int fitWidth,
			int[] actualWidth) {
		actualWidth[0] = tokenWidth;
		if (tokenWidth < fitWidth) {
			return new Token(token);
		} else {
			Token partialToken = new Token();
			int partialLength = token.length * fitWidth / tokenWidth / 2; // start
																			// from
																			// a
																			// portion
																			// that
																			// is
																			// very
																			// unlikely
																			// to
																			// be
																			// longer
																			// than
																			// a
																			// line

			int widthOfPartialToken = fu.getSubStringWidth(font_, content_,
					token.start, partialLength);
			actualWidth[0] = widthOfPartialToken;

			while (partialLength < token.length
					&& widthOfPartialToken < fitWidth) // keep shrinking until
														// it fits
			{
				++partialLength;
				actualWidth[0] = widthOfPartialToken;
				widthOfPartialToken = fu.getSubStringWidth(font_, content_,
						token.start, partialLength);
			}

			partialToken.set(token.start, partialLength - 1, false);
			return partialToken;
		}
	}

	public void breakIt() {
		if (content_ == null) {
			return;
		}

		lineIndex = 0;
		int currentLineWidth = 0;

		Token token = this.nextToken();
		while (token.length > 0) {
			// NSLog(@"(%d, %d, %x, %d)", token.start, token.length,
			// token.flags, token.emoticon != nil);
			int currentLineMaxWidth = lineIndex == 0 ? firstLineWidth_
					: clipWidth_;

			int tokenWidth = 0;
			if (token.emoticon == null) {
				tokenWidth = fu.getSubStringWidth(font_, content_, token.start,
						token.length);
			} else {
				tokenWidth = token.emoticon.getWidth()
						+ token.emoticon.getXSpacing() * 2;
			}

			if (tokenWidth > clipWidth_) // if the token doesn't fit the full
											// line, break it
			{
				if (clipWidth_ < MIN_REASONABLE_BOUND_WIDTH) // too small to be
																// reasonable
				{
					this.addSegment(token.toTextSegment(lineIndex, tokenWidth));

					++lineIndex;
					currentLineWidth = 0;
					if (lineIndex >= maxLines_) {
						break;
					}
					token = this.nextToken();
				} else {
					int widthLeft = currentLineMaxWidth - currentLineWidth;
					if (widthLeft > clipWidth_ / 2) // width left is more than
													// half of full line width
					{
						int[] actualWidth = new int[1];
						Token partialToken = this.partOfToken(token,
								tokenWidth, widthLeft, actualWidth);

						this.addSegment(new TextSegment(partialToken.start,
								partialToken.length, lineIndex, actualWidth[0]));

						token.start += partialToken.length;
						token.length -= partialToken.length;
						{
							++lineIndex;
							currentLineWidth = 0;
							if (lineIndex >= maxLines_) {
								break;
							}
						}
						if (token.length < 0) {
							token = this.nextToken();
						}
					} else {
						++lineIndex;
						currentLineWidth = 0;
						if (lineIndex >= maxLines_) {
							break;
						}
					}
				}
			} else {
				if (currentLineWidth + tokenWidth > currentLineMaxWidth) {
					++lineIndex;
					currentLineWidth = 0;

					if (lineIndex >= maxLines_) {
						break;
					}
				}

				this.addSegment(token.toTextSegment(lineIndex, tokenWidth));

				if (token.lineAfter) {
					++lineIndex;
					currentLineWidth = 0;
					if (lineIndex >= maxLines_) {
						break;
					}
				} else {
					currentLineWidth += tokenWidth;
				}

				token = this.nextToken();
			}
		}
	}

	public void breakText(String content, Vector pieces, Object font,
			IFontUtil fu, int firstLineWidth, short clipWidth, int maxLines,
			Hashtable emoticonTable) {

		this.fu = fu;
		// not retaining the temp variables
		content_ = content;
		segments_ = pieces;
		font_ = font;

		firstLineWidth_ = firstLineWidth;
		clipWidth_ = clipWidth;
		maxLines_ = maxLines > 0 ? maxLines : 0x7fff;
		contentLength_ = content.length();
		currentIndex_ = 0;
		this.emoticonTable = emoticonTable;

		this.breakIt();

		content_ = null;
		segments_ = null;
		font_ = null;
	}
}
