package milk.implement;

public interface SetEventListener {
    
	int LANGUAGE_EN=1;
	int LANGUAGE_CN=2;
	
	int IMAGE_QUALITY_HIGH=3;
	int IMAGE_QUALITY_NORMAL=4;
	
	void soundStateChanged(boolean isSoundOn);
	
	void soundVolumeChanged(int volume);
	
	void languageChanged(int language);
	
	void imageQualityChanged(int quality);
	
}
