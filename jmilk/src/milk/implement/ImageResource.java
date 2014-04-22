package milk.implement;

class ImageResource {

	Object image;
	int refCount;
	long lastUsedTime = System.currentTimeMillis();

	public Object getImage() {
		return image;
	}

	public void setImage(Object image) {
		this.image = image;
	}

	public int getRefCount() {
		return refCount;
	}

	public void setRefCount(int refCount) {
		this.refCount = refCount;
	}

	public void clearMem() {
		image = null;
	}

}
