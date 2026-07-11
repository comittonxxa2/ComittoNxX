package src.comitton.common;

import static src.comitton.dialog.ToolbarEditDialog.COMMAND_ID;

public class DialogSharedData{

	public boolean[] mPageSelectToolbar = new boolean[COMMAND_ID.length];
	public int[] mPageSelectToolbarIndex = new int[COMMAND_ID.length];
	public int mToolbarSize;

	public DialogSharedData() {}
}
