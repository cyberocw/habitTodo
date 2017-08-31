 package com.cyberocw.habittodosecretary;

 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.design.widget.NavigationView;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.DrawerLayout;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.AppCompatActivity;

 import com.crashlytics.android.Crashlytics;
 import com.cyberocw.habittodosecretary.alaram.AlarmFragment;
 import com.cyberocw.habittodosecretary.category.CategoryFragment;
 import com.cyberocw.habittodosecretary.common.vo.FileVO;
 import com.cyberocw.habittodosecretary.file.AttachmentTask;
 import com.cyberocw.habittodosecretary.file.FileHelper;
 import com.cyberocw.habittodosecretary.file.OnAttachingFileListener;
 import com.cyberocw.habittodosecretary.memo.MemoFragment;
 import com.cyberocw.habittodosecretary.memo.vo.MemoVO;
 import com.cyberocw.habittodosecretary.util.CommonUtils;

 import java.util.ArrayList;

 import io.fabric.sdk.android.Fabric;

 public class ShareActivity extends AppCompatActivity implements AlarmFragment.OnFragmentInteractionListener, CategoryFragment.OnFragmentInteractionListener, MemoFragment.OnFragmentInteractionListener, OnAttachingFileListener {
	public AlarmFragment mMainFragment;
	public static String TAG = "mainActivity";

	private NavigationView mNavigationView;
	private DrawerLayout mDrawer;
	private Fragment mFragment;
	private ActionBar actionBar;
	 public static Context mCtx;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCtx = getApplicationContext();

		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.activity_main);

		//MemoDialogNew dialogNew = new MemoDialogNew();
		Bundle bundle = new Bundle();

		MemoVO memoVO = new MemoVO();

		Intent intent = getIntent();
		String action = intent.getAction();
		String type = intent.getType();
		if ("text/plain".equals(type)) {
			memoVO.setContents(intent.getStringExtra(Intent.EXTRA_TEXT));    // 가져온 인텐트의 텍스트 정보
			if(intent.hasExtra(Intent.EXTRA_TITLE))
				memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
			else
				memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_SUBJECT));
		}
		else{
			memoVO.setContents(intent.getStringExtra(Intent.EXTRA_TEXT));    // 가져온 인텐트의 텍스트 정보
			if(intent.hasExtra(Intent.EXTRA_TITLE))
				memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_TITLE));
			else
				memoVO.setTitle(intent.getStringExtra(Intent.EXTRA_SUBJECT));

			Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
			if (uri != null) {
				/*String name = FileHelper.getNameFromUri(mCtx , uri);
				String fileName = FileHelper.getNameFromUri(getActivity(), uri);
				new AttachmentTask(mCtx, null, uri, fileName, this).execute();*/
				ArrayList<FileVO> arrFile = new ArrayList<>();
				FileVO fVO = new FileVO();
				fVO.setUriPath(uri.getPath());
				fVO.setUri(uri.toString());
				arrFile.add(fVO);
				memoVO.setFileList(arrFile);
			}
		}
		mFragment = new MemoFragment();

		bundle.putSerializable(Const.PARAM.MEMO_VO, memoVO);
		bundle.putSerializable(Const.MEMO.MEMO_INTERFACE_CODE.SHARE_MEMO_MODE, true);

		mFragment.setArguments(bundle);


		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.replace(R.id.main_container, mFragment).commit();
		//ft.addToBackStack(null).commit();
		//fragment.setTargetFragment(this, Const.MEMO.MEMO_INTERFACE_CODE.ADD_MEMO_CODE);

		CommonUtils.logCustomEvent("ShareActivity", "1");
	}

	public void finishActivity4(){
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.beginTransaction().remove(mFragment);
		finish();
	}

	@Override
	public void onFragmentInteraction(Uri uri) {

	}

	 @Override
	 public void onAttachingFileErrorOccurred(FileVO mAttachment) {

	 }

	 @Override
	 public void onAttachingFileFinished(FileVO mAttachment) {

	 }
 }
