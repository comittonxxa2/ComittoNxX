package src.comitton.common;

// equals()をオーバーライド
import lombok.Data;
// 拡張フィルターのパラメータの受け渡しが面倒なので作成した
@Data
public class ExternalFilterData implements Cloneable {

	public boolean mCheckExtFilter;
	public int mRadioFilter;

	public int mFilterStage1;
	public int mFilterStage2;
	public int mFilterStage3;
	public int mFilterStage4;

	// パラメータ
	public int mRadius;
	public float mSigma_spatial;
	public float mSigma_range;

	public int mGuided_r;
	public float mGuided_eps;

	public int mAd_iterations;
	public float mAd_k;
	public float mAd_lambda;

	// NL-Means パラメータ
	// 探索窓サイズ
	public int mNlm_search_window;
	// 類似度比較パッチサイズ
	public int mNlm_patch_size;	
	// フィルター強度パラメータ
	public float mNlm_h;

	// Wavelet パラメータ
	// しきい値
	public int mWavelet_threshold;

	public float mScurve_gain;
	public float mScurve_cutoff;

	public int mAdaptive_window_size;
	public int mAdaptive_c;

	public int mVersion;

	// classの複製をオーバーライド
	@Override
	public ExternalFilterData clone() {
		try {
			return (ExternalFilterData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError();
		}
	}
}
