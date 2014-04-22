package milk.ui;


import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.VerticalFieldManager;

public class OneLineInputField extends VerticalFieldManager {

	private EditField editField;
	private boolean isLastLine=false;
	
	public OneLineInputField(long style) {
		super(HORIZONTAL_SCROLL | NO_VERTICAL_SCROLL);
		
		this.editField = new EditField(style) {
			
			protected boolean keyChar(char key, int status, int time) {
				boolean value = super.keyChar(key, status, time);
				relayout();
				updateScroll();
				return value;
			}

			public void paint(Graphics g) {
				super.paint(g);
			}

			protected boolean navigationMovement(int dx, int dy, int arg2,
					int arg3) {
				
				if (dy > 0 && isLastLine) {
					return true;
				}
		
				return super.navigationMovement(dx, dy, arg2, arg3);
			}

			protected void updateScroll() {
				int strLength = getFont().getAdvance(this.getText());
				int currentScroll = getManager().getHorizontalScroll();
				int strLengthToCursor = getFont().getAdvance(
						this.getText().substring(0, getCursorPosition()));
				int strLengthFromCursorToEnd = getFont().getAdvance(
						this.getText().substring(getCursorPosition(),
								this.getText().length()));
				int currentManagerWidth = getManager().getVisibleWidth();
				int neededOffset = 0;

				int cursorMargin = 30;

				if (strLengthToCursor >= cursorMargin
						&& currentManagerWidth > cursorMargin * 2) {
					// Cursor near the end of the string
					if (strLengthFromCursorToEnd < currentManagerWidth
							&& strLength > currentManagerWidth) {
						neededOffset = strLength - currentManagerWidth + 10; // Leave
					} else
					// Cursor close to right edge of the field
					if (currentScroll + currentManagerWidth - cursorMargin < strLengthToCursor) {
						neededOffset = strLengthToCursor + cursorMargin
								- currentManagerWidth;
					} else // Cursor close to left edge of the field
					if (currentScroll + cursorMargin > strLengthToCursor) {
						neededOffset = strLengthToCursor - cursorMargin;
					}
				}

				if (neededOffset > 0) {
					getManager().setHorizontalScroll(neededOffset);
				}

			}
		};

		add(this.editField);
	}

	public EditField getEditField() {
		return this.editField;
	}

	public void setEditable(boolean editable){
		editField.setEditable(editable);
	}

	public void setLastLine() {
		isLastLine=true;
	}
	
	public void setNotLastLine() {
		isLastLine=true;
	}

	public void sublayout(int width, int height) {
		super.sublayout(width, height);
		if (this.editField != null) {
			int textLength = this.editField.getFont().getAdvance(
					this.editField.getText());
			// Leave some room to see the cursor at the end and to prevent
			// scroll bugs
			setVirtualExtent(textLength + width / 2, height);
		}
	}

	public void relayout() {
		updateLayout();
	}

//	public String getText() {
//		return this.editField.getText();
//	}

	public void setText(String text) {
		this.editField.setText(text);
	}
	
	public void insert(String text){
		this.editField.insert(text);
	}


	protected void onUnfocus() {
		setCursorPosition(0);
		super.setHorizontalScroll(0);
		super.onUnfocus();
	}

	public boolean navigationMovement(int dx, int dy) {
		if (dy == 0) {
			if (dx > 0) {
				if (getCursorPosition() >= editField.getTextLength()||editField.getTextLength()==0)
					return true;
				 else {
					return false;
				}
			} else if (dx < 0) {
				if (getCursorPosition() == 0)
					return true;
				else {
					return false;
				}
			} else
				return false;
		} else {
			return true;
		}
	}

	public void setCursorPosition(int pos) {
		this.editField.setCursorPosition(pos);
	}

//	public int getInsertPositionOffset() {
//		return this.editField.getCursorPosition();
//	}

	public void setChangeListener(FieldChangeListener listener) {
		super.setChangeListener(listener);
		this.editField.setChangeListener(listener);
	}

	public int getCursorPosition() {
		return this.editField.getCursorPosition();
	}


//	public void doLayout(int width, int height) {
//		layout(width, height);
//	}
//
//	public void setFilter(TextFilter filter) {
//		this.editField.setFilter(filter);
//	}

}