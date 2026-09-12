package src.comitton.config;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

public class ListViewScrollUtils {

	// アクティビティを再起動
	public static void restartActivityWithPosition(Activity activity, ListView listView) {
		Intent intent = activity.getIntent();
		if (listView != null) {
			int index = listView.getFirstVisiblePosition();
			View v = listView.getChildAt(0);
			int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
			intent.putExtra("scroll_index", index);
			intent.putExtra("scroll_top", top);
		}
		activity.finish();
		activity.startActivity(intent);
	}
	// ListViewの位置を元に戻す
	public static void restorePosition(Activity activity, final ListView listView) {
		if (listView == null) return;
		Intent intent = activity.getIntent();
		final int index = intent.getIntExtra("scroll_index", -1);
		final int top = intent.getIntExtra("scroll_top", 0);
		if (index != -1) {
			listView.post(new Runnable() {
				@Override
				public void run() {
					listView.setSelectionFromTop(index, top);
				}
			});
		}
	}
}
