package src.comitton.config;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jp.dip.muracoro.comittonx.R;

public class DirChooserActivity extends AppCompatActivity {

	private File currentDir;
	private List<File> fileList;
	private List<String> fileNameList;
	private ArrayAdapter<String> adapter;
	
	private TextView textCurrentPath;
	private ListView listDirs;
	private Button btnSelect;
	private static SharedPreferences sharedPreferences;

	private static final String ROOT_LABEL = "/storage_roots_virtual";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dir_chooser);

		textCurrentPath = findViewById(R.id.text_current_path);
		listDirs = findViewById(R.id.list_dirs);
		btnSelect = findViewById(R.id.btn_select);
		// ナビゲーションバーとボタンが被らないようにインセットを適用
		ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, windowInsets) -> {
			Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
			// 選択ボタンにナビゲーションバーの高さ分の下部マージンを追加
			android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) btnSelect.getLayoutParams();
			int extraMargin = (int) (16 * getResources().getDisplayMetrics().density);
			params.bottomMargin = insets.bottom + extraMargin;
			btnSelect.setLayoutParams(params);
			// 必要に応じてリストの一番下も隠れないようにパディングを追加
			listDirs.setPadding(listDirs.getPaddingLeft(), listDirs.getPaddingTop(), listDirs.getPaddingRight(), insets.bottom + btnSelect.getHeight());
			return WindowInsetsCompat.CONSUMED;
		});

		String startPath = getIntent().getStringExtra("START_PATH");
		if (startPath != null) {
			currentDir = new File(startPath);
		}
		else {
			// 初期値として内蔵ストレージを設定
			currentDir = Environment.getExternalStorageDirectory();
		}
		// ディレクトリの読み出し
		loadDirectory(currentDir);
		// ディレクトリパスがタップされた時の処理
		listDirs.setOnItemClickListener((parent, view, position, id) -> {
			File selected = fileList.get(position);
			if (selected != null && selected.equals(new File(ROOT_LABEL))) {
				// もし「ストレージ一覧」に戻る特殊な項目ならルート一覧を再読込
				showStorageRoots();
			}
			else if (selected.isDirectory()) {
				// ディレクトリの読み出し
				loadDirectory(selected);
			}
		});
		// 選択ボタンがタップされた場合の処理
		btnSelect.setOnClickListener(v -> {
			if (currentDir != null && !currentDir.getAbsolutePath().equals(ROOT_LABEL)) {
				Intent resultIntent = new Intent();
				resultIntent.putExtra("EXTRA_SELECTED_PATH", currentDir.getAbsolutePath());
				setResult(RESULT_OK, resultIntent);
				finish();
			}
			else {
				Resources res = getResources();
				Toast.makeText(this, res.getString(R.string.ValidDirectory), Toast.LENGTH_SHORT).show();
			}
		});
	}
	// ディレクトリの読み出し
	private void loadDirectory(File dir) {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			// アクセスできない場合や仮想ルートの場合はストレージ一覧を表示
			showStorageRoots();
			return;
		}
		currentDir = dir;
		textCurrentPath.setText(currentDir.getAbsolutePath());
		fileList = new ArrayList<>();
		fileNameList = new ArrayList<>();
		// 親ディレクトリへの処理
		File parentFile = currentDir.getParentFile();
		// もし /storage の直下にいるなど、これ以上上がれない、またはストレージのルート付近に来た場合
		if (parentFile != null && !parentFile.getAbsolutePath().equals("/")) {
			fileList.add(parentFile);
			fileNameList.add("..");
		} else {
			// 最上位に達したときは「ストレージ一覧」に戻る選択肢を出す
			fileList.add(new File(ROOT_LABEL));
			Resources res = getResources();
			fileNameList.add(res.getString(R.string.ReturnStorageList));
		}
		File[] files = currentDir.listFiles();
		if (files != null) {
			List<File> subDirs = new ArrayList<>();
			for (File f : files) {
				if (f.isDirectory() && f.canRead()) {
					subDirs.add(f);
				}
			}
			Collections.sort(subDirs);
			for (File f : subDirs) {
				fileList.add(f);
				fileNameList.add(f.getName());
			}
		}
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNameList);
		listDirs.setAdapter(adapter);
	}

	// 利用可能なすべてのストレージ(内蔵＋SDカード)のルートを列挙してリストに表示する
	private void showStorageRoots() {
		currentDir = new File(ROOT_LABEL);
		Resources res = getResources();
		textCurrentPath.setText(res.getString(R.string.StorageSelection));
		fileList = new ArrayList<>();
		fileNameList = new ArrayList<>();
		List<File> roots = getStorageRoots();
		for (File root : roots) {
			fileList.add(root);
			// 表示名を分かりやすくする（内蔵かSDカードか）
			String name = root.getAbsolutePath();
			if (name.contains("emulated") || name.toLowerCase().contains("internal")) {
				fileNameList.add(res.getString(R.string.InternalStorage) + " (" + name + ")");
			}
			else {
				fileNameList.add(res.getString(R.string.ExternalSDcard) + " (" + name + ")");
			}
		}

		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileNameList);
		listDirs.setAdapter(adapter);
	}

	// システムから内蔵・SDカードのルートパスを正確に取得する
	private List<File> getStorageRoots() {
		List<File> roots = new ArrayList<>();
		File[] externalDirs = ContextCompat.getExternalFilesDirs(this, null);
		for (File file : externalDirs) {
			if (file != null) {
				String path = file.getAbsolutePath();
				int index = path.indexOf("/Android/data");
				if (index != -1) {
					File rootDir = new File(path.substring(0, index));
					if (rootDir.exists() && rootDir.isDirectory() && !roots.contains(rootDir)) {
						roots.add(rootDir);
					}
				}
				else {
					if (!roots.contains(file)) {
						roots.add(file);
					}
				}
			}
		}
		// 万が一取得できない場合のフォールバックとして標準の外部ストレージを追加
		if (roots.isEmpty()) {
			roots.add(Environment.getExternalStorageDirectory());
		}
		return roots;
	}
}
