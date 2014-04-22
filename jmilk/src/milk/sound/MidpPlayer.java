package milk.sound;

import java.io.InputStream;
import java.util.Hashtable;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

import milk.implement.Adaptor;

public class MidpPlayer {
	// j2me
	// MidpPlayer.loadMidpPlayer("splash.mid");

	// MidpPlayer.playMidpSound("splash.mid", 10);
	// bb
	// MidpPlayer.loadMidpPlayer("/test.amr");

	// MidpPlayer.playMidpSound("/test.amr", 1);

	private Player currentPlayer;
	private String currentMusicName;
	private Hashtable soundCache;
	private static MidpPlayer midpPlayer;

	private MidpPlayer() {
		// String types[] = Manager.getSupportedContentTypes(null);
		// for (int i = 0; i < types.length; i++) {
		// System.out.println("-------------getSupportedContentTypes:"
		// + types[i]);
		// }
	}

	public static void loadMidpPlayer(String fileName) {
		if (midpPlayer == null)
			midpPlayer = new MidpPlayer();
		midpPlayer.registerPlayer(fileName);
	}

	public static void unloadMidpPlayer(String fileName) {
		if (midpPlayer != null) {
			// midpPlayer.stopPlayer();
			midpPlayer.unregisterPlayer(fileName);
		}
	}

	public static void playMidpSound(String fileName, int loop) {
		if (midpPlayer == null)
			midpPlayer = new MidpPlayer();
		midpPlayer.playSound(fileName, loop);
		// System.out.println("/fileName:"+fileName);
	}

	public static void stopMidpSound() {
		if (midpPlayer != null) {
			midpPlayer.stopPlayer();
		}
	}

	private void registerPlayer(String fileName) {
		fileName = getMediaName(fileName);
		if (soundCache == null)
			soundCache = new Hashtable();
		if (!soundCache.containsKey(fileName)) {
			Player player = createNewPlayer(fileName);
			if (player != null)
				soundCache.put(fileName, player);
			else {
				// System.out.println("Player p =null");
			}

		}
	}

	private void unregisterPlayer(String fileName) {
		fileName = getMediaName(fileName);
		if (soundCache != null && soundCache.containsKey(fileName)) {
			Player player = (Player) soundCache.remove(fileName);
			player.deallocate();
			player = null;
		}
	}

	private void playSound(String fileName, int loop) {
		fileName = getMediaName(fileName);
		if (fileName.equals(currentMusicName)) {// �ж��Ƿ������һ�β���������ͬ
			play(loop, fileName);
		} else {
			playSound(fileName, loop, true);
		}
	}

	private boolean isPlaying() {
		return currentPlayer != null
				&& currentPlayer.getState() == Player.STARTED;
	}

	private void playSound(String fileName, int loop, boolean interrupt) {
		if (isPlaying()) {// �ж��Ƿ����ڲ���������Ч
			return;
		}
		if (soundCache != null) {
			currentPlayer = (Player) soundCache.get(fileName);
			// System.out.println("get player from table fileName=" + fileName);
			if (currentPlayer == null)
				currentPlayer = createNewPlayer(fileName);
		} else {
			stopPlayer();
			currentPlayer = createNewPlayer(fileName);
		}
		play(loop, fileName);
	}

	private Player createNewPlayer(String fileName) {
		try {// ��ʼ��������,�����µ���Ч
				// System.out.println("create new Player:" + fileName);
			InputStream is = getClass().getResourceAsStream(fileName);
			if (is == null) {
				// System.out.println("createNewPlayer() InputStream = null!!:"
				// + fileName);
				return null;
			}
			Player p = Manager.createPlayer(is, getMediaType(fileName));
			p.realize();
			p.prefetch();
			return p;
		} catch (Exception e) {
			Adaptor.exception(e);
		}
		return null;
	}

	private void play(int loop, String fileName) {
		if (currentPlayer != null) {
			try {
				if (loop >= 1 || loop == -1)
					currentPlayer.setLoopCount(loop);
				currentPlayer.start();
				currentMusicName = fileName;
				return;
			} catch (Exception e) {
			}
		}
	}

	private void stopPlayer() {
		if (currentPlayer != null) {
			try {
				currentPlayer.stop();
				currentPlayer.close();
				// currentPlayer = null;
			} catch (MediaException e) {
				e.printStackTrace();
			}
		}
		currentMusicName = null;
	}

	private String getMediaName(String fileName) {
		String mediaName = fileName;
		if (!mediaName.startsWith("/")) {
			mediaName = "/" + mediaName;
		}
		if (mediaName.indexOf(".") == -1) {// Ĭ��Ϊmid��ʽ
			mediaName = mediaName + ".mid";
		}
		return mediaName;
	}

	private String getMediaType(String fileName) {
		String type;
		if (fileName.endsWith(".wav")) {
			type = "audio/x-wav";
		} else if (fileName.endsWith(".amr")) {
			type = "audio/amr";
		} else {
			type = "audio/midi";
		}
		return type;
	}

}
