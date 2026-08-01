package src.comitton.dialog;

import java.util.EventListener;

import src.comitton.common.DEF;
import src.comitton.common.ExternalFilterData;
import src.comitton.common.Logcat;
import src.comitton.config.SetImageActivity;
import src.comitton.dialog.ListDialog.ListSelectListener;
import jp.dip.muracoro.comittonx.R;
import src.comitton.imageview.ImageManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Spinner;
import android.view.View.OnClickListener;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

@SuppressLint("NewApi")
public class ImageConfigDialog extends TabDialogFragment implements OnClickListener, OnDismissListener, OnSeekBarChangeListener {
	public static final int CLICK_REVERT   = 0;
	public static final int CLICK_OK       = 1;
	public static final int CLICK_APPLY    = 2;

	private final int SELLIST_ALGORITHM  = 0;
	private final int SELLIST_VIEW_MODE  = 1;
	private final int SELLIST_SCALE_MODE = 2;
	private final int SELLIST_MARGIN_CUT = 3;
	private final int SELLIST_MARGIN_CUTCOLOR = 4;
	private final int SELLIST_DISPLAY_POSITION = 5;
	private final int SELLIST_SCROLL_DIRECTION = 6;

	private final int SELECT_FILTER_BILATERAL = 1;
	private final int SELECT_FILTER_GUIDED = 2;
	private final int SELECT_FILTER_ANISOTROPIC_DIFFUSION = 3;
	private final int SELECT_FILTER_NL_MEANS = 4;
	private final int SELECT_FILTER_WAVELET_THRESHOLD = 5;
	private final int SELECT_FILTER_S_CURVE = 1;
	private final int SELECT_FILTER_ADAPTIVE_THRESHOLD = 2;

	private final int SELECT_FILTER_PRE = 0;
	private final int SELECT_FILTER_POST = 1;

	private final int[] SCALENAME_ORDER = { 0, 1, 6, 2, 3, 7, 4, 5 };

	private ImageConfigListenerInterface mListener = null;

	private ListDialog mListDialog;

	private boolean mInvert;
	private boolean mGray;
	private boolean mColoring;
	private boolean mMoire;
	private boolean mTopSingle;
	private int mSharpen;
	private int mBright;
	private int mGamma;
	private int mBkLight;
	private int mContrast;
	private int mHue;
	private int mSaturation;
	private int mKelvin;
	private int mRedLevel;
	private int mGreenLevel;
	private int mBlueLevel;
	private int mRedLevelBackup;
	private int mGreenLevelBackup;
	private int mBlueLevelBackup;
	private static boolean mCheckRgbLevel;
	private int mAlgoMode;
	private int mDispMode;
	private int mScaleMode;
	private int mMgnCut;
	private int mMgnCutColor;
	private int mDisplayPosition;
	private int mScrollDirection;
	private ExternalFilterData mExternalFilterData;
	private ExternalFilterData mexternalfilterdata;
	private ExternalFilterData mexternalfilterdatacopy;
	private boolean mIsSave;

	private int mAlgoModeTemp;
	private int mDispModeTemp;
	private int mScaleModeTemp;
	private int mMgnCutTemp;
	private int mMgnCutColorTemp;
	private int mDisplayPositionTemp;
	private int mScrollDirectionTemp;

	private Button mBtnRevert;
	private Button mBtnApply;
	private Button mBtnOK;
	private Button mBtnInit1;
	private Button mBtnInit2;
	private CheckBox mChkGray;
	private CheckBox mChkColoring;
	private CheckBox mChkInvert;
	private CheckBox mChkMoire;
	private CheckBox mChkTopSingle;
	private CheckBox mChkIsSave;
	private CheckBox mChkRgbLevel;
	private CheckBox mChkExtFilter;
	private Spinner mSpinnerExtFilter1;
	private Spinner mSpinnerExtFilter2;
	private Spinner mSpinnerExtFilter3;
	private Spinner mSpinnerExtFilter4;
	private TextView mTxtSharpen;
	private TextView mTxtBright;
	private TextView mTxtGamma;
	private TextView mTxtBkLight;
	private TextView mTxtContrast;
	private TextView mTxtHue;
	private TextView mTxtSaturation;
	private TextView mTxtKelvin;
	private TextView mTxtRedLevel;
	private TextView mTxtGreenLevel;
	private TextView mTxtBlueLevel;
	private TextView mTxtExtFilter1;
	private TextView mTxtExtFilter2;
	private TextView mTxtExtFilter3;
	private TextView mTxtExtFilter4;
	private TextView mTxtRadius;
	private TextView mTxtSigmaspatial;
	private TextView mTxtSigmarange;
	private TextView mTxtGuidedr;
	private TextView mTxtGuidedeps;
	private TextView mTxtAditerations;
	private TextView mTxtAdk;
	private TextView mTxtAdlambda;
	private TextView mTxtNlmsearchwindow;
	private TextView mTxtNlmpatchsize;
	private TextView mTxtNlmh;
	private TextView mTxtWaveletthreshold;
	private TextView mTxtScurvegain;
	private TextView mTxtScurvecutoff;
	private TextView mTxtAdaptivewindowsize;
	private TextView mTxtAdaptivec;
	private TextView mTxtInit1;
	private TextView mTxtInit2;
	private SeekBar mSkbSharpen;
	private SeekBar mSkbBright;
	private SeekBar mSkbGamma;
	private SeekBar mSkbBkLight;
	private SeekBar mSkbContrast;
	private SeekBar mSkbHue;
	private SeekBar mSkbSaturation;
	private SeekBar mSkbKelvin;
	private SeekBar mSkbRedLevel;
	private SeekBar mSkbGreenLevel;
	private SeekBar mSkbBlueLevel;
	private SeekBar mSkbRadius;
	private SeekBar mSkbSigmaspatial;
	private SeekBar mSkbSigmarange;
	private SeekBar mSkbGuidedr;
	private SeekBar mSkbGuidedeps;
	private SeekBar mSkbAditerations;
	private SeekBar mSkbAdk;
	private SeekBar mSkbAdlambda;
	private SeekBar mSkbNlmsearchwindow;
	private SeekBar mSkbNlmpatchsize;
	private SeekBar mSkbNlmh;
	private SeekBar mSkbWaveletthreshold;
	private SeekBar mSkbScurvegain;
	private SeekBar mSkbScurvecutoff;
	private SeekBar mSkbAdaptivewindowsize;
	private SeekBar mSkbAdaptivec;
	private TextView mTxtAlgoMode;
	private TextView mTxtDispMode;
	private TextView mTxtScaleMode;
	private TextView mTxtMgncut;
	private TextView mTxtMgncutColor;
	private TextView mTxtDisplayPosition;
	private TextView mTxtScrollDirection;
	private Button mBtnAlgoMode;
	private Button mBtnDispMode;
	private Button mBtnScaleMode;
	private Button mBtnMgncut;
	private Button mBtnMgncutColor;
	private Button mBtnDisplayPosition;
	private Button mBtnScrollDirection;
	private RadioGroup mRadioGroup;
	private RadioButton mRadioButtonPre;
	private RadioButton mRadioButtonPost;

	private String mAlgoModeTitle;
	private String mDispModeTitle;
	private String mScaleModeTitle;
	private String mMgnCutTitle;
	private String mMgnCutColorTitle;
	private String mDisplayOpsitionTitle;
	private String mScrollDirectionTitle;

	private String mSharpenStr;
	private String mBrightStr;
	private String mGammaStr;
	private String mBkLightStr;
	private String mContrastStr;
	private String mHueStr;
	private String mSaturationStr;
	private String mKelvinStr;
	private String mRedLevelStr;
	private String mGreenLevelStr;
	private String mBlueLevelStr;

	private String mRadiusStr;
	private String mSigmaspatialStr;
	private String mSigmarangeStr;
	private String mGuidedrStr;
	private String mGuidedepsStr;
	private String mAditerationsStr;
	private String mAdkStr;
	private String mAdlambdaStr;
	private String mNlmsearchwindowStr;
	private String mNlmpatchsizeStr;
	private String mNlmhStr;
	private String mWaveletthresholdStr;
	private String mScurvegainStr;
	private String mScurvecutoffStr;
	private String mAdaptivewindowsizeStr;
	private String mAdaptivecStr;

	private String mAutoStr;
	private static String mNoneStr;
	private String mDegreeStr;

	private String[] mAlgoModeItems;
	private String[] mDispModeItems;
	private String[] mScaleModeItems;
	private String[] mMgnCutItems;
	private String[] mMgnCutColorItems;
	private String[] mDisplayPositionItems;
	private String[] mScrollDirectionItems;

	private int mSelectMode;
	private int mCommandId;
	private static int[] mKelvinRgb = { 100, 100, 100 };
	private static float[] mGuided_eps_data = { 0.001f, 0.002f, 0.005f, 0.01f, 0.02f, 0.05f, 0.1f };
	private final int SELECT_GUIDED_INIT = 3;

	public ImageConfigDialog(AppCompatActivity activity, @StyleRes int themeResId, int command_id, boolean isclose, MenuDialog.MenuSelectListener listener) {
		super(activity, themeResId, isclose, false, false, true, listener);

		Resources res = activity.getResources();
		mAutoStr = res.getString(R.string.auto);
		mNoneStr = res.getString(R.string.none);
		mDegreeStr = res.getString(R.string.degree);

		mCommandId = command_id;

		int nItem;

		// 画像補間法の選択肢設定
		mAlgoModeTitle = res.getString(R.string.algoriMenu);
		nItem = SetImageActivity.AlgoModeName.length;
		mAlgoModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mAlgoModeItems[i] = res.getString(SetImageActivity.AlgoModeName[i]);
		}

		// 見開きモードの選択肢設定
		mDispModeTitle = res.getString(R.string.tguide02);
		nItem = SetImageActivity.ViewName.length;
		mDispModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mDispModeItems[i] = res.getString(SetImageActivity.ViewName[i]);
		}

		// サイズ設定の選択肢設定
		mScaleModeTitle = res.getString(R.string.tguide03);
		nItem = SetImageActivity.ScaleName.length;
		mScaleModeItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mScaleModeItems[i] = res.getString(SetImageActivity.ScaleName[SCALENAME_ORDER[i]]);
		}

		// 余白削除
		mMgnCutTitle = res.getString(R.string.mgnCutMenu);
		nItem = SetImageActivity.MgnCutName.length;
		mMgnCutItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mMgnCutItems[i] = res.getString(SetImageActivity.MgnCutName[i]);
		}

		// 余白削除の色
		mMgnCutColorTitle = res.getString(R.string.mgnCutColorMenu);
		nItem = SetImageActivity.MgnCutColorName.length;
		mMgnCutColorItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mMgnCutColorItems[i] = res.getString(SetImageActivity.MgnCutColorName[i]);
		}

		// 画面の表示位置
		mDisplayOpsitionTitle = res.getString(R.string.DisplayPositionMenu);
		nItem = SetImageActivity.DisplayPositionName.length;
		mDisplayPositionItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mDisplayPositionItems[i] = res.getString(SetImageActivity.DisplayPositionName[i]);
		}

		// スクロール方向
		mScrollDirectionTitle = res.getString(R.string.ScrollDirectionMenu);
		nItem = SetImageActivity.ScrollMode.length;
		mScrollDirectionItems = new String[nItem];
		for (int i = 0; i < nItem; i++) {
			mScrollDirectionItems[i] = res.getString(SetImageActivity.ScrollMode[i]);
		}

		LayoutInflater inflater = LayoutInflater.from(mActivity);

		addSection(res.getString(R.string.imgConfFilter));
		addItem(inflater.inflate(R.layout.imageconfig_filter, null, false));

		if (mCommandId == DEF.MENU_IMGCONF) {
			addSection(res.getString(R.string.imgConfOther));
		}
		addItem(inflater.inflate(R.layout.imageconfig_other, null, false));
	}

	public void setConfig(boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave, int displayposition, int contrast, int hue, int saturation, boolean coloring, int scrolldirection, int kelvin, boolean chkrgblevel, int redrevel, int greenlevel, int bluerevel, ExternalFilterData externalfilterdata) {
		mGray = gray;
		mColoring = coloring;
		mInvert = invert;
		mMoire = moire;
		mTopSingle = topsingle;
		mSharpen = sharpen;
		mBright = bright;
		mGamma = gamma;
		mBkLight = bklight;
		mContrast = contrast;
		mHue = hue;
		mSaturation = saturation;
		mKelvin = kelvin;
		mCheckRgbLevel = chkrgblevel;
		mRedLevel = redrevel;
		mGreenLevel = greenlevel;
		mBlueLevel = bluerevel;
		mRedLevelBackup = redrevel;
		mGreenLevelBackup = greenlevel;
		mBlueLevelBackup = bluerevel;
		mAlgoModeTemp  = mAlgoMode  = algomode;
		mDispModeTemp  = mDispMode  = dispmode;
		mScaleModeTemp = mScaleMode = scalemode;
		mMgnCutTemp    = mMgnCut    = mgncut;
		mMgnCutColorTemp    = mMgnCutColor    = mgncutcolor;
		mDisplayPositionTemp = mDisplayPosition = displayposition;
		mScrollDirectionTemp = mScrollDirection = scrolldirection;
		mKelvinRgb = ImageManager.getRGBFromKelvin(kelvin);
		mExternalFilterData = externalfilterdata;
		if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
			// 複製を作成
			mexternalfilterdata = mExternalFilterData.clone();
			mexternalfilterdatacopy = mExternalFilterData.clone();
		}

		mIsSave = issave;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		super.onCreateView(inflater, container, savedInstanceState);

		Resources res = mActivity.getResources();
		addHeader(res.getString(R.string.imgConfMenu));

		LinearLayout footer = (LinearLayout)inflater.inflate(R.layout.imagetextconfig_footer, mFooter, true);
		footer.setBackgroundColor(0x80000000);
		// Android 5.1でテキストの色がおかしかったので暫定
		((CheckBox)footer.findViewById(R.id.chk_save)).setTextAppearance(mActivity, mThemeResId);
		//addFooter(footer);

		mChkIsSave = (CheckBox) mView.findViewById(R.id.chk_save);

		for( int i = 0; i < mViewArray.size(); ++i) {
			mChkGray = mChkGray != null ? mChkGray : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_gray);
			mChkColoring = mChkColoring != null ? mChkColoring : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_coloring);
			mChkInvert = mChkInvert != null ? mChkInvert : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_invert);
			mChkMoire = mChkMoire != null ? mChkMoire : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_moire);
			mChkTopSingle = mChkTopSingle != null ? mChkTopSingle : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_topsingle);
			mChkRgbLevel = mChkRgbLevel != null ? mChkRgbLevel : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_rgblevelenable);
			mChkExtFilter = mChkExtFilter != null ? mChkExtFilter : (CheckBox) mViewArray.get(i).findViewById(R.id.chk_extfilter);
			mSpinnerExtFilter1 = mSpinnerExtFilter1 != null ? mSpinnerExtFilter1 : (Spinner) mViewArray.get(i).findViewById(R.id.spinner_extfilter1);
			mSpinnerExtFilter2 = mSpinnerExtFilter2 != null ? mSpinnerExtFilter2 : (Spinner) mViewArray.get(i).findViewById(R.id.spinner_extfilter2);
			mSpinnerExtFilter3 = mSpinnerExtFilter3 != null ? mSpinnerExtFilter3 : (Spinner) mViewArray.get(i).findViewById(R.id.spinner_extfilter3);
			mSpinnerExtFilter4 = mSpinnerExtFilter4 != null ? mSpinnerExtFilter4 : (Spinner) mViewArray.get(i).findViewById(R.id.spinner_extfilter4);

			mTxtSharpen = mTxtSharpen != null ? mTxtSharpen : (TextView) mViewArray.get(i).findViewById(R.id.label_sharpen);
			mTxtBright = mTxtBright != null ? mTxtBright : (TextView) mViewArray.get(i).findViewById(R.id.label_bright);
			mTxtGamma = mTxtGamma != null ? mTxtGamma : (TextView) mViewArray.get(i).findViewById(R.id.label_gamma);
			mTxtContrast = mTxtContrast != null ? mTxtContrast : (TextView) mViewArray.get(i).findViewById(R.id.label_contrast);
			mTxtHue = mTxtHue != null ? mTxtHue : (TextView) mViewArray.get(i).findViewById(R.id.label_hue);
			mTxtSaturation = mTxtSaturation != null ? mTxtSaturation : (TextView) mViewArray.get(i).findViewById(R.id.label_saturation) ;
			mTxtKelvin = mTxtKelvin != null ? mTxtKelvin : (TextView) mViewArray.get(i).findViewById(R.id.label_kelvin) ;
			mTxtRedLevel = mTxtRedLevel != null ? mTxtRedLevel : (TextView) mViewArray.get(i).findViewById(R.id.label_redlevel) ;
			mTxtGreenLevel = mTxtGreenLevel != null ? mTxtGreenLevel : (TextView) mViewArray.get(i).findViewById(R.id.label_greenlevel) ;
			mTxtBlueLevel = mTxtBlueLevel != null ? mTxtBlueLevel : (TextView) mViewArray.get(i).findViewById(R.id.label_bluelevel) ;
			mTxtBkLight = mTxtBkLight != null ? mTxtBkLight : (TextView) mViewArray.get(i).findViewById(R.id.label_bklight);
			mTxtExtFilter1 = mTxtExtFilter1 != null ? mTxtExtFilter1 : (TextView) mViewArray.get(i).findViewById(R.id.label_textextfilter1);
			mTxtExtFilter2 = mTxtExtFilter2 != null ? mTxtExtFilter2 : (TextView) mViewArray.get(i).findViewById(R.id.label_textextfilter2);
			mTxtExtFilter3 = mTxtExtFilter3 != null ? mTxtExtFilter3 : (TextView) mViewArray.get(i).findViewById(R.id.label_textextfilter3);
			mTxtExtFilter4 = mTxtExtFilter4 != null ? mTxtExtFilter4 : (TextView) mViewArray.get(i).findViewById(R.id.label_textextfilter4);
			mTxtRadius = mTxtRadius != null ? mTxtRadius : (TextView) mViewArray.get(i).findViewById(R.id.label_radius);
			mTxtSigmaspatial = mTxtSigmaspatial != null ? mTxtSigmaspatial : (TextView) mViewArray.get(i).findViewById(R.id.label_sigma_spatial);
			mTxtSigmarange = mTxtSigmarange != null ? mTxtSigmarange : (TextView) mViewArray.get(i).findViewById(R.id.label_sigma_range);
			mTxtGuidedr = mTxtGuidedr != null ? mTxtGuidedr : (TextView) mViewArray.get(i).findViewById(R.id.label_guided_r);
			mTxtGuidedeps = mTxtGuidedeps != null ? mTxtGuidedeps : (TextView) mViewArray.get(i).findViewById(R.id.label_guided_eps);
			mTxtAditerations = mTxtAditerations != null ? mTxtAditerations : (TextView) mViewArray.get(i).findViewById(R.id.label_ad_iterations);
			mTxtAdk = mTxtAdk != null ? mTxtAdk : (TextView) mViewArray.get(i).findViewById(R.id.label_ad_k);
			mTxtAdlambda = mTxtAdlambda != null ? mTxtAdlambda : (TextView) mViewArray.get(i).findViewById(R.id.label_ad_lambda);
			mTxtNlmsearchwindow = mTxtNlmsearchwindow != null ? mTxtNlmsearchwindow : (TextView) mViewArray.get(i).findViewById(R.id.label_nlm_search_window);
			mTxtNlmpatchsize = mTxtNlmpatchsize != null ? mTxtNlmpatchsize : (TextView) mViewArray.get(i).findViewById(R.id.label_nlm_patch_size);
			mTxtNlmh = mTxtNlmh != null ? mTxtNlmh : (TextView) mViewArray.get(i).findViewById(R.id.label_nlm_h);
			mTxtWaveletthreshold = mTxtWaveletthreshold != null ? mTxtWaveletthreshold : (TextView) mViewArray.get(i).findViewById(R.id.label_wavelet_threshold);
			mTxtScurvegain = mTxtScurvegain != null ? mTxtScurvegain : (TextView) mViewArray.get(i).findViewById(R.id.label_scurve_gain);
			mTxtScurvecutoff = mTxtScurvecutoff != null ? mTxtScurvecutoff : (TextView) mViewArray.get(i).findViewById(R.id.label_scurve_cutoff);
			mTxtAdaptivewindowsize = mTxtAdaptivewindowsize != null ? mTxtAdaptivewindowsize : (TextView) mViewArray.get(i).findViewById(R.id.label_adaptive_window_size);
			mTxtAdaptivec = mTxtAdaptivec != null ? mTxtAdaptivec : (TextView) mViewArray.get(i).findViewById(R.id.label_adaptive_c);

			mSkbSharpen = mSkbSharpen != null ? mSkbSharpen : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_sharpen);
			mSkbBright = mSkbBright != null ? mSkbBright : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bright);
			mSkbGamma = mSkbGamma != null ? mSkbGamma : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_gamma);
			mSkbContrast = mSkbContrast != null ? mSkbContrast : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_contrast);
			mSkbHue = mSkbHue != null ? mSkbHue : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_hue);
			mSkbSaturation = mSkbSaturation != null ? mSkbSaturation : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_saturation);
			mSkbKelvin = mSkbKelvin != null ? mSkbKelvin : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_kelvin);
			mSkbRedLevel = mSkbRedLevel != null ? mSkbRedLevel : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_redlevel);
			mSkbGreenLevel = mSkbGreenLevel != null ? mSkbGreenLevel : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_greenlevel);
			mSkbBlueLevel = mSkbBlueLevel != null ? mSkbBlueLevel : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bluelevel);
			mSkbBkLight = mSkbBkLight != null ? mSkbBkLight : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_bklight);
			mSkbRadius = mSkbRadius != null ? mSkbRadius : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_radius);
			mSkbSigmaspatial = mSkbSigmaspatial != null ? mSkbSigmaspatial : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_sigma_spatial);
			mSkbSigmarange = mSkbSigmarange != null ? mSkbSigmarange : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_sigma_range);
			mSkbGuidedr = mSkbGuidedr != null ? mSkbGuidedr : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_guided_r);
			mSkbGuidedeps = mSkbGuidedeps != null ? mSkbGuidedeps : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_guided_eps);
			mSkbAditerations = mSkbAditerations != null ? mSkbAditerations : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_ad_iterations);
			mSkbAdk = mSkbAdk != null ? mSkbAdk : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_ad_k);
			mSkbAdlambda = mSkbAdlambda != null ? mSkbAdlambda : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_ad_lambda);
			mSkbNlmsearchwindow = mSkbNlmsearchwindow != null ? mSkbNlmsearchwindow : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_nlm_search_window);
			mSkbNlmpatchsize = mSkbNlmpatchsize != null ? mSkbNlmpatchsize : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_nlm_patch_size);
			mSkbNlmh = mSkbNlmh != null ? mSkbNlmh : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_nlm_h);
			mSkbWaveletthreshold = mSkbWaveletthreshold != null ? mSkbWaveletthreshold : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_wavelet_threshold);
			mSkbScurvegain = mSkbScurvegain != null ? mSkbScurvegain : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_scurve_gain);
			mSkbScurvecutoff = mSkbScurvecutoff != null ? mSkbScurvecutoff : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_scurve_cutoff);
			mSkbAdaptivewindowsize = mSkbAdaptivewindowsize != null ? mSkbAdaptivewindowsize : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_adaptive_window_size);
			mSkbAdaptivec = mSkbAdaptivec != null ? mSkbAdaptivec : (SeekBar) mViewArray.get(i).findViewById(R.id.seek_adaptive_c);

			mTxtAlgoMode = mTxtAlgoMode != null ? mTxtAlgoMode : (TextView) mViewArray.get(i).findViewById(R.id.label_algomode);
			mTxtDispMode = mTxtDispMode != null ? mTxtDispMode : (TextView) mViewArray.get(i).findViewById(R.id.label_spread);
			mTxtScaleMode = mTxtScaleMode != null ? mTxtScaleMode : (TextView) mViewArray.get(i).findViewById(R.id.label_scale);
			mTxtMgncut = mTxtMgncut != null ? mTxtMgncut : (TextView) mViewArray.get(i).findViewById(R.id.label_mgncut);
			mTxtMgncutColor = mTxtMgncutColor != null ? mTxtMgncutColor : (TextView) mViewArray.get(i).findViewById(R.id.label_mgncutcolor);
			mTxtDisplayPosition = mTxtDisplayPosition != null ? mTxtDisplayPosition : (TextView) mViewArray.get(i).findViewById(R.id.label_displayposition);
			mTxtScrollDirection = mTxtScrollDirection != null ? mTxtScrollDirection : (TextView) mViewArray.get(i).findViewById(R.id.label_scrolldirection);
			mTxtInit1 = mTxtInit1 != null ? mTxtInit1 : (TextView) mViewArray.get(i).findViewById(R.id.label_init1);
			mTxtInit2 = mTxtInit2 != null ? mTxtInit2 : (TextView) mViewArray.get(i).findViewById(R.id.label_init2);

			mBtnAlgoMode = mBtnAlgoMode != null ? mBtnAlgoMode : (Button) mViewArray.get(i).findViewById(R.id.btn_algomode);
			mBtnDispMode = mBtnDispMode != null ? mBtnDispMode : (Button) mViewArray.get(i).findViewById(R.id.btn_spread);
			mBtnScaleMode = mBtnScaleMode != null ? mBtnScaleMode : (Button) mViewArray.get(i).findViewById(R.id.btn_scale);
			mBtnMgncut = mBtnMgncut != null ? mBtnMgncut : (Button) mViewArray.get(i).findViewById(R.id.btn_mgncut);
			mBtnMgncutColor = mBtnMgncutColor != null ? mBtnMgncutColor : (Button) mViewArray.get(i).findViewById(R.id.btn_mgncutcolor);
			mBtnDisplayPosition = mBtnDisplayPosition != null ? mBtnDisplayPosition : (Button) mViewArray.get(i).findViewById(R.id.btn_displayposition);
			mBtnScrollDirection = mBtnScrollDirection != null ? mBtnScrollDirection : (Button) mViewArray.get(i).findViewById(R.id.btn_scrolldirection);
			mRadioGroup = mRadioGroup != null ? mRadioGroup : (RadioGroup) mViewArray.get(i).findViewById(R.id.radioGroupFilter);
			mRadioButtonPre = mRadioButtonPre != null ? mRadioButtonPre : (RadioButton) mViewArray.get(i).findViewById(R.id.radio_extprefilter);
			mRadioButtonPost = mRadioButtonPost != null ? mRadioButtonPost : (RadioButton) mViewArray.get(i).findViewById(R.id.radio_extpostfilter);
			mBtnInit1 = mBtnInit1 != null ? mBtnInit1 : (Button) mViewArray.get(i).findViewById(R.id.button_init1);
			mBtnInit2 = mBtnInit2 != null ? mBtnInit2 : (Button) mViewArray.get(i).findViewById(R.id.button_init2);
		}

		mKelvinRgb = ImageManager.getRGBFromKelvin(mKelvin);

		if (mTxtRedLevel != null) mRedLevelStr = mTxtRedLevel.getText().toString();
		if (mTxtGreenLevel != null) mGreenLevelStr = mTxtGreenLevel.getText().toString();
		if (mTxtBlueLevel != null) mBlueLevelStr = mTxtBlueLevel.getText().toString();
		mChkRgbLevel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// チェックされた時の処理
					mSkbRedLevel.setEnabled(true);
					mSkbGreenLevel.setEnabled(true);
					mSkbBlueLevel.setEnabled(true);
					String str;
					str = getRgbLevelStr(mRedLevel);
					mTxtRedLevel.setText(mRedLevelStr.replaceAll("%", str));
					mSkbRedLevel.setProgress(mRedLevel);
					str = getRgbLevelStr(mGreenLevel);
					mTxtGreenLevel.setText(mGreenLevelStr.replaceAll("%", str));
					mSkbGreenLevel.setProgress(mGreenLevel);
					str = getRgbLevelStr(mBlueLevel);
					mTxtBlueLevel.setText(mBlueLevelStr.replaceAll("%", str));
					mSkbBlueLevel.setProgress(mBlueLevel);
				}
				else {
					// チェックが外れた時の処理
					mSkbRedLevel.setEnabled(false);
					mSkbGreenLevel.setEnabled(false);
					mSkbBlueLevel.setEnabled(false);
					String str;
					str = getRgbLevelStr(mKelvinRgb[0]);
					mTxtRedLevel.setText(mRedLevelStr.replaceAll("%", str));
					mSkbRedLevel.setProgress(mKelvinRgb[0]);
					str = getRgbLevelStr(mKelvinRgb[1]);
					mTxtGreenLevel.setText(mGreenLevelStr.replaceAll("%", str));
					mSkbGreenLevel.setProgress(mKelvinRgb[1]);
					str = getRgbLevelStr(mKelvinRgb[2]);
					mTxtBlueLevel.setText(mBlueLevelStr.replaceAll("%", str));
					mSkbBlueLevel.setProgress(mKelvinRgb[2]);
				}
			}
		});

		if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
			if (mChkExtFilter != null) mChkExtFilter.setChecked(mexternalfilterdata.mCheckExtFilter);
			if (mRadioButtonPre != null) mRadioButtonPre.setChecked((mexternalfilterdata.mRadioFilter == SELECT_FILTER_PRE) ? true : false);
			if (mRadioButtonPost != null) mRadioButtonPost.setChecked((mexternalfilterdata.mRadioFilter == SELECT_FILTER_POST) ? true : false);
		}

		mChkExtFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					// チェックされた時の処理
					mexternalfilterdata.mCheckExtFilter = true;
				}
				else {
					// チェックが外れた時の処理
					mexternalfilterdata.mCheckExtFilter = false;
				}
				// 拡張フィルターの表示更新
				setFilterView();
			}
		});

		mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_extprefilter) {
					// 画像補間前が選択されたときの処理
					mexternalfilterdata.mRadioFilter = SELECT_FILTER_PRE;
				}
				else if (checkedId == R.id.radio_extpostfilter) {
					// 画像補間後が選択されたときの処理
					mexternalfilterdata.mRadioFilter = SELECT_FILTER_POST;
				}
			}
		});

		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF) {
			mChkGray.setVisibility(View.GONE);
			mChkColoring.setVisibility(View.GONE);
			mChkInvert.setVisibility(View.GONE);
			mChkMoire.setVisibility(View.GONE);
			mChkTopSingle.setVisibility(View.GONE);

			mTxtAlgoMode.setVisibility(View.GONE);
			mTxtDispMode.setVisibility(View.GONE);
			mTxtScaleMode.setVisibility(View.GONE);
			mTxtMgncut.setVisibility(View.GONE);
			mTxtMgncutColor.setVisibility(View.GONE);
			mTxtDisplayPosition.setVisibility(View.GONE);
			mTxtScrollDirection.setVisibility(View.GONE);

			mBtnAlgoMode.setVisibility(View.GONE);
			mBtnDispMode.setVisibility(View.GONE);
			mBtnScaleMode.setVisibility(View.GONE);
			mBtnMgncut.setVisibility(View.GONE);
			mBtnMgncutColor.setVisibility(View.GONE);
			mBtnDisplayPosition.setVisibility(View.GONE);
			mBtnScrollDirection.setVisibility(View.GONE);

		}
		if (mCommandId == DEF.MENU_WEBIMGCONF) {
			mChkMoire.setVisibility(View.GONE);
			mChkTopSingle.setVisibility(View.GONE);

			mChkExtFilter.setVisibility(View.GONE);
			mTxtAlgoMode.setVisibility(View.GONE);
			mTxtDispMode.setVisibility(View.GONE);
			mTxtScaleMode.setVisibility(View.GONE);
			mTxtMgncut.setVisibility(View.GONE);
			mTxtMgncutColor.setVisibility(View.GONE);
			mTxtDisplayPosition.setVisibility(View.GONE);
			mTxtScrollDirection.setVisibility(View.GONE);

			mBtnAlgoMode.setVisibility(View.GONE);
			mBtnDispMode.setVisibility(View.GONE);
			mBtnScaleMode.setVisibility(View.GONE);
			mBtnMgncut.setVisibility(View.GONE);
			mBtnMgncutColor.setVisibility(View.GONE);
			mBtnDisplayPosition.setVisibility(View.GONE);
			mBtnScrollDirection.setVisibility(View.GONE);
		}
		if (mCommandId == DEF.MENU_SHARPEN || mCommandId == DEF.MENU_BRIGHT || mCommandId == DEF.MENU_GAMMA || mCommandId == DEF.MENU_BKLIGHT || mCommandId == DEF.MENU_CONTRAST || mCommandId == DEF.MENU_HUE || mCommandId == DEF.MENU_SATURATION || mCommandId == DEF.MENU_KELVIN) {
			// 拡張フィルター以外の場合は表示させない
			mChkExtFilter.setVisibility(View.GONE);
			setFilterView();
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_SHARPEN) {
			mTxtSharpen.setVisibility(View.GONE);
			mSkbSharpen.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_BRIGHT) {
			mTxtBright.setVisibility(View.GONE);
			mSkbBright.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_GAMMA) {
			mTxtGamma.setVisibility(View.GONE);
			mSkbGamma.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_BKLIGHT) {
			mTxtBkLight.setVisibility(View.GONE);
			mSkbBkLight.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_CONTRAST) {
			mTxtContrast.setVisibility(View.GONE);
			mSkbContrast.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_HUE) {
			mTxtHue.setVisibility(View.GONE);
			mSkbHue.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_SATURATION) {
			mTxtSaturation.setVisibility(View.GONE);
			mSkbSaturation.setVisibility(View.GONE);
		}
		if (mCommandId != DEF.MENU_IMGCONF && mCommandId != DEF.MENU_WEBIMGCONF && mCommandId != DEF.MENU_KELVIN) {
			mTxtKelvin.setVisibility(View.GONE);
			mSkbKelvin.setVisibility(View.GONE);
			mChkRgbLevel.setVisibility(View.GONE);
			mTxtRedLevel.setVisibility(View.GONE);
			mSkbRedLevel.setVisibility(View.GONE);
			mTxtGreenLevel.setVisibility(View.GONE);
			mSkbGreenLevel.setVisibility(View.GONE);
			mTxtBlueLevel.setVisibility(View.GONE);
			mSkbBlueLevel.setVisibility(View.GONE);
		}

		// 拡張フィルターの表示更新
		setFilterView();

		if (mChkGray != null) mChkGray.setChecked(mGray);
		if (mChkColoring != null) mChkColoring.setChecked(mColoring);
		if (mChkInvert != null) mChkInvert.setChecked(mInvert);
		if (mChkMoire != null) mChkMoire.setChecked(mMoire);
		if (mChkTopSingle != null) mChkTopSingle.setChecked(mTopSingle);
		if (mChkIsSave != null) mChkIsSave.setChecked(mIsSave);
		if (mChkRgbLevel != null) mChkRgbLevel.setChecked(mCheckRgbLevel);

		if (mTxtSharpen != null) mSharpenStr = mTxtSharpen.getText().toString();
		if (mTxtBright != null) mBrightStr = mTxtBright.getText().toString();
		if (mTxtGamma != null) mGammaStr = mTxtGamma.getText().toString();
		if (mTxtBkLight != null) mBkLightStr = mTxtBkLight.getText().toString();
		if (mTxtContrast != null) mContrastStr = mTxtContrast.getText().toString();
		if (mTxtHue != null) mHueStr = mTxtHue.getText().toString();
		if (mTxtSaturation != null) mSaturationStr = mTxtSaturation.getText().toString();
		if (mTxtKelvin != null) mKelvinStr = mTxtKelvin.getText().toString();
		if (mTxtSharpen != null) mTxtSharpen.setText(mSharpenStr.replaceAll("%", getSharpenStr(getContext(), mSharpen)));
		if (mTxtBright != null) mTxtBright.setText(mBrightStr.replaceAll("%", getBrightGammaStr(getContext(), mBright)));
		if (mTxtGamma != null) mTxtGamma.setText(mGammaStr.replaceAll("%", getBrightGammaStr(getContext(), mGamma)));
		if (mTxtBkLight != null) mTxtBkLight.setText(mBkLightStr.replaceAll("%", getBkLight(mBkLight)));
		if (mTxtContrast != null) mTxtContrast.setText(mContrastStr.replaceAll("%", getBrightGammaStr(getContext(), mContrast)));
		if (mTxtHue != null) mTxtHue.setText(mHueStr.replaceAll("%", getBrightGammaStr(getContext(), mHue)));
		if (mTxtSaturation != null) mTxtSaturation.setText(mSaturationStr.replaceAll("%", getBrightGammaStr(getContext(), mSaturation)));
		if (mTxtKelvin != null) mTxtKelvin.setText(mKelvinStr.replaceAll("%", getBrightGammaStr(getContext(), mKelvin)));

		if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
			if (mTxtRadius != null) mRadiusStr = mTxtRadius.getText().toString();
			if (mTxtRadius != null) mTxtRadius.setText(mRadiusStr.replaceAll("%",getRadiusStr(mexternalfilterdata.mRadius)));
			if (mTxtSigmaspatial != null) mSigmaspatialStr = mTxtSigmaspatial.getText().toString();
			if (mTxtSigmaspatial != null) mTxtSigmaspatial.setText(mSigmaspatialStr.replaceAll("%",getSigmaspatialStr((int)(mexternalfilterdata.mSigma_spatial * 10))));
			if (mTxtSigmarange != null) mSigmarangeStr = mTxtSigmarange.getText().toString();
			if (mTxtSigmarange != null) mTxtSigmarange.setText(mSigmarangeStr.replaceAll("%",getSigmarangeStr((int)(mexternalfilterdata.mSigma_range))));
			if (mTxtGuidedr != null) mGuidedrStr = mTxtGuidedr.getText().toString();
			if (mTxtGuidedeps != null) mGuidedepsStr = mTxtGuidedeps.getText().toString();
			if (mTxtAditerations != null) mAditerationsStr = mTxtAditerations.getText().toString();
			if (mTxtAdk != null) mAdkStr = mTxtAdk.getText().toString();
			if (mTxtAdlambda != null) mAdlambdaStr = mTxtAdlambda.getText().toString();
			if (mTxtNlmsearchwindow != null) mNlmsearchwindowStr = mTxtNlmsearchwindow.getText().toString();
			if (mTxtNlmpatchsize != null) mNlmpatchsizeStr = mTxtNlmpatchsize.getText().toString();
			if (mTxtNlmh != null) mNlmhStr = mTxtNlmh.getText().toString();
			if (mTxtWaveletthreshold != null) mWaveletthresholdStr = mTxtWaveletthreshold.getText().toString();
			if (mTxtScurvegain != null) mScurvegainStr = mTxtScurvegain.getText().toString();
			if (mTxtScurvecutoff != null) mScurvecutoffStr = mTxtScurvecutoff.getText().toString();
			if (mTxtAdaptivewindowsize != null) mAdaptivewindowsizeStr = mTxtAdaptivewindowsize.getText().toString();
			if (mTxtAdaptivec != null) mAdaptivecStr = mTxtAdaptivec.getText().toString();
		}

		if (mSkbSharpen != null) mSkbSharpen.setMax(32);
		if (mSkbSharpen != null) mSkbSharpen.setOnSeekBarChangeListener(this);
		if (mSkbBright != null) mSkbBright.setMax(10);
		if (mSkbBright != null) mSkbBright.setOnSeekBarChangeListener(this);
		if (mSkbGamma != null) mSkbGamma.setMax(10);
		if (mSkbGamma != null) mSkbGamma.setOnSeekBarChangeListener(this);
		if (mSkbBkLight != null) mSkbBkLight.setMax(11);
		if (mSkbBkLight != null) mSkbBkLight.setOnSeekBarChangeListener(this);
		if (mSkbContrast != null) mSkbContrast.setMax(20);
		if (mSkbContrast != null) mSkbContrast.setOnSeekBarChangeListener(this);
		if (mSkbHue != null) mSkbHue.setMax(40);
		if (mSkbHue != null) mSkbHue.setOnSeekBarChangeListener(this);
		if (mSkbSaturation != null) mSkbSaturation.setMax(80);
		if (mSkbSaturation != null) mSkbSaturation.setOnSeekBarChangeListener(this);
		if (mSkbKelvin != null) mSkbKelvin.setMax(70);
		if (mSkbKelvin != null) mSkbKelvin.setOnSeekBarChangeListener(this);
		if (mSkbRedLevel != null) mSkbRedLevel.setMax(100);
		if (mSkbRedLevel != null) mSkbRedLevel.setOnSeekBarChangeListener(this);
		if (mSkbGreenLevel != null) mSkbGreenLevel.setMax(100);
		if (mSkbGreenLevel != null) mSkbGreenLevel.setOnSeekBarChangeListener(this);
		if (mSkbBlueLevel != null) mSkbBlueLevel.setMax(100);
		if (mSkbBlueLevel != null) mSkbBlueLevel.setOnSeekBarChangeListener(this);

		if (mSkbRadius != null) mSkbRadius.setMax(9);
		if (mSkbRadius != null) mSkbRadius.setOnSeekBarChangeListener(this);
		if (mSkbSigmaspatial != null) mSkbSigmaspatial.setMax(95);
		if (mSkbSigmaspatial != null) mSkbSigmaspatial.setOnSeekBarChangeListener(this);
		if (mSkbSigmarange != null) mSkbSigmarange.setMax(99);
		if (mSkbSigmarange != null) mSkbSigmarange.setOnSeekBarChangeListener(this);

		if (mSkbGuidedr != null) mSkbGuidedr.setMax(19);
		if (mSkbGuidedr != null) mSkbGuidedr.setOnSeekBarChangeListener(this);
		if (mSkbGuidedeps != null) mSkbGuidedeps.setMax(mGuided_eps_data.length - 1);
		if (mSkbGuidedeps != null) mSkbGuidedeps.setOnSeekBarChangeListener(this);

		if (mSkbAditerations != null) mSkbAditerations.setMax(19);
		if (mSkbAditerations != null) mSkbAditerations.setOnSeekBarChangeListener(this);
		if (mSkbAdk != null) mSkbAdk.setMax(98);
		if (mSkbAdk != null) mSkbAdk.setOnSeekBarChangeListener(this);
		if (mSkbAdlambda != null) mSkbAdlambda.setMax(24);
		if (mSkbAdlambda != null) mSkbAdlambda.setOnSeekBarChangeListener(this);

		if (mSkbNlmsearchwindow != null) mSkbNlmsearchwindow.setMax(10);
		if (mSkbNlmsearchwindow != null) mSkbNlmsearchwindow.setOnSeekBarChangeListener(this);
		if (mSkbNlmpatchsize != null) mSkbNlmpatchsize.setMax(3);
		if (mSkbNlmpatchsize != null) mSkbNlmpatchsize.setOnSeekBarChangeListener(this);
		if (mSkbNlmh != null) mSkbNlmh.setMax(58);
		if (mSkbNlmh != null) mSkbNlmh.setOnSeekBarChangeListener(this);

		if (mSkbWaveletthreshold != null) mSkbWaveletthreshold.setMax(100);
		if (mSkbWaveletthreshold != null) mSkbWaveletthreshold.setOnSeekBarChangeListener(this);

		if (mSkbScurvegain != null) mSkbScurvegain.setMax(58);
		if (mSkbScurvegain != null) mSkbScurvegain.setOnSeekBarChangeListener(this);
		if (mSkbScurvecutoff != null) mSkbScurvecutoff.setMax(100);
		if (mSkbScurvecutoff != null) mSkbScurvecutoff.setOnSeekBarChangeListener(this);

		if (mSkbAdaptivewindowsize != null) mSkbAdaptivewindowsize.setMax(48);
		if (mSkbAdaptivewindowsize != null) mSkbAdaptivewindowsize.setOnSeekBarChangeListener(this);
		if (mSkbAdaptivec != null) mSkbAdaptivec.setMax(100);
		if (mSkbAdaptivec != null) mSkbAdaptivec.setOnSeekBarChangeListener(this);

		// 初期値が0だと表示が更新されないので故意にずらした値で設定を入れる
		if (mSkbSharpen != null) mSkbSharpen.setProgress(32 - mSharpen);
		if (mSkbSharpen != null) mSkbSharpen.setProgress(mSharpen);
		if (mSkbBright != null) mSkbBright.setProgress(10 - (mBright + 5));
		if (mSkbBright != null) mSkbBright.setProgress(mBright + 5);
		if (mSkbGamma != null) mSkbGamma.setProgress(10 - (mGamma + 5));
		if (mSkbGamma != null) mSkbGamma.setProgress(mGamma + 5);
		if (mSkbBkLight != null) mSkbBkLight.setProgress(mBkLight);
		if (mSkbContrast != null) mSkbContrast.setProgress(20 - (mContrast / 5));
		if (mSkbContrast != null) mSkbContrast.setProgress(mContrast / 5);
		if (mSkbHue != null) mSkbHue.setProgress(40 - (mHue / 5 + 20));
		if (mSkbHue != null) mSkbHue.setProgress(mHue / 5 + 20);
		if (mSkbSaturation != null) mSkbSaturation.setProgress(80 - (mSaturation / 5));
		if (mSkbSaturation != null) mSkbSaturation.setProgress(mSaturation / 5);
		if (mSkbKelvin != null) mSkbKelvin.setProgress(70 - mKelvin);
		if (mSkbKelvin != null) mSkbKelvin.setProgress(mKelvin);

		if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
			// 1から始まるので-1する
			if (mSkbRadius != null) mSkbRadius.setProgress(9);
			if (mSkbRadius != null) mSkbRadius.setProgress(mexternalfilterdata.mRadius - 1);
			// 0.5から0.1ステップで始まるので10倍して-5する
			if (mSkbSigmaspatial != null) mSkbSigmaspatial.setProgress(95);
			if (mSkbSigmaspatial != null) mSkbSigmaspatial.setProgress((int)(mexternalfilterdata.mSigma_spatial * 10 - 5));
			// 1から始まるので-1する
			if (mSkbSigmarange != null) mSkbSigmarange.setProgress(99);
			if (mSkbSigmarange != null) mSkbSigmarange.setProgress((int)(mexternalfilterdata.mSigma_range - 1));

			// 1から始まるので-1する
			if (mSkbGuidedr != null) mSkbGuidedr.setProgress(19);
			if (mSkbGuidedr != null) mSkbGuidedr.setProgress(mexternalfilterdata.mGuided_r - 1);
			if (mSkbGuidedeps != null) {
				// 目的のパラメータを探す
				int count = -1;
				for (int i = 0 ; i < mGuided_eps_data.length; i++) {
					if (mGuided_eps_data[i] == mexternalfilterdata.mGuided_eps) {
						count = i;
						break;
					}
				}
				if (count == -1) {
					// 一覧になかった場合は0.01に初期化
					count = SELECT_GUIDED_INIT;
				}
				mSkbGuidedeps.setProgress(mGuided_eps_data.length - 1);
				mSkbGuidedeps.setProgress(count);
			}

			// 1から始まるので-1する
			if (mSkbAditerations != null) mSkbAditerations.setProgress(19);
			if (mSkbAditerations != null) mSkbAditerations.setProgress(mexternalfilterdata.mAd_iterations - 1);
			// 1.0から0.5ステップで始まるので2倍して-2する
			if (mSkbAdk != null) mSkbAdk.setProgress(98);
			if (mSkbAdk != null) mSkbAdk.setProgress((int)(mexternalfilterdata.mAd_k * 2 - 2));
			// 0.01から0.01ステップで始まるので100倍して-1する
			if (mSkbAdlambda != null) mSkbAdlambda.setProgress(24);
			if (mSkbAdlambda != null) mSkbAdlambda.setProgress((int)(mexternalfilterdata.mAd_lambda * 100 - 1));

			// 5から2ステップで始まるので-5して2で割り算する
			if (mSkbNlmsearchwindow != null) mSkbNlmsearchwindow.setProgress(10);
			if (mSkbNlmsearchwindow != null) mSkbNlmsearchwindow.setProgress((mexternalfilterdata.mNlm_search_window - 5) / 2);
			// 3から2ステップで始まるので-3して2で割り算する
			if (mSkbNlmpatchsize != null) mSkbNlmpatchsize.setProgress(3);
			if (mSkbNlmpatchsize != null) mSkbNlmpatchsize.setProgress((mexternalfilterdata.mNlm_patch_size - 3) / 2);
			// 1.0から0.5ステップで始まるので2倍して-2する
			if (mSkbNlmh != null) mSkbNlmh.setProgress(58);
			if (mSkbNlmh != null) mSkbNlmh.setProgress((int)(mexternalfilterdata.mNlm_h * 2 - 2));

			if (mSkbWaveletthreshold != null) mSkbWaveletthreshold.setProgress(100);
			if (mSkbWaveletthreshold != null) mSkbWaveletthreshold.setProgress(mexternalfilterdata.mWavelet_threshold);

			// 0から0.01ステップで始まるので100倍する
			if (mSkbScurvecutoff != null) mSkbScurvecutoff.setProgress(100);
			if (mSkbScurvecutoff != null) mSkbScurvecutoff.setProgress((int)(mexternalfilterdata.mScurve_cutoff * 100));
			// 1.0から0.5ステップで始まるので2倍して-2する
			if (mSkbScurvegain != null) mSkbScurvegain.setProgress(58);
			if (mSkbScurvegain != null) mSkbScurvegain.setProgress((int)(mexternalfilterdata.mScurve_gain * 2 - 2));

			// 3から2ステップで始まるので-3して2で割り算する
			if (mSkbAdaptivewindowsize != null) mSkbAdaptivewindowsize.setProgress(48);
			if (mSkbAdaptivewindowsize != null) mSkbAdaptivewindowsize.setProgress((mexternalfilterdata.mAdaptive_window_size - 3) / 2);
			// -50から始まるので+50する
			if (mSkbAdaptivec != null) mSkbAdaptivec.setProgress(mexternalfilterdata.mAdaptive_c + 50);
		}

		if (mBtnAlgoMode != null) mBtnAlgoMode.setText(mAlgoModeItems[mAlgoMode]);
		// ボタンの文字を小文字対応にする(Lanczos3を表示させるため)
		if (mBtnAlgoMode != null) mBtnAlgoMode.setAllCaps(false);
		if (mBtnDispMode != null) mBtnDispMode.setText(mDispModeItems[mDispMode]);
		if (mBtnScaleMode != null) mBtnScaleMode.setText(mScaleModeItems[mScaleMode]);
		if (mBtnMgncut != null) mBtnMgncut.setText(mMgnCutItems[mMgnCut]);
		if (mBtnMgncutColor != null) mBtnMgncutColor.setText(mMgnCutColorItems[mMgnCutColor]);
		if (mBtnDisplayPosition != null) mBtnDisplayPosition.setText(mDisplayPositionItems[mDisplayPosition]);

		if (mBtnScrollDirection != null) mBtnScrollDirection.setText(mScrollDirectionItems[mScrollDirection]);

		if (mBtnAlgoMode != null) mBtnAlgoMode.setOnClickListener(this);
		if (mBtnDispMode != null) mBtnDispMode.setOnClickListener(this);
		if (mBtnScaleMode != null) mBtnScaleMode.setOnClickListener(this);
		if (mBtnMgncut != null) mBtnMgncut.setOnClickListener(this);
		if (mBtnMgncutColor != null) mBtnMgncutColor.setOnClickListener(this);
		if (mBtnDisplayPosition != null) mBtnDisplayPosition.setOnClickListener(this);
		if (mBtnScrollDirection != null) mBtnScrollDirection.setOnClickListener(this);

		if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
			if (mSpinnerExtFilter1 != null) mSpinnerExtFilter1.setSelection(mexternalfilterdata.mFilterStage1);
			if (mSpinnerExtFilter2 != null) mSpinnerExtFilter2.setSelection(mexternalfilterdata.mFilterStage2);
			if (mSpinnerExtFilter3 != null) mSpinnerExtFilter3.setSelection(mexternalfilterdata.mFilterStage3);
			if (mSpinnerExtFilter4 != null) mSpinnerExtFilter4.setSelection(mexternalfilterdata.mFilterStage4);
		}

		mBtnOK = (Button) mView.findViewById(R.id.btn_ok);
		mBtnApply = (Button) mView.findViewById(R.id.btn_apply);
		mBtnRevert = (Button) mView.findViewById(R.id.btn_revert);

		mBtnOK.setOnClickListener(this);
		mBtnApply.setOnClickListener(this);
		mBtnRevert.setOnClickListener(this);
		mBtnInit1.setOnClickListener(this);
		mBtnInit2.setOnClickListener(this);

		// ドロップダウンUI
		mSpinnerExtFilter1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// position が現在選択されたアイテムの位置(0始まり)
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// 何も選択されていない場合の処理
			}
		});
		mSpinnerExtFilter2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// position が現在選択されたアイテムの位置(0始まり)
				if (position == SELECT_FILTER_BILATERAL) {
					mSkbRadius.setVisibility(View.VISIBLE);
					mSkbSigmaspatial.setVisibility(View.VISIBLE);
					mSkbSigmarange.setVisibility(View.VISIBLE);
					mTxtRadius.setVisibility(View.VISIBLE);
					mTxtSigmaspatial.setVisibility(View.VISIBLE);
					mTxtSigmarange.setVisibility(View.VISIBLE);
				}
				else {
					mSkbRadius.setVisibility(View.GONE);
					mSkbSigmaspatial.setVisibility(View.GONE);
					mSkbSigmarange.setVisibility(View.GONE);
					mTxtRadius.setVisibility(View.GONE);
					mTxtSigmaspatial.setVisibility(View.GONE);
					mTxtSigmarange.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_GUIDED) {
					mSkbGuidedr.setVisibility(View.VISIBLE);
					mSkbGuidedeps.setVisibility(View.VISIBLE);
					mTxtGuidedr.setVisibility(View.VISIBLE);
					mTxtGuidedeps.setVisibility(View.VISIBLE);
				}
				else {
					mSkbGuidedr.setVisibility(View.GONE);
					mSkbGuidedeps.setVisibility(View.GONE);
					mTxtGuidedr.setVisibility(View.GONE);
					mTxtGuidedeps.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_ANISOTROPIC_DIFFUSION) {
					mSkbAditerations.setVisibility(View.VISIBLE);
					mSkbAdk.setVisibility(View.VISIBLE);
					mSkbAdlambda.setVisibility(View.VISIBLE);
					mTxtAditerations.setVisibility(View.VISIBLE);
					mTxtAdk.setVisibility(View.VISIBLE);
					mTxtAdlambda.setVisibility(View.VISIBLE);
				}
				else {
					mSkbAditerations.setVisibility(View.GONE);
					mSkbAdk.setVisibility(View.GONE);
					mSkbAdlambda.setVisibility(View.GONE);
					mTxtAditerations.setVisibility(View.GONE);
					mTxtAdk.setVisibility(View.GONE);
					mTxtAdlambda.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_NL_MEANS) {
					mSkbNlmsearchwindow.setVisibility(View.VISIBLE);
					mSkbNlmpatchsize.setVisibility(View.VISIBLE);
					mSkbNlmh.setVisibility(View.VISIBLE);
					mTxtNlmsearchwindow.setVisibility(View.VISIBLE);
					mTxtNlmpatchsize.setVisibility(View.VISIBLE);
					mTxtNlmh.setVisibility(View.VISIBLE);
				}
				else {
					mSkbNlmsearchwindow.setVisibility(View.GONE);
					mSkbNlmpatchsize.setVisibility(View.GONE);
					mSkbNlmh.setVisibility(View.GONE);
					mTxtNlmsearchwindow.setVisibility(View.GONE);
					mTxtNlmpatchsize.setVisibility(View.GONE);
					mTxtNlmh.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_WAVELET_THRESHOLD) {
					mSkbWaveletthreshold.setVisibility(View.VISIBLE);
					mTxtWaveletthreshold.setVisibility(View.VISIBLE);
				}
				else {
					mSkbWaveletthreshold.setVisibility(View.GONE);
					mTxtWaveletthreshold.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_BILATERAL || position == SELECT_FILTER_GUIDED || position == SELECT_FILTER_ANISOTROPIC_DIFFUSION || position == SELECT_FILTER_NL_MEANS || position == SELECT_FILTER_WAVELET_THRESHOLD) {
					mTxtInit1.setVisibility(View.VISIBLE);
					mBtnInit1.setVisibility(View.VISIBLE);
				}
				else {
					mTxtInit1.setVisibility(View.GONE);
					mBtnInit1.setVisibility(View.GONE);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// 何も選択されていない場合の処理
			}
		});
		mSpinnerExtFilter3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// position が現在選択されたアイテムの位置(0始まり)
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// 何も選択されていない場合の処理
			}
		});
		mSpinnerExtFilter4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// position が現在選択されたアイテムの位置(0始まり)
				if (position == SELECT_FILTER_S_CURVE) {
					mSkbScurvegain.setVisibility(View.VISIBLE);
					mSkbScurvecutoff.setVisibility(View.VISIBLE);
					mTxtScurvegain.setVisibility(View.VISIBLE);
					mTxtScurvecutoff.setVisibility(View.VISIBLE);
				}
				else {
					mSkbScurvegain.setVisibility(View.GONE);
					mSkbScurvecutoff.setVisibility(View.GONE);
					mTxtScurvegain.setVisibility(View.GONE);
					mTxtScurvecutoff.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_ADAPTIVE_THRESHOLD) {
					mSkbAdaptivewindowsize.setVisibility(View.VISIBLE);
					mSkbAdaptivec.setVisibility(View.VISIBLE);
					mTxtAdaptivewindowsize.setVisibility(View.VISIBLE);
					mTxtAdaptivec.setVisibility(View.VISIBLE);
				}
				else {
					mSkbAdaptivewindowsize.setVisibility(View.GONE);
					mSkbAdaptivec.setVisibility(View.GONE);
					mTxtAdaptivewindowsize.setVisibility(View.GONE);
					mTxtAdaptivec.setVisibility(View.GONE);
				}
				if (position == SELECT_FILTER_S_CURVE || position == SELECT_FILTER_ADAPTIVE_THRESHOLD) {
					mTxtInit2.setVisibility(View.VISIBLE);
					mBtnInit2.setVisibility(View.VISIBLE);
				}
				else {
					mTxtInit2.setVisibility(View.GONE);
					mBtnInit2.setVisibility(View.GONE);
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// 何も選択されていない場合の処理
			}
		});

		return mView;
	}

	// 拡張フィルターの表示更新
	private void setFilterView() {
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSpinnerExtFilter1.setVisibility(View.GONE);
			mSpinnerExtFilter2.setVisibility(View.GONE);
			mSpinnerExtFilter3.setVisibility(View.GONE);
			mSpinnerExtFilter4.setVisibility(View.GONE);
			mTxtExtFilter1.setVisibility(View.GONE);
			mTxtExtFilter2.setVisibility(View.GONE);
			mTxtExtFilter3.setVisibility(View.GONE);
			mTxtExtFilter4.setVisibility(View.GONE);
			mRadioButtonPre.setVisibility(View.GONE);
			mRadioButtonPost.setVisibility(View.GONE);
			mTxtInit1.setVisibility(View.GONE);
			mBtnInit1.setVisibility(View.GONE);
			mTxtInit2.setVisibility(View.GONE);
			mBtnInit2.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mCheckExtFilter) {
			// チェックされた時の処理
			mSpinnerExtFilter1.setVisibility(View.VISIBLE);
			mSpinnerExtFilter2.setVisibility(View.VISIBLE);
			mSpinnerExtFilter3.setVisibility(View.VISIBLE);
			mSpinnerExtFilter4.setVisibility(View.VISIBLE);
			mTxtExtFilter1.setVisibility(View.VISIBLE);
			mTxtExtFilter2.setVisibility(View.VISIBLE);
			mTxtExtFilter3.setVisibility(View.VISIBLE);
			mTxtExtFilter4.setVisibility(View.VISIBLE);
			mRadioButtonPre.setVisibility(View.VISIBLE);
			mRadioButtonPost.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbRadius.setVisibility(View.GONE);
			mSkbSigmaspatial.setVisibility(View.GONE);
			mSkbSigmarange.setVisibility(View.GONE);
			mTxtRadius.setVisibility(View.GONE);
			mTxtSigmaspatial.setVisibility(View.GONE);
			mTxtSigmarange.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage2 == SELECT_FILTER_BILATERAL) {
			// チェックされた時の処理
			mSkbRadius.setVisibility(View.VISIBLE);
			mSkbSigmaspatial.setVisibility(View.VISIBLE);
			mSkbSigmarange.setVisibility(View.VISIBLE);
			mTxtRadius.setVisibility(View.VISIBLE);
			mTxtSigmaspatial.setVisibility(View.VISIBLE);
			mTxtSigmarange.setVisibility(View.VISIBLE);
			mTxtInit1.setVisibility(View.VISIBLE);
			mBtnInit1.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbGuidedr.setVisibility(View.GONE);
			mSkbGuidedeps.setVisibility(View.GONE);
			mTxtGuidedr.setVisibility(View.GONE);
			mTxtGuidedeps.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage2 == SELECT_FILTER_GUIDED) {
			// チェックされた時の処理
			mSkbGuidedr.setVisibility(View.VISIBLE);
			mSkbGuidedeps.setVisibility(View.VISIBLE);
			mTxtGuidedr.setVisibility(View.VISIBLE);
			mTxtGuidedeps.setVisibility(View.VISIBLE);
			mTxtInit1.setVisibility(View.VISIBLE);
			mBtnInit1.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbAditerations.setVisibility(View.GONE);
			mSkbAdk.setVisibility(View.GONE);
			mSkbAdlambda.setVisibility(View.GONE);
			mTxtAditerations.setVisibility(View.GONE);
			mTxtAdk.setVisibility(View.GONE);
			mTxtAdlambda.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage2 == SELECT_FILTER_ANISOTROPIC_DIFFUSION) {
			// チェックされた時の処理
			mSkbAditerations.setVisibility(View.VISIBLE);
			mSkbAdk.setVisibility(View.VISIBLE);
			mSkbAdlambda.setVisibility(View.VISIBLE);
			mTxtAditerations.setVisibility(View.VISIBLE);
			mTxtAdk.setVisibility(View.VISIBLE);
			mTxtAdlambda.setVisibility(View.VISIBLE);
			mTxtInit1.setVisibility(View.VISIBLE);
			mBtnInit1.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbNlmsearchwindow.setVisibility(View.GONE);
			mSkbNlmpatchsize.setVisibility(View.GONE);
			mSkbNlmh.setVisibility(View.GONE);
			mTxtNlmsearchwindow.setVisibility(View.GONE);
			mTxtNlmpatchsize.setVisibility(View.GONE);
			mTxtNlmh.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage2 == SELECT_FILTER_NL_MEANS) {
			// チェックされた時の処理
			mSkbNlmsearchwindow.setVisibility(View.VISIBLE);
			mSkbNlmpatchsize.setVisibility(View.VISIBLE);
			mSkbNlmh.setVisibility(View.VISIBLE);
			mTxtNlmsearchwindow.setVisibility(View.VISIBLE);
			mTxtNlmpatchsize.setVisibility(View.VISIBLE);
			mTxtNlmh.setVisibility(View.VISIBLE);
			mTxtInit1.setVisibility(View.VISIBLE);
			mBtnInit1.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbWaveletthreshold.setVisibility(View.GONE);
			mTxtWaveletthreshold.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage2 == SELECT_FILTER_WAVELET_THRESHOLD) {
			// チェックされた時の処理
			mSkbWaveletthreshold.setVisibility(View.VISIBLE);
			mTxtWaveletthreshold.setVisibility(View.VISIBLE);
			mTxtInit1.setVisibility(View.VISIBLE);
			mBtnInit1.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbScurvegain.setVisibility(View.GONE);
			mSkbScurvecutoff.setVisibility(View.GONE);
			mTxtScurvegain.setVisibility(View.GONE);
			mTxtScurvecutoff.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage4 == SELECT_FILTER_S_CURVE) {
			// チェックされた時の処理
			mSkbScurvegain.setVisibility(View.VISIBLE);
			mSkbScurvecutoff.setVisibility(View.VISIBLE);
			mTxtScurvegain.setVisibility(View.VISIBLE);
			mTxtScurvecutoff.setVisibility(View.VISIBLE);
			mTxtInit2.setVisibility(View.VISIBLE);
			mBtnInit2.setVisibility(View.VISIBLE);
		}
		if (mexternalfilterdata == null || !mexternalfilterdata.mCheckExtFilter) {
			// チェックが外れた時の処理
			mSkbAdaptivewindowsize.setVisibility(View.GONE);
			mSkbAdaptivec.setVisibility(View.GONE);
			mTxtAdaptivewindowsize.setVisibility(View.GONE);
			mTxtAdaptivec.setVisibility(View.GONE);
		}
		else if ((mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) && mexternalfilterdata.mFilterStage4 == SELECT_FILTER_ADAPTIVE_THRESHOLD) {
			// チェックされた時の処理
			mSkbAdaptivewindowsize.setVisibility(View.VISIBLE);
			mSkbAdaptivec.setVisibility(View.VISIBLE);
			mTxtAdaptivewindowsize.setVisibility(View.VISIBLE);
			mTxtAdaptivec.setVisibility(View.VISIBLE);
			mTxtInit2.setVisibility(View.VISIBLE);
			mBtnInit2.setVisibility(View.VISIBLE);
		}
	}

	public void setImageConfigListner(ImageConfigListenerInterface listener) {
		mListener = listener;
	}

	public interface ImageConfigListenerInterface extends EventListener {

	    // メニュー選択された
	    public void onButtonSelect(int select, boolean gray, boolean invert, boolean moire, boolean topsingle, int sharpen, int bright, int gamma, int bklight, int algomode, int dispmode, int scalemode, int mgncut, int mgncutcolor, boolean issave, int displayposition, int contrast, int hue, int saturation, boolean coloring, int scrolldirection, int kelvin, boolean chkrgblevel, int redrevel, int greenlevel, int bluerevel, ExternalFilterData externalfilterdata);
	    public void onClose();
	}

	private void showSelectList(int index) {
		if (mListDialog != null) {
			return;
		}

		// 選択対象
		mSelectMode = index;

		// 選択肢を設定
		String[] items = null;

		String title;
		int selIndex;
		switch (index) {
			case SELLIST_ALGORITHM:
				// 画像補間法の選択肢設定
				title = mAlgoModeTitle;
				items = mAlgoModeItems;
				selIndex = mAlgoModeTemp;
				break;
			case SELLIST_VIEW_MODE:
				// 見開きモードの選択肢設定
				title = mDispModeTitle;
				items = mDispModeItems;
				selIndex = mDispModeTemp;
				break;
			case SELLIST_SCALE_MODE:
				// サイズ設定の選択肢設定
				title = mScaleModeTitle;
				items = mScaleModeItems;
				selIndex = mScaleModeTemp;
				break;
			case SELLIST_MARGIN_CUT:
				// 余白削除
				title = mMgnCutTitle;
				items = mMgnCutItems;
				selIndex = mMgnCutTemp;
				break;
			case SELLIST_MARGIN_CUTCOLOR:
				// 余白削除
				title = mMgnCutColorTitle;
				items = mMgnCutColorItems;
				selIndex = mMgnCutColorTemp;
				break;
			case SELLIST_DISPLAY_POSITION:
				// 画面の表示位置
				title = mDisplayOpsitionTitle;
				items = mDisplayPositionItems;
				selIndex = mDisplayPositionTemp;
				break;
			case SELLIST_SCROLL_DIRECTION:
				// スクロール操作によるページ移動の縦横選択の設定
				title = mScrollDirectionTitle;
				items = mScrollDirectionItems;
				selIndex = mScrollDirectionTemp;
				break;
			default:
				return;
		}
		mListDialog = new ListDialog(mActivity, R.style.MyDialog, title, items, selIndex, new ListSelectListener() {
			@Override
			public void onSelectItem(int index) {
				switch (mSelectMode) {
					case SELLIST_ALGORITHM:
						// 画像補間法
						mAlgoModeTemp = index;
						mBtnAlgoMode.setText(mAlgoModeItems[index]);
						break;
					case SELLIST_VIEW_MODE:
						// 見開き設定変更
						mDispModeTemp = index;
						mBtnDispMode.setText(mDispModeItems[index]);
						break;
					case SELLIST_SCALE_MODE: {
						// 画像拡大率の変更
						mScaleModeTemp = index;
						mBtnScaleMode.setText(mScaleModeItems[index]);
						break;
					}
					case SELLIST_MARGIN_CUT:
						// 余白削除
						mMgnCutTemp = index;
						mBtnMgncut.setText(mMgnCutItems[index]);
						break;
					case SELLIST_MARGIN_CUTCOLOR:
						// 余白削除
						mMgnCutColorTemp = index;
						mBtnMgncutColor.setText(mMgnCutColorItems[index]);
						break;
					case SELLIST_DISPLAY_POSITION:
						// 画面の表示位置
						mDisplayPositionTemp = index;
						mBtnDisplayPosition.setText(mDisplayPositionItems[index]);
					case SELLIST_SCROLL_DIRECTION:
						// スクロール操作によるページ移動の縦横選択の設定
						mScrollDirectionTemp = index;
						mBtnScrollDirection.setText(mScrollDirectionItems[index]);
						break;
				}
			}

			@Override
			public void onClose() {
				// 終了
				mListDialog = null;
			}
		});
		mListDialog.show();
		return;
	}

	@Override
	public void onClick(View v) {
		if (mBtnAlgoMode == v) {
			// 画像補間法
			showSelectList(SELLIST_ALGORITHM);
			return;
		}
		if (mBtnDispMode == v) {
			// 画像補間法
			showSelectList(SELLIST_VIEW_MODE);
			return;
		}
		if (mBtnScaleMode == v) {
			// 画像補間法
			showSelectList(SELLIST_SCALE_MODE);
			return;
		}
		if (mBtnMgncut == v) {
			// 画像補間法
			showSelectList(SELLIST_MARGIN_CUT);
			return;
		}
		if (mBtnMgncutColor == v) {
			// 画像補間法
			showSelectList(SELLIST_MARGIN_CUTCOLOR);
			return;
		}
		if (mBtnDisplayPosition == v) {
			// 画面の表示位置
			showSelectList(SELLIST_DISPLAY_POSITION);
			return;
		}
		if (mBtnScrollDirection == v) {
			// スクロール操作によるページ移動の縦横選択の設定
			showSelectList(SELLIST_SCROLL_DIRECTION);
			return;
		}

		int select = CLICK_REVERT;

		// ボタンクリック
		if (mBtnOK == v) {
			select = CLICK_OK;
		}
		else if (mBtnApply == v) {
			select = CLICK_APPLY;
		}
		else if (mBtnInit1 == v) {
			// 初期値をセット
			switch (mSpinnerExtFilter2.getSelectedItemPosition()) {
				case SELECT_FILTER_BILATERAL:
					mexternalfilterdata.mRadius = DEF.RADIUS;
					mSkbRadius.setProgress(mexternalfilterdata.mRadius - 1);
					mexternalfilterdata.mSigma_spatial = DEF.SIGMA_SPARTIAL;
					mSkbSigmaspatial.setProgress((int)(mexternalfilterdata.mSigma_spatial * 10 - 5));
					mexternalfilterdata.mSigma_range = DEF.SIGMA_RANGE;
					mSkbSigmarange.setProgress((int)(mexternalfilterdata.mSigma_range - 1));
					break;
				case SELECT_FILTER_GUIDED:
					mexternalfilterdata.mGuided_r = DEF.GUIDED_R;
					mSkbGuidedr.setProgress(mexternalfilterdata.mGuided_r - 1);
					mexternalfilterdata.mGuided_eps = DEF.GUIDED_EQS;
					// 目的のパラメータを探す
					int count = -1;
					for (int i = 0 ; i < mGuided_eps_data.length; i++) {
						if (mGuided_eps_data[i] == mexternalfilterdata.mGuided_eps) {
							count = i;
							break;
						}
					}
					if (count == -1) {
						// 一覧になかった場合は0.01に初期化
						count = SELECT_GUIDED_INIT;
					}
					mSkbGuidedeps.setProgress(count);
					break;
				case SELECT_FILTER_ANISOTROPIC_DIFFUSION:
					mexternalfilterdata.mAd_iterations = DEF.AD_ITERATIONS;
					mSkbAditerations.setProgress(mexternalfilterdata.mAd_iterations - 1);
					mexternalfilterdata.mAd_k = DEF.AD_K;
					mSkbAdk.setProgress((int)(mexternalfilterdata.mAd_k * 2 - 2));
					mexternalfilterdata.mAd_lambda = DEF.AD_LAMBDA;
					mSkbAdlambda.setProgress((int)(mexternalfilterdata.mAd_lambda * 100 - 1));
					break;
				case SELECT_FILTER_NL_MEANS:
					mexternalfilterdata.mNlm_search_window = DEF.NLM_SEARCH_WINDOW;
					mSkbNlmsearchwindow.setProgress((mexternalfilterdata.mNlm_search_window - 5) / 2);
					mexternalfilterdata.mNlm_patch_size = DEF.NLM_PATCH_SIZE;
					mSkbNlmpatchsize.setProgress((mexternalfilterdata.mNlm_patch_size - 3) / 2);
					mexternalfilterdata.mNlm_h = DEF.NLM_H;
					mSkbNlmh.setProgress((int)(mexternalfilterdata.mNlm_h * 2 - 2));
					break;
				case SELECT_FILTER_WAVELET_THRESHOLD:
					mexternalfilterdata.mWavelet_threshold = DEF.WAVELET_THRESHOLD;
					mSkbWaveletthreshold.setProgress(mexternalfilterdata.mWavelet_threshold);
					break;
			}
			return;
		}
		else if (mBtnInit2 == v) {
			// 初期値をセット
			switch (mSpinnerExtFilter4.getSelectedItemPosition()) {
				case SELECT_FILTER_S_CURVE:
					mexternalfilterdata.mScurve_cutoff = DEF.SCURVE_CUTOFF;
					mSkbScurvecutoff.setProgress((int)(mexternalfilterdata.mScurve_cutoff * 100));
					mexternalfilterdata.mScurve_gain = DEF.SCURVE_GAIN;
					mSkbScurvegain.setProgress((int)(mexternalfilterdata.mScurve_gain * 2 - 2));
					break;
				case SELECT_FILTER_ADAPTIVE_THRESHOLD:
					mexternalfilterdata.mAdaptive_window_size = DEF.ADAPTIVE_WINDOW_SIZE;
					mSkbAdaptivewindowsize.setProgress((mexternalfilterdata.mAdaptive_window_size - 3) / 2);
					mexternalfilterdata.mAdaptive_c = DEF.ADAPTIVE_C;
					mSkbAdaptivec.setProgress(mexternalfilterdata.mAdaptive_c + 50);
					break;
			}
			return;
		}

		if (select == CLICK_REVERT) {
			// 戻すは元の値を通知
			mListener.onButtonSelect(select, mGray, mInvert, mMoire, mTopSingle, mSharpen, mBright, mGamma, mBkLight, mAlgoMode, mDispMode, mScaleMode, mMgnCut, mMgnCutColor, mIsSave, mDisplayPosition, mContrast, mHue, mSaturation, mColoring, mScrollDirection, mKelvin, mCheckRgbLevel, mRedLevelBackup, mGreenLevelBackup, mBlueLevelBackup, mExternalFilterData);
		}
		else {
			// OK/適用は設定された値を通知
			boolean gray = mChkGray.isChecked();
			boolean coloring = mChkColoring.isChecked();
			boolean invert = mChkInvert.isChecked();
			boolean moire = mChkMoire.isChecked();
			boolean topsingle = mChkTopSingle.isChecked();
			boolean issave = mChkIsSave.isChecked();
			int sharpen = mSkbSharpen.getProgress();
			int bright = mSkbBright.getProgress() - 5;
			int gamma = mSkbGamma.getProgress() - 5;
			int bklight = mSkbBkLight.getProgress();
			int contrast = mSkbContrast.getProgress() * 5;
			int hue = (mSkbHue.getProgress() - 20) * 5;
			int saturation = mSkbSaturation.getProgress() * 5;
			int kelvin = mSkbKelvin.getProgress();
			boolean chkrgblevel = mChkRgbLevel.isChecked();
			int redrevel = mSkbRedLevel.getProgress();
			int greenlevel = mSkbGreenLevel.getProgress();
			int bluerevel = mSkbBlueLevel.getProgress();
			if (chkrgblevel) {
				mRedLevel = redrevel;
				mGreenLevel = greenlevel;
				mBlueLevel = bluerevel;
			}
			if (mCommandId == DEF.MENU_IMGCONF || mCommandId == DEF.MENU_EXT_FILTER) {
				// 拡張フィルターの値を通知
				mexternalfilterdatacopy.mFilterStage1 = mSpinnerExtFilter1.getSelectedItemPosition();
				mexternalfilterdatacopy.mFilterStage2 = mSpinnerExtFilter2.getSelectedItemPosition();
				mexternalfilterdatacopy.mFilterStage3 = mSpinnerExtFilter3.getSelectedItemPosition();
				mexternalfilterdatacopy.mFilterStage4 = mSpinnerExtFilter4.getSelectedItemPosition();
				mexternalfilterdatacopy.mCheckExtFilter = mChkExtFilter.isChecked();
				mexternalfilterdatacopy.mRadioFilter = (mRadioButtonPost.isChecked()) ? SELECT_FILTER_POST : SELECT_FILTER_PRE;
				// 1から始まるので+1する
				mexternalfilterdatacopy.mRadius = mSkbRadius.getProgress() + 1;
				// 0.5から0.1ステップで始まるので10で割り算して0.5を加算する
				mexternalfilterdatacopy.mSigma_spatial = ((float)mSkbSigmaspatial.getProgress()) / 10 + 0.5f;
				// 1から始まるので+1する
				mexternalfilterdatacopy.mSigma_range = (float)(mSkbSigmarange.getProgress() + 1);
				// 1から始まるので+1する
				mexternalfilterdatacopy.mGuided_r = mSkbGuidedr.getProgress() + 1;
				// テーブルからデータを取り出す
				mexternalfilterdatacopy.mGuided_eps = (float)(mGuided_eps_data[mSkbGuidedeps.getProgress()]);
				// 1から始まるので+1する
				mexternalfilterdatacopy.mAd_iterations = mSkbAditerations.getProgress() + 1;
				// 1.0から0.5ステップで始まるので2で割り算して1.0を加算する
				mexternalfilterdatacopy.mAd_k = ((float)mSkbAdk.getProgress()) / 2 + 1.0f;
				// 0.01から0.01ステップで始まるので100で割り算して0.01を加算する
				mexternalfilterdatacopy.mAd_lambda = ((float)mSkbAdlambda.getProgress()) / 100 + 0.01f;
				// 5から2ステップで始まるので2倍して5を加算する
				mexternalfilterdatacopy.mNlm_search_window = mSkbNlmsearchwindow.getProgress() * 2 + 5;
				// 3から2ステップで始まるので2倍して3を加算する
				mexternalfilterdatacopy.mNlm_patch_size = mSkbNlmpatchsize.getProgress() * 2 + 3;
				// 1.0から0.5ステップで始まるので2で割り算して1.0を加算する
				mexternalfilterdatacopy.mNlm_h = ((float)mSkbNlmh.getProgress()) / 2 + 1.0f;
				mexternalfilterdatacopy.mWavelet_threshold = mSkbWaveletthreshold.getProgress();
				// 0から0.01ステップで始まるので100で割り算する
				mexternalfilterdatacopy.mScurve_cutoff = ((float)mSkbScurvecutoff.getProgress()) / 100;
				// 1.0から0.5ステップで始まるので2で割り算して1.0を加算する
				mexternalfilterdatacopy.mScurve_gain = ((float)mSkbScurvegain.getProgress()) / 2 + 1.0f;
				// 3から2ステップで始まるので2倍して3を加算する
				mexternalfilterdatacopy.mAdaptive_window_size = mSkbAdaptivewindowsize.getProgress() * 2 + 3;
				// -50から始まるので-50する
				mexternalfilterdatacopy.mAdaptive_c = mSkbAdaptivec.getProgress() - 50;
			}

			mListener.onButtonSelect(select, gray, invert, moire, topsingle, sharpen, bright, gamma, bklight, mAlgoModeTemp, mDispModeTemp, mScaleModeTemp, mMgnCutTemp, mMgnCutColorTemp, issave, mDisplayPositionTemp, contrast, hue, saturation, coloring, mScrollDirectionTemp, kelvin, chkrgblevel, redrevel, greenlevel, bluerevel, mexternalfilterdatacopy);
		}

		if (select != CLICK_APPLY) {
			// 適用以外では閉じる
			this.dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		mListener.onClose();
		super.dismiss();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// 変更通知
		if (seekBar == mSkbSharpen) {
			String str = getSharpenStr(getContext(), progress);
			mTxtSharpen.setText(mSharpenStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbBkLight) {
			String str = getBkLight(progress);
			mTxtBkLight.setText(mBkLightStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbBright) {
			String str = getBrightGammaStr(getContext(), progress);
			mTxtBright.setText(mBrightStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbGamma) {
			String str = getBrightGammaStr(getContext(), progress);
			mTxtGamma.setText(mGammaStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbContrast) {
			String str = getContrastStr(progress);
			mTxtContrast.setText(mContrastStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbHue) {
			String str = getHueStr(getContext(), progress);
			mTxtHue.setText(mHueStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbSaturation) {
			String str = getSaturationStr(progress);
			mTxtSaturation.setText(mSaturationStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbKelvin) {
			String str = getKelvinStr(getContext(), progress);
			mTxtKelvin.setText(mKelvinStr.replaceAll("%", str));
			mKelvinRgb = ImageManager.getRGBFromKelvin(progress);
			if (!mChkRgbLevel.isChecked()) {
				str = getRgbLevelStr(mKelvinRgb[0]);
				mTxtRedLevel.setText(mRedLevelStr.replaceAll("%", str));
				mSkbRedLevel.setProgress(mKelvinRgb[0]);
				str = getRgbLevelStr(mKelvinRgb[1]);
				mTxtGreenLevel.setText(mGreenLevelStr.replaceAll("%", str));
				mSkbGreenLevel.setProgress(mKelvinRgb[1]);
				str = getRgbLevelStr(mKelvinRgb[2]);
				mTxtBlueLevel.setText(mBlueLevelStr.replaceAll("%", str));
				mSkbBlueLevel.setProgress(mKelvinRgb[2]);
			}
		}
		else if (seekBar == mSkbRedLevel) {
			String str = getRgbLevelStr(progress);
			mTxtRedLevel.setText(mRedLevelStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbGreenLevel) {
			String str = getRgbLevelStr(progress);
			mTxtGreenLevel.setText(mGreenLevelStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbBlueLevel) {
			String str = getRgbLevelStr(progress);
			mTxtBlueLevel.setText(mBlueLevelStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbRadius) {
			String str = getRadiusStr(progress);
			mTxtRadius.setText(mRadiusStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbSigmaspatial) {
			String str = getSigmaspatialStr(progress);
			mTxtSigmaspatial.setText(mSigmaspatialStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbSigmarange) {
			String str = getSigmarangeStr(progress);
			mTxtSigmarange.setText(mSigmarangeStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbGuidedr) {
			String str = getGuidedrStr(progress);
			mTxtGuidedr.setText(mGuidedrStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbGuidedeps) {
			String str = getGuidedepsStr(progress);
			mTxtGuidedeps.setText(mGuidedepsStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbAditerations) {
			String str = getAditerationsStr(progress);
			mTxtAditerations.setText(mAditerationsStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbAdk) {
			String str = getAdkStr(progress);
			mTxtAdk.setText(mAdkStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbAdlambda) {
			String str = getAdlambdaStr(progress);
			mTxtAdlambda.setText(mAdlambdaStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbNlmsearchwindow) {
			String str = getNlmsearchwindowStr(progress);
			mTxtNlmsearchwindow.setText(mNlmsearchwindowStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbNlmpatchsize) {
			String str = getNlmpatchsizeStr(progress);
			mTxtNlmpatchsize.setText(mNlmpatchsizeStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbNlmh) {
			String str = getNlmhStr(progress);
			mTxtNlmh.setText(mNlmhStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbWaveletthreshold) {
			String str = getWaveletthresholdStr(progress);
			mTxtWaveletthreshold.setText(mWaveletthresholdStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbScurvegain) {
			String str = getScurvegainStr(progress);
			mTxtScurvegain.setText(mScurvegainStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbScurvecutoff) {
			String str = getScurvecutoffStr(progress);
			mTxtScurvecutoff.setText(mScurvecutoffStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbAdaptivewindowsize) {
			String str = getAdaptivewindowsizeStr(progress);
			mTxtAdaptivewindowsize.setText(mAdaptivewindowsizeStr.replaceAll("%", str));
		}
		else if (seekBar == mSkbAdaptivec) {
			String str = getAdaptivecStr(progress);
			mTxtAdaptivec.setText(mAdaptivecStr.replaceAll("%", str));
		}
		else {
		}
	}

	public static String getSharpenStr(Context context, int progress) {
		String str;
		if (progress == 0) {
			Resources res = context.getResources();
			str = res.getString(R.string.none);
		}
		else if (progress < 16) {
			str = String.valueOf(progress % 16) + "/16";
		}
		else if (progress % 16 == 0) {
			str = String.valueOf(progress / 16);
		}
		else {
			str =  String.valueOf(progress / 16) + " + " + String.valueOf(progress % 16) + "/16";
		}
		return str;
	}

	private String getBkLight(int progress) {
		String str;
		if (progress >= 11) {
			str = mAutoStr;
		}
		else {
			str = String.valueOf(progress * 10) + "%";
		}
		return str;
	}

	public static String getBrightGammaStr(Context context, int progress) {
		String str;
		if (progress == 5) {
			Resources res = context.getResources();
			str = res.getString(R.string.none);
		}
		else if (progress < 5) {
			str = String.valueOf(progress - 5);
		}
		else {
			str = "+" + String.valueOf(progress - 5);
		}
		return str;
	}

	public static String getContrastStr(int progress) {
		String str;
		str = String.valueOf(progress * 5) + "%";
		return str;
	}

	public static String getHueStr(Context context, int progress) {
		Resources res = context.getResources();
		String str;
		str = String.valueOf((progress - 20) * 5) + res.getString(R.string.degree);
		return str;
	}

	public static String getSaturationStr(int progress) {
		String str;
		str = String.valueOf(progress * 5) + "%";
		return str;
	}

	public static String getKelvinStr(Context context, int progress) {
		Resources res = context.getResources();
		String str;
		str = String.valueOf(progress * 100 + 3000) + "K";
		if (progress == 35) {
			str += " : " + res.getString(R.string.kelvin00);
		}
		else if (progress <= 4) {
			str += " : " + res.getString(R.string.kelvin01);
		}
		else if (progress > 4 && progress <= 13) {
			str += " : " + res.getString(R.string.kelvin02);
		}
		else if (progress > 13 && progress <= 24) {
			str += " : " + res.getString(R.string.kelvin03);
		}
		else if (progress > 24 && progress <= 38) {
			str += " : " + res.getString(R.string.kelvin04);
		}
		else if (progress > 38 && progress <= 55) {
			str += " : " + res.getString(R.string.kelvin05);
		}
		else if (progress > 55) {
			str += " : " + res.getString(R.string.kelvin06);
		}
		return str;
	}

	public static String getRgbLevelStr(int progress) {
		String str;
		str = String.valueOf(progress) + "%";
		return str;
	}

	public static String getRadiusStr(int progress) {
		String str;
		// 1から始まるので+1する
		str = String.valueOf(progress + 1);
		return str;
	}
	public static String getSigmaspatialStr(int progress) {
		String str;
		// 0.5から0.1ステップ
		str = String.valueOf(((float)progress + 5) / 10);
		return str;
	}
	public static String getSigmarangeStr(int progress) {
		String str;
		// 1から始まるので+1する
		str = String.valueOf(progress + 1);
		return str;
	}
	public static String getGuidedrStr(int progress) {
		String str;
		// 1から始まるので+1する
		str = String.valueOf(progress + 1);
		return str;
	}
	public static String getGuidedepsStr(int progress) {
		String str;
		str = String.valueOf(mGuided_eps_data[progress]);
		return str;
	}
	public static String getAditerationsStr(int progress) {
		String str;
		// 1から始まるので+1する
		str = String.valueOf(progress + 1);
		return str;
	}
	public static String getAdkStr(int progress) {
		String str;
		// 1.0から0.5ステップ
		str = String.valueOf(((float)progress + 2) / 2);
		return str;
	}
	public static String getAdlambdaStr(int progress) {
		String str;
		// 0.01から0.01ステップ
		str = String.valueOf(((float)progress + 1) / 100);
		return str;
	}
	public static String getNlmsearchwindowStr(int progress) {
		String str;
		// 5から2ステップ
		str = String.valueOf(progress * 2 + 5);
		return str;
	}
	public static String getNlmpatchsizeStr(int progress) {
		String str;
		// 3から2ステップ
		str = String.valueOf(progress * 2 + 3);
		return str;
	}
	public static String getNlmhStr(int progress) {
		String str;
		// 1.0から0.5ステップ
		str = String.valueOf(((float)progress) / 2 + 1.0f);
		return str;
	}
	public static String getWaveletthresholdStr(int progress) {
		String str;
		str = String.valueOf(progress);
		return str;
	}
	public static String getScurvegainStr(int progress) {
		String str;
		// 1.0から0.5ステップ
		str = String.valueOf(((float)progress) / 2 + 1.0f);
		return str;
	}
	public static String getScurvecutoffStr(int progress) {
		String str;
		// 0.01ステップ
		str = String.valueOf(((float)progress) / 100);
		return str;
	}
	public static String getAdaptivewindowsizeStr(int progress) {
		String str;
		// 3から2ステップ
		str = String.valueOf(progress * 2 + 3);
		return str;
	}
	public static String getAdaptivecStr(int progress) {
		String str;
		// -50から1ステップ
		str = String.valueOf(progress - 50);
		return str;
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング開始
		return;
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// シークバーのトラッキング終了
		return;
	}
}