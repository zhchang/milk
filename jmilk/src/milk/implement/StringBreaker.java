package milk.implement;

import java.util.Vector;

import milk.ui2.MilkFont;

/**
 * 
 * @author livecn
 */
public class StringBreaker {
	private static final StringBreaker BREAKER = new StringBreaker();

	private int total = 0;
	private int progress = 0;
	private boolean running = true;

	/**
	 * Breaks the given string into multiple lines without breaking words. [NOT]
	 * This method will trim leading spaces of each generated line. [NOT] This
	 * method will reduce any group of adjacent spaces into one space.
	 */
	public synchronized void contentToLines(String content, Vector lines,
			MilkFont font, int firstLineWidth, int clipWidth, int maxLines,
			int startIdx, int endIdx)// , boolean smileys)
	{
		running = true;
		total = (endIdx > startIdx && startIdx >= 0) ? endIdx - startIdx
				: content.length();

		if (firstLineWidth <= 0)
			firstLineWidth = clipWidth;

		int curWidth = 0;
		int lastWordBreakPos = -1;
		int lastWordWidth = 0;

		if (startIdx < 0)
			startIdx = 0;
		if (endIdx < 0)
			endIdx = content.length();

		int _start = startIdx;
		int _end = startIdx;
		// int _firstLineMaxWidth = clipWidth - startX;
		// int wordCount = 0;
		// int loopcount = 0;
		for (_end = startIdx; _end < endIdx; _end++/* ,loopcount++ */) {
			if (!running)
				return;
			progress = _end;

			// Stop if already enough.
			if (maxLines > 0 && lines.size() >= maxLines)
				break;

			// //Ignore spaces at line starting.
			// if(curWidth == 0 && content.charAt(_start) == ' ')
			// {
			// _start++;
			// continue;
			// }
			// First line needs to start from startX.
			int maxWidth = (lines.isEmpty() ? firstLineWidth : clipWidth);
			// int maxWidth = (loopcount == 0 ? clipWidth - startX : clipWidth);

			char curChar = content.charAt(_end);

			// If sees line break, then break it.
			if (curChar == '\r' || curChar == '\n') {
				// Add.
				lines.addElement(new TextLine(_start, _end, curWidth));
				// Reset.
				curWidth = 0;
				lastWordBreakPos = -1;
				lastWordWidth = 0;
				_start = _end + 1;
				// Skip \n after \r.
				if (curChar == '\r' && _start < endIdx
						&& content.charAt(_start) == '\n') {
					_start++;
					_end++;
				}
				continue;
			}

			int widthForNewSubLine = curWidth;

			// !!!If is Arabic or Hebrew text, MUST calculate the width of the
			// whole word!!!
			// Because in these two languages the width of the word are
			// different from
			// the sum of the width of the individual characters in the same
			// word.
			if (isArabicOrHebrew(curChar)) {
				int _wordStart = _end;
				char _curChar = curChar;
				while (_end < endIdx) {
					_curChar = content.charAt(_end);
					if (!isArabicOrHebrew(_curChar)) {
						// System.out.println("_end=" + _end + " _curChar=" +
						// (int)_curChar);
						// Push back this non-Arabic character.
						// Note we are in a FOR-LOOP, so need to minus 2.
						_end--;
						break;
					}
					curChar = _curChar;
					_end++;
				}
				if (_end >= endIdx) {
					// break;
					_end = endIdx - 1;
				}
				// wordCount++;
				int wordWidth = font.substringWidth(content, _wordStart, _end
						- _wordStart + 1);
				// Do the above again. This time breaks when going out of
				// screen.
				// Note we don't do this in the above procedure, in order to
				// avoid unnecessarily computing substringWidth for each Arabic
				// text.
				if (wordWidth >= maxWidth) {
					_end = _wordStart;
					wordWidth = 0;
					while (_end < endIdx && wordWidth <= maxWidth) {
						_curChar = content.charAt(_end);
						if (!isArabicOrHebrew(_curChar)) {
							// System.out.println("_end=" + _end + " _curChar="
							// + (int)_curChar);
							// Push back this non-Arabic character.
							// Note we are in a FOR-LOOP, so need to minus 2.
							_end--;
							break;
						}
						wordWidth = font.substringWidth(content, _wordStart,
								_end - _wordStart + 1);
						curChar = _curChar;
						_end++;
					}
					if (_end >= endIdx) {
						// break;
						_end = endIdx - 1;
					}
				}
				curWidth += wordWidth;
				// System.out.println("_start=" + _start + " _wordStart=" +
				// _wordStart + " _end=" + _end + " curWidth=" + curWidth +
				// " maxWidth=" + maxWidth + " wordWidth=" + wordWidth);
			}
			// Content to be added.
			else {
				// Add this character.
				// Remember last breakable character.
				// if(!isUnbreakable(curChar))
				if (isBreakable(curChar)) {
					lastWordBreakPos = _end;
					lastWordWidth = widthForNewSubLine;
				}
				// Increase width.
				// if(smileys && Smiley.getSmiley(content, _end) >= 0)
				// {
				// curWidth += Smiley.SMILEY_SIZE;
				// }
				// else
				{
					curWidth += font.charWidth(curChar);
				}
			}
			// If exceeds screen width, break the line and try to keep words
			// unbroken.
			if (curWidth > maxWidth) {
				// Special case: Width even not enough for ONE character!
				if (_end - _start == 0) {
					// For the first line, do not add it. Add the place_holder
					// instead.
					if (lines.isEmpty()) {
						lines.addElement(new TextLine(0, 0, 0));
						continue;
					}
					// For the rest of the lines, add one character anyway.
					else {
						_end++;
					}
				}

				// !!!Disabled because now EndIdx is exclusive!!!
				// Delete this character first.
				// This ensures that if this is a Chinese character, it will not
				// go out of screen.
				// _end--;

				// If is now at middle of a string and in this line there was a
				// space,
				// then break it.
				// if(isUnbreakable(curChar))
				// System.out.println("curWidth=" + curWidth + " charWidth=" +
				// font.charWidth(curChar) + " maxWidth=" + maxWidth +
				// " curChar=" + (int)curChar + " isBreakable=" +
				// isBreakable(curChar) + " lastWordBreakPos=" +
				// lastWordBreakPos + (lastWordBreakPos < 0 ? "" :
				// " lastWordBreak=" +
				// (int)(content.charAt(lastWordBreakPos))));
				if (!isBreakable(curChar)) {
					// Can break from last space, do it.
					if (lastWordBreakPos >= 0) {
						// !!!Note we skip (i.e, include it in this round) the
						// lastWordBreakPos
						// to avoid re-break from it next round so to avoid
						// infinite loop!!!
						_end = lastWordBreakPos + 1;
						widthForNewSubLine = lastWordWidth;
						// wordCount--;
					}
					// There is no last space, then skip for first line, and add
					// anyway for the rest.
					else if (lines.isEmpty()) {
						lines.addElement(new TextLine(0, 0, 0));
						continue;
					}
				}
				// We must start from this current character again next time. So
				// we
				// remember this here.
				// Remember we are in a FOR-LOOP where _end will be incremented
				// by one
				// next time!
				int newEnd = _end - 1;

				// Add.
				lines.addElement(new TextLine(_start, _end, widthForNewSubLine));
				curWidth = 0;
				lastWordBreakPos = -1;
				lastWordWidth = 0;
				_start = _end;
				// To reset the counter.
				_end = newEnd;
			}
		}
		if (_end - _start > 0)
			lines.addElement(new TextLine(_start, _end, curWidth));
	}

	public static synchronized StringBreaker getBreaker() {
		return BREAKER;
	}

	public int getProgress() {
		// if(total > 0)
		// return progress * ProgressReader.MAX_PROGRESS_VALUE / total;
		// return 0;
		if (total > 0)
			return progress;
		return 0;
	}

	public void stop() {
		running = false;
		// try{Thread.currentThread().join();}catch(Exception e){}
	}

	private static final String breakable = " -";

	private static boolean isBreakable(char c) {
		// !!!Only spaces, hyphens, Chinese, and Japanese are breakable.
		return breakable.indexOf(c) >= 0 || (c >= '\u2e80' && c <= '\u9fff')
				|| (c >= '\uf900' && c <= '\ufaff');
	}

	private static boolean isArabicOrHebrew(char c) {
		return (c >= '\u0590' && c <= '\u06ff');
	}

	// private static boolean isUnbreakable(char c)
	// {
	// final String unbreakable = "\"#$%'():[]{}";
	// return (StringUtils.isChar(c) || unbreakable.indexOf(c) >= 0);
	// }
}
