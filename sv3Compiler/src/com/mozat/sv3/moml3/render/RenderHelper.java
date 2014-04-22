package com.mozat.sv3.moml3.render;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.mozat.sv3.moml3.parser.Moml3Parser;
import com.mozat.sv3.smartview3.elements.Sv3Div;
import com.mozat.sv3.smartview3.elements.Sv3Page;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;

public class RenderHelper {
	public static BufferedImage loadImage(String path) throws IOException {
		return ImageIO.read(new File(path));
	}

	public static void writeAsPng(RenderedImage image, String path)
			throws IOException {
		// Write generated image to a file
		// Save as PNG
		File file = new File(path);
		ImageIO.write(image, "png", file);
	}

	public static void render(Sv3Page page, FontUtil fu, Graphics g, int w,
			int h) {
		// g.setColor(Color.WHITE);
		// g.fillRect(0, 0, w, h);
		Rect bounding = new Rect(0, -page.getScrollY(), w, 0x7fff);
		Rect viewPort = new Rect(0, 0, w, h);
		IRenderContext ctx = new RenderContext(g, bounding, viewPort, fu);

		page.render(ctx);
	}

	public static void render(Sv3Div root, FontUtil fu, Graphics g, int w, int h) {
		// g.setColor(Color.WHITE);
		// g.fillRect(0, 0, w, h);
		IRenderContext ctx = new RenderContext(g, new Rect(0, 0, w, 0x7fff),
				new Rect(0, 0, w, h), fu);
		root.render(ctx);
	}

	public static void layout(Sv3Div root, FontUtil fu, int w) {
		layout(root, fu, w, -1);
	}

	// public static void layout(Sv3Page page, FontUtil fu, int w, int h) {
	// LayoutContext lc = new LayoutContext(w, 0x7fffff);
	// lc.definedWidth = (short) w;
	// lc.definedHeight = (short) h;
	// page.getRoot().layout(lc, fu);
	// }

	public static void layout(Sv3Div root, FontUtil fu, int w, int h) {
		LayoutContext lc = new LayoutContext(w, 0x7fffff);
		root.layout(lc, fu);
	}

	// Returns a generated image.
	public static BufferedImage renderToImage(Sv3Div root, int width, int height) {
		// Create a buffered image in which to draw
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		Graphics g = bufferedImage.createGraphics();
		FontUtil fu = new FontUtil(g);
		layout(root, fu, width, height);
		render(root, fu, g, width, height);
		return bufferedImage;
	}

	static void generateImage(final String src, final String out)
			throws IOException {
		Moml3Parser p = new Moml3Parser(src);
		Sv3Div root = p.parseSafely().page.getRoot();
		RenderedImage image = renderToImage(root, 320, 480);
		writeAsPng(image, out);
	}

	public static boolean imagePixelEqual(BufferedImage img1, BufferedImage img2)
			throws InterruptedException {
		int w1 = img1.getWidth();
		int h1 = img1.getHeight();
		int w2 = img2.getWidth();
		int h2 = img2.getHeight();
		if (w1 != w2 || h1 != h2) {
			return false;
		} else {
			int[] pixels1 = new int[w1 * h1];
			PixelGrabber pg1 = new PixelGrabber(img1, 0, 0, w1, h1, pixels1, 0,
					w1);
			pg1.grabPixels();
			int[] pixels2 = new int[w2 * h2];
			PixelGrabber pg2 = new PixelGrabber(img2, 0, 0, w2, h2, pixels2, 0,
					w2);
			pg2.grabPixels();
			for (int i = 0; i < w1 * h1; ++i) {
				if (pixels1[i] != pixels2[i]) {
					return false;
				}
			}
			return true;
		}
	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		// generateImage(, "out.png");
		Moml3Parser p = new Moml3Parser(
				"<div bgColor='red' width='200' height='300' />");
		Sv3Div root = p.parseSafely().page.getRoot();
		BufferedImage image = renderToImage(root, 320, 480);
		BufferedImage image2 = loadImage("out.png");
		System.out.println(imagePixelEqual(image, image2));
	}
}
