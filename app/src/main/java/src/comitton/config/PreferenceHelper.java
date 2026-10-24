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
					final ListView list = (ListView) listView;
					// 画面の回転やレイアウト変更時（サイズ変化など）に確実に適用する
					list.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
						@Override
						public void onLayoutChange(View v, int left, int top, int right, int bottom,
												   int oldLeft, int oldTop, int oldRight, int oldBottom) {
							// レイアウトパス中の直接変更を避けるため、postで遅延実行する
							v.post(new Runnable() {
								@Override
								public void run() {
									makeAllTitlesMultiline(v);
								}
							});
						}
					});
					list.setOnScrollListener(new AbsListView.OnScrollListener() {
						@Override
						public void onScrollStateChanged(AbsListView view, int scrollState) {
							// スクロールが完全に停止したタイミングで再適用し、表示戻りを防止する
							if (scrollState == SCROLL_STATE_IDLE) {
								view.post(new Runnable() {
									@Override
									public void run() {
										makeAllTitlesMultiline(view);
									}
								});
							}
						}
						@Override
						public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
							// スクロール中の高頻度な更新は避けるため、ここでは何もしない
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
			// 変更前後で状態が異なる場合のみ更新して無駄なレイアウト要求を減らす
			if (tv.getMaxLines() != 10 || tv.getEllipsize() != null) {
				tv.setSingleLine(false);
				tv.setMaxLines(10);
				tv.setEllipsize(null);
			}
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
