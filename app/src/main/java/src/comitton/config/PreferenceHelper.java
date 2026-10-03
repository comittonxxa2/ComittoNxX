package src.comitton.config;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

// Preferenceのタイトル表示制限を解除するユーティリティクラス
public class PreferenceHelper {

	// 指定したルートView配下にあるPreferenceタイトルの1行制限を解除しスクロール時にも折り返しを維持するように設定
	public static void enableMultilineTitles(final View rootView) {
		if (rootView == null) return;

		rootView.post(new Runnable() {
			@Override
			public void run() {
				// 画面全体からandroid.R.id.titleを持っているすべてのTextViewを再帰的に探して解除
				makeAllTitlesMultiline(rootView);
				// ListView のスクロール監視を設定して再利用時の表示戻りを防止
				View listView = rootView.findViewById(android.R.id.list);
				if (listView instanceof ListView) {
					ListView list = (ListView) listView;
					list.setOnScrollListener(new AbsListView.OnScrollListener() {
						@Override
						public void onScrollStateChanged(AbsListView view, int scrollState) {
							// スクロール状態が変わったタイミングで解除処理を実行
							makeAllTitlesMultiline(view);
						}
						@Override
						public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
							// スクロール中も常に表示項目に対して解除処理を実行
							makeAllTitlesMultiline(view);
						}
					});
				}
			}
		});
	}
	// 画面内のすべてのViewを探索してandroid.R.id.titleの制限を解除するメソッド
	private static void makeAllTitlesMultiline(View view) {
		if (view == null) return;
		// IDがandroid.R.id.titleかつTextViewであれば設定変更
		if (view.getId() == android.R.id.title && view instanceof TextView) {
			TextView tv = (TextView) view;
			// 1行制限を解除
			tv.setSingleLine(false);
			// 最大10行まで許可
			tv.setMaxLines(10);
			// 省略(...)を解除
			tv.setEllipsize(null);
		}
		// 親ビュー(ViewGroup)の場合は子要素をすべて検索
		else if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				makeAllTitlesMultiline(group.getChildAt(i));
			}
		}
	}
}
