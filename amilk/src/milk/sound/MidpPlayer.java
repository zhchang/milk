package milk.sound;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import milk.ui.UIHelper;
import android.app.Service;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

public final class MidpPlayer
{
	private Hashtable audioPlayers = new Hashtable();

	private final static String TAG=MidpPlayer.class.getName();
	
	private static MidpPlayer instance;

	private static AudioManager audioManager;

	private static int max;
	
	static
	{
		audioManager = (AudioManager) UIHelper.milk.getSystemService(Service.AUDIO_SERVICE);
		max = audioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
	}

	public void clear()
	{
		instance = null;
		if (audioPlayers != null)
		{
			Enumeration en = audioPlayers.keys();
			while (en.hasMoreElements())
			{
				Object key = en.nextElement();
				Object o = audioPlayers.get(key);
				if (o != null)
				{
					MediaPlayer mp = (MediaPlayer) o;
					mp.stop();
					mp.release();
					mp = null;
				}
			}
		}
		audioPlayers.clear();
	}

	private MidpPlayer()
	{
	}

	public static synchronized MidpPlayer getInstance()
	{
		if (instance == null)
		{
			instance = new MidpPlayer();
		}
		return instance;
	}

	public void playSound(int id, int loop)
	{
		Object o = audioPlayers.get(id);
		if (o != null)
		{
			MediaPlayer m = (MediaPlayer) o;
			try
			{
				if (!m.isPlaying())
				{
					if (loop == -1)
					{
						m.setLooping(true);
					}
					m.start();
				}
			} catch (IllegalStateException e)
			{
				e.printStackTrace();
			} 

		}
	}

	public void registerPlayer(int id, Object o)
	{
		audioPlayers.put(id, o);
	}

	public void unRegisterPlayer(int id)
	{
		if (audioPlayers.containsKey(id))
		{
			Object o = audioPlayers.get(id);
			if (o != null)
			{
				MediaPlayer mp = (MediaPlayer) o;
				if (mp.isPlaying())
				{
					mp.stop();
				}
			}
		}
		audioPlayers.remove(id);
	}

	public void stopSound(int id)
	{
		Object o = audioPlayers.get(id);
		if (o != null)
		{
			MediaPlayer m = (MediaPlayer) o;
			m.stop();
		}
	}

	public MediaPlayer getPlayerBybytes(byte[] bytes)
	{
		FileOutputStream fos=null;
		try
		{
			File tempMp3 = File.createTempFile("milk", "mp3", UIHelper.milk.getCacheDir());
			tempMp3.deleteOnExit();
			fos = new FileOutputStream(tempMp3);
			fos.write(bytes);
			fos.close();
			MediaPlayer mediaPlayer = new MediaPlayer();
			FileInputStream fis = new FileInputStream(tempMp3);
			mediaPlayer.setDataSource(fis.getFD());
			return mediaPlayer;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public void pauseAllMediaPlayer()
	{
		if (audioPlayers != null && audioPlayers.size() > 0)
		{
			Enumeration keys = audioPlayers.keys();
			while (keys.hasMoreElements())
			{
				Object key = (Object) keys.nextElement();
				if (key != null)
				{
					Object value = audioPlayers.get(key);
					if (value != null)
					{
						MediaPlayer mp = (MediaPlayer) value;
						mp.pause();
					}

				}
			}
		}
	}

	public void recoveryBgMediaPlayer()
	{
		if (audioPlayers != null && audioPlayers.size() > 0)
		{
			Enumeration keys = audioPlayers.keys();
			while (keys.hasMoreElements())
			{
				Object key = (Object) keys.nextElement();
				if (key != null)
				{
					Object value = audioPlayers.get(key);
					if (value != null)
					{
						MediaPlayer mp = (MediaPlayer) value;
						if (mp.isLooping())
						{
							try
							{
								mp.start();
							} catch (IllegalStateException e)
							{
								e.printStackTrace();
							}

						}
					}

				}
			}
		}
	}

	public void setUpperVolume()
	{
		audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
	}

	public void setLowerVolume()
	{
		audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
	}
	
	public int getCurrentVolume()
	{
		int currentVolume = audioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
		Log.i(TAG, "currentVolume "+currentVolume+" max "+max);
		return currentVolume;
	}

}
