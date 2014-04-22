package com.mozat.sv3.moml3.render;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ImageObserver;

import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;

/**
 * 
 * @author luyx
 */
public class RenderContext extends IRenderContext implements ImageObserver {
	Graphics g;

	public RenderContext(Graphics g, Rect bounding, Rect viewPort, IFontUtil fu) {
		super(bounding, viewPort, fu);
		this.g = g;
	}

	@Override
	public void setColor(int c) {
		g.setColor(new Color(c));
	}

	@Override
	public void drawLine(int i, int j, int k, int l) {
		g.drawLine(i, j, k, l);
	}

	@Override
	public void drawString(String title, int x, int y) {
		// g.drawString baseline, to convert to top-left corner we do y +=
		// ascent
		g.drawString(title, x, y + fu.getAscent(g.getFont()));
	}

	@Override
	public void drawSubstring(String title, int offset, int length, int x, int y) {
		String substr = title.substring(offset, offset + length);
		g.drawString(substr, x, y + fu.getAscent(g.getFont()));
	}

	@Override
	public int getClipWidth() {
		return (int) g.getClipBounds().getWidth();
	}

	@Override
	public int getClipHeight() {
		return (int) g.getClipBounds().getHeight();
	}

	@Override
	public int getClipX() {
		return (int) g.getClipBounds().getX();
	}

	@Override
	public int getClipY() {
		return (int) g.getClipBounds().getY();
	}

	@Override
	public Object getGraphics() {
		return g;
	}

	@Override
	public void clipRect(int x, int y, int width, int height) {
		g.clipRect(x, y, width, height);
	}

	@Override
	public void setClip(int x, int y, int width, int height) {
		g.setClip(x, y, width, height);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		g.fillRect(x, y, width, height);
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		g.drawRect(x, y, width, height);
	}

	private RenderContext subCtx;

	public synchronized IRenderContext getSubCtx(Rect padded, Rect viewport) {
		if (subCtx == null) {
			subCtx = new RenderContext(g, padded, viewport, fu);
		} else {
			subCtx.setGraphics(g);
			subCtx.getBounding().copy(padded);
			subCtx.getViewPort().copy(viewport);
		}
		return subCtx;
	}

	private void setGraphics(Graphics g) {
		this.g = g;
	}

	@Override
	public void setFont(Object font) {
		g.setFont((Font) font);
	}

	@Override
	public void drawImage(Object image, int x, int y) {
		g.drawImage((Image) image, x, y, this);
	}

	@Override
	public void drawImage(Object img, int x, int y, int w, int h) {
		g.drawImage((Image) img, x, y, w, h, this);
	}

	@Override
	public boolean imageUpdate(Image arg0, int arg1, int arg2, int arg3,
			int arg4, int arg5) {
		return false;
	}

}
